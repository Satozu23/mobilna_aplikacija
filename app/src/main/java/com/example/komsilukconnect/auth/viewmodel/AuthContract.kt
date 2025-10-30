package com.example.komsilukconnect.auth.viewmodel
import android.net.Uri
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val imageUri: Uri? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

sealed class AuthEvent {
    data class EmailChanged(val email: String) : AuthEvent()
    data class PasswordChanged(val password: String) : AuthEvent()
    data class FirstNameChanged(val firstName: String) : AuthEvent()
    data class LastNameChanged(val lastName: String) : AuthEvent()
    data class PhoneNumberChanged(val phoneNumber: String) : AuthEvent()
    data class ImageUriChanged(val uri: Uri?) : AuthEvent()
    object Login : AuthEvent()
    object Register : AuthEvent()
    object NavigationHandled : AuthEvent()
}