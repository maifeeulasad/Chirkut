package com.maifee.chirkut.data

import android.content.Context
import com.mua.minidb.EntityStore
import com.mua.minidb.entityStore

/** Thin repository around the {@link ChirkutMessage} minidb store. */
class MessagesRepository(context: Context) {

    private val store: EntityStore<ChirkutMessage> =
        ChirkutDatabase.get(context).entityStore(ChirkutMessage::class)

    fun insert(peer: String, body: String, outgoing: Boolean): ChirkutMessage {
        val draft = ChirkutMessage(peerAddress = peer, body = body, outgoing = outgoing)
        val key = store.put(draft)
        return draft.copy(id = key.toString())
    }

    fun conversation(peer: String): List<ChirkutMessage> =
        store.query()
            .where("peerAddress").eq(peer)
            .orderBy("createdAt")
            .toList()

    fun recentPeers(): List<String> =
        store.getAll().map { it.peerAddress }.distinct()

    fun clearPeer(peer: String) {
        val keys = store.query().where("peerAddress").eq(peer).toList().mapNotNull { it.id }
        if (keys.isNotEmpty()) store.deleteMany(keys)
    }
}
