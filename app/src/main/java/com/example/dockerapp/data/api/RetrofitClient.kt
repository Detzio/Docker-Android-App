package com.example.dockerapp.data.api

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var baseUrl: String = ""
    
    private var authUsername: String? = null
    private var authPassword: String? = null
    
    // Instances uniques réutilisables
    private var _apiService: ApiService? = null
    private var _okHttpClient: OkHttpClient? = null
    private var _retrofit: Retrofit? = null
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE // Désactiver complètement le logging
    }
    
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        
        if (authUsername != null && authPassword != null) {
            val credentials = Credentials.basic(authUsername!!, authPassword!!)
            requestBuilder.header("Authorization", credentials)
        }
        val url = originalRequest.url.toString()
        if (url.contains("/containers/create") || url.contains("/images/create")) {
            requestBuilder.header("Connection", "close")
        }
        chain.proceed(requestBuilder.build())
    }
    
    // Ajout d'un intercepteur pour gérer les erreurs et éviter les accumulations
    private val errorInterceptor = Interceptor { chain ->
        try {
            val response = chain.proceed(chain.request())
            response
        } catch (e: Exception) {
            throw e
        }
    }
    
    fun setCredentials(username: String, password: String, serverUrl: String) {
        // Si les credentials changent, on doit recréer les instances
        if (authUsername != username || authPassword != password || baseUrl != serverUrl) {
            cleanup()
            
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
    }
    
    fun clearCredentials() {
        cleanup()
        authUsername = null
        authPassword = null
        baseUrl = ""
    }
    
    private fun cleanup() {
        _apiService = null
        _retrofit = null
        _okHttpClient?.let { client ->
            try {
                client.dispatcher.executorService.shutdown()
                client.connectionPool.evictAll()
            } catch (e: Exception) {
                // Ignorer les erreurs de nettoyage
            }
        }
        _okHttpClient = null
    }
    
    val apiService: ApiService
        get() {
            return _apiService ?: createApiService().also { _apiService = it }
        }
    
    private fun createApiService(): ApiService {
        val retrofit = _retrofit ?: createRetrofit().also { _retrofit = it }
        return retrofit.create(ApiService::class.java)
    }
    
    private fun createRetrofit(): Retrofit {
        val client = _okHttpClient ?: createOkHttpClient().also { _okHttpClient = it }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(errorInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .callTimeout(180, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(okhttp3.ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .build()
    }
}
