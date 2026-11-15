# minidb — `com.maifee:minidb:0.4.0`

Local, on-device embedded database; IndexedDB-shaped Kotlin API backed by a
native engine. One snapshot file per database under
`context.filesDir/minidb/<name>`, rewritten atomically on every committed
write. No server, no network — fits Chirkut's "local only" requirement.

Base package: `com.mua.minidb`.

## Opening a database

```kotlin
val db: MiniDatabase = MiniDB.open(
    context,
    name = "chirkut",
    entities = listOf(ChirkutMessage::class, CallLogEntry::class),
)
```

Version-less overload opens at `max(@Entity.version)`; bumping any entity
triggers a migration pass. A manual overload also exists:
`MiniDB.open(context, name, version) { txn, old, new -> ... }`.

Companion ops: `MiniDB.engineVersion()`, `MiniDB.databaseNames(context)`,
`MiniDB.deleteDatabase(context, name)`.

## Entities (annotation-driven)

```kotlin
@Entity(store = "messages", version = 1, autoIncrement = false)
data class ChirkutMessage(
    @PrimaryKey(generate = KeyStrategy.UUID) val id: String? = null,
    @Index val peerAddress: String,
    val body: String,
    val outgoing: Boolean,
    @CreatedAt val createdAt: Long? = null,
    @UpdatedAt val updatedAt: Long? = null,
)
```

Annotations (all in `com.mua.minidb`):

| Annotation          | Where      | Purpose                                              |
| ------------------- | ---------- | ---------------------------------------------------- |
| `@Entity`           | class      | store name, autoIncrement, schema version            |
| `@PrimaryKey`       | property   | mark key; `generate = KeyStrategy.UUID/TIMESTAMP/...` or custom `generator = ...` |
| `@Index`            | property   | secondary index (optional `name`, `unique`)          |
| `@Ignore`           | property   | exclude from persistence (needs default value)       |
| `@CreatedAt`        | property   | auto-set once on first put                           |
| `@UpdatedAt`        | property   | refreshed on every put                               |
| `@Validate`         | property   | repeatable; attach a `ValidationRule` class          |
| `@Calculated`       | property   | derive from the entity before validation + timestamps|
| `@RetentionPolicy`  | class      | auto-delete past `seconds` old (by `CreatedAt` or custom field) |

Supported property types: `String`, `Long`, `Int`, `Double`, `Float`,
`Boolean`, `java.util.Date`, their nullables, and
`List<String|Long|Int|Double>` (a `List` + `@Index` = multi-entry index).

Composite keys: annotate several properties with `@PrimaryKey`. Addressed in
lookups as `listOf(a, b)`. Key generation is not available for composite
keys.

## EntityStore (typed CRUD)

```kotlin
val store: EntityStore<ChirkutMessage> =
    db.entityStore(ChirkutMessage::class)

store.put(msg)                 // returns MiniKey
store.putAll(list)             // one transaction, all-or-nothing
store.get(id)                  // T? — raw value, list or MiniKey
store.getMany(ids)             // misses omitted
store.getAll()                 // key-order
store.getKeys()                // just keys
store.findByIndex("peerAddress", "192.168.1.2")
store.findFirstByIndex("peerAddress", "192.168.1.2")
store.delete(id)               // Boolean
store.deleteMany(ids)          // count
store.clear()
store.count()
store.cleanupExpired()         // when @RetentionPolicy declared
```

Each call is its own transaction; batch helpers share one and roll the whole
batch back on failure.

## Query builder

```kotlin
val recent = messages.query()
    .where("peerAddress").eq("192.168.1.2")
    .and("outgoing").eq(false)
    .orderBy("createdAt", descending = true)
    .offset(0).limit(50)
    .toList()
```

Operators on a `FieldCondition`: `eq`, `gt`, `gte`, `lt`, `lte`, `between`,
`notBetween`, `startsWith`, `endsWith`, `contains`, `matches`, `in_`,
`notIn`, `containsAny`, `containsAll`. Grouped sub-expressions via
`.where { it.where("...").eq(...).or("...").eq(...) }`. Index-pruned reads
via `.useIndex(name).range(lower, upper)`. Aggregations on `Query`:
`toList()`, `count()`, `firstOrNull()`, `any()`.

## Transactions

```kotlin
db.read { txn -> txn.objectStore("messages").get(MiniKey.of(id)) }
db.write { txn ->
    txn.objectStore("messages").put(
        value = bytes,
        key = MiniKey.of(id),
        indexKeys = mapOf("by_peer" to listOf(MiniKey.of(peer))),
    )
}
```

Writes commit if the block returns, abort if it throws. One writer per
thread; the database lock is not reentrant.

## Keys and ranges

`MiniKey` is the sum type the engine stores: `.of(String)`, `.of(Long)`,
`.of(Double)`, `.of(Date)`, `.of(List<MiniKey>)`. `MiniKeyRange.only(k)`,
`MiniKeyRange.bound(lo, hi)`, `MiniKeyRange.lowerBound(k)`,
`MiniKeyRange.upperBound(k)`, `MiniKeyRange.all()`.

## Chirkut usage

Database bootstrap: [`data/ChirkutDatabase.kt`](../../chirkut/app/src/main/java/com/maifee/chirkut/data/ChirkutDatabase.kt).
Entities: [`ChirkutMessage.kt`](../../chirkut/app/src/main/java/com/maifee/chirkut/data/ChirkutMessage.kt),
[`CallLogEntry.kt`](../../chirkut/app/src/main/java/com/maifee/chirkut/data/CallLogEntry.kt).
Repositories: [`MessagesRepository.kt`](../../chirkut/app/src/main/java/com/maifee/chirkut/data/MessagesRepository.kt),
[`CallLogRepository.kt`](../../chirkut/app/src/main/java/com/maifee/chirkut/data/CallLogRepository.kt).
