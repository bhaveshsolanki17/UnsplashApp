package com.bhavesh.unsplashapp.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bhavesh.unsplashapp.data.model.FavoriteImage

@Composable
fun SwipeToDeleteItem(
    item: FavoriteImage,
    onDelete: (FavoriteImage) -> Unit,
    onClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete(item)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Delete",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
            ) {
                GridImageItem(
                    imageUrl = item.imageUrl,
                    label = item.userName,
                    isFavorite = true,
                    onFavoriteClick = { onDelete(item) }
                )
            }
        }
    )
}