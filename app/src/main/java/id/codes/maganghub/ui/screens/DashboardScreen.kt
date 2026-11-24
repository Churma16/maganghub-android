package id.codes.maganghub.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.codes.maganghub.background.NotificationHelper
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.ui.theme.BelumHadirAmber
import id.codes.maganghub.ui.theme.BelumHadirAmberContainer
import id.codes.maganghub.ui.theme.BelumHadirAmberText
import id.codes.maganghub.ui.theme.BelumHadirRed
import id.codes.maganghub.ui.theme.BelumHadirRedContainer
import id.codes.maganghub.ui.theme.BelumHadirRedText
import id.codes.maganghub.ui.theme.CardBorderDark
import id.codes.maganghub.ui.theme.CardBorderLight
import id.codes.maganghub.ui.theme.HadirGreen
import id.codes.maganghub.ui.theme.HadirGreenDark
import id.codes.maganghub.ui.theme.HadirGreenLight
import id.codes.maganghub.ui.theme.HadirGreenText
import id.codes.maganghub.ui.theme.LiburSky
import id.codes.maganghub.ui.theme.LiburSkyContainer
import id.codes.maganghub.ui.theme.LiburSkyText
import id.codes.maganghub.ui.viewmodel.AttendanceUiState

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Error or Success Banner
        AnimatedVisibility(
            visible = uiState.errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BannerCard(
                icon = Icons.Default.Error,
                message = uiState.errorMessage.orEmpty(),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
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

        // Account Status Bar (User summary)
        AccountSummaryCard(
            isLoggedIn = uiState.isLoggedIn,
            username = uiState.username,
            onOpenLogin = onOpenLoginDialog
        )

        // Main Attendance Status Block
        AttendanceHeroCard(
            uiState = uiState,
            onOpenLogin = onOpenLoginDialog
        )

        // Next Reminder & Countdown Indicator
        ReminderStatusCard(
            nextReminderText = uiState.nextReminderFormatted,
            isReminderEnabled = uiState.reminderSettings.isEnabled,
            intervalMinutes = uiState.reminderSettings.intervalMinutes
        )

        // Details Breakdown Card
        if (uiState.attendanceData != null) {
            AttendanceDetailCard(
                uiState = uiState,
                formatLastSync = formatLastSync
            )
        }

        // Action Buttons Row
        ActionButtonsSection(
            isChecking = uiState.isChecking,
            isLoggedIn = uiState.isLoggedIn,
            onCheckNow = onCheckNow,
            onOpenMonevPortal = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(NotificationHelper.URL_MONEV_DASHBOARD)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        )

        // Last Sync Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
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

@Composable
private fun AccountSummaryCard(
    isLoggedIn: Boolean,
    username: String,
    onOpenLogin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isLoggedIn) HadirGreenLight else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User",
                            tint = if (isLoggedIn) HadirGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = if (isLoggedIn) (username.ifBlank { "Akun Kemnaker" }) else "Belum Masuk Akun",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isLoggedIn) "Tersambung ke Monev Kemnaker" else "Login untuk auto-check presensi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isLoggedIn) {
                Button(
                    onClick = onOpenLogin,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Masuk", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AttendanceHeroCard(
    uiState: AttendanceUiState,
    onOpenLogin: () -> Unit
) {
    val status = uiState.attendanceStatus

    val (bgGradientColor, textColor, iconColor, statusTitle, statusSubtitle, statusIcon) = when {
        !uiState.isLoggedIn -> StatusVisual(
            bgColor = MaterialTheme.colorScheme.surfaceVariant,
            textColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = "Kredensial Belum Terpasang",
            subtitle = "Masukkan akun SSO Kemnaker untuk memantau status presensi dan logbook harian otomatis.",
            icon = Icons.Default.Info
        )
        status == AttendanceStatus.HADIR -> StatusVisual(
            bgColor = HadirGreenLight,
            textColor = HadirGreenText,
            iconColor = HadirGreenDark,
            title = "Presensi & Logbook Selesai",
            subtitle = "Presensi dan laporan harian hari ini sudah tercatat di sistem Monev Kemnaker.",
            icon = Icons.Default.CheckCircle
        )
        status == AttendanceStatus.LIBUR -> StatusVisual(
            bgColor = LiburSkyContainer,
            textColor = LiburSkyText,
            iconColor = LiburSky,
            title = "Hari Libur / Non-Aktif",
            subtitle = "Hari ini terjadwal libur atau non-operasional. Tidak diperlukan pengisian logbook.",
            icon = Icons.Default.Weekend
        )
        status == AttendanceStatus.TIDAK_AKTIF -> StatusVisual(
            bgColor = MaterialTheme.colorScheme.surfaceVariant,
            textColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = "Magang Tidak Aktif",
            subtitle = "Status magang Anda tercatat tidak aktif di sistem Kemnaker.",
            icon = Icons.Default.Info
        )
        status == AttendanceStatus.BELUM_HADIR -> StatusVisual(
            bgColor = BelumHadirRedContainer,
            textColor = BelumHadirRedText,
            iconColor = BelumHadirRed,
            title = "Belum Presensi & Isi Logbook!",
            subtitle = "Batas akhir pengisian adalah pukul 23:59 WIB malam ini. Harap segera isi logbook di portal Monev.",
            icon = Icons.Default.Warning
        )
        else -> StatusVisual(
            bgColor = BelumHadirAmberContainer,
            textColor = BelumHadirAmberText,
            iconColor = BelumHadirAmber,
            title = "Status Belum Diperiksa",
            subtitle = "Tekan tombol 'Cek Sekarang' di bawah untuk memeriksa kehadiran hari ini.",
            icon = Icons.Default.Info
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = bgGradientColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Badge Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = if (uiState.isLoggedIn) status.label.uppercase() else "PERLU LOGIN",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                Icon(
                    imageVector = statusIcon,
                    contentDescription = statusTitle,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = statusTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Text(
                text = statusSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )

            if (!uiState.isLoggedIn) {
                Button(
                    onClick = onOpenLogin,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Masuk Akun Kemnaker")
                }
            }
        }
    }
}

@Composable
private fun ReminderStatusCard(
    nextReminderText: String,
    isReminderEnabled: Boolean,
    intervalMinutes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isReminderEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Pengingat",
                        tint = if (isReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
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
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AttendanceDetailCard(
    uiState: AttendanceUiState,
    formatLastSync: (Long) -> String
) {
    val data = uiState.attendanceData ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Rincian Kehadiran",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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
            fontWeight = FontWeight.Medium
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
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (isChecking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
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
                .height(48.dp)
        ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Buka Monev MagangHub Portal", fontWeight = FontWeight.Medium)
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
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
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

private data class StatusVisual(
    val bgColor: Color,
    val textColor: Color,
    val iconColor: Color,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)
