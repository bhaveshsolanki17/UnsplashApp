package com.bhavesh.unsplashapp.ui.favourite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bhavesh.unsplashapp.data.model.FavoriteImage
import com.bhavesh.unsplashapp.ui.common.ErrorMessage
import com.bhavesh.unsplashapp.ui.common.LoadingIndicator
import com.bhavesh.unsplashapp.ui.common.SwipeToDeleteItem
import com.bhavesh.unsplashapp.ui.routes.AppRoutes
import com.bhavesh.unsplashapp.ui.state.UiState

@Composable
fun FavoritesScreen(navController: NavController, viewModel: FavoritesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    when (state) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Success -> FavoritesList(
            favorites = (state as UiState.Success<List<FavoriteImage>>).data,
            onNavigate = { id -> navController.navigate("${AppRoutes.DETAIL}/${id}") },
            onDelete = viewModel::removeFavorite
        )

        is UiState.Error -> ErrorMessage((state as UiState.Error).message)
    }
}

@Composable
fun FavoritesList(
    favorites: List<FavoriteImage>,
    onNavigate: (String) -> Unit,
    onDelete: (FavoriteImage) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(favorites, key = { it.id }) { image ->
            SwipeToDeleteItem(
                item = image,
                onDelete = onDelete,
                onClick = { onNavigate(image.id) }
            )
        }
    }
}
