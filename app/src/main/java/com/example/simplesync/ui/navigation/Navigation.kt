package com.example.simplesync.ui.navigation

import android.util.Log
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.simplesync.ui.pages.EventPage
import com.example.simplesync.ui.pages.ExternalCalendarSyncPage
import com.example.simplesync.ui.pages.NewEventPage
import com.example.simplesync.ui.pages.ProfileScreen
import com.example.simplesync.ui.pages.SearchPage
import com.example.simplesync.ui.pages.SettingsPage
import com.example.simplesync.ui.pages.SignIn
import com.example.simplesync.ui.pages.SignUp
import com.example.simplesync.ui.pages.MyAccountPage
import com.example.simplesync.ui.pages.FriendsPage
import com.example.simplesync.ui.pages.NotificationsPage
import com.example.simplesync.ui.pages.EventDetailsPage
import com.example.simplesync.viewmodel.EventViewModel

/*
    The purpose of this function is to collect nav into a single, easy-to-use file,
    and have the class to collect allowed constants & useful data. This allows for
    recording state related to navigation & accessing it more easily.
 */

// Guide to setting up nav sourced from this medium article and adapted for the app
// https://medium.com/@KaushalVasava/navigation-in-jetpack-compose-full-guide-beginner-to-advanced-950c1133740

// Also used was the Android JetSnack compose sample
// this is not a one-to-one copy/paste, but it did help inform how to collect nav into one location.
// https://github.com/android/compose-samples/blob/main/Jetsnack/app/src/main/java/com/example/jetsnack/ui/navigation/JetsnackNavController.kt

// This code is modified from a base I built in CS 346. It's well-made, no need to let
// good code go to waste.
// To use it, just import this navigation page, and call
// navController.nav(navController.PAGE), where PAGE is a string defined below
@Composable
fun rememberSimpleSyncNavController(
    navController: NavHostController = rememberNavController()
): SimpleSyncNavController = remember(navController) {
    SimpleSyncNavController(navController)
}

@Composable
fun SimpleSyncAppNav(
    modifier: Modifier = Modifier,
    navController: SimpleSyncNavController,
    // If we want to pass data between UI screens, use this.
    //viewModel: AppViewModel,
    startDestination: String = "EVENTS"
){
    NavHost(
        modifier = modifier,
        navController = navController.navController,
        startDestination = startDestination,
        // transitions removed following this forum post:
        // https://stackoverflow.com/questions/70191128/how-to-remove-default-transitions-in-jetpack-compose-navigation
        // Just makes things slightly faster, may or may not be what we want.
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            ExitTransition.None
        }
    ) {
        composable(navController.SIGN_IN) {
            SignIn(navController)
        }
        composable(navController.EVENTS) {
            EventPage(navController)
        }
        composable(navController.NEW_EVENT) {
            NewEventPage(navController)
        }
        composable(navController.SEARCH) {
            SearchPage(navController)
        }
        composable(navController.SETTINGS) {
            SettingsPage(navController)
        }
        composable(navController.PROFILE) {
            ProfileScreen(navController)
        }
        composable(navController.SIGN_UP) {
            SignUp(navController)
        }
        composable(navController.MY_ACCOUNT) {
            MyAccountPage(navController)
        }
        composable(navController.EXTERNAL_SIGN_IN) {
            ExternalCalendarSyncPage(navController)
        }
        composable(navController.FRIENDS) {
            FriendsPage(navController)
        }
        composable(navController.NOTIFICATIONS) {
            NotificationsPage(navController)
        }
        composable("${navController.EVENT_DETAILS}/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")

            if (eventId != null) {
                val viewModel: EventViewModel = hiltViewModel()
                val event by viewModel.selectedEvent.collectAsState()

                // Fetch when this composable loads
                LaunchedEffect(eventId) {
                    viewModel.fetchEventById(eventId)
                }
                when {
                    event == null -> Text("Event not found or still loading")
                    else -> EventDetailsPage(navController, event!!)
                }
            } else {
                    Text("Missing event ID")
            }
        }
    }
}

@Stable
class SimpleSyncNavController(
    val navController: NavHostController,

    ){
    // there were examples of these being enums, or objects, or other things
    // It's honestly more convenient to have them here as constants.
    // these are the routes used in the nav function below.
    // no dedicated home?
    val SIGN_UP = "SIGN_UP"
    val SIGN_IN = "SIGN_IN"
    val EVENTS = "EVENTS"
    val NEW_EVENT = "NEW_EVENT"
    val SETTINGS = "SETTINGS"
    val SEARCH = "SEARCH"
    val PROFILE = "PROFILE"
    val MY_ACCOUNT = "MY_ACCOUNT"
    val EXTERNAL_SIGN_IN = "EXTERNAL_SIGN_IN"
    val FRIENDS = "FRIENDS"
    val NOTIFICATIONS = "NOTIFICATIONS"
    val EVENT_DETAILS = "EVENT_DETAILS"

    // Helper function to navigate to event details page based on event ID
    fun eventDetailsRoute(eventId: String) = "$EVENT_DETAILS/$eventId"

    // and, for convenience, we also save the current page!
    // This is a kotlin class instead of a composable, so no need for statefulness

    fun bottomButtonNav( route:String ){
        if (route != navController.currentDestination?.route){
            Log.d("nav", "Scaffold nav to $route")
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
                // we can use a popupto to clear out the backstack.
                // We mostly want to maintain location while scrolling events,
                // so that's where we will pop to. This lets us backspace from there
                // directly to the welcome page, or out of the app if not implemented.
                // Bug was caused by failing to include popUpTo in nav function below.
                // okay no there's still bugs in here, I remember I didn't fix it entirely.
                popUpTo(EVENTS) { saveState = true }
            }
        } else {
            Log.d("nav","Nav Warn: Navigating to current page")
        }
    }
    fun backButtonNav(){
        navController.popBackStack()
    }
    fun nav(route: String){
        Log.d("NAV", "Going to $route")
        navController.navigate( route ) {
            // prevents duplicates
            launchSingleTop = true
            restoreState = true

            //popUpTo(EVENTS) { saveState = true }
        }
    }
}
