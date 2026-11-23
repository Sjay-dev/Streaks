# Streaks

Streaks is a private, offline Android app for tracking time away from a habit you want to quit. It is designed around dignity, forgiveness, and the freedom to begin again. You choose a discreet goal name; the app does not assume a particular habit, belief, or recovery path.

## What ships

- Multiple independent goals with custom colors and editable start dates and times.
- A live elapsed-time counter that is calculated from stored timestamps and continues while the app is closed.
- Optional private fields for why you are quitting and the first action to take when an urge arrives.
- One-tap urge support that shows the helpful action before asking for an optional reflection. Opening it never changes the timer.
- Setback recording with a chosen date/time, optional note, preserved attempt history, immediate undo, personal best, and duplicate-tap protection.
- A separate archive action, so ending a goal is never treated as a setback.
- Daily local-time reminders with sound or silent delivery and neutral notification previews.
- System, Light, and Dark appearance settings, including readable very light and very dark goal colors.
- A non-destructive Room migration from the v1 database; date-only v1 starts and endings remain date-only instead of receiving invented times.
- Complete deletion of goals, attempts, urge reflections, reminder history, and pending alarms from Settings.

## Privacy and offline behavior

Streaks has no Internet permission, account system, analytics, ads, cloud sync, community features, or remote content. Core flows work in airplane mode. Notification previews never include a goal name, reason, urge plan, or setback note.

The application sets `android:allowBackup="false"`, excluding its private database and preferences from Android backup. Data remains in the app's local storage until the app is uninstalled or **Settings > Delete all private data** is confirmed.

## Fictional walkthrough

1. Create a goal named **Clear sky**, backdate its start if appropriate, and optionally write: “I want more attention for the people I love.”
2. Add an urge action such as “Put my phone down and change rooms,” then choose an optional 8:30 p.m. reminder.
3. From Home, tap **I'm having an urge** to see that action immediately. Closing the dialog leaves the running timer untouched.
4. If a setback happens, choose its time and save it. The completed attempt stays in History and a new timer begins. Use **Undo** on the confirmation if the save was accidental.
5. Use **End / archive this goal** only when you want to stop tracking the goal without recording a setback.

## Build and verify

Requirements: Android Studio or a compatible JDK, plus the Android SDK declared by the project.

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

The debug build is signed with the standard Android debug key and can be installed locally:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

A release build can be compiled with `./gradlew :app:assembleRelease`, but publication signing is intentionally not configured in this repository. Signing and store publication remain under the maintainer's control.

## Architecture

- Kotlin and Jetpack Compose with Material 3
- Room/SQLite with explicit schema migration
- Hilt dependency injection
- Coroutines, Flow, and DataStore preferences
- `AlarmManager` reminders that are recalculated in local wall-clock time after boot, time, timezone, or package changes
