package com.example.simplesync.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.components.EditableProfilePicture
import com.example.simplesync.ui.components.EventField
import com.example.simplesync.ui.components.ScreenTitle
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun MyAccountPage(navController: SimpleSyncNavController) {
    val viewModel: UserViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenTitle("My Account")
            EditableProfilePicture(
                viewModel = viewModel,
                snackbarHostState = snackbarHostState,
                size = 96.dp
            )
            UserDetails(viewModel, snackbarHostState, navController)
        }
    }
}

@Composable
fun UserDetails(
    viewModel: UserViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    navController: SimpleSyncNavController
) {
    val currUser by viewModel.currUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    currUser?.let { user ->
        val authUser = user.authUser
        val metadata = user.userMetadata

        var firstName by remember { mutableStateOf(metadata.firstName) }
        var lastName by remember { mutableStateOf(metadata.lastName) }
        var username by remember { mutableStateOf(metadata.username) }

        EventField("Username:", username, onValueChange = {username = it})
        EventField("First Name:", firstName, onValueChange = {firstName = it})
        EventField("Last Name:", lastName, onValueChange = {lastName = it})

        Spacer(modifier = Modifier.height(16.dp))

        Text("User ID: ${authUser.id}")
        Text("Email: ${authUser.email}")
        Text("Created At: ${authUser.createdAt}")
        Text("Last Sign In: ${authUser.lastSignInAt}")

        Button(
            onClick = {
                viewModel.updateUserMetadata(
                    firstName = firstName,
                    lastName = lastName,
                    username = username
                ) { success ->
                    coroutineScope.launch {
                        if (success) {
                            snackbarHostState.showSnackbar("Updating...")
                            navController.navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("updated", true)
                            navController.nav(navController.PROFILE)
                        } else {
                            snackbarHostState.showSnackbar("Failed to update user")
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Save Changes", color = MaterialTheme.colorScheme.onPrimary)
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}