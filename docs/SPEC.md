# Miano Home — Functional Spec

A custom home-screen launcher for Fire OS that turns the TV into the family's main household hub. It covers entertainment, calendar, tasks, maintenance, projects, kids' habits, smart home and an AI assistant.

## 1. Target device

| Item | Value |
|---|---|
| Device | Amazon Fire TV Stick 4K Max (1st Gen, 2021) |
| OS | Fire OS 7.x (Android 9 / API 28 base). **Check the exact build on the device** (Settings → My Fire TV → About) |
| RAM / storage | 2 GB / 8 GB. Keep the TV app a thin client |
| Input | Alexa Voice Remote (D-pad, Select, Back, Home, Menu, Play/Pause, Volume, Power, Mic) |

> Note: the source conversation mentioned Home-redirect tools built for **Fire OS 8**. This device runs **Fire OS 7**, so the Home-button approach has to be tested against this device's firmware.

## 2. Core principle

Keep Fire OS underneath; don't flash a custom ROM. Miano Home is a normal Android APK that acts as the home screen. Fire OS keeps handling Wi-Fi, Bluetooth, video/audio, DRM, 4K/HDR/Dolby and Alexa. Everything can be undone: uninstall the launcher and the Home redirector, and the stock Amazon UI comes back.

## 3. Must keep working

- Disney+, Fubo and YouTube, using the **official Fire TV builds** (launched, never rebuilt)
- RetroArch / SNES / arcade games
- Fire TV remote: navigation, select, back, play/pause, volume, mute, TV power
- Alexa voice features (system-level)

## 4. Boot & navigation flow

1. Fire OS boots as normal.
2. Miano Home appears, through Home-button redirection when available; otherwise it is opened like any app.
3. The user picks a tile, and the launcher opens the installed app through Android's package manager (launch intent).
4. The app runs on its own. **Back** is handled by that app.
5. **Home** returns to Miano Home if redirection works on this firmware.
6. Retro Games opens RetroArch, or a specific game through RetroArch's launch integration.

### Remote mapping

| Button | Behavior |
|---|---|
| D-pad | Move focus across menus and game lists |
| Select | Open the focused item |
| Back | Previous screen (in-app: the app's own behavior) |
| Home | Return to Miano Home (*firmware-dependent*) |
| Play/Pause | Passed through to the media app |
| Volume/Mute/Power | Unchanged (HDMI-CEC / IR) |
| Mic | Amazon's Alexa. **Cannot be taken over by third-party apps** |
| Dedicated app buttons | Remapping not guaranteed without root |

## 5. Household features

| Module | Scope |
|---|---|
| Entertainment | Disney+, Fubo, YouTube, RetroArch tiles |
| Family Calendar | Events, school, appointments (synced from external calendars) |
| Tasks | Personal and shared to-dos |
| Maintenance | HVAC, generator, filters, seasonal work, history log, recurring schedules |
| Projects | Renovations, garden, plans: milestones and blockers |
| Kids' Habits | Morning/bedtime routines, rewards |
| Smart Home | Lights, sensors, climate, utilities (through Home Assistant) |
| AI Assistant | Voice/text queries and authorized actions |

### Profiles

- **Adult / family profile**: full access.
- **Kids Mode** (Style D): habit boards, rewards and allowed content only. No admin, projects or consequential actions.

### Household modes (context layouts)

- **Living Room**: entertainment first, with unobtrusive reminders
- **Morning Brief**: today's agenda, chores, habits
- **Family Time**: shared activities
- **Home Management**: operations dashboard

## 6. UI style directions

- **A. Cinematic Living Room**: full-width artwork, large app tiles, compact strip for events, chores and status
- **B. Warm Family Command Center**: cream/sage neutrals, readable cards
- **C. Modern Mission Control**: dark theme, metrics, project progress, maintenance alerts, AI quick actions
- **D. Playful Family Profiles**: game-like, avatars, habit boards, rewards

**Chosen:** a hybrid of A and C for the main TV screen, with D for Kids Mode.

## 7. Architecture

```
Fire TV Stick (thin client)
  Miano Home launcher — Kotlin + Jetpack Compose for TV
    ├─ Streaming app launcher
    ├─ Family dashboard
    └─ Kids Mode
            │  HTTPS + WebSocket
            ▼
Home server (source of truth)
  Household API — FastAPI or Node.js, auth, WebSocket push
    ├─ PostgreSQL        tasks, habits, projects, maintenance
    ├─ Home Assistant    sensors, scenes, devices
    ├─ Calendar connectors
    └─ AI + MCP gateway  OpenAI reasoning + allowlisted tools

Phone/desktop web UI — same API (data entry is easier off the TV)
```

Changes show up everywhere straight away: a chore marked done on the TV appears on the phone right away.

## 8. AI & voice

### Voice routes

1. **Alexa Custom Skill.** Uses the existing remote. Alexa → skill (Lambda/HTTPS) → home AI gateway. Limited to an invocation phrase plus defined intents ("Alexa, ask Miano Home what's on our calendar"). It does not give an open microphone stream.
2. **Native voice satellite.** A separate mic on the home server or a voice satellite → speech recognition / realtime AI → orchestrator + MCP. Allows natural conversation ("Miano, what's left on the garage project?").

### MCP tools (allowlist)

| Tool | Purpose |
|---|---|
| `calendar.get_agenda` | Read upcoming events |
| `tasks.create` | Add a household task |
| `maintenance.log` | Record completed maintenance |
| `projects.get_status` | Milestones and blockers |
| `habits.mark_complete` | Update a child's routine |
| `home.get_status` | Read sensors |
| `home.run_scene` | Run approved scenes |

### Guardrails

- The backend validates permissions and parameters before running anything.
- Explicit confirmation for sensitive actions: unlocking, purchases, deletions, security changes.
- Per-user permissions, execution logs, no unrestricted shell access for the model.
- Read-only tools ship first; write actions come later.

## 9. Build phases

1. **TV foundation**: launcher APK, remote focus navigation, profiles, streaming tiles, RetroArch, basic dashboard. Validate Disney+, Fubo, YouTube and the remote **before** any Home redirection.
2. **Household data**: calendar, tasks, maintenance, projects, kids' routines, plus the phone web UI.
3. **AI**: OpenAI assistant with read-only tools, then authorized writes.
4. **Voice**: Alexa Custom Skill, then an optional local voice satellite.
5. **Automation**: daily briefings, recurring maintenance, routine rewards, smart-home scenes.

## 10. Known risks / open questions

- Home-button redirection on **Fire OS 7** for this device: method and reliability need testing. Firmware updates may break it.
- Dedicated app-button remapping without root: not guaranteed.
- The Alexa mic can't be captured by the app.
- Home server hardware/OS not yet decided.
- Backend stack choice (FastAPI vs Node.js) not yet decided.
