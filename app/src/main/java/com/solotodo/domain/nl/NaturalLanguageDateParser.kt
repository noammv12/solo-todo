package com.solotodo.domain.nl

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Small natural-language parser for task entry. Covers the token grammar in
 * `interactions.html` §NL:
 *
 *  - **date**: today, tomorrow, tonight, yesterday, this/next weekday,
 *    "in N days/weeks/months", mon–sun
 *  - **time**: morning/noon/afternoon/evening/night, "h am/pm", "hh:mm"
 *  - **stat**: `@str @int @sen @vit`
 *  - **list**: `#work #personal #custom`
 *  - **priority**: `!!!` (critical) / `!!` (high) / `!` (normal)
 *
 * Unrecognised tokens stay in the title verbatim. The parser is greedy-longest
 * for date phrases and tolerant of mixed case.
 */
class NaturalLanguageDateParser(
    private val clock: Clock = Clock.System,
    private val zone: TimeZone = TimeZone.currentSystemDefault(),
) {
    data class Parse(
        val title: String,
        val dueAt: kotlinx.datetime.Instant? = null,
        val stat: String? = null,
        val list: String? = null,
        val priority: Int = 0,
        val tokensFound: List<Token> = emptyList(),
    )

    /** Parsed token with the source substring, so the UI can highlight chips. */
    data class Token(val kind: Kind, val value: String, val raw: String) {
        enum class Kind { DATE, TIME, STAT, LIST, PRIORITY }
    }

    fun parse(input: String): Parse {
        var working = input.trim()
        val tokens = mutableListOf<Token>()

        val stat = extractPrefixed(working, '@')?.also {
            working = working.replace(it.raw, " ")
            tokens += Token(Token.Kind.STAT, it.value, it.raw)
        }
        val list = extractPrefixed(working, '#')?.also {
            working = working.replace(it.raw, " ")
            tokens += Token(Token.Kind.LIST, it.value, it.raw)
        }

        val priority = extractPriority(working)?.also {
            working = working.replace(it.raw, " ")
            tokens += Token(Token.Kind.PRIORITY, it.value, it.raw)
        }

        val date = extractDate(working)?.also {
            working = working.replace(it.raw, " ")
            tokens += Token(Token.Kind.DATE, it.value, it.raw)
        }
        val time = extractTime(working)?.also {
            working = working.replace(it.raw, " ")
            tokens += Token(Token.Kind.TIME, it.value, it.raw)
        }

        val title = working.split(Regex("\\s+")).filter { it.isNotBlank() }.joinToString(" ")

        val localTime = time?.parsedTime ?: LocalTime(9, 0) // default target: 9:00 am
        val dueInstant = date?.parsedDate?.let { LocalDateTime(it, localTime).toInstant(zone) }

        return Parse(
            title = title,
            dueAt = dueInstant,
            stat = stat?.value,
            list = list?.value,
            priority = priority?.intValue ?: 0,
            tokensFound = tokens,
        )
    }

    // ─── extractors ───

    private data class PrefixHit(val value: String, val raw: String)
    private fun extractPrefixed(src: String, prefix: Char): PrefixHit? {
        val m = Regex("""(?:^|\s)($prefix([A-Za-z][A-Za-z0-9_-]{0,32}))(?=\s|$)""").find(src) ?: return null
        return PrefixHit(value = m.groupValues[2].lowercase(), raw = m.groupValues[1])
    }

    private data class PriorityHit(val intValue: Int, val value: String, val raw: String)
    private fun extractPriority(src: String): PriorityHit? {
        val m = Regex("""(?:^|\s)(!!!|!!|!)(?=\s|$)""").find(src) ?: return null
        val bangs = m.groupValues[1]
        val lvl = when (bangs) { "!!!" -> 2; "!!" -> 1; else -> 0 }
        return PriorityHit(intValue = lvl, value = bangs, raw = bangs)
    }

    private data class DateHit(val parsedDate: LocalDate, val value: String, val raw: String)
    private fun extractDate(src: String): DateHit? {
        val today = clock.now().toLocalDateTime(zone).date

        // "in N days|weeks|months"
        val relative = Regex("""(?:^|\s)(in\s+(\d{1,3})\s+(day|days|week|weeks|month|months))(?=\s|$)""", RegexOption.IGNORE_CASE)
            .find(src)
        if (relative != null) {
            val n = relative.groupValues[2].toInt()
            val unit = relative.groupValues[3].lowercase()
            val date = when {
                unit.startsWith("day") -> today.plus(n, DateTimeUnit.DAY)
                unit.startsWith("week") -> today.plus(n * 7, DateTimeUnit.DAY)
                unit.startsWith("month") -> today.plus(n, DateTimeUnit.MONTH)
                else -> today
            }
            return DateHit(parsedDate = date, value = relative.groupValues[1], raw = relative.groupValues[1])
        }

        val simple = Regex("""(?:^|\s)(today|tomorrow|tonight|yesterday)(?=\s|$)""", RegexOption.IGNORE_CASE).find(src)
        if (simple != null) {
            val word = simple.groupValues[1].lowercase()
            val date = when (word) {
                "today", "tonight" -> today
                "tomorrow" -> today.plus(1, DateTimeUnit.DAY)
                "yesterday" -> today.plus(-1, DateTimeUnit.DAY)
                else -> today
            }
            return DateHit(parsedDate = date, value = word, raw = simple.groupValues[1])
        }

        val wk = Regex("""(?:^|\s)((?:this|next)\s+)?(mon|tue|wed|thu|fri|sat|sun)(?:day|sday|nesday|rsday|urday)?(?=\s|$)""", RegexOption.IGNORE_CASE).find(src)
        if (wk != null) {
            val qual = wk.groupValues[1].trim().lowercase()
            val dow = parseDayOfWeek(wk.groupValues[2]) ?: return null
            val date = nextOrThisDayOfWeek(today, dow, forceNext = qual == "next")
            return DateHit(parsedDate = date, value = (qual.ifBlank { "this" } + " " + dow.name.lowercase()).trim(), raw = wk.groupValues[0].trim())
        }

        return null
    }

    private data class TimeHit(val parsedTime: LocalTime, val value: String, val raw: String)
    private fun extractTime(src: String): TimeHit? {
        val keyword = Regex("""(?:^|\s)(morning|noon|afternoon|evening|night)(?=\s|$)""", RegexOption.IGNORE_CASE).find(src)
        if (keyword != null) {
            val word = keyword.groupValues[1].lowercase()
            val t = when (word) {
                "morning" -> LocalTime(8, 0)
                "noon" -> LocalTime(12, 0)
                "afternoon" -> LocalTime(14, 0)
                "evening" -> LocalTime(19, 0)
                "night" -> LocalTime(22, 0)
                else -> LocalTime(9, 0)
            }
            return TimeHit(parsedTime = t, value = word, raw = keyword.groupValues[1])
        }

        val explicit = Regex("""(?:^|\s)(\d{1,2})(?::(\d{2}))?\s*(am|pm)(?=\s|$)""", RegexOption.IGNORE_CASE).find(src)
        if (explicit != null) {
            val hour12 = explicit.groupValues[1].toInt().coerceIn(1, 12)
            val minute = explicit.groupValues[2].toIntOrNull() ?: 0
            val ampm = explicit.groupValues[3].lowercase()
            val hour24 = when {
                ampm == "am" && hour12 == 12 -> 0
                ampm == "am" -> hour12
                ampm == "pm" && hour12 == 12 -> 12
                else -> hour12 + 12
            }
            val t = LocalTime(hour24, minute)
            return TimeHit(parsedTime = t, value = "$hour12${if (minute > 0) ":$minute" else ""}$ampm", raw = explicit.groupValues[0].trim())
        }

        val hhmm = Regex("""(?:^|\s)(\d{1,2}):(\d{2})(?=\s|$)""").find(src)
        if (hhmm != null) {
            val h = hhmm.groupValues[1].toInt().coerceIn(0, 23)
            val m = hhmm.groupValues[2].toInt().coerceIn(0, 59)
            return TimeHit(parsedTime = LocalTime(h, m), value = "${h}:${"%02d".format(m)}", raw = hhmm.groupValues[0].trim())
        }

        return null
    }

    // ─── helpers ───

    private fun parseDayOfWeek(s: String): DayOfWeek? = when (s.lowercase().take(3)) {
        "mon" -> DayOfWeek.MONDAY
        "tue" -> DayOfWeek.TUESDAY
        "wed" -> DayOfWeek.WEDNESDAY
        "thu" -> DayOfWeek.THURSDAY
        "fri" -> DayOfWeek.FRIDAY
        "sat" -> DayOfWeek.SATURDAY
        "sun" -> DayOfWeek.SUNDAY
        else -> null
    }

    private fun nextOrThisDayOfWeek(today: LocalDate, target: DayOfWeek, forceNext: Boolean): LocalDate {
        var daysAhead = (target.ordinal - today.dayOfWeek.ordinal + 7) % 7
        if (daysAhead == 0 && forceNext) daysAhead = 7
        return today.plus(daysAhead, DateTimeUnit.DAY)
    }
}
