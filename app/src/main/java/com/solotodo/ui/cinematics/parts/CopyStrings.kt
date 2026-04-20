package com.solotodo.ui.cinematics.parts

import com.solotodo.data.local.Rank

/**
 * Rank-up copy strings, verbatim from `copy.html` §06.
 *
 * Reduce-Motion variant collapses to a single line — spec: "Rank advanced. {R}."
 * rendered uppercase to match the System voice.
 */
object CopyStrings {
    data class Copy(val kicker: String?, val title: String, val subtitle: String?)

    fun forRank(rank: Rank, reduceMotion: Boolean): Copy {
        if (reduceMotion) return Copy(kicker = null, title = "RANK ADVANCED. ${rank.name}.", subtitle = null)
        return when (rank) {
            Rank.D -> Copy(null, "RANK UP · D", "THE SYSTEM NOTICES.")
            Rank.C -> Copy(null, "RANK UP · C", "THE HUNTER PERSISTS.")
            Rank.B -> Copy(null, "RANK UP · B", "PROVEN.")
            Rank.A -> Copy(null, "RANK UP · A", "FEW REACH THIS DEPTH.")
            Rank.S -> Copy("ARISE", "MONARCH", null)
            Rank.E -> Copy(null, "RANK UP · E", null) // unreachable in practice
        }
    }
}
