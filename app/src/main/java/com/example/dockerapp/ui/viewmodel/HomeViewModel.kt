package com.example.dockerapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dockerapp.data.api.RetrofitClient
import com.example.dockerapp.data.model.Container
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val TAG = "HomeViewModel"

    private val _containers = MutableStateFlow<List<Container>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _selectedStateFilter = MutableStateFlow<String?>(null)
    val selectedStateFilter: StateFlow<String?> = _selectedStateFilter
    
    private val _filteredContainers = MutableStateFlow<List<Container>>(emptyList())
    val filteredContainers: StateFlow<List<Container>> = _filteredContainers
    
    private var loadContainersJob: Job? = null
    private var statsLoopJob: Job? = null
    
    init {
        viewModelScope.launch {
            // Combine les flux pour filtrer les conteneurs
            combine(
                _containers,
                _searchQuery,
                _selectedStateFilter
            ) { containers, query, stateFilter ->
                containers.filter { container ->
                    val matchesQuery = container.names?.any { 
                        it.contains(query, ignoreCase = true) 
                    } ?: container.id.contains(query, ignoreCase = true)
                    
                    val matchesState = stateFilter == null || container.state == stateFilter
                    
                    matchesQuery && matchesState
                }
            }.collect {
                _filteredContainers.value = it
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateStateFilter(state: String?) {
        _selectedStateFilter.value = state
    }

    fun loadContainers() {
        if (loadContainersJob?.isActive == true) return

        Log.d(TAG, "loading containers...")

        loadContainersJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = RetrofitClient.apiService.getContainers()
                if (response.isSuccessful) {
                    val containers = response.body()
                    Log.d("HomeViewModel", "Containers received: ${containers?.size}")
                    Log.d("HomeViewModel", "Raw response: $containers")
                    _containers.value = containers ?: emptyList()
                } else {
                    Log.e("HomeViewModel", "Error response: ${response.errorBody()?.string()}")
                    _error.value = "Erreur lors du chargement des conteneurs: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Network error", e)
                _error.value = "Erreur réseau: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startContainer(containerId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.startContainer(containerId)
                if (response.isSuccessful) {
                     loadContainers() // Recharger la liste après l'action
                } else {
                    _error.value = "Impossible de démarrer le conteneur"
                }
            } catch (e: Exception) {
                _error.value = "Erreur: ${e.message}"
            }
        }
    }

    fun stopContainer(containerId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.stopContainer(containerId)
                if (response.isSuccessful) {
                     loadContainers()
                } else {
                    _error.value = "Impossible d'arrêter le conteneur"
                }
            } catch (e: Exception) {
                _error.value = "Erreur: ${e.message}"
            }
        }
    }

    fun restartContainer(containerId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.restartContainer(containerId)
                if (response.isSuccessful) {
                     loadContainers()
                } else {
                    _error.value = "Impossible de redémarrer le conteneur"
                }
            } catch (e: Exception) {
                _error.value = "Erreur: ${e.message}"
            }
        }
    }   

    private suspend fun loadContainerStats(containerId: String) {
        try {
            val response = RetrofitClient.apiService.getContainerStats(containerId, stream = false)
            if (response.isSuccessful) {
                val stats = response.body()
                stats?.let { containerStats ->
                    val cpuPercentage = containerStats.calculateCpuPercentage()
                    val memoryUsage = containerStats.memoryStats.usage

                    Log.d("HomeViewModel", "Container ${containerStats.name} stats: CPU=$cpuPercentage%, Memory=${memoryUsage}B")

                    val updatedContainers = _containers.value.map { container ->
                        if (container.id == containerId) {
                            container.copy(
                                cpuUsage = cpuPercentage,
                                memoryUsage = memoryUsage
                            )
                        } else {
                            container
                        }
                    }
                    _containers.value = updatedContainers
                }
            } else {
                Log.e("HomeViewModel", "Erreur stats conteneur $containerId: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Erreur stats conteneur $containerId", e)
        }
    }

    fun startStatsLoop(intervalPerRequestMs: Long = 200) {

        if (statsLoopJob?.isActive == true) return

        statsLoopJob = viewModelScope.launch {
            Log.d(TAG, "startStatsLoop: started")
            while (true) {
                val runningContainers = _containers.value.filter { it.state.equals("running", ignoreCase = true) }

                if (runningContainers.isEmpty()) {
                    delay(intervalPerRequestMs)
                }

                for ((index, container) in runningContainers.withIndex()) {
                    try {
                        loadContainerStats(container.id)
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Erreur refresh stats ${container.id}", e)
                    }

                    delay(intervalPerRequestMs)
                }

            }
        }
    }

    private val _navigationEvent = MutableStateFlow<Pair<String, String>?>(null)
    val navigationEvent: StateFlow<Pair<String, String>?> = _navigationEvent
    
    private val _detailsNavigationEvent = MutableStateFlow<Pair<String, String>?>(null)
    val detailsNavigationEvent: StateFlow<Pair<String, String>?> = _detailsNavigationEvent
    
    fun navigateToLogs(containerId: String, containerName: String?) {
        val displayName = containerName ?: containerId.take(12)
        _navigationEvent.value = Pair(containerId, displayName)
    }
    
    fun navigateToDetails(containerId: String, containerName: String?) {
        val displayName = containerName ?: containerId.take(12)
        _detailsNavigationEvent.value = Pair(containerId, displayName)
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
    
    fun onDetailsNavigationHandled() {
        _detailsNavigationEvent.value = null
    }

    fun refreshContainers() {
        loadContainers()
    }

    fun deleteContainer(containerId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteContainer(containerId, force = false)
                if (response.isSuccessful) {
                    loadContainers() // Recharger la liste après suppression
                } else {
                    _error.value = when (response.code()) {
                        409 -> "Impossible de supprimer un conteneur en cours d'exécution. Arrêtez-le d'abord."
                        404 -> "Conteneur introuvable."
                        else -> "Impossible de supprimer le conteneur (code: ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la suppression: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        loadContainersJob?.cancel()
        statsLoopJob?.cancel()
    }
}
