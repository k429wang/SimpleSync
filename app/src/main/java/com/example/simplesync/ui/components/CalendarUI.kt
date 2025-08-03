package com.example.simplesync.ui.components

import com.example.simplesync.ui.navigation.SimpleSyncNavController
import android.util.Log
import com.example.simplesync.model.AbstractCalendar
import com.example.simplesync.model.ConcreteCalendar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.model.OnceTimeSlot
import com.example.simplesync.model.DailyTimeSlot
import com.example.simplesync.model.WeeklyTimeSlot
import com.example.simplesync.model.Recurrence
import com.example.simplesync.model.TimeBlock
import com.example.simplesync.viewmodel.EventViewModel
import com.example.simplesync.viewmodel.UserViewModel
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Composable for the availability grid
@Composable
fun AvailabilityGrid(
    navController: SimpleSyncNavController,
    calendar: AbstractCalendar,
    modifier: Modifier = Modifier,
    strategy : String = "NEW",
    startHour: Int = 8,
    endHour: Int = 20,
    slotDuration: Int = 30
) : LocalDateTime? {
    // this needs requiresAPI calls. I want to update to not use this, ideally.
    // problem for future me.
    val eventViewModel: EventViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val currUser by userViewModel.currUser.collectAsState()
    val events by eventViewModel.events.collectAsState()

    val baseCalendar = remember {calendar}
    // I don't think we actually want this to be a remember at this part.
    var returnTime by remember { mutableStateOf<LocalDateTime?>( null ) }

    // for each owned event, add it to our calendar.
    // This isn't like, efficient, but it's good for demo purposes.
    // startTime and endTime are instants so we just need to convert each.
    // For now, we assume it's the one type of event that we have.
    // This is actually still good design because converting allows us to
    // get availability instead of making computing it super complex here.
    val timeZone = TimeZone.currentSystemDefault()

    val decoratedCalendar = remember(events, baseCalendar) {
        var result = baseCalendar
        for (event in events) {
            val st = event.startTime.toLocalDateTime(timeZone)
            val et = event.endTime.toLocalDateTime(timeZone)
            if (event.recurrence == Recurrence.DAILY) {
                result = DailyTimeSlot(
                    decorating = result,
                    startTime = LocalDateTime.of(st.year, st.month,st.dayOfMonth, st.hour, st.minute),
                    endTime = LocalDateTime.of(et.year, et.month, et.dayOfMonth, et.hour, et.minute)
                )
            } else if (event.recurrence == Recurrence.WEEKLY ){
                result = WeeklyTimeSlot(
                    decorating = result,
                    startTime = LocalDateTime.of(st.year, st.month,st.dayOfMonth, st.hour, st.minute),
                    endTime = LocalDateTime.of(et.year, et.month, et.dayOfMonth, et.hour, et.minute)
                )
            } else if (event.recurrence == Recurrence.ONCE ){
                result = OnceTimeSlot(
                    decorating = result,
                    startTime = LocalDateTime.of(st.year, st.month,st.dayOfMonth, st.hour, st.minute),
                    endTime = LocalDateTime.of(et.year, et.month, et.dayOfMonth, et.hour, et.minute)
                )
            }
            Log.d("CAL", event.toString())
        }
        Log.d("CAL", events.toString())
        result
    }

    LaunchedEffect(currUser) {
        currUser?.let {
            eventViewModel.fetchEventsForUser(it.authUser.id)
            // TODO: Currently only fetches owned events.
        }
    }

    val today = remember { LocalDate.now() }
    val days = remember { (0 until 7).map { today.plusDays(it.toLong()) } }
    val timeSlots : MutableList<LocalTime> = remember { mutableListOf() }

    Column(modifier = modifier.fillMaxWidth().wrapContentHeight()) {
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
            modifier = Modifier.fillMaxWidth().height(400.dp).
            background(color= Color(0XD3D3D3FF), shape= RoundedCornerShape(4.dp)),
            verticalArrangement = Arrangement.spacedBy(2.dp)

        ) {
            // we really only need a single small for loop to generate it, not a function.
            timeSlots.clear() // because of the remember
            for (minutes in 60*startHour .. 60*endHour step slotDuration){
                timeSlots.add(LocalTime.of(minutes/60, minutes%60))
            }

            Log.d("CAL", "$timeSlots")

            items(timeSlots) { slot ->
                returnTime = timeSlotRow(
                    navController = navController,
                    timeSlot = slot,
                    days = days,
                    availabilityData = decoratedCalendar.getAvailability(today.atTime(0,0)),
                    modifier = Modifier.fillMaxWidth(),
                    strategy = strategy
                )
            }
        }
    }
    return returnTime
}

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
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun timeSlotRow (
    navController: SimpleSyncNavController,
    timeSlot: LocalTime,
    days: List<LocalDate>,
    availabilityData: MutableList<TimeBlock>,
    modifier: Modifier = Modifier,
    strategy: String
    ): LocalDateTime? {
    var returnTime by remember { mutableStateOf<LocalDateTime?>( null ) }
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
            val slotTime = LocalDateTime.of(day, timeSlot)

            var flag = true
            availabilityData.forEach{ block ->
                // if this is true, then the block is within an availability zone, should be red
                if (slotTime >= block.startTime && slotTime < block.endTime){
                    flag = false
                }
            }

            val color = if (flag) Color(0xFF4CAF50) else Color(0xFFF44336)

            // This box component is not ideal, but it's serviceable.
            // A horizontalDivider is better, but not interactable.
            // We could also just do away with the borders and background and padding.
            // That would make things line up. Then if we keep horizontal padding, we get the right effect.
            // That works tbh.
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(20.dp)
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(color)
                    .clickable{
                        // do something! Redirect to a page to add
                        // an event, or modify that event.
                        Log.d("CAL","Flag is set to: $flag")
                        if (strategy == "NEW"){
                            if (flag) {
                                Log.d("CAL", "Calling NavController")
                                navController.bottomButtonNav(navController.NEW_EVENT)
                                // return is null now
                            } else {
                                // navigate to specific event this refers to
                            }
                        } else if (strategy == "RETURN")
                            if (flag) {
                                returnTime = LocalDateTime.of(day, timeSlot)
                            } else {
                                returnTime = LocalDateTime.of(day, timeSlot)
                            }
                    }

            ) {
                // Optional: Add content here for interactivity
            }
        }
    }
    return returnTime
}

// Usage Example
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