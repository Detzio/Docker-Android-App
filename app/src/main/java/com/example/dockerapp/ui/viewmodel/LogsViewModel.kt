package com.example.dockerapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dockerapp.data.api.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class LogsViewModel : ViewModel() {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var logsJob: Job? = null

    // Charger les logs du conteneur
    fun loadLogs(containerId: String) {
        // Annuler le job existant
        logsJob?.cancel()
        
        _isLoading.value = true
        _error.value = null
        
        Log.d("LogsViewModel", "Chargement des logs pour le conteneur: $containerId")
        
        logsJob = viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getContainerLogs(
                    containerId = containerId,
                    stdout = true,
                    stderr = true,
                    follow = false,
                    tail = "100"
                )
                
                if (response.isSuccessful) {
                    Log.d("LogsViewModel", "Logs récupérés avec succès")
                    handleLogsResponse(response)
                } else {
                    Log.e("LogsViewModel", "Erreur lors de la récupération des logs: ${response.code()}")
                    _error.value = "Erreur serveur: ${response.code()}"
                    _isLoading.value = false
                }
                
            } catch (e: Exception) {
                Log.e("LogsViewModel", "Erreur lors de la récupération des logs", e)
                _error.value = "Erreur de connexion: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun handleLogsResponse(response: Response<ResponseBody>) {
        Log.d("LogsViewModel", "Traitement des logs")
        
        val responseBody = response.body()
        if (responseBody == null) {
            _error.value = "Aucune donnée reçue"
            _isLoading.value = false
            return
        }
        
        try {
            val reader = BufferedReader(InputStreamReader(responseBody.byteStream()))
            val logLines = mutableListOf<String>()
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val cleanLine = cleanDockerLogLine(line!!)
                if (cleanLine.isNotBlank()) {
                    logLines.add(cleanLine)
                }
            }
            
            _logs.value = logLines
            _isLoading.value = false
            
            Log.d("LogsViewModel", "Logs chargés: ${logLines.size} lignes")
            
        } catch (e: IOException) {
            Log.e("LogsViewModel", "Erreur de lecture des logs", e)
            _error.value = "Erreur de lecture: ${e.message}"
            _isLoading.value = false
        } finally {
            responseBody.close()
        }
    }

    // Nettoie le format spécial des logs Docker
    private fun cleanDockerLogLine(line: String): String {
        // Les logs Docker peuvent contenir des caractères spéciaux au début
        // Format typique: 8 premiers octets = en-tête, puis le texte du log
        return try {
            if (line.length > 8) {
                line.substring(8).trim()
            } else {
                line.trim()
            }
        } catch (e: Exception) {
            line.trim()
        }
    }
    
    // Rafraîchir manuellement les logs
    fun refreshLogs(containerId: String) {
        loadLogs(containerId)
    }
    
    fun clearError() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        logsJob?.cancel()
    }
}