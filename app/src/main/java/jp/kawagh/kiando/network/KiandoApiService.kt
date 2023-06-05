package jp.kawagh.kiando.network

import kotlinx.serialization.Serializable
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface KiandoApiService {
    @GET("/")
    suspend fun getSFENResponse(): Response<SFENResponse>

    // TODO set requestBody https://square.github.io/retrofit/
    @Multipart
    @POST("/")
    suspend fun getSFENResponse(@Part("photo") photo: RequestBody): Response<SFENResponse>
}

@Serializable
data class SFENResponse(val sfen: String)