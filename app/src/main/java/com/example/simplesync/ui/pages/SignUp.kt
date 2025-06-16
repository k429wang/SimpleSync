package com.example.simplesync.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.viewmodel.SignUpViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SignUp(
    navController: SimpleSyncNavController,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var shouldShowPassword by remember { mutableStateOf(false) }
    val signUpResult by viewModel.signUpResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect (signUpResult) {
        signUpResult?.let {
            it.onSuccess {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Signed up successfully! Check your email.")
                }
                navController.nav(navController.SIGN_IN)  // Redirect to sign-in
            }.onFailure { e ->
                print("ERROR: ${e.message}")
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error: ${e.message}")
                }
            }
        }
    }

    Scaffold (
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column (modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {

            Text("Sign Up", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (shouldShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
                // Button to toggle visibility of password
                trailingIcon = {
                    val icon = if (shouldShowPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    val desc = if (shouldShowPassword) "Hide Password" else "Show Password"

                    IconButton(onClick = {shouldShowPassword = !shouldShowPassword}) {
                        Icon(imageVector = icon, contentDescription = desc)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button (
                onClick = { viewModel.signUp(email, firstName, lastName, password) },
                enabled = email.isNotBlank() && password.length >= 6
            ) {
                Text("Create Account")
            }
        }
    }
}
