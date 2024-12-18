package com.example.app_volleyball_finalandroid.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android_final.R
import com.example.android_final.components.PersonaButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuView(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Menu de voleibol",
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
                text = "Menu Principal",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(30.dp))
            PersonaButton(text = "Partido", onClick = { navController.navigate("/marcador") })
            Spacer(modifier = Modifier.height(16.dp))
            PersonaButton(text = "Historial", onClick = { navController.navigate("/historial") })
            Spacer(modifier = Modifier.height(30.dp))

            Image(painter = painterResource(id = R.drawable.img_volley), contentDescription = "menu")
        }
    }
}
