package com.example.dockerapp.ui.screen

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dockerapp.data.model.Container
import com.example.dockerapp.ui.theme.DockerBlue
import com.example.dockerapp.ui.theme.DockerDarkBlue
import com.example.dockerapp.ui.theme.LightBackground
import com.example.dockerapp.ui.theme.LightOnError
import com.example.dockerapp.ui.theme.LightOnPrimary
import com.example.dockerapp.ui.theme.LightSurface
import com.example.dockerapp.ui.theme.StatusPaused
import com.example.dockerapp.ui.theme.StatusRunning
import com.example.dockerapp.ui.theme.StatusStopped
import com.example.dockerapp.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val containers by homeViewModel.filteredContainers.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val error by homeViewModel.error.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    val selectedStateFilter by homeViewModel.selectedStateFilter.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.loadContainers()
        homeViewModel.startStatsLoop()
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Docker App", color = DockerBlue) },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Déconnexion")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DockerDarkBlue,
                        titleContentColor = DockerBlue,
                        actionIconContentColor = LightOnPrimary
                    )
                )

                // Barre de recherche
                TextField(
                    value = searchQuery,
                    onValueChange = { homeViewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Rechercher un conteneur...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LightSurface,
                        unfocusedContainerColor = LightSurface,
                        focusedIndicatorColor = DockerBlue,
                        unfocusedIndicatorColor = DockerBlue,
                        cursorColor = DockerBlue
                    )
                )

                // Filtres d'état
                ScrollableTabRow(
                    selectedTabIndex = when(selectedStateFilter) {
                        "running" -> 1
                        "exited" -> 2
                        else -> 0
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    containerColor = LightSurface,
                    contentColor = DockerBlue,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[
                                when(selectedStateFilter) {
                                    "running" -> 1
                                    "exited" -> 2
                                    else -> 0
                                }
                            ]),
                            color = DockerBlue
                        )
                    }
                ) {
                    Tab(
                        selected = selectedStateFilter == null,
                        onClick = { homeViewModel.updateStateFilter(null) }
                    ) {
                        Text("Tous")
                    }
                    Tab(
                        selected = selectedStateFilter == "running",
                        onClick = { homeViewModel.updateStateFilter("running") }
                    ) {
                        Text("En cours")
                    }
                    Tab(
                        selected = selectedStateFilter == "exited",
                        onClick = { homeViewModel.updateStateFilter("exited") }
                    ) {
                        Text("Arrêtés")
                    }
                }
            }
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
                        Button(onClick = { homeViewModel.loadContainers() }) {
                            Text("Réessayer")
                        }
                    }
                }
                containers.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Aucun conteneur trouvé",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { homeViewModel.loadContainers() }) {
                            Text("Actualiser")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(containers) { container ->
                            ContainerCard(container = container)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ContainerCard(
    container: Container,
    homeViewModel: HomeViewModel = viewModel()
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            val displayName = container.names?.firstOrNull()?.removePrefix("/") 
                ?: if (container.id.isNotEmpty()) container.id.take(12) else "Unknown"
            
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Image et description
            Text(
                text = "Image: ${container.image}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
            container.description?.let { desc ->
                Text(
                    text = "Description: $desc",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // État et status
            Row {
                Text(
                    text = "État: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                Text(
                    text = container.state,
                    color = when (container.state.lowercase()) {
                        "running" -> StatusRunning
                        "exited" -> StatusStopped
                        "paused" -> StatusPaused
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            Text(
                text = container.status,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )
            
            // Informations système pour les conteneurs en cours d'exécution
            if (container.state.lowercase() == "running") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CPU: ${String.format("%.2f", container.cpuUsage)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Text(
                        text = "RAM: ${formatSize(container.memoryUsage)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }
            
            // Ports
            container.ports?.takeIf { it.isNotEmpty() }?.let { ports ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ports:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                ports.forEach { port ->
                    Text(
                        text = "${port.publicPort}:${port.privatePort} (${port.type})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            // Boutons d'action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (container.state.lowercase()) {
                    "running" -> {
                        Button(
                            onClick = { homeViewModel.stopContainer(container.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusStopped,
                                contentColor = LightOnError
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Arrêter")
                        }
                        
                        // Afficher Redémarrer seulement si le conteneur est en cours d'exécution
                        Button(
                            onClick = { homeViewModel.restartContainer(container.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DockerBlue,
                                contentColor = LightOnPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Redémarrer")
                        }
                    }
                    "exited" -> {
                        Button(
                            onClick = { homeViewModel.startContainer(container.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusRunning,
                                contentColor = LightOnPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Démarrer")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { 
                        val displayName = container.names?.firstOrNull()?.removePrefix("/") 
                        homeViewModel.navigateToLogs(container.id, displayName)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DockerDarkBlue,
                        contentColor = LightOnPrimary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Logs")
                }
                
                Button(
                    onClick = { 
                        val displayName = container.names?.firstOrNull()?.removePrefix("/") 
                        homeViewModel.navigateToDetails(container.id, displayName)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DockerBlue.copy(alpha = 0.8f),
                        contentColor = LightOnPrimary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Détails")
                }
            }
        }
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

@Preview(showBackground = true)
@Composable
fun ContainerCardPreview() {
    ContainerCard(
        container = Container(
            id = "abc123",
            names = listOf("/test-container"),
            image = "nginx:latest",
            state = "running",
            status = "Up 2 hours"
        )
    )
}

