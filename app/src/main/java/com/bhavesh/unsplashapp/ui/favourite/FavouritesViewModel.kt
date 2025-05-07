package com.bhavesh.unsplashapp.ui.favourite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavesh.unsplashapp.data.model.FavoriteImage
import com.bhavesh.unsplashapp.data.repository.ImageRepository
import com.bhavesh.unsplashapp.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<FavoriteImage>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<FavoriteImage>>> = _uiState

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            repository.getFavoriteImages()
                .catch { e ->
                    _uiState.value = UiState.Error(e.localizedMessage ?: "Unexpected error")
                }
                .collectLatest { images ->
                    _uiState.value = UiState.Success(images)
                }
        }
    }

    fun removeFavorite(image: FavoriteImage) {
        viewModelScope.launch {
            repository.removeFromFavorites(image)
        }
    }
}