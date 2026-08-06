package com.example.personalvault.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Query("SELECT * FROM vault_entries ORDER BY updatedAt DESC")
    fun getAllEntries(): Flow<List<VaultEntity>>

    @Query("SELECT * FROM vault_entries ORDER BY updatedAt DESC")
    suspend fun getAllEntriesDirectList(): List<VaultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: VaultEntity)

    @Query("DELETE FROM vault_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)

    @Query("DELETE FROM vault_entries")
    suspend fun deleteAllEntries()

    @Query("SELECT * FROM vault_meta WHERE id = 'vault_meta' LIMIT 1")
    fun getVaultMeta(): Flow<VaultMetaEntity?>

    @Query("SELECT * FROM vault_meta WHERE id = 'vault_meta' LIMIT 1")
    suspend fun getVaultMetaDirect(): VaultMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVaultMeta(meta: VaultMetaEntity)

    @Query("DELETE FROM vault_meta")
    suspend fun deleteVaultMeta()
}
