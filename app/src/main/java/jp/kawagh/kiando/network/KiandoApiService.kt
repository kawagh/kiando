package jp.kawagh.kiando.network

import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

interface KiandoApiService {
    @GET("/")
    suspend fun getSFENResponse(): Response<SFENResponse>

    // TODO set requestBody https://square.github.io/retrofit/
    @Multipart
    @POST("/uploadfile")
    suspend fun getSFENResponse(@Part image: MultipartBody.Part): Response<SFENResponse>
//    suspend fun getSFENResponse(@Body image: File): Response<SFENResponse>
}

@Serializable
data class SFENResponse(val sfen: String)