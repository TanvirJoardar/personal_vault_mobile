package com.example.personalvault.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.crypto.BiometricAuthManager
import com.example.personalvault.crypto.BiometricStatus
import com.example.personalvault.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentAutoLock: Int,
    isBiometricEnabled: Boolean = false,
    onAutoLockChange: (Int) -> Unit,
    onEnableBiometric: (masterPassword: String) -> Unit,
    onDisableBiometric: () -> Unit,
    onChangePassword: (String) -> Unit,
    onExportBackup: ((String) -> Unit) -> Unit,
    onImportBackup: (String) -> Unit,
    onWipeVault: () -> Unit,
    onBack: () -> Unit
) {
    var showChangePassDialog by remember { mutableStateOf(false) }
    var showEnableBiometricDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showWipeConfirmDialog by remember { mutableStateOf(false) }

    var exportedJsonText by remember { mutableStateOf("") }
    var importJsonInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vault Settings", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VaultBgPrimary)
            )
        },
        containerColor = VaultBgRoot
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Auto-lock timer section
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultBgSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Database Storage", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val dbSizeText = remember(context) {
                        try {
                            val dbFile = context.getDatabasePath("vault_database.db")
                            val walFile = context.getDatabasePath("vault_database.db-wal")
                            val shmFile = context.getDatabasePath("vault_database.db-shm")
                            var totalBytes = 0L
                            if (dbFile.exists()) totalBytes += dbFile.length()
                            if (walFile.exists()) totalBytes += walFile.length()
                            if (shmFile.exists()) totalBytes += shmFile.length()
                            
                            if (totalBytes < 1024) "$totalBytes B"
                            else if (totalBytes < 1024 * 1024) "%.2f KB".format(totalBytes / 1024.0)
                            else "%.2f MB".format(totalBytes / (1024.0 * 1024.0))
                        } catch (e: Exception) {
                            "Unknown"
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Occupied Vault DB Space", color = TextSecondary, fontSize = 13.sp)
                        Text(dbSizeText, color = AccentPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auto-lock timer section
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultBgSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Auto-Lock Inactivity Timeout", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf(1, 5, 15, 0).forEach { mins ->
                        val label = if (mins == 0) "Never" else "$mins minutes"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = currentAutoLock == mins,
                                onClick = { onAutoLockChange(mins) }
                            )
                            Text(label, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security & Passwords
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultBgSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Security & Authentication", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { showChangePassDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Master Password")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = VaultBgRoot)
                    Spacer(modifier = Modifier.height(16.dp))

                    val bioStatus = remember(context) { BiometricAuthManager.canAuthenticate(context) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = "Biometric Icon",
                            tint = AccentSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Biometric Unlock", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text(
                                text = when (bioStatus) {
                                    BiometricStatus.AVAILABLE -> "Fingerprint / Face Unlock alternative"
                                    BiometricStatus.NOT_ENROLLED -> "No fingerprint/face set up in system"
                                    BiometricStatus.NO_HARDWARE -> "Biometric hardware unavailable"
                                    BiometricStatus.UNAVAILABLE -> "Biometric hardware currently disabled"
                                },
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        if (bioStatus == BiometricStatus.AVAILABLE) {
                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        showEnableBiometricDialog = true
                                    } else {
                                        onDisableBiometric()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Backup & Restore
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultBgSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Encrypted Backup & Restore", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Text(
                        "Backups store your encrypted items securely in JSON format.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                onExportBackup { json ->
                                    exportedJsonText = json
                                    showExportDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Export JSON")
                        }

                        Button(
                            onClick = { showImportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Import JSON")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Danger Zone
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultBgSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AccentDanger)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Danger Zone", fontWeight = FontWeight.Bold, color = AccentDanger, fontSize = 16.sp)
                    }
                    Text(
                        "Wiping the vault permanently removes all stored passwords, documents, and data.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                    Button(
                        onClick = { showWipeConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentDanger),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset & Wipe Entire Vault")
                    }
                }
            }
        }
    }

    // Change Password Dialog
    if (showChangePassDialog) {
        var newPass by remember { mutableStateOf("") }
        var confirmNewPass by remember { mutableStateOf("") }
        var err by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePassDialog = false },
            containerColor = VaultBgSurface,
            title = { Text("Change Master Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it; err = null },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmNewPass,
                        onValueChange = { confirmNewPass = it; err = null },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (err != null) {
                        Text(err!!, color = AccentDanger, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newPass.length < 6) err = "Minimum 6 characters"
                    else if (newPass != confirmNewPass) err = "Passwords do not match"
                    else {
                        onChangePassword(newPass)
                        showChangePassDialog = false
                    }
                }) {
                    Text("Save New Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePassDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = VaultBgSurface,
            title = { Text("Encrypted Backup JSON") },
            text = {
                Column {
                    Text("Copy this encrypted JSON string to a safe backup location:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Vault Backup JSON", exportedJsonText))
                    showExportDialog = false
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Close") }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = VaultBgSurface,
            title = { Text("Import Backup JSON") },
            text = {
                Column {
                    Text("Paste an exported JSON backup below:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (importJsonInput.isNotBlank()) {
                        onImportBackup(importJsonInput)
                        showImportDialog = false
                    }
                }) {
                    Text("Import Backup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Enable Biometric Confirmation Dialog
    if (showEnableBiometricDialog) {
        var masterPassInput by remember { mutableStateOf("") }
        var passErr by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showEnableBiometricDialog = false },
            containerColor = VaultBgSurface,
            title = { Text("Enable Biometric Unlock") },
            text = {
                Column {
                    Text(
                        "Please enter your Master Password to authorize biometric unlock registration.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = masterPassInput,
                        onValueChange = { masterPassInput = it; passErr = null },
                        label = { Text("Master Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passErr != null) {
                        Text(passErr!!, color = AccentDanger, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (masterPassInput.isBlank()) {
                        passErr = "Password required"
                    } else {
                        onEnableBiometric(masterPassInput)
                        showEnableBiometricDialog = false
                    }
                }) {
                    Text("Enable Biometrics")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnableBiometricDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Wipe Confirmation Dialog
    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            containerColor = VaultBgSurface,
            title = { Text("Wipe Vault Data?", color = AccentDanger, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure? This will delete all master keys and encrypted entries permanently.") },
            confirmButton = {
                Button(
                    onClick = {
                        onWipeVault()
                        showWipeConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentDanger)
                ) {
                    Text("Wipe Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}
