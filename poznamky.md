# Pracovní poznámky (Claude)

Průběžný log práce, nálezů a chyb — aby bylo možné se vracet.
Slouží i jako souhrn projektu, ze kterého jde projekt myšlenkově obnovit.

## Souhrn projektu (co to je a jak je to postavené)

**mjdev-desktop** = desktopové prostředí psané v Compose Multiplatform (desktop JVM + Android)
+ vlastní wayland kompozitor **mjdevc** (Kotlin/Native + wlroots 0.18, modul `compositor/`).

Hlavní myšlenka designu: **wallpaper řídí barvy celého systému**. Z aktuálního pozadí
(obrázek/gif/video, rotují se) se extrahuje paleta (dominantní barva rohů, nejtmavší = pozadí),
z ní se odvodí text/ikony (inverzní, čitelné) a vygeneruje se i GTK 2/3/4 téma, takže nativní
gtk aplikace vypadají jako součást systému — fotorealisticky. Změna se má projevit okamžitě
i u otevřených oken (xsettings/portal + střídání jména tématu Mjdev/Mjdev-alt).

Moduly:
- `composeApp/` — KMP aplikace, build file `composeApp.gradle.kts`.
  - `commonMain` — UI komponenty (panel, menu aplikací, control center, greeter, tooltip,
    sliding panely…), manageri jako rozhraní (`IDesktopContext`, `I*Manager`, `IPalette`,
    `ITheme`, `IApp`, `IUser`…), pluginnable systém stránek control centra (`IPage`,
    `controlCenterPages`), AI manager (pluginy OpenAI/Gemini, TTS/STT).
  - `desktopMain` — `DesktopContext` (soubor DeskptopContext.kt — překlep v názvu),
    okenní systém: `ChromeWindow`/`DesktopWindow`/`ChromeWindowState` (AWT undecorated
    transparent okna, mjdev:: title prefix), `WindowsManager` = IPC klient kompozitoru
    (unix socket `$XDG_RUNTIME_DIR/mjdev-compositor.sock`, json příkazy list-windows/
    activate/minimize/close/subscribe), ThemeManagerLinux (gtk css generátor),
    adb nástroje, Environment (mjdev session detekce).
  - `androidMain` — MainActivity, vlastní `DesktopContext`, Theme; manageri bez
    implementace padají na `EMPTY` z companion (reflexe v `createManager` else větvi).
- `compositor/` — Kotlin/Native linuxX64 binárka `mjdevc.kexe`; C shim (`native/shim.c`)
  nad wlroots přes cinterop; IPC server (Ipc.kt), políčka oken (WindowModel), Policy
  (pravidla velikostí oken), GeometryStore (persistence geometrie per app),
  Session (spouští shell = mjdev-desktop). Session soubory v `session/`
  (mjdev-session skript, mjdev.desktop pro gdm wayland-sessions).
- `buildSrc/` — konvence (BasePlugin, MultiPlatformPlugin, BuildExt, TaskExt.registerTask).

Kontext/DI vzor: `IDesktopContext` (abstract, commonMain) drží manager cache;
`open val xyManager: IXyManager by this` deleguje do `createManager(KClass)` platformního
kontextu; UI bere kontext přes `LocalDesktopContext` + `withDesktopContext { }` scope
(`DesktopContextScope` — pohodlné accessory barvy/manageri/theme).

Distribuce: `./gradlew buildAll` → `release/mjdev-desktop-<ver>.deb` (obsahuje
/opt/mjdev-desktop app image, /usr/bin/mjdevc, /usr/bin/mjdev-session,
/usr/share/wayland-sessions/mjdev.desktop) + portable tar.gz. GitHub Actions
`.github/workflows/release.yml` (tag v* nebo ručně) buildí v ubuntu:25.04 kontejneru.
Verze/metadata v `gradle/libs.versions.toml` (`app-*` klíče) — nikde nehardcodovat.

## 2026-06-12 — Merge multiplatform → main

Hotovo, commit `035278b` (+ fixupy uživatele `6b128fc`, `fe8fd7f`).

