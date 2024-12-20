package com.kire.market_place_android.presentation.model.product

import com.kire.market_place_android.presentation.util.VProductType

sealed interface ItemFields {
    var value: String
    val category: VProductType
}

sealed class ProductUiEvent {

    data class ChangeItemName(
        override var value: String,
        override val category: VProductType
    ) : ProductUiEvent(), ItemFields

    data class ChangeItemCategory(
        override var value: String,
        override val category: VProductType
    ) : ProductUiEvent(), ItemFields

    data class ChangeItemPrice(
        override var value: String,
        override val category: VProductType
    ) : ProductUiEvent(), ItemFields

    data class ChangeItemDiscountPrice(
        override var value: String,
        override val category: VProductType
    ) : ProductUiEvent(), ItemFields

    data class ChangeItemMeasure(
        override var value: String,
        override val category: VProductType
    ) : ProductUiEvent(), ItemFields

    data class ChangeItemStored(
        override var value: String,
        override val category: VProductType
    ) : ProductUiEvent(), ItemFields

    data class ChangeItemDescription(
        override var value: String,
        override val category: VProductType
    ) : ProductUiEvent(), ItemFields

    data class SelectItem(val item: Product) : ProductUiEvent()
    data class AddItem(var image: ByteArray, var item: Product) : ProductUiEvent()
    data class DeleteItem(var item: Product) : ProductUiEvent()
}