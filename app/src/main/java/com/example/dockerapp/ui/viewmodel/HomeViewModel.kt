package com.example.dockerapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dockerapp.data.api.RetrofitClient
import com.example.dockerapp.data.model.Container
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _containers = MutableStateFlow<List<Container>>(emptyList())
    val containers: StateFlow<List<Container>> = _containers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadContainers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val response = RetrofitClient.apiService.getContainers()
                if (response.isSuccessful) {
                    _containers.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Erreur lors du chargement des conteneurs"
                }
            } catch (e: Exception) {
                _error.value = "Erreur réseau: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
