package com.example.personalvault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_entries")
data class VaultEntity(
    @PrimaryKey val id: String,
    val sectionType: String,
    val ciphertext: String,
    val iv: String,
    val createdAt: Long,
    val updatedAt: Long
)
