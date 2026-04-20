# Solo ToDo — Android

Solo Leveling–themed productivity app. Native Android (Kotlin + Jetpack Compose).

**Status:** Phase 0 — scaffolding. Empty project that builds and launches to a placeholder screen.

- Design source: `C:\Users\noamm\solo-todo-design\sololeveling\` (readonly reference)
- Implementation plan: `C:\Users\noamm\.claude\plans\how-will-you-do-jazzy-lake.md`

---

## What's in here right now

```
solo-todo/
  settings.gradle.kts         # Single module :app for now (multi-module split in Phase 1)
  build.gradle.kts            # Root: plugin aliases only
  gradle.properties           # JVM args, caching, AndroidX flags
  gradle/
    libs.versions.toml        # Pinned versions: Kotlin 2.1.0, AGP 8.7.3, Compose BOM 2025.01.00, etc.
    wrapper/
      gradle-wrapper.properties   # Gradle 8.11.1
  app/
    build.gradle.kts          # :app module — Compose, Hilt, Room, Navigation, DataStore wired
    proguard-rules.pro
    src/main/
      AndroidManifest.xml
      java/com/solotodo/
        SoloTodoApp.kt              # @HiltAndroidApp entry
        MainActivity.kt             # Compose host
        ui/placeholder/
          PlaceholderScreen.kt      # "SYSTEM · SOLO TODO · Phase 0 ready"
        designsystem/
          Tokens.kt                 # All color/spacing/motion tokens ported from styles.css
          theme/
            SoloTodoTheme.kt        # MaterialTheme dark-only wrapper
      res/
        values/{strings,colors,themes}.xml
        xml/{backup_rules,data_extraction_rules}.xml
        mipmap-anydpi-v26/ic_launcher{,_round}.xml
        drawable/ic_launcher_foreground.xml     # Placeholder icon; real icon commissioned later
```

---

## Phase 0 — First-run setup (verify when you're home)

You already have Android Studio + SDK installed (platforms 34/35/36, build-tools 36.1.0). So:

1. **Open the project**
   - Launch Android Studio → **File → Open** → pick `C:\Users\noamm\solo-todo`
   - Let AS sync Gradle. First sync downloads Gradle 8.11.1 + all deps (~1-2 min depending on cache).

2. **If AS prompts about a missing Gradle wrapper jar:**
   - Open the built-in **Terminal** tab in AS
   - Run: `gradle wrapper --gradle-version 8.11.1`
   - Re-sync (Elephant icon on toolbar, or `File → Sync Project with Gradle Files`)
   - This is a one-time step; the `gradlew` / `gradlew.bat` scripts land in the project root and should be committed.

3. **Pick a run target**
   - **Option A (easiest):** enable USB debugging on your phone, plug it in, accept the prompt.
   - **Option B:** Tools → Device Manager → create a Pixel 7 Pro AVD with API 35.

4. **Run**
   - Click ▶ Run (or Shift+F10). The app installs and launches.
   - Expected screen: dark void background, cyan `[ SYSTEM ]` label, `SOLO TODO` heading, and `Phase 0 ready. Awakening pending.` underneath.

5. **Report back:**
   - Either ✅ it builds and shows the placeholder → we move to Phase 1, OR
   - ❌ paste the Gradle sync / build error and I'll fix it.

---

## Phase 0 success criteria

- [ ] `File → Open` against `solo-todo/` completes with zero red errors in Build Output
- [ ] `./gradlew :app:assembleDebug` (from AS Terminal) succeeds
- [ ] Running on device/emulator shows the placeholder screen
- [ ] Launcher icon shows the placeholder cyan diamond

---

## Next phases (summary)

| Phase | What | Where you'll feel it |
|---|---|---|
| 1 | Design system port — Panel, RankGlyph, DiamondCheck, fonts, scanline | Gallery screen matches prototype visuals |
| 2 | Data layer — Room schemas, DAOs, repositories | Tests pass; data persists across restarts |
| 3 | Daily Quest FSM + Status/Quests tabs | Add tasks, complete them, streak counts |
| 4 | Auth + Supabase + sync | Sign in, see tasks across devices |
| 5 | Cinematics + rank progression | Rank up on the correct day; cinematics play |
| 6 | Onboarding + Profile + Archive + Widgets | Awakening flows; home-screen widget works |
| 7 | Dungeons + Settings + Weekly Reflection | Multi-floor projects; weekly wrap-up |
| 8 | Monetization — ads + IAP | Purchase removes ads |
| 9 | Polish + beta + launch | Play Store open testing, then production |

Full plan: `C:\Users\noamm\.claude\plans\how-will-you-do-jazzy-lake.md`

---

## Design principles (copied from HANDOFF.md + chat transcript)

- **The System voice:** short, imperative, no emoji, no first-person. Uppercase at key moments; title-case fallback available as accessibility setting.
- **Penalty is non-punitive:** copy uses "atone" / "recovering", not "failed" / "broken". Streak breaks, data never does.
- **IP distance:** inspired by Solo Leveling (the webtoon/anime). No character names, no recreated panels, no sampled audio. Independent legal review before marketing.
- **Sound:** commissioned original SFX. Never AI-generated or sampled from anime/games.
- **Prototype wins disagreements** with code until explicitly renegotiated. When in doubt, reopen `Solo ToDo.html` in a browser.

---

## Monetization model (overrides HANDOFF.md — see chat transcript)

- **Free tier:** full app with AdMob ads (banner + interstitial on specific triggers).
- **Remove ads monthly:** $1.99 — IAP product ID `remove_ads_monthly`.
- **Remove ads lifetime:** $7.99 — IAP product ID `remove_ads_lifetime`.
- **No "Pro" tier.** Customization unlocks come with either paid option.

---

## Dev notes for future-me (Claude)

- Build config expects `minSdk = 26`. Adaptive launcher icons work natively; no legacy PNG fallback needed.
- Hilt is wired but currently has zero bindings. First bindings land in Phase 2 (repositories).
- Edge-to-edge is on (`enableEdgeToEdge()` in MainActivity + transparent status/nav bars in theme). Respect insets with `WindowInsets` in every screen.
- Room + KSP configured. Schemas for Phase 2 live in `data/local/entity/` (to be created).
- ProGuard rules protect Room entities and `kotlinx.serialization` — add new keeps as we add annotated classes.
- No analytics / crash reporting yet. Add in Phase 3 (PostHog) and Phase 4 (Crashlytics + FCM).
