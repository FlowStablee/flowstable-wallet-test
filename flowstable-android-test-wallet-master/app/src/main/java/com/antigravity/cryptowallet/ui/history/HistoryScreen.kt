package com.antigravity.cryptowallet.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antigravity.cryptowallet.data.db.TransactionEntity
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    var selectedTx by remember { mutableStateOf<TransactionEntity?>(null) }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(16.dp)
    ) {
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrutalistHeader("History")
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(
                    imageVector = Icons.Filled.Refresh, 
                    contentDescription = "Sync",
                    tint = BrutalBlack,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions yet.", color = BrutalBlack)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(transactions) { tx ->
                    TransactionItem(tx, onClick = { selectedTx = tx })
                }
            }
        }

        // Transaction Detail Dialog
        selectedTx?.let { tx ->
            androidx.compose.ui.window.Dialog(onDismissRequest = { selectedTx = null }) {
                Column(
                    modifier = Modifier
                        .background(BrutalWhite)
                        .border(2.dp, BrutalBlack)
                        .padding(24.dp)
                ) {
                    BrutalistHeader("Details")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DetailRow("Status", tx.status.uppercase(), if(tx.status == "success") Color(0xFF00C853) else Color.Red)
                    DetailRow("Network", tx.network)
                    DetailRow("From", tx.fromAddress.take(10) + "..." + tx.fromAddress.takeLast(10))
                    DetailRow("To", tx.toAddress.take(10) + "..." + tx.toAddress.takeLast(10))
                    DetailRow("Hash", tx.hash.take(10) + "..." + tx.hash.takeLast(10))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        com.antigravity.cryptowallet.ui.components.BrutalistButton(
                            text = "Copy Hash",
                            onClick = { clipboardManager.setText(AnnotatedString(tx.hash)) },
                            modifier = Modifier.weight(1f),
                            inverted = true
                        )
                        com.antigravity.cryptowallet.ui.components.BrutalistButton(
                            text = "Close",
                            onClick = { selectedTx = null },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, color: Color = BrutalBlack) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun TransactionItem(tx: TransactionEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BrutalBlack)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon + Type
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(BrutalBlack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (tx.type == "receive") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = tx.type,
                    tint = BrutalWhite
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = tx.type.uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = BrutalBlack
                )
                Text(
                    text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(tx.timestamp)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // Amount + Status
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if(tx.type == "receive") "+" else "-"} ${tx.value} ${tx.symbol}",
                fontWeight = FontWeight.Bold,
                color = if (tx.type == "receive") Color(0xFF006400) else BrutalBlack
            )
            Text(
                text = tx.status.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (tx.status == "success") Color.Gray else Color.Red
            )
        }
    }
}
