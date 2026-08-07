package com.example.personalvault.ui.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
                        if (entry.signInProvider != SignInProvider.NONE) {
                            val providerStr = if (entry.signInProvider == SignInProvider.CUSTOM) {
                                entry.customSignInProvider.ifBlank { "Custom" }
                            } else {
                                entry.signInProvider.name.lowercase().replaceFirstChar { it.uppercase() }
                            }
                            DetailItem("Sign-in Method", providerStr, context)
                        }
                        DetailItem("Additional Info / Notes", entry.additionalInfo, context)
                        RenderAttachments(entry.attachments)
                    }
                    is VaultEntry.Document -> {
                        DetailItem("Document Name", entry.documentName, context)
                        val docTypeStr = if (entry.documentType == DocumentType.CUSTOM) {
                            entry.customDocumentType.ifBlank { "Custom" }
                        } else {
                            entry.documentType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                        }
                        DetailItem("Type", docTypeStr, context)
                        DetailItem("Description", entry.description, context)
                        if (entry.tags.isNotEmpty()) {
                            DetailItem("Tags", entry.tags.joinToString(", "), context)
                        }
                        RenderAttachments(entry.files)
                    }
                    is VaultEntry.DrivingLicense -> {
                        DetailItem("License Name", entry.licenseName, context)
                        DetailItem("Holder Name", entry.holderName, context)
                        DetailItem("License Number", entry.licenseNumber, context, isSensitive = true, isHidden = !showSensitiveData)
                        val licTypeStr = if (entry.licenseType == LicenseType.CUSTOM) {
                            entry.customLicenseType.ifBlank { "Custom" }
                        } else {
                            entry.licenseType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                        }
                        DetailItem("License Type", licTypeStr, context)
                        DetailItem("Issue Date", entry.issueDate, context)
                        DetailItem("Expiry Date", entry.expiryDate, context)
                        RenderAttachments(entry.files)
                    }
                    is VaultEntry.Certificate -> {
                        DetailItem("Certificate Name", entry.certificateName, context)
                        val certTypeStr = if (entry.certificateType == CertificateType.CUSTOM) {
                            entry.customCertificateType.ifBlank { "Custom" }
                        } else {
                            entry.certificateType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                        }
                        DetailItem("Certificate Type", certTypeStr, context)
                        DetailItem("Institution", entry.institutionName, context)
                        DetailItem("Completion Year", entry.yearOfCompletion, context)
                        DetailItem("Description", entry.description, context)
                        RenderAttachments(entry.files)
                    }
                    is VaultEntry.IdCard -> {
                        DetailItem("ID Name", entry.cardName, context)
                        val idTypeStr = if (entry.cardType == IdCardType.CUSTOM) {
                            entry.customCardType.ifBlank { "Custom" }
                        } else {
                            entry.cardType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                        }
                        DetailItem("Card Type", idTypeStr, context)
                        DetailItem("Holder Name", entry.holderName, context)
                        DetailItem("Card Number", entry.cardNumber, context, isSensitive = true, isHidden = !showSensitiveData)
                        DetailItem("Issue Date", entry.issueDate, context)
                        DetailItem("Expiry Date", entry.expiryDate, context)
                        DetailItem("Description", entry.description, context)
                        RenderAttachments(entry.files)
                    }
                    is VaultEntry.Bank -> {
                        DetailItem("Bank Name", entry.bankName, context)
                        val bankTypeStr = if (entry.bankType == BankAccountType.CUSTOM) {
                            entry.customBankType.ifBlank { "Custom" }
                        } else {
                            entry.bankType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                        }
                        DetailItem("Account Type", bankTypeStr, context)
                        DetailItem("Branch Name", entry.branchName, context)
                        DetailItem("Account Number", entry.accountNumber, context, isSensitive = true, isHidden = !showSensitiveData)
                        DetailItem("Account Holder", entry.accountHolderName, context)
                        DetailItem("Routing Number", entry.routingNumber, context)
                        DetailItem("SWIFT Code", entry.swiftCode, context)
                        DetailItem("IFSC Code", entry.ifscCode, context)
                        DetailItem("Notes", entry.additionalInfo, context)

                        if (entry.cards.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Bank Cards (${entry.cards.size})", fontWeight = FontWeight.Bold, color = AccentSecondary, fontSize = 14.sp)
                            entry.cards.forEachIndexed { index, card ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(VaultBgCard, RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    val cardTypeStr = if (card.cardType == BankCardType.CUSTOM) {
                                        card.customCardType.ifBlank { "Custom" }
                                    } else {
                                        card.cardType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                                    }
                                    Text("Card #${index + 1} ($cardTypeStr)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                    DetailItem("Card Number", card.cardNumber, context, isSensitive = true, isHidden = !showSensitiveData)
                                    DetailItem("PIN", card.pin, context, isSensitive = true, isHidden = !showSensitiveData)
                                    DetailItem("CVV", card.cvv, context, isSensitive = true, isHidden = !showSensitiveData)
                                    DetailItem("Expiry Date", card.expiryDate, context)

                                    val cardFiles = listOfNotNull(card.cardImage) + card.attachments
                                    if (cardFiles.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Card Scans & Images", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                        RenderAttachments(cardFiles)
                                    }
                                }
                            }
                        }

                        val bankFiles = listOfNotNull(entry.signatureFile)
                        if (bankFiles.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Bank Signature / File", fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 12.sp)
                            RenderAttachments(bankFiles)
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

@Composable
private fun RenderAttachments(files: List<EncryptedFileAttachment>) {
    if (files.isEmpty()) return

    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = VaultBgCard)
    Spacer(modifier = Modifier.height(8.dp))
    Text("Attached Files & Images", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentPrimary)

    files.forEach { file ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(VaultBgCard, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (file.mimeType.startsWith("image/")) Icons.Default.Image else Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(file.fileName, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text("${file.size / 1024} KB", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }

            if (file.mimeType.startsWith("image/") && file.encryptedData.isNotBlank()) {
                val bitmap = remember(file.encryptedData) {
                    try {
                        val bytes = Base64.decode(file.encryptedData, Base64.NO_WRAP)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (bitmap != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = file.fileName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .background(VaultBgSurface, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
