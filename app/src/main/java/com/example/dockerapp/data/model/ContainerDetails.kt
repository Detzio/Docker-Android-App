package com.example.dockerapp.data.model

import com.google.gson.annotations.SerializedName

data class ContainerDetails(
    @SerializedName("Id")
    val id: String = "",
    @SerializedName("Name")
    val name: String = "",
    @SerializedName("Config")
    val config: ContainerConfig? = null,
    @SerializedName("State")
    val state: ContainerState? = null,
    @SerializedName("Image")
    val image: String = "",
    @SerializedName("Created")
    val created: String = "",
    @SerializedName("NetworkSettings")
    val networkSettings: NetworkSettings? = null,
    @SerializedName("Mounts")
    val mounts: List<Mount>? = null,
    @SerializedName("HostConfig")
    val hostConfig: HostConfig? = null,
    @SerializedName("RestartCount")
    val restartCount: Int = 0,
    @SerializedName("Platform")
    val platform: String = "",
    @SerializedName("Driver")
    val driver: String = ""
)

data class ContainerConfig(
    @SerializedName("Hostname")
    val hostname: String = "",
    @SerializedName("User")
    val user: String = "",
    @SerializedName("Env")
    val env: List<String>? = null,
    @SerializedName("Cmd")
    val cmd: List<String>? = null,
    @SerializedName("Image")
    val image: String = "",
    @SerializedName("WorkingDir")
    val workingDir: String = "",
    @SerializedName("Entrypoint")
    val entrypoint: List<String>? = null,
    @SerializedName("Labels")
    val labels: Map<String, String>? = null,
    @SerializedName("ExposedPorts")
    val exposedPorts: Map<String, Any>? = null
)

data class ContainerState(
    @SerializedName("Status")
    val status: String = "",
    @SerializedName("Running")
    val running: Boolean = false,
    @SerializedName("Paused")
    val paused: Boolean = false,
    @SerializedName("Restarting")
    val restarting: Boolean = false,
    @SerializedName("OOMKilled")
    val oomKilled: Boolean = false,
    @SerializedName("Dead")
    val dead: Boolean = false,
    @SerializedName("Pid")
    val pid: Int = 0,
    @SerializedName("ExitCode")
    val exitCode: Int = 0,
    @SerializedName("Error")
    val error: String = "",
    @SerializedName("StartedAt")
    val startedAt: String = "",
    @SerializedName("FinishedAt")
    val finishedAt: String = ""
)

data class NetworkSettings(
    @SerializedName("IPAddress")
    val ipAddress: String = "",
    @SerializedName("Gateway")
    val gateway: String = "",
    @SerializedName("Networks")
    val networks: Map<String, NetworkInfo>? = null,
    @SerializedName("Ports")
    val ports: Map<String, List<PortBinding>?>? = null
)

data class NetworkInfo(
    @SerializedName("IPAddress")
    val ipAddress: String = "",
    @SerializedName("Gateway")
    val gateway: String = "",
    @SerializedName("NetworkID")
    val networkId: String = ""
)

data class PortBinding(
    @SerializedName("HostIp")
    val hostIp: String = "",
    @SerializedName("HostPort")
    val hostPort: String = ""
)

data class Mount(
    @SerializedName("Type")
    val type: String = "",
    @SerializedName("Source")
    val source: String = "",
    @SerializedName("Destination")
    val destination: String = "",
    @SerializedName("Mode")
    val mode: String = "",
    @SerializedName("RW")
    val rw: Boolean = false,
    @SerializedName("Propagation")
    val propagation: String = ""
)

data class HostConfig(
    @SerializedName("CpuShares")
    val cpuShares: Int = 0,
    @SerializedName("Memory")
    val memory: Long = 0,
    @SerializedName("RestartPolicy")
    val restartPolicy: RestartPolicy? = null,
    @SerializedName("NetworkMode")
    val networkMode: String = "",
    @SerializedName("PortBindings")
    val portBindings: Map<String, List<PortBinding>?>? = null
)

data class RestartPolicy(
    @SerializedName("Name")
    val name: String = "",
    @SerializedName("MaximumRetryCount")
    val maximumRetryCount: Int = 0
)