package com.example.simplesync.ui.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.di.SupabaseClientEntryPoint
import com.example.simplesync.ui.navigation.SimpleSyncAppNav
import com.example.simplesync.ui.navigation.rememberSimpleSyncNavController
import dagger.hilt.android.EntryPointAccessors
import io.github.jan.supabase.auth.auth
import com.example.simplesync.viewmodel.SignInViewModel


@RequiresApi(Build.VERSION_CODES.O)
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
    val signInViewModel: SignInViewModel = hiltViewModel()


    val isSignedIn by signInViewModel.isSignedIn.collectAsState()
    LaunchedEffect(isSignedIn) {
        if (!isSignedIn) {
            navController.navController.navigate(navController.SIGN_IN) {
                popUpTo(0)
                launchSingleTop = true
            }
        } else {
            navController.navController.navigate(navController.HOME) {
                popUpTo(0)
                launchSingleTop = true
            }
        }
    }

    SimpleSyncAppNav(navController = navController)//, viewModel = viewModel)
}