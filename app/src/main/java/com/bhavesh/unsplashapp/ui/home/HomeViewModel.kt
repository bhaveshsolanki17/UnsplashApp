package com.bhavesh.unsplashapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavesh.unsplashapp.data.model.FavoriteImage
import com.bhavesh.unsplashapp.data.model.UnsplashImage
import com.bhavesh.unsplashapp.data.repository.ImageRepository
import com.bhavesh.unsplashapp.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    init {
        fetchImages()
        observeFavorites()
    }

    private fun fetchImages() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = repository.getImages()
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Unexpected error")
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

    fun toggleFavorite(image: UnsplashImage) {
        viewModelScope.launch {
            if (_favoriteIds.value.contains(image.id)) {
                repository.removeFromFavorites(
                    FavoriteImage(image.id, image.urls.thumb, image.user.name)
                )
            } else {
                repository.addToFavorites(
                    FavoriteImage(image.id, image.urls.thumb, image.user.name)
                )
            }
        }
    }
}