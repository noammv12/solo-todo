package com.solotodo.ui.status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solotodo.core.a11y.ReduceMotionPolicy
import com.solotodo.data.onboarding.WelcomeBannerStore
import com.solotodo.designsystem.SoloTokens
import com.solotodo.designsystem.components.Panel
import com.solotodo.designsystem.components.TypeIn
import com.solotodo.designsystem.fonts.SystemDisplay
import com.solotodo.designsystem.fonts.SystemMono
import com.solotodo.designsystem.modifiers.glow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * One-shot post-onboarding banner: shown the first time the user lands on
 * Status after completing Awakening, auto-dismisses after 6s, never returns.
 *
 * Reduce-motion: skips the TypeIn reveal, shows the full text immediately;
 * 6s dismissal stays the same.
 */
@Composable
fun WelcomeBanner(
    viewModel: WelcomeBannerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val visible by viewModel.visible.collectAsState()
    val reduceMotion by viewModel.reduceMotion.collectAsState()
    var dismissed by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible && !dismissed) {
            // Mark shown as soon as the banner renders so a quick backgrounding
            // can't re-show it on next launch.
            viewModel.markShown()
            delay(6_000)
            dismissed = true
        }
    }

    AnimatedVisibility(
        visible = visible && !dismissed,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier,
    ) {
        Panel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            glow = true,
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "SYSTEM · AWAKENED",
                    color = SoloTokens.Colors.Glow,
                    fontFamily = SystemMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 0.30.em,
                    modifier = Modifier.glow(SoloTokens.Glow.Cyan),
                )
                Spacer(Modifier.height(2.dp))
                if (reduceMotion) {
                    Text(
                        text = BODY,
                        color = SoloTokens.Colors.Text,
                        fontFamily = SystemDisplay,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.04.em,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    TypeIn(
                        text = BODY,
                        charsPerSecond = 38,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = SystemDisplay,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.04.em,
                        ),
                    )
                }
            }
        }
    }
}

private const val BODY = "A NEW HUNTER HAS ENTERED."

@HiltViewModel
class WelcomeBannerViewModel @Inject constructor(
    private val store: WelcomeBannerStore,
    reduceMotionPolicy: ReduceMotionPolicy,
) : ViewModel() {

    /** Inverted: `visible = true` until the banner has been shown. */
    val visible: StateFlow<Boolean> = store.shown
        .map { shown -> !shown }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val reduceMotion: StateFlow<Boolean> = reduceMotionPolicy.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun markShown() {
        viewModelScope.launch { store.markShown() }
    }
}
