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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.viewmodel.UserViewModel


@Composable
fun UserProfile(navController: SimpleSyncNavController) {
    val viewModel: UserViewModel = hiltViewModel()
    val currUser by viewModel.currUser.collectAsState()

    var userNameInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("User Profile Page", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

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

        currUser?.let { user ->
            Text(
                "Selected User:\n${user.userName} - ${user.email} (${user.firstName} ${user.lastName})",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("List of all users:")
        UsersList(viewModel)
    }
}

@Composable
fun UsersList(viewModel: UserViewModel = hiltViewModel()) {
    val users by viewModel.users.collectAsState()

    LazyColumn {
        items(users, key = { user -> user.userName }) { user ->
            Text(
                text = "${user.userName} - ${user.email}: ${user.firstName} ${user.lastName}",
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
