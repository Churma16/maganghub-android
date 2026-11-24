package id.codes.maganghub.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.codes.maganghub.background.NotificationHelper
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.ui.theme.DangerRedContainer
import id.codes.maganghub.ui.theme.DangerRedText
import id.codes.maganghub.ui.theme.HadirGreen
import id.codes.maganghub.ui.theme.HadirGreenLight
import id.codes.maganghub.ui.theme.HadirGreenText
import id.codes.maganghub.ui.theme.InfoSky
import id.codes.maganghub.ui.theme.InfoSkyBorder
import id.codes.maganghub.ui.theme.InfoSkyContainer
import id.codes.maganghub.ui.theme.InfoSkyText
import id.codes.maganghub.ui.theme.KemnakerSky
import id.codes.maganghub.ui.theme.KemnakerSkyContainer
import id.codes.maganghub.ui.theme.WarningAmber
import id.codes.maganghub.ui.theme.WarningAmberContainer
import id.codes.maganghub.ui.theme.WarningAmberText
import id.codes.maganghub.ui.viewmodel.AttendanceUiState
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: AttendanceUiState,
    onCheckNow: () -> Unit,
    onOpenLoginDialog: () -> Unit,
    formatLastSync: (Long) -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val openMonevPortal: () -> Unit = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(NotificationHelper.URL_MONEV_DASHBOARD)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Greeting
        HeaderGreetingSection(username = uiState.username)

        // 2. Official Monev Web Announcement Banner
        SoftAmberAnnouncementBanner()

        // 3. Runtime Error or Success Alert Banner
        AnimatedVisibility(
            visible = uiState.errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BannerCard(
                icon = Icons.Default.Error,
                message = uiState.errorMessage.orEmpty(),
                containerColor = DangerRedContainer,
                contentColor = DangerRedText
            )
        }

        AnimatedVisibility(
            visible = uiState.successMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BannerCard(
                icon = Icons.Default.CheckCircle,
                message = uiState.successMessage.orEmpty(),
                containerColor = HadirGreenLight,
                contentColor = HadirGreenText
            )
        }

        // 4. Main "Hari Ini" Card
        HariIniCard(
            uiState = uiState,
            onOpenLogin = onOpenLoginDialog,
            onOpenPortal = openMonevPortal
        )

        // 5. Next Reminder Status Card
        ReminderStatusCard(
            nextReminderText = uiState.nextReminderFormatted,
            isReminderEnabled = uiState.reminderSettings.isEnabled,
            intervalMinutes = uiState.reminderSettings.intervalMinutes
        )

        // 6. Attendance Details Breakdown Card
        if (uiState.attendanceData != null) {
            AttendanceDetailCard(uiState = uiState)
        }

        // 7. Action Buttons Section
        ActionButtonsSection(
            isChecking = uiState.isChecking,
            isLoggedIn = uiState.isLoggedIn,
            onCheckNow = onCheckNow,
            onOpenMonevPortal = openMonevPortal
        )

        // 8. Last Sync Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Terakhir diperbarui: ${formatLastSync(uiState.lastSyncTimestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Header Greeting: "Halo, [Username] 👋"
 */
