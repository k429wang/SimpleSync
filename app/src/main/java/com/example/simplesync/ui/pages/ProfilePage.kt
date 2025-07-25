package com.example.simplesync.ui.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.components.AvailabilityGrid
import com.example.simplesync.model.ConcreteCalendar
import com.example.simplesync.ui.components.ReadOnlyProfilePicture
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.viewmodel.SignInViewModel
import com.example.simplesync.viewmodel.UserViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    navController: SimpleSyncNavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val currUser by viewModel.currUser.collectAsState()
    val email = currUser?.authUser?.email
    val username = currUser?.userMetadata?.username
    val pfpUrl = currUser?.userMetadata?.profilePicURL
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        val updated = navController.navController
            .previousBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("updated") == true

        if (updated) {
            snackbarHostState.showSnackbar("Account info updated successfully!")
            navController.navController
                .previousBackStackEntry
                ?.savedStateHandle
                ?.remove<Boolean>("updated")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Profile",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = email ?: "Unknown email",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "@${username ?: "Unknown user"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                ReadOnlyProfilePicture(
                    imageUrl = pfpUrl,
                    size = 96.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "My Availability",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card {
                AvailabilityGrid(navController, calendar = ConcreteCalendar())
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add new", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { /* TODO: Sync external calendar */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sync External", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Settings",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()
            SettingsOption("My Account") {
                navController.nav(navController.MY_ACCOUNT)
            }
            HorizontalDivider()
            SettingsOption("Privacy")
            HorizontalDivider()
            SettingsOption("Notification Settings")
            HorizontalDivider()
            SettingsOption("External Calendar Sign-in") {
                navController.nav(navController.EXTERNAL_SIGN_IN)
            }
            HorizontalDivider()


            val viewModel: SignInViewModel = hiltViewModel()
            SettingsOption("Sign Out") {
                viewModel.signOut()
                navController.navController.navigate(navController.SIGN_IN)
                {
                    popUpTo(0)
                    launchSingleTop = true
                }
            }
            HorizontalDivider()

        }
    }
}

@Composable
fun SettingsOption(text: String, onClick: () -> Unit = {}) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
        )
    }
}