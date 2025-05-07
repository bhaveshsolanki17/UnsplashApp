package com.bhavesh.unsplashapp.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.bhavesh.unsplashapp.ui.routes.AppRoutes

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToFavourites: () -> Unit
) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, AppRoutes.HOME),
        BottomNavItem("Favourites", Icons.Default.Favorite, AppRoutes.FAVOURITES)
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    when (item.route) {
                        AppRoutes.HOME -> onNavigateToHome()
                        AppRoutes.FAVOURITES -> onNavigateToFavourites()
                    }
                }
            )
        }
    }
}