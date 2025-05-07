package com.bhavesh.unsplashapp.data.db


import androidx.room.Database
import androidx.room.RoomDatabase
import com.bhavesh.unsplashapp.data.model.FavoriteImage

@Database(entities = [FavoriteImage::class], version = 1)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
}
