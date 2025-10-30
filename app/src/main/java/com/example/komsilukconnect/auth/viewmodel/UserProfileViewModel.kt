package com.example.komsilukconnect.auth.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.komsilukconnect.auth.data.ReviewModel
import com.example.komsilukconnect.auth.data.UserModel
import com.example.komsilukconnect.auth.data.UserRepository
import com.example.komsilukconnect.auth.data.UserRepositoryImpl
import com.example.komsilukconnect.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val user: UserModel? = null,
    val reviews: List<ReviewModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class UserProfileViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val userRepository: UserRepository = UserRepositoryImpl()
    private val userId: String = savedStateHandle.get<String>("userId")!!

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val userResult = userRepository.getUserById(userId)
            if (userResult is Result.Success) {
                userRepository.getReviewsForUser(userId).collect { reviews ->
                    _uiState.value = UserProfileUiState(
                        user = userResult.data,
                        reviews = reviews,
                        isLoading = false
                    )
                }
            } else if (userResult is Result.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = userResult.exception.message, isLoading = false)
            }
        }
    }

    fun addReview(rating: Float, comment: String) {
        viewModelScope.launch {
            userRepository.addReview(userId, rating, comment)
        }
    }
}