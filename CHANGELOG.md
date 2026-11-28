# Changelog

## [0.3.0] - 2026-11-28

* fix(ui,widget): align holiday purple theme, fix moon icon vector, and handle midnight rollover (#50)
* feat(settings): use interactive sliders for reminder timing, intervals, and mentor thresholds (#49)
* fix(update): force launch update and external links in standalone browser instead of in-app custom tabs (#48)

## [0.2.2] - 2026-11-27

* fix(ui): restrict mentor sync debug telemetry row to preview and debug builds (#47)

## [0.2.1] - 2026-11-27

* fix(mentor): persist participant metadata and pass query parameters to fetchAttendances (#45) (#46)
* fix(widget): ensure synchronous settings persistence and singleton cache for instant widget sync (#44)

## [0.2.0] - 2026-11-26

* docs: add public beta release disclaimers to README and Terms of Service (#43)
* feat(state): centralize work phase resolver and harmonize dashboard-widget state parity (#42)
* feat(widget): trigger on-demand sync from widget entry and instant event update on login (#40)
* feat(copy): streamline presensi and logbook copy to unified logbook entity (#41)
* fix(widget): isolate action button touch target to prevent app redirection (#39)
* fix(ui): pause and resume login dialog when legal document drawer opens (#38)
* perf(build): enable R8 minification and resource shrinking for release builds (#37)
* perf(battery): optimize background sync with confirmed attendance early-return and 3-hour mentor check TTL (#36)
* docs: update README with app screenshots and new features documentation (#34)
* fix(security): resolve CWE security findings across storage, network, and manifest (#35)
* feat(widget): implementasi adaptasi fase harian dan token warna tema widget (#32)
* feat(widget): add previewLayout and static XML preview for widget picker (#33)
* feat(dashboard): implement adaptive daily work phases and work start time picker (#31)
* feat(widget): add footer row with notification status microcopy and sync timestamp (#30)
* feat(settings): add toast and haptic feedback on reminder preferences change (#29)
* feat(dashboard): streamline bottom CTA to full-width dynamic Monev action (#28)
* feat(update): implement in-app update checker matching Figma mockups (#26)
* feat(notification): align notification layout, calm tone, and actions with mockup (#25)
* feat(feedback): implement feedback modal bottom sheet and telegram bridge contract (#23)
* feat(legal): implement privacy policy, terms of service, and settings bantuan card (#24)
* feat(mentor): implement mentor review reminder card, background alerts, and settings (#22)
* fix(reminder): reschedule exact alarm immediately on settings update and display dynamic reminder time (#21)
* feat(ui): make system navigation bar adaptive to theme (#20)
* fix(ci): use direct glob and fail-on-unmatched for snapshot apk release asset (#19)
* feat(ui): implement light and dark mode override theme slider (#14)
* chore(ui): rename settings footer title to MagangHub Logbook Companion (#18)
* feat(ui): unify internship status and day type into single card (#17)
* feat(notification): streamline action buttons to plain snooze and buka monev (#16)
* ci: parallelize snapshot test & build jobs and add preview cleanup workflow (#15)
* chore(version): set project base version to 0.1.0 (alpha) (#12)
* feat(profile): dynamically fetch user profile and display avatar via Coil (#13)
* feat(ui): redesign dashboard and settings with purpose-first utility and background permission (#10)
* chore: update repository references to maganghub-logbook-companion (#11)
* docs: rewrite README with plain language, remove emojis, and rename project to MagangHub Logbook Companion (#9)
* ci: make release APK build on-demand to speed up PR CI verification (#7)
* feat(ci): add dedicated PR preview build workflow (#5)
* feat(ui): align theme and dashboard layout with official Kemnaker Monev web (#4)
* feat(resilience): add anti-ban mobile UA, HTTP 429 backoff, jitter, and SSO circuit breaker (#6)
* refactor: simplify deeply nested conditionals with guard clauses and early exits (#3)
* fix(security): permit Kemnaker cleartext redirects and enforce HTTPS auto-upgrade (#2)
* fix(app): resolve Kotlin compilation errors in MagangHubApiClient and repository (#1)
* feat(release): implement automated snapshot & manual release pipelines matching wallet forwarder
* feat: initial commit for MagangHub Android app (Compose + WorkManager + SSO)

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
