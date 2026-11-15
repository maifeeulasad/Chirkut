package com.maifee.chirkut.data

import android.content.Context
import com.mua.minidb.EntityStore
import com.mua.minidb.entityStore

/** Repository for completed call records. */
class CallLogRepository(context: Context) {

    private val store: EntityStore<CallLogEntry> =
        ChirkutDatabase.get(context).entityStore(CallLogEntry::class)

    fun record(peer: String, role: String, outcome: String, durationMs: Long) {
        store.put(
            CallLogEntry(
                peerAddress = peer,
                role = role,
                outcome = outcome,
                durationMillis = durationMs,
            ),
        )
    }

    fun history(): List<CallLogEntry> =
        store.query().orderBy("createdAt", descending = true).toList()

    fun forPeer(peer: String): List<CallLogEntry> =
        store.query()
            .where("peerAddress").eq(peer)
            .orderBy("createdAt", descending = true)
            .toList()
}
