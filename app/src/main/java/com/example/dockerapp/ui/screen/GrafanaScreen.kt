package com.example.dockerapp.ui.screen

import android.content.Intent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    val authState by grafanaViewModel.authState.collectAsState()
    val isAuthenticated by grafanaViewModel.isAuthenticated.collectAsState()
    val isLoading by grafanaViewModel.isLoading.collectAsState()
    val errorMessage by grafanaViewModel.errorMessage.collectAsState()
    val dashboards by grafanaViewModel.dashboards.collectAsState()
    val dataSources by grafanaViewModel.dataSources.collectAsState()
    val cpuMetrics by grafanaViewModel.cpuMetrics.collectAsState()
    val memoryMetrics by grafanaViewModel.memoryMetrics.collectAsState()
    val networkMetrics by grafanaViewModel.networkMetrics.collectAsState()
    val availableMetrics by grafanaViewModel.availableMetrics.collectAsState()
    val grafanaUrl by grafanaViewModel.grafanaUrl.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DockerLogo(size = 32.dp)
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        Text("Grafana Analytics", color = DockerBlue)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Retour",
                            tint = LightOnPrimary
                        )
                    }
                },
                actions = {
                    if (isAuthenticated) {
                        IconButton(onClick = { grafanaViewModel.refreshData() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Actualiser",
                                tint = LightOnPrimary
                            )
                        }
                        IconButton(onClick = { grafanaViewModel.logout() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Déconnexion",
                                tint = LightOnPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
            when (authState) {
                is GrafanaViewModel.AuthState.Loading -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RotatingDockerLogo(
                            size = 64.dp,
                            color = DockerBlue
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Connexion à Grafana...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = DockerBlue
                        )
                    }
                }
                
                is GrafanaViewModel.AuthState.NotAuthenticated -> {
                    GrafanaLoginForm(
                        onAuthenticate = { username, password, serverUrl ->
                            grafanaViewModel.authenticate(username, password, serverUrl)
                        },
                        errorMessage = errorMessage,
                        onClearError = { grafanaViewModel.clearError() }
                    )
                }
                
                is GrafanaViewModel.AuthState.Authenticated -> {
                    if (isLoading) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            RotatingDockerLogo(
                                size = 64.dp,
                                color = DockerBlue
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Chargement des données...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = DockerBlue
                            )
                        }
                    } else {
                        GrafanaContent(
                            dashboards = dashboards,
                            dataSources = dataSources,
                            cpuMetrics = cpuMetrics,
                            memoryMetrics = memoryMetrics,
                            networkMetrics = networkMetrics,
                            availableMetrics = availableMetrics,
                            grafanaUrl = grafanaUrl,
                            errorMessage = errorMessage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GrafanaLoginForm(
    onAuthenticate: (String, String, String) -> Unit,
    errorMessage: String?,
    onClearError: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf("") }
    
    // Effacer l'erreur quand l'utilisateur modifie les champs
    LaunchedEffect(username, password, serverUrl) {
        if (errorMessage != null) {
            onClearError()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DockerLogo(size = 64.dp)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Connexion Grafana",
            style = MaterialTheme.typography.headlineMedium,
            color = DockerBlue
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            label = { Text("URL du serveur Docker") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            ),
            supportingText = { 
                Text("https://exemple.sh:3000")
            }
        )
        
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nom d'utilisateur Grafana") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe Grafana") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )
        
        Button(
            onClick = { onAuthenticate(username, password, serverUrl) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = username.isNotEmpty() && password.isNotEmpty() && serverUrl.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = DockerBlue,
                contentColor = LightOnPrimary
            )
        ) {
            Text("Se connecter à Grafana")
        }
        
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun GrafanaContent(
    dashboards: List<com.example.dockerapp.data.model.GrafanaDashboard>,
    dataSources: List<com.example.dockerapp.data.model.GrafanaDataSource>,
    cpuMetrics: List<com.example.dockerapp.ui.viewmodel.MetricPoint>,
    memoryMetrics: List<com.example.dockerapp.ui.viewmodel.MetricPoint>,
    networkMetrics: List<com.example.dockerapp.ui.viewmodel.MetricPoint>,
    availableMetrics: List<String>,
    grafanaUrl: String,
    errorMessage: String?
) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section des métriques en temps réel
        item {
            Column {
                Text(
                    text = "Métriques en temps réel",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DockerBlue
                )
                
                // Bouton pour ouvrir Grafana dans le navigateur
                if (grafanaUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, grafanaUrl.toUri())
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Gérer l'erreur si aucun navigateur n'est disponible
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DockerBlue
                        )
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Ouvrir Grafana",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ouvrir Grafana")
                    }
                }
            }
        }
        
        // Cartes de résumé
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                item {
                    MetricSummaryCard(
                        title = "CPU",
                        value = "${String.format("%.1f", cpuMetrics.lastOrNull()?.value ?: 0.0)}%",
                        subtitle = "Utilisation moyenne",
                        icon = {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = StatusRunning
                            )
                        },
                        modifier = Modifier.width(150.dp)
                    )
                }
                
                item {
                    MetricSummaryCard(
                        title = "Mémoire",
                        value = "${String.format("%.0f", memoryMetrics.lastOrNull()?.value ?: 0.0)} MB",
                        subtitle = "Utilisation actuelle",
                        icon = {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = DockerBlue
                            )
                        },
                        modifier = Modifier.width(150.dp)
                    )
                }
                
                item {
                    MetricSummaryCard(
                        title = "Réseau",
                        value = "${String.format("%.2f", networkMetrics.lastOrNull()?.value ?: 0.0)}",
                        subtitle = "Trafic I/O (MB/s)",
                        icon = {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = StatusPaused
                            )
                        },
                        modifier = Modifier.width(150.dp)
                    )
                }
            }
        }
        
        // Graphiques détaillés
        item {
            MetricChart(
                title = "Utilisation CPU (%)",
                data = cpuMetrics,
                unit = "%",
                color = StatusRunning,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            MetricChart(
                title = "Utilisation Mémoire (MB)",
                data = memoryMetrics,
                unit = "MB",
                color = DockerBlue,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            MetricChart(
                title = "Trafic Réseau (MB/s)",
                data = networkMetrics,
                unit = "MB/s",
                color = StatusPaused,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Section des dashboards disponibles
        if (dashboards.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Dashboards disponibles",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DockerBlue
                )
            }
            
            items(dashboards) { dashboard ->
                DashboardCard(dashboard = dashboard)
            }
        }
        
        // Section des sources de données
        if (dataSources.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Sources de données",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DockerBlue
                )
            }
            
            items(dataSources) { dataSource ->
                DataSourceCard(dataSource = dataSource)
            }
        }
        
        // Section des métriques disponibles
        if (availableMetrics.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Métriques disponibles",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DockerBlue
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Métriques détectées dans Prometheus:",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableMetrics) { metric ->
                                AssistChip(
                                    onClick = { },
                                    label = { 
                                        Text(
                                            text = metric,
                                            style = MaterialTheme.typography.bodySmall
                                        ) 
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = DockerBlue.copy(alpha = 0.1f),
                                        labelColor = DockerBlue
                                    )
                                )
                            }
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

@Composable
private fun DashboardCard(
    dashboard: com.example.dockerapp.data.model.GrafanaDashboard
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = dashboard.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
            
            if (dashboard.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dashboard.tags) { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = DockerBlue.copy(alpha = 0.1f),
                                labelColor = DockerBlue
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "UID: ${dashboard.uid}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun DataSourceCard(
    dataSource: com.example.dockerapp.data.model.GrafanaDataSource
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dataSource.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                
                if (dataSource.isDefault) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Défaut") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = DockerBlue.copy(alpha = 0.1f),
                            labelColor = DockerBlue
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Type: ${dataSource.type}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Text(
                text = "URL: ${dataSource.url}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}