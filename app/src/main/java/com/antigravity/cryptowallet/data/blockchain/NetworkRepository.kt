package com.antigravity.cryptowallet.data.blockchain

import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

data class Network(
    val id: String,
    val name: String,
    val rpcUrl: String,
    val initialRpc: String, // Keep a default one
    val chainId: Long,
    val symbol: String,
    val coingeckoId: String
)

@Singleton
class NetworkRepository @Inject constructor() {
    private val infuraKey = "2e73eb0da821430d818d929e16963fc3"
    
    val networks = listOf(
        Network("eth", "Ethereum", "https://mainnet.infura.io/v3/$infuraKey", "https://mainnet.infura.io/v3/$infuraKey", 1, "ETH", "ethereum"),
        Network("arb", "Arbitrum One", "https://arbitrum-mainnet.infura.io/v3/$infuraKey", "https://arbitrum-mainnet.infura.io/v3/$infuraKey", 42161, "ETH", "ethereum"),
        Network("op", "Optimism", "https://optimism-mainnet.infura.io/v3/$infuraKey", "https://optimism-mainnet.infura.io/v3/$infuraKey", 10, "ETH", "ethereum"),
        Network("matic", "Polygon", "https://polygon-mainnet.infura.io/v3/$infuraKey", "https://polygon-mainnet.infura.io/v3/$infuraKey", 137, "POL", "matic-network"),
        Network("bsc", "BNB Chain", "https://bsc-dataseed.binance.org", "https://bsc-dataseed.binance.org", 56, "BNB", "binancecoin")
    )
    
    fun getNetwork(id: String) = networks.find { it.id == id } ?: networks.first()
}
