# Desktop widget plugins

Widgets are plugins. The memory chart is the built-in one; external plugins are jar files placed in
`~/.psippr/plugins/`. The control center **Plugins** tab lists them (image, label, on/off switch,
expandable details) and the enabled ones are drawn on the desktop.

## Jar layout

```
my-plugin.jar
├── plugin.json                                   metadata, read WITHOUT running plugin code
├── icon.png                                      optional image (or name it in plugin.json "icon")
├── META-INF/services/org.psippr.desktop.plugins.IDesktopPlugin   -> one implementation class
└── com/example/MyPlugin.class
```

`plugin.json`:

```json
{
  "id": "com.example.clock",
  "name": "Clock",
  "description": "Shows the time.",
  "version": "1.0",
  "author": "Example",
  "icon": "icon.png"
}
```

## Code

Implement `org.psippr.desktop.plugins.IDesktopPlugin`: expose a `RemoteDocument` (the look, plain
data: `RECT`, `ARC`, `TEXT` ops with geometry normalized to 0..1) and return live
`RemoteVariables` from `variables()` (numbers drive `sweepVar` of arcs, texts replace `{name}`
placeholders). Colors use palette tokens (`BACKGROUND`, `TEXT`, `ICONS`, `BORDER`, `SELECTED`) so a
widget follows the wallpaper; `#RRGGBB` / `#AARRGGBB` are also accepted. See
`MemoryWidgetPlugin` for a complete example. `RemoteDocument.encode()` / `decode()` give the JSON form.
The widget is placed by `anchor` (e.g. `BOTTOM_END`) and can be shifted from there with the optional
`offsetX` / `offsetY` (dp, negative = left / up; both 0 by default).

## Security

A plugin jar runs with the same permissions as the desktop once it is enabled. Metadata and image
are read without loading any class, and nothing is loaded until the user switches the plugin on.
Only enable plugins you trust.

State (which plugins are enabled) is stored in `~/.psippr/desktop/plugins.json`.

## Built-in widgets

- **Memory** (`builtin.memory`) — donut chart, drawn from a `RemoteDocument`.
- **Music player** (`builtin.music`) — plays the files of the user's Music folder (mp3, flac, ogg,
  wav, m4a, aac, opus; sub-folders up to 3 levels) as a card (design `screenshots/widgets/music.jpg`):
  header with the playing time of the track, raised card with the previous / current / next track,
  title with its folder name, seek bar with times and previous / play-pause / next. Colors follow
  the desktop palette. It sits in the right column just above the memory widget.
  Off by default; switch it on in the control center Plugins tab. A document cannot hold buttons, so
  it uses the optional `IDesktopPlugin.content` composable (the document then only gives size and
  anchor). Audio comes from the klibs.io library ComposeMediaPlayer (`composemediaplayer-audio`).
