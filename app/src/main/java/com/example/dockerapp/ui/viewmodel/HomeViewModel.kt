package com.example.dockerapp.ui.viewmodel

import android.util.Log
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
                    val containers = response.body()
                    Log.d("HomeViewModel", "Containers received: ${containers?.size}")
                    Log.d("HomeViewModel", "Raw response: ${containers}")
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
}
