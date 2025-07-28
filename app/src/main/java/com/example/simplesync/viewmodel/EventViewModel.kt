package com.example.simplesync.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplesync.model.Attendee
import com.example.simplesync.model.Event
import com.example.simplesync.model.EventRole
import com.example.simplesync.model.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val EVENTS_TABLE = "events"
const val ATTENDEES_TABLE = "attendees"

@HiltViewModel
class EventViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val events: StateFlow<List<Event>> = _events
    val selectedEvent: StateFlow<Event?> = _selectedEvent

    // Used to display success/failure messages
    private val _eventResult = MutableStateFlow<Result<Event>?>(null)
    val eventResult: StateFlow<Result<Event>?> = _eventResult

    private val _attendeeResult = MutableStateFlow<Result<Attendee>?>(null)
    val attendeeResult: StateFlow<Result<Attendee>?> = _attendeeResult

    private val _attendeesForEvent = MutableStateFlow<List<Attendee>>(emptyList())
    val attendeesForEvent: StateFlow<List<Attendee>> = _attendeesForEvent

    // Retrieve the events for a specific user
    // Not just the events they own, but those they are a part of.
    fun fetchEventsForUser(userId: String) {
        viewModelScope.launch {
            try {
                val events = supabase
                    .from(EVENTS_TABLE)
                    .select(
                        Columns.raw("""
                        id, owner, name, description, start_time, end_time, type, location, recurrence, visibility, created_at, updated_at,
                        attendees!inner(event_id, user_id, invited_by, invite_status)
                    """.trimIndent())
                    ) {
                        filter {
                            eq("attendees.user_id", userId)
                            eq("attendees.invite_status", "ACCEPTED")
                        }
                    }
                    .decodeList<Event>()
                _events.value = events
            } catch (e: Exception) {
                _events.value = emptyList()
                Log.e("EventViewModel", "Error fetching events for user", e)
            }
        }
    }


    // Retrieve event based on event ID
    fun fetchEventById(eventId: String) {
        viewModelScope.launch {
            try {
                val fetched = supabase.from(EVENTS_TABLE).select {
                    filter {
                        eq("id", eventId)
                    }
                    limit(1)
                }.decodeSingle<Event>()

                _selectedEvent.value = fetched
            } catch (e: Exception) {
                // Handle failure in frontend, since it's a simple targeted fetch
                _selectedEvent.value = null
                Log.e("EventViewModel", "Fetching failed", e)
            }
        }
    }

    // Retrieve the attendees for an event
    fun fetchAttendeesForEvent(eventId: String) {
        viewModelScope.launch {
            try {
                val attendees = supabase.from(ATTENDEES_TABLE).select {
                    filter { eq("event_id", eventId) }
                }.decodeList<Attendee>()

                _attendeesForEvent.value = attendees
            } catch (e: Exception) {
                Log.e("EventViewModel", "Failed to fetch attendees", e)
            }
        }
    }

    // Create an event
    fun createEvent(event: Event) {
        viewModelScope.launch {
            try {
                // Insert into events
                // NOTE that a corresponding Attendees row will be created via Supabase Trigger
                val inserted = supabase.from(EVENTS_TABLE).insert(event) {
                    select()
                }.decodeSingle<Event>()
                fetchEventsForUser(inserted.owner) // Update state with latest list of events
                _eventResult.value = Result.success(inserted)
            } catch (e: Exception) {
                // Handle failure in frontend
                _eventResult.value = Result.failure(e)
            }
        }
    }

    // Invite a user to an event
    fun inviteUserToEvent(eventId: String, toUser: String, fromUser: String, role: EventRole) {
        viewModelScope.launch {
            try {
                val inserted = supabase.from(ATTENDEES_TABLE).insert(
                    Attendee(
                        eventId=eventId,
                        userId=toUser,
                        role=role,
                        invitedBy=fromUser,
                        inviteStatus=Status.PENDING,
                    )
                ) {
                    select()
                }.decodeSingle<Attendee>()

                fetchAttendeesForEvent(inserted.eventId)
                _attendeeResult.value = Result.success(inserted)
            } catch (e: Exception) {
                // Handle failure in frontend
                _attendeeResult.value = Result.failure(e)
            }
        }
    }

    fun removeAttendee(eventId: String, userId: String) {
        viewModelScope.launch {
            try {
                supabase.from(ATTENDEES_TABLE).delete {
                    filter {
                        eq("event_id", eventId)
                        eq("user_id", userId)
                    }
                }
                fetchAttendeesForEvent(eventId)
            } catch (e: Exception) {
                Log.e("EventViewModel", "Failed to remove attendee", e)
            }
        }
    }

    // Update an existing event's fields
    fun updateEvent(event: Event, byUser: String?) {
        viewModelScope.launch {
            try {
                if (byUser == null) {
                    throw IllegalArgumentException("byUser must not be null when updating an event.")
                }

                val inserted = supabase.from(EVENTS_TABLE).update(event) {
                        filter {
                            eq("id", event.id)
                        }
                        select()
                    }.decodeSingle<Event>()
                fetchEventsForUser(byUser) // Update state with latest list of events
                _eventResult.value = Result.success(inserted)
            } catch (e: Exception) {
                // Handle failure in frontend
                _eventResult.value = Result.failure(e)
            }
        }
    }
}