package com.kire.market_place_android.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.kire.market_place_android.domain.model.order.OrderDomain
import com.kire.market_place_android.domain.model.pick_up_point.PickUpPointDomain
import com.kire.market_place_android.domain.use_case.manager.util.IManagerUseCases

import com.kire.market_place_android.presentation.mapper.order.toPresentation
import com.kire.market_place_android.presentation.mapper.pick_up_point.toPresentation
import com.kire.market_place_android.presentation.mapper.toPresentation
import com.kire.market_place_android.presentation.model.IRequestResult
import com.kire.market_place_android.presentation.model.manager.ManagerOrderState
import com.kire.market_place_android.presentation.model.manager.ManagerOrderUiEvent
import com.kire.market_place_android.presentation.model.order.Order
import com.kire.market_place_android.presentation.model.pick_up_point.PickUpPoint

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class ManagerViewModel @Inject constructor(
    private val managerUseCases: IManagerUseCases
) : ViewModel() {

    private val _requestResult: MutableStateFlow<IRequestResult> =
        MutableStateFlow(IRequestResult.Idle)
    val requestResult: StateFlow<IRequestResult> = _requestResult.asStateFlow()

    private val _order: MutableStateFlow<Order> = MutableStateFlow(Order())
    val order: StateFlow<Order> = _order.asStateFlow()

    private val _pickUpPoint: MutableStateFlow<PickUpPoint> = MutableStateFlow(PickUpPoint())
    val pickUpPoint: StateFlow<PickUpPoint> = _pickUpPoint.asStateFlow()

    private val _managerOrderState: MutableStateFlow<ManagerOrderState> =
        MutableStateFlow(ManagerOrderState())
    val managerOrderState: StateFlow<ManagerOrderState> = _managerOrderState.asStateFlow()

    fun onEvent(event: ManagerOrderUiEvent) {
        when (event) {

            is ManagerOrderUiEvent.productSelect -> selectProduct(event.id)

            ManagerOrderUiEvent.confirmOrder -> confirmOrder(
                id = _order.value.orderId,
                received = _managerOrderState.value.received,
                returned = _managerOrderState.value.returned
            )
        }
    }

    private fun updateManagerOrderState(update: ManagerOrderState.() -> ManagerOrderState) =
        viewModelScope.launch(Dispatchers.Default) {
            _managerOrderState.value = _managerOrderState.value.update()
        }

    private fun selectProduct(id: Int) = viewModelScope.launch(Dispatchers.Default) {

        val managerOrderStateValue = _managerOrderState.value
        val received = managerOrderStateValue.received
        val returned = managerOrderStateValue.returned

        updateManagerOrderState {
            if (!received.contains(id))
                copy(
                    received = received.plusElement(id),
                    returned = returned.minusElement(id)
                )
            else
                copy(
                    received = received.minusElement(id),
                    returned = returned.plus(id)
                )
        }
    }

    fun makeRequestResultIdle() = viewModelScope.launch(Dispatchers.Default) {
        _requestResult.value = IRequestResult.Idle
    }

    fun getOrderedProductsByOrderId(id: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value = managerUseCases.getOrderedProductsByOrderIdUseCase(id = id)
                .toPresentation<OrderDomain>()
                .also { result ->
                    if (result is IRequestResult.Success<*>)
                        _order.value = (result.data as OrderDomain).toPresentation()
                            .also {
                                _managerOrderState.value =
                                    _managerOrderState.value.copy(
                                        returned = it.products.map { it.product.id }
                                    )
                            }
                }
        }

    fun getPickUpPointByManagerId(id: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value = managerUseCases.getPickUpPointByManagerId(id = id)
                .toPresentation<PickUpPointDomain>()
                .also { result ->
                    if (result is IRequestResult.Success<*>) {
                        _pickUpPoint.value = (result.data as PickUpPointDomain).toPresentation()
                    }
                }
        }

    private fun confirmOrder(id: Int, received: List<Int>, returned: List<Int>) =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value = managerUseCases.confirmOrderUseCase(
                id = id,
                received = received,
                returned = returned
            ).toPresentation<Nothing>()
        }
}