# MingCute icons — `com.maifee.mingcute:mingcute-icons(+compose):3.0.2`

Two sibling artifacts:

- **`mingcute-icons`** — the raw drawables. Each icon ships as two vector
  drawables, `mgc_<name>_line` and `mgc_<name>_fill`, under base package
  `com.maifee.mingcute`.
- **`mingcute-icons-compose`** — a thin Compose binding on top:
  `MingcuteIcon(name, contentDescription, modifier, style, colorFilter)`
  and `rememberMingcutePainter(name, style)`, both in
  `com.maifee.mingcute.compose`.

## Compose usage

```kotlin
MingcuteIcon(
    name = "send_plane",              // picks mgc_send_plane_line
    contentDescription = "Send",
    style = MingcuteStyle.LINE,       // LINE (default) or FILL
    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
    modifier = Modifier.size(24.dp),
)
```

Or get a plain painter for use inside other composables:

```kotlin
val icon = rememberMingcutePainter("phone_call", style = MingcuteStyle.FILL)
Icon(painter = icon, contentDescription = null)
```

Icons ship opaque black; recolor via `colorFilter = ColorFilter.tint(...)`
(Compose binding) or the `tint` of whatever `Icon(...)` composable you wrap
them in. Resolution works by constructing the drawable name from
`name` + `style.suffix()` and looking it up on the current context; an
unknown name fails fast with `IllegalArgumentException`.

## Non-Compose usage

```kotlin
val resId = Mingcute.resId(context, "phone_off", MingcuteStyle.LINE)
imageView.setImageResource(resId)
```

`Mingcute.resId` returns `0` for unknown names; the Compose binding
promotes that to an exception.

## Icon names used in Chirkut

From `R.txt` in the AAR — pick names without the `mgc_` prefix and
`_line`/`_fill` suffix:

| Use                       | Name                 |
| ------------------------- | -------------------- |
| Chat tab / open thread    | `chat_1`             |
| Send a message            | `send_plane`         |
| Place a call              | `phone_call`         |
| Incoming call / ringing   | `phone_incoming`     |
| Hang up                   | `phone_off`          |
| Mic on / mute toggle      | `mic` / `mic_off`    |
| Nearby peers list         | `group`              |
| Dismiss / close dialog    | `close`              |
| Forward                   | `arrow_right`        |
| Settings                  | `settings_2`         |

The full set is enumerated in
[`mingcute-icons-3.0.2.aar/R.txt`](https://repo1.maven.org/maven2/com/maifee/mingcute/mingcute-icons/3.0.2/mingcute-icons-3.0.2.aar);
every entry of the form `int drawable mgc_<name>_<style>` is a usable
`name = "<name>", style = MingcuteStyle.<STYLE>` pair.
