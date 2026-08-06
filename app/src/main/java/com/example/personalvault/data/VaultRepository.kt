package com.example.personalvault.data

import com.example.personalvault.crypto.CryptoUtils
import com.example.personalvault.model.EncryptedEntryExport
import com.example.personalvault.model.VaultEntry
import com.example.personalvault.model.VaultExportData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.crypto.SecretKey

class VaultRepository(private val dao: VaultDao) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    var activeVaultKey: SecretKey? = null
        private set

    val vaultMeta: Flow<VaultMetaEntity?> = dao.getVaultMeta()

    val decryptedEntries: Flow<List<VaultEntry>> = dao.getAllEntries().map { entities ->
        val key = activeVaultKey ?: return@map emptyList()
        entities.mapNotNull { entity ->
            try {
                val decryptedJson = CryptoUtils.decryptData(key, entity.ciphertext, entity.iv)
                json.decodeFromString<VaultEntry>(decryptedJson)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun isVaultSetup(): Boolean {
        return dao.getVaultMetaDirect() != null
    }

    suspend fun setupVault(
        masterPassword: String,
        recoveryEmailHint: String = ""
    ): String {
        val vaultKeyRaw = CryptoUtils.generateVaultKey()
        val salt = CryptoUtils.generateSalt()
        val passwordWrappingKey = CryptoUtils.deriveKey(masterPassword, salt)
        val (encVK, encVKIV) = CryptoUtils.encryptVaultKey(vaultKeyRaw, passwordWrappingKey)

        val recoveryPhrase = CryptoUtils.generateRecoveryPhrase()
        val normalizedRecoveryPhrase = CryptoUtils.normalizeRecoveryKey(recoveryPhrase)
        val recoverySalt = CryptoUtils.generateSalt()
        val recoveryWrappingKey = CryptoUtils.deriveKey(normalizedRecoveryPhrase, recoverySalt)
        val (recEncVK, recEncVKIV) = CryptoUtils.encryptVaultKey(vaultKeyRaw, recoveryWrappingKey)

        val meta = VaultMetaEntity(
            salt = CryptoUtils.toBase64(salt),
            encryptedVaultKey = encVK,
            encryptedVaultKeyIV = encVKIV,
            recoverySalt = CryptoUtils.toBase64(recoverySalt),
            recoveryEncryptedVaultKey = recEncVK,
            recoveryEncryptedVaultKeyIV = recEncVKIV,
            recoveryEmailHint = recoveryEmailHint,
            autoLockMinutes = 5
        )

        dao.saveVaultMeta(meta)

        activeVaultKey = javax.crypto.spec.SecretKeySpec(vaultKeyRaw, "AES")
        return recoveryPhrase
    }

    suspend fun unlockVault(masterPassword: String): Boolean {
        val meta = dao.getVaultMetaDirect() ?: return false
        return try {
            val salt = CryptoUtils.fromBase64(meta.salt)
            val passwordWrappingKey = CryptoUtils.deriveKey(masterPassword, salt)
            val vaultKey = CryptoUtils.decryptVaultKey(
                meta.encryptedVaultKey,
                meta.encryptedVaultKeyIV,
                passwordWrappingKey
            )
            activeVaultKey = vaultKey
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun recoverVault(recoveryKeyInput: String): Boolean {
        val meta = dao.getVaultMetaDirect() ?: return false
        val recoverySalt = CryptoUtils.fromBase64(meta.recoverySalt)

        val formattedKey = CryptoUtils.normalizeRecoveryKey(recoveryKeyInput)
        val cleanKey = CryptoUtils.cleanRecoveryKeyRaw(recoveryKeyInput)
        val legacyRaw = recoveryKeyInput.trim().uppercase()

        val candidates = listOf(formattedKey, cleanKey, legacyRaw).distinct()

        for (candidate in candidates) {
            try {
                val recoveryWrappingKey = CryptoUtils.deriveKey(candidate, recoverySalt)
                val vaultKey = CryptoUtils.decryptVaultKey(
                    meta.recoveryEncryptedVaultKey,
                    meta.recoveryEncryptedVaultKeyIV,
                    recoveryWrappingKey
                )
                activeVaultKey = vaultKey
                return true
            } catch (e: Exception) {
                // try next candidate
            }
        }
        return false
    }

    suspend fun regenerateRecoveryKey(): String {
        val key = activeVaultKey ?: throw IllegalStateException("Vault is locked")
        val meta = dao.getVaultMetaDirect() ?: throw IllegalStateException("No vault metadata")

        val newRecoveryPhrase = CryptoUtils.generateRecoveryPhrase()
        val normalizedPhrase = CryptoUtils.normalizeRecoveryKey(newRecoveryPhrase)
        val newRecoverySalt = CryptoUtils.generateSalt()
        val recoveryWrappingKey = CryptoUtils.deriveKey(normalizedPhrase, newRecoverySalt)
        val (recEncVK, recEncVKIV) = CryptoUtils.encryptVaultKey(key.encoded, recoveryWrappingKey)

        val updatedMeta = meta.copy(
            recoverySalt = CryptoUtils.toBase64(newRecoverySalt),
            recoveryEncryptedVaultKey = recEncVK,
            recoveryEncryptedVaultKeyIV = recEncVKIV
        )
        dao.saveVaultMeta(updatedMeta)
        return newRecoveryPhrase
    }

    fun lockVault() {
        activeVaultKey = null
    }

    suspend fun saveEntry(entry: VaultEntry) {
        val key = activeVaultKey ?: throw IllegalStateException("Vault is locked")
        val plaintextJson = json.encodeToString(entry)
        val (ciphertext, iv) = CryptoUtils.encryptData(key, plaintextJson)

        val entity = VaultEntity(
            id = entry.id,
            sectionType = entry.sectionType.key,
            ciphertext = ciphertext,
            iv = iv,
            createdAt = entry.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertEntry(entity)
    }

    suspend fun deleteEntry(id: String) {
        dao.deleteEntryById(id)
    }

    suspend fun changeMasterPassword(newPassword: String) {
        val key = activeVaultKey ?: throw IllegalStateException("Vault is locked")
        val meta = dao.getVaultMetaDirect() ?: throw IllegalStateException("No vault meta")

        val newSalt = CryptoUtils.generateSalt()
        val newWrappingKey = CryptoUtils.deriveKey(newPassword, newSalt)
        val (encVK, encVKIV) = CryptoUtils.encryptVaultKey(key.encoded, newWrappingKey)

        val updatedMeta = meta.copy(
            salt = CryptoUtils.toBase64(newSalt),
            encryptedVaultKey = encVK,
            encryptedVaultKeyIV = encVKIV
        )
        dao.saveVaultMeta(updatedMeta)
    }

    suspend fun updateAutoLockMinutes(minutes: Int) {
        val meta = dao.getVaultMetaDirect() ?: return
        val updatedMeta = meta.copy(autoLockMinutes = minutes)
        dao.saveVaultMeta(updatedMeta)
    }

    suspend fun updateBiometricEnabled(enabled: Boolean) {
        val meta = dao.getVaultMetaDirect() ?: return
        val updatedMeta = meta.copy(isBiometricEnabled = enabled)
        dao.saveVaultMeta(updatedMeta)
    }

    suspend fun exportVaultJson(): String {
        val entities = dao.getAllEntriesDirectList()
        val exportList = entities.map {
            EncryptedEntryExport(
                id = it.id,
                sectionType = it.sectionType,
                ciphertext = it.ciphertext,
                iv = it.iv,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }
        val exportData = VaultExportData(entries = exportList)
        return json.encodeToString(exportData)
    }

    suspend fun importVaultJson(jsonString: String): Int {
        val exportData = json.decodeFromString<VaultExportData>(jsonString)
        var importedCount = 0
        exportData.entries.forEach { export ->
            val entity = VaultEntity(
                id = export.id,
                sectionType = export.sectionType,
                ciphertext = export.ciphertext,
                iv = export.iv,
                createdAt = export.createdAt,
                updatedAt = export.updatedAt
            )
            dao.insertEntry(entity)
            importedCount++
        }
        return importedCount
    }

    suspend fun wipeVault() {
        activeVaultKey = null
        dao.deleteAllEntries()
        dao.deleteVaultMeta()
    }
}
