package com.solotodo.designsystem.fonts

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.solotodo.R

/**
 * Google Fonts provider, verified via the Play Services font cert arrays in
 * `res/values/font_certs.xml`. On Play Store devices the fonts are fetched
 * from the OS-level cache; on stripped-down distributions we fall back to
 * system defaults.
 */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

/** Display font for every piece of System UI (tabs, labels, titles, ranks). */
val RajdhaniFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Rajdhani"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Rajdhani"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Rajdhani"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Rajdhani"), fontProvider = provider, weight = FontWeight.Bold),
)

/** Backup display face used in places Rajdhani is too condensed (cinematic titles). */
val OrbitronFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Orbitron"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Orbitron"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Orbitron"), fontProvider = provider, weight = FontWeight.Bold),
)

/** Monospace — system signals, metadata, tabular numbers, parse chips. */
val JetBrainsMonoFontFamily = FontFamily(
    Font(googleFont = GoogleFont("JetBrains Mono"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("JetBrains Mono"), fontProvider = provider, weight = FontWeight.Medium),
)

/** Body copy — only used for long-form prose (Reflection screen, task descriptions). */
val InterFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.SemiBold),
)

/**
 * Primary display face. UI chrome, headlines, rank glyphs.
 * Falls through to Orbitron, then system sans, then default.
 */
val SystemDisplay: FontFamily = RajdhaniFontFamily

/** Mono face. Used anywhere numbers or system signals appear. */
val SystemMono: FontFamily = JetBrainsMonoFontFamily

/** Body face. Only where prose needs to breathe. */
val SystemBody: FontFamily = InterFontFamily
