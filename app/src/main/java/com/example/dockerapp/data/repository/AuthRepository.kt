package com.example.dockerapp.data.repository

import com.example.dockerapp.data.api.RetrofitClient
import com.example.dockerapp.data.db.UserCredentialsDao
import com.example.dockerapp.data.model.UserCredentials
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val userCredentialsDao: UserCredentialsDao) {
    
    suspend fun login(username: String, password: String, serverUrl: String): Boolean {
        RetrofitClient.setCredentials(username, password, serverUrl)
        
        return try {
            val response = RetrofitClient.apiService.getInfo()
            if (response.isSuccessful) {
                // Sauvegarde des identifiants en local si la connexion est réussie
                saveCredentials(username, password, serverUrl)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun saveCredentials(username: String, password: String, serverUrl: String) {
        // Désactive tous les identifiants existants avant d'en ajouter un nouveau
        userCredentialsDao.deactivateAllCredentials()
        // Sauvegarde les nouveaux identifiants comme actifs
        userCredentialsDao.saveCredentials(UserCredentials(username, password, serverUrl, isActive = true))
    }
    
    fun getActiveCredentials(): Flow<UserCredentials?> {
        return userCredentialsDao.getActiveCredentials()
    }

    suspend fun logout() {
        // Réinitialise les identifiants dans RetrofitClient
        RetrofitClient.clearCredentials()
        // Supprime tous les identifiants de la base de données
        userCredentialsDao.deleteAllCredentials()
    }
}
