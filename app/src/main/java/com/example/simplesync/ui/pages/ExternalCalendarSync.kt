package com.example.simplesync.ui.pages
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.common.api.ApiException
import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.viewmodel.ExternalCalendarViewModel

import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.navigation.SimpleSyncNavController

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope

@Composable
fun LaunchGoogleSignIn(calendarViewModel: ExternalCalendarViewModel) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            calendarViewModel.setGoogleAccount(account)
        } catch (e: ApiException) {
            calendarViewModel.setGoogleAccount(null)
            Log.e("GoogleSignIn", "Sign-in error", e)
        }
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(CALENDAR_SCOPE))
        .build()
    val signInClient = GoogleSignIn.getClient(context, gso)

    Button(
        onClick = {
            signInClient.signOut().addOnCompleteListener {
                launcher.launch(signInClient.signInIntent)
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Sign in with Google", color = Color.White)
    }
}

@Composable
fun UploadICSFile(uploadedFileName: androidx.compose.runtime.MutableState<String>) {
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast("/") ?: "Selected File"
            uploadedFileName.value = name

            val inputStream = context.contentResolver.openInputStream(uri)
            val fileContent = inputStream?.bufferedReader()?.use { it.readText() }
            Log.d("ICS File", "Content: $fileContent")
        }
    }

    Button(
        onClick = {
            filePickerLauncher.launch("text/calendar")
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Upload Calendar File", color = Color.White)
    }
}

private const val CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar"

@Composable
fun ExternalCalendarSyncPage(navController: SimpleSyncNavController) {
    val calendarViewModel: ExternalCalendarViewModel = hiltViewModel()
    val context = LocalContext.current

    // Always check for a cached Google account when entering this screen
    LaunchedEffect(Unit) {
        if (calendarViewModel.googleAccount.value == null) {
            val cachedAccount = GoogleSignIn.getLastSignedInAccount(context)
            calendarViewModel.setGoogleAccount(cachedAccount)
        }
    }
    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column {
                Text(
                    text = "Sync to External Calendar",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val context = LocalContext.current
//                        val account = GoogleSignIn.getLastSignedInAccount(context)
//                        val googleAccount = remember {
//                            mutableStateOf(account?.email ?: "Not synced")
//                        }
                        val outlookAccount = remember { mutableStateOf("Not synced") }
                        val uploadedFileName = remember { mutableStateOf("No file uploaded") }

                        Spacer(modifier = Modifier.height(0.dp))

                        LaunchGoogleSignIn(calendarViewModel)
                        val googleAccount = calendarViewModel.googleAccount.value
                        Text(
                            text = "Current: ${googleAccount?.email ?: "Not synced"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(64.dp))

                        Button(
                            onClick = { /* TODO: Implement Outlook Calendar sign-in */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign in with Outlook", color = Color.White)
                        }
                        Text(
                            text = "Current: ${outlookAccount.value}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(64.dp))

                        UploadICSFile(uploadedFileName)
                        Text(
                            text = "Current: ${uploadedFileName.value}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExternalCalendarSyncPreview() {
    // Preview cannot provide a real navController, so pass a stub or mock if needed
    // ExternalCalendarSyncPage(navController = ...)
}
