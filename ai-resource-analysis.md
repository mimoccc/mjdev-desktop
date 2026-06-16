# Analýza minimalizace resource usage (CPU/RAM)

Datum: 2026-06-16. Cíl: **non-invazivní** snížení CPU/RAM bez změny viditelného chování.

---

## HOTOVO (kompiluje desktop + android, čeká na běhové ověření)
- **#0 ADB mirror → Dispatchers.IO** — `AdbScreenMirror`/`DeviceState` blokující adb I/O přesunuto
  z Compose UI dispatcheru na IO (smyčka i `tapOnDevice`), `refreshInterval` 100→200 ms.
  Odstraňuje zamrzání desktopu při zobrazeném mirroru.
- **#1 Bulk pixel write** — nový `imageBitmapFromArgb` (desktop Skia `installPixels`, android
  `Bitmap.createBitmap`) nahradil per-pixel `Canvas.drawRect` v `GifDecoder.setPixels` a
  `ImageBitmap.cut`. ~2M draw volání/snímek → jeden bulk zápis. Výstup identický.
- **#3 Paleta bez mezibitmap** — `Palette.update` čte pixely jednou (`image.pixels`) a vzorkuje
  4 rohy přímo z `IntArray` přes `ImageUtils.dominantArgbColor` (Int-keyed histogram). Zrušeny
  4× `cut()` bitmapy + Color-boxing.
- **#4 Clock polling** — `CustomExt.timeFlow` spí do dalšího celého sekundového okraje místo
  pollingu á 200 ms (~1 probuzení/s).
- **#7.1 Shell blokující I/O → IO** — `Shell.init` launch přepnut z `Dispatchers.Default` na `IO`
  (blokující `ProcessBuilder.waitFor` nehladoví CPU-sized Default pool).

Pozn.: přímý `java.util.HashMap` v commonMain shazuje K2 actualizer (přes `OsRelease`) — používat
`mutableMapOf`.

---

## ZBÝVÁ K ROZHODNUTÍ

### #2 — GIF snímky v RAM (LRU) — POZOR na návrh
Naivní LRU paměť **nesníží** (možná zvýší): snímek jako `IntArray` (heap, w·h·4 B) je stejně velký
jako `ImageBitmap`, takže držet všechny IntArrays + pár přestavěných bitmap = stejně/víc než dnes
(všechny bitmapy). Reálnou úsporu dá jen:
- **(a)** ukládat snímky komprimovaně — palette-index **1 B/px** (~4× méně heapu) a kompozitovat
  on-demand; čisté pro **sekvenční** přehrávání, ale rozbíjí náhodný seek (`prevFrame`/`reset`
  v `GifViewState`) — nutno dořešit (keyframe replay nebo udržovat poslední kompozici);
- **(b)** **coil3 animated decoder** pro přehrávání (coil GIF dekodér v projektu už je, používá se
  pro paletu) — deleguje správu snímků, ruší vlastní `GifDecoder`; mění verified-working playback
  path → ověřit vizuál 1:1 za běhu.
Vybrat (a)/(b). Měřit RSS + počet živých bitmap před/po. `imageBitmapFromArgb` (z #1) dělá rebuild
snímku levný, takže lazy decode je teď reálná cesta.

### #7.2 — Scope leaky v default argumentech @Composable
`CoroutineScope(Dispatchers.Default)` jako default arg (`AppsMenu.kt:264,276`, bázová
`VisibilityState.kt:22`) → nový nezrušený scope při každé rekompozici. Fix = scope z
`context.scope`/`rememberCoroutineScope()`/`remember{}`. **Pozor:** dock/sliding je křehký kód
(leave-zone race, měsíce ladění) — udělat cíleně po domluvě, ne plošně. (`LaunchedEffect.kt:37`
`CoroutineScope(context).launch(context)` ověřit zrušení.)

---

## KE ZVÁŽENÍ (prodiskutovat, zatím neřešit)

### #5 — Mrtvý `colorFilter` na GIF/video (spíš correctness)
`ImageAny` (`components/image/ImageAny.kt:126-151`) předává `colorFilter`
(`BackgroundImage` → `ContrastColorFilter(1.5f)`) do `GifView`, ale `GifView`ovo `Image(...)`
(`components/image/GifView.kt:39-45`) ho ignoruje → filtr je mrtvý (ai-todo #9). Buď propsat
(1 řádek), nebo parametr zahodit. Neovlivní výkon.

### #6 — Délka Crossfade tapety (možná změna chování)
`BackgroundImage.kt:36-37` `fadeInDuration = 60000/8 = 7,5 s`, `fadeOutDuration = 15 s`. Po celou
dobu fade se překresluje fullscreen pozadí (a běží-li GIF, i jeho snímky). Zkrácení by ušetřilo,
ale je to viditelná změna — jen podnět.

---

## Měření (před/po)
- RAM: `jcmd <pid> GC.heap_info`, RSS přes `ps -o rss`, heap dump (počet `ImageBitmap`/`IntArray`).
- CPU: async-profiler / VisualVM — podíl `drawRect`/`writePixels`/`setPixels` při běžící GIF tapetě;
  doba načtení tapety. Vždy stejná GIF + statická tapeta jako kontrola vizuálu.
