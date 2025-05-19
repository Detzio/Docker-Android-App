package com.example.dockerapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.DisposableEffect
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
import com.example.dockerapp.ui.theme.LightOnPrimary
import com.example.dockerapp.ui.viewmodel.LogsViewModel
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
    
    // Initialiser le streaming des logs
    LaunchedEffect(containerId) {
        logsViewModel.startLogsStreaming(containerId)
    }
    
    // Assure-toi que le streaming est arrêté quand l'écran est quitté
    DisposableEffect(Unit) {
        onDispose {
            logsViewModel.stopLogsStreaming()
        }
    }
    
    // Défiler automatiquement vers le bas à chaque nouveau log
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(logs.size - 1)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Logs: $containerName",
                        color = LightOnPrimary
                    )
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
            if (logs.isEmpty() && isLoading) {
                // Affichage du chargement initial
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = DockerBlue)
                    Text(
                        text = "Chargement des logs...",
                        modifier = Modifier.padding(top = 16.dp),
                        color = DockerBlue
                    )
                }
            } else if (error != null) {
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
                        onClick = { logsViewModel.startLogsStreaming(containerId) }
                    ) {
                        Text("Réessayer")
                    }
                }
            } else {
                // Affichage des logs
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (logs.isEmpty()) {
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
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            state = listState
                        ) {
                            items(logs) { logLine ->
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
                
                // Indicateur de chargement pendant le streaming
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        color = DockerBlue,
                        strokeWidth = 2.dp
                    )
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