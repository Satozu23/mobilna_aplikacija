package com.example.komsilukconnect.post.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.komsilukconnect.post.data.PostModel
import com.example.komsilukconnect.post.data.PostRepository
import com.example.komsilukconnect.post.data.PostRepositoryImpl
import com.example.komsilukconnect.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PostDetailsEvent {
    object ApplyToPost : PostDetailsEvent()
    object SuccessMessageShown : PostDetailsEvent()
}
data class PostDetailsUiState(
    val post: PostModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val applySuccess: Boolean = false
)

class PostDetailsViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postRepository: PostRepository = PostRepositoryImpl()

    private val _uiState = MutableStateFlow(PostDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val postId: String = savedStateHandle.get<String>("postId")!!

    init {
        loadPostDetails()
    }

    private fun loadPostDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = postRepository.getPostById(postId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(post = result.data, isLoading = false)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.exception.message, isLoading = false)
                }
            }
        }
    }
    fun onEvent(event: PostDetailsEvent) {
        when (event) {
            is PostDetailsEvent.ApplyToPost -> {
                applyToPost()
            }
            is PostDetailsEvent.SuccessMessageShown -> {
                _uiState.value = _uiState.value.copy(applySuccess = false)
            }
        }
    }

    private fun applyToPost() {
        _uiState.value.post?.let { postToApply ->
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, applySuccess = false)

                when (val result = postRepository.applyToPost(postToApply)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, applySuccess = true)
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.exception.message)
                    }
                }
            }
        }
    }
}