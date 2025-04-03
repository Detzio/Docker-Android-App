# Docker-App
Le projet annuel a pour objectif de développer une application mobile qui permettra de gérer et de surveiller des conteneurs Docker. Cette application a pour but de faciliter la visualisation des statuts des conteneurs, leur gestion à distance ainsi que leur suivi en temps réel depuis un appareil mobile.

## Fonctionnalités principales :
### MainActivity.kt
- **Rôle**: Point d'entrée principal de l'application Android
- **Fonctions**:
    - `onCreate()`: Configure l'interface utilisateur avec Jetpack Compose et initialise le LoginViewModel

### LoginViewModel.kt
- **Rôle**: Gère toute la logique d'authentification et l'état de connexion
- **Fonctions**:
    - `checkSavedCredentials()`: Vérifie et utilise automatiquement des identifiants sauvegardés
    - `login()`: Tente l'authentification avec les identifiants fournis
    - `logout()`: Déconnecte l'utilisateur et efface les identifiants
    - Utilise des StateFlow pour gérer les états de connexion (Idle, Loading, Success, Error)

### LoginScreen.kt
- **Rôle**: Interface de l'écran de connexion en Jetpack Compose
- **Fonctions**:
    - Affiche les champs de saisie d'identifiants et le bouton de connexion
    - Gère l'affichage des états de chargement et des erreurs
    - Redirige vers HomeScreen en cas d'authentification réussie

### HomeScreen.kt
- **Rôle**: Interface de l'écran d'accueil après connexion
- **Fonctions**:
    - Affiche une barre supérieure avec bouton de déconnexion
    - Affiche un message de bienvenue
    - Permet la déconnexion via un IconButton

### AppNavigation.kt
- **Rôle**: Gestion de la navigation entre écrans
- **Fonctions**:
    - Définit les routes (login, home)
    - Gère la navigation conditionnelle selon l'état d'authentification
    - Gère les transitions lors de la connexion/déconnexion

### AuthRepository.kt
- **Rôle**: Couche intermédiaire entre l'API et la base de données locale
- **Fonctions**:
    - `login()`: Authentifie via l'API et sauvegarde les identifiants si succès
    - `saveCredentials()`: Persiste les identifiants dans la base de données locale
    - `logout()`: Supprime les identifiants et réinitialise RetrofitClient

### UserCredentials.kt
- **Rôle**: Entité Room représentant les identifiants utilisateur
- **Fonctions**:
    - Définit la structure des données d'authentification

### UserCredentialsDao.kt
- **Rôle**: Interface d'accès à la base de données pour les identifiants
- **Fonctions**:
    - CRUD pour les identifiants utilisateur
    - Méthodes spécifiques comme `getActiveCredentials()` et `deactivateAllCredentials()`

### AppDatabase.kt
- **Rôle**: Configuration de la base de données Room
- **Fonctions**:
    - Implémente un singleton pour accéder à la base de données
    - Fournit l'accès aux DAOs (Data Access Object (Objet d'Accès aux Données))

### RetrofitClient.kt
- **Rôle**: Client HTTP pour communiquer avec l'API Docker
- **Fonctions**:
    - Configure Retrofit avec authentification Basic
    - Gère l'interception et l'ajout d'en-têtes d'autorisation
    - Fournit l'instance configurée d'ApiService

### ApiService.kt
- **Rôle**: Interface des endpoints de l'API
- **Fonctions**:
    - `getInfo()`: Point d'entrée pour vérifier l'authentification

### Color.kt
- **Rôle**: Définit les couleurs de l'application
- **Fonctions**:
    - Définit les couleurs primaires, secondaires et d'arrière-plan

### Type.kt
- **Rôle**: Définit les styles de typographie de l'application
- **Fonctions**:
    - Définit les styles de texte pour les titres, le corps, etc.

### Theme.kt
- **Rôle**: Définit le thème de l'application
- **Fonctions**:
    - Applique les couleurs et les styles de typographie

*Cette application implémente l'architecture MVVM, utilisant Room pour le stockage local, Retrofit pour les appels API, et Jetpack Compose pour l'interface utilisateur.*