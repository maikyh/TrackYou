package com.example.trackingyou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.trackingyou.ui.theme.TrackingYouTheme

// El modelo de Usuario permanece igual
data class User(
    val nombre: String,
    val apellidos: String,
    val estatura: String,
    val peso: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackingYouTheme {
                val users = remember { mutableStateListOf(
                    User("Miguel", "Garza Carranza", "1.75", "70"),
                    User("David", "Cardenas Gonzalez", "1.80", "75"),
                    User("Felipe", "Lara Adame", "1.70", "68")
                )}

                var showAddUserDialog by remember { mutableStateOf(false) }
                var userToEdit by remember { mutableStateOf<User?>(null) }
                var userToDelete by remember { mutableStateOf<User?>(null) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
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
                            onEditUser = { user ->
                                userToEdit = user
                            },
                            onDeleteUser = { userToDelete = it }
                        )

                        // Diálogo para agregar usuario
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

                        // Diálogo para editar usuario
                        userToEdit?.let { user ->
                            AddUserDialog(
                                titleText = "Editar Usuario",
                                initialUser = user,
                                onDismiss = { userToEdit = null },
                                onUserAdded = { editedUser ->
                                    val index = users.indexOfFirst { it == user }
                                    if (index != -1) {
                                        users[index] = editedUser
                                    }
                                    userToEdit = null
                                }
                            )
                        }

                        // Diálogo de confirmación para eliminar
                        userToDelete?.let { user ->
                            AlertDialog(
                                onDismissRequest = { userToDelete = null },
                                title = { Text("Eliminar Usuario") },
                                text = {
                                    Text("¿Estás seguro de que deseas eliminar a ${user.nombre} ${user.apellidos}? Esta acción no se puede deshacer.")
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            users.remove(user)
                                            userToDelete = null
                                        }
                                    ) {
                                        Text("Eliminar", color = Color.Red)
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { userToDelete = null }
                                    ) {
                                        Text("Cancelar")
                                    }
                                }
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
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = peso,
                    onValueChange = { peso = it },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombre.isNotBlank() && apellidos.isNotBlank() &&
                        estatura.isNotBlank() && peso.isNotBlank()) {
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
fun UserCell(
    user: User,
    onEditUser: (User) -> Unit,
    onDeleteUser: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

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

            // Menú desplegable de opciones
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
                    // Opción Editar Usuario
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

                    // Opción Eliminar Usuario
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
    }
}

@Composable
fun UserCellList(
    users: List<User>,
    onEditUser: (User) -> Unit,
    onDeleteUser: (User) -> Unit,
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun NavigationBarPreview() {
    TrackingYouTheme {
        NavigationBar()
    }
}