@Composable
private fun HeaderGreetingSection(username: String) {
    val displayName = username.ifBlank { "Rekan Magang" }
    Text(
        text = "Halo, $displayName 👋",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

/**
 * Soft Amber announcement card ("Batas pengisian logbook 23:59 WIB")
 */
@Composable
private fun SoftAmberAnnouncementBanner() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = WarningAmberContainer,
        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Batas pengisian logbook 23:59 WIB setiap hari kerja. Lewat dari batas waktu dianggap tidak hadir.",
                style = MaterialTheme.typography.bodySmall,
                color = WarningAmberText,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Main "Hari Ini" Card
 */
@Composable
private fun HariIniCard(
    uiState: AttendanceUiState,
    onOpenLogin: () -> Unit,
    onOpenPortal: () -> Unit
) {
    val status = uiState.attendanceStatus

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Header: Calendar icon in soft blue square, formatted Date, and pill badge "Aktif"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(KemnakerSkyContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Kalender",
                            tint = KemnakerSky,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = formatDisplayDate(uiState.attendanceData?.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = HadirGreenLight
                ) {
                    Text(
                        text = if (uiState.attendanceData?.activeInternship != false) "Aktif" else "Non-Aktif",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HadirGreenText
                    )
                }
            }

            // Center Status Content
            when {
                !uiState.isLoggedIn -> {
                    CenterStatusSection(
                        icon = Icons.Default.Person,
                        iconContainerColor = KemnakerSkyContainer,
                        iconColor = KemnakerSky,
                        title = "Kredensial Belum Terpasang",
                        description = "Masukkan akun SSO Kemnaker untuk memantau status presensi dan logbook harian otomatis.",
                        buttonText = "Masuk Akun Kemnaker >",
                        buttonColor = KemnakerSky,
                        onButtonClick = onOpenLogin
                    )
                }
                status == AttendanceStatus.HADIR -> {
                    CenterStatusSection(
                        icon = Icons.Default.CheckCircle,
                        iconContainerColor = HadirGreenLight,
                        iconColor = HadirGreen,
                        title = "Laporan hari ini sudah diisi",
                        description = "Laporan dan kehadiran Anda sudah tercatat.",
                        buttonText = "Lihat Laporan Hari Ini >",
                        buttonColor = HadirGreen,
                        onButtonClick = onOpenPortal
                    )
                }
                status == AttendanceStatus.LIBUR -> {
                    CenterStatusSection(
                        icon = Icons.Default.Weekend,
                        iconContainerColor = KemnakerSkyContainer,
                        iconColor = KemnakerSky,
                        title = "Hari Libur / Non-Aktif",
                        description = "Hari ini terjadwal libur atau non-operasional. Tidak diperlukan pengisian logbook.",
                        buttonText = "Lihat Dashboard Monev >",
                        buttonColor = KemnakerSky,
                        onButtonClick = onOpenPortal
                    )
                }
                status == AttendanceStatus.TIDAK_AKTIF -> {
                    CenterStatusSection(
                        icon = Icons.Default.Info,
                        iconContainerColor = InfoSkyContainer,
                        iconColor = InfoSky,
                        title = "Magang Tidak Aktif",
                        description = "Status magang Anda tercatat tidak aktif di sistem Kemnaker.",
                        buttonText = "Buka Monev Portal >",
                        buttonColor = KemnakerSky,
                        onButtonClick = onOpenPortal
                    )
                }
                status == AttendanceStatus.BELUM_HADIR -> {
                    CenterStatusSection(
                        icon = Icons.Default.Warning,
                        iconContainerColor = WarningAmberContainer,
                        iconColor = WarningAmber,
                        title = "Laporan hari ini belum diisi",
                        description = "Laporan dan kehadiran Anda belum tercatat. Segera isi sebelum batas waktu berakhir.",
                        buttonText = "Isi Laporan Hari Ini >",
                        buttonColor = HadirGreen,
                        onButtonClick = onOpenPortal
                    )
                }
                else -> {
                    CenterStatusSection(
                        icon = Icons.Default.Info,
                        iconContainerColor = InfoSkyContainer,
                        iconColor = InfoSky,
                        title = "Status Belum Diperiksa",
                        description = "Tekan tombol 'Cek Sekarang' di bawah untuk memeriksa kehadiran hari ini.",
                        buttonText = "Buka Monev Portal >",
                        buttonColor = KemnakerSky,
                        onButtonClick = onOpenPortal
                    )
                }
            }

            // Inner deadline box: Light blue container with info icon
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = InfoSkyContainer,
                border = BorderStroke(1.dp, InfoSkyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = KemnakerSky,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Batas pengisian laporan hari ini pukul 23.59 WIB",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = InfoSkyText
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterStatusSection(
    icon: ImageVector,
    iconContainerColor: Color,
    iconColor: Color,
    title: String,
    description: String,
    buttonText: String,
    buttonColor: Color,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Large circular icon (76.dp)
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(iconContainerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(42.dp)
            )
        }

        // Bold headline
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Main CTA Button
        Button(
            onClick = onButtonClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * Next Reminder & Countdown Indicator
 */
@Composable
private fun ReminderStatusCard(
    nextReminderText: String,
    isReminderEnabled: Boolean,
    intervalMinutes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isReminderEnabled) KemnakerSkyContainer else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Pengingat",
                    tint = if (isReminderEnabled) KemnakerSky else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Jadwal Pengingat",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isReminderEnabled) HadirGreenLight else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isReminderEnabled) "AKTIF ($intervalMinutes m)" else "NONAKTIF",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isReminderEnabled) HadirGreenText else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = nextReminderText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Details Breakdown Card
 */
@Composable
private fun AttendanceDetailCard(uiState: AttendanceUiState) {
    val data = uiState.attendanceData ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Rincian Kehadiran",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = KemnakerSky
            )

            DetailRow(
                icon = Icons.Default.DateRange,
                label = "Tanggal Sistem",
                value = data.date.ifBlank { "-" }
            )

            DetailRow(
                icon = Icons.Default.CheckCircle,
                label = "Status Magang",
                value = if (data.activeInternship) "Magang Aktif 🟢" else "Tidak Aktif ⚪"
            )

            DetailRow(
                icon = Icons.Default.Weekend,
                label = "Hari Libur / Off",
                value = when {
                    data.isHoliday -> "Hari Libur Nasional"
                    data.isScheduledOffDay -> "Off Day Terjadwal"
                    else -> "Hari Kerja Normal"
                }
            )

            DetailRow(
                icon = Icons.Default.AccessTime,
                label = "Presensi Tercatat",
                value = if (data.hasAttendance) "Sudah Presensi & Terisi ✅" else "Belum Ada Presensi ❌"
            )
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActionButtonsSection(
    isChecking: Boolean,
    isLoggedIn: Boolean,
    onCheckNow: () -> Unit,
    onOpenMonevPortal: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onCheckNow,
            enabled = !isChecking && isLoggedIn,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = KemnakerSky,
                contentColor = Color.White
            )
        ) {
            if (isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Memeriksa Server Monev...")
            } else {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cek Sekarang", fontWeight = FontWeight.SemiBold)
            }
        }

        OutlinedButton(
            onClick = onOpenMonevPortal,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp), tint = KemnakerSky)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Buka Monev MagangHub Portal", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun BannerCard(
    icon: ImageVector,
    message: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private val ZONE_WIB = ZoneId.of("Asia/Jakarta")
private val INDONESIAN_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID"))

private fun formatDisplayDate(dateStr: String?): String {
    if (!dateStr.isNullOrBlank()) {
        try {
            val date = LocalDate.parse(dateStr)
            return date.format(INDONESIAN_DATE_FORMATTER)
        } catch (_: Exception) {
            return dateStr
        }
    }
    return LocalDate.now(ZONE_WIB).format(INDONESIAN_DATE_FORMATTER)
}
