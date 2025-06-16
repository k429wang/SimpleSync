package com.example.simplesync.viewmodel

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

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                supabaseClient.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _signInResult.value = Result.success(true)
            } catch (e: Exception) {
                _signInResult.value = Result.failure(e)
            }
        }
    }
}