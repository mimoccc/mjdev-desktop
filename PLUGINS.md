# Desktop widget plugins

Widgets are plugins. The memory chart is the built-in one; external plugins are jar files placed in
`~/.mjdev/plugins/`. The control center **Plugins** tab lists them (image, label, on/off switch,
expandable details) and the enabled ones are drawn on the desktop.

## Jar layout

```
my-plugin.jar
├── plugin.json                                   metadata, read WITHOUT running plugin code
├── icon.png                                      optional image (or name it in plugin.json "icon")
├── META-INF/services/org.mjdev.desktop.plugins.IDesktopPlugin   -> one implementation class
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

Implement `org.mjdev.desktop.plugins.IDesktopPlugin`: expose a `RemoteDocument` (the look, plain
data: `RECT`, `ARC`, `TEXT` ops with geometry normalized to 0..1) and return live
`RemoteVariables` from `variables()` (numbers drive `sweepVar` of arcs, texts replace `{name}`
placeholders). Colors use palette tokens (`BACKGROUND`, `TEXT`, `ICONS`, `BORDER`, `SELECTED`) so a
widget follows the wallpaper; `#RRGGBB` / `#AARRGGBB` are also accepted. See
`MemoryWidgetPlugin` for a complete example. `RemoteDocument.encode()` / `decode()` give the JSON form.

## Security

A plugin jar runs with the same permissions as the desktop once it is enabled. Metadata and image
are read without loading any class, and nothing is loaded until the user switches the plugin on.
Only enable plugins you trust.

State (which plugins are enabled) is stored in `~/.mjdev/desktop/plugins.json`.