Co se při merge dělalo (důležité pro případné regrese):

- multiplatform přestrukturoval projekt: `src/main/.../eu.mjdev` → `composeApp/src/{common,desktop,android}Main/.../org.mjdev`.
- Kompozitorové změny z main přeneseny do nové struktury:
  - `WindowsManager` (IPC klient kompozitoru) → `composeApp/desktopMain/managers/window/WindowsManager.kt`;
    nově rozhraní `IWindowsManager` v commonMain (vzor ostatních managerů, `EMPTY` fallback pro Android).
  - `windowFocusGraceDelay` přidán do `ITheme` + obou `Theme` (desktop, android), používá
    `MainWindow` (menu + control center: focus bounce kompozitoru).
  - `mjdev::` prefix titulku v desktop `ChromeWindow` (rozpoznání shell oken kompozitorem).
  - `setAwtWindowClass()` v desktop `Main.kt` (WM_CLASS=mjdev-desktop; potřebuje
    `--add-opens java.desktop/sun.awt.X11=ALL-UNNAMED`, přidáno do composeApp.gradle.kts).
  - `EnvironmentLinux`: detekce mjdev session (`XDG_CURRENT_DESKTOP=mjdev` nebo socket),
    NEnastavovat GTK_THEME (blokuje live přepínání témat).
  - `ThemeManagerLinux`: střídání jmen témat Mjdev/Mjdev-alt (gtk reload css jen při změně jména),
    `setGTKTheme` + `setColorScheme` po regeneraci.
  - `DesktopPanelRunningApps` (taskbar) → commonMain, přes `IWindowsManager` (na androidu prázdné).
  - `DesktopPanelIcon`: runningOverride/focusedOverride + zvýrazněný kroužek fokusu.
  - Klik na ikonu v panelu: fokus → minimalizace; běží → fokus; jinak start (DesktopPanelWindow).
  - `VisibilityState.toggle()` ruší pending hideJob.
- `IApp` dostal `windowClass` (default fullAppName, desktop App overriduje StartupWMClass).
- Build:
  - modul `:compositor` přidán do settings; build file přejmenován na `compositor/compositor.gradle.kts`
    (settings přejmenovává build soubory dle jména modulu!).
  - katalog: klíče `app-pkg-name`/`app-pkg-version` (staré packageName/packageVersion už nejsou),
    přidán plugin alias `kotlin-serialization`.
  - root build.gradle.kts: tasky `packageMjdevDeb`, `packageMjdevAppImage`, `buildAll`,
    `runDesktop`, `runDebug` — POZOR: konfigurační cache je zapnutá; v doLast/doFirst closure
    se NESMÍ zachytit script objekt (žádné top-level fun/val ze scriptu; vše zkopírovat do
    lokálních val uvnitř tasku, exec{}/copy{} nahrazeny ProcessBuilder/copyRecursively).
  - stejné pravidlo aplikováno v compositor.gradle.kts (compileShim, runNested, installSession).
  - repozitáře: google() přesunut hned za mavenCentral (jogamp.org bývá nedostupný a timeoutuje).
  - AdbScreenMirror používal Skia v commonMain → rozbitý Android; vyřešeno expect/actual
    `ByteArray.decodeToImageBitmap()` (desktop Skia, android BitmapFactory).
  - workflow `.github/workflows/release.yml`: přidána instalace Android SDK (cmdline-tools)
    + openjdk-17 (toolchain 17), kontejner ubuntu:25.04 kvůli libwlroots-0.18.
- Ověřeno: `compileKotlinDesktop` OK, `compileDebugKotlinAndroid` OK,
  kompozitor link OK, `buildAll` vygeneroval `release/mjdev-desktop-1.0.3.deb` (262M)
  a `release/mjdev-desktop-1.0.3-linux-x64.tar.gz` (275M). Deb obsahuje mjdevc,
  mjdev-session, wayland session file, /opt/mjdev-desktop.

Známé warny (neřešeno): deprecated ClipboardManager/Instant, expect/actual classes beta,
pkg-config při konfiguraci (config cache problem=warn).

