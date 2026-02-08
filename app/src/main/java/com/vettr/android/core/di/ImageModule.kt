package com.vettr.android.core.di

import android.content.Context
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.vettr.android.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import javax.inject.Singleton

/**
 * Hilt module for providing image loading dependencies with Coil.
 * Configures memory and disk caching for optimal performance and minimal data usage.
 */
@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    private const val MEMORY_CACHE_SIZE_MB = 50
    private const val DISK_CACHE_SIZE_MB = 200
    private const val DISK_CACHE_DIRECTORY = "image_cache"

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context as PlatformContext)
            .components {
                // Use OkHttp for network requests
                add(OkHttpNetworkFetcherFactory(callFactory = okHttpClient))
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizeBytes(MEMORY_CACHE_SIZE_MB * 1024L * 1024L)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve(DISK_CACHE_DIRECTORY).toOkioPath())
                    .maxSizeBytes(DISK_CACHE_SIZE_MB * 1024L * 1024L)
                    .build()
            }
            .apply {
                // Enable debug logging in debug builds
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            // Enable crossfade animation
            .crossfade(true)
            .build()
    }
}
