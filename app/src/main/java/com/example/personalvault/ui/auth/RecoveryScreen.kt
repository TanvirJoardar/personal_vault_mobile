package com.example.personalvault.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.ui.theme.*

@Composable
fun RecoveryScreen(
    recoveryEmailHint: String = "",
    onRecover: (recoveryKey: String) -> Unit,
    onBackToLock: () -> Unit
) {
    var recoveryKey by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultBgRoot)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp),
            colors = CardDefaults.cardColors(containerColor = VaultBgSurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Recovery Key Icon",
                    tint = AccentSecondary,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Vault Recovery",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Enter your 16-character recovery key (e.g. A1B2-C3D4-E5F6-7890)",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                if (recoveryEmailHint.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = VaultBgPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Recovery Hint:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentSecondary
                            )
                            Text(
                                text = recoveryEmailHint,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = recoveryKey,
                    onValueChange = { input ->
                        // Clean input and format cleanly as UPPERCASE
                        val clean = input.filter { it.isLetterOrDigit() }.uppercase()
                        recoveryKey = if (clean.length <= 16) {
                            clean.chunked(4).joinToString("-")
                        } else {
                            input.trim().uppercase()
                        }
                    },
                    label = { Text("16-Character Recovery Key") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { if (recoveryKey.isNotBlank()) onRecover(recoveryKey) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentSecondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Recover & Unlock Vault", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onBackToLock) {
                    Text("Back to Master Password Login", color = AccentPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}
