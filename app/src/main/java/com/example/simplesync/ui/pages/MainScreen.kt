package com.example.simplesync.ui.pages

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.simplesync.ui.navigation.SimpleSyncAppNav
import com.example.simplesync.ui.navigation.rememberSimpleSyncNavController
import com.example.simplesync.viewmodel.SignInViewModel


@Composable
fun MainScreen(
    signInViewModel: SignInViewModel = hiltViewModel(),
) {
    val navController = rememberSimpleSyncNavController()

    // Navigate to the sign in page if not logged in
    val isSignedIn by signInViewModel.isSignedIn.collectAsState()
    LaunchedEffect(isSignedIn) {
        if (!isSignedIn) {
            navController.navController.navigate(navController.SIGN_IN) {
                popUpTo(0)
                launchSingleTop = true
            }
        } else {
            navController.navController.navigate(navController.EVENTS) {
                popUpTo(0)
                launchSingleTop = true
            }
        }
    }

    SimpleSyncAppNav(navController = navController)
}