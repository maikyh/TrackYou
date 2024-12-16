package com.example.trackingyou

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun OnboardingScreen(
    navController: NavHostController,
    onboardingViewModel: OnboardingViewModel
) {
    val pages = listOf(
        OnboardingPage("Bienvenido", "¡Descubre las mejores funciones de nuestra app!", R.drawable.icon1),
        OnboardingPage("Monitorea tu salud", "Registra tus datos y realiza un seguimiento de tu progreso.", R.drawable.icon2),
        OnboardingPage("Mantente saludable", "Recibe recomendaciones personalizadas para un estilo de vida saludable.", R.drawable.icon3)
    )

    var currentPage by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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

        Text(
            text = pages[currentPage].title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = pages[currentPage].description,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF486FC7),
            modifier = Modifier
                .padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                        // Marcar que el onboarding ha sido mostrado
                        onboardingViewModel.setOnboardingShown()

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
