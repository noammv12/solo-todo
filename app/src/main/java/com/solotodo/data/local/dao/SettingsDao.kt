package com.solotodo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solotodo.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: UserSettingsEntity)

    @Query("SELECT * FROM user_settings WHERE id = 'me' LIMIT 1")
    suspend fun get(): UserSettingsEntity?

    @Query("SELECT * FROM user_settings WHERE id = 'me' LIMIT 1")
    fun observe(): Flow<UserSettingsEntity?>
}
