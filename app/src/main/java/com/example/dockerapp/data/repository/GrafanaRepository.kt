package com.example.dockerapp.data.repository

import com.example.dockerapp.data.api.GrafanaRetrofitClient
import com.example.dockerapp.data.db.GrafanaCredentialsDao
import com.example.dockerapp.data.model.GrafanaCredentials
import com.example.dockerapp.data.model.*
import com.example.dockerapp.data.api.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class GrafanaRepository(private val grafanaCredentialsDao: GrafanaCredentialsDao) {
    
    suspend fun testConnection(username: String, password: String, serverUrl: String): Boolean {
        return try {
            GrafanaRetrofitClient.setCredentials(username, password, serverUrl)
            val response = GrafanaRetrofitClient.apiService.getHealth()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun saveCredentials(username: String, password: String, serverUrl: String) {
        val credentials = GrafanaCredentials(
            username = username,
            password = password,
            serverUrl = serverUrl
        )
        grafanaCredentialsDao.saveCredentials(credentials)
    }
    
    fun getCredentials(): Flow<GrafanaCredentials?> {
        return grafanaCredentialsDao.getCredentials()
    }
    
    suspend fun clearCredentials() {
        grafanaCredentialsDao.deleteAllCredentials()
        GrafanaRetrofitClient.clearCredentials()
    }
    
    suspend fun getDashboards(): List<GrafanaDashboard> {
        return try {
            val response = GrafanaRetrofitClient.apiService.searchDashboards()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDataSources(): List<GrafanaDataSource> {
        return try {
            val response = GrafanaRetrofitClient.apiService.getDataSources()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun queryMetrics(
        dataSourceUid: String,
        expression: String,
        timeRange: Pair<String, String> = getDefaultTimeRange()
    ): List<QueryResult> {
        return try {
            val request = GrafanaQueryRequest(
                queries = listOf(
                    GrafanaQuery(
                        refId = "A",
                        expr = expression,
                        datasource = QueryDataSource(
                            type = "prometheus",
                            uid = dataSourceUid
                        )
                    )
                ),
                from = timeRange.first,
                to = timeRange.second
            )
            
            val response = GrafanaRetrofitClient.apiService.queryData(request)
            if (response.isSuccessful) {
                response.body()?.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getAvailableMetrics(): List<String> {
        return try {
            // Essayer de récupérer les métriques disponibles via l'API label values
            val commonMetrics = listOf(
                "up",
                "prometheus_build_info",
                "container_cpu_usage_seconds_total",
                "container_memory_usage_bytes",
                "container_network_receive_bytes_total",
                "node_cpu_seconds_total",
                "node_memory_MemTotal_bytes",
                "docker_container_cpu_usage_percent",
                "docker_container_memory_usage_bytes"
            )
            commonMetrics
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun getDefaultTimeRange(): Pair<String, String> {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60 * 60 * 1000)
        
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        
        return Pair(
            format.format(Date(oneHourAgo)),
            format.format(Date(now))
        )
    }
    
    fun buildGrafanaUrl(serverUrl: String): String {
        // Convertir l'URL Docker vers l'URL Grafana (port 3000)
        var grafanaUrl = serverUrl.trim()
        
        // Supprimer '/info' s'il est présent
        if (grafanaUrl.endsWith("/info")) {
            grafanaUrl = grafanaUrl.substring(0, grafanaUrl.length - 5)
        }
        
        // Remplacer le port par 3000
        grafanaUrl = grafanaUrl.replace(":2376", ":3000")
            .replace(":2377", ":3000")
            .replace(":2375", ":3000")
        
        // S'assurer que l'URL se termine par '/'
        if (!grafanaUrl.endsWith("/")) {
            grafanaUrl += "/"
        }
        
        return grafanaUrl
    }
}