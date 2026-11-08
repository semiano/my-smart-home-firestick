# Installs the debug APK on the Fire TV Stick over Wi-Fi ADB and opens it.
# Usage:  .\scripts\install.ps1 -FireStickIp 192.168.1.50
param(
    [Parameter(Mandatory = $true)][string]$FireStickIp,
    [string]$Apk = (Join-Path $PSScriptRoot "..\app\build\outputs\apk\debug\app-debug.apk")
)

$ErrorActionPreference = "Stop"
$target = "$($FireStickIp):5555"

if (-not (Test-Path $Apk)) {
    throw "APK not found at $Apk. Build it first (Android Studio: Build > Build APK(s), or .\gradlew assembleDebug)."
}

adb connect $target
Write-Host "If the TV shows 'Allow USB debugging?', choose Always allow, then re-run this script if install fails."
adb -s $target install -r $Apk
adb -s $target shell am start -n com.miano.home/.MainActivity
