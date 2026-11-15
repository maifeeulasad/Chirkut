package com.maifee.chirkut.data

import com.mua.minidb.CreatedAt
import com.mua.minidb.Entity
import com.mua.minidb.Index
import com.mua.minidb.KeyStrategy
import com.mua.minidb.PrimaryKey
import com.mua.minidb.UpdatedAt

/**
 * A single chat message persisted in minidb. One store per peer is overkill
 * here — the [peerAddress] index lets us scope queries without a separate
 * store per conversation.
 */
@Entity(store = "messages", version = 1)
data class ChirkutMessage(
    @PrimaryKey(generate = KeyStrategy.UUID) val id: String? = null,
    @Index val peerAddress: String,
    val body: String,
    val outgoing: Boolean,
    @CreatedAt val createdAt: Long? = null,
    @UpdatedAt val updatedAt: Long? = null,
)
