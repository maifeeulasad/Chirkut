# Project libraries

Chirkut is built on four first-party libraries, used exclusively — no
alternatives (no Room, no Material Icons, no AlertDialog, etc.).

| Library                       | Role                        | Docs                       |
| ----------------------------- | --------------------------- | -------------------------- |
| `com.maifee:velvet:0.1.0`     | Compose UI kit              | [`velvet.md`](velvet.md)   |
| `com.maifee:minidb:0.4.0`     | Local embedded database     | [`minidb.md`](minidb.md)   |
| `com.maifee.mingcute:mingcute-icons:3.0.2`         | Icon drawables (XML / View) | [`mingcute.md`](mingcute.md) |
| `com.maifee.mingcute:mingcute-icons-compose:3.0.2` | Compose binding for the icons | [`mingcute.md`](mingcute.md) |

All four are published to Maven Central under group IDs `com.maifee` and
`com.maifee.mingcute`. Gradle declaration lives in
[`chirkut/app/build.gradle`](../../chirkut/app/build.gradle).

These notes were extracted from each library's published sources jar
(0.1.0 / 0.4.0 / 3.0.2) so the API surface documented here is exactly what
ships. Refer back when adding new screens or data access.
