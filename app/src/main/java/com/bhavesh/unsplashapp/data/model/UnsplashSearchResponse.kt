package com.bhavesh.unsplashapp.data.model

data class UnsplashSearchResponse(
    val total: Int,
    val results: List<UnsplashImage>
)
