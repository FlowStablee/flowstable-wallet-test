package com.antigravity.cryptowallet.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.cryptowallet.data.models.AssetUiModel
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.history.HistoryViewModel
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite
import com.antigravity.cryptowallet.utils.QrCodeGenerator
import androidx.compose.foundation.Image

@Composable
fun TokenDetailScreen(
    assetId: String,
    onBack: () -> Unit,
    onNavigateToSend: (String) -> Unit = {},
    onNavigateToTxDetail: (String) -> Unit = {},
    viewModel: WalletViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val asset = viewModel.assets.find { it.id == assetId } ?: return
    val transactions by historyViewModel.transactions.collectAsState()
    val filteredTransactions = transactions.filter { it.symbol.uppercase() == asset.symbol.uppercase() }
    
    var showReceiveDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    if (showReceiveDialog) {
        Dialog(onDismissRequest = { showReceiveDialog = false }) {
            Column(
                modifier = Modifier
                    .background(BrutalWhite)
                    .border(2.dp, BrutalBlack)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrutalistHeader("Receive ${asset.symbol}")
                Spacer(modifier = Modifier.height(16.dp))
                
                val qrBitmap = remember(viewModel.address) {
                    QrCodeGenerator.generateQrCode(viewModel.address)
                }
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Wallet Address QR Code",
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = viewModel.address,
                    fontWeight = FontWeight.Bold,
                    color = BrutalBlack,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable {
                        clipboardManager.setText(AnnotatedString(viewModel.address))
                    }
                )
                Text("(Tap to copy)", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                BrutalistButton(text = "Close", onClick = { showReceiveDialog = false })
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(16.dp)
    ) {
        // Navigation & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrutalBlack)
            }
            Spacer(modifier = Modifier.width(8.dp))
            BrutalistHeader(asset.symbol)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Price Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrutalBlack)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        asset.name.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        asset.balanceUsd,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = BrutalBlack
                    )
                }
                
                // Explorer Icon Button
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                IconButton(onClick = {
                    val network = viewModel.getNetwork(asset.chainId)
                    val url = if (asset.contractAddress != null) {
                        "${network.explorerUrl}token/${asset.contractAddress}"
                    } else {
                        "${network.explorerUrl}address/${viewModel.address}"
                    }
                    uriHandler.openUri(url)
                }) {
                    Icon(Icons.Default.OpenInNew, contentDescription = "View on Explorer", tint = BrutalBlack)
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isPositive = asset.priceChange24h >= 0
                Text(
                    text = String.format("${if (isPositive) "+" else ""}$%.2f (%.2f%%)", asset.price * (asset.priceChange24h / 100), asset.priceChange24h),
                    color = if (isPositive) Color(0xFF00C853) else Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    " 24h",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoBox(label = "BALANCE", value = asset.balance, modifier = Modifier.weight(1f))
            InfoBox(label = "NETWORK", value = asset.networkName, modifier = Modifier.weight(1f))
        }

        if (asset.contractAddress != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BrutalBlack)
                    .clickable { clipboardManager.setText(AnnotatedString(asset.contractAddress)) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("CONTRACT ADDRESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(
                        asset.contractAddress.take(10) + "..." + asset.contractAddress.takeLast(10),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.ContentCopy, 
                    contentDescription = "Copy", 
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History Section Label
        Text("TRANSACTION HISTORY", fontWeight = FontWeight.Black, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(2.dp, BrutalBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No transactions found for ${asset.symbol}",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(2.dp, BrutalBlack),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions) { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BrutalBlack)
                            .clickable { onNavigateToTxDetail(tx.hash) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tx.type.uppercase(), fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Text(
                                if (tx.type == "send") "To: ${tx.toAddress.take(6)}...${tx.toAddress.takeLast(4)}" 
                                else "From: ${tx.fromAddress.take(6)}...${tx.fromAddress.takeLast(4)}",
                                fontSize = 10.sp, color = Color.Gray
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${if (tx.type == "receive") "+" else "-"}${tx.value} ${tx.symbol}",
                                fontWeight = FontWeight.Bold,
                                color = if (tx.type == "receive") Color(0xFF00C853) else Color.Red
                            )
                            Text(
                                java.text.SimpleDateFormat("MMM dd, HH:mm").format(java.util.Date(tx.timestamp)),
                                fontSize = 10.sp, color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BrutalistButton(
                text = "SEND",
                onClick = { onNavigateToSend(asset.id) },
                modifier = Modifier.weight(1f)
            )
            BrutalistButton(
                text = "RECEIVE",
                onClick = { showReceiveDialog = true },
                modifier = Modifier.weight(1f),
                inverted = true
            )
        }
    }
}

@Composable
fun InfoBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(1.dp, BrutalBlack)
            .padding(12.dp)
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = BrutalBlack)
    }
}
