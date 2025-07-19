package com.example.simplesync.viewmodel

import android.util.Log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {
    private val _signInResult = MutableStateFlow<Result<Boolean>?>(null)
    val signInResult: StateFlow<Result<Boolean>?> = _signInResult

    // StateFlow to indicate if user is signed in
    private val _isSignedIn = MutableStateFlow(supabaseClient.auth.currentSessionOrNull() != null)

    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            val session = supabaseClient.auth.currentSessionOrNull()
            _isSignedIn.value = session != null
            //debug log
            if (session != null) {
                Log.d("SignInViewModel", "Session found! User is signed in. Session: $session")
            } else {
                Log.d("SignInViewModel", "No session found. User is NOT signed in.")
            }
        }
    }
    val isSignedIn: StateFlow<Boolean> = _isSignedIn

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                supabaseClient.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _signInResult.value = Result.success(true)
                _isSignedIn.value = true
            } catch (e: Exception) {
                _signInResult.value = Result.failure(e)
                _isSignedIn.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            supabaseClient.auth.signOut()

            //debug logs
            val session = supabaseClient.auth.currentSessionOrNull()
            if (session != null) {
                Log.d("SignInViewModel", "Session still exists after sign out! Session: $session")
            } else {
                Log.d("SignInViewModel", "No session found after sign out.")
            }
            _isSignedIn.value = false
        }
    }
}