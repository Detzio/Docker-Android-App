package com.example.dockerapp.data.api

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://dorian.sh:2377/"
    
    private var authUsername: String? = null
    private var authPassword: String? = null
    
    fun setCredentials(username: String, password: String) {
        authUsername = username
        authPassword = password
    }
    
    fun clearCredentials() {
        authUsername = null
        authPassword = null
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
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()
    
    val apiService: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}
