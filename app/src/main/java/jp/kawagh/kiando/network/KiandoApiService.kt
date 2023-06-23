package jp.kawagh.kiando.network

import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface KiandoApiService {
    @GET("/")
    suspend fun getSFENResponse(): Response<SFENResponse>

    @Multipart
    @POST("/uploadfile/")
    suspend fun getSFENResponse(@Part image: MultipartBody.Part): Response<SFENResponse>
}

@Serializable
data class SFENResponse(val sfen: String)
