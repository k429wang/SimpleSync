package com.example.simplesync.ui.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.simplesync.di.SupabaseClientEntryPoint
import com.example.simplesync.ui.navigation.SimpleSyncAppNav
import com.example.simplesync.ui.navigation.rememberSimpleSyncNavController
import dagger.hilt.android.EntryPointAccessors
import io.github.jan.supabase.auth.auth

@Composable
fun MainScreen(){//viewModel: AppViewModel) {
    val navController = rememberSimpleSyncNavController()
//    val viewModel = RecipeViewModel()

    // Access SupabaseClient via Hilt EntryPoint
    val context = LocalContext.current
    val supabaseClient = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SupabaseClientEntryPoint::class.java
        ).supabaseClient()
    }

    // Navigate to the sign in page if not logged in
    val isSignedIn = remember { supabaseClient.auth.currentSessionOrNull() != null }
    LaunchedEffect(Unit) {
        if(!isSignedIn) {
            navController.nav(navController.SIGN_IN)
        }
    }

    SimpleSyncAppNav(navController = navController)//, viewModel = viewModel)
}