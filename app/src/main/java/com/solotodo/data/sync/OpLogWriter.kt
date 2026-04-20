package com.solotodo.data.sync

import com.solotodo.data.DeviceId
import com.solotodo.data.local.OpKind
import com.solotodo.data.local.dao.OpLogDao
import com.solotodo.data.local.entity.OpLogEntity
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Narrow helper that appends to `op_log` alongside repository writes.
 *
 * Repositories pair every entity mutation with an [OpLogWriter.record] call
 * inside the same Room transaction, so local state and the outbox always agree.
 *
 * Phase 4 sync engine drains this table and pushes to Supabase.
 */
@Singleton
class OpLogWriter @Inject constructor(
    private val opLogDao: OpLogDao,
    private val deviceId: DeviceId,
    private val clock: Clock = Clock.System,
) {
    suspend fun record(
        entity: String,
        entityId: String,
        kind: OpKind,
        fieldsJson: String?,
        fieldTimestampsJson: String,
    ) {
        val now = clock.now()
        opLogDao.insert(
            OpLogEntity(
                opId = UUID.randomUUID().toString(),
                entity = entity,
                entityId = entityId,
                kind = kind,
                fields = fieldsJson,
                fieldTimestamps = fieldTimestampsJson,
                originDeviceId = deviceId.value,
                appliedAt = now,
                syncedAt = null,
            ),
        )
    }
}
