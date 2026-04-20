package com.solotodo.data.local

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Room type converters. All stored as SQLite-friendly primitives:
 *  - Instant → Long (unix millis)
 *  - LocalDate → String (ISO yyyy-mm-dd)
 *  - Enums → String (names)
 */
class Converters {

    @TypeConverter fun instantToMillis(value: Instant?): Long? = value?.toEpochMilliseconds()
    @TypeConverter fun millisToInstant(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter fun localDateToString(value: LocalDate?): String? = value?.toString()
    @TypeConverter fun stringToLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter fun statKindToString(value: StatKind?): String? = value?.name
    @TypeConverter fun stringToStatKind(value: String?): StatKind? = value?.let { StatKind.valueOf(it) }

    @TypeConverter fun rankToString(value: Rank?): String? = value?.name
    @TypeConverter fun stringToRank(value: String?): Rank? = value?.let { Rank.valueOf(it) }

    @TypeConverter fun targetKindToString(value: DailyQuestTargetKind?): String? = value?.name
    @TypeConverter fun stringToTargetKind(value: String?): DailyQuestTargetKind? = value?.let { DailyQuestTargetKind.valueOf(it) }

    @TypeConverter fun floorStateToString(value: FloorState?): String? = value?.name
    @TypeConverter fun stringToFloorState(value: String?): FloorState? = value?.let { FloorState.valueOf(it) }

    @TypeConverter fun opKindToString(value: OpKind?): String? = value?.name
    @TypeConverter fun stringToOpKind(value: String?): OpKind? = value?.let { OpKind.valueOf(it) }

    @TypeConverter fun themeToString(value: ThemeAccent?): String? = value?.name
    @TypeConverter fun stringToTheme(value: String?): ThemeAccent? = value?.let { ThemeAccent.valueOf(it) }
}
