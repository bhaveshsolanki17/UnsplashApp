package com.bhavesh.unsplashapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteImage(
    @PrimaryKey val id: String,
    val imageUrl: String,
    val userName: String
)
