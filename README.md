# Mjdev Desktop

An kotlin compose desktop environment like DDE Desktop.
New desktop environment with many functio
nality i miss in another desktops.
Build on compose kotlin and jvm.

(Currently only linux os is supported as main target)

## functionality about to be and or implemented:

- lightweight, no memory/cpu eater, as much as possible
- animated/gif/video/any image wallpapers (from default dir, extendable)
- all what can be animated -> animated (just woow effect, not realy everything)
- auto-hide all things when user is working, avail whole screen-space (as much as possible)
- dynamic background (user can turn on/off)
- adaptive dynamic color of whole system when ( background change and user does not override colors)
- adaptive dynamic system (gtk/gnome) theme auto-generation
- control panel with all system settings on left (modal window), include custom pages
- configurable desktop panels
- state icons customizable (status tray)
- configurable left menu (full screen will be included)
- custom desktop widgets, animated
- api for developers to avail them to create widgets & etc, remote views
- material design icons, overridable with one file, iconic font file - ttf
- custom control panel pages
- ai page in control panel & ai integrated to os
- scriptable extensions
- installable from one standalone package
- multiplatform support (android, linux, windows)
- support for themes & customization
- one click solution for non-technical users
- autoconnect and show android device on desktop
- stt / tts a.i.
- iot support
- connect to another desktop to share files (p2p)
- no files on desktop (only links, user invisible action. all will be placed in home folder, as it should be)
- ai typing helper like copilot from desktop documents (secure, offline)
- custom apps api

# screenshots

![Snímka obrazovky z 2024-09-19 19-20-45.png](screenshots/Sn%C3%ADmka%20obrazovky%20z%202024-09-19%2019-20-45.png)
![Snímka obrazovky z 2024-09-19 19-20-26.png](screenshots/Sn%C3%ADmka%20obrazovky%20z%202024-09-19%2019-20-26.png)
![Snímka obrazovky z 2024-09-19 21-11-47.png](screenshots/Sn%C3%ADmka%20obrazovky%20z%202024-09-19%2021-11-47.png)

# build

- import to intellij idea
- click run or build (or select from run dialog what you want to do)

# used libraries

- [Jetbrains Compose Desktop] (https://github.com/JetBrains/compose-multiplatform)
  Main library for to made app
- [Ini4J] (https://github.com/facebookarchive/ini4j)
  Parse Desktop Files
- [OkHttp] (https://github.com/square/okhttp)
  For images & for future use (weather/ external apis)
- [Coil] (https://github.com/coil-kt/coil)
  Image loader
- [Coroutines Swing] (https://github.com/Kotlin/kotlinx.coroutines)
  Coroutines in JVM
- [Google Gson] (https://github.com/google/gson)
  JSON Parser
- [FuzzyWuzzy] (https://github.com/xdrop/fuzzywuzzy)
  Fuzzy search
- [Thechance101 Chart] (https://github.com/TheChance101/AAY-chart)
  Compose charts
- [Shreyaspatil Generative AI] (https://github.com/PatilShreyas/generative-ai-kmp/)
  AI support
- [jna] (https://github.com/java-native-access/jna)
  Load external libraries, for X11/Wayland
- [Alexzhirkevich Grose] (https://github.com/alexzhirkevich/qrose)
  QR code & Bar code (easy connect pc & mobile / Wifi share)
- [Marc Apps TTS] (https://github.com/Marc-JB/TextToSpeechKt)
  TTS support
- [Hypfvieh DBus] (https://github.com/hypfvieh/dbus-java)
  DBus support
- [Kevinnzou Compose Webview] (https://github.com/KevinnZou/compose-webview-multiplatform)
  Widgets with web view
- [Mozilla Rhino] (https://github.com/mozilla/rhino)
  Custom scripting & widgets code
- [Bytedeco FFMpeg] (https://github.com/bytedeco/javacv)
  Video wallpapers & widgets