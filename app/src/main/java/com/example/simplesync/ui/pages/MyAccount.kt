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
        val metadata = user.userMetadata

        val firstName = remember { mutableStateOf(metadata.firstName) }
        val lastName = remember { mutableStateOf(metadata.lastName) }
        val username = remember { mutableStateOf(metadata.username) }

        EventField("Username:", username)
        EventField("First Name:", firstName)
        EventField("Last Name:", lastName)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Email: ${user.authUser.email}")
        Text("Created At: ${user.authUser.createdAt}")
        Text("Last Sign-In: ${user.authUser.lastSignInAt}")

        Button(
            onClick = {
                viewModel.updateUserMetadata(
                    firstName = firstName.value,
                    lastName = lastName.value,
                    username = username.value
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