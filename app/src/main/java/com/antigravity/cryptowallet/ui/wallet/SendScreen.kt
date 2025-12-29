package com.antigravity.cryptowallet.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.cryptowallet.ui.components.BrutalistButton
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    assetId: String,
    onBack: () -> Unit,
    onSendSuccess: (String) -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val asset = viewModel.assets.find { it.id == assetId } ?: return
    var recipientAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrutalBlack)
            }
            BrutalistHeader("Send ${asset.symbol}")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Asset Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrutalBlack)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("BALANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("${asset.balance} ${asset.symbol}", fontWeight = FontWeight.Black)
                }
                Text(asset.networkName, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recipient
        Text("RECIPIENT ADDRESS", fontWeight = FontWeight.Black, fontSize = 12.sp)
        OutlinedTextField(
            value = recipientAddress,
            onValueChange = { recipientAddress = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("0x...") },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = BrutalBlack,
                unfocusedBorderColor = BrutalBlack
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Amount
        Text("AMOUNT", fontWeight = FontWeight.Black, fontSize = 12.sp)
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("0.0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = BrutalBlack,
                unfocusedBorderColor = BrutalBlack
            ),
            singleLine = true,
            suffix = { Text(asset.symbol, fontWeight = FontWeight.Bold) }
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isSending) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = BrutalBlack)
        } else {
            BrutalistButton(
                text = "CONFIRM SEND",
                onClick = {
                    if (recipientAddress.isBlank() || amount.isBlank()) {
                        errorMessage = "Please fill all fields"
                        return@BrutalistButton
                    }
                    isSending = true
                    errorMessage = null
                    scope.launch {
                        try {
                            val txHash = viewModel.sendAsset(asset, recipientAddress, amount)
                            onSendSuccess(txHash)
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Transaction failed"
                        } finally {
                            isSending = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
