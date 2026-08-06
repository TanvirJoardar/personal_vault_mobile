package com.example.personalvault.ui.vault

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalvault.crypto.BiometricAuthManager
import com.example.personalvault.data.VaultDatabase
import com.example.personalvault.data.VaultMetaEntity
import com.example.personalvault.data.VaultRepository
import com.example.personalvault.model.AppScreen
import com.example.personalvault.model.SectionType
import com.example.personalvault.model.VaultEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VaultRepository
    val vaultMeta: StateFlow<VaultMetaEntity?>

    private val _screen = MutableStateFlow(AppScreen.LOCK)
    val screen: StateFlow<AppScreen> = _screen.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedSection = MutableStateFlow<SectionType?>(null) // null = All
    val selectedSection: StateFlow<SectionType?> = _selectedSection.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _generatedRecoveryPhrase = MutableStateFlow<String?>(null)
    val generatedRecoveryPhrase: StateFlow<String?> = _generatedRecoveryPhrase.asStateFlow()

    // Dialog States
    private val _editingEntry = MutableStateFlow<VaultEntry?>(null)
    val editingEntry: StateFlow<VaultEntry?> = _editingEntry.asStateFlow()

    private val _showEntryForm = MutableStateFlow(false)
    val showEntryForm: StateFlow<Boolean> = _showEntryForm.asStateFlow()

    private val _showPasswordGenerator = MutableStateFlow(false)
    val showPasswordGenerator: StateFlow<Boolean> = _showPasswordGenerator.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    init {
        val dao = VaultDatabase.getDatabase(application).vaultDao()
        repository = VaultRepository(dao)
        vaultMeta = repository.vaultMeta.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        viewModelScope.launch {
            _isLoading.value = true
            val isSetup = repository.isVaultSetup()
            _screen.value = if (isSetup) AppScreen.LOCK else AppScreen.SETUP
            _isLoading.value = false
        }
    }

    val entries: StateFlow<List<VaultEntry>> = combine(
        repository.decryptedEntries,
        _selectedSection,
        _searchQuery,
        _onlyFavorites
    ) { allEntries, section, query, favOnly ->
        allEntries.filter { entry ->
            val matchesSection = (section == null) || (entry.sectionType == section)
            val matchesQuery = query.isBlank() || entry.title.contains(query, ignoreCase = true) ||
                    when (entry) {
                        is VaultEntry.Password -> entry.username.contains(query, ignoreCase = true) || entry.url.contains(query, ignoreCase = true)
                        is VaultEntry.Document -> entry.documentType.name.contains(query, ignoreCase = true)
                        is VaultEntry.DrivingLicense -> entry.holderName.contains(query, ignoreCase = true) || entry.licenseNumber.contains(query, ignoreCase = true)
                        is VaultEntry.Certificate -> entry.institutionName.contains(query, ignoreCase = true)
                        is VaultEntry.IdCard -> entry.cardNumber.contains(query, ignoreCase = true) || entry.holderName.contains(query, ignoreCase = true)
                        is VaultEntry.Bank -> entry.accountNumber.contains(query, ignoreCase = true) || entry.accountHolderName.contains(query, ignoreCase = true)
                    }
            val matchesFav = !favOnly || (entry is VaultEntry.Password && entry.isFavorite)
            matchesSection && matchesQuery && matchesFav
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun setupVault(password: String, emailHint: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val phrase = repository.setupVault(password, emailHint)
                _generatedRecoveryPhrase.value = phrase
                _screen.value = AppScreen.SETUP
                showToast("Vault setup successfully! Keep your recovery key safe.")
            } catch (e: Exception) {
                showToast("Failed to setup vault: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun completeSetupAndEnterVault() {
        _generatedRecoveryPhrase.value = null
        _screen.value = AppScreen.MAIN
    }

    fun unlockVault(password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.unlockVault(password)
            if (success) {
                _screen.value = AppScreen.MAIN
                showToast("Vault unlocked")
            } else {
                showToast("Incorrect master password")
            }
            _isLoading.value = false
        }
    }

    fun recoverVault(recoveryKey: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.recoverVault(recoveryKey)
            if (success) {
                _screen.value = AppScreen.MAIN
                showToast("Vault unlocked with recovery key")
            } else {
                showToast("Invalid recovery key")
            }
            _isLoading.value = false
        }
    }

    fun lockVault() {
        repository.lockVault()
        _screen.value = AppScreen.LOCK
        showToast("Vault locked")
    }

    fun navigateToRecovery() {
        _screen.value = AppScreen.RECOVERY
    }

    fun navigateToLock() {
        _screen.value = AppScreen.LOCK
    }

    fun setSection(section: SectionType?) {
        _selectedSection.value = section
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesFilter() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    fun openEntryForm(entryToEdit: VaultEntry? = null) {
        _editingEntry.value = entryToEdit
        _showEntryForm.value = true
    }

    fun closeEntryForm() {
        _showEntryForm.value = false
        _editingEntry.value = null
    }

    fun openPasswordGenerator() {
        _showPasswordGenerator.value = true
    }

    fun closePasswordGenerator() {
        _showPasswordGenerator.value = false
    }

    fun openSettings() {
        _showSettings.value = true
    }

    fun closeSettings() {
        _showSettings.value = false
    }

    fun saveEntry(entry: VaultEntry) {
        viewModelScope.launch {
            try {
                repository.saveEntry(entry)
                closeEntryForm()
                showToast("Entry saved")
            } catch (e: Exception) {
                showToast("Error saving entry: ${e.localizedMessage}")
            }
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteEntry(id)
                showToast("Entry deleted")
            } catch (e: Exception) {
                showToast("Error deleting entry: ${e.localizedMessage}")
            }
        }
    }

    fun changePassword(newPassword: String, context: Context? = null) {
        viewModelScope.launch {
            try {
                repository.changeMasterPassword(newPassword)
                if (context != null && vaultMeta.value?.isBiometricEnabled == true) {
                    BiometricAuthManager.saveEncryptedMasterPassword(context, newPassword)
                }
                showToast("Master password updated successfully")
            } catch (e: Exception) {
                showToast("Error updating password: ${e.localizedMessage}")
            }
        }
    }

    fun enableBiometric(context: Context, masterPassword: String) {
        viewModelScope.launch {
            val success = BiometricAuthManager.saveEncryptedMasterPassword(context, masterPassword)
            if (success) {
                repository.updateBiometricEnabled(true)
                showToast("Biometric unlock enabled successfully")
            } else {
                showToast("Failed to save biometric key")
            }
        }
    }

    fun disableBiometric(context: Context) {
        viewModelScope.launch {
            BiometricAuthManager.clearBiometricData(context)
            repository.updateBiometricEnabled(false)
            showToast("Biometric unlock disabled")
        }
    }

    fun updateAutoLock(minutes: Int) {
        viewModelScope.launch {
            repository.updateAutoLockMinutes(minutes)
            showToast("Auto-lock set to $minutes min")
        }
    }

    fun regenerateRecoveryKey(onKeyGenerated: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val newKey = repository.regenerateRecoveryKey()
                onKeyGenerated(newKey)
                showToast("New Recovery Key generated")
            } catch (e: Exception) {
                showToast("Error generating recovery key: ${e.localizedMessage}")
            }
        }
    }

    fun exportBackup(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = repository.exportVaultJson()
                onExportReady(json)
                showToast("Backup generated")
            } catch (e: Exception) {
                showToast("Export failed: ${e.localizedMessage}")
            }
        }
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            try {
                val count = repository.importVaultJson(jsonString)
                showToast("Imported $count entries")
            } catch (e: Exception) {
                showToast("Import failed: ${e.localizedMessage}")
            }
        }
    }

    fun wipeVault(context: Context? = null) {
        viewModelScope.launch {
            if (context != null) {
                BiometricAuthManager.clearBiometricData(context)
            }
            repository.wipeVault()
            _generatedRecoveryPhrase.value = null
            _screen.value = AppScreen.SETUP
            closeSettings()
            showToast("Vault reset. Setup a new master password.")
        }
    }
}
