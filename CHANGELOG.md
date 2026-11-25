# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-11-25

### Added
- **SIAPkerja Kemnaker SSO Integration**: Automated 4-step OAuth2 login (GET login, CSRF token extraction, POST credentials, authorization code exchange for Bearer token) with transparent auto re-login on 401 Unauthorized.
- **Hardware-Backed AES-256 Storage**: Secure credential and session token storage via `EncryptedSharedPreferences` backed by Android Keystore.
- **Dual-Engine Scheduler (16:00 - 23:59 WIB)**:
  - Exact `AlarmManager` wakeup (`setAlarmClock` / `RTC_WAKEUP`) designed to penetrate Doze Mode and deep sleep.
  - `WorkManager` background safety net (`AttendanceCheckWorker`) with network-connected constraints.
  - `BOOT_COMPLETED` and `MY_PACKAGE_REPLACED` broadcast receivers to auto-reschedule alarms after device reboot or app update.
- **Presence & Holiday Awareness**: Automatic holiday (`is_holiday`) and scheduled off-day (`is_scheduled_off_day`) detection, with auto-mute after successful attendance submission.
- **Interactive High-Priority Notifications**:
  - Urgent reminder banner with custom sound and vibration pattern.
  - Direct quick action buttons directly on notifications:
    - 🌐 **Buka Monev** (direct browser intent to Monev MagangHub portal)
    - 🔄 **Cek Ulang** (background attendance re-verification trigger)
    - ⏱️ **Snooze 30 Menit** (30-minute reminder delay)
  - Daily attendance confirmation notifications upon verified presence.
- **Modern Jetpack Compose & Material 3 UI**:
  - Material 3 theming with dynamic status color badges (Emerald Green for attended, Amber/Coral for pending).
  - Real-time dashboard showing current attendance status, schedule countdown, and manual check button.
  - Full configuration settings for Kemnaker credentials, reminder start hour (15:00-18:00 WIB), interval (15-60 min), sound, vibration, and test notification.
- **CI/CD & Release Pipeline**:
  - Automated APK compilation via GitHub Actions.
  - Snapshot pre-releases for `main` branch merges.
  - Official SemVer automated versioning, release notes generation, and signed APK publishing.
