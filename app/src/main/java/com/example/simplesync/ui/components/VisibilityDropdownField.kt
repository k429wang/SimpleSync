package com.example.simplesync.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VisibilityDropdownField(
    value: String,
    onValueChange: (String) -> Unit
) {
    val visibilityOptions = mapOf(
        "Solo" to "Only you can see the event.",
        "Private" to "Only invited users can see the event.",
        "Public" to "All your friends can see the event."
    )

    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Visibility:",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .width(100.dp)
                .padding(end = 4.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }
                },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 16.sp)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                visibilityOptions.forEach { (option, description) ->
                    DropdownMenuItem(
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                        text = {
                            Column {
                                Text(text = option, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

