package com.solotodo.data.onboarding

import com.solotodo.data.local.SoloTodoDb
import com.solotodo.data.local.StatKind
import com.solotodo.data.notifications.NotificationPrefs
import com.solotodo.data.repository.DailyQuestRepository
import com.solotodo.data.repository.SettingsRepository
import com.solotodo.data.repository.TaskRepository
import com.solotodo.domain.nl.NaturalLanguageDateParser
import com.solotodo.domain.onboarding.PresetBank
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test

/**
 * Verifies [OnboardingCommitter.commitBody] — the pure orchestration logic
 * extracted from the public `commit()` so it's testable without trying to
 * mock Room's `withTransaction` extension (which can't be reliably stubbed
 * with mockk).
 *
 * End-to-end Room atomicity is covered by the manual smoke in the plan's
 * §Stage 15.
 */
class OnboardingCommitterTest {

    private val db: SoloTodoDb = mockk(relaxed = true)
    private val settings: SettingsRepository = mockk(relaxed = true)
    private val dqRepo: DailyQuestRepository = mockk(relaxed = true)
    private val taskRepo: TaskRepository = mockk(relaxed = true)
    private val nlParser: NaturalLanguageDateParser = mockk()
    private val fixedNow = Instant.parse("2026-04-21T12:00:00Z")
    private val clock = object : Clock {
        override fun now(): Instant = fixedNow
    }

    private lateinit var committer: OnboardingCommitter

    @Before fun setUp() {
        coEvery { taskRepo.create(any(), any(), any(), any(), any(), any(), any()) } returns "task-id"
        committer = OnboardingCommitter(db, settings, dqRepo, taskRepo, nlParser, clock)
    }

    @Test fun `commit with full draft + ALLOW writes settings + items + task in order`() = runTest {
        val parsed = NaturalLanguageDateParser.Parse(
            title = "book flight to Seoul",
            dueAt = Instant.parse("2026-04-24T15:00:00Z"),
            stat = "INT",
            list = "travel",
            priority = 1,
        )
        every { nlParser.parse("Book flight to Seoul friday 3pm @int #travel !!") } returns parsed

        val draft = AwakeningDraft(
            designation = "SHADOW",
            selectedPresetIds = listOf(
                PresetBank.ID_WATER,
                PresetBank.ID_READ,
                PresetBank.ID_WORKOUT,
                PresetBank.ID_FOCUS,
            ),
            firstQuestRaw = "Book flight to Seoul friday 3pm @int #travel !!",
        )

        committer.commitBody(draft, notificationsAllowed = true)

        coVerifyOrder {
            settings.finishAwakening(
                designation = "SHADOW",
                dailyQuestCount = 4,
                notificationsJson = NotificationPrefs.Channels.ON.toJson(),
                now = fixedNow,
            )
            dqRepo.replaceActiveItemsFromPresets(
                listOf(
                    PresetBank.ID_WATER,
                    PresetBank.ID_READ,
                    PresetBank.ID_WORKOUT,
                    PresetBank.ID_FOCUS,
                ),
                fixedNow,
            )
            taskRepo.create(
                title = "book flight to Seoul",
                rawInput = "Book flight to Seoul friday 3pm @int #travel !!",
                dueAt = Instant.parse("2026-04-24T15:00:00Z"),
                stat = StatKind.INT,
                listId = "travel",
                priority = 1,
                xp = 0,
            )
        }
    }

    @Test fun `commit with DECLINE writes OFF notifications json`() = runTest {
        val draft = AwakeningDraft(
            designation = "SHADOW",
            selectedPresetIds = listOf(PresetBank.ID_WATER, PresetBank.ID_READ, PresetBank.ID_WORKOUT),
            firstQuestRaw = null,
        )
        committer.commitBody(draft, notificationsAllowed = false)
        coVerify {
            settings.finishAwakening(
                designation = "SHADOW",
                dailyQuestCount = 3,
                notificationsJson = NotificationPrefs.Channels.OFF.toJson(),
                now = fixedNow,
            )
        }
    }

    @Test fun `no first quest typed → no task created`() = runTest {
        val draft = AwakeningDraft(
            designation = "SHADOW",
            selectedPresetIds = listOf(PresetBank.ID_WATER, PresetBank.ID_READ, PresetBank.ID_WORKOUT),
            firstQuestRaw = null,
        )
        committer.commitBody(draft, notificationsAllowed = false)
        coVerify(exactly = 0) { taskRepo.create(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test fun `whitespace-only first quest → no task created`() = runTest {
        val draft = AwakeningDraft(
            designation = "SHADOW",
            selectedPresetIds = listOf(PresetBank.ID_WATER, PresetBank.ID_READ, PresetBank.ID_WORKOUT),
            firstQuestRaw = "   \t  ",
        )
        committer.commitBody(draft, notificationsAllowed = false)
        coVerify(exactly = 0) { taskRepo.create(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test fun `null designation defaults to HUNTER`() = runTest {
        val draft = AwakeningDraft(
            designation = null,
            selectedPresetIds = listOf(PresetBank.ID_WATER, PresetBank.ID_READ, PresetBank.ID_WORKOUT),
        )
        committer.commitBody(draft, notificationsAllowed = false)
        coVerify {
            settings.finishAwakening(
                designation = "HUNTER",
                dailyQuestCount = 3,
                notificationsJson = any(),
                now = fixedNow,
            )
        }
    }

    @Test fun `too-short designation defaults to HUNTER`() = runTest {
        val draft = AwakeningDraft(
            designation = "A",
            selectedPresetIds = listOf(PresetBank.ID_WATER, PresetBank.ID_READ, PresetBank.ID_WORKOUT),
        )
        committer.commitBody(draft, notificationsAllowed = false)
        coVerify {
            settings.finishAwakening(
                designation = "HUNTER",
                dailyQuestCount = 3,
                notificationsJson = any(),
                now = fixedNow,
            )
        }
    }

    @Test fun `empty preset list defaults to PresetBank defaultThree`() = runTest {
        val draft = AwakeningDraft(
            designation = "SHADOW",
            selectedPresetIds = emptyList(),
        )
        committer.commitBody(draft, notificationsAllowed = false)
        coVerify {
            dqRepo.replaceActiveItemsFromPresets(PresetBank.defaultThree(), fixedNow)
            settings.finishAwakening(
                designation = "SHADOW",
                dailyQuestCount = 3,
                notificationsJson = any(),
                now = fixedNow,
            )
        }
    }

    @Test fun `unknown preset ids are filtered out`() = runTest {
        val draft = AwakeningDraft(
            designation = "SHADOW",
            selectedPresetIds = listOf(
                PresetBank.ID_WATER,
                "bogus_preset",
                PresetBank.ID_READ,
                PresetBank.ID_WORKOUT,
            ),
        )
        committer.commitBody(draft, notificationsAllowed = false)
        coVerify {
            dqRepo.replaceActiveItemsFromPresets(
                listOf(PresetBank.ID_WATER, PresetBank.ID_READ, PresetBank.ID_WORKOUT),
                fixedNow,
            )
        }
    }

    @Test fun `over-5 preset list is truncated to 5`() = runTest {
        val draft = AwakeningDraft(
            designation = "SHADOW",
            selectedPresetIds = PresetBank.ALL.take(6).map { it.id },
        )
        committer.commitBody(draft, notificationsAllowed = false)
        coVerify {
            dqRepo.replaceActiveItemsFromPresets(
                PresetBank.ALL.take(5).map { it.id },
                fixedNow,
            )
        }
    }
}
