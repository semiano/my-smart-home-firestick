package com.miano.home.launch

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri

sealed interface LaunchResult {
    data object Launched : LaunchResult
    data object OpenedStore : LaunchResult
    data object NotInstalled : LaunchResult
    data class Failed(val reason: String?) : LaunchResult
}

data class InstalledApp(val packageName: String, val label: String)

/**
 * Finds and opens the official installed apps (Disney+, Fubo, YouTube, RetroArch...).
 * Nothing is rebuilt or wrapped: we just ask Android to start the app's own TV entry point.
 */
class AppLauncher(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    /** Every app that has a TV (leanback) entry point, except this launcher. */
    fun installedTvApps(): List<InstalledApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .map { InstalledApp(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    /** The installed package for a shortcut, or null if it isn't installed. */
    fun resolve(shortcut: AppShortcut): String? {
        shortcut.packageCandidates.firstOrNull { launchIntentFor(it) != null }?.let { return it }
        return installedTvApps()
            .firstOrNull { shortcut.labelMatch.containsMatchIn(it.label) }
            ?.packageName
    }

    fun launch(shortcut: AppShortcut): LaunchResult {
        val pkg = resolve(shortcut) ?: return openStore(shortcut.packageCandidates.first())
        return launchPackage(pkg)
    }

    fun launchPackage(pkg: String): LaunchResult {
        val intent = launchIntentFor(pkg) ?: return LaunchResult.NotInstalled
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return try {
            context.startActivity(intent)
            LaunchResult.Launched
        } catch (e: ActivityNotFoundException) {
            LaunchResult.Failed(e.message)
        } catch (e: SecurityException) {
            LaunchResult.Failed(e.message)
        }
    }

    /** The app's own TV banner (320x180) if it has one, else its icon. */
    fun bannerFor(shortcut: AppShortcut): Drawable? {
        val pkg = resolve(shortcut) ?: return null
        return try {
            pm.getApplicationBanner(pkg) ?: pm.getApplicationIcon(pkg)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun launchIntentFor(pkg: String): Intent? =
        pm.getLeanbackLaunchIntentForPackage(pkg) ?: pm.getLaunchIntentForPackage(pkg)

    /** Opens the Amazon Appstore page for a missing app. */
    private fun openStore(pkg: String): LaunchResult {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("amzn://apps/android?p=$pkg"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            LaunchResult.OpenedStore
        } catch (e: ActivityNotFoundException) {
            LaunchResult.NotInstalled
        }
    }
}
