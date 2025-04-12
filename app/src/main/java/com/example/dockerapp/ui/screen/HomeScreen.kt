package com.example.dockerapp.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.dockerapp.ui.viewmodel.HomeViewModel
import com.example.dockerapp.ui.viewmodel.LoginViewModel
import com.example.dockerapp.ui.theme.*
import androidx.compose.material3.TextFieldDefaults
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val containers by homeViewModel.filteredContainers.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val error by homeViewModel.error.collectAsState()
    val searchQuery by homeViewModel.searchQuery.collectAsState()
    val selectedStateFilter by homeViewModel.selectedStateFilter.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "Loading containers...")
        homeViewModel.loadContainers()
    }

    // Actualiser les statistiques toutes les 5 secondes pour les conteneurs en cours d'exécution
    LaunchedEffect(Unit) {
        while (true) {
            try {
                homeViewModel.refreshContainersStats()
            } catch (e: Exception) {
                Log.e("HomeScreen", "Erreur refresh stats", e)
            }
            delay(2000) // Rafraîchissement toutes les 2 secondes
        }
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
                        TabRowDefaults.Indicator(
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

@OptIn(ExperimentalMaterial3Api::class)
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
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
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
                        text = "CPU: ${String.format("%.1f", container.cpuUsage)}%",
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
                            )
                        ) {
                            Text("Arrêter")
                        }
                    }
                    "exited" -> {
                        Button(
                            onClick = { homeViewModel.startContainer(container.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusRunning,
                                contentColor = LightOnPrimary
                            )
                        ) {
                            Text("Démarrer")
                        }
                    }
                }
                Button(
                    onClick = { homeViewModel.restartContainer(container.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DockerBlue,
                        contentColor = LightOnPrimary
                    )
                ) {
                    Text("Redémarrer")
                }
            }
        }
    }
}

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

