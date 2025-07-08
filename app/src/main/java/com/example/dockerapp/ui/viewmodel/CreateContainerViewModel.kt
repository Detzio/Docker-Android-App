package com.example.dockerapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dockerapp.data.api.RetrofitClient
import com.example.dockerapp.data.model.ContainerCreateRequest
import com.example.dockerapp.data.model.DockerImage
import com.example.dockerapp.data.model.DockerVolume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import okio.Buffer

class CreateContainerViewModel : ViewModel() {
    
    private val TAG = "CreateContainerViewModel"
    
    private val _images = MutableStateFlow<List<DockerImage>>(emptyList())
    val images: StateFlow<List<DockerImage>> = _images
    
    private val _volumes = MutableStateFlow<List<DockerVolume>>(emptyList())
    val volumes: StateFlow<List<DockerVolume>> = _volumes
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _selectedImage = MutableStateFlow<String>("")
    val selectedImage: StateFlow<String> = _selectedImage
    
    private val _containerName = MutableStateFlow<String>("")
    val containerName: StateFlow<String> = _containerName
    
    private val _customImage = MutableStateFlow<String>("")
    val customImage: StateFlow<String> = _customImage
    
    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating
    
    private val _isPullingImage = MutableStateFlow(false)
    val isPullingImage: StateFlow<Boolean> = _isPullingImage
    
    private val _isDeletingImage = MutableStateFlow(false)
    val isDeletingImage: StateFlow<Boolean> = _isDeletingImage
    
