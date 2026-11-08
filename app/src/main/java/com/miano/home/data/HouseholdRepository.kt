package com.miano.home.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

enum class HouseholdMode(val label: String) {
    LivingRoom("Living Room"),
    MorningBrief("Morning Brief"),
    FamilyTime("Family Time"),
    HomeManagement("Home Mgmt"),
}

data class CalendarEvent(val title: String, val whenLabel: String, val source: String)

data class Chore(val title: String, val assignee: String)

enum class StatusTone { Good, Neutral, Warning }

data class HomeStatus(val label: String, val tone: StatusTone)

data class TonightSummary(
    val nextEvent: CalendarEvent?,
    val chores: List<Chore>,
    val status: List<HomeStatus>,
)

data class ContinueWatching(
    val appShortcutId: String,
    val appLabel: String,
    val title: String,
    val detail: String,
)

/**
 * Where the TV gets household data. The home server is the source of truth;
 * the TV only reads and displays it (thin client).
 */
interface HouseholdRepository {
    fun tonight(): Flow<TonightSummary>
    fun continueWatching(): Flow<ContinueWatching?>
}

/** Fixed sample data so the UI runs before the household API exists. */
class SampleHouseholdRepository : HouseholdRepository {

    override fun tonight(): Flow<TonightSummary> = flowOf(
        TonightSummary(
            nextEvent = CalendarEvent("Soccer practice pickup", "Tomorrow 8:00 AM", "Family calendar"),
            chores = listOf(
                Chore("Load dishwasher", "Anyone"),
                Chore("Take out recycling", "[Name]"),
            ),
            status = listOf(
                HomeStatus("Doors locked", StatusTone.Good),
                HomeStatus("Inside 68°F", StatusTone.Neutral),
                HomeStatus("HVAC filter due", StatusTone.Warning),
            ),
        ),
    )

    override fun continueWatching(): Flow<ContinueWatching?> = flowOf(
        ContinueWatching(
            appShortcutId = "disney",
            appLabel = "Disney+",
            title = "[Show title]",
            detail = "Pick up where you left off",
        ),
    )
}
