package com.bhavesh.unsplashapp.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bhavesh.unsplashapp.ui.common.ErrorMessage
import com.bhavesh.unsplashapp.ui.common.GenericImageGrid
import com.bhavesh.unsplashapp.ui.common.GridImageItem
import com.bhavesh.unsplashapp.ui.common.LoadingGridPlaceholder
import com.bhavesh.unsplashapp.ui.routes.AppRoutes
import com.bhavesh.unsplashapp.ui.state.UiState

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val favorites by viewModel.favoriteIds.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val gridState = rememberLazyGridState()

    val isAtEnd = remember(gridState) {
        derivedStateOf {
            gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == gridState.layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(isAtEnd.value) {
        if (isAtEnd.value) viewModel.loadNextPage()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.onSearchQueryChanged(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Search images...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        when (state) {
            is UiState.Loading -> LoadingGridPlaceholder()
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
                },
                state = gridState
            )

            is UiState.Error -> ErrorMessage((state as UiState.Error).message)
        }
    }
}