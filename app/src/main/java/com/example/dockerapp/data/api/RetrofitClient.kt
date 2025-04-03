package com.example.dockerapp.data.api

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var baseUrl: String = ""
    
    private var authUsername: String? = null
    private var authPassword: String? = null
    
    fun setCredentials(username: String, password: String, serverUrl: String) {
        authUsername = username
        authPassword = password
        
        // Nettoyer et formater l'URL
        var cleanUrl = serverUrl.trim()
        
        // Supprimer '/info' s'il est présent à la fin
        if (cleanUrl.endsWith("/info")) {
            cleanUrl = cleanUrl.substring(0, cleanUrl.length - 5)
        }
        
        // S'assurer que l'URL se termine par '/'
        if (!cleanUrl.endsWith("/")) {
            cleanUrl += "/"
        }
        
        baseUrl = cleanUrl
    }
    
    fun clearCredentials() {
        authUsername = null
        authPassword = null
        baseUrl = "" // Réinitialiser à la valeur par défaut
    }
    
    private val authInterceptor = Interceptor { chain ->
        val request = if (authUsername != null && authPassword != null) {
            val credentials = Credentials.basic(authUsername!!, authPassword!!)
            chain.request().newBuilder()
                .header("Authorization", credentials)
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    val apiService: ApiService
        get() {
            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()
                
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
}
