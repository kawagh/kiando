package jp.kawagh.kiando.network

import retrofit2.Response
import retrofit2.http.GET

interface KiandoApiService {
    @GET("/")
    suspend fun getSFENResponse(): Response<SFENResponse>
}

data class SFENResponse(val sfen: String)