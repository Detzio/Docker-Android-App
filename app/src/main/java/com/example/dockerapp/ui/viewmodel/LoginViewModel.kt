package com.example.dockerapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dockerapp.data.db.AppDatabase
import com.example.dockerapp.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AuthRepository
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated
    
    // Flag pour éviter de vérifier les identifiants après une déconnexion
    private var checkingEnabled = true
    private var credentialCheckJob: Job? = null
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = AuthRepository(database.userCredentialsDao())
        checkSavedCredentials()
    }
    
    private fun checkSavedCredentials() {
        // Annuler tout job existant avant d'en créer un nouveau
        credentialCheckJob?.cancel()
        
        if (!checkingEnabled) return
        
        credentialCheckJob = viewModelScope.launch {
            repository.getActiveCredentials().collect { credentials ->
                // Ne procéder que si le checking est toujours activé
                if (credentials != null && checkingEnabled) {
                    try {
                        val success = repository.login(
                            credentials.username, 
                            credentials.password,
                            credentials.serverUrl
                        )
                        _isAuthenticated.value = success
                    } catch (e: Exception) {
                        // Gérer les erreurs silencieusement pour la connexion automatique
                        _isAuthenticated.value = false
                    }
                }
            }
        }
    }
    
    fun login(username: String, password: String, serverUrl: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            try {
                val success = repository.login(username, password, serverUrl)
                if (success) {
                    checkingEnabled = true
                    _loginState.value = LoginState.Success
                    _isAuthenticated.value = true
                } else {
                    _loginState.value = LoginState.Error("Échec de l'authentification")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Une erreur est survenue")
            }
        }
    }
    
    fun logout() {
        // Annuler d'abord le job de vérification des identifiants
        credentialCheckJob?.cancel()
        
        // Désactiver le checking et effectuer la déconnexion
        checkingEnabled = false
        
        viewModelScope.launch {
            try {
                repository.logout()
            } finally {
                // Mettre à jour l'état même en cas d'erreur
                _isAuthenticated.value = false
                _loginState.value = LoginState.Idle
            }
        }
    }
    
    // Réinitialiser l'état de connexion
    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }
    
    // Assurer le nettoyage lors de la destruction du ViewModel
    override fun onCleared() {
        super.onCleared()
        credentialCheckJob?.cancel()
    }
    
    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
