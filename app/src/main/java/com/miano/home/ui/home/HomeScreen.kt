package com.miano.home.ui.home

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.miano.home.data.ContinueWatching
import com.miano.home.data.HouseholdMode
import com.miano.home.data.HouseholdRepository
import com.miano.home.data.TonightSummary
import com.miano.home.launch.AppCatalog
import com.miano.home.launch.AppLauncher
import com.miano.home.launch.AppShortcut
import com.miano.home.launch.LaunchResult
import com.miano.home.ui.theme.MianoColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The Living Room home screen (mockup: Style A + C).
 * Sizes are in dp for a 960x540 dp TV canvas (1080p at xhdpi), with ~5% overscan margins.
 */
@Composable
fun HomeScreen(repository: HouseholdRepository, launcher: AppLauncher) {
    val tonightFlow = remember(repository) { repository.tonight() }
    val watchingFlow = remember(repository) { repository.continueWatching() }
    val tonight by tonightFlow.collectAsState(initial = null)
    val watching by watchingFlow.collectAsState(initial = null)

    var mode by rememberSaveable { mutableStateOf(HouseholdMode.LivingRoom) }
    val context = LocalContext.current

    val banners: Map<String, ImageBitmap?> = remember(launcher) {
        AppCatalog.featured.associate { app ->
            app.id to launcher.bannerFor(app)?.let { d ->
                runCatching { d.toBitmap().asImageBitmap() }.getOrNull()
            }
        }
    }

    val onLaunch: (AppShortcut) -> Unit = { app ->
        when (val result = launcher.launch(app)) {
            LaunchResult.Launched -> Unit
            LaunchResult.OpenedStore -> toast(context, "${app.label} isn't installed. Opening the Appstore.")
            LaunchResult.NotInstalled -> toast(context, "${app.label} isn't installed.")
            is LaunchResult.Failed -> toast(context, "Couldn't open ${app.label}: ${result.reason ?: "unknown error"}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MianoColors.Background)
            .padding(horizontal = 48.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TopBar(mode = mode, onModeSelected = { mode = it })
        when (mode) {
            HouseholdMode.LivingRoom -> LivingRoom(tonight, watching, banners, onLaunch)
            else -> ModePlaceholder(mode)
        }
    }
}

@Composable
private fun TopBar(mode: HouseholdMode, onModeSelected: (HouseholdMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Row {
                Text("MIANO ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MianoColors.Text)
                Text("HOME", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MianoColors.Accent)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HouseholdMode.entries.forEach { m ->
                    ModePill(label = m.label, selected = m == mode, onClick = { onModeSelected(m) })
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(rememberClock(), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MianoColors.Text)
            Box(
                modifier = Modifier.size(32.dp).background(MianoColors.NeutralBg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("F", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MianoColors.Text)
            }
        }
    }
}

@Composable
private fun LivingRoom(
    tonight: TonightSummary?,
    watching: ContinueWatching?,
    banners: Map<String, ImageBitmap?>,
    onLaunch: (AppShortcut) -> Unit,
) {
    val firstTile = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { firstTile.requestFocus() } }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(262.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeroCard(watching, onLaunch, Modifier.weight(1f).fillMaxHeight())
            TonightCard(tonight, Modifier.width(270.dp).fillMaxHeight())
        }

        Text("Watch & play", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MianoColors.Text)
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
        ) {
            AppCatalog.featured.forEachIndexed { index, app ->
                AppTile(
                    app = app,
                    banner = banners[app.id],
                    onClick = { onLaunch(app) },
                    modifier = if (index == 0) Modifier.focusRequester(firstTile) else Modifier,
                )
            }
        }
    }
}

@Composable
private fun HeroCard(watching: ContinueWatching?, onLaunch: (AppShortcut) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(MianoColors.SurfaceRaised, RoundedCornerShape(14.dp))
            .padding(horizontal = 26.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        if (watching == null) {
            Text("Nothing to resume", fontSize = 22.sp, color = MianoColors.TextSecondary)
            return@Column
        }
        Text(
            "CONTINUE WATCHING · ${watching.appLabel.uppercase()}",
            fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, color = MianoColors.Accent,
        )
        Text(watching.title, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MianoColors.Text)
        Text(watching.detail, fontSize = 14.sp, color = MianoColors.TextSecondary)
        Spacer(Modifier.height(14.dp))
        val app = AppCatalog.byId(watching.appShortcutId)
        if (app != null) {
            // Fire TV apps don't expose a shared "resume" API; for now this opens the app.
            Button(onClick = { onLaunch(app) }) { Text("Open ${app.label}") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TonightCard(tonight: TonightSummary?, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(MianoColors.Surface, RoundedCornerShape(14.dp))
            .border(1.dp, MianoColors.Border, RoundedCornerShape(14.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Tonight", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MianoColors.Text)
        if (tonight == null) {
            Text("Loading…", fontSize = 13.sp, color = MianoColors.TextMuted)
            return@Column
        }
        tonight.nextEvent?.let { e ->
            SectionLabel("Next up")
            Text(e.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MianoColors.Text)
            Text("${e.whenLabel} · ${e.source}", fontSize = 12.sp, color = MianoColors.TextSecondary)
        }
        if (tonight.chores.isNotEmpty()) {
            SectionLabel("Chores left")
            tonight.chores.forEach { c ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(c.title, fontSize = 13.sp, color = MianoColors.Text)
                    Text(c.assignee, fontSize = 13.sp, color = MianoColors.TextMuted)
                }
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            tonight.status.forEach { StatusChip(it) }
        }
    }
}

@Composable
private fun ModePlaceholder(mode: HouseholdMode) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MianoColors.Surface, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "${mode.label} layout comes in a later phase. See the mockups.",
            fontSize = 16.sp,
            color = MianoColors.TextSecondary,
        )
    }
}

@Composable
private fun rememberClock(): String {
    val format = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    var now by remember { mutableStateOf(format.format(Date())) }
    LaunchedEffect(Unit) {
        while (true) {
            now = format.format(Date())
            delay(10_000)
        }
    }
    return now
}

private fun toast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}
