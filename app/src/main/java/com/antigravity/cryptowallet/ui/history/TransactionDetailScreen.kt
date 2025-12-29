package com.antigravity.cryptowallet.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite

@Composable
fun TransactionDetailScreen(
    txHash: String,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsState()
    val tx = transactions.find { it.hash == txHash }
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrutalBlack)
            }
            BrutalistHeader("Receipt")
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (tx == null) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("Transaction details locally not found.\nIt might still be indexing.", textAlign = TextAlign.Center, color = Color.Gray)
            }
            BrutalistButton(text = "Go Back", onClick = onBack, modifier = Modifier.fillMaxWidth())
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, BrutalBlack)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00C853), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("TRANSACTION SUCCESSFUL", fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text(
                    "${if (tx.type == "receive") "+" else "-"}${tx.value} ${tx.symbol}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = if (tx.type == "receive") Color(0xFF00C853) else Color.Red
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            DetailItem("Status", tx.status.uppercase())
            DetailItem("Network", tx.network)
            DetailItem("From", tx.fromAddress)
            DetailItem("To", tx.toAddress)
            DetailItem("Date", java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(java.util.Date(tx.timestamp)))
            DetailItem("Hash", tx.hash)

            Spacer(modifier = Modifier.height(32.dp))

            BrutalistButton(
                text = "VIEW ON BLOCKSCAN",
                onClick = {
                    // This is a generic blockscan link or chain specific
                    val url = "https://blockscan.com/tx/${tx.hash}"
                    uriHandler.openUri(url)
                },
                modifier = Modifier.fillMaxWidth(),
                inverted = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BrutalistButton(
                text = "DONE",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        Divider(color = BrutalBlack.copy(alpha = 0.1f))
    }
}
