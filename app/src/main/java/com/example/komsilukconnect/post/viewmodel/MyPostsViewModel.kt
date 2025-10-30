package com.example.komsilukconnect.post.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.komsilukconnect.auth.data.AuthRepository
import com.example.komsilukconnect.auth.data.AuthRepositoryImpl
import com.example.komsilukconnect.post.data.Applicant
import com.example.komsilukconnect.post.data.PostModel
import com.example.komsilukconnect.post.data.PostRepository
import com.example.komsilukconnect.post.data.PostRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class MyPostItemState(
    val post: PostModel,
    val applicants: List<Applicant> = emptyList()
)

data class MyPostsUiState(
    val myPosts: List<MyPostItemState> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MyPostsViewModel : ViewModel() {
    private val postRepository: PostRepository = PostRepositoryImpl()
    private val authRepository: AuthRepository = AuthRepositoryImpl()

    private val _uiState = MutableStateFlow(MyPostsUiState())
    val uiState = _uiState.asStateFlow()

    fun onLogout() {
        authRepository.logout()
    }
    init {
        loadMyPosts()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadMyPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            postRepository.getPostsByCurrentUser()
                .flatMapLatest { myPosts ->
                    if (myPosts.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        val listOfFlows = myPosts.map { post ->
                            postRepository.getApplicantsForPost(post.id).map { applicants ->
                                MyPostItemState(post, applicants)
                            }
                        }
                        combine(listOfFlows) { arrayOfMyPostItemState ->
                            arrayOfMyPostItemState.toList()
                        }
                    }
                }
                .collect { combinedList ->
                    _uiState.value = MyPostsUiState(myPosts = combinedList, isLoading = false)
                }
        }
    }

    fun completeInteraction(post: PostModel, applicant: Applicant) {
        viewModelScope.launch {
            postRepository.completeInteraction(post, applicant)
        }
    }
}