    private val _creationSuccess = MutableStateFlow<String?>(null)
    val creationSuccess: StateFlow<String?> = _creationSuccess
    
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Charger les images
                try {
                    val imagesResponse = RetrofitClient.apiService.getImages()
                    if (imagesResponse.isSuccessful) {
                        _images.value = imagesResponse.body() ?: emptyList()
                        Log.d(TAG, "Images loaded: ${_images.value.size}")
                    } else {
                        Log.e(TAG, "Error loading images: ${imagesResponse.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading images", e)
                }
                
                // Charger les volumes
                try {
                    val volumesResponse = RetrofitClient.apiService.getVolumes()
                    if (volumesResponse.isSuccessful) {
                        _volumes.value = volumesResponse.body()?.volumes ?: emptyList()
                        Log.d(TAG, "Volumes loaded: ${_volumes.value.size}")
                    } else {
                        Log.e(TAG, "Error loading volumes: ${volumesResponse.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading volumes", e)
                }
                
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateSelectedImage(image: String) {
        _selectedImage.value = image
        if (image.isNotEmpty()) {
            _customImage.value = ""
        }
    }
    
    fun updateCustomImage(image: String) {
        _customImage.value = image
        if (image.isNotEmpty()) {
            _selectedImage.value = ""
        }
    }
    
    fun updateContainerName(name: String) {
        _containerName.value = name
    }
    
    fun deleteImage(imageName: String) {
        viewModelScope.launch {
            _isDeletingImage.value = true
            _error.value = null
            
            try {
                Log.d(TAG, "Starting delete process for image: $imageName")
                
                val response = RetrofitClient.apiService.deleteImage(
                    imageName = imageName,
                    force = true
                )
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Image deleted successfully: $imageName")
                    loadImages() // Recharger la liste des images
                } else {
                    val errorMessage = when (response.code()) {
                        404 -> "Image non trouvée"
                        409 -> "Impossible de supprimer l'image : elle est utilisée par un conteneur"
                        else -> "Erreur lors de la suppression (code: ${response.code()})"
                    }
                    _error.value = errorMessage
                    Log.e(TAG, "Error deleting image: ${response.code()}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Exception while deleting image", e)
                _error.value = "Erreur lors de la suppression de l'image: ${e.message}"
            } finally {
                _isDeletingImage.value = false
            }
        }
    }
    
    private suspend fun loadImages() {
        try {
            val imagesResponse = RetrofitClient.apiService.getImages()
            if (imagesResponse.isSuccessful) {
                _images.value = imagesResponse.body() ?: emptyList()
                Log.d(TAG, "Images reloaded: ${_images.value.size}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reloading images", e)
        }
    }
    
    fun createContainer() {
        val imageToUse = if (_selectedImage.value.isNotEmpty()) {
            _selectedImage.value
        } else if (_customImage.value.isNotEmpty()) {
            _customImage.value
        } else {
            _error.value = "Veuillez sélectionner ou saisir une image"
            return
        }
        
        viewModelScope.launch {
            _isCreating.value = true
            _error.value = null
            
            try {
                Log.d(TAG, "Creating container with image: $imageToUse")
                
                val success = createContainerWithImage(imageToUse)
                
                if (!success) {
                    Log.d(TAG, "Image not found, attempting to pull: $imageToUse")
                    _isPullingImage.value = true
                
                    kotlinx.coroutines.delay(1000)
                    
                    val pullSuccess = pullImageIfNeeded(imageToUse)
                    _isPullingImage.value = false
                    
                    if (pullSuccess) {
                        kotlinx.coroutines.delay(2000)
                        createContainerWithImage(imageToUse)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error creating container", e)
                _error.value = when (e) {
                    is java.net.SocketTimeoutException -> "Timeout lors de la création. Vérifiez votre connexion Docker."
                    is java.lang.IllegalStateException -> "Connexion interrompue. Le serveur Docker semble inaccessible."
                    else -> "Erreur: ${e.message}"
                }
            } finally {
                _isCreating.value = false
                _isPullingImage.value = false
            }
        }
    }
    
    private suspend fun createContainerWithImage(imageToUse: String): Boolean {
        return try {
            val request = ContainerCreateRequest(
                image = imageToUse,
                name = _containerName.value.takeIf { it.isNotBlank() }
            )
            
            val response = RetrofitClient.apiService.createContainer(
                body = request,
                name = _containerName.value.takeIf { it.isNotBlank() }
            )
            
            if (response.isSuccessful) {
                val result = response.body()
                _creationSuccess.value = result?.id ?: "success"
                Log.d(TAG, "Container created successfully: ${result?.id}")
                true
            } else {
                when (response.code()) {
                    404 -> false
                    409 -> {
                        _error.value = "Un conteneur avec ce nom existe déjà."
                        true
                    }
                    else -> {
                        _error.value = "Erreur lors de la création du conteneur (code: ${response.code()})"
                        true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in createContainerWithImage", e)
            when (e) {
                is java.net.SocketTimeoutException -> {
                    _error.value = "Timeout lors de la création du conteneur. Vérifiez votre connexion Docker."
                    false
                }
                is java.lang.IllegalStateException -> {
                    _error.value = "Connexion interrompue. Le serveur Docker semble avoir un problème."
                    false
                }
                else -> {
                    _error.value = "Erreur de connexion: ${e.message}"
                    false
                }
            }
        }
    }
    
    private suspend fun pullImageIfNeeded(imageToUse: String): Boolean {
        return try {
            val (imageName, tag) = if (imageToUse.contains(":")) {
                val parts = imageToUse.split(":")
                parts[0] to parts[1]
            } else {
                imageToUse to "latest"
            }
            
            val response = RetrofitClient.apiService.pullImage(
                fromImage = imageName,
                tag = tag
            )
            
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    try {
                        responseBody.use { body ->
                            val source = body.source()
                            val buffer = Buffer()
                            while (!source.exhausted()) {
                                source.read(buffer, 8192)
                                buffer.clear()
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error reading pull response stream, but pull might have succeeded", e)
                    }
                }
                Log.d(TAG, "Image pull completed")
                loadImages()
                true
            } else {
                Log.e(TAG, "Image pull failed with code: ${response.code()}")
                _error.value = when (response.code()) {
                    404 -> "L'image '$imageToUse' n'existe pas sur Docker Hub."
                    else -> "Impossible de télécharger l'image '$imageToUse' (code: ${response.code()})"
                }
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during image pull", e)
            _error.value = when (e) {
                is java.net.SocketTimeoutException -> "Timeout lors du téléchargement de l'image. Vérifiez votre connexion."
                is java.lang.IllegalStateException -> "Téléchargement interrompu. Veuillez réessayer."
                else -> "Erreur lors du téléchargement de l'image: ${e.message}"
            }
            false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearSuccess() {
        _creationSuccess.value = null
    }
}

