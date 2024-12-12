package com.example.trackingyou

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.trackingyou.ui.theme.TrackingYouTheme
import java.text.SimpleDateFormat
import java.util.*

data class Record(
    val fecha: String,
    val glucosa: String,
    val presion: String
)

data class User(
    val nombre: String,
    val apellidos: String,
    val estatura: String,
    val peso: String,
    val registros: MutableList<Record> = mutableListOf()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackingYouTheme {
                val navController = rememberNavController()

                // Lista de usuarios con registros
                val users = remember {
                    mutableStateListOf(
                        User("Miguel", "Garza Carranza", "1.75", "70"),
                        User("David", "Cardenas Gonzalez", "1.80", "75"),
                        User("Felipe", "Lara Adame", "1.70", "68")
                    )
                }

                NavHost(navController = navController, startDestination = "userList") {
                    composable("userList") {
                        UserListScreen(
                            users = users,
                            onUserClick = { user ->
                                navController.navigate("userDetail/${user.nombre}_${user.apellidos}")
                            }
                        )
                    }
                    composable(
                        "userDetail/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")
                        val user = users.find { "${it.nombre}_${it.apellidos}" == userId }
                        user?.let {
                            UserDetailScreen(
                                user = it,
                                onAddRecord = { newRecord ->
                                    it.registros.add(newRecord)
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    users: MutableList<User>,
    onUserClick: (User) -> Unit
) {
    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    Scaffold(
        topBar = { TopNavigationBar() },
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
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
        ) {
            UserCellList(
                users = users,
                onEditUser = { user -> userToEdit = user },
                onDeleteUser = { user -> userToDelete = user },
                onUserClick = onUserClick
            )

            if (showAddUserDialog) {
                AddUserDialog(
                    titleText = "Agregar Nuevo Usuario",
                    onDismiss = { showAddUserDialog = false },
                    onUserAdded = { newUser ->
                        users.add(newUser)
                        showAddUserDialog = false
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
                    user.registros.add(newRecord)
                    showAddRecordDialog = false
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
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    label = { Text("Apellidos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estatura,
                    onValueChange = { estatura = it },
                    label = { Text("Estatura (m)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = peso,
                    onValueChange = { peso = it },
                    label = { Text("Peso (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombre.isNotBlank() && apellidos.isNotBlank() &&
                        estatura.isNotBlank() && peso.isNotBlank()
                    ) {
                        val newUser = User(nombre, apellidos, estatura, peso)
                        onUserAdded(newUser)
                    }
                }
            ) {
                Text(if (initialUser == null) "Agregar" else "Guardar", color = MaterialTheme.colorScheme.primary)
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
                "¿Estas seguro de que deseas eliminar a ${user.nombre} ${user.apellidos}? Esta accion no se puede deshacer.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
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
                    onValueChange = { glucosa = it },
                    label = { Text("Glucosa (mg/dL)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = presion,
                    onValueChange = { presion = it },
                    label = { Text("Presion Arterial (mmHg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (glucosa.isNotBlank() && presion.isNotBlank()) {
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("${user.nombre} ${user.apellidos}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atras")
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(user.registros) { record ->
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
                    onAddRecord(newRecord)
                    showAddRecordDialog = false
                }
            )
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

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun UserListScreenPreview() {
    TrackingYouTheme {
        UserListScreen(
            users = mutableStateListOf(
                User("Miguel", "Garza Carranza", "1.75", "70"),
                User("David", "Cardenas Gonzalez", "1.80", "75"),
                User("Felipe", "Lara Adame", "1.70", "68")
            ),
            onUserClick = {}
        )
    }
}