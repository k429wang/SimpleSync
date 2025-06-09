package com.example.simplesync.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.layout.*

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource

import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.ui.navigation.rememberSimpleSyncNavController

// icons imported as resources, since we wanted specific ones.
import com.example.simplesync.R

@OptIn(ExperimentalMaterial3Api::class)

// This will work for now, I've spent enough time on it.
// I can improve it later following this article:
// https://medium.com/@heetkanabar/make-your-bottom-nav-bar-beautiful-with-this-ui-in-jetpack-compose-0d0abbba16e0

@Composable
fun SimpleSyncScaffold(
    navController : SimpleSyncNavController,
    pageName: String,
    displayPage: @Composable () -> Unit
) {
    // we need to know the current page, but this feels like breaking encapsulation.
    // I want to make the nav wrapper store it, but  storing it as a value doesn't work
    // because of the back button. I'd have to store it as part of a stack, and then
    // popUpTo in the same way, so I may as well do this and not do the same thing twice.
    // If they didn't want me to do it this way, their code is bad, not mine.
    val page = navController.navController.currentBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier,
        // for some reason, TopAppBar is experimental.
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = pageName) },
                actions = { }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton( onClick = {navController.nav(navController.EVENTS)}){
                        Icon(
                            painter = painterResource(R.drawable.calendar_month_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    // change when friends page is added
                    IconButton( onClick = {navController.nav(navController.HOME)}){
                        Icon(
                            painter = painterResource(R.drawable.group_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    IconButton( onClick = {navController.nav(navController.NEW_EVENT)}){
                        Icon(
                            painter = painterResource(R.drawable.add_circle_24dp_e3e3e3_fill1_wght500_grad0_opsz24),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    // change when events page is added
                    IconButton( onClick = {navController.nav(navController.HOME)}){
                        Icon(
                            painter = painterResource(R.drawable.notifications_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    IconButton( onClick = {navController.nav(navController.CALENDAR)}){
                        Icon(
                            painter = painterResource(R.drawable.account_circle_24dp_e3e3e3_fill1_wght400_grad0_opsz24),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                },
            )
        },
        content = {
            paddingValues ->
            Column(modifier=Modifier.padding(paddingValues)){
                displayPage()
            }
        }
    )
}

@Preview
@Composable
fun ShowScaffold(){
//displays arbitrary content inside itself.
    SimpleSyncScaffold(rememberSimpleSyncNavController(),"Content"){ Text("Hello")}
}
