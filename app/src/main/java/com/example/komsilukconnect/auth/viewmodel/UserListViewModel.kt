package com.example.komsilukconnect.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.komsilukconnect.auth.data.UserModel
import com.example.komsilukconnect.auth.data.UserRepository
import com.example.komsilukconnect.auth.data.UserRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserListUiState(
    val users: List<UserModel> = emptyList(),
    val isLoading: Boolean = false
)

class UserListViewModel : ViewModel() {
    private val userRepository: UserRepository = UserRepositoryImpl()
    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepository.getAllUsers().collect { users ->
                _uiState.value = _uiState.value.copy(users = users, isLoading = false)
            }
        }
    }
}