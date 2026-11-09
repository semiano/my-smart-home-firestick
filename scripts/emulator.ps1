# Runs Miano Home in an Android TV emulator on Windows (no Android Studio needed).
#
# Android TV 9 (API 28) is the closest public match to Fire OS 7. Amazon doesn't
# publish a Fire TV emulator, so Amazon-only apps and the Appstore aren't available here.
#
# Usage:
#   .\scripts\emulator.ps1            first run: installs everything (~3-4 GB), then starts the TV
#   .\scripts\emulator.ps1 -NoInstall start the emulator and install/launch the APK only
#
# Installs into %LOCALAPPDATA%\MianoHome (portable JDK + Android SDK). Delete that folder to remove it all.
# Needs the Windows "Windows Hypervisor Platform" feature for acceleration.
param([switch]$NoInstall)

# "Continue": sdkmanager/adb/emulator write normal output to stderr, which Windows
# PowerShell 5.1 would treat as a failure. Downloads use -ErrorAction Stop instead.
$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

$root    = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$base    = Join-Path $env:LOCALAPPDATA "MianoHome"
$jdkDir  = Join-Path $base "jdk"
$sdk     = Join-Path $base "android-sdk"
$avdName = "MianoTV"
$image   = "system-images;android-28;android-tv;x86"
$apk     = Join-Path $root "app\build\outputs\apk\debug\app-debug.apk"

New-Item -ItemType Directory -Force $base | Out-Null
$env:ANDROID_HOME     = $sdk
$env:ANDROID_SDK_ROOT = $sdk
$env:ANDROID_AVD_HOME = Join-Path $base "avd"
New-Item -ItemType Directory -Force $env:ANDROID_AVD_HOME | Out-Null

function Get-Zip($url, $dest) {
    $zip = Join-Path $base "download.zip"
    Write-Host "Downloading $url"
    Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing -ErrorAction Stop
    Expand-Archive -Path $zip -DestinationPath $dest -Force -ErrorAction Stop
    Remove-Item $zip
}

if (-not $NoInstall) {
    # 1. Portable JDK 17 (needed by sdkmanager/avdmanager)
    if (-not (Test-Path (Join-Path $jdkDir "bin\java.exe"))) {
        $tmp = Join-Path $base "jdk-tmp"
        Get-Zip "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse" $tmp
        $inner = Get-ChildItem $tmp -Directory | Select-Object -First 1
        Move-Item $inner.FullName $jdkDir
        Remove-Item $tmp -Recurse -Force
    }

    # 2. Android command-line tools
    $toolsBin = Join-Path $sdk "cmdline-tools\latest\bin"
    if (-not (Test-Path (Join-Path $toolsBin "sdkmanager.bat"))) {
        $tmp = Join-Path $base "tools-tmp"
        Get-Zip "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip" $tmp
        New-Item -ItemType Directory -Force (Join-Path $sdk "cmdline-tools") | Out-Null
        Move-Item (Join-Path $tmp "cmdline-tools") (Join-Path $sdk "cmdline-tools\latest")
        Remove-Item $tmp -Recurse -Force
    }
}

$env:JAVA_HOME = $jdkDir
$env:Path = "$jdkDir\bin;$sdk\cmdline-tools\latest\bin;$sdk\platform-tools;$sdk\emulator;$env:Path"

if (-not $NoInstall) {
    # 3. Emulator + Android TV 9 system image (accepts the Android SDK License Agreement)
    Write-Host "Installing emulator and Android TV image (this is the big download)..."
    $yesFile = Join-Path $base "yes.txt"
    Set-Content -Path $yesFile -Value ((1..50 | ForEach-Object { "y" }) -join "`r`n") -Encoding ascii
    cmd /c "sdkmanager.bat --licenses < `"$yesFile`" > nul"
    cmd /c "sdkmanager.bat platform-tools emulator `"$image`" < `"$yesFile`""
    if ($LASTEXITCODE -ne 0) { throw "sdkmanager failed." }
    if (-not (Test-Path (Join-Path $sdk "emulator\emulator.exe"))) { throw "Emulator didn't install; see output above." }

    # 4. TV device (1080p, 2 GB RAM like the Fire Stick 4K Max)
    $avdList = (& avdmanager.bat list avd) -join "`n"
    if ($avdList -notmatch "Name: $avdName") {
        "no" | avdmanager.bat create avd -n $avdName -k $image -d "tv_1080p" --force
        $ini = Join-Path $env:ANDROID_AVD_HOME "$avdName.avd\config.ini"
        Add-Content $ini "hw.ramSize=2048`nhw.keyboard=yes`nhw.dPad=yes`nhw.gpu.enabled=yes`nhw.gpu.mode=auto"
    }
}

# 5. Acceleration check
$accel = (& emulator.exe -accel-check 2>&1) -join "`n"
Write-Host $accel
if ($accel -notmatch "is installed and usable") {
    Write-Warning "No hardware acceleration. Turn on 'Windows Hypervisor Platform' in Windows Features (needs admin + reboot), then re-run with -NoInstall."
    exit 1
}

# 6. Start the emulator (unless one is already running)
$running = (& adb.exe devices) -match "emulator-"
if (-not $running) {
    Write-Host "Starting the TV emulator..."
    Start-Process emulator.exe -ArgumentList "-avd", $avdName, "-no-snapshot-save", "-no-boot-anim"
}

adb.exe wait-for-device
Write-Host "Waiting for Android to finish booting..."
do {
    Start-Sleep -Seconds 3
    $booted = (& adb.exe shell getprop sys.boot_completed 2>$null) -join ""
} until ($booted.Trim() -eq "1")

# 7. Install and open Miano Home
if (-not (Test-Path $apk)) { throw "APK not found at $apk. Run .\scripts\docker-build.ps1 first." }
adb.exe install -r $apk
adb.exe shell am start -n com.miano.home/.MainActivity
Write-Host "Miano Home is running. Arrow keys = D-pad, Enter = Select, Esc = Back, Home = Home."
