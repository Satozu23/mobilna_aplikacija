package com.example.komsilukconnect.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.komsilukconnect.auth.data.AuthRepository
import com.example.komsilukconnect.auth.data.AuthRepositoryImpl
import com.example.komsilukconnect.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri

class AuthViewModel : ViewModel() {

    private val authRepository: AuthRepository = AuthRepositoryImpl()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {

            is AuthEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(email = event.email)
            }
            is AuthEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(password = event.password)
            }
            is AuthEvent.FirstNameChanged -> {
                _uiState.value = _uiState.value.copy(firstName = event.firstName)
            }
            is AuthEvent.LastNameChanged -> {
                _uiState.value = _uiState.value.copy(lastName = event.lastName)
            }
            is AuthEvent.PhoneNumberChanged -> {
                _uiState.value = _uiState.value.copy(phoneNumber = event.phoneNumber)
            }
            is AuthEvent.ImageUriChanged -> {
                _uiState.value = _uiState.value.copy(imageUri = event.uri)
            }

            is AuthEvent.Login -> {
                loginUser()
            }
            is AuthEvent.Register -> {
            registerUser(_uiState.value.imageUri)
        }

            is AuthEvent.NavigationHandled -> {
                _uiState.value = _uiState.value.copy(isSuccess = false, errorMessage = null)
            }
        }
    }

    private fun registerUser(imageUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = authRepository.registerUser(_uiState.value, imageUri)

            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
            }
        }
    }

    private fun loginUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = authRepository.loginUser(_uiState.value)

            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message
                    )
                }
            }
        }
    }
}