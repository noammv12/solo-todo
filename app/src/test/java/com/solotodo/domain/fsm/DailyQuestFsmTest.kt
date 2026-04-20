package com.solotodo.domain.fsm

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyQuestFsmTest {

    private fun ctx(
        state: DailyQuestFsmState = DailyQuestFsmState.DORMANT,
        stack: Int = 0,
        vacation: Boolean = false,
    ) = DailyQuestContext(state, stack, vacation)

    @Test fun `DORMANT to ACTIVE on midnight`() {
        val r = DailyQuestFsm.reduce(ctx(), DailyQuestEvent.TickMidnight)
        assertEquals(DailyQuestFsmState.ACTIVE, r.state)
    }

    @Test fun `ACTIVE all_done leads to COMPLETE`() {
        val r = DailyQuestFsm.reduce(ctx(DailyQuestFsmState.ACTIVE), DailyQuestEvent.AllDone)
        assertEquals(DailyQuestFsmState.COMPLETE, r.state)
    }

    @Test fun `COMPLETE rolls to ACTIVE on midnight`() {
        val r = DailyQuestFsm.reduce(ctx(DailyQuestFsmState.COMPLETE), DailyQuestEvent.TickMidnight)
        assertEquals(DailyQuestFsmState.ACTIVE, r.state)
    }

    @Test fun `ACTIVE with incomplete midnight hits EXPIRED`() {
        val r = DailyQuestFsm.reduce(ctx(DailyQuestFsmState.ACTIVE), DailyQuestEvent.TickMidnight)
        assertEquals(DailyQuestFsmState.EXPIRED, r.state)
    }

    @Test fun `EXPIRED acknowledged goes to PENALTY`() {
        val r = DailyQuestFsm.reduce(ctx(DailyQuestFsmState.EXPIRED), DailyQuestEvent.AckPenaltyModal)
        assertEquals(DailyQuestFsmState.PENALTY, r.state)
    }

    @Test fun `PENALTY recovered on all_done_with_penalty`() {
        val r = DailyQuestFsm.reduce(ctx(DailyQuestFsmState.PENALTY), DailyQuestEvent.AllDoneWithPenalty)
        assertEquals(DailyQuestFsmState.RECOVERED, r.state)
        assertEquals(0, r.penaltyStack)
    }

    @Test fun `PENALTY stacks on missed midnight and caps at 3`() {
        var c = ctx(DailyQuestFsmState.PENALTY)
        repeat(5) { c = DailyQuestFsm.reduce(c, DailyQuestEvent.TickMidnight) }
        assertEquals(DailyQuestFsm.MAX_PENALTY_STACK, c.penaltyStack)
        assertEquals(DailyQuestFsmState.PENALTY, c.state)
    }

    @Test fun `vacation_on short circuits to DORMANT`() {
        val r = DailyQuestFsm.reduce(ctx(DailyQuestFsmState.ACTIVE), DailyQuestEvent.VacationOn)
        assertEquals(DailyQuestFsmState.DORMANT, r.state)
        assertEquals(true, r.onVacation)
    }

    @Test fun `ticks ignored while on vacation`() {
        val r = DailyQuestFsm.reduce(ctx(DailyQuestFsmState.ACTIVE, vacation = true), DailyQuestEvent.TickMidnight)
        assertEquals(DailyQuestFsmState.DORMANT, r.state)
    }

    @Test fun `vacation_off returns to ACTIVE`() {
        val r = DailyQuestFsm.reduce(ctx(DailyQuestFsmState.DORMANT, vacation = true), DailyQuestEvent.VacationOff)
        assertEquals(DailyQuestFsmState.ACTIVE, r.state)
        assertEquals(false, r.onVacation)
    }
}
