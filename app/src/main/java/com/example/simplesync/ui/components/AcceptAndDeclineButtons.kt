package com.example.simplesync.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AcceptAndDeclineButtons(
    onAccept: (() -> Unit), // For accept/decline buttons
    onDecline: (() -> Unit)
) {
    Column {
        TextButton(onClick = onAccept) {
            Text("Accept", color = Color(0xFF4CAF50))
        }
        TextButton(onClick = onDecline) {
            Text("Decline", color = Color(0xFFF44336))
        }
    }
}