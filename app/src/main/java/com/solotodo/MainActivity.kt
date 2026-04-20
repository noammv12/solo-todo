package com.solotodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.solotodo.designsystem.theme.SoloTodoTheme
import com.solotodo.ui.cinematics.CinematicHost
import com.solotodo.ui.devgallery.DevGalleryScreen
import com.solotodo.ui.nav.SoloTodoNav
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoloTodoTheme {
                var showGallery by remember { mutableStateOf(false) }
                // Root Box so the cinematic overlay draws above the nav host
                // (including bottom sheets rendered inside it). Cinematic Z=200
                // per SoloTokens.ZIndex.Cinematic.
                Box(Modifier.fillMaxSize()) {
                    if (showGallery) {
                        DevGalleryScreen()
                    } else {
                        SoloTodoNav(
                            onOpenDevGallery = if (BuildConfig.DEBUG) ({ showGallery = true }) else null,
                        )
                    }
                    CinematicHost()
                }
            }
        }
    }
}
