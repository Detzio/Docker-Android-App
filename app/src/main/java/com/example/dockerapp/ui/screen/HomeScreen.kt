package com.example.dockerapp.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dockerapp.data.model.Container
import com.example.dockerapp.ui.viewmodel.HomeViewModel
import com.example.dockerapp.ui.viewmodel.LoginViewModel

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

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Docker App") },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Déconnexion")
                        }
                    }
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
                    singleLine = true
                )
                
                // Filtres d'état
                ScrollableTabRow(
                    selectedTabIndex = when(selectedStateFilter) {
                        "running" -> 1
                        "exited" -> 2
                        else -> 0
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
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
        }
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
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Utilisation plus sûre de l'ID et du nom
            val displayName = container.names?.firstOrNull()?.removePrefix("/") 
                ?: if (container.id.isNotEmpty()) container.id.take(12) else "Unknown"
            
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Image: ${container.image}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = "État: ",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = container.state,
                    color = when (container.state.lowercase()) {
                        "running" -> MaterialTheme.colorScheme.primary
                        "exited" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            Text(
                text = container.status,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (container.state.lowercase()) {
                    "running" -> {
                        Button(
                            onClick = { homeViewModel.stopContainer(container.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Arrêter")
                        }
                    }
                    "exited" -> {
                        Button(
                            onClick = { homeViewModel.startContainer(container.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Démarrer")
                        }
                    }
                }
                Button(
                    onClick = { homeViewModel.restartContainer(container.id) }
                ) {
                    Text("Redémarrer")
                }
            }
        }
    }
}
