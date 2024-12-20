package com.kire.market_place_android.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kire.market_place_android.domain.model.admin.AdminUserInfoDomain
import com.kire.market_place_android.domain.model.pick_up_point.PickUpPointDomain

import com.kire.market_place_android.domain.use_case.admin.util.IAdminUseCases
import com.kire.market_place_android.presentation.mapper.admin.toPresentation
import com.kire.market_place_android.presentation.mapper.pick_up_point.toPresentation
import com.kire.market_place_android.presentation.mapper.toPresentation
import com.kire.market_place_android.presentation.model.IRequestResult
import com.kire.market_place_android.presentation.model.pick_up_point.PickUpPoint
import com.kire.market_place_android.presentation.model.admin.AdminPickUpPointState
import com.kire.market_place_android.presentation.model.admin.AdminPickUpPointUiEvent
import com.kire.market_place_android.presentation.model.admin.AdminUserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminUseCases: IAdminUseCases
) : ViewModel() {

    private val _allUsers: MutableStateFlow<List<AdminUserInfo>> = MutableStateFlow(emptyList())
    val allUsers: StateFlow<List<AdminUserInfo>> = _allUsers.asStateFlow()

    private val _allPickUpPoints: MutableStateFlow<List<PickUpPoint>> =
        MutableStateFlow(emptyList())
    val allPickUpPoints: StateFlow<List<PickUpPoint>> = _allPickUpPoints.asStateFlow()

    private val _requestResult: MutableStateFlow<IRequestResult> =
        MutableStateFlow(IRequestResult.Idle)
    val requestResult: StateFlow<IRequestResult> = _requestResult.asStateFlow()

    var adminPickUpPointState by mutableStateOf(AdminPickUpPointState())
        private set

    fun onEvent(event: AdminPickUpPointUiEvent) {
        when (event) {

            is AdminPickUpPointUiEvent.ChangeOnDismissRequest -> updateAdminPickUpPointState {
                copy(onDismissRequest = event.value)
            }

            is AdminPickUpPointUiEvent.DeletePickUpPoint -> {
                viewModelScope.launch(Dispatchers.IO) {
                    deletePickUpPoint(id = event.id).join()
                    getAllPickUpPoints()
                }
            }

            is AdminPickUpPointUiEvent.CreatePickUpPoint -> {
                viewModelScope.launch(Dispatchers.IO) {
                    createPickUpPoint(managerId = event.managerId, address = event.address).join()
                    getAllPickUpPoints()
                }

            }

            is AdminPickUpPointUiEvent.ChangeIsCreateBottomBarExpanded -> updateAdminPickUpPointState {
                copy(
                    isCreateBottomBarExpanded = event.value,
                    bottomBarManagerId = ""
                )
            }

            is AdminPickUpPointUiEvent.ChangeIsUpdateBottomBarExpanded -> updateAdminPickUpPointState {
                copy(
                    isUpdateBottomBarExpanded = event.value,
                    pickUpPointToUpdateId = event.pickUpPointToUpdateId,
                    bottomBarAddress = event.address,
                    bottomBarManagerId = event.managerId
                )
            }

            is AdminPickUpPointUiEvent.bottomBarManagerIdChanged -> updateAdminPickUpPointState {
                copy(bottomBarManagerId = event.value)
            }

            is AdminPickUpPointUiEvent.bottomBarAddressChanged -> updateAdminPickUpPointState {
                copy(bottomBarAddress = event.value)
            }

            is AdminPickUpPointUiEvent.UpdatePickUpPoint -> {
                viewModelScope.launch(Dispatchers.IO) {
                    updatePickUpPoint(
                        id = event.pickUpPointToUpdateId,
                        managerId = event.managerId,
                        address = event.address
                    ).join()
                    getAllPickUpPoints()
                }
            }
        }
    }

    fun makeRequestResultIdle() = viewModelScope.launch(Dispatchers.Default) {
        _requestResult.value = IRequestResult.Idle
    }

    fun getAllUsers() =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value =
                adminUseCases.getAllUsersUseCase().toPresentation<List<AdminUserInfoDomain>>()
                    .also { result ->
                        if (result is IRequestResult.Success<*>)
                            _allUsers.value = (result.data as List<*>).map {
                                (it as AdminUserInfoDomain).toPresentation()
                            }
                    }
        }

    private fun getAllPickUpPoints() =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value =
                adminUseCases.getAllPickUpPointsUseCase().toPresentation<List<PickUpPointDomain>>()
                    .also { result ->
                        if (result is IRequestResult.Success<*>)
                            _allPickUpPoints.value = (result.data as List<*>).map {
                                (it as PickUpPointDomain).toPresentation()
                            }
                    }
        }

    fun createPickUpPoint(managerId: Int, address: String) =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value =
                adminUseCases.createPickUpPointUseCase(managerId = managerId, address = address)
                    .toPresentation<Nothing>()
        }

    fun updatePickUpPoint(id: Int, address: String, managerId: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value = adminUseCases.updatePickUpPointUseCase(
                id = id,
                address = address,
                managerId = managerId
            ).toPresentation<Nothing>()
        }

    fun deletePickUpPoint(id: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value =
                adminUseCases.deletePickUpPointUseCase(id = id).toPresentation<Nothing>()
        }

    private fun updateAdminPickUpPointState(update: AdminPickUpPointState.() -> AdminPickUpPointState) =
        viewModelScope.launch(Dispatchers.Default) {
            adminPickUpPointState = adminPickUpPointState.update()
        }
}