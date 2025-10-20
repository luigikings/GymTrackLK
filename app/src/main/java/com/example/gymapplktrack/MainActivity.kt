package com.example.gymapplktrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.gymapplktrack.ui.GymTrackApp
import com.example.gymapplktrack.ui.theme.GymTrackTheme

val LocalAppContainer = staticCompositionLocalOf<AppContainer> { error("Contenedor no inicializado") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as GymTrackApplication).container
        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                GymTrackTheme {
                    GymTrackApp()
                }
            }
        }
    }
}
