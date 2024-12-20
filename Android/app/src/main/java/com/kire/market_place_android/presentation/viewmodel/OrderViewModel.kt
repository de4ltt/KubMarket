package com.kire.market_place_android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kire.market_place_android.domain.model.order.OrderDomain
import com.kire.market_place_android.domain.use_case.common.util.CommonUseCases
import com.kire.market_place_android.presentation.mapper.order.toDomain
import com.kire.market_place_android.presentation.mapper.order.toPresentation
import com.kire.market_place_android.presentation.mapper.product.toListOrderedProduct
import com.kire.market_place_android.presentation.mapper.toPresentation
import com.kire.market_place_android.presentation.model.IRequestResult
import com.kire.market_place_android.presentation.model.order.Order
import com.kire.market_place_android.presentation.model.order.OrderedProduct
import com.kire.market_place_android.presentation.model.product.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val commonUseCases: CommonUseCases
): ViewModel() {

    private val _requestResult: MutableStateFlow<IRequestResult> = MutableStateFlow(IRequestResult.Idle)
    val requestResult: StateFlow<IRequestResult> = _requestResult.asStateFlow()

    private val _orders: MutableStateFlow<List<Order>> = MutableStateFlow(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    fun getOrders() =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value = commonUseCases.getOrdersUseCase().toPresentation<List<OrderDomain>>()
                .also { result ->
                    if (result is IRequestResult.Success<*>)
                        _orders.value = (result.data as List<*>).map {
                            (it as OrderDomain).toPresentation()
                        }
                }
        }

    fun makeRequestResultIdle() = viewModelScope.launch(Dispatchers.Default) {
        _requestResult.value = IRequestResult.Idle
    }

    fun createOrder(pickUpPointId: Int, orderedProducts: List<Product>, orderPrice: BigDecimal) =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value = commonUseCases.createOrderUseCase(
                pickUpPointId = pickUpPointId,
                orderedProducts = orderedProducts.toListOrderedProduct().toDomain(),
                orderPrice = orderPrice
            ).toPresentation<Nothing>()
            getOrders()
        }
}