package com.bhavesh.unsplashapp.data.api

import com.bhavesh.unsplashapp.data.model.UnsplashImage
import com.bhavesh.unsplashapp.data.model.UnsplashSearchResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface UnsplashApi {

    @Headers("Accept-Version: v1")
    @GET("photos")
    suspend fun getImages(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): List<UnsplashImage>

    @GET("search/photos")
    suspend fun searchImages(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): UnsplashSearchResponse

    @GET("photos/{id}")
    suspend fun getImageDetails(
        @Path("id") id: String
    ): UnsplashImage
}
