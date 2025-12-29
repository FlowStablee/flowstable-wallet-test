package com.antigravity.cryptowallet.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.cryptowallet.ui.components.BrutalistHeader
import com.antigravity.cryptowallet.ui.security.SecurityViewModel
import com.antigravity.cryptowallet.ui.theme.BrutalBlack
import com.antigravity.cryptowallet.ui.theme.BrutalWhite

@Composable
fun SeedRevealScreen(
    onBack: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val mnemonic = remember { viewModel.getMnemonic() }
    val words = mnemonic.split(" ")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalWhite)
            .padding(24.dp)
    ) {
        BrutalistHeader("Secret Phrase")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Write down these 12 words in order and keep them safe. NEVER share them with anyone.",
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Grid of words
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            words.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEachIndexed { indexInRow, word ->
                        val globalIndex = words.indexOf(word) + 1
                        WordBox(index = globalIndex, word = word, modifier = Modifier.weight(1f))
                    }
                    if (row.size < 3) {
                        repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .border(2.dp, BrutalBlack),
            colors = ButtonDefaults.buttonColors(containerColor = BrutalBlack),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text("I HAVE SAVED IT", color = BrutalWhite, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WordBox(index: Int, word: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .border(1.dp, BrutalBlack)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index.",
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.width(20.dp)
        )
        Text(
            text = word,
            fontWeight = FontWeight.Bold,
            color = BrutalBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
