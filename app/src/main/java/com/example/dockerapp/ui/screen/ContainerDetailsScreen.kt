package com.example.dockerapp.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dockerapp.data.model.ContainerDetails
import com.example.dockerapp.data.model.ContainerStats
import com.example.dockerapp.ui.theme.*
import com.example.dockerapp.ui.viewmodel.ContainerDetailsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerDetailsScreen(
    containerId: String,
    containerName: String,
    onBack: () -> Unit,
    viewModel: ContainerDetailsViewModel = viewModel()
) {
    val containerDetails by viewModel.containerDetails.collectAsState()
    val containerStats by viewModel.containerStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val actionInProgress by viewModel.actionInProgress.collectAsState()

    LaunchedEffect(containerId) {
        viewModel.loadContainerDetails(containerId)
    }

    // Actualiser les stats toutes les 5 secondes si le conteneur est en cours d'exécution
    LaunchedEffect(containerDetails?.state?.running) {
        if (containerDetails?.state?.running == true) {
            while (isActive) {
                delay(5000)
                if (isActive) {
                    viewModel.refreshStats(containerId)
                }
            }
        }
    }
    
    // Arrêter l'actualisation des stats quand on quitte l'écran
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopStatsRefresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(containerName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadContainerDetails(containerId) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualiser")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DockerDarkBlue,
                    titleContentColor = DockerBlue,
                    navigationIconContentColor = LightOnPrimary,
                    actionIconContentColor = LightOnPrimary
                )
            )
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Erreur",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadContainerDetails(containerId) }) {
                            Text("Réessayer")
                        }
                    }
                }
                containerDetails != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Actions du conteneur
                        item {
                            ContainerActionsCard(
                                containerDetails = containerDetails!!,
                                actionInProgress = actionInProgress,
                                onStart = { viewModel.startContainer(containerId) },
                                onStop = { viewModel.stopContainer(containerId) },
                                onRestart = { viewModel.restartContainer(containerId) }
                            )
                        }

                        // Informations générales
                        item {
                            GeneralInfoCard(containerDetails!!)
                        }

                        // Statistiques (si le conteneur est en cours d'exécution)
                        if (containerDetails!!.state?.running == true && containerStats != null) {
                            item {
                                StatsCard(containerStats!!)
                            }
                        }

                        // État du conteneur
                        item {
                            StateCard(containerDetails!!.state)
                        }

                        // Configuration
                        item {
                            ConfigurationCard(containerDetails!!.config)
                        }

                        // Réseau
                        if (containerDetails!!.networkSettings != null) {
                            item {
                                NetworkCard(containerDetails!!.networkSettings!!)
                            }
                        }

                        // Montages
                        if (!containerDetails!!.mounts.isNullOrEmpty()) {
                            item {
                                MountsCard(containerDetails!!.mounts!!)
                            }
                        }

                        // Variables d'environnement
                        if (!containerDetails!!.config?.env.isNullOrEmpty()) {
                            item {
                                EnvironmentCard(containerDetails!!.config!!.env!!)
                            }
                        }

                        // Labels
                        if (!containerDetails!!.config?.labels.isNullOrEmpty()) {
                            item {
                                LabelsCard(containerDetails!!.config!!.labels!!)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContainerActionsCard(
    containerDetails: ContainerDetails,
    actionInProgress: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (containerDetails.state?.status?.lowercase()) {
                    "running" -> {
                        Button(
                            onClick = onStop,
                            enabled = !actionInProgress,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusStopped,
                                contentColor = LightOnError
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (actionInProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = LightOnError
                                )
                            } else {
                                Text("Arrêter")
                            }
                        }
                        
                        Button(
                            onClick = onRestart,
                            enabled = !actionInProgress,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DockerBlue,
                                contentColor = LightOnPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (actionInProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = LightOnPrimary
                                )
                            } else {
                                Text("Redémarrer")
                            }
                        }
                    }
                    "exited" -> {
                        Button(
                            onClick = onStart,
                            enabled = !actionInProgress,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusRunning,
                                contentColor = LightOnPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (actionInProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = LightOnPrimary
                                )
                            } else {
                                Text("Démarrer")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeneralInfoCard(containerDetails: ContainerDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Informations générales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow("ID", containerDetails.id.take(12))
            InfoRow("Nom", containerDetails.name.removePrefix("/"))
            InfoRow("Image", containerDetails.image)
            InfoRow("Plateforme", containerDetails.platform)
            InfoRow("Driver", containerDetails.driver)
            InfoRow("Redémarrages", containerDetails.restartCount.toString())
            
            if (containerDetails.created.isNotEmpty()) {
                InfoRow("Créé", formatDateTime(containerDetails.created))
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun StatsCard(containerStats: ContainerStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Statistiques en temps réel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CPU",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "${String.format("%.1f", containerStats.calculateCpuPercentage())}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DockerBlue
                    )
                }
                
                Column {
                    Text(
                        text = "Mémoire",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = formatSize(containerStats.memoryStats.usage),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DockerBlue
                    )
                }
                
                Column {
                    Text(
                        text = "Limite mémoire",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = formatSize(containerStats.memoryStats.limit),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DockerBlue
                    )
                }
            }
        }
    }
}

@Composable
fun StateCard(state: com.example.dockerapp.data.model.ContainerState?) {
    if (state == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "État du conteneur",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow("Statut", state.status)
            InfoRow("En cours d'exécution", if (state.running) "Oui" else "Non")
            InfoRow("En pause", if (state.paused) "Oui" else "Non")
            InfoRow("Redémarrage", if (state.restarting) "Oui" else "Non")
            InfoRow("PID", state.pid.toString())
            InfoRow("Code de sortie", state.exitCode.toString())
            
            if (state.startedAt.isNotEmpty()) {
                InfoRow("Démarré le", formatDateTime(state.startedAt))
            }
            
            if (state.finishedAt.isNotEmpty() && state.finishedAt != "0001-01-01T00:00:00Z") {
                InfoRow("Terminé le", formatDateTime(state.finishedAt))
            }
            
            if (state.error.isNotEmpty()) {
                InfoRow("Erreur", state.error)
            }
        }
    }
}

