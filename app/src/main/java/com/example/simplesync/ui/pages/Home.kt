package com.example.simplesync.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.simplesync.ui.navigation.SimpleSyncNavController

@Composable
fun HomePage(navController: SimpleSyncNavController) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ){
    Button(
        onClick = {
            navController.nav(navController.CALENDAR)
        },
        colors = ButtonDefaults.buttonColors(Color.Gray)
    )
    { Text(text = "CALENDAR", color = Color.Black)}
    Button(
        onClick = {
            navController.nav(navController.EVENTS)
        },
        colors = ButtonDefaults.buttonColors(Color.Gray)
    )
    { Text(text = "EVENTS", color = Color.Black)}
    Button(
        onClick = {
            navController.nav(navController.NEW_EVENT)
        },
        colors = ButtonDefaults.buttonColors(Color.Gray)
    )
    { Text(text = "NEW_EVENT", color = Color.Black)}
    }
}