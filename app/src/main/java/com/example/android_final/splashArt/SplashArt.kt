package com.example.android_final.splashArt

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.android_final.R
import kotlinx.coroutines.delay


@Composable
fun SplashView(navController: NavController) {
    // Animatable para manejar la posición vertical (desplazamiento en Y)
    val offsetY = remember { Animatable(-500f) } // inicia fuera de la pantalla (arriba)

    // Estado de animación Lottie
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_splash))
    val progress = animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever // animación infinita
    )

    // Lanzar animación al cargar la vista
    LaunchedEffect(Unit) {
        offsetY.animateTo(
            targetValue = 0f, // termina en la posición original (sin desplazamiento)
            animationSpec = tween(
                durationMillis = 1000, // duración de la animación
                easing = EaseOutBounce // efecto rebote
            )
        )
        delay(4000) // espera 6 segundos antes de navegar
        navController.navigate("/onboarding") {
            popUpTo(0) // elimina el splash de la pila de navegación
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        //imagen con animación de desplazamiento
        Image(
            painter = painterResource(id = R.drawable.fuerza_ginyu),
            contentDescription = "",
            modifier = Modifier
                .offset(y = offsetY.value.dp) //aplica el desplazamiento animado
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Equipo 100",
            style = MaterialTheme.typography.headlineLarge
        )
        Box(modifier = Modifier
//            .offset(y = offsetY.value.dp)
            .size(250.dp)
            .align(Alignment.CenterHorizontally)){
            LottieAnimation(
                composition = composition,
                progress = progress.value
            )
        }
    }
}