## STEP 1 (ukoly.txt)

### Úkol 1: barvy komponent dle wallpaperu — ROZPRACOVÁNO

Řetěz: `Desktop` → `BackgroundImage.onChange(src)` → `palette.update(src)` →
`loadPicture` → výřezy rohů → `topMostColor` → backgroundColorState/textColorState →
`themeManager.createFromPalette()`.

Nalezené chyby:
1. `Palette.update()`: textColor se počítá ze STARÉ backgroundColor (isLight i barva čtené
   před přiřazením nové) → text/ikony neodpovídají aktuálnímu pozadí.
2. `Palette.focusedTextBackgroundColor` getter/setter deleguje omylem na textColorState
   (copy-paste chyba).
3. `BackgroundImage`: `images` je obyčejný MutableList — naplnění na pozadí (ProviderLocal přes
   `DesktopConfig.addBackground` v coroutine) NEvyvolá rekompozici → seznam zůstane pro UI
   „prázdný", currentBackground zůstane Color → loadPicture(Color) selže → paleta se nikdy
   nepřepočítá. Pravděpodobná hlavní příčina „barvy se nemění".
4. `DesktopConfig.load`: DEFAULT instance se sdílí/cachuje per user, addBackground se volá
   při každém load → duplikáty? (k ověření)

Plán opravy: desktopBackgrounds jako SnapshotStateList (mutableStateListOf), v update()
počítat text z nové barvy, opravit focusedTextBackgroundColor, ošetřit duplikáty v load.

### Úkol 1: HOTOVO (k otestování uživatelem)
Opraveno:
- `Palette.update()` — text/focusedTextBackground se počítá z NOVÉ barvy pozadí (inverzní),
  synchronizace `context.theme.backgroundColor` přesunuta sem (dřív se četla stará hodnota
  hned po async update v Desktop.kt).
