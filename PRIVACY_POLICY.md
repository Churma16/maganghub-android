# Kebijakan Privasi (Privacy Policy)

**Terakhir diperbarui:** 25 November 2026

MagangHub Logbook Companion adalah aplikasi pendamping independen sumber terbuka (*open-source*) untuk peserta program MagangHub Kemnaker RI. Dokumen ini menjelaskan bagaimana aplikasi menangani data kamu dengan prinsip transparansi dan perlindungan privasi penuh.

---

## 1. Prinsip Utama: Tanpa Server Perantara (*Zero-Server*)

Aplikasi ini beroperasi dengan arsitektur *local-first*:
* **Tidak ada server perantara:** Aplikasi tidak menyimpan data kamu di server pengembang maupun pihak ketiga mana pun.
* **Komunikasi langsung:** Seluruh komunikasi jaringan untuk kebutuhan presensi hanya terjadi langsung antara perangkat HP kamu dengan server resmi Kementerian Ketenagakerjaan Republik Indonesia:
  * `siapkerja.kemnaker.go.id` (otentikasi SSO SiapKerja)
  * `monev.maganghub.kemnaker.go.id` (pengambilan status presensi dan logbook)

---

## 2. Data yang Disimpan di Perangkat

Semua data operasional tersimpan secara lokal di memori internal perangkat kamu:
* **Kredensial Akun (Username/NIK & Kata Sandi):** Disimpan menggunakan `EncryptedSharedPreferences` dengan enkripsi tingkat perangkat keras (AES-256 GCM) yang dikunci oleh Android Keystore bawaan perangkat.
* **Token Sesi & Cookie:** Digunakan untuk memperbarui status presensi di latar belakang tanpa harus meminta kamu login berulang kali.
* **Pengaturan Preferensi:** Jadwal pengingat harian, interval pengulangan notifikasi, batas hari pengingat mentor, opsi lewati hari libur, dan preferensi tema (terang/gelap).

---

## 3. Pengiriman Masukan & Diagnostik Perangkat (*Feedback*)

Aplikasi menyediakan fitur sukarela **"Kirim masukan"** untuk melaporkan kendala teknis atau menyampaikan usul fitur:
* **Data yang dikirimkan:**
  * Kategori laporan (`Bug`, `Saran fitur`, atau `Lainnya`).
  * Pesan masukan yang kamu ketik secara sukarela.
  * Metadata teknis perangkat: Merek dan tipe perangkat (`device_model`), versi Android beserta level SDK (`os_version`), versi build aplikasi (`app_version`), serta waktu pengiriman (*timestamp*).
* **Tujuan pengumpulan data:** Metadata teknis semata-mata digunakan oleh pengembang untuk mereproduksi kendala tampilan/fungsional pada tipe perangkat atau versi Android tertentu.
* **Tanpa Data Pribadi:** Pengiriman masukan bersifat anonim dan **tidak menyertakan data sensitif** apa pun (tidak ada nama, NIK, kata sandi, token otentikasi, nomor telepon, maupun catatan presensi kamu).

---

## 4. Penggunaan Izin Perangkat (*Permissions*)

Aplikasi meminta izin Android berikut untuk menjalankan fungsinya:
* **Akses Internet (`android.permission.INTERNET`):** Menghubungi API resmi Kemnaker untuk mengambil status kehadiran dan mengirim laporan masukan yang kamu buat.
* **Notifikasi (`android.permission.POST_NOTIFICATIONS`):** Menampilkan alarm pengingat jika presensi belum diisi atau laporan belum disetujui mentor.
* **Alarm Tepat Waktu (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`):** Menjadwalkan pengingat agar berbunyi tepat pada jam yang telah kamu tentukan.
* **Mulai Otomatis Saat Boot (`RECEIVE_BOOT_COMPLETED`):** Mengaktifkan kembali jadwal alarm pengingat secara otomatis setelah HP selesai dinyalakan ulang (*restart*).
* **Pengabaian Optimasi Baterai:** Mencegah sistem Android menghentikan proses alarm pengingat di latar belakang.

---

## 5. Pelacakan dan Iklan Pihak Ketiga

Aplikasi ini:
* **Bebas pelacak & analitik:** Tidak memuat Google Analytics, Firebase SDK, Meta SDK, maupun pustaka pelacak telemetri lainnya.
* **Bebas iklan:** Tidak menampilkan iklan komersial apa pun.
* **Tidak memperjualbelikan data:** Tidak ada data yang dibagikan atau diperjualbelikan kepada pihak ketiga.

---

## 6. Hak Pengguna & Penghapusan Data

Kamu memiliki kendali penuh atas data yang tersimpan:
* **Keluar Akun (*Logout*):** Menekan tombol "Keluar dari akun" di menu Pengaturan akan menghapus seluruh kredensial terenkripsi, token sesi, dan riwayat cache dari perangkat secara permanen.
* **Hapus Aplikasi (*Uninstall*):** Menghapus instalasi aplikasi dari perangkat secara otomatis membersihkan seluruh data lokal tanpa sisa.

---

## 7. Transparansi dan Kontak

Kode sumber aplikasi ini terbuka (*open-source*) dan dapat diaudit secara bebas di [GitHub MagangHub Logbook Companion](https://github.com/Churma16/maganghub-logbook-companion).

Jika kamu memiliki pertanyaan, ingin mengusulkan perbaikan, atau menemukan potensi celah keamanan, silakan buka tiket laporan di [GitHub Issues](https://github.com/Churma16/maganghub-logbook-companion/issues).
