package com.solotodo.data.local

/**
 * Shared enum types used across Task, DailyQuestItem, etc.
 *
 * Persisted as string values matching the `CHECK(... IN(...))` constraints
 * declared in `data-state.html` §06 (sample migration).
 */

/** Stat axis. Every Daily Quest item requires one; Tasks optionally carry one. */
enum class StatKind { STR, INT, SEN, VIT }

/** Hunter rank. Ordered E (lowest) → S (highest). */
enum class Rank {
    E, D, C, B, A, S;

    /** Next rank in the progression, or `null` at S. */
    fun next(): Rank? = when (this) {
        E -> D
        D -> C
        C -> B
        B -> A
        A -> S
        S -> null
    }

    /** Previous rank, or `null` at E. */
    fun previous(): Rank? = when (this) {
        E -> null
        D -> E
        C -> D
        B -> C
        A -> B
        S -> A
    }
}

/** Target kind for a DailyQuestItem. `BOOLEAN` is just done/not-done. */
enum class DailyQuestTargetKind { COUNT, DURATION, BOOLEAN }

/** Daily Quest state machine states (ref: data-state.html §02). */
enum class DailyQuestState { DORMANT, ACTIVE, COMPLETE, EXPIRED, PENALTY, RECOVERED }

/** Dungeon floor states (ref: data-state.html §04). */
enum class FloorState { LOCKED, OPEN, CLEARING, CLEARED }

/** Op-log entry kind. */
enum class OpKind { CREATE, PATCH, DELETE }

/** Theme accent palette. */
enum class ThemeAccent { CYAN, GOLD, SHADOW }
