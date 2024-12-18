package com.example.android_final.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android_final.components.PersonaButton
import androidx.compose.ui.res.painterResource
import com.example.android_final.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialView(navController: NavController){
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Historial de volleyball",
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
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {

                Text(
                    text = "Equipo A: 1 \n Equipo B: 5 \n El 12/12/2024",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = "Equipo A: 3 \n Equipo B: 2 \n El 11/12/2024",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = "Equipo A: 0 \n Equipo B: 0  \n El 10/12/2024",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(30.dp))

            }
        }
}
