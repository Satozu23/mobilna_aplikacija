package com.example.komsilukconnect.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.komsilukconnect.post.data.PostModel
import com.example.komsilukconnect.post.data.PostRepository
import com.example.komsilukconnect.post.data.PostRepositoryImpl
import com.example.komsilukconnect.post.data.PostType
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.example.komsilukconnect.utils.calculateDistance

data class HomeUiState(
    val allPosts: List<PostModel> = emptyList(),
    val filteredPosts: List<PostModel> = emptyList(),
    val selectedFilter: PostType? = null,
    val searchQuery: String = "",
    val searchRadiusKm: Float = 1.0f,
    val isLoading: Boolean = false,
    val newNearbyPost: PostModel? = null
)

sealed class HomeEvent {
    data class OnFilterChanged(val filter: PostType?) : HomeEvent()
    data class OnSearchQueryChanged(val query: String) : HomeEvent()
    data class OnRadiusChanged(val radius: Float) : HomeEvent()
    object NotificationShown : HomeEvent()
}

class HomeViewModel : ViewModel() {

    private val postRepository: PostRepository = PostRepositoryImpl()
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val notifiedPostIds = mutableSetOf<String>()

    init {
        loadAllPosts()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnFilterChanged -> {
                _uiState.value = _uiState.value.copy(selectedFilter = event.filter)
            }
            is HomeEvent.OnSearchQueryChanged -> {
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
            }
            is HomeEvent.OnRadiusChanged -> {
                _uiState.value = _uiState.value.copy(searchRadiusKm = event.radius)
            }
            is HomeEvent.NotificationShown -> {
                _uiState.value = _uiState.value.copy(newNearbyPost = null)
            }
        }
        applyFilters()
    }

    fun checkForNewNearbyPosts(userLocation: LatLng) {
        val radiusInKm = _uiState.value.searchRadiusKm.toDouble()
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid == null) return

        val nearbyPosts = _uiState.value.allPosts.filter { post ->
            post.location?.let { postLocation ->
                calculateDistance(userLocation, LatLng(postLocation.latitude, postLocation.longitude)) <= radiusInKm
            } ?: false
        }

        val newPostToShow = nearbyPosts.firstOrNull { post ->
            post.id !in notifiedPostIds && post.authorUid != currentUserUid
        }

        newPostToShow?.let {
            notifiedPostIds.add(it.id)
            _uiState.value = _uiState.value.copy(newNearbyPost = it)
        }
    }

    private fun loadAllPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            postRepository.getAllPosts().collect { posts ->
                _uiState.value = _uiState.value.copy(allPosts = posts, isLoading = false)
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val state = _uiState.value
        val typeFilteredList = when (state.selectedFilter) {
            null -> state.allPosts
            else -> state.allPosts.filter { it.type == state.selectedFilter }
        }
        val finalList = if (state.searchQuery.isBlank()) {
            typeFilteredList
        } else {
            typeFilteredList.filter {
                it.title.contains(state.searchQuery, ignoreCase = true) ||
                        it.description.contains(state.searchQuery, ignoreCase = true)
            }
        }
        _uiState.value = state.copy(filteredPosts = finalList)
    }
}