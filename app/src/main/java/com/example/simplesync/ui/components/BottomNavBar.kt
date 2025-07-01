package com.example.simplesync.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.R

@Composable
fun BottomNavBar(navController: SimpleSyncNavController) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.calendar_icon),
                    contentDescription = "Calendar",
                    modifier = Modifier.size(24.dp)
                )
            },
            selected = false,
            onClick = { navController.bottomButtonNav(navController.CALENDAR) }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.friends_icon),
                    contentDescription = "Team",
                    modifier = Modifier.size(24.dp)
                )
            },
            selected = false,
            onClick = { navController.bottomButtonNav(navController.FRIENDS) }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(24.dp)
                )
            },
            selected = false,
            onClick = { navController.bottomButtonNav(navController.NEW_EVENT) }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.notifications_icon),
                    contentDescription = "Notifications",
                    modifier = Modifier.size(24.dp)
                )
            },
            selected = false,
            onClick = { navController.bottomButtonNav(navController.EVENTS) }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.profile_icon),
                    contentDescription = "Profile",
                    modifier = Modifier.size(24.dp)
                )
            },
            selected = false,
            onClick = { navController.bottomButtonNav(navController.PROFILE) }
        )
    }
}
