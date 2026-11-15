package com.maifee.chirkut.data

import android.content.Context
import com.mua.minidb.MiniDB
import com.mua.minidb.MiniDatabase

/**
 * Single process-wide minidb handle. Entity list is the schema; bumping an
 * entity's `version` triggers a migration pass on next open.
 */
object ChirkutDatabase {
    private const val NAME = "chirkut"

    @Volatile private var instance: MiniDatabase? = null

    fun get(context: Context): MiniDatabase {
        val existing = instance
        if (existing != null) return existing
        synchronized(this) {
            val again = instance
            if (again != null) return again
            val db = MiniDB.open(
                context.applicationContext,
                NAME,
                entities = listOf(ChirkutMessage::class, CallLogEntry::class),
            )
            instance = db
            return db
        }
    }
}
