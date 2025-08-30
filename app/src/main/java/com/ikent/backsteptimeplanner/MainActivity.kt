package com.ikent.backsteptimeplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ikent.backsteptimeplanner.ui.AppScaffold
import com.ikent.backsteptimeplanner.ui.theme.BackStepTimePlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(0x00000000, 0x00000000),
            navigationBarStyle = SystemBarStyle.auto(0x00000000, 0x00000000)
        )

        setContent {
            BackStepTimePlannerTheme {
                AppScaffold()
            }
        }
    }
}
