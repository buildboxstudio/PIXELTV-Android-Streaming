package com.idlix.tv.data.api

import com.idlix.tv.data.model.RedeemRequest
import com.idlix.tv.data.model.RedeemResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface MajorplayApiService {

    @POST
    suspend fun redeemPlay(
        @Url url: String,
        @Header("Referer") referer: String = IdlixSource.REFERER,
        @Header("Origin") origin: String = IdlixSource.ORIGIN,
        @Body request: RedeemRequest
    ): RedeemResponse
}
