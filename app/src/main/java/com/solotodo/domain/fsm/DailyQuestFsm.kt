package com.solotodo.domain.fsm

/**
 * Daily Quest state machine — pure logic (no Android, no coroutines, no IO).
 *
 * States + transitions mirror `data-state.html` §02 plus our plan override
 * (rank demotion disabled; penalty stacking >3 is silently capped to 3
 * instead of triggering a demotion cinematic).
 *
 * The real-world driver is a WorkManager tick that fires at user-local
 * midnight and calls [tickMidnight] with the world state it observes. On
 * app foreground, the app can replay missed ticks by feeding the latest
 * state to [tickMidnight] repeatedly.
 */
enum class DailyQuestFsmState { DORMANT, ACTIVE, COMPLETE, EXPIRED, PENALTY, RECOVERED }

data class DailyQuestContext(
    val state: DailyQuestFsmState,
    /** How many midnight ticks the user has missed while in PENALTY state. Cap 3. */
    val penaltyStack: Int,
    /** True if user has `vacation_until` set and it hasn't passed yet. */
    val onVacation: Boolean,
)

/** Events that drive the FSM. Guards live inside the transition functions. */
sealed class DailyQuestEvent {
    /** Midnight tick fired. */
    data object TickMidnight : DailyQuestEvent()
    /** User finished all today's DQ items. */
    data object AllDone : DailyQuestEvent()
    /** User acknowledged the penalty modal. */
    data object AckPenaltyModal : DailyQuestEvent()
    /** User finished all DQ items **and** the injected penalty task. */
    data object AllDoneWithPenalty : DailyQuestEvent()
    /** User enabled vacation mode. */
    data object VacationOn : DailyQuestEvent()
    /** Vacation period ended — resume normal ticking. */
    data object VacationOff : DailyQuestEvent()
}

object DailyQuestFsm {
    const val MAX_PENALTY_STACK = 3

    fun reduce(ctx: DailyQuestContext, event: DailyQuestEvent): DailyQuestContext {
        // Vacation short-circuit: any event while on vacation keeps us DORMANT.
        if (ctx.onVacation && event !is DailyQuestEvent.VacationOff) {
            return ctx.copy(state = DailyQuestFsmState.DORMANT)
        }

        return when (event) {
            DailyQuestEvent.VacationOn -> ctx.copy(state = DailyQuestFsmState.DORMANT, onVacation = true)
            DailyQuestEvent.VacationOff -> ctx.copy(state = DailyQuestFsmState.ACTIVE, onVacation = false)

            DailyQuestEvent.TickMidnight -> when (ctx.state) {
                DailyQuestFsmState.DORMANT -> ctx.copy(state = DailyQuestFsmState.ACTIVE)
                DailyQuestFsmState.ACTIVE -> ctx.copy(state = DailyQuestFsmState.EXPIRED)
                DailyQuestFsmState.COMPLETE -> ctx.copy(state = DailyQuestFsmState.ACTIVE)
                DailyQuestFsmState.EXPIRED -> ctx.copy(state = DailyQuestFsmState.EXPIRED) // still needs ack
                DailyQuestFsmState.PENALTY -> ctx.copy(
                    penaltyStack = (ctx.penaltyStack + 1).coerceAtMost(MAX_PENALTY_STACK),
                )
                DailyQuestFsmState.RECOVERED -> ctx.copy(state = DailyQuestFsmState.ACTIVE)
            }

            DailyQuestEvent.AllDone -> when (ctx.state) {
                DailyQuestFsmState.ACTIVE -> ctx.copy(state = DailyQuestFsmState.COMPLETE)
                else -> ctx
            }

            DailyQuestEvent.AckPenaltyModal -> when (ctx.state) {
                DailyQuestFsmState.EXPIRED -> ctx.copy(state = DailyQuestFsmState.PENALTY)
                else -> ctx
            }

            DailyQuestEvent.AllDoneWithPenalty -> when (ctx.state) {
                DailyQuestFsmState.PENALTY -> ctx.copy(
                    state = DailyQuestFsmState.RECOVERED,
                    penaltyStack = 0,
                )
                else -> ctx
            }
        }
    }
}
