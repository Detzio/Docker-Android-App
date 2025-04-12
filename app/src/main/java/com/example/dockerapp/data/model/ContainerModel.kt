package com.example.dockerapp.data.model

import com.google.gson.annotations.SerializedName

data class Container(
    @SerializedName("Id")
    val id: String = "",
    @SerializedName("Names")
    val names: List<String>? = null,
    @SerializedName("Image")
    val image: String = "",
    @SerializedName("State")
    val state: String = "",
    @SerializedName("Status")
    val status: String = "",
    @SerializedName("Ports")
    val ports: List<Port>? = null
)

data class Port(
    @SerializedName("PrivatePort")
    val privatePort: Int,
    @SerializedName("PublicPort")
    val publicPort: Int,
    @SerializedName("Type")
    val type: String
)
