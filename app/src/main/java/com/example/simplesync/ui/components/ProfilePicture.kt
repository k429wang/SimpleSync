package com.example.simplesync.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.simplesync.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip

@Composable
fun ProfilePicture(
    viewModel: UserViewModel,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currUser by viewModel.currUser.collectAsState()
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.use { stream -> stream.readBytes() }
            bytes?.let {
                scope.launch {
                    val success = viewModel.uploadProfilePicture(it)
                    if (success) {
                        snackbarHostState.showSnackbar("Profile picture updated")
                        viewModel.fetchCurrentUser() // Refresh from Supabase
                    } else {
                        snackbarHostState.showSnackbar("Failed to upload picture")
                    }
                }
            }
        }
    }

    val imageUrl = currUser?.userMetadata?.profilePicURL?.let {
        // Add cache busting suffix to make the URL appear new to Coil
        // everytime currUser updates (which it does in uploadProfilePicture)
        if (it.isNotBlank()) "$it?t=${System.currentTimeMillis()}" else null
    }

    val hasProfilePic = !imageUrl.isNullOrBlank() && imageUrl != "null"

    Box (
        modifier = Modifier
            .size(128.dp)
            .clip(CircleShape)
            .clickable { imagePickerLauncher.launch("image/*") }
    ) {
        if (hasProfilePic) {
            // Display the user's stored profile picture
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        } else {
            // Display the default profile picture
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Profile",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Edit Icon Button overlay (top-right corner)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-16).dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Profile Picture",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text("Click to update", style = MaterialTheme.typography.bodySmall)
}
