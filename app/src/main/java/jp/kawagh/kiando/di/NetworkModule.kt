package jp.kawagh.kiando.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.kawagh.kiando.BuildConfig
import jp.kawagh.kiando.network.KiandoApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ForEmulator

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ForRealDevice

    @Provides
    @Singleton
    fun provideKiandoApiService(@ForRealDevice retrofit: Retrofit): KiandoApiService =
        retrofit.create(KiandoApiService::class.java)

    @ForEmulator
    @Provides
    @Singleton
    fun provideRetrofitForEmulator(httpClient: OkHttpClient): Retrofit {
        val url = "http://10.0.2.2:8000/"
        return Retrofit.Builder()
            .baseUrl(url)
            .client(httpClient)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @ForRealDevice
    @Provides
    @Singleton
    fun provideRetrofitForRealDevice(httpClient: OkHttpClient): Retrofit {
        val url = if (BuildConfig.DEBUG) {
            BuildConfig.apiUrl
        } else {
            "http://192.168.1.4:8000/"
        }
        return Retrofit.Builder()
            .baseUrl(url)
            .client(httpClient)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.HEADERS
                }
            )
            .build()
    }
}
