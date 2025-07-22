package com.example.simplesync.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.GoogleAuthUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.UserRecoverableAuthException
import android.content.Intent
import com.google.gson.Gson

data class CalendarEventsResponse(
    val items: List<CalendarEvent>
)

data class CalendarEvent(
    val id: String?,
    val summary: String?,
    val description: String?,   // <-- add this line
    val location: String?,
    val start: CalendarDateTime?,
    val end: CalendarDateTime?
)

data class CalendarDateTime(
    val dateTime: String? = null,
    val date: String? = null
)

class ExternalCalendarViewModel : ViewModel() {
    val consentRequiredIntent = MutableLiveData<Intent?>()

    var googleAccount = mutableStateOf<GoogleSignInAccount?>(null)
        private set

    fun setGoogleAccount(account: GoogleSignInAccount?) {
        googleAccount.value = account
    }

    // add to list
    fun fetchGoogleCalendarEvents(
        context: Context,
        onSuccess: (List<CalendarEvent>) -> Unit = {}
    ) {
        val account = googleAccount.value
        if (account == null) {
            Log.d("CalendarViewModel", "No Google account connected!")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {

                val accountObj = account.account
                if (accountObj == null) {
                    Log.d("CalendarViewModel", "GoogleSignInAccount.account is null!")
                    return@launch
                }

                val token = GoogleAuthUtil.getToken(
                    context,
                    accountObj,
                    "oauth2:https://www.googleapis.com/auth/calendar.readonly"
                )

                val url = "https://www.googleapis.com/calendar/v3/calendars/primary/events"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()
                val json = response.body?.string()
                Log.d("CalendarViewModel", "Events JSON: $json")
                // TODO: Parse JSON and update state as needed

                val eventsResponse = Gson().fromJson(json, CalendarEventsResponse::class.java)

                if (eventsResponse.items.isEmpty()) {
                    Log.d("CalendarViewModel", "No events found in calendar!")
                } else {
                    for (event in eventsResponse.items) {
                        Log.d(
                            "CalendarViewModel",
                            "Event: id=${event.id}, summary=${event.summary}, description=${event.description}, location=${event.location}, start=${event.start?.dateTime ?: event.start?.date}, end=${event.end?.dateTime ?: event.end?.date}"
                        )
                    }
                }
                onSuccess(eventsResponse.items)

            } catch (e: UserRecoverableAuthException) {
                // Notify the UI to launch the consent intent
                consentRequiredIntent.postValue(e.intent)
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Calendar fetch error", e)
            }
        }
    }

}