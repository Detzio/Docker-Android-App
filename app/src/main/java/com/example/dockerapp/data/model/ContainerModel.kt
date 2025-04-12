package com.example.dockerapp.data.model

data class Container(
    val id: String = "",
    val names: List<String>? = null,
    val image: String = "",
    val state: String = "",
    val status: String = ""
)
