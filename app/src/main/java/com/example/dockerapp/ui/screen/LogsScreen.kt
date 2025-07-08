package com.example.dockerapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dockerapp.ui.theme.DockerBlue
import com.example.dockerapp.ui.theme.DockerDarkBlue
import com.example.dockerapp.ui.theme.LightBackground
import com.example.dockerapp.ui.theme.LightOnError
import com.example.dockerapp.ui.theme.LightOnPrimary
import com.example.dockerapp.ui.theme.LightSurface
import com.example.dockerapp.ui.theme.StatusPaused
import com.example.dockerapp.ui.theme.StatusRunning
import com.example.dockerapp.ui.theme.StatusStopped
import com.example.dockerapp.ui.viewmodel.LogsViewModel
import com.example.dockerapp.ui.components.RotatingDockerLogo
import com.example.dockerapp.ui.components.DockerLogo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    containerId: String,
    containerName: String = "",
    onBack: () -> Unit,
    logsViewModel: LogsViewModel = viewModel()
) {
    val logs by logsViewModel.logs.collectAsState()
    val isLoading by logsViewModel.isLoading.collectAsState()
    val error by logsViewModel.error.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Charger les logs au démarrage
    LaunchedEffect(containerId) {
        logsViewModel.loadLogs(containerId)
    }

    // Scroll vers le bas quand les logs changent et qu'ils ne sont pas vides
    LaunchedEffect(logs, isLoading) {
        if (logs.isNotEmpty() && !isLoading) {
            coroutineScope.launch {
                listState.scrollToItem(logs.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DockerLogo(size = 28.dp, color = LightOnPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Logs: $containerName",
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
                    IconButton(
                        onClick = { 
                            coroutineScope.launch {
                                // Scroll vers le bas après refresh
                                logsViewModel.refreshLogs(containerId)
                            }
                        },
                        enabled = !isLoading
                    ) {
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DockerDarkBlue
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading && logs.isEmpty() -> {
                    // Affichage du chargement initial
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
                        Text(
                            text = "Chargement des logs...",
                            modifier = Modifier.padding(top = 16.dp),
                            color = DockerBlue
                        )
                    }
                }
                
                error != null -> {
                    // Affichage des erreurs
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
                        Text(
                            text = error ?: "Une erreur s'est produite",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { 
                                logsViewModel.clearError()
                                logsViewModel.loadLogs(containerId) 
                            }
                        ) {
                            Text("Réessayer")
                        }
                    }
                }
                
                logs.isEmpty() -> {
                    // Aucun log disponible
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun log disponible",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                else -> {
                    // Affichage des logs
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        itemsIndexed(
                            items = logs,
                            key = { index, _ -> index }
                        ) { index, logLine ->
                            Text(
                                text = logLine,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 2.dp),
                                color = getLogColor(logLine),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

// Fonction pour colorer les logs selon leur contenu
@Composable
fun getLogColor(logLine: String): Color {
    return when {
        logLine.contains("error", ignoreCase = true) ||
                logLine.contains("fail", ignoreCase = true) ||
                logLine.contains("exception", ignoreCase = true) -> Color.Red

        logLine.contains("warn", ignoreCase = true) -> Color(0xFFFFA500) // Orange

        logLine.contains("info", ignoreCase = true) -> DockerBlue

        else -> MaterialTheme.colorScheme.onSurface
    }
}
