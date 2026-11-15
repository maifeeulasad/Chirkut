package com.maifee.chirkut.data

import com.mua.minidb.CreatedAt
import com.mua.minidb.Entity
import com.mua.minidb.Index
import com.mua.minidb.KeyStrategy
import com.mua.minidb.PrimaryKey

/**
 * History row for one completed/failed call.
 * @author Maifee Ul Asad
 */
@Entity(store = "call_log", version = 1)
data class CallLogEntry(
    @PrimaryKey(generate = KeyStrategy.UUID) val id: String? = null,
    @Index val peerAddress: String,
    /** CALLER or CALLEE, as the enum name. */
    val role: String,
    /** Final CallEndReason name, or "COMPLETED". */
    val outcome: String,
    val durationMillis: Long,
    @CreatedAt val createdAt: Long? = null,
)
