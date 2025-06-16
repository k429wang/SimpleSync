package com.example.simplesync.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ExternalCalendarSyncPage() {
    Scaffold { padding ->
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
                        val googleAccount = remember { mutableStateOf("Not synced") }
                        val outlookAccount = remember { mutableStateOf("Not synced") }
                        val uploadedFileName = remember { mutableStateOf("No file uploaded") }

                        Spacer(modifier = Modifier.height(0.dp))

                        Button(
                            onClick = { /* TODO: Implement Google Calendar sign-in */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign in with Google", color = Color.White)
                        }
                        Text(
                            text = "Current: ${googleAccount.value}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Divider()
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
                        Divider()
                        Spacer(modifier = Modifier.height(64.dp))

                        Button(
                            onClick = { /* TODO: Implement file upload */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Upload Calendar File", color = Color.White)
                        }
                        Text(
                            text = "Current: ${uploadedFileName.value}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Divider()
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
    ExternalCalendarSyncPage()
}
