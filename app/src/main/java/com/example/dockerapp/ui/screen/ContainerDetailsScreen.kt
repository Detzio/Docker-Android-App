package com.example.dockerapp.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dockerapp.data.model.ContainerDetails
import com.example.dockerapp.data.model.ContainerStats
import com.example.dockerapp.ui.components.DockerLogo
import com.example.dockerapp.ui.components.RotatingDockerLogo
import com.example.dockerapp.ui.theme.DockerBlue
import com.example.dockerapp.ui.theme.DockerDarkBlue
import com.example.dockerapp.ui.theme.DockerOrange
import com.example.dockerapp.ui.theme.ErrorColor
import com.example.dockerapp.ui.theme.LightBackground
import com.example.dockerapp.ui.theme.LightSurface
import com.example.dockerapp.ui.theme.StatusPaused
import com.example.dockerapp.ui.theme.StatusRunning
import com.example.dockerapp.ui.theme.StatusStopped
import com.example.dockerapp.ui.theme.WarningColor
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
    viewModel: ContainerDetailsViewModel = viewModel(),
    onNavigateToTerminal: (containerId: String, containerName: String?) -> Unit
) {
    val containerDetails by viewModel.containerDetails.collectAsState()
    val containerStats by viewModel.containerStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val actionInProgress by viewModel.actionInProgress.collectAsState()

    LaunchedEffect(containerId) {
        viewModel.loadContainerDetails(containerId)
    }    

    // Actualiser les stats toutes les 10 secondes si le conteneur est en cours d'exécution
    LaunchedEffect(containerDetails?.state?.running) {
        if (containerDetails?.state?.running == true) {
            while (isActive) {
                delay(10000)
                if (isActive && containerDetails?.state?.running == true) {
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
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DockerLogo(size = 28.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(containerName) 
                    }
                },
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
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = DockerBlue
                )
            )
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightBackground)
        ) {
            when {
                isLoading -> {
                    RotatingDockerLogo(
                        size = 64.dp,
                        modifier = Modifier.align(Alignment.Center),
                        color = DockerBlue
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
                            color = ErrorColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error ?: "",
                            color = ErrorColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadContainerDetails(containerId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DockerBlue
                            )
                        ) {
                            Text("Réessayer", color = Color.White)
                        }
                    }
                }
                containerDetails != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Actions du conteneur
                        item {
                            ContainerActionsCard(
                                containerDetails = containerDetails!!,
                                actionInProgress = actionInProgress,
                                onStart = { viewModel.startContainer(containerId) },
                                onStop = { viewModel.stopContainer(containerId) },
                                onRestart = { viewModel.restartContainer(containerId) },
                                onTerminal = { onNavigateToTerminal(containerId, containerName) }
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
    onTerminal: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DockerDarkBlue),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
                                disabledContainerColor = StatusStopped.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (actionInProgress) {
                                RotatingDockerLogo(
                                    size = 20.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("Arrêter", color = Color.White)
                            }
                        }

                        Button(
                            onClick = onRestart,
                            enabled = !actionInProgress,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DockerBlue,
                                disabledContainerColor = DockerBlue.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (actionInProgress) {
                                RotatingDockerLogo(
                                    size = 20.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("Redémarrer", color = Color.White)
                            }
                        }
                    }
                    "exited" -> {
                        Button(
                            onClick = onStart,
                            enabled = !actionInProgress,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusRunning,
                                disabledContainerColor = StatusRunning.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (actionInProgress) {
                                RotatingDockerLogo(
                                    size = 20.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("Démarrer", color = Color.White)
                            }
                        }
                    }
                }
            }

            when (containerDetails.state?.status?.lowercase()) {
                "running" -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onTerminal,
                            enabled = !actionInProgress,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DockerOrange,
                                disabledContainerColor = DockerOrange.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (actionInProgress) {
                                RotatingDockerLogo(
                                    size = 20.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("Terminal", color = Color.White)
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
    DetailCard(title = "Informations générales") {
        InfoRow("ID", containerDetails.id.take(12), DockerDarkBlue)
        InfoRow("Nom", containerDetails.name.removePrefix("/"), DockerBlue)
        InfoRow("Image", containerDetails.image)
        InfoRow("Plateforme", containerDetails.platform)
        InfoRow("Driver", containerDetails.driver)
        InfoRow("Redémarrages", containerDetails.restartCount.toString(), 
            if(containerDetails.restartCount > 0) WarningColor else null)
        
        if (containerDetails.created.isNotEmpty()) {
            InfoRow("Créé", formatDateTime(containerDetails.created))
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun StatsCard(containerStats: ContainerStats) {
    DetailCard(title = "Statistiques en temps réel") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem(
                label = "CPU",
                value = "${String.format("%.2f", containerStats.calculateCpuPercentage())}%",
                color = DockerBlue
            )
            
            StatItem(
                label = "Mémoire",
                value = formatSize(containerStats.memoryStats.usage),
                color = DockerBlue
            )
            
            StatItem(
                label = "Limite mémoire",
                value = formatSize(containerStats.memoryStats.limit),
                color = DockerDarkBlue
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun StateCard(state: com.example.dockerapp.data.model.ContainerState?) {
    if (state == null) return
    
    val statusColor = when (state.status.lowercase()) {
        "running" -> StatusRunning
        "exited" -> StatusStopped
        "paused" -> StatusPaused
        else -> Color.Gray
    }
    
    DetailCard(title = "État du conteneur") {
        InfoRow("Statut", state.status, statusColor)
        InfoRow("En cours d'exécution", if (state.running) "Oui" else "Non", 
            if (state.running) StatusRunning else StatusStopped)
        InfoRow("En pause", if (state.paused) "Oui" else "Non",
            if (state.paused) StatusPaused else null)
        InfoRow("Redémarrage", if (state.restarting) "Oui" else "Non",
            if (state.restarting) WarningColor else null)
        InfoRow("PID", state.pid.toString())
        InfoRow("Code de sortie", state.exitCode.toString(), 
            if (state.exitCode > 0) ErrorColor else null)
        
        if (state.startedAt.isNotEmpty()) {
            InfoRow("Démarré le", formatDateTime(state.startedAt))
        }
        
        if (state.finishedAt.isNotEmpty() && state.finishedAt != "0001-01-01T00:00:00Z") {
            InfoRow("Terminé le", formatDateTime(state.finishedAt))
        }
        
        if (state.error.isNotEmpty()) {
            InfoRow("Erreur", state.error, ErrorColor)
        }
    }
}

@Composable
fun ConfigurationCard(config: com.example.dockerapp.data.model.ContainerConfig?) {
    if (config == null) return
    
    DetailCard(title = "Configuration") {
        if (config.hostname.isNotEmpty()) {
            InfoRow("Hostname", config.hostname)
        }
        if (config.user.isNotEmpty()) {
            InfoRow("Utilisateur", config.user, DockerBlue)
        }
        if (config.workingDir.isNotEmpty()) {
            InfoRow("Répertoire de travail", config.workingDir)
        }
        
        config.cmd?.let { cmd ->
            if (cmd.isNotEmpty()) {
                InfoRow("Commande", cmd.joinToString(" "), DockerDarkBlue)
            }
        }
        
        config.entrypoint?.let { entrypoint ->
            if (entrypoint.isNotEmpty()) {
                InfoRow("Point d'entrée", entrypoint.joinToString(" "), DockerDarkBlue)
            }
        }
    }
}

@Composable
fun NetworkCard(networkSettings: com.example.dockerapp.data.model.NetworkSettings) {
    DetailCard(title = "Réseau") {
        if (networkSettings.ipAddress.isNotEmpty()) {
            InfoRow("Adresse IP", networkSettings.ipAddress, DockerBlue)
        }
        if (networkSettings.gateway.isNotEmpty()) {
            InfoRow("Passerelle", networkSettings.gateway, DockerBlue)
        }
        
        // Gestion sécurisée des réseaux
        networkSettings.networks?.let { networks ->
            networks.forEach { (networkName, networkInfo) ->
                if (networkName.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Réseau: $networkName",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = DockerDarkBlue
                    )
                    if (networkInfo.ipAddress.isNotEmpty()) {
                        InfoRow("  IP", networkInfo.ipAddress, DockerBlue)
                    }
                    if (networkInfo.gateway.isNotEmpty()) {
                        InfoRow("  Passerelle", networkInfo.gateway, DockerBlue)
                    }
                }
            }
        }
        
        // Gestion sécurisée des ports
        networkSettings.ports?.let { portsMap ->
            if (portsMap.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ports:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = DockerDarkBlue
                )
                
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
                                InfoRow("  $port", hostInfo, DockerOrange)
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
    DetailCard(title = "Montages") {
        mounts.forEach { mount ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    InfoRow("Type", mount.type, DockerDarkBlue)
                    InfoRow("Source", mount.source)
                    InfoRow("Destination", mount.destination, DockerBlue)
                    InfoRow("Mode", mount.mode)
                    InfoRow("Lecture/Écriture", if (mount.rw) "Oui" else "Non", 
                        if (mount.rw) StatusRunning else StatusStopped)
                }
            }
        }
    }
}

@Composable
fun EnvironmentCard(env: List<String>) {
    DetailCard(title = "Variables d'environnement") {
        LazyColumn(
            modifier = Modifier
                .height(200.dp)
                .background(LightSurface, shape = MaterialTheme.shapes.small)
        ) {
            items(env) { variable ->
                val parts = variable.split("=", limit = 2)
                if (parts.size == 2) {
                    InfoRow(parts[0], parts[1], DockerBlue)
                } else {
                    InfoRow("Variable", variable)
                }
            }
        }
    }
}

@Composable
fun LabelsCard(labels: Map<String, String>) {
    DetailCard(title = "Labels") {
        LazyColumn(
            modifier = Modifier
                .height(150.dp)
                .background(LightSurface, shape = MaterialTheme.shapes.small)
        ) {
            items(labels.entries.toList()) { (key, value) ->
                InfoRow(key, value, DockerBlue)
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DockerDarkBlue
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                thickness = 1.dp,
                color = LightSurface
            )
            
            content()
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: Color.Black,
            fontWeight = if (valueColor != null) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(2f),
            overflow = TextOverflow.Ellipsis
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

