package com.example.simplesync.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.model.User
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.viewmodel.UserViewModel
import kotlinx.coroutines.launch


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

            Spacer(modifier = Modifier.height(24.dp))

            UsersList(viewModel)

            Spacer(modifier = Modifier.height(24.dp))

            CreateUserForm(viewModel, snackbarHostState)
        }
    }
}

@Composable
fun UserDetails(viewModel: UserViewModel = hiltViewModel()) {
    val currUser by viewModel.currUser.collectAsState()
    var userNameInput by remember { mutableStateOf("") }

    Text("User Details:")

    // Text field to input ID
    OutlinedTextField(
        value = userNameInput,
        onValueChange = { userNameInput = it },
        label = { Text("Enter User ID") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Button to trigger fetch
    Button(
        onClick = { viewModel.getUserById(userNameInput) },
        enabled = userNameInput.isNotBlank()
    ) {
        Text("Fetch User by ID")
    }

    // Display fetched user details
    currUser?.let { user ->
        Text(
            "Selected User:\n${user.userName} - ${user.email} (${user.firstName} ${user.lastName})",
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun UsersList(viewModel: UserViewModel = hiltViewModel()) {
    val users by viewModel.users.collectAsState()

    Text("All Users:")
    LazyColumn {
        items(users, key = { user -> user.userName }) { user ->
            Text(
                text = "${user.userName} - ${user.email}: ${user.firstName} ${user.lastName}",
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Composable
fun CreateUserForm(
    viewModel: UserViewModel,
    snackbarHostState: SnackbarHostState
) {
    var userName by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Column {
        Text("Enter details for new user:", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Username") },
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
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val newUser = User(
                    userName = userName,
                    email = email,
                    firstName = firstName,
                    lastName = lastName
                )
                viewModel.createUser(
                    newUser = newUser,
                    onSuccess = {
                        userName = ""
                        email = ""
                        firstName = ""
                        lastName = ""
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("User created!")
                        }
                    },
                    onError = { e ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Error: ${e.message}")
                        }
                    }
                )
            },
            enabled = userName.isNotBlank() && email.isNotBlank() && firstName.isNotBlank() && lastName.isNotBlank()
        ) {
            Text("Create User")
        }
    }
}