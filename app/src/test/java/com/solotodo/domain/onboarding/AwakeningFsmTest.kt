package com.solotodo.domain.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AwakeningFsmTest {

    private fun step(from: AwakeningState, event: AwakeningEvent) =
        AwakeningFsm.transition(from, event)

    // ── Happy path ──────────────────────────────────────────────────────────

    @Test fun `black + awaken → designate`() {
        assertEquals(
            AwakeningState.Designate,
            step(AwakeningState.Black, AwakeningEvent.AwakenTapped),
        )
    }

    @Test fun `designate submitted → dailyQuest`() {
        assertEquals(
            AwakeningState.DailyQuest,
            step(AwakeningState.Designate, AwakeningEvent.DesignationSubmitted("SHADOW")),
        )
    }

    @Test fun `designate skipped → dailyQuest`() {
        assertEquals(
            AwakeningState.DailyQuest,
            step(AwakeningState.Designate, AwakeningEvent.DesignationSkipped),
        )
    }

    @Test fun `dailyQuest committed with 3 ids → firstQuest`() {
        assertEquals(
            AwakeningState.FirstQuest,
            step(
                AwakeningState.DailyQuest,
                AwakeningEvent.DailyQuestCommitted(listOf("a", "b", "c")),
            ),
        )
    }

    @Test fun `dailyQuest committed with 5 ids → firstQuest`() {
        assertEquals(
            AwakeningState.FirstQuest,
            step(
                AwakeningState.DailyQuest,
                AwakeningEvent.DailyQuestCommitted(listOf("a", "b", "c", "d", "e")),
            ),
        )
    }

    @Test fun `firstQuest captured → permissions`() {
        assertEquals(
            AwakeningState.Permissions,
            step(AwakeningState.FirstQuest, AwakeningEvent.FirstQuestCaptured("buy milk")),
        )
    }

    @Test fun `firstQuest skipped → permissions`() {
        assertEquals(
            AwakeningState.Permissions,
            step(AwakeningState.FirstQuest, AwakeningEvent.FirstQuestSkipped),
        )
    }

    @Test fun `permissions resolved allowed → complete`() {
        assertEquals(
            AwakeningState.Complete,
            step(AwakeningState.Permissions, AwakeningEvent.PermissionsResolved(allowed = true)),
        )
    }

    @Test fun `permissions resolved denied → complete`() {
        assertEquals(
            AwakeningState.Complete,
            step(AwakeningState.Permissions, AwakeningEvent.PermissionsResolved(allowed = false)),
        )
    }

    // ── Invalid DailyQuest counts are no-ops ────────────────────────────────

    @Test fun `dailyQuest committed with 2 ids → no-op`() {
        val s = AwakeningState.DailyQuest
        assertSame(s, step(s, AwakeningEvent.DailyQuestCommitted(listOf("a", "b"))))
    }

    @Test fun `dailyQuest committed with 6 ids → no-op`() {
        val s = AwakeningState.DailyQuest
        assertSame(s, step(s, AwakeningEvent.DailyQuestCommitted(listOf("a", "b", "c", "d", "e", "f"))))
    }

    @Test fun `dailyQuest committed empty → no-op`() {
        val s = AwakeningState.DailyQuest
        assertSame(s, step(s, AwakeningEvent.DailyQuestCommitted(emptyList())))
    }

    // ── Wrong-event-for-state guards (pick a few representative) ───────────

    @Test fun `black + any non-awaken → no-op`() {
        val s = AwakeningState.Black
        assertSame(s, step(s, AwakeningEvent.DesignationSkipped))
        assertSame(s, step(s, AwakeningEvent.DailyQuestCommitted(listOf("a", "b", "c"))))
        assertSame(s, step(s, AwakeningEvent.PermissionsResolved(allowed = true)))
    }

    @Test fun `complete is absorbing`() {
        val s = AwakeningState.Complete
        assertSame(s, step(s, AwakeningEvent.AwakenTapped))
        assertSame(s, step(s, AwakeningEvent.PermissionsResolved(allowed = false)))
    }

    // ── Resume-from-draft seeding ──────────────────────────────────────────

    @Test fun `resumeFrom empty draft → black`() {
        assertEquals(
            AwakeningState.Black,
            AwakeningFsm.resumeFrom(
                hasDesignation = false,
                committedSelection = false,
                hasFirstQuestRaw = false,
            ),
        )
    }

    @Test fun `resumeFrom with designation only → dailyQuest`() {
        assertEquals(
            AwakeningState.DailyQuest,
            AwakeningFsm.resumeFrom(
                hasDesignation = true,
                committedSelection = false,
                hasFirstQuestRaw = false,
            ),
        )
    }

    @Test fun `resumeFrom with committed selection → firstQuest`() {
        assertEquals(
            AwakeningState.FirstQuest,
            AwakeningFsm.resumeFrom(
                hasDesignation = true,
                committedSelection = true,
                hasFirstQuestRaw = false,
            ),
        )
    }

    @Test fun `resumeFrom with firstQuest raw typed → firstQuest`() {
        assertEquals(
            AwakeningState.FirstQuest,
            AwakeningFsm.resumeFrom(
                hasDesignation = true,
                committedSelection = true,
                hasFirstQuestRaw = true,
            ),
        )
    }
}
