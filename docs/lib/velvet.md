# velvet — `com.maifee:velvet:0.1.0`

Jetpack Compose UI kit layered on Material 3. Components mirror Ant Design's
(`antd`) naming and prop shape, with `antd` string unions translated to
Kotlin enums and slots translated to `@Composable` lambdas.

Base package: `com.maifee.velvet.component.<kind>`.

## App shell

```kotlin
setContent {
    VelvetApp(config = VelvetConfig(/* theme tokens */)) {
        ChirkutRoot()
    }
}
```

`VelvetApp` wires `VelvetConfigProvider` and hosts the global
message/notification controllers. Inside it:

```kotlin
val message = VelvetAppDefaults.message
message?.success("Saved!")

val notification = VelvetAppDefaults.notification
notification?.info("New call from …")
```

## Primitives used in Chirkut

### Button — `button/VelvetButton.kt`

```kotlin
VelvetButton(
    onClick = { sendMessage() },
    type = ButtonType.Primary,     // Primary | Default | Dashed | Text | Link
    size = ButtonSize.Medium,       // Large | Medium | Small
    shape = ButtonShape.Default,    // Default | Round | Circle
    danger = false,
    disabled = false,
    loading = false,
    block = false,                  // fill parent width
    icon = { MingcuteIcon(name = "send_plane", contentDescription = null) },
    iconPlacement = IconPlacement.Start,
) { Text("Send") }
```

### Input — `input/VelvetInput.kt`

```kotlin
VelvetInput(
    value = reactive,                  // Reactive<String> from rememberBinding
    placeholder = "Type a message…",
    allowClear = true,
    password = false,
    onChange = { typed -> /* … */ },
)
```

Multi-line: `VelvetTextArea`; number input: `VelvetInputNumber`. Both
follow the same `value`/`defaultValue`/`onChange`/placeholder/disabled
pattern.

### Card — `card/VelvetCard.kt`

```kotlin
VelvetCard(title = "Peers nearby", extra = { VelvetButton(...) {} }) {
    content()
}
```

### List — `list/VelvetList.kt`

```kotlin
VelvetList(items = peers) { peer ->
    Row { Text(peer.name); Spacer(Modifier.weight(1f)); ActionButton(peer) }
}
```

For long/scrolling data prefer a stock `LazyColumn`.

### Typography — `typography/VelvetTypography.kt`

`VelvetText(text, type = TextType.Default, strong, italic, underline,
delete, disabled, code)`; `TextType` is `Default | Secondary | Success |
Warning | Danger`. Headings: `VelvetTitle(text, level = 1..5)`.

### Avatar — `avatar/VelvetAvatar.kt`

`VelvetAvatar(text = "MA")` or image/icon variants.

## Theme + config

`VelvetConfig` is the ambient config (antd `ConfigProvider`). Apply custom
tokens:

```kotlin
VelvetApp(
    config = VelvetConfig(
        theme = VelvetTheme(
            colors = VelvetColors(primary = Color(0xFF3A86FF)),
        ),
    ),
) { … }
```

## Reactive values

velvet has its own tiny reactive layer (`com.maifee.velvet.reactive`):

```kotlin
val name = rememberBinding(defaultValue = "")
VelvetInput(value = name, onChange = { /* also fires */ })
LaunchedEffect(name.value) { println(name.value) }
```

Pass a `Reactive<T>` to controlled components or omit it for an
uncontrolled one driven by `defaultValue` + `onChange`.

## Full component catalogue

(`com.maifee.velvet.component.<name>`, every one has a `Velvet<Name>`
composable plus preview file in the sources jar)

```
affix, alert, anchor, app, autocomplete, avatar, badge, borderbeam,
breadcrumb, button, calendar, card, carousel, cascader, checkbox,
collapse, colorpicker, config, datepicker, descriptions, divider,
drawer, dropdown, empty, feedback, flex, floatbutton, form, grid, icon,
image, input, inputnumber, layout, list, masonry, mentions, menu,
message, modal, notification, pagination, popconfirm, popover,
progress, qrcode, radio, rate, result, segmented, select, skeleton,
slider, space, spin, splitter, statistic, steps, switch, table, tabs,
tag, timeline, timepicker, tooltip, tour, transfer, tree, treeselect,
typography, upload, util, watermark
```

## Chirkut usage

Root shell: [`ui/ChirkutApp.kt`](../../chirkut/app/src/main/java/com/maifee/chirkut/ui/ChirkutApp.kt) wraps everything in `VelvetApp`.
Call surface + chat surface built from `VelvetCard`, `VelvetButton`,
`VelvetInput`, `VelvetList`, `VelvetAvatar`.
