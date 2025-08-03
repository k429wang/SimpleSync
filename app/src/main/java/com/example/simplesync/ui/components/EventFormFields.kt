package com.example.simplesync.ui.components

import DropdownField
import androidx.compose.runtime.Composable
import kotlinx.datetime.Instant

@Composable
fun EventFormFields(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    startTime: Instant?,
    onStartTimeChange: (Instant?) -> Unit,
    endTime: Instant?,
    onEndTimeChange: (Instant?) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    recurrence: String,
    onRecurrenceChange: (String) -> Unit,
    visibility: String,
    onVisibilityChange: (String) -> Unit
) {
    val typeOptions = listOf("IRL", "Virtual")
    val recurrenceOptions = listOf("Once", "Daily", "Weekly")

    EventField("Name:", name, onNameChange)
    EventField("Description:", description, onDescriptionChange)

    DateTimePickerField("Start Time", startTime, onStartTimeChange)
    DateTimePickerField("End Time", endTime, onEndTimeChange)

    DropdownField("Type:", typeOptions, type, onTypeChange)
    EventField("Location:", location, onLocationChange)
    DropdownField("Recurrence:", recurrenceOptions, recurrence, onRecurrenceChange)
    VisibilityDropdownField(visibility, onVisibilityChange)
}