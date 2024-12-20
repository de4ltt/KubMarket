package com.kire.market_place_android.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kire.market_place_android.domain.model.auth.AuthResultDomain
import com.kire.market_place_android.domain.use_case.common.util.IAuthUseCases
import com.kire.market_place_android.presentation.model.auth.AuthState
import com.kire.market_place_android.presentation.model.auth.AuthUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCases: IAuthUseCases
) : ViewModel() {

    var authState by mutableStateOf(AuthState())
        private set

    private val _authResultDomainChannel = Channel<AuthResultDomain<List<String?>>>()
    val authResultChannel = _authResultDomainChannel.receiveAsFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {

            is AuthUiEvent.LogInPhoneChanged -> updateAuthState {
                copy(logInPhone = event.value)
            }
            is AuthUiEvent.LogInPasswordChanged -> updateAuthState {
                copy(logInPassword = event.value)
            }
            is AuthUiEvent.LogOnNameChanged -> updateAuthState {
                copy(logOnName = event.value)
            }
            is AuthUiEvent.LogOnPhoneChanged -> updateAuthState {
                copy(logOnPhone = event.value)
            }
            is AuthUiEvent.LogOnEmailChanged -> updateAuthState {
                copy(logOnEmail = event.value)
            }
            is AuthUiEvent.LogOnPasswordChanged -> updateAuthState {
                copy(logOnPassword = event.value)
            }
            is AuthUiEvent.LogOnRepeatedPasswordChanged -> updateAuthState {
                copy(logOnRepeatedPassword = event.value)
            }

            AuthUiEvent.LogIn -> logIn()
            AuthUiEvent.LogOn -> logOn()
        }
    }

    suspend fun isTokenExpired() = withContext(Dispatchers.IO) {
        authUseCases.isTokenExpiredUseCase()
    }

    private fun logOn() = viewModelScope.launch(Dispatchers.IO) {

        updateAuthState {
            copy(isLoading = true)
        }

        val result = authUseCases.logOnUseCase(
            username = authState.logOnName,
            phone = authState.logOnPhone,
            email = authState.logOnEmail,
            password = authState.logOnPassword
        )

        _authResultDomainChannel.send(result)

        updateAuthState {
            copy(isLoading = false)
        }
    }

    private fun logIn() = viewModelScope.launch(Dispatchers.IO) {

        updateAuthState {
            copy(isLoading = true)
        }

        val result = authUseCases.logInUseCase(
            phone = authState.logInPhone,
            password = authState.logInPassword
        )

        _authResultDomainChannel.send(result)

        updateAuthState {
            copy(isLoading = false)
        }
    }

    private fun updateAuthState(update: AuthState.() -> AuthState) =
        viewModelScope.launch(Dispatchers.Default) {
            authState = authState.update()
        }

    init {

        viewModelScope.launch(Dispatchers.IO) {
            authUseCases.isAuthenticatedUseCase().collect {
                _isAuthenticated.value = it
            }
        }
    }
}