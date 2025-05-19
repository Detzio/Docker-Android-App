package com.example.dockerapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dockerapp.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.coroutines.coroutineContext

class LogsViewModel : ViewModel() {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var logsJob: Job? = null

    // Démarrer le streaming des logs
    fun startLogsStreaming(containerId: String) {
        // Annuler le job existant s'il y en a un
        stopLogsStreaming()
        
        _isLoading.value = true
        _error.value = null
        
        Log.d("LogsViewModel", "Démarrage du streaming des logs pour le conteneur: $containerId")
        
        logsJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                // D'abord, récupérer les derniers logs sans streaming
                val initialResponse = RetrofitClient.apiService.getContainerLogs(
                    containerId = containerId,
                    stdout = true,
                    stderr = true,
                    follow = false,
                    tail = "50"  // Récupérer les 50 dernières lignes
                )
                
                if (initialResponse.isSuccessful) {
                    Log.d("LogsViewModel", "Logs initiaux récupérés avec succès")
                    handleLogsResponse(initialResponse)
                    
                    // Puis démarrer le streaming pour les nouveaux logs
                    delay(500) // Petite pause avant de démarrer le streaming
                    
                    val streamResponse = RetrofitClient.apiService.getContainerLogs(
                        containerId = containerId,
                        stdout = true,
                        stderr = true,
                        follow = true,
                        tail = "0"  // Ne pas répéter les logs déjà récupérés
                    )
                    
                    handleLogsResponse(streamResponse)
                } else {
                    Log.e("LogsViewModel", "Erreur lors de la récupération des logs initiaux: ${initialResponse.code()}")
                    withContext(Dispatchers.Main) {
                        _error.value = "Erreur serveur: ${initialResponse.code()}"
                        _isLoading.value = false
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("LogsViewModel", "Erreur lors du streaming des logs", e)
                    _error.value = "Erreur de connexion: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }
      private suspend fun handleLogsResponse(response: Response<ResponseBody>) {
        Log.d("LogsViewModel", "Réponse des logs reçue, statut: ${response.code()}")
        
        if (!response.isSuccessful) {
            withContext(Dispatchers.Main) {
                _error.value = "Erreur serveur: ${response.code()}"
                _isLoading.value = false
            }
            return
        }
        
        val responseBody = response.body()
        if (responseBody == null) {
            withContext(Dispatchers.Main) {
                _error.value = "Aucune donnée reçue"
                _isLoading.value = false
            }
            return
        }
        
        // Nettoyer les logs précédents
        withContext(Dispatchers.Main) {
            _logs.value = emptyList()
        }
        
        // Lire le flux de logs
        try {
            val reader = BufferedReader(InputStreamReader(responseBody.byteStream()))
            val logLines = mutableListOf<String>()
            
            // Première indication que le chargement a commencé
            withContext(Dispatchers.Main) {
                _isLoading.value = true
                logLines.add("Chargement des logs...")
                _logs.value = logLines.toList()
            }
            
            var line: String?
            
            while (coroutineContext.isActive) {
                try {
                    line = withContext(Dispatchers.IO) {
                        reader.readLine()
                    }
                    
                    if (line == null) {
                        delay(500) // Attendre un peu avant de vérifier à nouveau
                        continue
                    }
                    
                    // Traitement du format spécial des logs Docker
                    val cleanLine = cleanDockerLogLine(line)
                    
                    withContext(Dispatchers.Main) {
                        if (logLines.size == 1 && logLines[0] == "Chargement des logs...") {
                            // Remplacer le message de chargement par le premier log
                            logLines.clear()
                        }
                        
                        logLines.add(cleanLine)
                        _logs.value = logLines.toList()
                    }
                } catch (e: IOException) {
                    // En cas d'erreur de lecture, vérifier si la coroutine est toujours active
                    if (coroutineContext.isActive) {
                        Log.e("LogsViewModel", "Erreur de lecture ligne", e)
                        delay(1000) // Attendre avant de réessayer
                    } else {
                        break
                    }
                }
            }
        } catch (e: IOException) {
            if (coroutineContext.isActive) {  // Seulement signaler l'erreur si ce n'est pas une annulation
                withContext(Dispatchers.Main) {
                    Log.e("LogsViewModel", "Erreur de lecture des logs", e)
                    _error.value = "Erreur de lecture: ${e.message}"
                }
            }
        } finally {
            withContext(Dispatchers.Main) {
                _isLoading.value = false
            }
        }
    }

    // Nettoie le format spécial des logs Docker
    private fun cleanDockerLogLine(line: String): String {
        // Les logs Docker peuvent contenir des caractères spéciaux au début
        // Format typique: 8 premiers octets = en-tête, puis le texte du log
        // Voir: https://docs.docker.com/engine/api/v1.41/#operation/ContainerLogs
        return if (line.length > 8) {
            try {
                line.substring(8)
            } catch (e: Exception) {
                line
            }
        } else {
            line
        }
    }
    
    // Arrêter le streaming des logs
    fun stopLogsStreaming() {
        logsJob?.cancel()
        logsJob = null
    }
    
    override fun onCleared() {
        super.onCleared()
        stopLogsStreaming()
    }
}