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
    val ports: List<Port>? = null,
    @SerializedName("SizeRw")
    val size: Long = 0,
    @SerializedName("SizeRootFs")
    val sizeRootFs: Long = 0,
    @SerializedName("CPUUsage")
    val cpuUsage: Double = 0.0,
    @SerializedName("MemoryUsage")
    val memoryUsage: Long = 0,
    @SerializedName("Description")
    val description: String? = null
)

data class Port(
    @SerializedName("IP")
    val ip: String = "",
    @SerializedName("PrivatePort")
    val privatePort: Int = 0,
    @SerializedName("PublicPort")
    val publicPort: Int = 0,
    @SerializedName("Type")
    val type: String = ""
)
