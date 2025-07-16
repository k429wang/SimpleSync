package com.example.simplesync.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AcceptAndDeclineButtons(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Column {
        IconButton(
            onClick = onAccept,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Accept",
                tint = Color(0xFF4CAF50)
            )
        }
        IconButton(
            onClick = onDecline,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Decline",
                tint = Color(0xFFF44336)
            )
        }
    }
}

