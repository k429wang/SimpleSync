package com.example.simplesync.model

import kotlinx.datetime.LocalTime
import java.time.LocalDateTime

//Abstract Calendar interface. This determines *what* our events are, and what time
// *those events* happen. We have what is effectively a wrapper around it,
// the WeeklyCalendar, which stores what the *current time* is, for comparisons.
// then, when that calendar queries isAvailable by passing a time, we get info.
// Now, alternative design choices:
// It would be good to do this iteratively, actually, rather than a recursive decorator.
// Then we get a start time and an end time, for display purposes.
// The point of the weekly calendar is to translate this good, useful decorator design
// to an iterative design for the purpose of displaying, I think.
data class TimeBlock (
    val startTime : LocalDateTime,
    val endTime: LocalDateTime,
    val dayOfWeek: Int
)

interface AbstractCalendar {
    fun getAvailability(time: LocalDateTime):MutableList<TimeBlock>
}

// Concrete Calendar - implements the accessor,
// default of a blank calendar is every time slot is free.
class ConcreteCalendar() : AbstractCalendar {
    override fun getAvailability(time: LocalDateTime): MutableList<TimeBlock> {
        return mutableListOf()
    }
}

// Decorator interface
// LocalTime info:
// expect class LocalTime(hour: Int,
//                        minute: Int,
//                        second: Int = 0,
//                        nanosecond: Int = 0) : Comparable<LocalTime>
// For this aspect of our code, we only really care about hours and not dates.
// For a repeating event on a monthly, weekly, biweekly, etc. basis,
// we do, and for one-time events, we do, but not for
// daily events. Each of those is implemented by a decorator directly.
// I should probably put the decorators in their own file (singular).
//
interface CalendarDecorator : AbstractCalendar {
    val decorating: AbstractCalendar
    // should find a way to make these private, I think.
    val startTime: LocalDateTime
    val endTime: LocalDateTime
    override fun getAvailability(time: LocalDateTime): MutableList<TimeBlock> {
        return decorating.getAvailability(time)
    }
}

// Concrete decorator for timeslots.
// Implement other decorators similarly to this one.
class DailyTimeSlot(
    override val decorating: AbstractCalendar,
    override val startTime: LocalDateTime,
    override val endTime: LocalDateTime
) : CalendarDecorator {
    override fun getAvailability(time: LocalDateTime): MutableList<TimeBlock> {
        val outList = decorating.getAvailability(time)
        for (i in 0..6){
            outList.add(TimeBlock(startTime,endTime,i))
        }
        return outList
    }
}

// Other decorators to write:
// Daily, Weekly, Monthly repeating time slots.
// weekend and weekday time slots
// Exception time slots
// Context time slot (making the availability true only in certain contexts?)
// Or is context a property of all time slots?
// Either way, it should be implemented one level up.