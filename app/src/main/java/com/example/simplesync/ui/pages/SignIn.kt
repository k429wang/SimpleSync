package com.example.simplesync.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.viewmodel.SignInViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SignIn(
    navController: SimpleSyncNavController,
    viewModel: SignInViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val signInResult by viewModel.signInResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect (signInResult) {
        signInResult?.let {
            it.onSuccess {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Signed in successfully!")
                }
                navController.nav(navController.HOME) // Go to home screen
            }.onFailure { e ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error: ${e.message}")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column (modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {

            Text("Sign In", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button (
                onClick = { viewModel.signIn(email, password) },
                enabled = email.isNotBlank() && password.isNotBlank()
            ) {
                Text("Sign In")
            }

            // Allow navigation to SignUp page
            TextButton (onClick = { navController.nav(navController.SIGN_UP) }) {
                Text("Don't have an account? Sign up")
            }
        }
    }
}