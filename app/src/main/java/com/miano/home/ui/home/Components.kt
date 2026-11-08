package com.miano.home.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.miano.home.data.HomeStatus
import com.miano.home.data.StatusTone
import com.miano.home.launch.AppShortcut
import com.miano.home.ui.theme.MianoColors

private val tileShape = RoundedCornerShape(12.dp)

/** Placeholder color per app, used only when the app's own banner can't be loaded. */
private fun fallbackTileColor(app: AppShortcut): Color = when (app.id) {
    "disney" -> Color(0xFF12305A)
    "fubo" -> Color(0xFF23272F)
    "youtube" -> Color(0xFF3A1515)
    "retro" -> Color(0xFF2A1F3D)
    else -> MianoColors.SurfaceRaised
}

/** A focusable app tile. Focus = white ring + slight zoom, as in the mockup. */
@Composable
fun AppTile(
    app: AppShortcut,
    banner: ImageBitmap?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = fallbackTileColor(app)
    Surface(
        onClick = onClick,
        modifier = modifier.size(width = 160.dp, height = 90.dp),
        shape = ClickableSurfaceDefaults.shape(shape = tileShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = bg,
            contentColor = Color.White,
            focusedContainerColor = bg,
            focusedContentColor = Color.White,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(3.dp, Color.White), shape = tileShape),
        ),
    ) {
        if (banner != null) {
            Image(
                bitmap = banner,
                contentDescription = app.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(app.label, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/** Household mode button in the top bar. */
@Composable
fun ModePill(label: String, selected: Boolean, onClick: () -> Unit) {
    val pill = RoundedCornerShape(50)
    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = pill),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) MianoColors.Text else MianoColors.Pill,
            contentColor = if (selected) MianoColors.Background else MianoColors.TextSecondary,
            focusedContainerColor = MianoColors.Accent,
            focusedContentColor = MianoColors.Background,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}

@Composable
fun StatusChip(status: HomeStatus) {
    val (bg, fg) = when (status.tone) {
        StatusTone.Good -> MianoColors.GoodBg to MianoColors.Good
        StatusTone.Warning -> MianoColors.WarningBg to MianoColors.Warning
        StatusTone.Neutral -> MianoColors.NeutralBg to MianoColors.TextSecondary
    }
    Text(
        text = status.label,
        fontSize = 11.sp,
        color = fg,
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color = MianoColors.TextMuted,
    )
}
