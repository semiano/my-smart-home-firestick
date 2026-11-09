# Builds the debug APK inside Docker.
# Output: app\build\outputs\apk\debug\app-debug.apk
# Usage:  .\scripts\docker-build.ps1            (build the APK)
#         .\scripts\docker-build.ps1 -Wrapper   (also generate gradlew + wrapper jar)
param([switch]$Wrapper)

# "Continue": Docker/Gradle write normal progress to stderr, which Windows PowerShell 5.1
# would otherwise treat as a failure. Exit codes are checked instead.
$ErrorActionPreference = "Continue"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

docker build -t miano-home-builder (Join-Path $root "docker")
if ($LASTEXITCODE -ne 0) { throw "Docker image build failed." }

$cmd = "gradle --no-daemon assembleDebug"
if ($Wrapper) { $cmd = "gradle --no-daemon wrapper --gradle-version 8.9 && $cmd" }

docker run --rm `
    -v "${root}:/project" `
    -v miano-gradle-cache:/root/.gradle `
    -v miano-android-keys:/root/.android `
    miano-home-builder sh -c $cmd
if ($LASTEXITCODE -ne 0) { throw "Gradle build failed." }

Write-Host "APK: $root\app\build\outputs\apk\debug\app-debug.apk"
