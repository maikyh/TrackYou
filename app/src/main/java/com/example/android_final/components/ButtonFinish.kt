package com.example.android_final.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android_final.dataStore.StoreBoarding
import com.example.app_volleyball_finalandroid.components.PersonaButton

@Composable
fun ButtonFinish(currentPage: Int, navController: NavController, store: StoreBoarding){
    Row (modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 20.dp),
        horizontalArrangement = if(currentPage != 2)Arrangement.SpaceBetween else Arrangement.Center)
    {
        if(currentPage>=3){
            PersonaButton(
                text = "Entrar",
                onClick = {
                    navController.navigate("/menu"){
                        popUpTo(0)
                    }
                })
        }
    }
}