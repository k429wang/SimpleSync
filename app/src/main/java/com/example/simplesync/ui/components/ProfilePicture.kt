package com.example.simplesync.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.Dp

@Composable
private fun BaseProfilePicture(
    imageUrl: String?,
    size: Dp,
    showEditIcon: Boolean,
    onEditClick: (() -> Unit)? = null
) {
    val hasProfilePic = !imageUrl.isNullOrBlank() && imageUrl != "null"
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .then(if (onEditClick != null) Modifier.clickable { onEditClick() } else Modifier)
    ) {
        if (hasProfilePic) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("$imageUrl?t=${System.currentTimeMillis()}")
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        } else {
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
                    modifier = Modifier.size(size * 0.375f)
                )
            }
        }

        if (showEditIcon && onEditClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = -size * 0.15f, y = -size * 0.125f)
                    .size(size * 0.175f)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile Picture",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(size * 0.09f)
                )
            }
        }
    }
}

@Composable
fun EditableProfilePicture(
    viewModel: UserViewModel,
    snackbarHostState: SnackbarHostState,
    size: Dp = 128.dp
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
                        viewModel.fetchCurrentUser()
                    } else {
                        snackbarHostState.showSnackbar("Failed to upload picture")
                    }
                }
            }
        }
    }

    val imageUrl = currUser?.userMetadata?.profilePicURL

    BaseProfilePicture(
        imageUrl = imageUrl,
        size = size,
        showEditIcon = true,
        onEditClick = { imagePickerLauncher.launch("image/*") }
    )
}

@Composable
fun ReadOnlyProfilePicture(
    imageUrl: String?,
    size: Dp = 128.dp
) {
    BaseProfilePicture(
        imageUrl = imageUrl,
        size = size,
        showEditIcon = false,
        onEditClick = null
    )
}


