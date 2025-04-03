package com.example.dockerapp.data.api

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("info")
    suspend fun getInfo(): Response<Any>
}
