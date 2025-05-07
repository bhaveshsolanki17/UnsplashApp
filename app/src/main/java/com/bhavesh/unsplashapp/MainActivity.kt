package com.bhavesh.unsplashapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bhavesh.unsplashapp.ui.common.BottomNavBar
import com.bhavesh.unsplashapp.ui.details.DetailScreen
import com.bhavesh.unsplashapp.ui.favourite.FavoritesScreen
import com.bhavesh.unsplashapp.ui.home.HomeScreen
import com.bhavesh.unsplashapp.ui.routes.AppRoutes
import com.bhavesh.unsplashapp.ui.theme.UnsplashAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            UnsplashAppTheme(darkTheme = isDarkTheme) {
                UnsplashAppScreen(
                    isDarkTheme = isDarkTheme, onToggleTheme = { isDarkTheme = !isDarkTheme })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnsplashAppScreen(
    isDarkTheme: Boolean, onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    Scaffold(topBar = {
        TopAppBar(title = { Text("Unsplash App") }, actions = {
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = "Toggle Theme"
                )
            }
        })
    }, bottomBar = {
        BottomNavBar(
            currentRoute = navController.currentBackStackEntry?.destination?.route ?: "",
            onNavigateToHome = { navController.navigate(AppRoutes.HOME) },
            onNavigateToFavourites = { navController.navigate(AppRoutes.FAVOURITES) })
    }) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(AppRoutes.HOME) {
                HomeScreen(navController)
            }
            composable(AppRoutes.FAVOURITES) {
                FavoritesScreen(navController)
            }
            composable("${AppRoutes.DETAIL}/{${AppRoutes.PARAM_IMAGE_ID}}") { backStackEntry ->
                DetailScreen(
                    imageId = backStackEntry.arguments?.getString(AppRoutes.PARAM_IMAGE_ID) ?: "",
                    navController = navController
                )
            }
        }
    }
}