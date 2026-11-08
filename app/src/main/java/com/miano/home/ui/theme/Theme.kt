package com.miano.home.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

/** Colors from the Living Room mockup (Style A + C). */
object MianoColors {
    val Background = Color(0xFF0E1116)
    val Surface = Color(0xFF161A21)
    val SurfaceRaised = Color(0xFF1B2230)
    val Pill = Color(0xFF1A1F27)
    val Border = Color(0xFF242B36)
    val Text = Color(0xFFF2F4F7)
    val TextSecondary = Color(0xFFC3CAD3)
    val TextMuted = Color(0xFF9AA3AF)
    val Accent = Color(0xFFF5A524)
    val Good = Color(0xFF7FD8A6)
    val GoodBg = Color(0xFF13261C)
    val Warning = Color(0xFFF5C46B)
    val WarningBg = Color(0xFF2E2312)
    val NeutralBg = Color(0xFF1F2733)
}

@Composable
fun MianoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = MianoColors.Accent,
            onPrimary = MianoColors.Background,
            background = MianoColors.Background,
            onBackground = MianoColors.Text,
            surface = MianoColors.Surface,
            onSurface = MianoColors.Text,
        ),
        content = content,
    )
}
