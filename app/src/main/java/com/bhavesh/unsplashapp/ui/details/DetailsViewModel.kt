package com.bhavesh.unsplashapp.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhavesh.unsplashapp.data.model.UnsplashImage
import com.bhavesh.unsplashapp.data.repository.ImageRepository
import com.bhavesh.unsplashapp.ui.routes.AppRoutes
import com.bhavesh.unsplashapp.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: ImageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val imageId: String = checkNotNull(savedStateHandle[AppRoutes.PARAM_IMAGE_ID])

    private val _uiState = MutableStateFlow<UiState<UnsplashImage>>(UiState.Loading)
    val uiState: StateFlow<UiState<UnsplashImage>> = _uiState

    init {
        fetchPhotoDetails(imageId)
    }

    private fun fetchPhotoDetails(id: String) {
        viewModelScope.launch {
            try {
                val photo = repository.getImage(id)
                _uiState.value = UiState.Success(photo)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Something went wrong")
            }
        }
    }
}