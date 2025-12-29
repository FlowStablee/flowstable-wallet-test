package com.antigravity.cryptowallet.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.cryptowallet.data.models.AssetUiModel
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite

@Composable
fun TokenDetailScreen(
    assetId: String,
    onBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val asset = viewModel.assets.find { it.id == assetId } ?: return
    val clipboardManager = LocalClipboardManager.current

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
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", size = 16.dp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History Section Label
        Text("TRANSACTION HISTORY", fontWeight = FontWeight.Black, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        // Placeholder for Filtered History
        // In a real implementation, we'd fetch transactions specifically for this asset/network
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(2.dp, BrutalBlack, shape = RoundedCornerShape(0.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No recent transactions for ${asset.symbol}",
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BrutalistButton(
                text = "SEND",
                onClick = { /* Navigate to Send with pre-selected asset */ },
                modifier = Modifier.weight(1f)
            )
            BrutalistButton(
                text = "RECEIVE",
                onClick = { /* Show Receive Dialog */ },
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
