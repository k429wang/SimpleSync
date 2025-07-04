package com.example.simplesync.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.components.ScreenTitle
import com.example.simplesync.ui.navigation.SimpleSyncNavController

@Composable
fun EventPage(navController: SimpleSyncNavController) {
    Text(
        text = "Event Page"
    )

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Title
            ScreenTitle("My events")
        }
    }
}