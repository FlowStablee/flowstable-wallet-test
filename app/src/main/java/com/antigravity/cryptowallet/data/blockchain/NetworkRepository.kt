package com.antigravity.cryptowallet.data.blockchain

import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

data class Network(
    val id: String,
    val name: String,
    val rpcUrl: String,
    val initialRpc: String,
    val chainId: Long,
    val symbol: String,
    val coingeckoId: String,
    val explorerUrl: String? = null,
    val explorerApiUrl: String? = null
)

@Singleton
class NetworkRepository @Inject constructor() {
    private val infuraKey = "2e73eb0da821430d818d929e16963fc3"
    
    val networks = listOf(
        Network("eth", "Ethereum", "https://mainnet.infura.io/v3/${infuraKey}", "https://mainnet.infura.io/v3/${infuraKey}", 1, "ETH", "ethereum", "https://etherscan.io/", "https://api.etherscan.io/api"),
        Network("base", "Base", "https://mainnet.base.org", "https://mainnet.base.org", 8453, "ETH", "base", "https://basescan.org/", "https://api.basescan.org/api"),
        Network("arb", "Arbitrum One", "https://arbitrum-mainnet.infura.io/v3/${infuraKey}", "https://arbitrum-mainnet.infura.io/v3/${infuraKey}", 42161, "ETH", "arbitrum", "https://arbiscan.io/", "https://api.arbiscan.io/api"),
        Network("op", "Optimism", "https://optimism-mainnet.infura.io/v3/${infuraKey}", "https://optimism-mainnet.infura.io/v3/${infuraKey}", 10, "ETH", "optimism", "https://optimistic.etherscan.io/", "https://api-optimistic.etherscan.io/api"),
        Network("matic", "Polygon", "https://polygon-mainnet.infura.io/v3/${infuraKey}", "https://polygon-mainnet.infura.io/v3/${infuraKey}", 137, "POL", "polygon-ecosystem", "https://polygonscan.com/", "https://api.polygonscan.com/api"),
        Network("bsc", "BNB Chain", "https://bsc-dataseed.binance.org", "https://bsc-dataseed.binance.org", 56, "BNB", "binance-coin", "https://bscscan.com/", "https://api.bscscan.com/api"),
        Network("avax", "Avalanche C", "https://api.avax.network/ext/bc/C/rpc", "https://api.avax.network/ext/bc/C/rpc", 43114, "AVAX", "avalanche-2", "https://snowtrace.io/", "https://api.snowtrace.io/api"),
        Network("cro", "Cronos", "https://evm.cronos.org", "https://evm.cronos.org", 25, "CRO", "crypto-com-chain", "https://cronoscan.com/", "https://api.cronoscan.com/api")
    )
    
    fun getNetwork(id: String) = networks.find { it.id == id } ?: networks.first()
}
