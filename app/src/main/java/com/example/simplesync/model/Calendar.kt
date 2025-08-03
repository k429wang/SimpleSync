package com.example.simplesync.model

import java.time.DayOfWeek
import java.time.LocalDate
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
    val dayOfWeek: DayOfWeek
)

interface AbstractCalendar {
    fun getAvailability(time: LocalDateTime):MutableList<TimeBlock>
}

// Concrete Calendar - implements the accessor,
// default of a blank calendar is every time slot is free.
class ConcreteCalendar : AbstractCalendar {
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
        // for the next week from today, we add a daily time slot
        val today = LocalDate.now()
        val days = (0 until 7).map { today.plusDays(it.toLong()) }
        for ( day in days ){
            // If it starts at a particular time, we don't want it repeating earlier.
            if ( day >= startTime.toLocalDate() ) {
                val nextStart = LocalDateTime.of(day, startTime.toLocalTime())
                val nextEnd = LocalDateTime.of(day, endTime.toLocalTime())
                outList.add(TimeBlock(nextStart, nextEnd, day.dayOfWeek))
            }
        }
        return outList
    }
}

class WeeklyTimeSlot (
    override val decorating: AbstractCalendar,
    override val startTime: LocalDateTime,
    override val endTime: LocalDateTime
) : CalendarDecorator {
    override fun getAvailability(time: LocalDateTime): MutableList<TimeBlock> {
        val outList = decorating.getAvailability(time)
        // we know which day of the week it is - the weekly time slot occurs
        // in the next nearest day with the same day of the week.
        val dayOfWeek = startTime.dayOfWeek
        // quick and dirty, tbh, but that's fine
        val today = LocalDate.now()
        val days = (0 until 7).map { today.plusDays(it.toLong()) }
        for ( day in days ){
            // this should only be true ONCE
            if (day.dayOfWeek == dayOfWeek && day >= startTime.toLocalDate()){
                val nextStart = LocalDateTime.of(day, startTime.toLocalTime())
                val nextEnd = LocalDateTime.of(day, endTime.toLocalTime())
                outList.add(TimeBlock(nextStart, nextEnd, dayOfWeek))
            }
        }
        return outList
    }
}

// unused but available
class WeekdailyTimeSlot (
    override val decorating: AbstractCalendar,
    override val startTime: LocalDateTime,
    override val endTime: LocalDateTime
) : CalendarDecorator {
    override fun getAvailability(time: LocalDateTime): MutableList<TimeBlock> {
        val outList = decorating.getAvailability(time)
        // we know which day of the week it is - the weekly time slot occurs
        // in the next nearest day with the same day of the week.
        val dayOfWeek = startTime.dayOfWeek
        // quick and dirty, tbh, but that's fine
        val today = LocalDate.now()
        val days = (0 until 7).map { today.plusDays(it.toLong()) }
        for ( day in days ){
            // this should only be true ONCE
            if (day.dayOfWeek == dayOfWeek && day >= startTime.toLocalDate() && day.dayOfWeek != DayOfWeek.SATURDAY && day.dayOfWeek != DayOfWeek.SUNDAY){
                val nextStart = LocalDateTime.of(day, startTime.toLocalTime())
                val nextEnd = LocalDateTime.of(day, endTime.toLocalTime())
                outList.add(TimeBlock(nextStart, nextEnd, dayOfWeek))
            }
        }
        return outList
    }
}

class OnceTimeSlot (
    override val decorating: AbstractCalendar,
    override val startTime: LocalDateTime,
    override val endTime: LocalDateTime,
) : CalendarDecorator {
    override fun getAvailability(time: LocalDateTime): MutableList<TimeBlock> {
        val outList = decorating.getAvailability(time)
        outList.add(TimeBlock(startTime,endTime,startTime.dayOfWeek))
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