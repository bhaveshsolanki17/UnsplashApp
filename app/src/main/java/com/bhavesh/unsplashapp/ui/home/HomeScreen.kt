package com.bhavesh.unsplashapp.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bhavesh.unsplashapp.ui.common.ErrorMessage
import com.bhavesh.unsplashapp.ui.common.GenericImageGrid
import com.bhavesh.unsplashapp.ui.common.GridImageItem
import com.bhavesh.unsplashapp.ui.common.LoadingIndicator
import com.bhavesh.unsplashapp.ui.routes.AppRoutes
import com.bhavesh.unsplashapp.ui.state.UiState

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val favorites by viewModel.favoriteIds.collectAsState()

    when (state) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Success -> GenericImageGrid(
            items = (state as UiState.Success).data,
            onItemClick = { navController.navigate("${AppRoutes.DETAIL}/${it.id}") },
            itemContent = { image ->
                GridImageItem(
                    imageUrl = image.urls.thumb,
                    label = image.user.name,
                    supportFavourites = true,
                    isFavorite = favorites.contains(image.id),
                    onFavoriteClick = { viewModel.toggleFavorite(image) }
                )
            }
        )

        is UiState.Error -> ErrorMessage((state as UiState.Error).message)
    }
}