package com.example.dockerapp.data.model

import com.google.gson.annotations.SerializedName

data class DockerImage(
    @SerializedName("Id")
    val id: String = "",
    @SerializedName("ParentId")
    val parentId: String = "",
    @SerializedName("RepoTags")
    val repoTags: List<String>? = null,
    @SerializedName("RepoDigests")
    val repoDigests: List<String>? = null,
    @SerializedName("Created")
    val created: Long = 0,
    @SerializedName("Size")
    val size: Long = 0,
    @SerializedName("VirtualSize")
    val virtualSize: Long = 0,
    @SerializedName("SharedSize")
    val sharedSize: Long = 0,
    @SerializedName("Labels")
    val labels: Map<String, String>? = null,
    @SerializedName("Containers")
    val containers: Int = 0
)

data class DockerVolume(
    @SerializedName("Name")
    val name: String = "",
    @SerializedName("Driver")
    val driver: String = "",
    @SerializedName("Mountpoint")
    val mountpoint: String = "",
    @SerializedName("CreatedAt")
    val createdAt: String = "",
    @SerializedName("Status")
    val status: Map<String, Any>? = null,
    @SerializedName("Labels")
    val labels: Map<String, String>? = null,
    @SerializedName("Scope")
    val scope: String = "",
    @SerializedName("Options")
    val options: Map<String, String>? = null,
    @SerializedName("UsageData")
    val usageData: VolumeUsageData? = null
)

data class VolumeUsageData(
    @SerializedName("Size")
    val size: Long = 0,
    @SerializedName("RefCount")
    val refCount: Int = 0
)

data class VolumeListResponse(
    @SerializedName("Volumes")
    val volumes: List<DockerVolume>? = null,
    @SerializedName("Warnings")
    val warnings: List<String>? = null
)

data class ContainerCreateRequest(
    @SerializedName("Image")
    val image: String,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("Cmd")
    val cmd: List<String>? = null,
    @SerializedName("Env")
    val env: List<String>? = null,
    @SerializedName("ExposedPorts")
    val exposedPorts: Map<String, Any>? = null,
    @SerializedName("HostConfig")
    val hostConfig: CreateHostConfig? = null
)

data class CreateHostConfig(
    @SerializedName("PortBindings")
    val portBindings: Map<String, List<CreatePortBinding>>? = null,
    @SerializedName("Binds")
    val binds: List<String>? = null
)

data class CreatePortBinding(
    @SerializedName("HostPort")
    val hostPort: String
)

data class ContainerCreateResponse(
    @SerializedName("Id")
    val id: String,
    @SerializedName("Warnings")
    val warnings: List<String>? = null
)
