package com.example.android_final.nav

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.android_final.onboarding.OnboardingView
import com.example.android_final.splashArt.SplashView
import com.example.android_final.views.HistorialView
import com.example.android_final.views.MarcadorView
import com.example.app_volleyball_finalandroid.views.MenuView

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun appNavigation(navController: NavHostController){
    NavHost(navController = navController, startDestination = "/splash"){
        composable("/splash"){
            SplashView(navController = navController)

        }
        composable("/onboarding"){
            OnboardingView(navController = navController)

        }
        composable("/menu"){
            MenuView(navController)

        }
        composable("/marcador"){
            MarcadorView(navController)

        }
        composable("/historial"){
            HistorialView(navController = navController)
        }

    }

}