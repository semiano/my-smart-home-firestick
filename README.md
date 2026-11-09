# Miano Home (My Smart Home Firestick)

A custom home screen for the Fire TV Stick 4K Max (1st gen, Fire OS 7). The plan is in [docs/SPEC.md](docs/SPEC.md).

This is **phase 1**: a Kotlin + Compose for TV app with the Living Room screen. It opens the official Disney+, Fubo, YouTube and RetroArch apps and shows sample household data. The other household modes are placeholders for now.

## Project layout

| Path | What it is |
|---|---|
| `app/src/main/AndroidManifest.xml` | TV launcher entry + HOME intent filter |
| `app/src/main/java/com/miano/home/launch/AppCatalog.kt` | Which apps appear and their package names |
| `app/src/main/java/com/miano/home/launch/AppLauncher.kt` | Finds and opens installed apps; falls back to the Amazon Appstore |
| `app/src/main/java/com/miano/home/data/HouseholdRepository.kt` | Data interface + sample data (later: home server API) |
| `app/src/main/java/com/miano/home/ui/home/` | Living Room screen and its parts |
| `scripts/` | Install over Wi-Fi ADB, list package names |

## Quick start: Docker build + TV emulator (no Android Studio)

```bash
powershell -File scripts/docker-build.ps1
```

```bash
powershell -File scripts/emulator.ps1
```

- `docker-build.ps1` builds `app-debug.apk` inside Docker. The Gradle cache and the debug signing key are kept in Docker volumes (`miano-gradle-cache`, `miano-android-keys`), so every build is signed the same way and updates install cleanly. Delete the `miano-android-keys` volume and you'll have to uninstall the app before the next install.
- `emulator.ps1` installs a portable JDK, the Android SDK and an **Android TV 9** emulator into `%LOCALAPPDATA%\MianoHome` (about 3–4 GB, first run only). It then starts the TV and installs and opens Miano Home. Later runs: add `-NoInstall`.
- The emulator runs on Windows, not in Docker. On Windows 10, WSL2 can't give containers the virtualization they need. It uses Windows Hypervisor Platform (WHPX) for speed.
- Android TV 9 matches Fire OS 7's Android base, but it isn't Fire OS. There's no Amazon Appstore, Disney+, Fubo or YouTube, so their tiles show "isn't installed". Test real app launching on the Fire Stick.
- Keyboard in the emulator: arrows = D-pad, Enter = Select, Esc = Back, Home = Home.

## 1. Build the APK with Android Studio (Windows)

1. Install **Android Studio**. It includes Java 17 and the Android SDK.
2. **File → Open** this folder. Android Studio downloads Gradle 8.9 and the dependencies on first sync.
3. **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
   The output is `app/build/outputs/apk/debug/app-debug.apk`.

To build from a terminal, Android Studio first has to create the Gradle wrapper (`gradlew`). After that:

```bash
./gradlew assembleDebug
```

## 2. Prepare the Fire TV Stick

1. **Settings → My Fire TV → About**: select the device name 7 times to unlock **Developer Options** (if it isn't shown already).
2. **Settings → My Fire TV → Developer Options**: turn on **ADB debugging**.
3. **Settings → My Fire TV → About → Network**: note the IP address.
4. Write down the **Fire OS version** under About and add it to `docs/SPEC.md`.

You need `adb`. It comes with Android Studio in `%LOCALAPPDATA%\Android\Sdk\platform-tools`. Add that folder to PATH.

## 3. Install and run

```bash
powershell -File scripts/install.ps1 -FireStickIp 192.168.1.50
```

The first time, the TV asks to allow debugging. Choose **Always allow**.

After that, Miano Home appears under **Your Apps** on the Fire TV home screen. You can move it to the front row.

## 4. Check the app package names

```bash
powershell -File scripts/list-packages.ps1 -FireStickIp 192.168.1.50
```

Compare the output with `AppCatalog.kt`. The Fubo and YouTube names there are marked **VERIFY**. If a package name is wrong, the launcher still finds the app by its label, but fixing the name makes it faster.

## Phase 1 test checklist

- [ ] D-pad moves between the mode pills, the Open button and every app tile; the focused item is obvious
- [ ] Disney+, Fubo, YouTube and RetroArch each open from their tile
- [ ] Home button returns to the Amazon home screen. Miano Home is still under Your Apps.
- [ ] Back on the Miano Home screen does nothing (it doesn't close the app)
- [ ] Volume, mute, power and Alexa still work
- [ ] Real app banners show on the tiles (otherwise placeholder colors)

Making the Home button open Miano Home is a **separate step** that depends on the firmware. Leave it until this checklist passes.
