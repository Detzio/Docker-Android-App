package com.example.dockerapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grafana_credentials")
data class GrafanaCredentials(
    @PrimaryKey
    val id: Int = 1, // Un seul enregistrement
    val username: String,
    val password: String,
    val serverUrl: String
)