package com.bhavesh.unsplashapp.utils

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri

fun downloadImage(context: Context, url: String, fileName: String) {
    val request = DownloadManager.Request(url.toUri()).apply {
        setTitle("Downloading image...")
        setDescription(fileName)
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "$fileName.jpg")
        setAllowedOverMetered(true)
    }

    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)
}