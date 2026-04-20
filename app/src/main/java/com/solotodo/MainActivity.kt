package com.solotodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.solotodo.designsystem.theme.SoloTodoTheme
import com.solotodo.ui.devgallery.DevGalleryScreen
import com.solotodo.ui.placeholder.PlaceholderScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoloTodoTheme {
                var showGallery by remember { mutableStateOf(false) }
                if (showGallery) {
                    DevGalleryScreen()
                } else {
                    PlaceholderScreen(
                        onOpenGallery = if (BuildConfig.DEBUG) ({ showGallery = true }) else null,
                    )
                }
            }
        }
    }
}
