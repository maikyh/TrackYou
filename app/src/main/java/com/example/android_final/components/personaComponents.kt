package com.example.android_final.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun PersonaButton( text:String, onClick:()->Unit){
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp)), //bordes redondeados
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black, //boton negro
            contentColor = Color(0xFFFFFFFF)
        )
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

