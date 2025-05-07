package com.bhavesh.unsplashapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bhavesh.unsplashapp.data.model.FavoriteImage
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

    @Query("SELECT * FROM favorites")
    fun getFavorites(): Flow<List<FavoriteImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: FavoriteImage)

    @Delete
    suspend fun delete(image: FavoriteImage)
}
