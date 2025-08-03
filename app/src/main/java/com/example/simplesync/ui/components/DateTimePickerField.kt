package com.example.simplesync.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.util.*

@Composable
fun DateTimePickerField(
    label: String,
    value: Instant?,
    onDateTimeSelected: (Instant) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val formatted = value?.let {
        val localDateTime = it.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.date} ${"%02d".format(localDateTime.time.hour)}:${"%02d".format(localDateTime.time.minute)}"
    } ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .width(100.dp)
                .padding(end = 4.dp)
        )

        OutlinedTextField(
            value = formatted,
            onValueChange = {},
            enabled = false,
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    DatePickerDialog(context, { _, y, m, d ->
                        val hour = calendar.get(Calendar.HOUR_OF_DAY)
                        val minute = calendar.get(Calendar.MINUTE)

                        TimePickerDialog(context, { _, h, min ->
                            val selectedDateTime = LocalDateTime(
                                year = y,
                                monthNumber = m + 1,
                                dayOfMonth = d,
                                hour = h,
                                minute = min
                            )
                            val instant = selectedDateTime.toInstant(TimeZone.currentSystemDefault())
                            onDateTimeSelected(instant)
                        }, hour, minute, false).show()

                    }, year, month, day).show()
                },
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 16.sp),
            singleLine = true
        )
    }
}
