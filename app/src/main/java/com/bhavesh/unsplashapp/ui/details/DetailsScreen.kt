package com.bhavesh.unsplashapp.ui.details

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.bhavesh.unsplashapp.data.model.UnsplashImage
import com.bhavesh.unsplashapp.ui.common.ErrorMessage
import com.bhavesh.unsplashapp.ui.common.LoadingIndicator
import com.bhavesh.unsplashapp.ui.state.UiState
import com.bhavesh.unsplashapp.utils.downloadImage
import com.bhavesh.unsplashapp.utils.hasStoragePermission
import com.bhavesh.unsplashapp.utils.openAppSettings
import com.bhavesh.unsplashapp.utils.shouldRequestWritePermission

@Composable
fun DetailScreen(
    imageId: String,
    navController: NavController,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    when (state) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorMessage((state as UiState.Error).message)
        is UiState.Success -> DetailContent(image = (state as UiState.Success).data)
    }
}

private fun showPermissionToast(context: Context) {
    Toast.makeText(
        context,
        "Storage permission permanently denied. Please enable it in settings.",
        Toast.LENGTH_LONG
    ).show()
}

@Composable
fun DetailContent(image: UnsplashImage) {
    val context = LocalContext.current
    val needsPermission = shouldRequestWritePermission()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            downloadImage(context, image.urls.full, image.id)
        } else {
            showPermissionToast(context)
            openAppSettings(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(image.urls.full),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = image.user.name, style = MaterialTheme.typography.titleLarge)
        Text(text = "@${image.user.username}", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = image.description ?: "No description available.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (needsPermission) {
                val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                if (hasStoragePermission(context)) {
                    downloadImage(context, image.urls.full, image.id)
                } else {
                    launcher.launch(permission)
                }
            } else {
                // No permission needed on Android 10+ for DownloadManager
                downloadImage(context, image.urls.full, image.id)
            }
        }) {
            Text("Download Image")
        }
    }
}

