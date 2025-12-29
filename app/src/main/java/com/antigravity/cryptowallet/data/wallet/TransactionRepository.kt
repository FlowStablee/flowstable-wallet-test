package com.antigravity.cryptowallet.data.wallet

import com.antigravity.cryptowallet.data.api.ExplorerApi
import com.antigravity.cryptowallet.data.blockchain.Network
import com.antigravity.cryptowallet.data.db.TransactionDao
import com.antigravity.cryptowallet.data.db.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val explorerApi: ExplorerApi
) {
    val transactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun refreshTransactions(address: String, network: Network) = withContext(Dispatchers.IO) {
        val apiUrl = network.explorerApiUrl ?: return@withContext
        
        try {
            // 1. Fetch Normal Transactions
            val normalResponse = explorerApi.getTransactionList(url = apiUrl, address = address)
            if (normalResponse.status == "1") {
                val entities = normalResponse.result.map { tx ->
                    val valueEth = BigDecimal(tx.value).divide(BigDecimal.TEN.pow(18)).toPlainString()
                    val type = if (tx.from.lowercase() == address.lowercase()) "send" else "receive"
                    TransactionEntity(
                        hash = tx.hash,
                        fromAddress = tx.from,
                        toAddress = tx.to,
                        value = valueEth,
                        symbol = network.symbol,
                        timestamp = tx.timeStamp.toLong() * 1000,
                        type = type,
                        status = "success",
                        network = network.name
                    )
                }
                transactionDao.insertTransactions(entities)
            }
            
            // 2. Fetch Token Transactions (ERC20)
            val tokenResponse = explorerApi.getERC20TransactionList(url = apiUrl, address = address)
            if (tokenResponse.status == "1") {
                val tokenEntities = tokenResponse.result.map { tx ->
                    // Note: tx.value for token transfers needs decimal mapping, 
                    // but Etherscan API result for tokentx usually doesn't include token decimal in this simple set
                    // We'll use a best-effort approach or assume 18 for now if token info is missing.
                    // In a perfect app, we'd lookup the token.
                    val valueToken = BigDecimal(tx.value).divide(BigDecimal.TEN.pow(18)).toPlainString()
                    val type = if (tx.from.lowercase() == address.lowercase()) "send" else "receive"
                    TransactionEntity(
                        hash = tx.hash,
                        fromAddress = tx.from,
                        toAddress = tx.to,
                        value = valueToken,
                        symbol = tx.tokenSymbol ?: network.symbol, // Some explorer APIs include tokenSymbol
                        timestamp = tx.timeStamp.toLong() * 1000,
                        type = type,
                        status = "success",
                        network = network.name
                    )
                }
                transactionDao.insertTransactions(tokenEntities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addTransaction(
        hash: String,
        from: String,
        to: String,
        value: String,
        symbol: String,
        type: String,
        status: String,
        network: String
    ) {
        val transaction = TransactionEntity(
            hash = hash,
            fromAddress = from,
            toAddress = to,
            value = value,
            symbol = symbol,
            timestamp = System.currentTimeMillis(),
            type = type,
            status = status,
            network = network
        )
        transactionDao.insertTransaction(transaction)
    }
}
