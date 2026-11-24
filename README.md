# 🚀 MagangHub Android

[![Build & Release Android APK](https://github.com/maganghub/maganghub-android/actions/workflows/build-apk.yml/badge.svg)](https://github.com/maganghub/maganghub-android/actions/workflows/build-apk.yml)
[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.04.01-informational.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-teal.svg)](https://m3.material.io)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

> **Asisten Pengingat & Pemantau Presensi MagangHub Kemnaker Otomatis untuk Android**  
> Mengeliminasi risiko lupa mengisi presensi dan logbook harian dengan spam reminders cerdas pukul **16:00 – 23:59 WIB**.

---

## 📋 Daftar Isi
1. [Latar Belakang](#-latar-belakang)
2. [Fitur Utama](#-fitur-utama)
3. [Arsitektur Aplikasi](#-arsitektur-aplikasi)
4. [Download & Cara Instalasi APK](#-download--cara-instalasi-apk)
5. [Panduan Pengaturan di Android (Anti-Kill OS)](#-panduan-pengaturan-di-android-anti-kill-os)
6. [Kompilasi & Build dari Source Code](#-kompilasi--build-dari-source-code)
7. [Keamanan & Privasi Kredensial](#-keamanan--privasi-kredensial)
8. [Siklus Rilis & Versioning](#-siklus-rilis--versioning)
9. [CI/CD Workflow](#-cicd-workflow)

---

## 🎯 Latar Belakang

Bagi peserta program magang bersertifikat Kementerian Ketenagakerjaan Republik Indonesia (Kemnaker) di portal **MagangHub Monev**, pengisian presensi kehadiran dan logbook harian memiliki batas waktu ketat setiap harinya hingga **23:59 WIB**. Jika terlewat satu hari saja, presensi berstatus alpha/tidak hadir dan tidak dapat disusul kembali.

**MagangHub Android** memporting kehandalan bot pemantau Go backend (`maganghub-checker-bot`) menjadi aplikasi mobile Android mandiri yang ringan, hemat daya, dan tahan banting terhadap Doze Mode sistem operasi Android modern.

---

## ✨ Fitur Utama

### 1. ⏰ Dual-Engine Background Scheduler (16:00 – 23:59 WIB)
* **AlarmManager Exact Receiver (`RTC_WAKEUP`)**: Menggunakan `setAlarmClock` yang dijamin oleh sistem Android untuk menembus Doze Mode pada jam pengingat tanpa tertunda oleh deep sleep perangkat.
* **WorkManager Periodic Safety Net**: Menjalankan pengecekan periodik berbasis batasan jaringan (`NetworkType.CONNECTED`) sebagai lapis cadangan jika AlarmManager dibatasi oleh sistem.
* **Boot & Package Update Receiver**: Otomatis mendaftarkan dan menjadwalkan ulang alarm segera setelah smartphone dinyalakan ulang (`BOOT_COMPLETED`) atau setelah pembaruan aplikasi (`MY_PACKAGE_REPLACED`).
* **Siklus Libur Cerdas**: Otomatis mendeteksi hari libur nasional atau jadwal libur mingguan (`is_holiday` / `is_scheduled_off_day`) dan tidak akan mengganggu Anda di hari libur.
* **Auto-Mute Pasca-Absen**: Segera setelah Anda mengisi presensi, scheduler langsung menonaktifkan pengingat hari itu dan tidur lelap hingga jadwal esok hari pukul 16:00 WIB.

### 2. 🔔 Notifikasi Prioritas Tinggi & Quick Actions
* **High-Priority Channel**: Notifikasi muncul dengan status heads-up banner disertai suara dan pola getaran khas.
* **Quick Action Buttons Langsung di Notifikasi**:
  * 🌐 **Buka Monev**: Membuka dashboard presensi Monev MagangHub langsung di browser default hanya dengan satu sentuhan.
  * 🔄 **Cek Ulang**: Memeriksa ulang status ke server Kemnaker di latar belakang tanpa harus membuka aplikasi.
  * ⏱️ **Snooze 30 Menit**: Menunda notifikasi berikutnya selama 30 menit jika Anda sedang di jalan atau rapat.
* **Notifikasi Konfirmasi Harian**: Ucapan selamat sekali sehari ketika presensi berhasil diverifikasi sudah masuk.

### 3. 🛡️ Otentikasi SIAPkerja Kemnaker SSO 4-Tahap
* Porting lengkap otomasi autentikasi dari Go:
  1. `GET /auth/login` MagangHub API untuk mendapatkan URL autorisasi SSO.
  2. `GET SSO authorization page` dengan penanganan cookie sesi dan ekstraksi `csrf-token`.
  3. `POST /auth/login` kredensial akun Kemnaker dengan header `X-CSRF-TOKEN` dan penangkapan kode otorisasi via HTTP 302 Location.
  4. `GET /auth/login/callback` penukaran authorization code dengan Bearer token Monev.
* **Transparan Auto Re-Login**: Jika token sesi kedaluwarsa (HTTP 401), aplikasi otomatis memperbarui token tanpa mengganggu pengguna.
* **Encrypted Storage**: Kredensial akun dan token disimpan secara aman menggunakan enkripsi AES-256 hardware Keystore (`EncryptedSharedPreferences`).

### 4. 🎨 Modern Jetpack Compose & Material 3 UI
* Palet warna adaptif: Emerald Green tenang untuk status "Sudah Absen", Amber/Coral tegas untuk status "Belum Absen".
* Status card real-time dengan countdown/waktu pengingat berikutnya.
* Mode pengaturan lengkap: ubah jam mulai pengingat (default: 16:00 WIB), interval peringatan (default: 30 menit), serta toggle suara dan getar.
* Tombol uji notifikasi instan untuk memastikan konfigurasi channel berjalan baik di perangkat Anda.

---

## 🏗️ Arsitektur Aplikasi

Aplikasi dibangun dengan prinsip **Clean Architecture** dan pola **MVVM (Model-View-ViewModel)** modern:

```mermaid
flowchart TD
    subgraph UI ["Layer UI (Jetpack Compose & Material 3)"]
        A[MainActivity] --> B[DashboardScreen]
        A --> C[SettingsScreen & LoginDialog]
        B --> D[AttendanceViewModel]
        C --> D
    end

    subgraph Background ["Layer Background & Notification"]
        E[BootReceiver] -->|Reschedule on Boot| G[AttendanceAlarmScheduler]
        F[AttendanceAlarmReceiver] -->|Alarm Trigger / Snooze| G
        G -->|Exact Alarm Wakeup| F
        F -->|Trigger Worker| H[AttendanceCheckWorker]
        I[NotificationHelper] <-- Displays & Actions --> F
        H -->|Show Alerts| I
    end

    subgraph Data ["Layer Data & Network"]
        D <--> J[AttendanceRepository]
        H <--> J
        J <--> K[MagangHubApiClient]
        J <--> L[EncryptedPreferences]
        K -->|4-Step SSO & Fetch Status| M[(Kemnaker SSO & Monev API)]
        L -->|AES-256 Storage| N[(Android Keystore)]
    end
```

### Struktur Direktori

```
app/src/main/
├── AndroidManifest.xml
├── java/id/codes/maganghub/
│   ├── MagangHubApp.kt                     # Application class & DI locator
│   ├── background/                         # Subsistem Penjadwalan & Notifikasi
│   │   ├── AttendanceAlarmScheduler.kt     # Pengatur jam exact alarm (WIB UTC+7)
│   │   ├── AttendanceAlarmReceiver.kt     # Penerima sinyal alarm & aksi tombol notifikasi
│   │   ├── AttendanceCheckWorker.kt       # CoroutineWorker WorkManager
│   │   ├── BootReceiver.kt                # Penerima BOOT_COMPLETED setelah restart
│   │   └── NotificationHelper.kt          # Kanal notifikasi, layout, & aksi cepat
│   ├── data/                               # Lapisan Data & Jaringan
│   │   ├── api/
│   │   │   ├── MagangHubApiClient.kt      # Klien HTTP & SSO 4-Step Kemnaker
│   │   │   ├── InMemoryCookieJar.kt       # Manajemen cookie sesi redirect SSO
│   │   │   └── Exceptions.kt              # Hierarki exception API & Auth
│   │   ├── model/
│   │   │   ├── AttendanceHomeData.kt      # Model respons endpoint /users/me/home
│   │   │   ├── AttendanceStatus.kt        # Enum status presensi (HADIR, dsb.)
│   │   │   └── UserSession.kt             # Data session & preferensi pengingat
│   │   ├── repository/
│   │   │   ├── AttendanceRepository.kt    # Kontrak repository
│   │   │   └── AttendanceRepositoryImpl.kt# Implementasi sinkronisasi & cache
│   │   └── storage/
│   │       └── EncryptedPreferences.kt    # Enkripsi kredensial AES-256
│   └── ui/                                 # Antarmuka Jetpack Compose
│       ├── MainActivity.kt                # Activity utama aplikasi
│       ├── components/                    # Komponen UI modular
│       ├── screens/                       # DashboardScreen & SettingsScreen
│       ├── theme/                         # Color, Theme, & Typography M3
│       └── viewmodel/
│           ├── AttendanceViewModel.kt     # Logika state UI & handler user
│           └── AttendanceUiState.kt       # State immutability holder
```

---

## 📥 Download & Cara Instalasi APK

### Pilihan Unduhan APK
1. **Official Release APK (`maganghub-v<version>.apk`)**:
   - Unduh rilis stabil APK resmi bertanda tangan dari tab **[GitHub Releases](../../releases)**.
   - Direkomendasikan untuk penggunaan harian oleh seluruh peserta magang.
2. **Snapshot Pre-Release APK (`maganghub-v<version>-snapshot.<sha>.apk`)**:
   - Setiap kali terjadi push atau merge kode ke branch `main`, workflow snapshot otomatis mengompilasi dan mempublikasikan preview build di [Releases](../../releases) bertanda tag `v<version>-snapshot.<sha>`.
   - Cocok untuk menguji fitur atau perbaikan terbaru sebelum rilis resmi diterbitkan.
3. **CI Build Artifacts (`maganghub-pr-apk`)**:
   - Pengembang dapat mengunduh artefak APK dari tab **[GitHub Actions](../../actions)** pada setiap pull request dan commit build verification.

### Menginstal di Smartphone Android
1. Unduh file `.apk` ke ponsel Android Anda.
2. Buka file `.apk` melalui aplikasi File Manager atau browser download manager.
3. Jika muncul peringatan keamanan *"Instal aplikasi dari sumber tidak dikenal"*, pilih **Setelan / Settings** lalu aktifkan **Izinkan untuk sumber ini**.
4. Tekan **Instal** dan tunggu hingga proses instalasi selesai.

---

## ⚙️ Panduan Pengaturan di Android (Anti-Kill OS)

Sistem operasi seperti **MIUI / HyperOS (Xiaomi)**, **ColorOS (Oppo/Realme)**, **FuntouchOS (Vivo)**, dan **One UI (Samsung)** memiliki manajemen RAM agresif yang dapat membekukan background process. Ikuti langkah berikut agar alarm presensi 100% selalu berbunyi tepat waktu:

1. **Izinkan Notifikasi (Android 13+)**:
   * Saat pertama kali membuka aplikasi, tekan **Allow / Izinkan** saat muncul dialog permintaan izin notifikasi.
2. **Izin Alarm & Pengingat Tepat Waktu (Android 12+)**:
   * Masuk ke *Pengaturan HP* > *Aplikasi* > *MagangHub* > *Alarm & Pengingat* > Aktifkan **Izinkan penyetelan alarm tepat waktu** (*Allow setting exact alarms*).
3. **Penghemat Baterai (Battery Optimization)**:
   * Masuk ke *Pengaturan HP* > *Aplikasi* > *MagangHub* > *Baterai* > Pilih **Tidak Dibatasi / Unrestricted** (Jangan dioptimalkan).
4. **Autostart / Mulai Otomatis (Khusus Xiaomi / Oppo / Vivo)**:
   * Masuk ke info aplikasi MagangHub > Aktifkan toggle **Mulai Otomatis / Autostart**.

---

## 🛠️ Kompilasi & Build dari Source Code

### Prasyarat
* **Java Development Kit (JDK)**: Versi 17 (disarankan Eclipse Temurin atau Oracle OpenJDK 17).
* **Android SDK**: Compile SDK 34, Min SDK 26.
* **Gradle**: 8.7 (disediakan via `gradlew`).

### Perintah Build Terminal

```bash
# Clone repositori
git clone https://github.com/maganghub/maganghub-android.git
cd maganghub-android

# Berikan izin eksekusi wrapper gradle
chmod +x gradlew

# Jalankan Unit Tests
./gradlew testDebugUnitTest

# Build Debug APK (output: app/build/outputs/apk/debug/maganghub-v1.0.0-dev.apk)
./gradlew assembleDebug

# Build Signed Release APK (output: app/build/outputs/apk/release/maganghub-v1.0.0.apk)
./gradlew assembleRelease

# Build Snapshot APK dengan Version Suffix kustom
./gradlew assembleRelease -PversionSuffix="-snapshot.abc1234"
```

File APK yang telah berhasil di-compile akan berada di:
* Debug APK: `app/build/outputs/apk/debug/maganghub-v<version>-dev.apk`
* Release APK: `app/build/outputs/apk/release/maganghub-v<version>.apk`

---

## 🔒 Keamanan & Privasi Kredensial

* **Enkripsi Hardware Keystore**: Password akun Kemnaker tidak disimpan sebagai plain-text, melainkan dienkripsi menggunakan skema AES-256 GCM yang kuncinya dikelola oleh Android Keystore hardware security module.
* **Koneksi Langsung**: Aplikasi hanya berkomunikasi langsung dengan server resmi Kemnaker:
  * SSO Kemnaker: `https://siapkerja.kemnaker.go.id/auth/login`
  * API MagangHub: `https://monev-api.maganghub.kemnaker.go.id/api/v1`
* **Tanpa Server Pihak Ketiga**: Tidak ada analitik pihak ketiga, tidak ada iklan, dan tidak ada server perantara eksternal. Kredensial Anda 100% tetap di perangkat Anda.

---

## 🔄 Siklus Rilis & Versioning

Repositori ini menerapkan siklus rilis dan manajemen versi otomatis berbasis GitHub Actions dan Semantic Versioning:

### 1. Pengembangan & Pull Request (PR)
* **Kerapian Versi**: Jangan mengubah field `versionCode` atau `versionName` di `app/build.gradle.kts` secara manual dalam PR fitur atau bugfix.
* **Conventional Commits**: Selalu gunakan format commit konvensional:
  * `feat:` — Fitur baru (memicu kenaikan versi Minor, misal `1.0.0` -> `1.1.0`).
  * `fix:` — Perbaikan bug (memicu kenaikan versi Patch, misal `1.0.0` -> `1.0.1`).
  * `BREAKING CHANGE:` atau tanda seru `!` — Perubahan struktural breaking (memicu kenaikan versi Major, misal `1.0.0` -> `2.0.0`).
  * `chore:`, `docs:`, `style:`, `refactor:`, `test:` — Perawatan rutin tanpa kenaikan versi langsung.
* **Gating CI**: Workflow pull request menjalankan `Verify No Manual Version Bumps in PR` untuk memastikan integritas versi build.

### 2. Snapshot Builds (Otomatis pada Push/Merge ke `main`)
* Setiap commit yang di-merge ke branch `main` secara otomatis memicu workflow **Snapshot Pre-Release**.
* Parameter `-PversionSuffix="-snapshot.${GITHUB_SHA::7}"` diteruskan ke Gradle untuk menghasilkan APK pratinjau `maganghub-v<version>-snapshot.<sha>.apk`.
* Diterbitkan secara otomatis sebagai GitHub Pre-Release untuk memudahkan verifikasi dan pengujian tim.

### 3. Official Releases (Manual Workflow Dispatch)
* Rilis resmi diterbitkan via workflow **Manual Release** pada tab GitHub Actions (`workflow_dispatch`).
* Workflow mengevaluasi commit sejak rilis stabil terakhir:
  * Menentukan tipe bump (`patch`, `minor`, atau `major`).
  * Menambah `versionCode` secara bertahap (+1) dan memperbarui `versionName` di `app/build.gradle.kts`.
  * Memperbarui `CHANGELOG.md` dengan daftar commit yang relevan.
  * Menghasilkan judul dan rangkuman rilis deskriptif.
  * Mengompilasi signed release APK `maganghub-v<version>.apk`.
  * Membuat git tag `v<version>`, commit versi, dan mempublikasikan GitHub Release resmi.

---

## 🤖 CI/CD Workflow

Proyek ini telah dikonfigurasi dengan pipeline CI/CD otomatis berbasis GitHub Actions:
* **CI Verification (`.github/workflows/build-apk.yml`)**:
  1. Validasi skrip rilis dengan unit test Node.js (`tests/releaseScripts.test.js`).
  2. Pengecekan guardrail versi PR (`verify-no-manual-version-bump.js`).
  3. Setup JDK 17 & Gradle Caching.
  4. Kompilasi Release APK (`assembleRelease`) dengan keystore signing fallback.
  5. Pengunggahan artefak APK build verification.
* **Snapshot Release (`.github/workflows/snapshot-release.yml`)**:
  - Berjalan saat push ke `main`, mengompilasi release snapshot APK dengan suffix `-PversionSuffix="-snapshot.${GITHUB_SHA::7}"`, membuat pre-release tag `v<version>-snapshot.<sha>`, dan mengunggah APK.
* **Manual Release (`.github/workflows/manual-release.yml`)**:
  - Dijalankan secara manual (`workflow_dispatch`) dengan opsi bump `auto`, `patch`, `minor`, atau `major`.
  - Mengotomasi seluruh siklus SemVer bump, changelog update, git tagging, APK compilation, dan penerbitan GitHub Release resmi.

---

## 📄 Lisensi

Didistribusikan di bawah Lisensi **MIT**. Silakan gunakan, pelajari, dan modifikasi sesuai kebutuhan program magang Anda.
