package com.example.simplesync.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient

// Allows us to access SupabaseClient to determine if we are signed in
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SupabaseClientEntryPoint {
    fun supabaseClient(): SupabaseClient
}