- `Palette.focusedTextBackgroundColor` — getter/setter delegoval omylem na textColorState.
- `DesktopConfig.desktopBackgrounds` — nyní `mutableStateListOf` (async naplnění od
  ProviderLocal dřív nevyvolalo rekompozici → UI nikdy nedostalo wallpapery → paleta se
  nepřepočítala — hlavní příčina „barvy se nemění").
- `DesktopConfig.load` — nová instance per user (DEFAULT se dřív sdílel a mutoval).

### Úkol 2: wiring OK po merge
palette.update → themeManager.createFromPalette → ThemeManagerLinux: gtk css z palety,
střídání jména Mjdev/Mjdev-alt (gtk reloadne css jen při změně jména), gsettings gtk-theme
+ color-scheme (prefer-light/dark dle pozadí) → živá změna otevřených gtk oken.
Závisí na opravě úkolu 1 (paleta se teď skutečně mění).

### Úkol 5: HOTOVO
- `packageMjdevApk` (release apk podepsaný debug klíčem → instalovatelný) → release/,
  zapojeno do `buildAll` (deb + tar.gz + apk). Workflow nahrává release/*.
- Oprava: META-INF/io.netty.versions.properties duplicity v APK → packaging excludes.
- Run akce: `runAndroid` (installDebug + adb am start), `runDesktop`, `runDebug`,
  `runNested` (kompozitor+desktop v okně aktuální session, bez odhlašování),
  `installDesktop` (build deb + sudo apt install → session na gdm), `buildAll`;
  ke všem .run XML pro IDE.

### Stav úkolů STEP 1
- [x] 1. dynamické barvy dle pozadí
- [x] 2. gtk 2/3/4 téma dle pozadí + okamžitá změna otevřených oken
- [x] 3. kompozitor: okna se otevírají fullscreen → OPRAVENO + otestováno nested
       Příčina: aplikace samy žádají maximize při startu (gtk si pamatuje stav, např.
       gnome-calculator měl v dconf window-maximized=true — znečištěné dřívějším chováním)
       a kompozitor žádost ctil. Oprava v shim.c: maximize požadavky klienta se ctí jen
       PO namapování okna (post-map = akce uživatele); startovní stav určuje GeometryStore.
       GeometryStore rozšířen: [x,y,w,h,maximized] — pamatuje i maximized + plovoucí
       geometrii (nová shim fn mjc_view_get_floating_geometry). Ověřený cyklus:
       otevření přirozená velikost → maximize → reopen maximalizované → unmaximize
       obnoví plovoucí → reopen na poslední pozici. Testováno nested (mjdevc --socket
       /tmp/mjc-test.sock + gnome-calculator + IPC list-windows).
       POZOR: gradle jednou nezaregistroval změnu shim.c (compileShim UP-TO-DATE) —
       pomohl touch shim.c + --rerun-tasks.
- [x] 4. drag-to-scroll v menu aplikací — existoval v AppsList, ale rozbitý:
       Orientation.Horizontal na vertikálním seznamu, neinvertovaná delta, magická ×8,
       clickEnabled nebyl state. Opraveno (Vertical, scrollBy(-delta), mutableStateOf),
       funguje pro desktop (myš) i android (touch).
- [x] 5. buildAll + GH action: apk pro android + deb pro linux (+ installDesktop, runNested)
- [x] 6. control panel na androidu: edge-swipe pás 24dp u pravého okraje (MainView),
       detectHorizontalDragGestures, tah doleva > 48dp → controlCenterState.show().
- [x] 7. greeter: komponenta `Greeter` je společná (commonMain) s parametrem
       passwordLogin; nový stav `IDesktopContext.authenticatedState` + suspend
       `authenticate(user, password)`.
       Linux: `login()` ověřuje heslo přes PAM helper `unix_chkpwd` (stejně jako
       screen lockery; bez nových závislostí, funguje pro přihlášeného uživatele).
       GreeterWindow = lock screen do ověření (debug run greeter přeskočí).
       Android: `login()` = systémový BiometricPrompt (otisk NEBO pin/heslo zařízení,
       API 28+; <28 propustí), USE_BIOMETRIC v manifestu, Greeter overlay v MainView
       (passwordLogin=false → klik na avatar spustí prompt).
- [x] 8. systemd: `session/mjdev-desktop.service` (user unit, kiosk/embedded;
       enable: systemctl --user enable --now mjdev-desktop, boot bez loginu:
       loginctl enable-linger). Balí se do deb (/usr/lib/systemd/user/) i do
       stageSession/install.sh. Pro normální desktop netřeba — session startuje gdm.
- [x] 9. jeden balíček: deb obsahuje shell (/opt, greeter je součást shellu),
       mjdevc, mjdev-session, wayland session, systemd user unit. Depends
       prověřeny — všechny potřebné (wlroots, xwayland, xkbcommon, X/asound/
       freetype pro bundlovanou JVM), nic nadbytečného nepřidáno.
       Ověřeno: buildAll → deb 262M + tar.gz 275M + apk 107M v release/.

## 2026-06-12 odpoledne — incident „pozadí se nemění" a náprava

Co se stalo a poučení (DŮLEŽITÉ):
1. Při ladění jsem nadělal víc škody než užitku: mutableStateListOf v DesktopConfig,
   polling s hardcode konstantou v BackgroundImage, debug logy (BGDBG), StateFlow
   experiment. Uživatel vše odmítl — POŽADAVEK: chovat se dle multiplatform větve.
2. NÁPRAVA: BackgroundImage.kt, DesktopConfig.kt, Desktop.kt, Palette.kt vráceny
   PŘESNĚ dle origin/multiplatform (git checkout origin/multiplatform -- <soubory>,
   branch jen ke čtení!). DesktopContextScope.backgrounds zpět MutableList<Any>.
   Kompilace desktop i android OK.
3. Diagnostický poznatek (nevyřešeno, jen zdokumentováno): zápis do compose snapshot
   stavu z Dispatchers.Default (provider plnící wallpapery, Palette.update) se v této
   aplikaci NEPROPSAL do čtení na AWT-EventQueue-0 (stejný objekt, writer viděl 5,
   reader trvale 0; oba hlásili GlobalSnapshot; sendApplyNotifications nepomohl).
   Podezření: custom application framework (YieldFrameClock/ApplicationApplier/vlastní
   GlobalSnapshotManager) v kombinaci s compose 1.8.2. Multiplatform verze fungovala
   díky tomu, že obyčejný ArrayList se čte vždy aktuální (žádné snapshot záznamy)
   a rekompozice přijdou z jiných podnětů.
4. PROVOZNÍ PRŮŠVIH: mé `pkill -f MainKt` zabíjelo vlastní shell (pattern matchoval
   vlastní příkaz) → postupně se nahromadilo 11 běžících instancí aplikace, které
   kontaminovaly testy i uživatelovo prostředí. Zabíjet přes pgrep+kill dle PID.
5. desktopRun: KMP carrier task nemá main class z compose configu → doplněno
   v composeApp.gradle.kts (desktopMainClass/desktopJvmArgs jako single source,
   tasks.matching("desktopRun") nastaví mainClass + jvmArgs). Ověřeno spuštěním.
   Run akce: buildAll, desktopRun, installDesktop, runAndroid, runDebug, runNested
   (runDesktop xml odstraněn jako duplikát desktopRun; generateIcons xml odkazoval
   na neexistující task — odstraněn).

Pravidla potvrzená uživatelem:
- Žádné hardcode konstanty (ani „technické" delaye) — vše z konfigurace/témat.
- multiplatform branch = referenční chování; zásahy do logiky jen po dohodě.
- Branch multiplatform NIKDY nemodifikovat (read-only reference).

## 2026-06-12 večer — nová paleta (kolorizace komponent dle wallpaperu)

Zadání uživatele (přesná specifikace):
- menu = barva výřezu ~128×128 px z rohu wallpaperu, kde se menu zobrazuje (levý dolní)
- control center (vpravo) = majoritní barva pravého pásu 128 × výška
- ikony = dle středu wallpaperu 128×128: světlý → nejtmavší barva wallpaperu,
  tmavý → nejsvětlejší; kontrast barva↔pozadí min 128/255
- logika zobrazení background NEMĚNIT; při každé změně wallpaperu přegenerovat
  barvy komponent + gtk 2/3/4 téma; jen palette část, vlastní metody, efektivní,
  multiplatform (Linux + Android)

Implementace (composeApp/commonMain):
- `Palette.kt` přepsána: vlastní analýza wallpaperu — jeden `toPixelMap` na region,
  vzorkování mřížkou (`sampleGrid=64`), kvantizace 4 bity/kanál (4096 bucketů),
  vítězný bucket zprůměrován → dominantní barva. Žádné kopie bitmap (staré
  `cut()`+histogram každý pixel nahrazeno). Regiony: 4 rohy + střed (128×128,
  `regionSize` param) + pravý pás 128×výška.
- backgroundColor = nejtmavší roh (zachována původní sémantika nonAlphaValue).
- text = inverz z NOVÉHO pozadí (darker/lighter o textFactor) + `ensureContrast`.
- `ensureContrast(color, against)`: luminance rozdíl < minContrast (128/255) →
  lerp k bílé (tmavé pozadí) / černé (světlé), closed form bez smyček.
- ikony: střed světlý → nejtmavší vzorkovaná barva, tmavý → nejsvětlejší + kontrast;
  `iconsTintColorState` (null = legacy odvození, dokud není wallpaper).
- nové barvy v `IPalette` (menuColor, controlCenterColor) + scope accessory;
  AppsMenu používá menuColor, ControlCenter controlCenterColor (čistá substituce).
- update(): analýza na Dispatchers.Default, zápis stavů + createFromPalette
  na Dispatchers.Main (zápisy z background vlákna se do oken nepropíší — známý jev).
- Vše parametry konstruktoru (regionSize, sampleGrid, minContrast) — žádný hardcode.

Ověřeno (desktop run): ~/.config/gtk-3.0/gtk.css bg_color #0B0B0A, text #B7B7B6
(z wallpaperu, ne default #202020), gsettings gtk-theme 'Mjdev'. Android kompiluje
(ThemeManager tam je EMPTY stub — barvy komponent fungují, gtk se netýká).
