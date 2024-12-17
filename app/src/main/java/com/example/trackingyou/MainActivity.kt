package com.example.trackingyou

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.trackingyou.ui.theme.TrackingYouTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class Record(
    val fecha: String = "",
    val glucosa: String = "",
    val presion: String = ""
)

data class User(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val estatura: String = "",
    val peso: String = "",
    val registros: MutableList<Record> = mutableListOf()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        setContent {
            TrackingYouTheme {
                val onboardingViewModel: OnboardingViewModel = viewModel()
                AppNavigation(onboardingViewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(onboardingViewModel: OnboardingViewModel) {
    val navController = rememberNavController()

    val isOnboardingShown by onboardingViewModel.isOnboardingShown.collectAsState()

    val startDestination = if (!isOnboardingShown) "onboarding" else "splash"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("onboarding") {
            OnboardingScreen(navController, onboardingViewModel)
        }

        composable("home") {
            HomeScreen(
                onNavigateToUserList = {
                    navController.navigate("userList") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("userList") {
            UserListScreen(
                onUserClick = { user ->
                    Log.d("Navigation", "Navigating to userDetail with id: ${user.id}")
                    navController.navigate("userDetail/${user.id}")
                }
            )
        }

        composable(
            "userDetail/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            val userListViewModel: UserListViewModel = viewModel()
            val users by userListViewModel.users.collectAsState()

            val user = users.find { it.id == userId }
            if (user != null) {
                UserDetailScreen(
                    user = user,
                    onAddRecord = { newRecord ->
                        userListViewModel.addRecord(user, newRecord)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Usuario no encontrado", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavigationBar() {
    TopAppBar(
        title = {
            Text(
                text = "TrackYou",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    )
}

@Composable
fun UserListScreen(
    onUserClick: (User) -> Unit
) {
    val users = remember { mutableStateListOf<User>() } // State to hold users
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var searchText by remember { mutableStateOf("") } // Estado para la búsqueda

    LaunchedEffect(Unit) {
        FirebaseService.fetchUsersFirestoreRealtime(
            onSuccess = { fetchedUsers ->
                val sortedUsers = fetchedUsers.sortedBy { it.nombre.lowercase(Locale.getDefault()) }
                users.clear()
                users.addAll(sortedUsers)
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = error.message
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopNavigationBar()
                SearchBar(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddUserDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Agregar Usuario",
                    tint = Color.White
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val filteredUsers = users
                .filter {
                    it.nombre.contains(searchText, ignoreCase = true) ||
                            it.apellidos.contains(searchText, ignoreCase = true)
                }
                .sortedBy { it.nombre.lowercase(Locale.getDefault()) }

            UserCellList(
                users = filteredUsers,
                onEditUser = { user -> userToEdit = user },
                onDeleteUser = { user -> userToDelete = user },
                onUserClick = onUserClick
            )

            // Show Add User Dialog
            if (showAddUserDialog) {
                AddUserDialog(
                    titleText = "Agregar Usuario",
                    onDismiss = { showAddUserDialog = false },
                    onUserAdded = { newUser ->
                        FirebaseService.addUserFirestore(
                            user = newUser,
                            onSuccess = {
                                showAddUserDialog = false
                            },
                            onFailure = { error ->
                                errorMessage = error.message
                                showAddUserDialog = false
                            }
                        )
                    }
                )

            }

            userToEdit?.let { user ->
                AddUserDialog(
                    titleText = "Editar Usuario",
                    initialUser = user,
                    onDismiss = { userToEdit = null },
                    onUserAdded = { editedUser ->
                        val index = users.indexOf(user)
                        if (index != -1) {
                            users[index] = editedUser
                        }
                        userToEdit = null
                    }
                )
            }

            userToDelete?.let { user ->
                ConfirmDeleteDialog(
                    user = user,
                    onConfirm = {
                        users.remove(user)
                        userToDelete = null
                    },
                    onDismiss = { userToDelete = null }
                )
            }
        }
    }
}

@Composable
fun UserCellList(
    users: List<User>,
    onEditUser: (User) -> Unit,
    onDeleteUser: (User) -> Unit,
    onUserClick: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(users) { user ->
            UserCard(
                user = user,
                onEditUser = onEditUser,
                onDeleteUser = onDeleteUser,
                onUserClick = onUserClick,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .animateContentSize()
            )
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onEditUser: (User) -> Unit,
    onDeleteUser: (User) -> Unit,
    onUserClick: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddRecordDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUserClick(user) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Avatar(user = user)

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${user.nombre} ${user.apellidos}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Estatura: ${user.estatura} m | Peso: ${user.peso} kg",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Mis opciones",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar Usuario") },
                        onClick = {
                            onEditUser(user)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Agregar Registro") },
                        onClick = {
                            showAddRecordDialog = true
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Agregar Registro",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Eliminar Usuario") },
                        onClick = {
                            onDeleteUser(user)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }

        if (showAddRecordDialog) {
            AddRecordDialog(
                user = user,
                onDismiss = { showAddRecordDialog = false },
                onRecordAdded = { newRecord ->
                    FirebaseService.addRecordToUser(
                        userId = user.id,
                        record = newRecord,
                        onSuccess = {
                            showAddRecordDialog = false
                        },
                        onFailure = {}
                    )
                }
            )
        }
    }
}


@Composable
fun Avatar(user: User) {
    val initials = "${user.nombre.firstOrNull()?.uppercaseChar() ?: 'U'}${user.apellidos.firstOrNull()?.uppercaseChar() ?: 'X'}"
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            text = initials,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
fun AddUserDialog(
    titleText: String,
    initialUser: User? = null,
    onDismiss: () -> Unit,
    onUserAdded: (User) -> Unit
) {
    var nombre by remember { mutableStateOf(initialUser?.nombre ?: "") }
    var apellidos by remember { mutableStateOf(initialUser?.apellidos ?: "") }
    var estatura by remember { mutableStateOf(initialUser?.estatura ?: "") }
    var peso by remember { mutableStateOf(initialUser?.peso ?: "") }

    var nombreError by remember { mutableStateOf(false) }
    var apellidosError by remember { mutableStateOf(false) }
    var estaturaError by remember { mutableStateOf(false) }
    var pesoError by remember { mutableStateOf(false) }

    // Función de validación
    fun validateInputs(): Boolean {
        val isNombreValid = isValidName(nombre)
        val isApellidosValid = isValidName(apellidos)
        val isEstaturaValid = estatura.toDoubleOrNull() != null && estatura.isNotBlank()
        val isPesoValid = peso.toDoubleOrNull() != null && peso.isNotBlank()

        nombreError = nombre.isBlank() || !isNombreValid
        apellidosError = apellidos.isBlank() || !isApellidosValid
        estaturaError = !isEstaturaValid
        pesoError = !isPesoValid

        return isNombreValid && isApellidosValid && isEstaturaValid && isPesoValid
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = titleText,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        nombreError = it.isBlank() || !isValidName(it)
                    },
                    label = { Text("Nombre") },
                    singleLine = true,
                    isError = nombreError,
                    modifier = Modifier.fillMaxWidth(),

                )
                if (nombreError) {
                    Text(
                        text = if (nombre.isBlank()) "El nombre no puede estar vacío" else "El nombre no puede contener números ni caracteres especiales",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                OutlinedTextField(
                    value = apellidos,
                    onValueChange = {
                        apellidos = it
                        apellidosError = it.isBlank() || !isValidName(it)
                    },
                    label = { Text("Apellidos") },
                    singleLine = true,
                    isError = apellidosError,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (apellidosError) {
                    Text(
                        text = if (apellidos.isBlank()) "Los apellidos no pueden estar vacíos" else "Los apellidos no pueden contener números ni caracteres especiales",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                OutlinedTextField(
                    value = estatura,
                    onValueChange = {
                        estatura = it
                        estaturaError = it.toDoubleOrNull() == null || it.isBlank()
                    },
                    label = { Text("Estatura (m)") },
                    singleLine = true,
                    isError = estaturaError,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (estaturaError) {
                    Text(
                        text = "Ingrese una estatura válida",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                OutlinedTextField(
                    value = peso,
                    onValueChange = {
                        peso = it
                        pesoError = it.toDoubleOrNull() == null || it.isBlank()
                    },
                    label = { Text("Peso (kg)") },
                    singleLine = true,
                    isError = pesoError,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (pesoError) {
                    Text(
                        text = "Ingrese un peso válido",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validateInputs()) {
                        val newUser = initialUser?.copy(
                            nombre = nombre,
                            apellidos = apellidos,
                            estatura = estatura,
                            peso = peso
                        ) ?: User(
                            id = UUID.randomUUID().toString(), // Asegura un ID único
                            nombre = nombre,
                            apellidos = apellidos,
                            estatura = estatura,
                            peso = peso
                        )

                        if (initialUser != null) {
                            FirebaseService.updateUserFirestore(
                                user = newUser,
                                onSuccess = { onDismiss() },
                                onFailure = { /* Manejar el error si es necesario */ }
                            )
                        } else {
                            FirebaseService.addUserFirestore(
                                user = newUser,
                                onSuccess = { onUserAdded(newUser) },
                                onFailure = { /* Manejar el error si es necesario */ }
                            )
                        }
                        onDismiss() // Cierra el diálogo
                    }
                },
                enabled = !nombreError && !apellidosError && !estaturaError && !pesoError
            ) {
                Text(if (initialUser != null) "Guardar" else "Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

fun isValidName(input: String): Boolean {
    return input.matches(Regex("^[A-Za-zÀ-ÿ '-]+$"))
}

@Composable
fun ConfirmDeleteDialog(
    user: User,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Eliminar Usuario",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                "¿Estás seguro de que deseas eliminar a ${user.nombre} ${user.apellidos}? Esta acción no se puede deshacer.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    FirebaseService.deleteUser(
                        userId = user.id,
                        onComplete = { success ->
                            if (success) {
                                onConfirm()
                            }
                            onDismiss()
                        }
                    )
                    onDismiss()
                }
            ) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun AddRecordDialog(
    user: User,
    onDismiss: () -> Unit,
    onRecordAdded: (Record) -> Unit
) {
    var glucosa by remember { mutableStateOf("") }
    var presion by remember { mutableStateOf("") }

    var glucosaError by remember { mutableStateOf(false) }
    var presionError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Agregar Registro para ${user.nombre}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = glucosa,
                    onValueChange = {
                        glucosa = it
                        glucosaError = it.toIntOrNull() == null || it.isBlank()
                    },
                    label = { Text("Glucosa (mg/dL)") },
                    singleLine = true,
                    isError = glucosaError,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (glucosaError) {
                    Text(
                        text = "Ingrese un valor de glucosa válido",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                OutlinedTextField(
                    value = presion,
                    onValueChange = {
                        presion = it
                        presionError = it.toIntOrNull() == null || it.isBlank()
                    },
                    label = { Text("Presión Arterial (mmHg)") },
                    singleLine = true,
                    isError = presionError,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (presionError) {
                    Text(
                        text = "Ingrese un valor de presión válido",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val isValid = glucosa.toIntOrNull() != null && presion.toIntOrNull() != null
                    if (isValid) {
                        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                        val newRecord = Record(fecha, glucosa, presion)
                        onRecordAdded(newRecord)
                    }
                }
            ) {
                Text("Agregar", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    user: User,
    onAddRecord: (Record) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showAddRecordDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) } // Para manejar errores

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("${user.nombre} ${user.apellidos}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddRecordDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Agregar Registro")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Registros de Salud",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (user.registros.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay registros disponibles.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                TableHeader()
                val sortedRecords = remember(user.registros) {
                    user.registros.sortedBy { it.fecha }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedRecords) { record ->
                        TableRow(record)
                    }
                }
            }
        }

        if (showAddRecordDialog) {
            AddRecordDialog(
                user = user,
                onDismiss = { showAddRecordDialog = false },
                onRecordAdded = { newRecord ->
                    FirebaseService.addRecordToUser(
                        userId = user.id,
                        record = newRecord,
                        onSuccess = {
                            onAddRecord(newRecord) // Opcional: si deseas realizar acciones adicionales
                            showAddRecordDialog = false
                        },
                        onFailure = { error ->
                            errorMessage = error.message
                            showAddRecordDialog = false
                        }
                    )
                }
            )
        }

        errorMessage?.let { msg ->
            Snackbar(
                action = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text("Cerrar")
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = msg)
            }
        }
    }
}


@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(8.dp)
    ) {
        Text(
            text = "Fecha",
            modifier = Modifier.weight(2f),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "Glucosa",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "Presion",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun TableRow(record: Record) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(
            text = record.fecha,
            modifier = Modifier.weight(2f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = record.glucosa,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = record.presion,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TopNavigationBarPreview() {
    TrackingYouTheme {
        TopNavigationBar()
    }
}

@Preview(showBackground = true)
@Composable
fun UserCardPreview() {
    TrackingYouTheme {
        UserCard(
            user = User("Miguel", "Garza Carranza", "1.75", "70"),
            onEditUser = {},
            onDeleteUser = {},
            onUserClick = {}
        )
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    val imageModifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center)

    LaunchedEffect(Unit) {

        delay(3000)
        onTimeout()
    }

    // Contenedor para la imagen
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = null,
            modifier = imageModifier
        )
    }
}

@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        label = { Text("Buscar usuario") },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Buscar"
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}

//onboarding
@Composable
fun OnboardingScreen(navController: NavHostController) {
    val pages = listOf(
        OnboardingPage("Bienvenido", "¡Descubre las mejores funciones de nuestra app!", R.drawable.icon1),
        OnboardingPage("Monitorea tu salud", "Registra tus datos y realiza un seguimiento de tu progreso.", R.drawable.icon2),
        OnboardingPage("Mantente saludable", "Recibe recomendaciones personalizadas para un estilo de vida saludable.", R.drawable.icon3)
    )

    var currentPage by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Fondo degradado
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Imagen del Onboarding
        Image(
            painter = painterResource(id = pages[currentPage].image),
            contentDescription = "Onboarding Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Título
        Text(
            text = pages[currentPage].title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Descripción
        Text(
            text = pages[currentPage].description,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF486FC7),
            modifier = Modifier
                .padding(horizontal = 32.dp)

        )

        Spacer(modifier = Modifier.height(32.dp))

        // Barra de progreso
        Row(horizontalArrangement = Arrangement.Center) {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (index == currentPage) Color.Blue else Color.Gray,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botones de navegación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentPage > 0) {
                Button(
                    onClick = { currentPage-- },
                    colors = ButtonDefaults.buttonColors(containerColor  = Color(0xFF486FC7)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Atrás", color = Color.White)
                }
            }

            if (currentPage < pages.size - 1) {
                Button(
                    onClick = { currentPage++ },
                    colors = ButtonDefaults.buttonColors(containerColor  = Color(0xFF0838A8)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Siguiente", color = Color.White)
                }
            } else {
                Button(
                    onClick = {

                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor  = Color(0xFF4CAF50)),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Comenzar", color = Color.White)
                }
            }
        }
    }
}

data class OnboardingPage(val title: String, val description: String, val image: Int)
@Composable
fun HomeScreen(onNavigateToUserList: () -> Unit) {

    val primaryColor = Color(0xFF6200EE)
    val secondaryColor = Color(0xFF03DAC5)
    val backgroundColor = Color(0xFFFFFFFF)
    val textColor = Color(0xFF000000)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {

            Image(
                painter = painterResource(id = R.drawable.icon4),
                contentDescription = "Imagen de bienvenida",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))


            Text(
                text = "¡Bienvenido a la App!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(modifier = Modifier.height(16.dp))


            Button(
                onClick = onNavigateToUserList,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor  = primaryColor)
            ) {
                Text(
                    text = "Ver lista de usuarios",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
