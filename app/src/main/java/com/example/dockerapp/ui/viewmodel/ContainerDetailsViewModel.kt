package com.example.dockerapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dockerapp.data.api.RetrofitClient
import com.example.dockerapp.data.model.ContainerDetails
import com.example.dockerapp.data.model.ContainerStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContainerDetailsViewModel : ViewModel() {
    private val _containerDetails = MutableStateFlow<ContainerDetails?>(null)
    val containerDetails: StateFlow<ContainerDetails?> = _containerDetails

    private val _containerStats = MutableStateFlow<ContainerStats?>(null)
    val containerStats: StateFlow<ContainerStats?> = _containerStats

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _actionInProgress = MutableStateFlow(false)
    val actionInProgress: StateFlow<Boolean> = _actionInProgress

    fun loadContainerDetails(containerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val response = RetrofitClient.apiService.getContainerDetails(containerId)
                if (response.isSuccessful) {
                    _containerDetails.value = response.body()
                    // Charger aussi les stats si le conteneur est en cours d'exécution
                    response.body()?.state?.let { state ->
                        if (state.running) {
                            loadContainerStats(containerId)
                        }
                    }
                } else {
                    _error.value = "Erreur lors du chargement des détails: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("ContainerDetailsVM", "Error loading details", e)
                _error.value = "Erreur réseau: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadContainerStats(containerId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getContainerStats(containerId, stream = false)
                if (response.isSuccessful) {
                    _containerStats.value = response.body()
                }
            } catch (e: Exception) {
                Log.e("ContainerDetailsVM", "Error loading stats", e)
            }
        }
    }

    fun refreshStats(containerId: String) {
        _containerDetails.value?.state?.let { state ->
            if (state.running) {
                loadContainerStats(containerId)
            }
        }
    }

    fun startContainer(containerId: String) {
        performContainerAction(containerId) {
            RetrofitClient.apiService.startContainer(containerId)
        }
    }

    fun stopContainer(containerId: String) {
        performContainerAction(containerId) {
            RetrofitClient.apiService.stopContainer(containerId)
        }
    }

    fun restartContainer(containerId: String) {
        performContainerAction(containerId) {
            RetrofitClient.apiService.restartContainer(containerId)
        }
    }

    private fun performContainerAction(containerId: String, action: suspend () -> retrofit2.Response<Unit>) {
        viewModelScope.launch {
            _actionInProgress.value = true
            _error.value = null
            
            try {
                val response = action()
                if (response.isSuccessful) {
                    // Recharger les détails après l'action
                    loadContainerDetails(containerId)
                } else {
                    _error.value = "Erreur lors de l'action: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("ContainerDetailsVM", "Error performing action", e)
                _error.value = "Erreur: ${e.message}"
            } finally {
                _actionInProgress.value = false
            }
        }
    }
}