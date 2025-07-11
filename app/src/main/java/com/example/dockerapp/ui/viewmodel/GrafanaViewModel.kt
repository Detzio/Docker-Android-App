package com.example.dockerapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dockerapp.data.api.GrafanaRetrofitClient
import com.example.dockerapp.data.db.AppDatabase
import com.example.dockerapp.data.model.*
import com.example.dockerapp.data.repository.GrafanaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GrafanaViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: GrafanaRepository
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState
    
    private val _dashboards = MutableStateFlow<List<GrafanaDashboard>>(emptyList())
    val dashboards: StateFlow<List<GrafanaDashboard>> = _dashboards
    
    private val _dataSources = MutableStateFlow<List<GrafanaDataSource>>(emptyList())
    val dataSources: StateFlow<List<GrafanaDataSource>> = _dataSources
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    // Métriques en temps réel
    private val _cpuMetrics = MutableStateFlow<List<MetricPoint>>(emptyList())
    val cpuMetrics: StateFlow<List<MetricPoint>> = _cpuMetrics
    
    private val _memoryMetrics = MutableStateFlow<List<MetricPoint>>(emptyList())
    val memoryMetrics: StateFlow<List<MetricPoint>> = _memoryMetrics
    
    private val _networkMetrics = MutableStateFlow<List<MetricPoint>>(emptyList())
    val networkMetrics: StateFlow<List<MetricPoint>> = _networkMetrics
    
    private val _availableMetrics = MutableStateFlow<List<String>>(emptyList())
    val availableMetrics: StateFlow<List<String>> = _availableMetrics
    
    private val _grafanaUrl = MutableStateFlow("")
    val grafanaUrl: StateFlow<String> = _grafanaUrl
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = GrafanaRepository(database.grafanaCredentialsDao())
        checkSavedCredentials()
    }
    
    private fun checkSavedCredentials() {
        viewModelScope.launch {
            repository.getCredentials().collect { credentials ->
                if (credentials != null) {
                    GrafanaRetrofitClient.setCredentials(
                        credentials.username,
                        credentials.password,
                        credentials.serverUrl
                    )
                    
                    // Tester la connexion
                    val isConnected = repository.testConnection(
                        credentials.username,
                        credentials.password,
                        credentials.serverUrl
                    )
                    
                    if (isConnected) {
                        _isAuthenticated.value = true
                        _authState.value = AuthState.Authenticated(credentials)
                        
                        // Construire l'URL Grafana pour le navigateur externe
                        val grafanaUrl = repository.buildGrafanaUrl(credentials.serverUrl)
                        _grafanaUrl.value = grafanaUrl
                        
                        loadDashboardsAndData()
                    } else {
                        _authState.value = AuthState.NotAuthenticated
                        _errorMessage.value = "Impossible de se connecter à Grafana"
                    }
                } else {
                    _authState.value = AuthState.NotAuthenticated
                }
            }
        }
    }
    
    fun authenticate(username: String, password: String, serverUrl: String) {
        if (username.isBlank() || password.isBlank() || serverUrl.isBlank()) {
            _errorMessage.value = "Veuillez remplir tous les champs"
            return
        }
        
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                _errorMessage.value = null
                
                // Tester la connexion
                val isConnected = repository.testConnection(username, password, serverUrl)
                if (isConnected) {
                        // Sauvegarder les identifiants
                        repository.saveCredentials(username, password, serverUrl)
                        
                        // Construire l'URL Grafana pour le navigateur externe
                        val grafanaUrl = repository.buildGrafanaUrl(serverUrl)
                        _grafanaUrl.value = grafanaUrl
                        
                        _isAuthenticated.value = true
                        _authState.value = AuthState.Authenticated(
                            GrafanaCredentials(username = username, password = password, serverUrl = serverUrl)
                        )
                        
                        loadDashboardsAndData()
                } else {
                    _errorMessage.value = "Impossible de se connecter à Grafana. Vérifiez vos identifiants."
                    _authState.value = AuthState.NotAuthenticated
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors de l'authentification: ${e.message}"
                _authState.value = AuthState.NotAuthenticated
            }
        }
    }
    
    private fun loadDashboardsAndData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Charger les dashboards
                val dashboards = repository.getDashboards()
                _dashboards.value = dashboards
                
                // Charger les sources de données
                val dataSources = repository.getDataSources()
                _dataSources.value = dataSources
                
                // Charger les métriques par défaut si on a une source de données Prometheus
                val prometheusDataSource = dataSources.find { it.type == "prometheus" }
                if (prometheusDataSource != null) {
                    // Charger les métriques disponibles
                    val metrics = repository.getAvailableMetrics()
                    _availableMetrics.value = metrics
                    
                    loadDefaultMetrics(prometheusDataSource.uid)
                } else {
                    // Pas de source Prometheus, générer des données de démonstration
                    generateDemoData()
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors du chargement des données: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun loadDefaultMetrics(dataSourceUid: String) {
        try {
            // Essayer différentes requêtes courantes pour Docker/conteneurs
            val commonQueries = listOf(
                // CPU queries
                "rate(container_cpu_usage_seconds_total[5m]) * 100",
                "cpu_usage_percent",
                "rate(cpu_usage_seconds_total[5m]) * 100",
                "100 - (avg(irate(node_cpu_seconds_total{mode=\"idle\"}[5m])) * 100)",
                
                // Memory queries  
                "container_memory_usage_bytes / 1024 / 1024",
                "memory_usage_bytes / 1024 / 1024",
                "node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes",
                
                // Network queries
                "rate(container_network_receive_bytes_total[5m]) / 1024 / 1024",
                "rate(network_io_bytes_total[5m]) / 1024 / 1024",
                "rate(node_network_receive_bytes_total[5m]) / 1024 / 1024"
            )
            
            // Essayer chaque requête et garder celles qui fonctionnent
            for (query in commonQueries.take(3)) { // Limiter pour éviter trop de requêtes
                try {
                    val data = repository.queryMetrics(dataSourceUid, query)
                    if (data.isNotEmpty()) {
                        when {
                            query.contains("cpu") || query.contains("CPU") -> {
                                _cpuMetrics.value = convertToMetricPoints(data)
                            }
                            query.contains("memory") || query.contains("Memory") -> {
                                _memoryMetrics.value = convertToMetricPoints(data)
                            }
                            query.contains("network") || query.contains("Network") -> {
                                _networkMetrics.value = convertToMetricPoints(data)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Continuer avec la requête suivante
                }
            }
            
            // Si aucune métrique n'est trouvée, générer des données de démonstration
            if (_cpuMetrics.value.isEmpty() && _memoryMetrics.value.isEmpty() && _networkMetrics.value.isEmpty()) {
                generateDemoData()
            }
            
        } catch (e: Exception) {
            generateDemoData()
        }
    }
    
    private fun generateDemoData() {
        val now = System.currentTimeMillis()
        val demoPoints = (0..20).map { i ->
            MetricPoint(
                timestamp = now - (20 - i) * 60000, // Points toutes les minutes
                value = 20.0 + (Math.random() * 60.0) // Valeurs aléatoires
            )
        }
        
        _cpuMetrics.value = demoPoints.map { it.copy(value = it.value * 0.8) } // CPU 0-50%
        _memoryMetrics.value = demoPoints.map { it.copy(value = it.value * 10) } // Memory 200-800MB
        _networkMetrics.value = demoPoints.map { it.copy(value = it.value * 0.1) } // Network 2-8MB/s
    }
    
    private fun convertToMetricPoints(queryResults: List<QueryResult>): List<MetricPoint> {
        return queryResults.flatMap { result ->
            result.datapoints.mapNotNull { datapoint ->
                try {
                    val value = (datapoint[0] as? Number)?.toDouble()
                    val timestamp = (datapoint[1] as? Number)?.toLong()
                    
                    if (value != null && timestamp != null) {
                        MetricPoint(timestamp, value)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
        }.sortedBy { it.timestamp }
    }
    
    fun refreshData() {
        if (_isAuthenticated.value) {
            loadDashboardsAndData()
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            repository.clearCredentials()
            _isAuthenticated.value = false
            _authState.value = AuthState.NotAuthenticated
            _dashboards.value = emptyList()
            _dataSources.value = emptyList()
            _cpuMetrics.value = emptyList()
            _memoryMetrics.value = emptyList()
            _networkMetrics.value = emptyList()
            _grafanaUrl.value = ""
            _errorMessage.value = null
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    sealed class AuthState {
        object Loading : AuthState()
        object NotAuthenticated : AuthState()
        data class Authenticated(val credentials: GrafanaCredentials) : AuthState()
    }
}

data class MetricPoint(
    val timestamp: Long,
    val value: Double
)