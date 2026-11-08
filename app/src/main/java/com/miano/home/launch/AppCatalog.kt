package com.miano.home.launch

/**
 * An app the home screen links to. The launcher tries each package name in order,
 * then falls back to matching the installed app's label, so a wrong or changed
 * package name still works.
 *
 * Package names marked VERIFY are best guesses for Fire TV. Confirm them on the
 * stick with scripts/list-packages.ps1 and update this list.
 */
data class AppShortcut(
    val id: String,
    val label: String,
    val packageCandidates: List<String>,
    val labelMatch: Regex,
)

object AppCatalog {
    val disneyPlus = AppShortcut(
        id = "disney",
        label = "Disney+",
        packageCandidates = listOf("com.disney.disneyplus"),
        labelMatch = Regex("disney", RegexOption.IGNORE_CASE),
    )

    val fubo = AppShortcut(
        id = "fubo",
        label = "Fubo",
        packageCandidates = listOf("com.fubo.firetv.screen", "tv.fubo.mobile"), // VERIFY
        labelMatch = Regex("fubo", RegexOption.IGNORE_CASE),
    )

    val youtube = AppShortcut(
        id = "youtube",
        label = "YouTube",
        packageCandidates = listOf("com.amazon.firetv.youtube", "com.google.android.youtube.tv"), // VERIFY
        labelMatch = Regex("^youtube$", RegexOption.IGNORE_CASE),
    )

    val retroArch = AppShortcut(
        id = "retro",
        label = "Retro Games",
        packageCandidates = listOf("com.retroarch.aarch64", "com.retroarch", "com.retroarch.ra32"),
        labelMatch = Regex("retroarch", RegexOption.IGNORE_CASE),
    )

    /** The "Watch & play" row, in order. */
    val featured = listOf(disneyPlus, fubo, youtube, retroArch)

    fun byId(id: String): AppShortcut? = featured.firstOrNull { it.id == id }
}
