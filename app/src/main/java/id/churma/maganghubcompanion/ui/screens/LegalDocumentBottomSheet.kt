package id.churma.maganghubcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.churma.maganghubcompanion.data.model.LegalDocumentType
import id.churma.maganghubcompanion.ui.theme.KemnakerSky
import id.churma.maganghubcompanion.ui.theme.PureWhite

/**
 * In-app modal bottom sheet for displaying Privacy Policy or Terms of Service.
 * Eliminates the need to open external browser pages for legal information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDocumentBottomSheet(
    documentType: LegalDocumentType?,
    onDismiss: () -> Unit
) {
    if (documentType == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()

    val sheetBg = if (isDark) Color(0xFF131D31) else MaterialTheme.colorScheme.surface
    val cardBg = if (isDark) Color(0xFF0B1220) else Color(0xFFF8FAFC)
    val dividerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Header
            RowHeader(
                documentType = documentType,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = dividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (documentType) {
                    LegalDocumentType.PRIVACY_POLICY -> PrivacyPolicyContent(cardBg = cardBg)
                    LegalDocumentType.TERMS_OF_SERVICE -> TermsOfServiceContent(cardBg = cardBg)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = dividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Close Action Button
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KemnakerSky,
                    contentColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Tutup",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RowHeader(
    documentType: LegalDocumentType,
    isDark: Boolean
) {
    val icon = when (documentType) {
        LegalDocumentType.PRIVACY_POLICY -> Icons.Outlined.Shield
        LegalDocumentType.TERMS_OF_SERVICE -> Icons.Outlined.Description
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = KemnakerSky,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = documentType.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "MagangHub Logbook Companion • Terakhir diperbarui 25 Nov 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegalSectionItem(
    title: String,
    body: String,
    cardBg: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PrivacyPolicyContent(cardBg: Color) {
    LegalSectionItem(
        title = "1. Prinsip Utama: Tanpa Server Perantara (Zero-Server)",
        body = "Aplikasi ini beroperasi secara local-first tanpa server perantara. Seluruh komunikasi jaringan presensi hanya terjadi langsung antara perangkat HP Anda dengan server resmi Kementerian Ketenagakerjaan RI (siapkerja.kemnaker.go.id dan monev.maganghub.kemnaker.go.id). Tidak ada data Anda yang dikirim atau disimpan ke server pengembang maupun pihak ketiga.",
        cardBg = cardBg
    )

    LegalSectionItem(
        title = "2. Data yang Disimpan di Perangkat",
        body = "Kredensial akun (Username/NIK & Kata Sandi) disimpan di penyimpanan lokal menggunakan EncryptedSharedPreferences dengan enkripsi perangkat keras AES-256 GCM yang dikunci Android Keystore. Token sesi dan cookie digunakan untuk memperbarui status presensi di latar belakang tanpa meminta Anda login ulang.",
        cardBg = cardBg
    )

    LegalSectionItem(
        title = "3. Pengiriman Masukan & Diagnostik Perangkat (Feedback)",
        body = "Ketika Anda secara sukarela menggunakan menu Kirim Masukan, aplikasi mengirimkan kategori laporan, pesan masukan, versi aplikasi, serta tipe perangkat (device model) dan versi Android untuk membantu reproduksi bug. Pengiriman ini bersifat anonim dan tidak menyertakan data sensitif (tanpa NIK, kata sandi, token, maupun nomor telepon).",
        cardBg = cardBg
    )

    LegalSectionItem(
        title = "4. Penggunaan Izin Perangkat (Permissions)",
        body = "Aplikasi meminta izin Internet untuk mengambil data presensi, Notifikasi untuk pengingat harian & review mentor, Exact Alarm & Boot untuk ketepatan alarm pengingat, serta pengabaian optimasi baterai agar alarm tidak dihentikan oleh sistem Android di background.",
        cardBg = cardBg
    )

    LegalSectionItem(
        title = "5. Nol Pelacak & Bebas Iklan",
        body = "Aplikasi bebas dari SDK analitik (tidak ada Google Analytics, Firebase SDK, atau Meta tracker) serta bebas iklan komersial. Tidak ada data yang dibagikan atau diperjualbelikan.",
        cardBg = cardBg
    )

    LegalSectionItem(
        title = "6. Hak Pengguna & Penghapusan Data",
        body = "Menekan tombol 'Keluar dari akun' di menu Pengaturan akan menghapus seluruh kredensial terenkripsi, token sesi, dan data cache secara permanen dari perangkat. Menghapus instalasi (uninstall) aplikasi otomatis membersihkan seluruh data tanpa sisa.",
        cardBg = cardBg
    )

    LegalSectionItem(
        title = "7. Transparansi & Sumber Terbuka",
        body = "Kode sumber aplikasi ini didistribusikan secara terbuka (open-source) dan dapat diaudit secara bebas melalui repositori resmi di GitHub.",
        cardBg = cardBg
    )
}

@Composable
private fun TermsOfServiceContent(cardBg: Color) {
    LegalSectionItem(
        title = "1. Status Aplikasi (Bukan Aplikasi Resmi & Versi Beta)",
        body = "MagangHub Logbook Companion adalah alat bantu independen sumber terbuka (unofficial companion tool) yang saat ini berada pada tahap rilis awal / versi beta (v0.x). Aplikasi ini TIDAK terafiliasi, tidak didukung secara resmi, dan tidak mewakili Kementerian Ketenagakerjaan Republik Indonesia (Kemnaker RI) maupun pengelola program MagangHub.",
        cardBg = cardBg
    )

    LegalSectionItem(
        title = "2. Tanggung Jawab Kehadiran & Pengisian Logbook",
        body = "Aplikasi ini murni berfungsi sebagai pengingat (reminder) dan alat pantau status. Keabsahan, kebenaran, dan ketepatan waktu pengisian presensi serta logbook magang harian tetap sepenuhnya menjadi tanggung jawab Anda sendiri di portal resmi Monev Kemnaker.",
        cardBg = cardBg
    )

    LegalSectionItem(
        title = "3. Keamanan Akun & Kredensial",
        body = "Anda bertanggung jawab menjaga kerahasiaan akun SiapKerja / Monev Anda. Kredensial akun disimpan hanya secara lokal di perangkat Anda menggunakan enkripsi Android Keystore dan tidak pernah dikirimkan ke server selain portal otentikasi resmi Kemnaker RI.",
        cardBg = cardBg
    )

    LegalSectionItem(
        title = "4. Ketergantungan Sistem & Layanan Eksternal",
        body = "Keandalan alarm pengingat bergantung pada sistem Android dan manajemen daya dari pabrikan HP (seperti MIUI/HyperOS, ColorOS, OneUI). Fungsi pengecekan otomatis juga bergantung pada ketersediaan portal Monev Kemnaker (pemeliharaan server atau perubahan sistem Kemnaker dapat memengaruhi aplikasi sementara waktu).",
        cardBg = cardBg
    )

    LegalSectionItem(
        title = "5. Batasan Tanggung Jawab (Limitation of Liability)",
        body = "Aplikasi disediakan 'apa adanya' (as-is) dalam tahap pengujian beta tanpa jaminan apa pun. Pengembang tidak bertanggung jawab atas sanksi magang/institusi, kendala pencatatan kehadiran, hilangnya hak uang saku, atau kerugian yang timbul akibat kelalaian pribadi, kegagalan alarm pada perangkat tertentu, atau gangguan teknis pada server Kemnaker RI.",
        cardBg = cardBg
    )

    LegalSectionItem(
        title = "6. Lisensi & Kode Sumber Terbuka",
        body = "Kode sumber aplikasi ini terbuka untuk dipelajari, diaudit, dan dikembangkan bersama secara transparan melalui repositori GitHub resmi.",
        cardBg = cardBg
    )
}
