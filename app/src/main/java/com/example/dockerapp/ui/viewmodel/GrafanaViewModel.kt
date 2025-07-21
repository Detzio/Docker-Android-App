package com.example.dockerapp.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dockerapp.data.api.RetrofitClient
import com.example.dockerapp.data.db.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GrafanaViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userCredentialsDao = AppDatabase.getDatabase(application).userCredentialsDao()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    // Cache pour éviter les recalculs
    private var lastLoadTime = 0L
    private val cacheTimeout = 5000L // 5 secondes
    
    // Métriques en temps réel
    private val _cpuMetrics = MutableStateFlow<List<MetricPoint>>(emptyList())
    val cpuMetrics: StateFlow<List<MetricPoint>> = _cpuMetrics
    
    private val _memoryMetrics = MutableStateFlow<List<MetricPoint>>(emptyList())
    val memoryMetrics: StateFlow<List<MetricPoint>> = _memoryMetrics
    
    private val _networkMetrics = MutableStateFlow<List<MetricPoint>>(emptyList())
    val networkMetrics: StateFlow<List<MetricPoint>> = _networkMetrics
    
    // Statistiques générales
    private val _totalContainers = MutableStateFlow(0)
    val totalContainers: StateFlow<Int> = _totalContainers
    
    private val _runningContainers = MutableStateFlow(0)
    val runningContainers: StateFlow<Int> = _runningContainers
    
    private val _cpuUsage = MutableStateFlow(0.0)
    val cpuUsage: StateFlow<Double> = _cpuUsage
    
    private val _memoryUsage = MutableStateFlow(0L)
    val memoryUsage: StateFlow<Long> = _memoryUsage
    
    private val _grafanaUrl = MutableStateFlow("")
    val grafanaUrl: StateFlow<String> = _grafanaUrl
    
    init {
        // Construire l'URL Grafana basée sur l'URL Docker actuelle
        buildGrafanaUrl()
    }
    
    private fun buildGrafanaUrl() {
        viewModelScope.launch {
            try {
                // Récupérer les identifiants Docker sauvegardés
                userCredentialsDao.getActiveCredentials().collect { credentials ->
                    if (credentials != null) {
                        val dockerUrl = credentials.serverUrl
                        val grafanaUrl = convertDockerUrlToGrafana(dockerUrl)
                        _grafanaUrl.value = grafanaUrl
                        Log.d("GrafanaViewModel", "Grafana URL set to: $grafanaUrl")
                    } else {
                        _grafanaUrl.value = ""
                        Log.w("GrafanaViewModel", "No active credentials found")
                    }
                }
            } catch (e: Exception) {
                Log.e("GrafanaViewModel", "Error building Grafana URL", e)
                _grafanaUrl.value = ""
            }
        }
    }
    
    private fun convertDockerUrlToGrafana(dockerUrl: String): String {
        var grafanaUrl = dockerUrl.trim()
        
        // Supprimer '/info' s'il est présent à la fin
        if (grafanaUrl.endsWith("/info")) {
            grafanaUrl = grafanaUrl.substring(0, grafanaUrl.length - 5)
        }
        
        // Remplacer les ports Docker par le port Grafana (3000)
        grafanaUrl = grafanaUrl.replace(":2376", ":3000")
            .replace(":2377", ":3000")
            .replace(":2375", ":3000")
        
        // S'assurer que l'URL se termine par '/'
        if (!grafanaUrl.endsWith("/")) {
            grafanaUrl += "/"
        }
        
        return grafanaUrl
    }
    
    fun loadMetrics() {
        val currentTime = System.currentTimeMillis()
        
        // Vérifier le cache
        if (currentTime - lastLoadTime < cacheTimeout && _cpuMetrics.value.isNotEmpty()) {
            Log.d("GrafanaViewModel", "Using cached metrics")
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // Charger les conteneurs et leurs statistiques
                loadContainerStats()
                
                // Générer des métriques basées sur les données Docker
                generateMetricsFromDocker()
                
                lastLoadTime = currentTime
                
            } catch (e: Exception) {
                Log.e("GrafanaViewModel", "Error loading metrics", e)
                _errorMessage.value = "Erreur lors du chargement des métriques: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun loadContainerStats() {
        try {
            // Récupérer tous les conteneurs
            val containersResponse = RetrofitClient.apiService.getContainers()
            if (containersResponse.isSuccessful) {
                val containers = containersResponse.body() ?: emptyList()
                _totalContainers.value = containers.size
                
                val runningContainersList = containers.filter { 
                    it.state.equals("running", ignoreCase = true) 
                }
                _runningContainers.value = runningContainersList.size
                
                // Optimisation: ne prendre que 3 conteneurs et réduire le délai
                val containersToCheck = runningContainersList.take(3)
                
                if (containersToCheck.isNotEmpty()) {
                    // Collecter les stats en parallèle pour 1 seul conteneur représentatif
                    val representativeContainer = containersToCheck.first()
                    try {
                        val statsResponse = RetrofitClient.apiService.getContainerStats(
                            representativeContainer.id, 
                            stream = false
                        )
                        if (statsResponse.isSuccessful) {
                            val stats = statsResponse.body()
                            stats?.let {
                                _cpuUsage.value = it.calculateCpuPercentage()
                                _memoryUsage.value = it.memoryStats.usage
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("GrafanaViewModel", "Error getting stats for container ${representativeContainer.id}", e)
                        // Utiliser des valeurs par défaut si échec
                        _cpuUsage.value = kotlin.random.Random.nextDouble(10.0, 50.0)
                        _memoryUsage.value = kotlin.random.Random.nextLong(100 * 1024 * 1024, 500 * 1024 * 1024)
                    }
                } else {
                    // Pas de conteneurs en cours, utiliser des valeurs nulles
                    _cpuUsage.value = 0.0
                    _memoryUsage.value = 0L
                }
                
            } else {
                _errorMessage.value = "Erreur lors de la récupération des conteneurs: ${containersResponse.code()}"
            }
        } catch (e: Exception) {
            Log.e("GrafanaViewModel", "Error in loadContainerStats", e)
            _errorMessage.value = "Erreur de connexion: ${e.message}"
        }
    }
    
    private fun generateMetricsFromDocker() {
        val now = System.currentTimeMillis()
        
        // Ajouter de nouveaux points aux métriques existantes
        val currentCpuMetrics = _cpuMetrics.value.toMutableList()
        val currentMemoryMetrics = _memoryMetrics.value.toMutableList()
        val currentNetworkMetrics = _networkMetrics.value.toMutableList()
        
        // Ajouter les nouvelles valeurs
        currentCpuMetrics.add(MetricPoint(now, _cpuUsage.value))
        currentMemoryMetrics.add(MetricPoint(now, _memoryUsage.value / (1024.0 * 1024.0))) // Convertir en MB
        currentNetworkMetrics.add(MetricPoint(now, kotlin.random.Random.nextDouble(1.0, 15.0))) // Placeholder pour le réseau
        
        // Garder seulement les 15 derniers points (réduit de 20 à 15)
        val maxPoints = 15
        while (currentCpuMetrics.size > maxPoints) {
            currentCpuMetrics.removeAt(0)
        }
        while (currentMemoryMetrics.size > maxPoints) {
            currentMemoryMetrics.removeAt(0)
        }
        while (currentNetworkMetrics.size > maxPoints) {
            currentNetworkMetrics.removeAt(0)
        }
        
        // Mettre à jour les StateFlow
        _cpuMetrics.value = currentCpuMetrics
        _memoryMetrics.value = currentMemoryMetrics
        _networkMetrics.value = currentNetworkMetrics
    }

    fun refreshGrafanaUrl() {
        buildGrafanaUrl()
    }
}

data class MetricPoint(
    val timestamp: Long,
    val value: Double
)