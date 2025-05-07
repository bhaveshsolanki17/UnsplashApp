package com.bhavesh.unsplashapp.data.model

data class UnsplashImage(
    val id: String,
    val description: String?,
    val urls: Urls,
    val user: User
)

data class Urls(
    val regular: String,
    val full: String,
    val thumb: String
)

data class User(
    val name: String,
    val username: String,
    val profile_image: ProfileImage
)

data class ProfileImage(
    val small: String
)
