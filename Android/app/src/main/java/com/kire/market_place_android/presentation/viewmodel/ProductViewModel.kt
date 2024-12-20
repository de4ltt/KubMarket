package com.kire.market_place_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kire.market_place_android.domain.model.product.CategoryDomain

import com.kire.market_place_android.domain.model.product.ProductDomain
import com.kire.market_place_android.domain.use_case.admin.util.IAdminUseCases
import com.kire.market_place_android.domain.use_case.common.util.ICommonUseCases
import com.kire.market_place_android.presentation.mapper.product.toDomain
import com.kire.market_place_android.presentation.mapper.product.toPresentation
import com.kire.market_place_android.presentation.mapper.toPresentation
import com.kire.market_place_android.presentation.model.IRequestResult
import com.kire.market_place_android.presentation.model.product.CartState
import com.kire.market_place_android.presentation.model.product.CartUiEvent
import com.kire.market_place_android.presentation.model.product.Category
import com.kire.market_place_android.presentation.model.product.Product
import com.kire.market_place_android.presentation.model.product.ProductUiEvent
import com.kire.market_place_android.presentation.util.VProductType
import com.kire.market_place_android.presentation.util.isProductValid

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val commonUseCases: ICommonUseCases,
    private val adminUseCases: IAdminUseCases
) : ViewModel() {

    private val _requestResult: MutableStateFlow<IRequestResult> =
        MutableStateFlow(IRequestResult.Idle)
    val requestResult: StateFlow<IRequestResult> = _requestResult.asStateFlow()

    private val _allProducts: MutableStateFlow<List<Product>> = MutableStateFlow(emptyList())
    val allProducts: StateFlow<List<Product>> = _allProducts.asStateFlow()

    private val _allCategories: MutableStateFlow<List<Category>> = MutableStateFlow(emptyList())
    val allCategories: StateFlow<List<Category>> = _allCategories.asStateFlow()

    private val _chosenProduct: MutableStateFlow<Product> = MutableStateFlow(Product())
    val chosenProduct: StateFlow<Product> = _chosenProduct.asStateFlow()

    private val _cartState: MutableStateFlow<CartState> = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    fun onEvent(event: CartUiEvent) {
        when (event) {
            is CartUiEvent.productSelect -> selectProduct(event.product)
            is CartUiEvent.chooseQuantity -> chooseQuantity(event.chosenQuantity, event.productId)
            is CartUiEvent.deleteFromCart -> deleteFromCart(event.productId)
            is CartUiEvent.addToCart -> addToCart(event.product)
            is CartUiEvent.changeChosenProduct -> changeChosenProduct(event.chosenProduct)
        }
    }

    fun onEvent(event: ProductUiEvent) {
        when (event) {
            is ProductUiEvent.ChangeItemCategory,
            is ProductUiEvent.ChangeItemDescription,
            is ProductUiEvent.ChangeItemDiscountPrice,
            is ProductUiEvent.ChangeItemMeasure,
            is ProductUiEvent.ChangeItemName,
            is ProductUiEvent.ChangeItemPrice,
            is ProductUiEvent.ChangeItemStored -> updateProductParameters(
                event.value.isProductValid(event.category)
            ) {
                updateChosenProduct { copy(category = event.value) }
            }

            is ProductUiEvent.AddItem -> addProduct(event.image, event.item)
            is ProductUiEvent.DeleteItem -> deleteProduct(event.item)
            is ProductUiEvent.SelectItem -> updateChosenProduct { event.item }
        }
    }


    private fun updateProductParameters(check: Boolean, update: () -> Unit) =
        viewModelScope.launch(Dispatchers.Default) {
            if (check) update()
        }

    private fun changeChosenProduct(product: Product) =
        viewModelScope.launch(Dispatchers.Default) {
            _chosenProduct.value = product
        }

    private fun addToCart(product: Product) {

        updateCartState {

            if (!_cartState.value.allProductsInCart.map { it.id }.contains(product.id))
                copy(
                    allProductsInCart = _cartState.value.allProductsInCart.plusElement(product),
                    toBuy = _cartState.value.toBuy.plusElement(product)
                )
            else
                copy(
                    toBuy = _cartState.value.toBuy.map {
                        if (it.id == product.id)
                            it.copy(chosenQuantity = it.chosenQuantity + product.chosenQuantity)
                        else it
                    }
                )
        }
    }

    private fun chooseQuantity(chosenQuantity: Int, productId: Int) {
        updateCartState {
            copy(
                toBuy = _cartState.value.toBuy.map {
                    if (it.id == productId)
                        it.copy(chosenQuantity = chosenQuantity)
                    else it
                }
            )
        }
    }

    private fun deleteFromCart(productId: Int) {
        updateCartState {
            copy(
                allProductsInCart = _cartState.value.allProductsInCart.filter { it.id != productId },
                toBuy = _cartState.value.toBuy.filter { it.id != productId }
            )
        }
    }

    private fun selectProduct(product: Product) {

        val cartStateValue = _cartState.value

        updateCartState {

            copy(
                toBuy =
                if (!cartStateValue.toBuy.map { it.id }.contains(product.id))
                    cartStateValue.toBuy.plusElement(product)
                else
                    cartStateValue.toBuy.filter { it.id != product.id }
            )
        }
    }

    fun makeRequestResultIdle() = viewModelScope.launch(Dispatchers.Default) {
        _requestResult.value = IRequestResult.Idle
    }

    fun refreshProducts() =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value =
                commonUseCases.getAllProductsUseCase().toPresentation<List<ProductDomain>>()
                    .also { result ->
                        if (result is IRequestResult.Success<*>)
                            _allProducts.value = (result.data as List<*>).map {
                                (it as ProductDomain).toPresentation()
                            }
                    }
        }

    fun getAllCategories() =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value =
                commonUseCases.getAllAvailableCategoriesUseCase()
                    .toPresentation<List<CategoryDomain>>()
                    .also { result ->
                        if (result is IRequestResult.Success<*>)
                            _allCategories.value = (result.data as List<*>).map {
                                (it as CategoryDomain).toPresentation()
                            }
                    }
        }

    fun updateProductById(id: Int, image: ByteArray, product: Product) =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value = adminUseCases.updateProductUseCase(
                id = id,
                image = image,
                product = product.toDomain()
            ).toPresentation<ProductDomain>()
        }

    fun addProduct(image: ByteArray, product: Product) =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value = adminUseCases.addProductUseCase(
                image = image,
                product = product.toDomain()
            ).toPresentation<Nothing>()
        }

    fun deleteProduct(product: Product) =
        viewModelScope.launch(Dispatchers.IO) {
            //TODO
        }

    private fun updateCartState(update: CartState.() -> CartState) =
        viewModelScope.launch(Dispatchers.Default) {
            _cartState.value = _cartState.value.update()
        }

    private fun updateChosenProduct(update: Product.() -> Product) =
        viewModelScope.launch(Dispatchers.Default) {
            _chosenProduct.value = _chosenProduct.value.update()
        }
}