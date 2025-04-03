package com.example.dockerapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dockerapp.data.model.UserCredentials
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCredentialsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCredentials(userCredentials: UserCredentials)

    @Delete
    suspend fun delete(userCredentials: UserCredentials)

    @Query("SELECT * FROM user_credentials WHERE isActive = 1 LIMIT 1")
    fun getActiveCredentials(): Flow<UserCredentials?>
    
    @Query("SELECT COUNT(*) FROM user_credentials WHERE isActive = 1")
    suspend fun hasActiveCredentials(): Int
    
    @Query("UPDATE user_credentials SET isActive = 0")
    suspend fun deactivateAllCredentials()

    @Query("DELETE FROM user_credentials")
    suspend fun deleteAllCredentials()
}