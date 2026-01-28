# Unofficial Ado Light Stick Controller

![Status](https://img.shields.io/badge/Status-Work_in_Progress-orange) ![License](https://img.shields.io/badge/License-GPLv3-blue)

**A stable, native replacement for the official Ado Light Stick app.**

## The Story / Motivation

This app was born out of frustration.

I attended the "Hibana" tour in Copenhagen. The concert was amazing, but the official light stick app was not. It requires an active internet connection (which is terrible in crowded venues) and, worst of all, it kills the Bluetooth connection the moment you turn off your screen.

So, right there in Copenhagen, I decided: **I can do this better.**

Back home in Germany, I started reverse-engineering the protocol to build an app that actually works. I initially didn't care about a clean commit history, but now that I am open-sourcing it, I will care for it in the future. This is a passion project by a fan, for fans.

## Features

### Solved Issues
* **Foreground Service:** The official app dies when the screen turns off. My app uses a proper Android Foreground Service to keep the connection alive. Lock your phone, put it in your pocket, and enjoy the show.
* **Offline First:** No internet required.
* **Privacy:** No tracking, no data collection. Just a tool that does its job.

### Known Bugs
* **No device connection check:** Right now the app does not check if a Light Stick is connected or not. If you e.g. turn off your Light Stick while using the app, it will crash.
* **Not perfect UI descriptions:** Some UI elements are not really good describing what happens (mainly the connection screen after you paired your Light Stick).

### App Capabilities
* **Smart Pairing:** The app remembers *your* specific Light Stick. In a venue with thousands of Bluetooth devices, this ensures you connect to *your* stick instantly.
* **Manual Control:** Color picking and brightness control.
* **Effects:** (WIP) Rainbow effect is currently implemented.

### Tech Stack
* **100% Native Android:** Built with **Kotlin** and **Jetpack Compose**.
* **Zero Bloat:** No Electron, no Vue, no React Native web-wrappers. This is pure native code for maximum performance and stability.

## Roadmap

* [ ] **More Effects:** Expanding the library.
* [ ] **Song Maps:** Presets matching specific Ado songs.
* [ ] **Groups:** A feature for group leaders to control multiple sticks (sync colors/effects) – perfect for fan groups at concerts. This feature will not require any internet connection. The device needs to support WiFi - Direct.

## How to Install (APK)

Since this app is not (yet) available on the Google Play Store, you need to install it manually ("sideloading").

1.  **Download:** Get the latest `.apk` file from the [Releases](https://github.com/Polyfish0/abo-light-stick-app/releases) section.
2.  **Open:** Tap on the downloaded file in your notification center or file manager.
3.  **Grant Permission:**
    * Android will likely show a warning: *"For security reasons, your phone is currently not allowed to install unknown apps from this source."*
    * Tap on **Settings**.
    * Toggle on **"Allow from this source"**.
4.  **Install:** Go back and tap **Install**.
5.  **Done:** The app is now ready for the next concert!

> **Note:** Your browser might warn you that the file "might be harmful". This is a standard Android warning for any app downloaded outside the Play Store. You can safely ignore it and tap "Download anyway".

## Disclaimer

This is an unofficial fan project. I am not affiliated with Ado, her management, or the official merchandise manufacturers. Use at your own risk.

Also notice that this app is still not finished! I would not even consider this a beta. I still provide the APK files to get feedback from users.

## License

This project is licensed under the **GNU General Public License v3.0**.
See the [LICENSE](LICENSE) file for details.
