package com.example.dockerapp.data.api

import com.example.dockerapp.data.model.Container
import com.example.dockerapp.data.model.ContainerStats
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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
    
    @GET("containers/{id}/stats")
    suspend fun getContainerStats(
        @Path("id") containerId: String,
        @Query("stream") stream: Boolean
    ): Response<ContainerStats>
    
    @GET("containers/{id}/logs")
    suspend fun getContainerLogs(
        @Path("id") containerId: String,
        @Query("stdout") stdout: Boolean = true,
        @Query("stderr") stderr: Boolean = true,
        @Query("follow") follow: Boolean = false,
        @Query("tail") tail: String = "100"
    ): Response<ResponseBody>
}
