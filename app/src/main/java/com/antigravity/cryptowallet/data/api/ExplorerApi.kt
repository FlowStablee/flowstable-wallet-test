package com.antigravity.cryptowallet.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface ExplorerApi {
    @GET
    suspend fun getTransactionList(
        @Url url: String,
        @Query("module") module: String = "account",
        @Query("action") action: String = "txlist",
        @Query("address") address: String,
        @Query("startblock") startblock: Int = 0,
        @Query("endblock") endblock: Int = 99999999,
        @Query("page") page: Int = 1,
        @Query("offset") offset: Int = 100,
        @Query("sort") sort: String = "desc",
        @Query("apikey") apikey: String? = null
    ): ExplorerResponse

    @GET
    suspend fun getERC20TransactionList(
        @Url url: String,
        @Query("module") module: String = "account",
        @Query("action") action: String = "tokentx",
        @Query("contractaddress") contractaddress: String? = null,
        @Query("address") address: String,
        @Query("startblock") startblock: Int = 0,
        @Query("endblock") endblock: Int = 99999999,
        @Query("page") page: Int = 1,
        @Query("offset") offset: Int = 100,
        @Query("sort") sort: String = "desc",
        @Query("apikey") apikey: String? = null
    ): ExplorerResponse
}

data class ExplorerResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<ExplorerTransaction>
)

data class ExplorerTransaction(
    @SerializedName("hash") val hash: String,
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String,
    @SerializedName("value") val value: String,
    @SerializedName("timeStamp") val timeStamp: String,
    @SerializedName("isError") val isError: String,
    @SerializedName("txreceipt_status") val txReceiptStatus: String?,
    @SerializedName("tokenSymbol") val tokenSymbol: String? = null,
    @SerializedName("tokenDecimal") val tokenDecimal: String? = null
)
