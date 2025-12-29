package com.antigravity.cryptowallet.data.wallet

import com.antigravity.cryptowallet.data.api.CoinCapApi
import com.antigravity.cryptowallet.data.api.DexScreenerApi
import com.antigravity.cryptowallet.data.blockchain.BlockchainService
import com.antigravity.cryptowallet.data.blockchain.NetworkRepository
import com.antigravity.cryptowallet.data.db.TokenDao
import com.antigravity.cryptowallet.data.db.TokenEntity
import com.antigravity.cryptowallet.data.models.AssetUiModel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetRepository @Inject constructor(
    private val walletRepository: WalletRepository,
    private val networkRepository: NetworkRepository,
    private val blockchainService: BlockchainService,
    private val tokenDao: TokenDao,
    private val coinCapApi: CoinCapApi,
    private val dexscreenerApi: DexScreenerApi,
    private val transactionRepository: TransactionRepository
) {
    // In-memory cache for last balances to make UI smooth
    private var lastResultList: List<AssetUiModel>? = null
    private val _assets = MutableSharedFlow<List<AssetUiModel>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val assets = _assets.asSharedFlow()

    suspend fun refreshAssets() = withContext(Dispatchers.IO) {
        if (!walletRepository.isWalletCreated()) return@withContext
        val address = walletRepository.getAddress()

        // 1. Ensure Defaults
        val savedTokens = tokenDao.getAllTokens().first()
        if (savedTokens.isEmpty()) {
            tokenDao.insertToken(TokenEntity(symbol = "USDT", name = "Tether", contractAddress = "0xdac17f958d2ee523a2206206994597c13d831ec7", decimals = 6, chainId = "eth", coingeckoId = "tether"))
        }
        val allTokens = tokenDao.getAllTokens().first()
        
        // Emit cached data first for smoothness
        lastResultList?.let { _assets.tryEmit(it) }

        // 2. Fetch Prices
        val networkIds = networkRepository.networks.map { it.coingeckoId ?: "" }.filter { it.isNotEmpty() }
        val coinCapData = try {
            coinCapApi.getAssets(networkIds.joinToString(",")).data
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
        val prices = coinCapData.associate { it.id to (it.priceUsd?.toDouble() ?: 0.0) }
        val priceChanges = coinCapData.associate { it.id to (it.changePercent24h?.toDouble() ?: 0.0) }

        val resultList = mutableListOf<AssetUiModel>()

        // 3. Fetch Native & Token Balances in Parallel
        val mainNetworks = listOf("eth", "bsc", "matic")
        val resultList = java.util.Collections.synchronizedList(mutableListOf<AssetUiModel>())

        coroutineScope {
            // Native Balances
            val nativeJobs = mainNetworks.map { netId ->
                async {
                    try {
                        val net = networkRepository.getNetwork(netId)
                        val balance = blockchainService.getBalance(net.rpcUrl, address)
                        val ethBalance = BigDecimal(balance).divide(BigDecimal.TEN.pow(18))
                        val price = prices[net.coingeckoId] ?: 0.0
                        val priceChange = priceChanges[net.coingeckoId] ?: 0.0
                        val balanceUsd = ethBalance.multiply(BigDecimal(price))

                        resultList.add(
                            AssetUiModel(
                                id = "native-${net.id}",
                                symbol = net.symbol,
                                name = net.name,
                                balance = String.format("%.7f %s", ethBalance, net.symbol).trimEnd('0').trimEnd('.'),
                                balanceUsd = String.format("$%.2f", balanceUsd),
                                iconUrl = null,
                                networkName = net.name,
                                chainId = net.id,
                                contractAddress = null,
                                rawBalance = ethBalance.toDouble(),
                                price = price,
                                priceChange24h = priceChange
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // Token Balances (Chunked to avoid overwhelming RPC)
            val tokenJobs = allTokens.chunked(20).flatMap { chunk ->
                chunk.map { token ->
                    async {
                        try {
                            val net = networkRepository.getNetwork(token.chainId)
                            val balance = blockchainService.getTokenBalance(net.rpcUrl, token.contractAddress!!, address)
                            val tokenBalance = BigDecimal(balance).divide(BigDecimal.TEN.pow(token.decimals))
                            
                            // Only fetch DexScreener if balance > 0 to save bandwidth/API
                            var price = 0.0
                            var priceChange = 0.0
                            
                            if (tokenBalance > BigDecimal.ZERO) {
                                try {
                                    val dexResponse = dexscreenerApi.getTokenPairs(token.contractAddress)
                                    val dexData = dexResponse.pairs?.maxByOrNull { it.volume?.h24 ?: 0.0 }
                                    price = dexData?.priceUsd?.toDouble() ?: 0.0
                                    priceChange = dexData?.priceChange?.h24 ?: 0.0
                                } catch (e: Exception) { }
                            }
                            
                            val balanceUsd = tokenBalance.multiply(BigDecimal(price))

                            resultList.add(
                                AssetUiModel(
                                    id = "token-${token.id}",
                                    symbol = token.symbol,
                                    name = token.name,
                                    balance = String.format("%.7f %s", tokenBalance, token.symbol).trimEnd('0').trimEnd('.'),
                                    balanceUsd = String.format("$%.2f", balanceUsd),
                                    iconUrl = null,
                                    networkName = net.name,
                                    chainId = net.id,
                                    contractAddress = token.contractAddress,
                                    rawBalance = tokenBalance.toDouble(),
                                    price = price,
                                    priceChange24h = priceChange
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            nativeJobs.awaitAll()
            tokenJobs.awaitAll()
        }

        lastResultList = resultList
        _assets.tryEmit(resultList)
    }

    suspend fun addToken(address: String, symbol: String, decimals: Int, chainId: String, name: String) {
        tokenDao.insertToken(
            TokenEntity(
                symbol = symbol,
                name = name,
                contractAddress = address,
                decimals = decimals,
                chainId = chainId,
                isCustom = true
            )
        )
        refreshAssets()
    }

    suspend fun sendAsset(asset: AssetUiModel, toAddress: String, amount: String): String = withContext(Dispatchers.IO) {
        val credentials = walletRepository.activeCredentials ?: throw Exception("Wallet not loaded")
        val amountValue = BigDecimal(amount)
        
        // Find network
        val netId = if (asset.id.startsWith("native-")) asset.id.removePrefix("native-") else {
             // For tokens, we need to find which chain they belong to. 
             // In this simple app, we can extract from metadata or assume.
             // Let's look up the token.
             val tokenId = asset.id.removePrefix("token-").toLongOrNull()
             val token = tokenId?.let { tokenDao.getTokenById(it) }
             token?.chainId ?: "eth"
        }
        val net = networkRepository.getNetwork(netId)
        
        val txHash = if (asset.id.startsWith("native-")) {
            val amountWei = amountValue.multiply(BigDecimal.TEN.pow(18)).toBigInteger()
            blockchainService.sendEth(net.rpcUrl, credentials, toAddress, amountWei)
        } else {
            // Token
            val tokenId = asset.id.removePrefix("token-").toLongOrNull()
            val token = tokenId?.let { tokenDao.getTokenById(it) } ?: throw Exception("Token info not found")
            val amountRaw = amountValue.multiply(BigDecimal.TEN.pow(token.decimals)).toBigInteger()
            blockchainService.sendToken(net.rpcUrl, credentials, token.contractAddress!!, toAddress, amountRaw)
        }
        
        // Add to history
        transactionRepository.addTransaction(
            hash = txHash,
            from = credentials.address,
            to = toAddress,
            value = amount,
            symbol = asset.symbol,
            type = "send",
            status = "success", // Ideally pending then poll, but for now simple
            network = net.name
        )
        
        refreshAssets()
        txHash
    }
}
