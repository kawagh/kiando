package jp.kawagh.kiando.network

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET

interface KiandoApiService {
    @GET("/")
    suspend fun getSFENResponse(): Response<SFENResponse>
}

@Serializable
data class SFENResponse(val sfen: String)