package com.example.komsilukconnect.ranking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.komsilukconnect.auth.data.UserModel
import com.example.komsilukconnect.ranking.data.RankingRepository
import com.example.komsilukconnect.ranking.data.RankingRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RankingUiState(
    val users: List<UserModel> = emptyList(),
    val isLoading: Boolean = false
)

class RankingViewModel : ViewModel() {
    private val rankingRepository: RankingRepository = RankingRepositoryImpl()
    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            rankingRepository.getUsersRanked().collect { users ->
                _uiState.value = _uiState.value.copy(users = users, isLoading = false)
            }
        }
    }
}