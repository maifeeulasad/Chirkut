# ADB wiring for cross-emulator testing

Chirkut peers talk over TCP on port **8989**. The Android emulator runs
each AVD inside its own user-mode NAT (`10.0.2.x`), so two AVDs cannot
reach each other directly — every AVD sees the host as `10.0.2.2` and
nothing else. To round-trip a chat between two emulators we bridge
through the host with `adb forward` + `adb reverse`.

See [`setup.ps1`](setup.ps1) for a runnable version of this; the rest
of this page explains what it does and why.

## Topology

```
┌─ emulator A (5554) ─┐      ┌──── host (ADB server) ────┐      ┌─ emulator B (5556) ─┐
│                     │      │                           │      │                     │
│  app  →  :9000 ─────┼──────┼─ host:28989 ──────────────┼──────┼──→  :8989  (server) │
│ (reverse)           │      │   (adb forward)           │      │                     │
│                     │      │                           │      │                     │
│ :8989  (server) ◄───┼──────┼───────── host:18989 ──────┼──────┼─────── :9000  app   │
│                     │      │   (adb forward)           │      │   (reverse)         │
└─────────────────────┘      └───────────────────────────┘      └─────────────────────┘
```

- `adb forward tcp:H tcp:G` — host port `H` on the ADB server becomes a
  TCP tunnel into guest port `G`. Used to expose each AVD's `:8989` to
  the host.
- `adb reverse tcp:G tcp:H` — guest port `G` becomes a TCP tunnel back
  to host port `H`. Used so the app, when it dials `127.0.0.1:9000` on
  the guest, actually lands on the other AVD's `:8989` via the host.

## The commands

```sh
ADB="C:/projecs/androidsdk/platform-tools/adb.exe"

# Expose each guest's chirkut server to the host (unique host ports).
"$ADB" -s emulator-5554 forward tcp:18989 tcp:8989
"$ADB" -s emulator-5556 forward tcp:28989 tcp:8989

# Reverse: guest :9000 -> host tunnel into the OTHER guest's :8989.
"$ADB" -s emulator-5554 reverse tcp:9000 tcp:28989
"$ADB" -s emulator-5556 reverse tcp:9000 tcp:18989
```

After this, each emulator can reach the other by dialing
`127.0.0.1:9000` — type that into Chirkut's "Connect to a peer" field
on either device.

**Why 9000 and not 8989?** `adb reverse tcp:8989 ...` would try to bind
`:8989` on the guest, which is already held by Chirkut's own
`ServerSocket`. Any free port on the guest works; 9000 is the
convention used here.

## Required app behaviour

The peer field accepts a raw host or `host:port`. On connect, Chirkut's
`Client` sends `HELLO|<self_addr>\n` so the receiver knows which
address to dial back — a plain inbound source IP (`127.0.0.1` through
a reverse tunnel) wouldn't be reachable from the receiver's side
otherwise. See
[`network/Client.java`](../../chirkut/app/src/main/java/com/maifee/chirkut/network/Client.java)
and
[`service/MessagingService.java`](../../chirkut/app/src/main/java/com/maifee/chirkut/service/MessagingService.java).

## Tear-down

Bridges survive emulator reboots (they live in the ADB server) but die
when `adb kill-server` runs. To remove explicitly:

```sh
"$ADB" -s emulator-5554 forward --remove-all
"$ADB" -s emulator-5556 forward --remove-all
"$ADB" -s emulator-5554 reverse --remove-all
"$ADB" -s emulator-5556 reverse --remove-all
```

## Not emulators?

Two physical phones on the same Wi-Fi don't need any of this — enter
the other phone's LAN IP (e.g. `192.168.1.42`) in the Connect field
and the default port 8989 applies. The adb bridge only exists to work
around the emulator NAT isolation.
