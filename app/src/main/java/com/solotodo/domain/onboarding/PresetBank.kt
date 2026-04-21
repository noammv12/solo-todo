package com.solotodo.domain.onboarding

import com.solotodo.data.local.DailyQuestTargetKind
import com.solotodo.data.local.StatKind
import com.solotodo.data.local.entity.DailyQuestItemEntity
import kotlinx.datetime.Instant

/**
 * The 9 Daily Quest presets surfaced during Awakening Step 3.
 *
 * User picks 3..5 of these during onboarding (retention override vs. the
 * prototype's 3..7). The first three are default-selected so a user who
 * taps COMMIT without altering selection still exits with a valid 3-item
 * daily quest. All presets default to a BOOLEAN target (tap-to-complete);
 * users upgrade individual items to count / duration in Protocol later.
 *
 * Order matters — it defines the display order in Step 3 and the
 * `order_index` written to Room on commit.
 */
data class Preset(
    val id: String,
    val title: String,
    val stat: StatKind,
    val xp: Int,
)

object PresetBank {

    val ALL: List<Preset> = listOf(
        Preset(ID_WATER,     "2 LITERS WATER",      StatKind.VIT, 3),
        Preset(ID_READ,      "READ 10 PAGES",       StatKind.SEN, 3),
        Preset(ID_WORKOUT,   "MORNING WORKOUT",     StatKind.STR, 3),
        Preset(ID_MEDITATE,  "MEDITATE 10 MIN",     StatKind.SEN, 3),
        Preset(ID_SLEEP,     "SLEEP BY 23:00",      StatKind.VIT, 3),
        Preset(ID_STRETCH,   "STRETCH 5 MIN",       StatKind.VIT, 2),
        Preset(ID_JOURNAL,   "JOURNAL 1 PAGE",      StatKind.SEN, 3),
        Preset(ID_WALK,      "WALK 30 MIN",         StatKind.STR, 3),
        Preset(ID_FOCUS,     "FOCUS BLOCK 25 MIN",  StatKind.INT, 3),
    )

    private val BY_ID: Map<String, Preset> = ALL.associateBy { it.id }

    /** Default pre-checked selection when entering Step 3 with a clean draft. */
    fun defaultThree(): List<String> = listOf(ID_WATER, ID_READ, ID_WORKOUT)

    fun byId(id: String): Preset? = BY_ID[id]

    /**
     * Build a [DailyQuestItemEntity] row for the given preset ID, ready to
     * upsert as part of the Awakening commit transaction. Target defaults to
     * BOOLEAN; `active=true`; ID is stable and human-readable so the same
     * preset keeps its identity across Replay Awakening.
     */
    fun buildEntity(
        presetId: String,
        orderIndex: Int,
        now: Instant,
        deviceId: String,
    ): DailyQuestItemEntity {
        val preset = byId(presetId)
            ?: error("Unknown preset id: $presetId")
        return DailyQuestItemEntity(
            id = presetId,
            title = preset.title,
            target = BOOLEAN_TARGET_JSON,
            stat = preset.stat,
            orderIndex = orderIndex,
            active = true,
            createdAt = now,
            updatedAt = now,
            originDeviceId = deviceId,
        )
    }

    // Stable IDs (match design doc copy + retention plan).
    const val ID_WATER = "preset_water_2l"
    const val ID_READ = "preset_read_10p"
    const val ID_WORKOUT = "preset_workout"
    const val ID_MEDITATE = "preset_meditate"
    const val ID_SLEEP = "preset_sleep_23"
    const val ID_STRETCH = "preset_stretch"
    const val ID_JOURNAL = "preset_journal"
    const val ID_WALK = "preset_walk_30"
    const val ID_FOCUS = "preset_focus_25"

    private const val BOOLEAN_TARGET_JSON = """{"kind":"${"boolean"}","value":1}"""

    // Retained only to make the enum reference compile-check; unused at runtime.
    @Suppress("unused") private val KIND_REF = DailyQuestTargetKind.BOOLEAN
}
