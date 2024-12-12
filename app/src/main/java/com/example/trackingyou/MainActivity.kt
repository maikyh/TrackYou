package com.example.trackingyou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@Composable
fun NavigationBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF0066B2))
            .padding(16.dp)
    ) {
        Text(
            text = "TrackYou",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun UserListScreen(
    users: MutableList<User>,
    onUserClick: (User) -> Unit
) {
    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    Scaffold(
        topBar = { NavigationBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddUserDialog = true },
                containerColor = Color(0xFF0066B2)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Agregar Usuario",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
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
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(users) { user ->
            UserCell(
                user = user,
                onEditUser = onEditUser,
                onDeleteUser = onDeleteUser,
                onUserClick = onUserClick,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun UserCell(
    user: User,
    onEditUser: (User) -> Unit,
    onDeleteUser: (User) -> Unit,
    onUserClick: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddRecordDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color(0xFFDBEAFF))
            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onUserClick(user) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "User Avatar",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "${user.nombre} ${user.apellidos}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Estatura: ${user.estatura} m, Peso: ${user.peso} kg",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More options",
                        tint = Color.Gray
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
                                contentDescription = "Editar"
                            )
                        }
                    )

                    // Opción Agregar Registro
                    DropdownMenuItem(
                        text = { Text("Agregar Registro") },
                        onClick = {
                            showAddRecordDialog = true
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Agregar Registro"
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
                                contentDescription = "Eliminar"
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
        title = { Text(titleText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    label = { Text("Apellidos") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = estatura,
                    onValueChange = { estatura = it },
                    label = { Text("Estatura (m)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = peso,
                    onValueChange = { peso = it },
                    label = { Text("Peso (kg)") },
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
                Text(if (initialUser == null) "Agregar" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
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
        title = { Text("Eliminar Usuario") },
        text = {
            Text("¿Estás seguro de que deseas eliminar a ${user.nombre} ${user.apellidos}? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Eliminar", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
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
        title = { Text("Agregar Registro para ${user.nombre}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = glucosa,
                    onValueChange = { glucosa = it },
                    label = { Text("Glucosa (mg/dL)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = presion,
                    onValueChange = { presion = it },
                    label = { Text("Presión Arterial (mmHg)") },
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
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
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
            TopAppBar(
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
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Registros de Salud",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (user.registros.isEmpty()) {
                Text("No hay registros disponibles.", color = Color.Gray)
            } else {
                TableHeader()
                LazyColumn {
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
            .background(Color(0xFFE0E0E0))
            .padding(8.dp)
    ) {
        Text(
            text = "Fecha",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Glucosa",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Presión",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TableRow(record: Record) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = record.fecha,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = record.glucosa,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = record.presion,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationBarPreview() {
    TrackingYouTheme {
        NavigationBar()
    }
}
