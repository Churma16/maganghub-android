# Post Mortem: Sinkronisasi Status "Menunggu Approval Mentor" Bernilai 0 (Issue #45)

**Tanggal Insiden:** 27 November 2026  
**Status:** Resolved  
**Severity:** P2 (Major Functional Bug)  
**Komponen Terdampak:**  
- `MagangHubApiClient.kt` (Deserialisasi profil & parsing logbook kehadiran)  
- `UserProfileData.kt` (Model profil pengguna Kemnaker Monev)  
- `AttendanceRepositoryImpl.kt` (Orkestrasi sinkronisasi data profil & riwayat absensi)  
- `EncryptedPreferences.kt` (Penyimpanan lokal `participant_id` & parameter tanggal)  
- `SettingsScreen.kt` & `AttendanceUiState.kt` (Telemetri diagnostik sinkronisasi)  
**Pelapor:** Developer / Pengguna Internal  
**Penangan:** Antigravity Agent  
**PR Terkait:** [#46 (fix/45-mentor-review-sync)](https://github.com/Churma16/maganghub-logbook-companion/pull/46)  
**Issue Terkait:** [#45](https://github.com/Churma16/maganghub-logbook-companion/issues/45)

---

> [!NOTE]
> Seluruh identitas pribadi, pengenal pengguna (UUID/ID), nama perusahaan, alamat email, dan kredensial telah disamarkan (*obfuscated*) demi kepatuhan terhadap privasi data.

---

## 1. Ringkasan Insiden

Pada versi rilis `v0.2.0` dan preview awal `v0.2.0-pr46.027a2b6`, kartu **"Menunggu approval Mentor"** pada Dashboard aplikasi MagangHub Logbook Companion serta ringkasan pada Home Screen Widget tetap menunjukkan status `0` atau *"Semua laporan sudah di-approve"*, meskipun portal resmi Kemnaker Monev Web (`https://monev.maganghub.kemnaker.go.id/dashboard`) secara faktual menampilkan:

> *"Menunggu peninjauan Mentor - 3 kehadiran dan laporan telah dikirim. Tidak ada tindakan yang perlu Anda lakukan."*

Akibatnya, pengguna tidak mendapatkan notifikasi atau pengingat review mentor saat laporan harian tertahan menunggu persetujuan pembimbing magang.

---

## 2. Dampak (Impact)

- **Akurasi Metrik:** Indikator pending approval mentor di aplikasi companion tidak sinkron dengan data faktual portal Monev Kemnaker (faktual: 3 laporan, aplikasi: 0 laporan).
- **Notifikasi Pengingat:** Fitur background alarm untuk pengingat mentor tidak terpicu karena nilai `pendingMentorReviewCount` terkunci di angka 0.
- **Transparansi Diagnostik:** Pengguna tidak dapat mengetahui penyebab kegagalan sinkronisasi dari UI karena seluruh pengecualian jaringan dan parsing tertelan (*swallowed*) tanpa visualisasi status di aplikasi build release.

---

## 3. Garis Waktu Kronologis (Chronological Timeline)

| Waktu (WIB) | Aktivitas / Kejadian |
|-------------|----------------------|
| **18:00** | Pengguna menguji preview build `v0.2.0-pr46.027a2b6` setelah implementasi awal fitur sinkronisasi mentor. |
| **18:24** | Pengguna melakukan sync manual (*Periksa Sekarang*). Dashboard berhasil sync ("Logbook berhasil dicek!"), namun kartu pending mentor tetap tidak muncul dan widget menyatakan *"Semua laporan sudah di-approve"*. |
| **18:28** | Pengguna melampirkan screenshot perbandingan dari Web Monev Kemnaker resmi yang menunjukkan 3 kehadiran menunggu peninjauan mentor. Pengguna menduga adanya ketidaksesuaian endpoint (*"salah endpoint kali, sebelomnya sempet ada lho ini"*). |
| **18:35** | Investigasi menyeluruh dilakukan pada bundle produksi Nuxt Monev Kemnaker (`De-VYfVO.js`, `Cpr9fSlH.js`, `Dj-UBjbN.js`, `Bdl9iSbj.js`, `DYUtPvyB.js`). Dikonfirmasi bahwa endpoint yang dipanggil oleh Web Monev adalah `GET /api/v1/attendances?participant_id={user.id}&start_date={start_date}&end_date={end_date}&limit=500`. |
| **18:42** | Ditemukan anomali utama: endpoint `GET /api/v1/users/me` mengembalikan properti `company` dalam bentuk objek `{ id: "...", name: "..." }`. Model Kotlin `UserProfileData` mendeklarasikannya sebagai `val company: String?`, memicu `JsonSyntaxException: Expected a string but was BEGIN_OBJECT`. |
| **18:48** | Menemukan rantai kegagalan sekunder (*silent cascade*): kegagalan deserialisasi profil ditangkap oleh blok `try-catch` kosong, menyebabkan `participant_id` bernilai `null`. Backend Kemnaker menolak query `/attendances` tanpa `participant_id`, menghasilkan list kosong. |
| **19:35** | Melakukan refactoring menyeluruh: menghapus field `company` kaku, menerapkan parser AST resilient (`JsonParser.parseString`), mendukung berbagai variasi envelope attendance, mengamankan persistence preference, dan menambahkan telemetri diagnostik di UI Settings. |
| **19:48** | Kode di-commit (`b37fd5b`) dan di-push ke branch `fix/45-mentor-review-sync`. CI Verification dan PR Preview Build APK dijalankan di GitHub Actions. |
| **19:52** | CI Verification dan Preview Build APK berhasil selesai (`v0.2.0-pr46.b37fd5b`). File APK diunduh dan diserahkan ke pengguna untuk verifikasi langsung. |

---

## 4. Analisis Akar Masalah (Root Cause Analysis)

Investigasi menemukan adanya 4 rantai kegagalan gabungan (*compound failure chain*):

### A. Perangkap Deserialisasi Tipe Kaku Gson (`BEGIN_OBJECT`)
Model profil pengguna sebelumnya didefinisikan sebagai berikut:
```kotlin
data class UserProfileData(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("company") val company: String? = null, // <-- Titik Kegagalan
    @SerializedName("internship_company") val internshipCompany: String? = null,
    @SerializedName("internship_start_date") val internshipStartDate: String? = null,
    @SerializedName("internship_end_date") val internshipEndDate: String? = null
)
```
Pada backend Monev Kemnaker, field `internship_company` memang bertipe string primitif, namun field `company` dikembalikan sebagai objek kompleks:
```json
{
  "data": {
    "id": "[REDACTED_PARTICIPANT_UUID]",
    "name": "[REDACTED_USER_NAME]",
    "company": {
      "id": "[REDACTED_COMPANY_ID]",
      "name": "[REDACTED_COMPANY_NAME]"
    },
    "internship_company": "[REDACTED_COMPANY_NAME]",
    "internship_start_date": "2026-08-XX",
    "internship_end_date": "2027-02-XX"
  }
}
```
Ketika Gson mencoba memetakan objek JSON tersebut ke Kotlin `String?`, runtime melemparkan:
```text
com.google.gson.JsonSyntaxException: java.lang.IllegalStateException: Expected a string but was BEGIN_OBJECT at line 1 column ... path $.data.company
```

### B. Exception Swallowing Menyebabkan Hilangnya Kredensial Metadata Peserta
Di dalam `AttendanceRepositoryImpl.kt`, pemanggilan `fetchUserProfile` dibungkus dalam blok `try-catch` best-effort:
```kotlin
try {
    val profile = apiClient.fetchUserProfile(validToken)
    storage.saveUserProfile(...)
} catch (pe: Exception) {
    Log.w(TAG, "Best-effort profile fetch during check failed: ${pe.message}")
}
```
Karena `pe` ditangkap tanpa eskalasi dan tanpa penanganan cadangan, seluruh proses penyimpanan metadata gagal dijalankan. Akibatnya:
- `participantId` tetap `null`
- `startDate` tetap `null`
- `endDate` tetap `null`

### C. Kontrak Backend Kemnaker untuk `/attendances` Mengharuskan `participant_id`
Berdasarkan reverse-engineering fungsi `He` pada Nuxt client Monev Web Kemnaker (`De-VYfVO.js`):
```javascript
He = async () => {
  let e = b.user?.id,
      t = b.user?.internship_start_date?.slice(0, 10),
      n = b.user?.internship_end_date?.slice(0, 10);
  if (!e || !t || !n) return;
  let r = L.value && L.value < n ? L.value : n;
  if (r < t) return;
  let [, i] = await Promise.all([
    P.fetchAttendances({ participant_id: e, start_date: t, end_date: r, limit: 500 }),
    ...
  ]);
  F.value = [...P.attendances.value];
};
```
Ketika `participantId` bernilai `null`, query parameter `participant_id` tidak ditambahkan ke URL OkHttp. Backend Kemnaker mengabaikan query atau mengembalikan HTTP 400 Bad Request, yang kembali tertelan oleh blok `catch (ae: Exception)` pada repository.

### D. Penanganan Nilai String Kosong pada Penyimpanan Preferensi
Fungsi ekstensi `updateOptionalString` pada `EncryptedPreferences` sebelumnya menghapus kunci jika nilai `value != null` tetapi blank:
```kotlin
private fun SharedPreferences.Editor.updateOptionalString(key: String, value: String?): SharedPreferences.Editor {
    return when {
        !value.isNullOrBlank() -> putString(key, value.trim())
        value != null -> remove(key) // <-- Menghapus KEY_PARTICIPANT_ID jika default string kosong "" dilewatkan
        else -> this
    }
}
```
Karena nilai default `UserProfileData.id` adalah string kosong `""`, eksekusi fungsi ini dapat secara tidak sengaja menghapus ID peserta yang valid dari storage terenkripsi.

---

## 5. Solusi & Perbaikan yang Diterapkan

### 1. Penghapusan Field Bermasalah & Penggunaan AST Parsing yang Tangguh
- Menghapus field `company` dari model `UserProfileData.kt` untuk mencegah bentrokan tipe Gson.
- Mengimplementasikan `parseUserProfileResponseBody(responseBody: String)` menggunakan `JsonParser.parseString`:
  - Mendukung ekstraksi kunci alternatif: `id`, `participant_id`, `participantId`, `user_id`.
  - Mendukung resolusi aman untuk `internship_company` baik jika dikirim sebagai string primitif maupun sebagai nested object `{ name: "..." }`.
  - Tidak bergantung pada skema model reflektif, sehingga kebal terhadap field tak terduga dari Kemnaker.

### 2. Penanganan Struktur Envelope Logbook yang Fleksibel
Fungsi `parseAttendancesResponseBody` diperbarui agar mampu mengekstrak array logbook dari berbagai variasi envelope API:
- Root JSON Array: `[ ... ]`
- Standard Data Array: `{ "data": [ ... ] }`
- Nested Pagination Envelope: `{ "data": { "attendances": [ ... ] } }` atau `{ "data": { "data": [ ... ] } }`
- Nested Attendance Object: mengekstrak langsung dari field `item.attendance` jika server membungkus logbook di dalam sub-objek.

### 3. Perlindungan Persistence Preferensi Lokal
Memperbaiki `updateOptionalString` pada `EncryptedPreferences`:
```kotlin
private fun SharedPreferences.Editor.updateOptionalString(key: String, value: String?): SharedPreferences.Editor {
    return if (!value.isNullOrBlank()) {
        putString(key, value.trim())
    } else {
        this // Tidak menghapus kunci yang sudah ada jika nilai baru bernilai blank/null
    }
}
```
Kunci hanya dihapus secara eksplisit saat sesi di-logout melalui `clearCredentials()`.

### 4. Sinkronisasi Profil Otomatis saat Force Refresh
Pada `AttendanceRepositoryImpl.kt`, pemanggilan `checkAttendance(forceRefresh = true)` (misalnya saat pengguna menekan tombol *Periksa Sekarang* di Dashboard) kini selalu memicu refresh profil jika `participantId` belum terisi atau saat refresh paksa diminta, menjamin metadata magang segera diperbarui.

### 5. Multi-Tier Fallback & Telemetri Diagnostik Real-Time
Menambahkan strategi pemanggilan bertingkat (*tiered fallback*) untuk `/attendances`:
1. **Tier 1:** `limit=500` dengan `participant_id` dan rentang tanggal lengkap.
2. **Tier 2:** `limit=100` dengan `participant_id` dan rentang tanggal.
3. **Tier 3:** `limit=100` dengan `participant_id` tanpa batas tanggal.
4. **Tier 4:** `limit=100` fallback tanpa parameter.

Hasil sinkronisasi (sukses/gagal, jumlah item, jumlah pending, dan masking ID peserta) dicatat dalam preferensi `KEY_LAST_MENTOR_SYNC_DEBUG` dan ditampilkan secara visual di layar **Pengaturan** (di bawah kartu *Pengingat Approval Mentor*) dengan format:
```text
Sinkronisasi: pId=[REDACTED_PREFIX].., 2026-08-XX..2026-11-XX, fetched=45, pending=3
```

---

## 6. Verifikasi & Pengujian

1. **Unit Testing Lokal & MockWebServer (`MagangHubApiClientTest.kt`)**:
   - `testFetchUserProfileWithCompanyObject`: Memverifikasi respons profil yang memuat field `company` berbentuk objek JSON `{ id: "...", name: "..." }` terurai sempurna tanpa error sintaksis.
   - `testParseAttendancesResponseBodyFlexibleFormats`: Memverifikasi parser berhasil membaca payload bertingkat `{ data: { attendances: [...] } }` dan direct array dengan nested object `attendance`.
   - `testCalculatePendingMentorReviewCount`: Memverifikasi filter `approval_status == SUBMITTED` dan `status != ABSENT && status != DAYOFF` menghitung distinct tanggal secara tepat (3 tanggal unik).
2. **GitHub Actions CI Verification**:
   - Workflow run ID `45993484761` sukses dalam 2 menit 22 detik tanpa error lint atau unit test.
3. **Build & Rilis Preview APK**:
   - Workflow run ID `45993503019` sukses menerbitkan pre-release APK `v0.2.0-pr46.b37fd5b` untuk pengujian langsung oleh pengguna.

---

## 7. Tindakan Pencegahan (Action Items & Prevention)

| No | Tindakan Pencegahan | Status | Target / Pemilik |
|:---|:---|:---:|:---|
| 1 | Hindari mapping tipe data primitif kaku (`String`) pada response API pihak ketiga yang berpotensi mengembalikan objek/polimorfik. | **Done** | Core Network (`MagangHubApiClient`) |
| 2 | Jangan pernah menelan (*swallow*) exception API penting di background worker/repository tanpa mencatat telemetri status ke UI diagnostik. | **Done** | Data Layer & UI State |
| 3 | Sediakan indikator status sinkronisasi transparan pada menu Settings agar kegagalan runtime dapat dianalisa langsung dari tangkapan layar pengguna. | **Done** | UI Layer (`SettingsScreen`) |
| 4 | Pastikan fungsi update storage lokal tidak menghapus cache ID pengguna saat menerima string kosong. | **Done** | Storage Layer (`EncryptedPreferences`) |
