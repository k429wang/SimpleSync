package com.example.simplesync.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.ui.components.BottomNavBar

@Composable
fun HomePage(navController: SimpleSyncNavController) {
    Scaffold(
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Welcome to Home Page!")
        }
    }
}
