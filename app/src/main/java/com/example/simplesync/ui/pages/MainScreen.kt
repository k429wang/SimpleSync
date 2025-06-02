package com.example.simplesync.ui.pages

import androidx.compose.runtime.Composable
import com.example.simplesync.ui.navigation.SimpleSyncAppNav
import com.example.simplesync.ui.navigation.rememberSimpleSyncNavController

@Composable
fun MainScreen(){//viewModel: AppViewModel) {
    val navController = rememberSimpleSyncNavController()
//    val viewModel = RecipeViewModel()

    SimpleSyncAppNav(navController = navController)//, viewModel = viewModel)
}