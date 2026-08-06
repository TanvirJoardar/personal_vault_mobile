package com.example.personalvault

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.personalvault.model.AppScreen
import com.example.personalvault.ui.auth.LockScreen
import com.example.personalvault.ui.auth.RecoveryScreen
import com.example.personalvault.ui.auth.SetupScreen
import com.example.personalvault.ui.theme.AccentPrimary
import com.example.personalvault.ui.theme.PersonalVaultTheme
import com.example.personalvault.ui.theme.VaultBgRoot
import com.example.personalvault.ui.vault.VaultMainScreen
import com.example.personalvault.ui.vault.VaultViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PersonalVaultTheme {
                VaultAppContent(viewModel)
            }
        }
    }
}

@Composable
fun VaultAppContent(viewModel: VaultViewModel) {
    val screen by viewModel.screen.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()
    val recoveryPhrase by viewModel.generatedRecoveryPhrase.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VaultBgRoot),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AccentPrimary)
        }
    } else {
        when (screen) {
            AppScreen.SETUP -> {
                SetupScreen(
                    recoveryPhrase = recoveryPhrase,
                    onSetup = { pwd, hint -> viewModel.setupVault(pwd, hint) },
                    onContinue = { viewModel.unlockVault("") }
                )
            }

            AppScreen.LOCK -> {
                LockScreen(
                    onUnlock = { pwd -> viewModel.unlockVault(pwd) },
                    onNavigateToRecovery = { viewModel.navigateToRecovery() },
                    onResetVault = { viewModel.wipeVault() }
                )
            }

            AppScreen.RECOVERY -> {
                RecoveryScreen(
                    onRecover = { key -> viewModel.recoverVault(key) },
                    onBackToLock = { viewModel.navigateToLock() }
                )
            }

            AppScreen.MAIN -> {
                VaultMainScreen(viewModel = viewModel)
            }
        }
    }
}
