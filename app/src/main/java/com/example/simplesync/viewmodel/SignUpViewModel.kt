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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _signUpResult = MutableStateFlow<Result<Boolean>?>(null)
    val signUpResult: StateFlow<Result<Boolean>?> = _signUpResult

    fun signUp(email: String, firstName: String, lastName: String, password: String) {
        viewModelScope.launch {
            try {
                supabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    // Package data for Users table for SQL trigger
                    this.data = buildJsonObject {
                        put("first_name", JsonPrimitive(firstName))
                        put("last_name", JsonPrimitive(lastName))
                    }
                }
                _signUpResult.value = Result.success(true)
            } catch (e: Exception) {
                // Handle error
                _signUpResult.value = Result.failure(e)
            }
        }
    }
}