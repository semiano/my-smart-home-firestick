# My Smart Home Firestick (Miano Home)

Custom Fire OS launcher + household hub. Full spec: [docs/SPEC.md](docs/SPEC.md).

## Key constraints
- Device: Fire TV Stick 4K Max **1st Gen (2021)**, Fire OS 7 (Android 9 / API 28), 2 GB RAM. TV app stays a thin client.
- Never require root or a custom ROM. Launch official Disney+, Fubo, YouTube and RetroArch through package-manager intents.
- Every screen must work with D-pad focus navigation only (Alexa Voice Remote).
- Home server is the source of truth. TV, phone and web are all clients of the same API.
- AI tools are allowlisted, permission-checked and logged. Sensitive actions need confirmation.

## Code layout
- Android app in `app/` (package `com.miano.home`), build/run steps in README.md.
- App shortcuts + package names: `launch/AppCatalog.kt`. Data comes through `data/HouseholdRepository.kt` (sample data until the API exists).
- UI sizes are dp for a 960x540 dp TV canvas with ~48dp side margins.

## Planned stack
- TV: Kotlin + Jetpack Compose for TV
- Backend: FastAPI or Node.js (TBD), PostgreSQL, WebSocket push, Home Assistant, MCP gateway + OpenAI
- UI: Style A+C hybrid for the main screen, Style D for Kids Mode
