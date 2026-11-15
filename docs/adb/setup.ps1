# Bridge two running AVDs so Chirkut can round-trip chat/calls between
# them. See README.md in this folder for the topology.
#
# Usage:
#   .\setup.ps1
#   .\setup.ps1 -A emulator-5554 -B emulator-5556
#   .\setup.ps1 -Reset       # tear down the bridge

param(
    [string]$Adb = 'C:\projecs\androidsdk\platform-tools\adb.exe',
    [string]$A = 'emulator-5554',
    [string]$B = 'emulator-5556',
    [int]$HostPortA = 18989,
    [int]$HostPortB = 28989,
    [int]$GuestPort = 9000,
    [int]$ChirkutPort = 8989,
    [switch]$Reset
)

& $Adb -s $A reverse --remove-all | Out-Null
& $Adb -s $B reverse --remove-all | Out-Null
& $Adb -s $A forward --remove-all | Out-Null
& $Adb -s $B forward --remove-all | Out-Null

if ($Reset) {
    Write-Output "Bridges removed on $A and $B."
    exit 0
}

& $Adb -s $A forward "tcp:$HostPortA" "tcp:$ChirkutPort" | Out-Null
& $Adb -s $B forward "tcp:$HostPortB" "tcp:$ChirkutPort" | Out-Null
& $Adb -s $A reverse "tcp:$GuestPort" "tcp:$HostPortB" | Out-Null
& $Adb -s $B reverse "tcp:$GuestPort" "tcp:$HostPortA" | Out-Null

Write-Output "Bridges up:"
Write-Output "  $A :$GuestPort  (outbound)  ->  host :$HostPortB  ->  $B :$ChirkutPort"
Write-Output "  $B :$GuestPort  (outbound)  ->  host :$HostPortA  ->  $A :$ChirkutPort"
Write-Output ""
Write-Output "In Chirkut on either device, enter peer: 127.0.0.1:$GuestPort"
