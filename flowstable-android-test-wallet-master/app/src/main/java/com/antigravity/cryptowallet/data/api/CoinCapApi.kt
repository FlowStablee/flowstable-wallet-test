package com.antigravity.cryptowallet.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinCapApi {
    @GET("v2/assets")
    suspend fun getAssets(
        @Query("ids") ids: String
    ): CoinCapResponse
}

data class CoinCapResponse(
    @SerializedName("data") val data: List<CoinCapAsset>
)

data class CoinCapAsset(
    @SerializedName("id") val id: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("priceUsd") val priceUsd: String?,
    @SerializedName("changePercent24h") val changePercent24h: String?
)
