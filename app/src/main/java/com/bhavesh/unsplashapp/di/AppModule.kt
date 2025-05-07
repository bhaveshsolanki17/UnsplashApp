package com.bhavesh.unsplashapp.di

import android.app.Application
import androidx.room.Room
import com.bhavesh.unsplashapp.data.api.AuthInterceptor
import com.bhavesh.unsplashapp.data.api.UnsplashApi
import com.bhavesh.unsplashapp.data.db.ImageDatabase
import com.bhavesh.unsplashapp.data.repository.ImageRepository
import com.bhavesh.unsplashapp.data.repository.ImageRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "https://api.unsplash.com/"

    @Provides
    @Singleton
    fun provideUnsplashApi(): UnsplashApi {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient
                    .Builder()
                    .addInterceptor(logger)
                    .addInterceptor(AuthInterceptor())
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UnsplashApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): ImageDatabase =
        Room
            .databaseBuilder(app, ImageDatabase::class.java, "image_db")
            .build()

    @Provides
    fun provideDao(db: ImageDatabase) = db.imageDao()

    @Provides
    @Singleton
    fun provideImageRepository(
        api: UnsplashApi,
        db: ImageDatabase
    ): ImageRepository = ImageRepositoryImpl(api, db.imageDao())
}
