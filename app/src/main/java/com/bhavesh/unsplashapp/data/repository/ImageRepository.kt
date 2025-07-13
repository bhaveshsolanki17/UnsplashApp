package com.bhavesh.unsplashapp.data.repository

import com.bhavesh.unsplashapp.data.api.UnsplashApi
import com.bhavesh.unsplashapp.data.db.ImageDao
import com.bhavesh.unsplashapp.data.model.FavoriteImage
import com.bhavesh.unsplashapp.data.model.UnsplashImage
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    suspend fun getImages(page: Int): List<UnsplashImage>
    suspend fun getImage(id: String): UnsplashImage
    suspend fun searchImages(query: String, page: Int): List<UnsplashImage>
    fun getFavoriteImages(): Flow<List<FavoriteImage>>
    suspend fun addToFavorites(image: FavoriteImage)
    suspend fun removeFromFavorites(image: FavoriteImage)
}

class ImageRepositoryImpl(
    private val api: UnsplashApi,
    private val dao: ImageDao
) : ImageRepository {

    override suspend fun getImages(page: Int): List<UnsplashImage> = api.getImages(page)
    override suspend fun getImage(id: String): UnsplashImage = api.getImageDetails(id)

    override suspend fun searchImages(query: String, page: Int): List<UnsplashImage> =
        api.searchImages(query, page).results

    override fun getFavoriteImages(): Flow<List<FavoriteImage>> = dao.getFavorites()

    override suspend fun addToFavorites(image: FavoriteImage) = dao.insert(image)

    override suspend fun removeFromFavorites(image: FavoriteImage) = dao.delete(image)
}

