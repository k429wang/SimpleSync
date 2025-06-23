package com.example.simplesync.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.layout.*

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight

import com.example.simplesync.ui.navigation.SimpleSyncNavController
import com.example.simplesync.ui.navigation.rememberSimpleSyncNavController

// icons imported as resources, since we wanted specific ones.

@OptIn(ExperimentalMaterial3Api::class)

// This will work for now, I've spent enough time on it.
// I can improve it later following this article, if that fits our use case.:
// https://medium.com/@heetkanabar/make-your-bottom-nav-bar-beautiful-with-this-ui-in-jetpack-compose-0d0abbba16e0

@Composable
fun SimpleSyncScaffold(
    navController: SimpleSyncNavController,
    pageName: String,
    displayPage: @Composable () -> Unit
) {
    // we need to know the current page, but this feels like breaking encapsulation.
    // I want to make the nav wrapper store it, but  storing it as a value doesn't work
    // because of the back button. I'd have to store it as part of a stack, and then
    // popUpTo in the same way, so I may as well do this and not do the same thing twice.
    Scaffold(
        modifier = Modifier,
        // for some reason, TopAppBar is experimental.
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = pageName,
                               style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)) },
                actions = { }
            )
        },
        bottomBar = {
            BottomNavBar(navController)
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