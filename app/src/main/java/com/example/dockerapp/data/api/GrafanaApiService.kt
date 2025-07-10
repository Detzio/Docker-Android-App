package com.example.dockerapp.data.api

import com.example.dockerapp.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

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
    
    // Get dashboard by UID
    @GET("api/dashboards/uid/{uid}")
    suspend fun getDashboard(@Path("uid") uid: String): Response<GrafanaDashboardDetail>
    
    // Query data
    @POST("api/ds/query")
    suspend fun queryData(
        @Body request: GrafanaQueryRequest
    ): Response<GrafanaQueryResponse>
    
    // Get data sources
    @GET("api/datasources")
    suspend fun getDataSources(): Response<List<GrafanaDataSource>>
    
    // Get data source by UID
    @GET("api/datasources/uid/{uid}")
    suspend fun getDataSource(@Path("uid") uid: String): Response<GrafanaDataSource>
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