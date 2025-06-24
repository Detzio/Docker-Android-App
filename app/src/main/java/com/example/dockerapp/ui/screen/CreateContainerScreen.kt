package com.example.dockerapp.ui.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dockerapp.data.model.DockerImage
import com.example.dockerapp.data.model.DockerVolume
import com.example.dockerapp.ui.theme.DockerBlue
import com.example.dockerapp.ui.theme.DockerDarkBlue
import com.example.dockerapp.ui.theme.LightBackground
import com.example.dockerapp.ui.theme.LightOnPrimary
import com.example.dockerapp.ui.theme.StatusRunning
import com.example.dockerapp.ui.viewmodel.CreateContainerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateContainerScreen(
    onBack: () -> Unit,
    onContainerCreated: () -> Unit,
    viewModel: CreateContainerViewModel = viewModel()
) {
    val images by viewModel.images.collectAsState()
    val volumes by viewModel.volumes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedImage by viewModel.selectedImage.collectAsState()
    val containerName by viewModel.containerName.collectAsState()
    val customImage by viewModel.customImage.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val isPullingImage by viewModel.isPullingImage.collectAsState()
    val creationSuccess by viewModel.creationSuccess.collectAsState()
    val isDeletingImage by viewModel.isDeletingImage.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var isImagesExpanded by remember { mutableStateOf(false) }
    var isVolumesExpanded by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        Log.d("CreateContainerScreen", "Loading data...")
        viewModel.loadData()
    }
    
    LaunchedEffect(error) {
        error?.let {
            Log.d("CreateContainerScreen", "Showing error: $it")
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(creationSuccess) {
        creationSuccess?.let {
            snackbarHostState.showSnackbar("Conteneur créé avec succès!")
            viewModel.clearSuccess()
            onContainerCreated()
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Créer un conteneur", color = DockerBlue) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DockerDarkBlue,
                    titleContentColor = DockerBlue,
                    navigationIconContentColor = LightOnPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LightBackground
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Configuration du conteneur
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Configuration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = containerName,
                                onValueChange = viewModel::updateContainerName,
                                label = { Text("Nom du conteneur (optionnel)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
                
                // Sélection d'image personnalisée
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Image personnalisée",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = customImage,
                                onValueChange = viewModel::updateCustomImage,
                                label = { Text("Nom de l'image (ex: nginx:latest)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("hello-world:latest, ubuntu:20.04, nginx:alpine, etc.") }
                            )
                        }
                    }
                }
                
                // Images disponibles
                if (images.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isImagesExpanded = !isImagesExpanded }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Images installées (${images.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = if (isImagesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isImagesExpanded) "Réduire" else "Étendre"
                                    )
                                }
                                
                                if (isImagesExpanded) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        images.forEach { image ->
                                            ImageCard(
                                                image = image,
                                                isSelected = selectedImage == (image.repoTags?.firstOrNull() ?: image.id),
                                                onSelect = { 
                                                    Log.d("CreateContainerScreen", "Image selected: $it")
                                                    viewModel.updateSelectedImage(it) 
                                                },
                                                onDelete = { imageName ->
                                                    viewModel.deleteImage(imageName)
                                                },
                                                isDeletingImage = isDeletingImage
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Volumes disponibles
                if (volumes.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isVolumesExpanded = !isVolumesExpanded }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Volumes disponibles (${volumes.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = if (isVolumesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isVolumesExpanded) "Réduire" else "Étendre"
                                    )
                                }
                                
                                if (isVolumesExpanded) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        volumes.take(3).forEach { volume ->
                                            VolumeCard(volume = volume)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Bouton de création
                item {
                    Button(
                        onClick = { viewModel.createContainer() },
                        enabled = !isCreating && !isPullingImage && (selectedImage.isNotEmpty() || customImage.isNotEmpty()),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusRunning,
                            contentColor = LightOnPrimary
                        )
                    ) {
                        if (isPullingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = LightOnPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Téléchargement...")
                        } else if (isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = LightOnPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Création...")
                        } else {
                            Text("Créer le conteneur")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImageCard(
    image: DockerImage,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    isDeletingImage: Boolean
) {
    val displayName = image.repoTags?.firstOrNull() ?: image.id.take(12)
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DockerBlue.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect(displayName) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = { onSelect(displayName) }
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Taille: ${formatSize(image.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            IconButton(
                onClick = { showDeleteDialog = true },
                enabled = !isDeletingImage
            ) {
                if (isDeletingImage) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer l'image",
                        tint = Color.Red
                    )
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmer la suppression") },
            text = { 
                Text("Êtes-vous sûr de vouloir supprimer l'image '$displayName' ?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(displayName)
                    }
                ) {
                    Text("Supprimer", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun VolumeCard(volume: DockerVolume) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = volume.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Driver: ${volume.driver}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
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