@Composable
fun ConfigurationCard(config: com.example.dockerapp.data.model.ContainerConfig?) {
    if (config == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (config.hostname.isNotEmpty()) {
                InfoRow("Hostname", config.hostname)
            }
            if (config.user.isNotEmpty()) {
                InfoRow("Utilisateur", config.user)
            }
            if (config.workingDir.isNotEmpty()) {
                InfoRow("Répertoire de travail", config.workingDir)
            }
            
            config.cmd?.let { cmd ->
                if (cmd.isNotEmpty()) {
                    InfoRow("Commande", cmd.joinToString(" "))
                }
            }
            
            config.entrypoint?.let { entrypoint ->
                if (entrypoint.isNotEmpty()) {
                    InfoRow("Point d'entrée", entrypoint.joinToString(" "))
                }
            }
        }
    }
}

@Composable
fun NetworkCard(networkSettings: com.example.dockerapp.data.model.NetworkSettings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Réseau",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (networkSettings.ipAddress.isNotEmpty()) {
                InfoRow("Adresse IP", networkSettings.ipAddress)
            }
            if (networkSettings.gateway.isNotEmpty()) {
                InfoRow("Passerelle", networkSettings.gateway)
            }
            
            // Gestion sécurisée des réseaux
            networkSettings.networks?.let { networks ->
                networks.forEach { (networkName, networkInfo) ->
                    if (networkName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Réseau: $networkName",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (networkInfo.ipAddress.isNotEmpty()) {
                            InfoRow("  IP", networkInfo.ipAddress)
                        }
                        if (networkInfo.gateway.isNotEmpty()) {
                            InfoRow("  Passerelle", networkInfo.gateway)
                        }
                    }
                }
            }
            
            // Gestion sécurisée des ports
            networkSettings.ports?.let { portsMap ->
                portsMap.forEach { (port, bindings) ->
                    bindings?.let { bindingsList ->
                        if (bindingsList.isNotEmpty()) {
                            bindingsList.forEach { binding ->
                                val hostInfo = when {
                                    binding.hostIp.isNotEmpty() && binding.hostPort.isNotEmpty() -> 
                                        "${binding.hostIp}:${binding.hostPort}"
                                    binding.hostPort.isNotEmpty() -> 
                                        binding.hostPort
                                    else -> 
                                        "Non mappé"
                                }
                                InfoRow("Port $port", hostInfo)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MountsCard(mounts: List<com.example.dockerapp.data.model.Mount>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Montages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            mounts.forEach { mount ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        InfoRow("Type", mount.type)
                        InfoRow("Source", mount.source)
                        InfoRow("Destination", mount.destination)
                        InfoRow("Mode", mount.mode)
                        InfoRow("Lecture/Écriture", if (mount.rw) "Oui" else "Non")
                    }
                }
            }
        }
    }
}

@Composable
fun EnvironmentCard(env: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Variables d'environnement",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier.height(200.dp)
            ) {
                items(env) { variable ->
                    val parts = variable.split("=", limit = 2)
                    if (parts.size == 2) {
                        InfoRow(parts[0], parts[1])
                    } else {
                        InfoRow("Variable", variable)
                    }
                }
            }
        }
    }
}

@Composable
fun LabelsCard(labels: Map<String, String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Labels",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyColumn(
                modifier = Modifier.height(150.dp)
            ) {
                items(labels.entries.toList()) { (key, value) ->
                    InfoRow(key, value)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
    }
}

@SuppressLint("DefaultLocale")
private fun formatSize(bytes: Long): String {
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

private fun formatDateTime(dateTime: String): String {
    return try {
        val instant = Instant.parse(dateTime)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateTime
    }
}