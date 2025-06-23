package com.example.dockerapp.data.api

import com.example.dockerapp.data.model.Container
import com.example.dockerapp.data.model.ContainerStats
import com.example.dockerapp.data.model.ContainerDetails
import com.example.dockerapp.data.model.ExecCreateRequest
import com.example.dockerapp.data.model.ExecCreateResponse
import com.example.dockerapp.data.model.ExecStartRequest
import com.example.dockerapp.data.model.DockerImage
import com.example.dockerapp.data.model.VolumeListResponse
import com.example.dockerapp.data.model.ContainerCreateRequest
import com.example.dockerapp.data.model.ContainerCreateResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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
    
    @GET("containers/{id}/json")
    suspend fun getContainerDetails(
        @Path("id") containerId: String
    ): Response<ContainerDetails>

    @POST("containers/{id}/exec")
    suspend fun createExecInstance(
        @Path("id") containerId: String,
        @Body body: ExecCreateRequest
    ): Response<ExecCreateResponse>

    @POST("exec/{id}/start")
    suspend fun startExec(
        @Path("id") execId: String,
        @Body body: ExecStartRequest
    ): Response<ResponseBody>

    @GET("images/json")
    suspend fun getImages(): Response<List<DockerImage>>
    
    @GET("volumes")
    suspend fun getVolumes(): Response<VolumeListResponse>
    
    @POST("containers/create")
    suspend fun createContainer(
        @Body body: ContainerCreateRequest,
        @Query("name") name: String? = null
    ): Response<ContainerCreateResponse>

    @POST("images/create")
    suspend fun pullImage(
        @Query("fromImage") fromImage: String,
        @Query("tag") tag: String? = null
    ): Response<ResponseBody>
    
    @DELETE("containers/{id}")
    suspend fun deleteContainer(
        @Path("id") containerId: String,
        @Query("force") force: Boolean = false
    ): Response<Unit>
    
    @DELETE("images/{name}")
    suspend fun deleteImage(
        @Path("name") imageName: String,
        @Query("force") force: Boolean = false
    ): Response<Unit>
}
