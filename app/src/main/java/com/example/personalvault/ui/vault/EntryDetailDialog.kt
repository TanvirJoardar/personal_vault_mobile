package com.example.personalvault.ui.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.model.*
import com.example.personalvault.ui.theme.*

@Composable
fun EntryDetailDialog(
    entry: VaultEntry,
    onDismiss: () -> Unit,
    onEdit: (VaultEntry) -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    var showSensitiveData by remember { mutableStateOf(false) }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VaultBgSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { showSensitiveData = !showSensitiveData }) {
                    Icon(
                        imageVector = if (showSensitiveData) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Sensitive Info",
                        tint = AccentPrimary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Section badge
                Surface(
                    color = AccentPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = entry.sectionType.label,
                        color = AccentPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                when (entry) {
                    is VaultEntry.Password -> {
                        DetailItem("Account Name", entry.accountName, context)
                        DetailItem("Username / Email", entry.username, context)
                        DetailItem("Password", entry.passwordValue, context, isSensitive = true, isHidden = !showSensitiveData)
                        DetailItem("URL", entry.url, context)
                        DetailItem("Notes", entry.additionalInfo, context)
                    }
                    is VaultEntry.Document -> {
                        DetailItem("Document Name", entry.documentName, context)
                        DetailItem("Type", entry.documentType.name, context)
                        DetailItem("Description", entry.description, context)
                        if (entry.tags.isNotEmpty()) {
                            DetailItem("Tags", entry.tags.joinToString(", "), context)
                        }
                    }
                    is VaultEntry.DrivingLicense -> {
                        DetailItem("License Name", entry.licenseName, context)
                        DetailItem("Holder Name", entry.holderName, context)
                        DetailItem("License Number", entry.licenseNumber, context, isSensitive = true, isHidden = !showSensitiveData)
                        DetailItem("Issue Date", entry.issueDate, context)
                        DetailItem("Expiry Date", entry.expiryDate, context)
                    }
                    is VaultEntry.Certificate -> {
                        DetailItem("Certificate Name", entry.certificateName, context)
                        DetailItem("Institution", entry.institutionName, context)
                        DetailItem("Completion Year", entry.yearOfCompletion, context)
                        DetailItem("Description", entry.description, context)
                    }
                    is VaultEntry.IdCard -> {
                        DetailItem("ID Name", entry.cardName, context)
                        DetailItem("Holder Name", entry.holderName, context)
                        DetailItem("Card Number", entry.cardNumber, context, isSensitive = true, isHidden = !showSensitiveData)
                        DetailItem("Issue Date", entry.issueDate, context)
                        DetailItem("Expiry Date", entry.expiryDate, context)
                    }
                    is VaultEntry.Bank -> {
                        DetailItem("Bank Name", entry.bankName, context)
                        DetailItem("Account Holder", entry.accountHolderName, context)
                        DetailItem("Account Number", entry.accountNumber, context, isSensitive = true, isHidden = !showSensitiveData)
                        DetailItem("Branch Name", entry.branchName, context)
                        DetailItem("Routing Number", entry.routingNumber, context)
                        DetailItem("SWIFT Code", entry.swiftCode, context)

                        entry.cards.forEachIndexed { index, card ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Associated Card #${index + 1}", fontWeight = FontWeight.Bold, color = AccentSecondary, fontSize = 13.sp)
                            DetailItem("Card Number", card.cardNumber, context, isSensitive = true, isHidden = !showSensitiveData)
                            DetailItem("Expiry / CVV", "${card.expiryDate} / ${if (showSensitiveData) card.cvv else "***"}", context)
                            DetailItem("PIN", card.pin, context, isSensitive = true, isHidden = !showSensitiveData)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onEdit(entry); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                Button(
                    onClick = { onDelete(entry.id); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentDanger)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    context: Context,
    isSensitive: Boolean = false,
    isHidden: Boolean = false
) {
    if (value.isBlank()) return

    val displayValue = if (isSensitive && isHidden) "••••••••••••" else value

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VaultBgCard, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayValue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
