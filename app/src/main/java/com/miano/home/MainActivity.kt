package com.miano.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import com.miano.home.data.SampleHouseholdRepository
import com.miano.home.launch.AppLauncher
import com.miano.home.ui.home.HomeScreen
import com.miano.home.ui.theme.MianoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launcher = AppLauncher(this)
        // Swap for the real HTTP/WebSocket repository once the household API exists.
        val repository = SampleHouseholdRepository()

        // This is the home screen: Back here does nothing instead of closing it.
        onBackPressedDispatcher.addCallback(this) { }

        setContent {
            MianoTheme {
                HomeScreen(repository = repository, launcher = launcher)
            }
        }
    }
}
