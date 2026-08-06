package com.example.personalvault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_meta")
data class VaultMetaEntity(
    @PrimaryKey val id: String = "vault_meta",
    val createdAt: Long = System.currentTimeMillis(),
    val salt: String,
    val encryptedVaultKey: String,
    val encryptedVaultKeyIV: String,
    val recoverySalt: String,
    val recoveryEncryptedVaultKey: String,
    val recoveryEncryptedVaultKeyIV: String,
    val recoveryEmailHint: String = "",
    val autoLockMinutes: Int = 5
)
