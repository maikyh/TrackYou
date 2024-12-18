package com.example.android_final.views

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.android_final.components.PersonaButton
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)//necesario para la fecha
@Composable
fun MarcadorView(navController: NavController) {
    var puntosEquipoA by remember { mutableStateOf(0) }
    var puntosEquipoB by remember { mutableStateOf(0) }
    var setsEquipoA by remember { mutableStateOf(0) }
    var setsEquipoB by remember { mutableStateOf(0) }
    var colorEquipo1 by remember { mutableStateOf(Color(0xFF00FF93)) }
    var colorEquipo2 by remember { mutableStateOf(Color(0xFF00FF93)) }

    val fechaActual = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Marcador de voleibol",
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = fechaActual,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            EquipoMarcador(
                equipo = "Equipo A",
                puntos = puntosEquipoA,
                sets = setsEquipoA,
                onPuntosChange = { delta ->
                    val nuevosPuntos = (puntosEquipoA + delta).coerceAtLeast(0)
                    if (nuevosPuntos > 24) {
                        setsEquipoA += 1
                        puntosEquipoA = 0
                        puntosEquipoB = 0
                    } else {
                        puntosEquipoA = nuevosPuntos
                    }
                },
                onColorChange = { colorEquipo1 = it },
                colorEquipo = colorEquipo1
            )
            EquipoMarcador(
                equipo = "Equipo B",
                puntos = puntosEquipoB,
                sets = setsEquipoB,
                onPuntosChange = { delta ->
                    val nuevosPuntos = (puntosEquipoB + delta).coerceAtLeast(0)
                    if (nuevosPuntos > 24) {
                        setsEquipoB += 1
                        puntosEquipoA = 0
                        puntosEquipoB = 0
                    } else {
                        puntosEquipoB = nuevosPuntos
                    }
                },
                onColorChange = { colorEquipo2 = it },
                colorEquipo = colorEquipo2
            )

            PersonaButton(
                text = "Guardar marcador",
                onClick = {
                    //implementar logica para guardar el marcador
                }
            )

            PersonaButton(
                text = "Reiniciar marcador",
                onClick = {
                    puntosEquipoA = 0
                    puntosEquipoB = 0
                    setsEquipoA = 0
                    setsEquipoB = 0
                }
            )
        }
    }
}

@Composable
fun EquipoMarcador(
    equipo: String,
    puntos: Int,
    sets: Int,
    onPuntosChange: (Int) -> Unit,
    onColorChange: (Color) -> Unit,
    colorEquipo: Color
) {
    var mostrarColorPicker by remember { mutableStateOf(false) }
    val colorPickerController = rememberColorPickerController()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colorEquipo)
    ) {
        Text(
            text = equipo.uppercase(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Black,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Puntos: $puntos",
            fontSize = 22.sp,
            color = Color.Black,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Sets: $sets",
            fontSize = 18.sp,
            color = Color.Black,
            style = MaterialTheme.typography.headlineLarge
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            PersonaButton(text = "-1", onClick = { onPuntosChange(-1) })
            PersonaButton(text = "+1", onClick = { onPuntosChange(1) })
        }
        PersonaButton(
            text = "Cambiar Color",
            onClick = { mostrarColorPicker = true }
        )
        if (mostrarColorPicker) {
            AlertDialog(
                onDismissRequest = { mostrarColorPicker = false },
                title = { Text("Selecciona un color") },
                text = {
                    Box(
                        modifier = Modifier
                            .height(150.dp)
                            .fillMaxWidth()
                    ) {
                        HsvColorPicker(
                            controller = colorPickerController,
                            modifier = Modifier.fillMaxSize(),
                            onColorChanged = { colorEnvelope ->
                                onColorChange(colorEnvelope.color)
                            }
                        )
                    }
                },
                confirmButton = {
                    PersonaButton(
                        text = "Aceptar",
                        onClick = { mostrarColorPicker = false }
                    )
                }
            )
        }
    }
}
