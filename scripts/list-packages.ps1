# Lists the installed package names for the apps Miano Home links to,
# so app/src/main/java/com/miano/home/launch/AppCatalog.kt can be checked.
# Usage:  .\scripts\list-packages.ps1 -FireStickIp 192.168.1.50
param([Parameter(Mandatory = $true)][string]$FireStickIp)

$target = "$($FireStickIp):5555"
adb connect $target | Out-Null
adb -s $target shell pm list packages |
    Select-String -Pattern "disney|fubo|youtube|retroarch" |
    ForEach-Object { $_.Line -replace "^package:", "" }
