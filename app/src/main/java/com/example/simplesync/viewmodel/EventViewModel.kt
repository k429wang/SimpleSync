package com.example.simplesync.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplesync.model.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val EVENTS_TABLE = "events"

@HiltViewModel
class EventViewModel @Inject constructor(
    private val supabase: SupabaseClient
) : ViewModel() {
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val events: StateFlow<List<Event>> = _events
    val selectedEvent: StateFlow<Event?> = _selectedEvent

    // Used to display success/failure messages
    private val _eventResult = MutableStateFlow<Result<Boolean>?>(null)
    val eventResult: StateFlow<Result<Boolean>?> = _eventResult

    // Retrieve the events for a specific user
    fun fetchEventsForUser(userId: String) {
        viewModelScope.launch {
            try {
                val fetched = supabase.from(EVENTS_TABLE).select {
                    filter {
                        eq("owner", userId)
                    }
                }.decodeList<Event>()
                _events.value = fetched
                _eventResult.value = Result.success(true)
            } catch (e: Exception) {
                _eventResult.value = Result.failure(e)
                Log.e("EventViewModel", "Decoding failed", e)
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
            }
        }
    }

    // Create an event
    fun createEvent(event: Event) {
        viewModelScope.launch {
            try {
                supabase.from(EVENTS_TABLE).insert(event)
                fetchEventsForUser(event.owner) // Update state with latest list of events
                _eventResult.value = Result.success(true)
            } catch (e: Exception) {
                _eventResult.value = Result.failure(e)
            }
        }
    }

    // Update an existing event's fields
    fun updateEvent(event: Event) {
        viewModelScope.launch {
            try {
                supabase.from(EVENTS_TABLE).update(event) {
                        filter {
                            eq("id", event.id)
                        }
                    }
                fetchEventsForUser(event.owner) // Update state with latest list of events
                _eventResult.value = Result.success(true)
            } catch (e: Exception) {
                _eventResult.value = Result.failure(e)
            }
        }
    }

}