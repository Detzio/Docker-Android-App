package com.example.dockerapp.data.model

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName

data class ContainerStats(
    @SerializedName("cpu_stats")
    val cpuStats: CpuStats,
    @SerializedName("precpu_stats")
    val preCpuStats: CpuStats,
    @SerializedName("memory_stats")
    val memoryStats: MemoryStats
) {
    @SuppressLint("DefaultLocale")
    fun calculateCpuPercentage(): Float {
        try {
            val cpuDelta = cpuStats.cpuUsage.totalUsage - preCpuStats.cpuUsage.totalUsage
            val systemDelta = cpuStats.systemCpuUsage - preCpuStats.systemCpuUsage

            if (systemDelta <= 0 || cpuDelta < 0) {
                return 0.0f
            }

            // Conversion en Double pour une meilleure précision
            val cpuDeltaD = cpuDelta.toDouble()
            val systemDeltaD = systemDelta.toDouble()
            val numCPUs = cpuStats.cpuUsage.percpuUsage.size.toDouble()

            // Nouveau calcul avec une meilleure précision
            val cpuPercent = ((cpuDeltaD / systemDeltaD) * numCPUs * 100.0)
                .coerceIn(0.0, 100.0)
                .toFloat()

            // Arrondir à 2 décimales pour éviter les valeurs trop petites
            return String.format("%.2f", cpuPercent).toFloat()
        } catch (e: Exception) {
            return 0.0f
        }
    }

}

data class CpuStats(
    @SerializedName("cpu_usage")
    val cpuUsage: CpuUsage,
    @SerializedName("system_cpu_usage")
    val systemCpuUsage: Long
)

data class CpuUsage(
    @SerializedName("total_usage")
    val totalUsage: Long,
    @SerializedName("percpu_usage")
    val percpuUsage: List<Long>
)

data class MemoryStats(
    @SerializedName("usage")
    val usage: Long,
    @SerializedName("max_usage")
    val maxUsage: Long,
    @SerializedName("limit")
    val limit: Long
)

