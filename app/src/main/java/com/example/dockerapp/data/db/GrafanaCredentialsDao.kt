package com.example.dockerapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dockerapp.data.model.GrafanaCredentials
import kotlinx.coroutines.flow.Flow

@Dao
interface GrafanaCredentialsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCredentials(credentials: GrafanaCredentials)

    @Query("SELECT * FROM grafana_credentials WHERE id = 1 LIMIT 1")
    fun getCredentials(): Flow<GrafanaCredentials?>

    @Query("DELETE FROM grafana_credentials")
    suspend fun deleteAllCredentials()
}