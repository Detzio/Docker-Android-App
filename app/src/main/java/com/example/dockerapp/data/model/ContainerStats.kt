package com.example.dockerapp.data.model

import android.annotation.SuppressLint
import android.util.Log
import com.google.gson.annotations.SerializedName
import kotlin.math.round

const val TAG = "ContainerStatModel"

data class ContainerStats(
    @SerializedName("cpu_stats")
    val cpuStats: CpuStats,
    @SerializedName("precpu_stats")
    val preCpuStats: CpuStats,
    @SerializedName("memory_stats")
    val memoryStats: MemoryStats
) {
    @SuppressLint("DefaultLocale")
    fun calculateCpuPercentage(): Double {
        try {
            val cpuDelta = (cpuStats.cpuUsage.totalUsage - preCpuStats.cpuUsage.totalUsage).toDouble()
            val systemDelta = (cpuStats.systemCpuUsage - preCpuStats.systemCpuUsage).toDouble()
            val onlineCpu = cpuStats.onlineCpu.toDouble()

//            Log.d(TAG, "calculateCpuPercentage: cpuDelta: $cpuDelta")
//            Log.d(TAG, "calculateCpuPercentage: systemDelta: $systemDelta")
//            Log.d(TAG, "calculateCpuPercentage: onlineCpu: $onlineCpu")

            if (systemDelta <= 0 || cpuDelta < 0) {
                return 0.0
            }

            // Nouveau calcul avec une meilleure précision
            val cpuPercent = ((cpuDelta / systemDelta) * onlineCpu * 100.0).coerceIn(0.0, 100.0)

//            Log.d(TAG, "calculateCpuPercentage: result: $cpuPercent")

            // Arrondir à 2 décimales
            return (round(cpuPercent * 100) / 100.0)
        } catch (e: Exception) {
            Log.e(TAG, "calculateCpuPercentage: error: ${e.message}")
            return 0.0
        }
    }
}

data class CpuStats(
    @SerializedName("cpu_usage")
    val cpuUsage: CpuUsage,
    @SerializedName("system_cpu_usage")
    val systemCpuUsage: Long,
    @SerializedName("online_cpus")
    val onlineCpu: Int
)

data class CpuUsage(
    @SerializedName("total_usage")
    val totalUsage: Long,
    @SerializedName("percpu_usage")
    val perCpuUsage: List<Long>
)

data class MemoryStats(
    @SerializedName("usage")
    val usage: Long,
    @SerializedName("max_usage")
    val maxUsage: Long,
    @SerializedName("limit")
    val limit: Long
)

// Version simplifiée pour réduire la consommation mémoire
//data class SimpleContainerStats(
//    val cpuPercentage: Float = 0f,
//    val memoryUsage: Long = 0L,
//    val memoryLimit: Long = 0L
//)

