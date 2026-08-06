package com.example.personalvault.ui.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.crypto.CryptoUtils
import com.example.personalvault.ui.theme.*

@Composable
fun PasswordGeneratorDialog(
    onDismiss: () -> Unit,
    onUsePassword: (String) -> Unit
) {
    var length by remember { mutableFloatStateOf(16f) }
    var useUpper by remember { mutableStateOf(true) }
    var useLower by remember { mutableStateOf(true) }
    var useNumbers by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }

    var generatedPassword by remember {
        mutableStateOf(
            CryptoUtils.generatePassword(
                length.toInt(), useUpper, useLower, useNumbers, useSymbols
            )
        )
    }

    val context = LocalContext.current

    fun regenerate() {
        generatedPassword = CryptoUtils.generatePassword(
            length.toInt(), useUpper, useLower, useNumbers, useSymbols
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VaultBgSurface,
        title = {
            Text("Password Generator", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Password display box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VaultBgCard, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = generatedPassword,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { regenerate() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = AccentSecondary)
                    }

                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Generated Password", generatedPassword))
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Length: ${length.toInt()}", fontSize = 14.sp, color = TextPrimary)
                Slider(
                    value = length,
                    onValueChange = {
                        length = it
                        regenerate()
                    },
                    valueRange = 8f..32f,
                    steps = 23,
                    colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Uppercase (A-Z)", color = TextPrimary, fontSize = 13.sp)
                    Checkbox(checked = useUpper, onCheckedChange = { useUpper = it; regenerate() })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lowercase (a-z)", color = TextPrimary, fontSize = 13.sp)
                    Checkbox(checked = useLower, onCheckedChange = { useLower = it; regenerate() })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Numbers (0-9)", color = TextPrimary, fontSize = 13.sp)
                    Checkbox(checked = useNumbers, onCheckedChange = { useNumbers = it; regenerate() })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Symbols (!@#$)", color = TextPrimary, fontSize = 13.sp)
                    Checkbox(checked = useSymbols, onCheckedChange = { useSymbols = it; regenerate() })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onUsePassword(generatedPassword); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
            ) {
                Text("Use Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
