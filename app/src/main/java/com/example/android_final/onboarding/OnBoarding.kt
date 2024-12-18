package com.example.android_final.onboarding

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.android_final.components.ButtonFinish
import com.example.android_final.data.PageData
import com.example.android_final.dataStore.StoreBoarding
import com.example.android_final.R



// ONBOARDING ES COMO EL TUTORIAL
// PARA EL ONBOARDING MOSTREMOS 4 PANTALLAS
// 1 EXPLICAR MENU
// 2 EXPLICAR MARCADORES
// 3 EXPLICAR HISTORIAL
// 4 PROMOCION GOOGLE PLAY

// Estructura para las vistas con pagedata(titulo, descripcion, imagen)
val onboardingPages = listOf(
    PageData("Menu", "Aqui puedes acceder a todas las funcionalidades de la app.", R.drawable.img_menu),
    PageData("Marcadores", "Registra los puntos y sets de los equipos.", R.drawable.img_marcador),
    PageData("Historial", "Consulta el historial de partidos anteriores.", R.drawable.img_historial),
    PageData("Calificanos en Google Play", "Si la app te gustó no olvides dejarnos 5 estrellas en tu reseña!", R.drawable.img_play)
)


//actual
@Composable
fun OnboardingView(navController: NavController) {
    val context = LocalContext.current
    val storeBoarding = StoreBoarding(context)

    val pagerState = rememberPagerState(0){ onboardingPages.size}

    OnBoardPages(
        item = onboardingPages,
        pagerState = pagerState,
        navController = navController,
        store = storeBoarding
    )
}



@Composable
fun OnBoardPages(
    item: List<PageData>,
    pagerState: PagerState,
    navController: NavController,
    store: StoreBoarding
) {

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(state = pagerState) { page ->
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {

                    Text(//titulo
                        text = item[page].title,
                        modifier = Modifier.padding(top = 24.dp),
                        color = Color.Black,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )

                    Image(painter = painterResource(id = item[page].image), contentDescription = "")//img

                    Text(//desc
                        text = item[page].desc,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp)) {

            //indicador pagina
            PagerIndicator(item.size, pagerState.currentPage)
            ButtonFinish(pagerState.currentPage, navController, store)
        }
    }
}

@Composable
fun PagerIndicator(totalPages: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        repeat(totalPages) { page ->
            Icon(
                imageVector = if (page == currentPage) Icons.Filled.ArrowForward else Icons.Default.ArrowForward,
                contentDescription = "Indicador de pagina",
                modifier = Modifier
                    .padding(4.dp)
                    .size(8.dp)
                    .background(
                        if (page == currentPage) Color.Black else Color.Gray,
                        shape = CircleShape
                    )
            )
        }
    }
}
