package com.ikent.backsteptimeplanner.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import com.ikent.backsteptimeplanner.ui.screen.PlannerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("逆算アプリ") },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Surface(Modifier.fillMaxSize().padding(padding)) {
            PlannerScreen()
        }
    }
}
