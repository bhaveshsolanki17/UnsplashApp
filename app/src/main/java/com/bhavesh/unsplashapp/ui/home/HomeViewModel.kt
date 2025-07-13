package com.bhavesh.unsplashapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavesh.unsplashapp.data.model.FavoriteImage
import com.bhavesh.unsplashapp.data.model.UnsplashImage
import com.bhavesh.unsplashapp.data.repository.ImageRepository
import com.bhavesh.unsplashapp.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<UnsplashImage>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<UnsplashImage>>> = _uiState

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds

    private val searchQuery = MutableStateFlow("")
    private val currentPage = MutableStateFlow(1)
    private val currentSearchPage = MutableStateFlow(1)
    private var isLoadingMore = false

    init {
        observeFavorites()
        observeSearchFlow()
        observeImageFlow()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        currentSearchPage.value = 1 // Reset search page
    }

    fun loadNextPage() {
        if (!isLoadingMore) {
            if (searchQuery.value.isBlank()) {
                currentPage.value += 1
            } else {
                currentSearchPage.value += 1
            }
        }
    }

    fun toggleFavorite(image: UnsplashImage) {
        viewModelScope.launch {
            val fav = FavoriteImage(image.id, image.urls.thumb, image.user.name)
            if (_favoriteIds.value.contains(image.id)) {
                repository.removeFromFavorites(fav)
            } else {
                repository.addToFavorites(fav)
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.getFavoriteImages().collect {
                _favoriteIds.value = it.map { img -> img.id }.toSet()
            }
        }
    }

    // 🔍 For search
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun observeSearchFlow() {
        viewModelScope.launch {
            combine(
                searchQuery.debounce(400).distinctUntilChanged(),
                currentSearchPage
            ) { query, page -> query.trim() to page }
                .filter { (query, _) -> query.isNotEmpty() }
                .flatMapLatest { (query, page) ->
                    fetchImagesFlow(query, page, isSearch = true)
                }
                .collectLatest { _uiState.value = it }
        }
    }

    // For getImages
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeImageFlow() {
        viewModelScope.launch {
            currentPage
                .filter { searchQuery.value.isBlank() }
                .flatMapLatest { page ->
                    fetchImagesFlow("", page, isSearch = false)
                }
                .collectLatest { _uiState.value = it }
        }
    }

    private fun fetchImagesFlow(
        query: String,
        page: Int,
        isSearch: Boolean
    ): Flow<UiState<List<UnsplashImage>>> =
        flow {
            isLoadingMore = true
            if (page == 1) emit(UiState.Loading)

            try {
                val result = if (isSearch) {
                    repository.searchImages(query, page)
                } else {
                    repository.getImages(page)
                }

                val currentList = (_uiState.value as? UiState.Success)?.data.orEmpty()
                val combined = if (page == 1) result else currentList + result
                emit(UiState.Success(combined))
            } catch (e: Exception) {
                emit(UiState.Error(e.localizedMessage ?: "Unexpected error"))
            } finally {
                isLoadingMore = false
            }
        }
}