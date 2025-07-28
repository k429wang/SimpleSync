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
import com.example.simplesync.viewmodel.EventViewModel
import com.example.simplesync.viewmodel.UserViewModel
import com.example.simplesync.model.Event
import com.example.simplesync.model.EventType
import com.example.simplesync.model.Recurrence
import com.example.simplesync.model.Visibility
import com.example.simplesync.ui.components.BottomNavBar
import com.example.simplesync.ui.navigation.SimpleSyncNavController

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope

import biweekly.Biweekly
import kotlinx.datetime.Instant
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

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
fun UploadICSFile(
    uploadedFileName: androidx.compose.runtime.MutableState<String>,
    eventViewModel: EventViewModel,
    currUserId: String?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && currUserId != null) {
            val name = uri.lastPathSegment?.substringAfterLast("/") ?: "Selected File"
            uploadedFileName.value = name

            coroutineScope.launch {
                // Fetch latest events for user before checking for duplicates
                eventViewModel.fetchEventsForUser(currUserId)

                // Read the file after fetching events
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileContent = inputStream?.bufferedReader()?.use { it.readText() }
                Log.d("ICS File", "Content: $fileContent")

                if (!fileContent.isNullOrBlank()) {
                    val ical = Biweekly.parse(fileContent).first()

                    for (vevent in ical.events) {
                        val uid = vevent.uid?.value
                        val summary = vevent.summary?.value ?: "(No title)"
                        val description = vevent.description?.value
                        val location = vevent.location?.value

                        val startDate = vevent.dateStart?.value
                        val endDate = vevent.dateEnd?.value
                        val startTime = startDate?.let { Instant.fromEpochMilliseconds(it.time) }
                        val endTime = endDate?.let { Instant.fromEpochMilliseconds(it.time) }

                        if (startTime == null || endTime == null) {
                            Log.d("ICS Import", "Skipped event (missing times): $summary")
                            continue
                        }

                        val appEvent = Event(
                            owner = currUserId,
                            name = summary,
                            description = description,
                            startTime = startTime,
                            endTime = endTime,
                            type = EventType.VIRTUAL,
                            location = location,
                            recurrence = Recurrence.ONCE,
                            visibility = Visibility.PUBLIC,
                            externalId = uid
                        )

                        // Use only the up-to-date duplicate check
                        if (uid == null || !eventViewModel.isDuplicateEvent(uid)) {
                            eventViewModel.createEvent(appEvent)
                            Log.d("ICS Import", "Created event: $summary ($uid)")
                        } else {
                            Log.d("ICS Import", "Skipped duplicate: $summary ($uid)")
                        }
                    }
                }
            }
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
                        val eventViewModel: EventViewModel = hiltViewModel()
                        val userViewModel: UserViewModel = hiltViewModel()
                        val currUser by userViewModel.currUser.collectAsState()
                        val currUserId = currUser?.authUser?.id

                        UploadICSFile(uploadedFileName, eventViewModel, currUserId)
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
