package com.example.dockerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_credentials")
data class UserCredentials(
    @PrimaryKey
    val username: String,
    val password: String,
    val serverUrl: String,
    val isActive: Boolean = true
)
