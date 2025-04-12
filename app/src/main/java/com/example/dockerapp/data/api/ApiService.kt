package com.example.dockerapp.data.api

import com.example.dockerapp.data.model.Container
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("info")
    suspend fun getInfo(): Response<Any>
    
    @GET("containers/json?all=true")
    suspend fun getContainers(): Response<List<Container>>
    
    @POST("containers/{id}/start")
    suspend fun startContainer(@Path("id") containerId: String): Response<Unit>
    
    @POST("containers/{id}/stop")
    suspend fun stopContainer(@Path("id") containerId: String): Response<Unit>
    
    @POST("containers/{id}/restart")
    suspend fun restartContainer(@Path("id") containerId: String): Response<Unit>
}
