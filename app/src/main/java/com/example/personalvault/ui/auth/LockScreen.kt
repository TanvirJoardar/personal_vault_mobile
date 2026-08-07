package com.example.personalvault.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.personalvault.crypto.BiometricAuthManager
import com.example.personalvault.crypto.BiometricStatus
import com.example.personalvault.ui.theme.*

@Composable
fun LockScreen(
    isBiometricEnabled: Boolean = false,
    onUnlock: (password: String) -> Unit,
    onNavigateToRecovery: () -> Unit,
    onResetVault: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var biometricErrorMsg by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity

    val canUseBiometrics = remember(isBiometricEnabled) {
        isBiometricEnabled &&
                BiometricAuthManager.canAuthenticate(context) == BiometricStatus.AVAILABLE &&
                BiometricAuthManager.hasSavedCredentials(context)
    }

    fun launchBiometricPrompt() {
        if (fragmentActivity != null && canUseBiometrics) {
            BiometricAuthManager.promptBiometricUnlock(
                activity = fragmentActivity,
                onSuccess = { masterPassword ->
                    onUnlock(masterPassword)
                },
                onError = { err ->
                    biometricErrorMsg = err
                }
            )
        }
    }

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
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Icon",
                    tint = AccentPrimary,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Vault Locked",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Enter your master password to unlock",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Master Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (password.isNotBlank()) onUnlock(password)
                    }),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { if (password.isNotBlank()) onUnlock(password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Unlock Vault", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                if (canUseBiometrics) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { launchBiometricPrompt() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentSecondary)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = "Biometric Unlock", modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unlock with Fingerprint", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (biometricErrorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(biometricErrorMsg!!, color = AccentDanger, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onNavigateToRecovery) {
                    Text("Forgot Password? Use Recovery Key", color = AccentSecondary, fontSize = 13.sp)
                }

                TextButton(onClick = { showResetConfirmDialog = true }) {
                    Text("Create New Master Data", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = VaultBgSurface,
            title = { Text("Create New Master Data?", color = AccentDanger, fontWeight = FontWeight.Bold) },
            text = { Text("Creating new master data will erase and reset all existing vault data. Are you sure you want to proceed?") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        onResetVault()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentDanger)
                ) {
                    Text("Reset & Create New")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
