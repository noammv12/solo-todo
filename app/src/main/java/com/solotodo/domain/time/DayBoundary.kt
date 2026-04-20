package com.solotodo.domain.time

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User-local midnight boundary math for the Daily Quest tick.
 *
 * Rationale (from `data-state.html` §02 and plan overrides):
 *  - Ticks fire at `00:00` in the user's IANA timezone.
 *  - DST spring-forward (2→3 AM gap): midnight unaffected.
 *  - DST fall-back (1 AM twice): the first pass is the real one; idempotent
 *    ticks make the second a no-op.
 *  - Travel: use the timezone *active at tick time*, not at log creation,
 *    unless UserSettings.anchorDailyToOriginalTimezone is on (not yet in spec).
 */
@Singleton
class DayBoundary @Inject constructor(
    private val zone: TimeZone,
    private val clock: Clock,
) {
    /** Current local date (what `today` means to the user right now). */
    fun today(): LocalDate = clock.now().toLocalDateTime(zone).date

    /** Local date for an arbitrary [instant]. */
    fun localDate(instant: Instant): LocalDate = instant.toLocalDateTime(zone).date

    /** Midnight at the start of [date] in the user's zone, as a UTC [Instant]. */
    fun startOfDay(date: LocalDate): Instant = date.atStartOfDayIn(zone)

    /** Midnight at the start of the *next* day — useful as an exclusive upper bound. */
    fun startOfNextDay(date: LocalDate): Instant =
        date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(zone)

    /** `[startOfToday, startOfTomorrow)` window. */
    fun todayWindow(): Pair<Instant, Instant> {
        val today = today()
        return startOfDay(today) to startOfNextDay(today)
    }

    /** True when [a] and [b] fall on the same local calendar day. */
    fun isSameLocalDay(a: Instant, b: Instant): Boolean = localDate(a) == localDate(b)

    /**
     * Previous local day — note DST is handled by kotlinx-datetime's own
     * `date.minus(1, DAY)` which works at the date level regardless of hour.
     */
    fun yesterday(): LocalDate = today().minus(1, DateTimeUnit.DAY)

    /** Next midnight after [instant] in the user's zone. */
    fun nextMidnight(instant: Instant = clock.now()): Instant {
        val todayDate = instant.toLocalDateTime(zone).date
        return LocalDateTime(todayDate.plus(1, DateTimeUnit.DAY), LocalTime(0, 0)).toInstant(zone)
    }
}
