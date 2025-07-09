package com.example.simplesync.ui.components

import com.example.simplesync.ui.navigation.SimpleSyncNavController
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.simplesync.model.AbstractCalendar
import com.example.simplesync.model.ConcreteCalendar
import com.example.simplesync.model.DailyTimeSlot
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplesync.model.TimeBlock
import com.example.simplesync.ui.navigation.rememberSimpleSyncNavController
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Composable for the availability grid
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AvailabilityGrid(
    navController: SimpleSyncNavController,
    calendar: AbstractCalendar,
    modifier: Modifier = Modifier,
    startHour: Int = 8,
    endHour: Int = 20,
    slotDuration: Int = 30
) {
    // this needs requiresAPI calls. I want to update to not use this, ideally.
    // problem for future me.
    val today = remember { LocalDate.now() }
    // convoluted AI-made code, but it works
    val days = remember { (0 until 7).map { today.plusDays(it.toLong()) } }
    // This has been fixed up from garbage AI nonsense.
    val timeSlots : MutableList<LocalTime> = remember { mutableListOf() }
    // the passed value is vestigial - remove at some point
    val availabilityData : MutableList<TimeBlock> = remember{ calendar.getAvailability(today.atTime(0,0)) }

    //
    Column(modifier = modifier.fillMaxWidth()) {
        // Day headers - makes sense to keep these, but pared down. No need for day-by-day,
        // we can just do Mon-Sun, no need for dates. Too much info.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.width(80.dp)) // Space for time labels

            days.forEach { day ->
                DayHeader(day = day, modifier = Modifier.weight(1f))
            }
        }

        // Grid body
        // This is actually really modifiable at this point. It's good.
        // I think it might be better to do it one column at a time, though.
        // I think that ends up being more efficient, but this is fine for now.
        LazyColumn(
            modifier = Modifier.fillMaxWidth().
            background(color= Color(0XD3D3D3FF), shape= RoundedCornerShape(4.dp)),
            verticalArrangement = Arrangement.spacedBy(2.dp)

        ) {
            // we really only need a single small for loop to generate it, not a funciton.
            for (minutes in 60*startHour .. 60*endHour step slotDuration){
                timeSlots.add(LocalTime.of(minutes/60, minutes%60))
            }

            items(timeSlots) { slot ->
                TimeSlotRow(
                    navController = navController,
                    timeSlot = slot,
                    days = days,
                    availabilityData = availabilityData,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayHeader(day: LocalDate, modifier: Modifier = Modifier) {
    val formatter = remember { DateTimeFormatter.ofPattern("EEE") }

    Column(
        modifier = modifier
            .padding(4.dp)
            .border(1.dp, Color.LightGray),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatter.format(day),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TimeSlotRow(
    navController: SimpleSyncNavController,
    timeSlot: LocalTime,
    days: List<LocalDate>,
    availabilityData: MutableList<TimeBlock>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time label
        Text(
            text = timeSlot.format(DateTimeFormatter.ofPattern("HH:mm")),
            modifier = Modifier.width(80.dp),
            fontSize = 12.sp
        )

        // Time slot cells
        days.forEach { day ->
            var flag = false
            availabilityData.forEach{ block ->
                // if this is true, then the block is within an availability zone
                if (timeSlot >= block.startTime.toLocalTime() && timeSlot <= block.endTime.toLocalTime()){
                    flag = true
                }
            }

            val color = if (flag) Color(0xFFF44336) else Color(0xFF4CAF50)

            // This box component is not ideal, but it's serviceable.
            // A horizontalDivider is better, but not interactable.
            // We could also just do away with the borders and background and padding.
            // That would make things line up. Then if we keep horizontal padding, we get the right effect.
            // That works tbh.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    //.border(1.dp, Color.LightGray)
                    .background(color)
                    //.padding(2.dp)
                    .clickable{
                        // do something! Redirect to a page to add
                        // an event, or modify that event.
                        if (flag) {
                            navController.nav(navController.NEW_EVENT)
                        } else {
                            // navigate to specific event this
                        }
                    }

            ) {
                // Optional: Add content here for interactivity
            }
        }
    }
}

// Usage Example
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarApp(
    navController: SimpleSyncNavController
) {
    val calendar = remember {
        DailyTimeSlot(
            decorating =
            DailyTimeSlot(
                decorating = ConcreteCalendar(),
                startTime = LocalDateTime.of(LocalDate.of(2025, 5, 18), LocalTime.of(12,30)),
                endTime = LocalDateTime.of(LocalDate.of(2025, 5, 18), LocalTime.of(16,30))
            ),
            startTime = LocalDateTime.of(LocalDate.of(2025, 5, 18), LocalTime.of(18,30)),
            endTime = LocalDateTime.of(LocalDate.of(2025, 5, 18), LocalTime.of(20,30))
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Weekly Availability",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AvailabilityGrid(
            navController = navController,
            calendar = calendar,
            modifier = Modifier.fillMaxSize()
        )
    }
}