package com.solotodo.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.solotodo.data.auth.AuthRepository
import com.solotodo.designsystem.SoloTokens
import com.solotodo.ui.auth.AuthViewModel
import com.solotodo.ui.auth.SignInScreen
import com.solotodo.ui.quests.QuestsScreen
import com.solotodo.ui.quickadd.QuickAddSheet
import com.solotodo.ui.status.StatusScreen

/**
 * App root: owns the NavHost, bottom tab bar, and the Quick Add sheet.
 */
@Composable
fun SoloTodoNav(
    onOpenDevGallery: (() -> Unit)? = null,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by authViewModel.state.collectAsState()
    // Route on auth state: sign-in screen until the user is authed (even Guest).
    when (authState.status) {
        AuthRepository.AuthState.Loading -> {
            // Brief splash matching app background; avoids flicker.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SoloTokens.Colors.BgVoid),
            )
            return
        }
        AuthRepository.AuthState.NotAuthed -> {
            SignInScreen(viewModel = authViewModel)
            return
        }
        else -> Unit // continue to the app below
    }

    val navController = rememberNavController()
    var quickAddOpen by remember { mutableStateOf(false) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = when (backStackEntry?.destination?.route) {
        SoloTab.QUESTS.route -> SoloTab.QUESTS
        else -> SoloTab.STATUS
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoloTokens.Colors.BgVoid),
    ) {
        val insets = WindowInsets.systemBars.asPaddingValues()
        NavHost(
            navController = navController,
            startDestination = SoloTab.STATUS.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = insets.calculateTopPadding()),
        ) {
            composable(SoloTab.STATUS.route) { StatusScreen() }
            composable(SoloTab.QUESTS.route) { QuestsScreen() }
        }

        // Bottom tab bar
        SoloTabBar(
            current = currentTab,
            onSelect = { tab ->
                navController.navigate(tab.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = insets.calculateBottomPadding()),
        )

        // FAB
        FloatingCaptureButton(
            onClick = { quickAddOpen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 20.dp,
                    bottom = insets.calculateBottomPadding() + 80.dp,
                ),
        )

        // Dev gallery access (debug only): tiny ghost text at top-right.
        if (onOpenDevGallery != null) {
            androidx.compose.material3.Text(
                text = "DEV",
                color = SoloTokens.Colors.TextDim,
                style = com.solotodo.designsystem.theme.SystemMonoLabel,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = insets.calculateTopPadding() + 8.dp, end = 12.dp)
                    .clickable { onOpenDevGallery() },
            )
        }
    }

    if (quickAddOpen) {
        QuickAddSheet(onDismiss = { quickAddOpen = false })
    }
}
