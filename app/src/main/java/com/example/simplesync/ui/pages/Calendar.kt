package com.example.simplesync.ui.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.simplesync.ui.components.CalendarApp
import com.example.simplesync.ui.navigation.SimpleSyncNavController


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarPage(navController: SimpleSyncNavController) {
    CalendarApp(navController)
}