package com.example.personalvault.ui.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.model.*
import com.example.personalvault.ui.settings.SettingsScreen
import com.example.personalvault.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultMainScreen(
    viewModel: VaultViewModel
) {
    val entries by viewModel.entries.collectAsState()
    val selectedSection by viewModel.selectedSection.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val onlyFavorites by viewModel.onlyFavorites.collectAsState()

    val showForm by viewModel.showEntryForm.collectAsState()
    val editingEntry by viewModel.editingEntry.collectAsState()
    val showGenerator by viewModel.showPasswordGenerator.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val meta by viewModel.vaultMeta.collectAsState()

    var viewingDetailEntry by remember { mutableStateOf<VaultEntry?>(null) }
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    if (showSettings) {
        SettingsScreen(
            currentAutoLock = meta?.autoLockMinutes ?: 5,
            isBiometricEnabled = meta?.isBiometricEnabled ?: false,
            onAutoLockChange = { viewModel.updateAutoLock(it) },
            onEnableBiometric = { masterPwd -> viewModel.enableBiometric(context, masterPwd) },
            onDisableBiometric = { viewModel.disableBiometric(context) },
            onChangePassword = { newPwd -> viewModel.changePassword(newPwd, context) },
            onRegenerateRecoveryKey = { callback -> viewModel.regenerateRecoveryKey(callback) },
            onExportBackup = { viewModel.exportBackup(it) },
            onImportBackup = { viewModel.importBackup(it) },
            onWipeVault = { viewModel.wipeVault(context) },
            onBack = { viewModel.closeSettings() }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = VaultBgSurface,
                drawerContentColor = TextPrimary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 20.dp, top = 12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Vault Navigation", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                    }

                    HorizontalDivider(color = VaultBgCard)
                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationDrawerItem(
                        label = { Text("All Vault Items") },
                        selected = selectedSection == null,
                        onClick = {
                            viewModel.setSection(null)
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = AccentPrimary.copy(alpha = 0.2f),
                            selectedIconColor = AccentPrimary,
                            selectedTextColor = AccentPrimary
                        )
                    )

                    SectionType.values().forEach { sec ->
                        val color = getSectionColor(sec)
                        val icon = getSectionIcon(sec)
                        NavigationDrawerItem(
                            label = { Text(sec.label) },
                            selected = selectedSection == sec,
                            onClick = {
                                viewModel.setSection(sec)
                                coroutineScope.launch { drawerState.close() }
                            },
                            icon = { Icon(icon, contentDescription = null, tint = if (selectedSection == sec) color else TextSecondary) },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = color.copy(alpha = 0.2f),
                                selectedTextColor = color
                            )
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    HorizontalDivider(color = VaultBgCard)
                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = false,
                        onClick = {
                            viewModel.openSettings()
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = TextSecondary) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Lock Vault") },
                        selected = false,
                        onClick = {
                            viewModel.lockVault()
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AccentDanger) }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu Sidebar", tint = TextPrimary)
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = AccentPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Personal Vault", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                Text("Zero-Knowledge Storage", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleFavoritesFilter() }) {
                            Icon(
                                imageVector = if (onlyFavorites) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorites",
                                tint = if (onlyFavorites) AccentWarning else TextSecondary
                            )
                        }
                        IconButton(onClick = { viewModel.openSettings() }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                        }
                        IconButton(onClick = { viewModel.lockVault() }) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = AccentDanger)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = VaultBgPrimary)
                )
            },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openEntryForm(null) },
                containerColor = AccentPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        },
        containerColor = VaultBgRoot
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search passwords, cards, licenses...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = VaultBgSurface,
                    unfocusedContainerColor = VaultBgSurface
                )
            )

            // Section Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedSection == null,
                        onClick = { viewModel.setSection(null) },
                        label = { Text("All Items", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
                items(SectionType.values()) { sec ->
                    FilterChip(
                        selected = selectedSection == sec,
                        onClick = { viewModel.setSection(sec) },
                        label = { Text(sec.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = getSectionColor(sec),
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Entries List or Empty State
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Empty",
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No Entries Found", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                        Text(
                            "Tap the + button to add your first encrypted item.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        VaultEntryCard(
                            entry = entry,
                            onClick = { viewingDetailEntry = entry },
                            onQuickCopy = { label, valToCopy ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText(label, valToCopy))
                                viewModel.showToast("Copied $label")
                            },
                            onToggleFavorite = { passEntry ->
                                val updated = passEntry.copy(isFavorite = !passEntry.isFavorite)
                                viewModel.saveEntry(updated)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    }

    // Detail Dialog
    if (viewingDetailEntry != null) {
        EntryDetailDialog(
            entry = viewingDetailEntry!!,
            onDismiss = { viewingDetailEntry = null },
            onEdit = { viewModel.openEntryForm(it) },
            onDelete = { viewModel.deleteEntry(it) }
        )
    }

    // Form Dialog
    if (showForm) {
        EntryFormDialog(
            initialSection = selectedSection,
            editingEntry = editingEntry,
            onDismiss = { viewModel.closeEntryForm() },
            onSave = { viewModel.saveEntry(it) },
            onOpenGenerator = { viewModel.openPasswordGenerator() }
        )
    }

    // Generator Dialog
    if (showGenerator) {
        PasswordGeneratorDialog(
            onDismiss = { viewModel.closePasswordGenerator() },
            onUsePassword = { generated ->
                // Handled in entry form
            }
        )
    }
}

@Composable
fun VaultEntryCard(
    entry: VaultEntry,
    onClick: () -> Unit,
    onQuickCopy: (label: String, valToCopy: String) -> Unit,
    onToggleFavorite: (VaultEntry.Password) -> Unit
) {
    val color = getSectionColor(entry.sectionType)
    val icon = getSectionIcon(entry.sectionType)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = VaultBgSurface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )

                val subText = when (entry) {
                    is VaultEntry.Password -> entry.username.ifBlank { entry.category }
                    is VaultEntry.Document -> entry.documentType.name
                    is VaultEntry.DrivingLicense -> "License: ${entry.licenseNumber}"
                    is VaultEntry.Certificate -> entry.institutionName
                    is VaultEntry.IdCard -> "ID: ${entry.cardNumber}"
                    is VaultEntry.Bank -> "Acc: ${entry.accountNumber}"
                }

                if (subText.isNotBlank()) {
                    Text(
                        text = subText,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Action icon button (Quick Copy)
            if (entry is VaultEntry.Password) {
                IconButton(onClick = { onToggleFavorite(entry) }) {
                    Icon(
                        imageVector = if (entry.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (entry.isFavorite) AccentWarning else TextMuted
                    )
                }
                if (entry.passwordValue.isNotBlank()) {
                    IconButton(onClick = { onQuickCopy("Password", entry.passwordValue) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Password", tint = AccentPrimary)
                    }
                }
            }
        }
    }
}

private fun getSectionColor(sectionType: SectionType): Color {
    return when (sectionType) {
        SectionType.PASSWORD -> AccentPrimary
        SectionType.DOCUMENT -> AccentSecondary
        SectionType.DRIVING_LICENSE -> AccentWarning
        SectionType.CERTIFICATE -> AccentSuccess
        SectionType.ID_CARD -> AccentDanger
        SectionType.BANK -> AccentPurple
    }
}

private fun getSectionIcon(sectionType: SectionType) = when (sectionType) {
    SectionType.PASSWORD -> Icons.Default.Key
    SectionType.DOCUMENT -> Icons.Default.Description
    SectionType.DRIVING_LICENSE -> Icons.Default.DirectionsCar
    SectionType.CERTIFICATE -> Icons.Default.School
    SectionType.ID_CARD -> Icons.Default.Badge
    SectionType.BANK -> Icons.Default.AccountBalance
}
