package com.idlix.tv.data.api

import com.idlix.tv.data.model.V1CatalogResponse
import com.idlix.tv.data.model.V1Detail
import com.idlix.tv.data.model.V1Play
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiV1Service {

    @GET("api/v1/catalog")
    suspend fun catalog(
        @Query("source") source: String? = null,
        @Query("type") type: String? = null,
        @Query("page") page: Int = 1,
        @Query("q") query: String? = null
    ): V1CatalogResponse

    @GET("api/v1/detail")
    suspend fun detail(
        @Query("source") source: String,
        @Query("type") type: String,
        @Query("id") id: String
    ): V1Detail

    @GET("api/v1/play")
    suspend fun play(
        @Query("source") source: String,
        @Query("type") type: String,
        @Query("id") id: String
    ): V1Play
}
