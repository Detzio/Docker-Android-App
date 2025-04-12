package com.example.dockerapp.data.api

import com.example.dockerapp.data.model.Container
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("info")
    suspend fun getInfo(): Response<Any>
    
    @GET("containers/json?all=true")
    suspend fun getContainers(): Response<List<Container>>
}

