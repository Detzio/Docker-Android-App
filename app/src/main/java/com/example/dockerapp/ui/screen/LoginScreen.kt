package com.example.dockerapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dockerapp.ui.viewmodel.LoginViewModel
import com.example.dockerapp.ui.components.RotatingDockerLogo
import com.example.dockerapp.ui.components.DockerLogo

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    navigateToHome: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val isInitialCheckInProgress by viewModel.isInitialCheckInProgress.collectAsState()

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navigateToHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Afficher l'écran de chargement lors de la vérification initiale
        if (isInitialCheckInProgress) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                RotatingDockerLogo(
                    size = 100.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Vérification des identifiants...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                DockerLogo(size = 64.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Dock Pilot",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Champ pour l'URL du serveur
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { 
                        serverUrl = it
                        viewModel.clearLoginError()
                    },
                    label = { Text("URL du serveur") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    supportingText = { Text("Exemple: https://exemple.sh:2377 (sans /info)") }
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        viewModel.clearLoginError()
                    },
                    label = { Text("Nom d'utilisateur") },
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
                    onValueChange = { 
                        password = it
                        viewModel.clearLoginError()
                    },
                    label = { Text("Mot de passe") },
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
                    onClick = { viewModel.login(username, password, serverUrl) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = username.isNotEmpty() && password.isNotEmpty() && serverUrl.isNotEmpty() && loginState != LoginViewModel.LoginState.Loading
                ) {
                    if (loginState == LoginViewModel.LoginState.Loading) {
                        RotatingDockerLogo(
                            size = 24.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Se connecter")
                    }
                }

                if (loginState is LoginViewModel.LoginState.Error) {
                    Text(
                        text = (loginState as LoginViewModel.LoginState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    
                    if ((loginState as LoginViewModel.LoginState.Error).message.contains("Connexion expirée")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.retryConnection() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Réessayer la connexion automatique")
                        }
                    }
                }
            }
        }
    }
}
