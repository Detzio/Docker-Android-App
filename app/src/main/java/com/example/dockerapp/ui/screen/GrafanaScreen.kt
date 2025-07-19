package com.example.dockerapp.ui.screen

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dockerapp.ui.components.DockerLogo
import com.example.dockerapp.ui.components.MetricChart
import com.example.dockerapp.ui.components.MetricSummaryCard
import com.example.dockerapp.ui.components.RotatingDockerLogo
import com.example.dockerapp.ui.theme.DockerBlue
import com.example.dockerapp.ui.theme.DockerDarkBlue
import com.example.dockerapp.ui.theme.LightBackground
import com.example.dockerapp.ui.theme.LightOnPrimary
import com.example.dockerapp.ui.theme.StatusPaused
import com.example.dockerapp.ui.theme.StatusRunning
import com.example.dockerapp.ui.viewmodel.GrafanaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrafanaScreen(
    onBack: () -> Unit,
    grafanaViewModel: GrafanaViewModel = viewModel()
) {
    val isLoading by grafanaViewModel.isLoading.collectAsState()
    val errorMessage by grafanaViewModel.errorMessage.collectAsState()
    val cpuMetrics by grafanaViewModel.cpuMetrics.collectAsState()
    val memoryMetrics by grafanaViewModel.memoryMetrics.collectAsState()
    val networkMetrics by grafanaViewModel.networkMetrics.collectAsState()
    val totalContainers by grafanaViewModel.totalContainers.collectAsState()
    val runningContainers by grafanaViewModel.runningContainers.collectAsState()
    val cpuUsage by grafanaViewModel.cpuUsage.collectAsState()
    val memoryUsage by grafanaViewModel.memoryUsage.collectAsState()
    val grafanaUrl by grafanaViewModel.grafanaUrl.collectAsState()
    
    LaunchedEffect(Unit) {
        grafanaViewModel.loadMetrics()
        grafanaViewModel.refreshGrafanaUrl()
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DockerLogo(size = 28.dp, color = LightOnPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Métriques Docker",
                            color = LightOnPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = LightOnPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { grafanaViewModel.loadMetrics() }) {
                        if (isLoading) {
                            RotatingDockerLogo(
                                size = 24.dp,
                                color = LightOnPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualiser",
                                tint = LightOnPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DockerDarkBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = LightBackground
    ) { paddingValues ->
        DockerMetricsContent(
            cpuMetrics = cpuMetrics,
            memoryMetrics = memoryMetrics,
            networkMetrics = networkMetrics,
            totalContainers = totalContainers,
            runningContainers = runningContainers,
            cpuUsage = cpuUsage,
            memoryUsage = memoryUsage,
            grafanaUrl = grafanaUrl,
            errorMessage = errorMessage,
            isLoading = isLoading,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun DockerMetricsContent(
    cpuMetrics: List<com.example.dockerapp.ui.viewmodel.MetricPoint>,
    memoryMetrics: List<com.example.dockerapp.ui.viewmodel.MetricPoint>,
    networkMetrics: List<com.example.dockerapp.ui.viewmodel.MetricPoint>,
    totalContainers: Int,
    runningContainers: Int,
    cpuUsage: Double,
    memoryUsage: Long,
    grafanaUrl: String,
    errorMessage: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    if (isLoading && cpuMetrics.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RotatingDockerLogo(
                    size = 64.dp,
                    color = DockerBlue
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Chargement des métriques...",
                    color = DockerBlue
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section des métriques en temps réel
            item {
                Column {
                    Text(
                        text = "Métriques en temps réel",
                        style = MaterialTheme.typography.titleLarge,
                        color = DockerBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Données provenant de l'API Docker",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            
            // Cartes de résumé
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        MetricSummaryCard(
                            title = "Conteneurs totaux",
                            value = totalContainers.toString(),
                            subtitle = "$runningContainers en cours",
                            modifier = Modifier.width(150.dp)
                        )
                    }
                    
                    item {
                        MetricSummaryCard(
                            title = "CPU Moyen",
                            value = "${String.format("%.1f", cpuUsage)}%",
                            subtitle = "Utilisation",
                            modifier = Modifier.width(150.dp)
                        )
                    }
                    
                    item {
                        MetricSummaryCard(
                            title = "Mémoire",
                            value = formatBytes(memoryUsage),
                            subtitle = "Utilisée",
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }
            }
            
            // Graphiques détaillés
            if (cpuMetrics.isNotEmpty()) {
                item {
                    MetricChart(
                        title = "Utilisation CPU",
                        data = cpuMetrics,
                        unit = "%",
                        color = StatusRunning,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            if (memoryMetrics.isNotEmpty()) {
                item {
                    MetricChart(
                        title = "Utilisation Mémoire",
                        data = memoryMetrics,
                        unit = "MB",
                        color = DockerBlue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            if (networkMetrics.isNotEmpty()) {
                item {
                    MetricChart(
                        title = "Trafic Réseau",
                        data = networkMetrics,
                        unit = "MB/s",
                        color = StatusPaused,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Bouton pour ouvrir Grafana externe
            if (grafanaUrl.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Ouvrir Grafana",
                                style = MaterialTheme.typography.titleMedium,
                                color = DockerBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Accéder à l'interface Grafana complète",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    Log.d("GrafanaScreen", "Opening Grafana URL: $grafanaUrl")
                                    if (grafanaUrl.isNotEmpty()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, grafanaUrl.toUri())
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Log.e("GrafanaScreen", "Error opening Grafana URL", e)
                                        }
                                    } else {
                                        Log.w("GrafanaScreen", "Grafana URL is empty")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DockerBlue
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ouvrir dans le navigateur")
                            }
                        }
                    }
                }
            }
            
            // Message d'erreur
            errorMessage?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1.0 -> String.format("%.1f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.1f KB", kb)
        else -> String.format("%d B", bytes)
    }
}