# Server-side window decorations

The mjdev compositor (`mjdevc`) draws GNOME-style window frames **itself**, in the
C shim (`native/shim.c`), instead of letting each app draw its own titlebar (CSD).
The frame is rendered with **cairo** into an image surface that is wrapped in a
custom `wlr_buffer` and shown as a `wlr_scene_buffer` node, so it is GPU-composited
like any other surface and moves/raises/minimizes together with its window.

Policy (colors, what counts as an app window) is driven from the Kotlin side; the
pixel-pushing lives in C. This follows the project's kotlin-first / compositor-only
split: surface layering and frame rendering are genuine compositor concerns.

## What the frame does

- **Look:** rounded top corners, a soft vertical gradient derived from the desktop
  background color, a hairline top highlight, the window title (cairo "toy" font
  API — no pango dependency), and a 1px border down the sides and bottom.
- **Three GNOME-style buttons** (right-aligned, order min · max · close):
  - **minimize** → hides the window (`mjc_view_set_minimized`)
  - **maximize** → toggles maximize (`mjc_view_set_maximized`)
  - **close** → closes the window (`mjc_view_close`)
  - hover highlights the button; the close button turns red on hover.
- **Move:** drag the titlebar (anywhere that isn't a button) to move the window.
- **Resize:** drag any **edge or corner**. An 8px hotzone around the frame starts an
  interactive resize with the correct edges; works on all four sides + four corners.
- **Cursors:**
  - over the resize hotzone → directional resize cursor
    (`n/s/e/w/ne/nw/se/sw-resize`),
  - over a button → `pointer` (hand),
  - over the draggable titlebar → `default` arrow.
- **Active/inactive styling:** the focused window's titlebar is brighter; unfocused
  windows are dimmed.
- **Focus-follows-mouse:** moving the pointer over an app window focuses it (no
  restack) — `MJC_FOCUS_FOLLOWS_MOUSE` in `shim.c`.

## Which windows get a frame

A window is decorated when it is a normal **app** window. It is **not** decorated when:

- it is **shell chrome** — the desktop's own wallpaper, panels, menus, control
  center (the Kotlin `Policy` marks these via `mjc_view_set_chrome(view, true)`),
- it is **chromeless** — either the shell asked for it
  (`mjc_view_set_decorated(view, false)` / IPC `set-decorated`), or the client itself
  demanded client-side decorations through the xdg-decoration protocol (we honor the
  client and leave it frameless),
- it is an X11 override-redirect surface (menus, tooltips, popups).

Decoration is decoupled from the stacking layer, so **always-on-top** and
**always-on-bottom** app windows keep their frame.

## Theme / colors over IPC

The frame colors come from the shell's live background palette, sent over the IPC
socket. Until the shell sends a palette, a dark-slate default is used.

The desktop shell pushes them automatically: `ThemeManagerLinux.createFromPalette()`
(invoked whenever the wallpaper/palette changes) calls
`CompositorControl.setDecorationTheme(backgroundColor, textColor)`
(`shared/.../helpers/compositor/CompositorControl.kt`), a one-shot IPC sender that
no-ops when not running inside mjdevc. So changing the wallpaper re-colorizes every
window frame.

```jsonc
// rgb channels 0..255; fg is optional and defaults to a contrasting
// black/white chosen from the bg luminance
{"cmd":"set-decoration-theme","bg_r":30,"bg_g":34,"bg_b":42}
{"cmd":"set-decoration-theme","bg_r":30,"bg_g":34,"bg_b":42,"fg_r":240,"fg_g":240,"fg_b":240}
```

Other decoration-related IPC commands:

```jsonc
{"cmd":"set-decorated","id":3,"decorated":false}   // chromeless / frameless
{"cmd":"always-on-top","id":3,"on":true}
{"cmd":"always-on-bottom","id":3,"on":true}
```

## C / native API (`native/shim.h`)

```c
void mjc_view_set_chrome(mjc_view *view, bool chrome);       // shell surface, no frame
void mjc_view_set_decorated(mjc_view *view, bool decorated); // chromeless toggle
void mjc_set_decoration_theme(mjc_server *server,
    float bg_r, float bg_g, float bg_b,
    float fg_r, float fg_g, float fg_b);                     // colors in 0..1
```

## Build / packaging

- `compositor.gradle.kts` adds `cairo` to the shim's pkg-config cflags and links it
  into `mjdevc`.
- `libcairo2` is declared in `gradle/libs.versions.toml`
  (`app-compositor-runtime-deps`), the single source of truth that feeds the deb
  `Depends`, the `installDesktop` apt step and `make-iso.sh`. `mjdevc` (the binary
  that links cairo) ships via the deb and the ISO, so those are the package formats
  that need the dependency.

## Geometry constants (`shim.c`)

```c
#define MJC_DECO_TITLEBAR_H 36   // titlebar height
#define MJC_DECO_RADIUS     10   // top-corner radius
#define MJC_DECO_BORDER     3    // side/bottom border width
#define MJC_DECO_BTN_R      9    // button circle radius
#define MJC_DECO_BTN_GAP    8    // gap between buttons
#define MJC_DECO_PAD        14   // title-left / buttons-right padding
#define MJC_DECO_RESIZE     6    // edge/corner resize hotzone half-width
```

## Next steps / known limitations

- **Maximized windows:** the titlebar is drawn directly above the content. When a
  window is maximized to the full output, the titlebar sits at the very top (under
  the shell top bar). Inset/work-area-aware maximize is a follow-up.
- **GTK apps that ignore xdg-decoration** still draw their own CSD titlebar, so they
  can show a double titlebar. A per-app override list can suppress this later.
- **Window transparency (ARGB) + blur ("glass"):** the cairo buffer is already ARGB
  (alpha-capable), so the frame foundation supports translucency. True window
  transparency and background blur (frosted-glass) are a deliberate future step —
  they need a blur pass in the render path (e.g. a scenefx-style wlroots scene), not
  just the decoration layer.
- **Drop shadows:** not yet drawn (kept off for now to avoid per-frame overdraw cost);
  belongs with the same blur/scenefx work above.
