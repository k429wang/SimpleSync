package com.example.simplesync.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.viewmodel.UserViewModel


@Composable
fun UserProfile(navController: SimpleSyncNavController) {
    val viewModel: UserViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold (
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text("User Profile Page", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            UserDetails(viewModel)
        }
    }
}

@Composable
fun UserDetails(viewModel: UserViewModel = hiltViewModel()) {
    val currUser by viewModel.currUser.collectAsState()

    Text("User Details:")

    currUser?.let { user ->
        val authUser = user.authUser
        val metadata = user.userMetadata

        Text(
            text = buildString {
                appendLine("User ID: ${authUser.id}")
                appendLine("Name: ${metadata.firstName} ${metadata.lastName}")
                appendLine("Email: ${authUser.email}")
                appendLine("Created At: ${authUser.createdAt}")
                appendLine("Last Sign In: ${authUser.lastSignInAt}")
            },
            modifier = Modifier.padding(top = 8.dp)
        )
    } ?: Text("Loading user info...")
}