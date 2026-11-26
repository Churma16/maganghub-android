package id.codes.maganghub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.codes.maganghub.ui.theme.KemnakerSky
import id.codes.maganghub.ui.theme.PureWhite

/**
 * Preview QA modal bottom sheet allowing testers to trigger each notification state mockup directly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestNotificationBottomSheet(
    onDismiss: () -> Unit,
    onTriggerDailyReminder: () -> Unit,
    onTriggerMentorReminder: () -> Unit,
    onTriggerSuccess: () -> Unit
) {
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
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Science,
                        contentDescription = null,
                        tint = KemnakerSky,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Uji Mockup Notifikasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = KemnakerSky.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "PREVIEW QA",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = KemnakerSky
                            )
                        }
                    }
                    Text(
                        text = "Pilih state notifikasi untuk menguji layout & perilaku:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            // State 1: Daily Reminder
            NotificationMockupCard(
                icon = Icons.Outlined.NotificationsActive,
                iconColor = Color(0xFFEF4444),
                iconBg = Color(0xFFEF4444).copy(alpha = 0.15f),
                title = "1. Pengingat Harian (Belum Lengkap)",
                description = "Logbook hari ini belum diisi • Sisa waktu ke 23.59 WIB\nAksi: [Buka Monev] [Tunda] [Sudah isi]",
                cardBg = cardBg,
                onClick = onTriggerDailyReminder
            )

            // State 2: Mentor Review
            NotificationMockupCard(
                icon = Icons.Outlined.Schedule,
                iconColor = Color(0xFFF59E0B),
                iconBg = Color(0xFFF59E0B).copy(alpha = 0.15f),
                title = "2. Peninjauan Mentor Tertunda",
                description = "5 kehadiran menunggu approval mentor\nAksi: [Buka Monev] [Tunda 1 hari]",
                cardBg = cardBg,
                onClick = onTriggerMentorReminder
            )

            // State 3: Success Completion
            NotificationMockupCard(
                icon = Icons.Outlined.CheckCircle,
                iconColor = Color(0xFF10B981),
                iconBg = Color(0xFF10B981).copy(alpha = 0.15f),
                title = "3. Semua Beres Hari Ini (Sukses)",
                description = "Logbook hari ini sudah lengkap\nPerilaku: Auto-dismiss hilang otomatis dalam 12 detik",
                cardBg = cardBg,
                onClick = onTriggerSuccess
            )

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            // Close button
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
private fun NotificationMockupCard(
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    title: String,
    description: String,
    cardBg: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
