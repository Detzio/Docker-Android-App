package com.example.dockerapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dockerapp.data.db.AppDatabase
import com.example.dockerapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AuthRepository
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = AuthRepository(database.userCredentialsDao())
        checkSavedCredentials()
    }
    
    private fun checkSavedCredentials() {
        viewModelScope.launch {
            repository.getActiveCredentials().collect { credentials ->
                if (credentials != null) {
                    // Tentative de connexion automatique
                    val success = repository.login(credentials.username, credentials.password)
                    _isAuthenticated.value = success
                }
            }
        }
    }
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            try {
                val success = repository.login(username, password)
                if (success) {
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
        viewModelScope.launch {
            repository.logout()
            _isAuthenticated.value = false
            _loginState.value = LoginState.Idle
        }
    }
    
    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }
    
    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
