package com.antigravity.cryptowallet.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

interface DexScreenerApi {
    @GET("latest/dex/tokens/{address}")
    suspend fun getTokenPairs(
        @Path("address") address: String
    ): DexScreenerResponse
}

data class DexScreenerResponse(
    @SerializedName("pairs") val pairs: List<DexPair>?
)

data class DexPair(
    @SerializedName("priceUsd") val priceUsd: String?,
    @SerializedName("baseToken") val baseToken: DexToken?,
    @SerializedName("volume") val volume: DexVolume?,
    @SerializedName("priceChange") val priceChange: DexPriceChange?
)

data class DexToken(
    @SerializedName("address") val address: String,
    @SerializedName("name") val name: String,
    @SerializedName("symbol") val symbol: String
)

data class DexVolume(
    @SerializedName("h24") val h24: Double?
)

data class DexPriceChange(
    @SerializedName("h24") val h24: Double?
)
