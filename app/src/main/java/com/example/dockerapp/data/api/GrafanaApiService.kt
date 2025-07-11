package com.example.dockerapp.data.api

import com.example.dockerapp.data.model.GrafanaDashboard
import com.example.dockerapp.data.model.GrafanaDataSource
import com.example.dockerapp.data.model.GrafanaHealth
import com.example.dockerapp.data.model.GrafanaQueryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GrafanaApiService {
    
    // Health check
    @GET("api/health")
    suspend fun getHealth(): Response<GrafanaHealth>
    
    // Search dashboards
    @GET("api/search")
    suspend fun searchDashboards(
        @Query("query") query: String? = null,
        @Query("tag") tag: String? = null,
        @Query("type") type: String = "dash-db",
        @Query("folderIds") folderIds: String? = null,
        @Query("starred") starred: Boolean? = null,
        @Query("limit") limit: Int = 5000
    ): Response<List<GrafanaDashboard>>

    // Query data
    @POST("api/ds/query")
    suspend fun queryData(
        @Body request: GrafanaQueryRequest
    ): Response<GrafanaQueryResponse>
    
    // Get data sources
    @GET("api/datasources")
    suspend fun getDataSources(): Response<List<GrafanaDataSource>>

}

data class GrafanaQueryRequest(
    val queries: List<GrafanaQuery>,
    val from: String,
    val to: String
)

data class GrafanaQuery(
    val refId: String,
    val expr: String?,
    val datasource: QueryDataSource,
    val intervalMs: Long = 15000,
    val maxDataPoints: Int = 1000
)

data class QueryDataSource(
    val type: String,
    val uid: String
)