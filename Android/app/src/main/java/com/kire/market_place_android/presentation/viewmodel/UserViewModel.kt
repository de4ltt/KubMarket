package com.kire.market_place_android.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kire.market_place_android.domain.model.pick_up_point.PickUpPointDomain
import com.kire.market_place_android.domain.model.user.UserDomain

import com.kire.market_place_android.domain.use_case.common.util.ICommonUseCases
import com.kire.market_place_android.presentation.mapper.pick_up_point.toPresentation
import com.kire.market_place_android.presentation.mapper.toPresentation
import com.kire.market_place_android.presentation.mapper.user.toPresentation
import com.kire.market_place_android.presentation.model.IRequestResult
import com.kire.market_place_android.presentation.model.pick_up_point.PickUpPoint
import com.kire.market_place_android.presentation.model.user.Role
import com.kire.market_place_android.presentation.model.user.ProfileState
import com.kire.market_place_android.presentation.model.user.UserUiEvent

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val commonUseCases: ICommonUseCases
) : ViewModel() {

    private val _role = MutableStateFlow(Role.USER)
    val role: StateFlow<Role> = _role.asStateFlow()

    private val _userId = MutableStateFlow(0)
    val userId: StateFlow<Int> = _userId.asStateFlow()

    private val _requestResult: MutableStateFlow<IRequestResult> =
        MutableStateFlow(IRequestResult.Idle)
    val requestResult: StateFlow<IRequestResult> = _requestResult.asStateFlow()

    var profileState by mutableStateOf(ProfileState())
        private set

    private val _chosenPickUpPoint: MutableStateFlow<PickUpPoint> = MutableStateFlow(PickUpPoint())
    val chosenPickUpPoint: StateFlow<PickUpPoint> = _chosenPickUpPoint.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _allPickUpPoints: MutableStateFlow<List<PickUpPoint>> =
        MutableStateFlow(emptyList())
    val allPickUpPoints: StateFlow<List<PickUpPoint>> = _allPickUpPoints.asStateFlow()

    fun onEvent(event: UserUiEvent) {
        when (event) {

            is UserUiEvent.UsernameChanged -> updateProfileState {
                copy(username = event.value)
            }

            is UserUiEvent.PhoneChanged -> updateProfileState {
                copy(phone = event.value)
            }

            is UserUiEvent.EmailChanged -> updateProfileState {
                copy(email = event.value)
            }

            is UserUiEvent.CardNumberChanged -> updateProfileState {
                copy(cardNumber = event.value)
            }

            is UserUiEvent.CvcChanged -> updateProfileState {
                copy(CVC = event.value)
            }

            is UserUiEvent.ValidityChanged -> updateProfileState {
                copy(validity = event.value)
            }

            is UserUiEvent.CurrentPasswordChanged -> updateProfileState {
                copy(currentPassword = event.value)
            }

            is UserUiEvent.NewPasswordChanged -> updateProfileState {
                copy(newPassword = event.value)
            }

            is UserUiEvent.ConfirmationPasswordChanged -> updateProfileState {
                copy(confirmationPassword = event.value)
            }

            UserUiEvent.ChangeUserInfo -> changeUserInfo()
            UserUiEvent.ChangeCard -> changeCard()
            UserUiEvent.ChangePassword -> changePassword()

            is UserUiEvent.ChoosePickUpPoint -> choosePickUpPoint(event.chosenPickUpPoint)
        }
    }

    private fun changeUserInfo() = viewModelScope.launch(Dispatchers.IO) {
        _requestResult.value = commonUseCases.changeUserInfoAndReturnUseCase(
            id = _userId.value,
            username = profileState.username,
            phone = profileState.phone,
            email = profileState.email
        ).toPresentation<Nothing>()
    }

    private fun changeCard() = viewModelScope.launch(Dispatchers.IO) {
        commonUseCases.changeUserCardUseCase(
            cardNumber = profileState.cardNumber,
            CVC = profileState.CVC,
            validity = profileState.validity
        )
    }

    private fun changePassword() = viewModelScope.launch(Dispatchers.IO) {
        commonUseCases.changePasswordUseCase(
            currentPassword = profileState.currentPassword,
            newPassword = profileState.newPassword,
            confirmationPassword = profileState.confirmationPassword
        )
    }

    private fun choosePickUpPoint(pickUpPoint: PickUpPoint) =
        viewModelScope.launch(Dispatchers.Default) {
            _chosenPickUpPoint.value = pickUpPoint
        }

    fun getAllPickUpPoints() =
        viewModelScope.launch(Dispatchers.IO) {
            _requestResult.value =
                commonUseCases.getAllPickUpPoints().toPresentation<List<PickUpPointDomain>>()
                    .also { result ->
                        if (result is IRequestResult.Success<*>)
                            _allPickUpPoints.value = (result.data as List<*>).map {
                                (it as PickUpPointDomain).toPresentation()
                            }
                    }
        }

    fun makeRequestResultIdle() = viewModelScope.launch(Dispatchers.Default) {
        _requestResult.value = IRequestResult.Idle
    }

    suspend fun logOut() = withContext(Dispatchers.Main) {
        commonUseCases.logOutUseCase()
    }

    fun updateUser() =
        viewModelScope.launch {
            _requestResult.value =
                commonUseCases.getUserInfoUseCase(_userId.value).toPresentation<UserDomain>()
                    .also { result ->
                        if (result is IRequestResult.Success<*>) {
                            val user = (result.data as UserDomain).toPresentation()
                            updateProfileState {
                                copy(
                                    username = user.username,
                                    phone = user.phone,
                                    email = user.email,
                                    cardNumber = user.cardNumber ?: "",
                                    CVC = user.CVC.toString(),
                                    validity = user.validity?.toString() ?: "",
                                    userDiscount = user.userDiscount,
                                    amountSpent = user.amountSpent,
                                    redemptionPercent = user.redemptionPercent
                                )
                            }
                        }
                    }
        }

    private fun updateProfileState(update: ProfileState.() -> ProfileState) =
        viewModelScope.launch(Dispatchers.Default) {
            profileState = profileState.update()
        }

    init {
        viewModelScope.let {

            it.launch(Dispatchers.IO) {
                commonUseCases.getRoleUseCase().collect { roleDomain ->
                    _role.value = roleDomain.toPresentation()
                }
            }

            it.launch(Dispatchers.IO) {
                commonUseCases.getUserIdUseCase().collect { userId ->
                    _userId.value = userId
                }
            }

            it.launch(Dispatchers.IO) {
                commonUseCases.isAuthenticatedUseCase().collect {
                    _isAuthenticated.value = it
                }
            }
        }
    }
}