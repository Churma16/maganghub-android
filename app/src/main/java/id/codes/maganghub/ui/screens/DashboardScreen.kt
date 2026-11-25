package id.codes.maganghub.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.codes.maganghub.background.NotificationHelper
import id.codes.maganghub.data.model.AttendanceStatus
import id.codes.maganghub.data.model.ThemeMode
import id.codes.maganghub.ui.theme.AccentSkyDark
import id.codes.maganghub.ui.theme.AccentSkyLight
import id.codes.maganghub.ui.theme.HeroCompleteBgDark
import id.codes.maganghub.ui.theme.HeroCompleteBgLight
import id.codes.maganghub.ui.theme.HeroCompleteBorderDark
import id.codes.maganghub.ui.theme.HeroCompleteBorderLight
import id.codes.maganghub.ui.theme.HeroCompleteDateDark
import id.codes.maganghub.ui.theme.HeroCompleteDateLight
import id.codes.maganghub.ui.theme.HeroCompleteDescDark
import id.codes.maganghub.ui.theme.HeroCompleteDescLight
import id.codes.maganghub.ui.theme.HeroCompleteIconDark
import id.codes.maganghub.ui.theme.HeroCompleteIconLight
import id.codes.maganghub.ui.theme.HeroCompleteTitleDark
import id.codes.maganghub.ui.theme.HeroCompleteTitleLight
import id.codes.maganghub.ui.theme.HeroIncompleteBgDark
import id.codes.maganghub.ui.theme.HeroIncompleteBgLight
import id.codes.maganghub.ui.theme.HeroIncompleteBorderDark
import id.codes.maganghub.ui.theme.HeroIncompleteBorderLight
import id.codes.maganghub.ui.theme.HeroIncompleteDateDark
import id.codes.maganghub.ui.theme.HeroIncompleteDateLight
import id.codes.maganghub.ui.theme.HeroIncompleteDescDark
import id.codes.maganghub.ui.theme.HeroIncompleteDescLight
import id.codes.maganghub.ui.theme.HeroIncompleteIconDark
import id.codes.maganghub.ui.theme.HeroIncompleteIconLight
import id.codes.maganghub.ui.theme.HeroIncompleteTitleDark
import id.codes.maganghub.ui.theme.HeroIncompleteTitleLight
import id.codes.maganghub.ui.theme.LogoutBtnTextDark
import id.codes.maganghub.ui.theme.LogoutBtnTextLight
import id.codes.maganghub.ui.theme.LogoutConfirmBgDark
import id.codes.maganghub.ui.theme.LogoutConfirmBgLight
import id.codes.maganghub.ui.theme.LogoutConfirmBorderDark
import id.codes.maganghub.ui.theme.LogoutConfirmBorderLight
import id.codes.maganghub.ui.theme.MetricLabelDark
import id.codes.maganghub.ui.theme.MetricLabelLight
import id.codes.maganghub.ui.theme.MetricPanelDark
import id.codes.maganghub.ui.theme.MetricPanelLight
import id.codes.maganghub.ui.theme.OnPrimaryCta
import id.codes.maganghub.ui.theme.OutlineButtonBorderDark
import id.codes.maganghub.ui.theme.OutlineButtonBorderLight
import id.codes.maganghub.ui.theme.OutlineButtonIconDark
import id.codes.maganghub.ui.theme.OutlineButtonIconLight
import id.codes.maganghub.ui.theme.OutlineDark
import id.codes.maganghub.ui.theme.OutlineLight
import id.codes.maganghub.ui.theme.PrimaryCta
import id.codes.maganghub.ui.theme.SegmentedTrackLight
import id.codes.maganghub.ui.theme.StatusHolidayBorderDark
import id.codes.maganghub.ui.theme.StatusHolidayBorderLight
import id.codes.maganghub.ui.theme.StatusHolidayIconDark
import id.codes.maganghub.ui.theme.StatusHolidayIconLight
import id.codes.maganghub.ui.theme.StatusInfoBgDark
import id.codes.maganghub.ui.theme.StatusInfoBgLight
import id.codes.maganghub.ui.theme.StatusInfoIconDark
import id.codes.maganghub.ui.theme.StatusInfoIconLight
import id.codes.maganghub.ui.theme.StatusInfoTextDark
import id.codes.maganghub.ui.theme.StatusInfoTextLight
import id.codes.maganghub.ui.theme.StatusSuccessBgDark
import id.codes.maganghub.ui.theme.StatusSuccessBgLight
import id.codes.maganghub.ui.theme.StatusSuccessIconDark
import id.codes.maganghub.ui.theme.StatusSuccessIconLight
import id.codes.maganghub.ui.theme.StatusSuccessTextDark
import id.codes.maganghub.ui.theme.StatusSuccessTextLight
import id.codes.maganghub.ui.theme.SurfaceDark
import id.codes.maganghub.ui.theme.SurfaceLight
import id.codes.maganghub.ui.theme.TextPrimaryDark
import id.codes.maganghub.ui.theme.TextPrimaryLight
import id.codes.maganghub.ui.theme.TextSecondaryDark
import id.codes.maganghub.ui.theme.TextSecondaryLight
import id.codes.maganghub.ui.viewmodel.AttendanceUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: AttendanceUiState,
    onCheckNow: () -> Unit,
    onOpenLoginDialog: () -> Unit,
    formatLastSync: (Long) -> String,
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    onDismissMessage: () -> Unit = {},
    onSnoozeMentorReminder: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onOpenTerms: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (uiState.themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val openMonevPortal: () -> Unit = {
        NotificationHelper.openWebUrl(context, NotificationHelper.URL_MONEV_DASHBOARD)
    }

    if (!uiState.isLoggedIn) {
        WelcomeScreen(
            onOpenLoginDialog = onOpenLoginDialog,
            modifier = modifier,
            onOpenPrivacyPolicy = onOpenPrivacyPolicy,
            onOpenTerms = onOpenTerms
        )
        return
    }

    var remainingTimeText by remember { mutableStateOf(calculateRemainingTime().first) }

    LaunchedEffect(Unit) {
        while (isActive) {
            val (timeText, delayMs) = calculateRemainingTime()
            remainingTimeText = timeText
            delay(delayMs)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Runtime Error Alert Banner with inline "Coba Lagi"
        AnimatedVisibility(
            visible = uiState.errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) LogoutConfirmBgDark else LogoutConfirmBgLight,
                border = BorderStroke(1.dp, if (isDark) LogoutConfirmBorderDark else LogoutConfirmBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isDark) LogoutBtnTextDark else LogoutBtnTextLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) LogoutBtnTextDark else LogoutBtnTextLight,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCheckNow()
                        }
                    ) {
                        Text(
                            text = "Coba Lagi",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) LogoutBtnTextDark else LogoutBtnTextLight
                        )
                    }
                }
            }
        }

        // 3. Success Banner
        AnimatedVisibility(
            visible = uiState.successMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BannerCard(
                icon = Icons.Default.CheckCircle,
                message = uiState.successMessage.orEmpty(),
                containerColor = if (isDark) HeroCompleteBgDark else HeroCompleteBgLight,
                borderColor = if (isDark) HeroCompleteBorderDark else HeroCompleteBorderLight,
                contentColor = if (isDark) StatusSuccessTextDark else StatusSuccessTextLight,
                onDismiss = onDismissMessage
            )
        }

        // 4. Hero Status Card (Purpose-First Utility)
        HeroStatusCard(
            uiState = uiState,
            remainingTimeText = remainingTimeText,
            isDark = isDark
        )

        // 5. Mini Status Cards (Status Magang & Jenis Hari)
        MiniStatusCards(
            uiState = uiState,
            isDark = isDark
        )

        // 6. Mentor Review Card (when pending reviews > 0)
        if (uiState.pendingMentorReviewCount > 0) {
            MentorReviewCard(
                uiState = uiState,
                isDark = isDark,
                onSnoozeMentorReminder = onSnoozeMentorReminder
            )
        }

        // 7. Pengingat Card with "Atur" Shortcut
        PengingatCard(
            uiState = uiState,
            onNavigateToSettings = onNavigateToSettings,
            haptic = haptic
        )

        // 7. Bottom Floating Primary CTA (Prominent Sky Blue + Monev Link)
        BottomCtaSection(
            openMonevPortal = openMonevPortal,
            haptic = haptic
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
 * High-Urgency Hero Status Card with Live Countdown to 23:59:59 WIB.
 */
@Composable
private fun HeroStatusCard(
    uiState: AttendanceUiState,
    remainingTimeText: String,
    isDark: Boolean
) {
    val isComplete = uiState.attendanceData?.hasAttendance == true || uiState.attendanceStatus == AttendanceStatus.HADIR

    val containerColor = when {
        isDark && isComplete -> HeroCompleteBgDark
        isDark && !isComplete -> HeroIncompleteBgDark
        !isDark && isComplete -> HeroCompleteBgLight
        else -> HeroIncompleteBgLight
    }
    val borderColor = when {
        isDark && isComplete -> HeroCompleteBorderDark
        isDark && !isComplete -> HeroIncompleteBorderDark
        !isDark && isComplete -> HeroCompleteBorderLight
        else -> HeroIncompleteBorderLight
    }
    val badgeColor = when {
        isDark && isComplete -> HeroCompleteIconDark
        isDark && !isComplete -> HeroIncompleteIconDark
        !isDark && isComplete -> HeroCompleteIconLight
        else -> HeroIncompleteIconLight
    }
    val badgeIcon = if (isComplete) Icons.Default.Check else Icons.Default.Close
    val statusTitle = if (isComplete) "Lengkap" else "Belum lengkap"
    val statusDescription = if (isComplete) {
        "Presensi dan kehadiran Anda hari ini sudah tercatat lengkap."
    } else {
        "Presensi dan logbook hari ini belum tercatat. Selesaikan sebelum batas waktu."
    }

    val titleColor = when {
        isDark && isComplete -> HeroCompleteTitleDark
        isDark && !isComplete -> HeroIncompleteTitleDark
        !isDark && isComplete -> HeroCompleteTitleLight
        else -> HeroIncompleteTitleLight
    }
    val dateColor = when {
        isDark && isComplete -> HeroCompleteDateDark
        isDark && !isComplete -> HeroIncompleteDateDark
        !isDark && isComplete -> HeroCompleteDateLight
        else -> HeroIncompleteDateLight
    }
    val descColor = when {
        isDark && isComplete -> HeroCompleteDescDark
        isDark && !isComplete -> HeroIncompleteDescDark
        !isDark && isComplete -> HeroCompleteDescLight
        else -> HeroIncompleteDescLight
    }
    val metricBoxColor = if (isDark) MetricPanelDark else MetricPanelLight
    val metricBorder = when {
        isDark -> BorderStroke(1.dp, Color.Transparent)
        isComplete -> BorderStroke(1.dp, HeroCompleteBorderLight)
        else -> BorderStroke(1.dp, HeroIncompleteBorderLight)
    }
    val metricLabelColor = if (isDark) MetricLabelDark else MetricLabelLight
    val metricValueColor = when {
        isDark -> Color.White
        isComplete -> HeroCompleteTitleLight
        else -> HeroIncompleteTitleLight
    }

    val animSpec = tween<Color>(300)
    val animatedContainerColor by animateColorAsState(containerColor, animSpec, label = "heroContainer")
    val animatedBorderColor by animateColorAsState(borderColor, animSpec, label = "heroBorder")
    val animatedBadgeColor by animateColorAsState(badgeColor, animSpec, label = "heroBadge")
    val animatedTitleColor by animateColorAsState(titleColor, animSpec, label = "heroTitle")
    val animatedDateColor by animateColorAsState(dateColor, animSpec, label = "heroDate")
    val animatedDescColor by animateColorAsState(descColor, animSpec, label = "heroDesc")
    val animatedMetricBoxColor by animateColorAsState(metricBoxColor, animSpec, label = "metricBox")
    val animatedMetricLabelColor by animateColorAsState(metricLabelColor, animSpec, label = "metricLabel")
    val animatedMetricValueColor by animateColorAsState(metricValueColor, animSpec, label = "metricValue")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
        border = BorderStroke(1.dp, animatedBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Badge & Status Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(animatedBadgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = statusTitle,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = formatHeroDate(uiState.attendanceData?.date),
                        style = MaterialTheme.typography.labelMedium,
                        color = animatedDateColor
                    )
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = animatedTitleColor
                    )
                }
            }

            // Description
            Text(
                text = statusDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = animatedDescColor,
                lineHeight = 20.sp
            )

            // Metric Box (Sisa Waktu & Batas Pengisian)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = animatedMetricBoxColor,
                border = metricBorder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sisa waktu",
                            style = MaterialTheme.typography.labelSmall,
                            color = animatedMetricLabelColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = remainingTimeText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = animatedMetricValueColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Batas pengisian",
                            style = MaterialTheme.typography.labelSmall,
                            color = animatedMetricLabelColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "23.59 WIB",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = animatedMetricValueColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mini Status Cards: Status Magang & Jenis Hari
 */
@Composable
private fun MiniStatusCards(uiState: AttendanceUiState, isDark: Boolean) {
    val isActive = uiState.attendanceData?.activeInternship != false
    val isHoliday = uiState.attendanceData?.isHoliday == true
    val isOffDay = uiState.attendanceData?.isScheduledOffDay == true
    val isLibur = isHoliday || isOffDay

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Row 1: Status Magang
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDark) StatusSuccessBgDark else StatusSuccessBgLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isDark) StatusSuccessIconDark else StatusSuccessIconLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Status magang",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isActive) "Magang aktif" else "Tidak aktif",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isActive) {
                        if (isDark) StatusSuccessBgDark else StatusSuccessBgLight
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = if (isActive) "Aktif" else "Non-Aktif",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) {
                            if (isDark) StatusSuccessTextDark else StatusSuccessTextLight
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp
            )

            // Row 2: Jenis Hari
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isLibur) {
                                if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                            } else {
                                if (isDark) StatusInfoBgDark else StatusInfoBgLight
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = if (isLibur) {
                            if (isDark) StatusHolidayIconDark else StatusHolidayIconLight
                        } else {
                            if (isDark) StatusInfoIconDark else StatusInfoIconLight
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Jenis hari",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            isHoliday -> "Hari Libur Nasional"
                            isOffDay -> "Off Day Terjadwal"
                            else -> "Hari kerja normal"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isLibur) {
                        if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                    } else {
                        if (isDark) StatusInfoBgDark else StatusInfoBgLight
                    },
                    border = if (isLibur) {
                        BorderStroke(1.dp, if (isDark) StatusHolidayBorderDark else StatusHolidayBorderLight)
                    } else null
                ) {
                    Text(
                        text = if (isLibur) "Libur" else "Wajib",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLibur) {
                            if (isDark) StatusHolidayIconDark else StatusHolidayIconLight
                        } else {
                            if (isDark) StatusInfoTextDark else StatusInfoTextLight
                        }
                    )
                }
            }
        }
    }
}

/**
 * Pengingat Card with Direct "Atur" Tab Switcher.
 */
@Composable
private fun PengingatCard(
    uiState: AttendanceUiState,
    onNavigateToSettings: () -> Unit,
    haptic: HapticFeedback
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Pengingat",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (!uiState.reminderSettings.isEnabled) {
                        "Pengingat dinonaktifkan"
                    } else {
                        uiState.nextReminderFormatted
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tiap ${uiState.reminderSettings.intervalMinutes} menit sampai 23:59. Berhenti otomatis jika sudah lengkap.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToSettings()
                }
            ) {
                Text(
                    text = "Atur",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Prominent Sky Blue Bottom Floating Primary CTA Button.
 */
@Composable
private fun BottomCtaSection(
    openMonevPortal: () -> Unit,
    haptic: HapticFeedback
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                openMonevPortal()
            },
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryCta,
                contentColor = OnPrimaryCta
            )
        ) {
            Text(
                text = "Isi laporan hari ini",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnPrimaryCta
            )
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 0.dp,
            modifier = Modifier.size(52.dp),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                openMonevPortal()
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "Buka Monev Portal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun BannerCard(
    icon: ImageVector,
    message: String,
    containerColor: Color,
    borderColor: Color? = null,
    contentColor: Color,
    onDismiss: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        border = borderColor?.let { BorderStroke(1.dp, it) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
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
            if (onDismiss != null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Mentor Review Card displayed when uiState.pendingMentorReviewCount > 0.
 * Normal State: subtle blue card, clock icon, progress toward reminder threshold.
 * Warning State: subtle amber card, warning icon, snooze 1 day action, copy template, share intent, and footer.
 */
@Composable
private fun MentorReviewCard(
    uiState: AttendanceUiState,
    isDark: Boolean,
    onSnoozeMentorReminder: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val pendingCount = uiState.pendingMentorReviewCount
    val threshold = uiState.reminderSettings.mentorReviewThresholdDays.coerceIn(1, 5)
    val isWarning = pendingCount >= threshold

    // Design tokens matching Kemnaker Monev theme
    val containerColor = if (isWarning) {
        if (isDark) Color(0xFF2E1B05) else Color(0xFFFFFBEB)
    } else {
        if (isDark) Color(0xFF0F2642) else Color(0xFFF0F9FF)
    }

    val borderColor = if (isWarning) {
        if (isDark) Color(0xFF78450F) else Color(0xFFFDE68A)
    } else {
        if (isDark) Color(0xFF1E4268) else Color(0xFFBAE6FD)
    }

    val iconBoxBg = if (isWarning) {
        if (isDark) Color(0xFF78350F) else Color(0xFFFEF3C7)
    } else {
        if (isDark) Color(0xFF075985) else Color(0xFFE0F2FE)
    }

    val iconTint = if (isWarning) {
        if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
    } else {
        if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    }

    val titleColor = if (isWarning) {
        if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E)
    } else {
        if (isDark) TextPrimaryDark else Color(0xFF0C4A6E)
    }

    val subtitleColor = if (isWarning) {
        if (isDark) Color(0xFFFDE68A).copy(alpha = 0.85f) else Color(0xFF78350F)
    } else {
        if (isDark) TextSecondaryDark else TextSecondaryLight
    }

    val progressColor = if (isWarning) {
        Color(0xFFF59E0B)
    } else {
        if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    }

    val progressTrackColor = if (isWarning) {
        if (isDark) Color(0xFF451A03) else Color(0xFFFDE68A)
    } else {
        if (isDark) Color(0xFF162033) else Color(0xFFE0F2FE)
    }

    val progressValue = if (isWarning) 1f else (pendingCount / threshold.toFloat()).coerceIn(0f, 1f)

    val intervalText = when (uiState.reminderSettings.mentorRepeatIntervalMinutes) {
        30 -> "30 mnt"
        60 -> "1 jam"
        180 -> "3 jam"
        1440 -> "1 hari"
        else -> "${uiState.reminderSettings.mentorRepeatIntervalMinutes} mnt"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Left Circle Icon + Title & Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(iconBoxBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isWarning) "Saatnya ingatkan Mentor" else "Menunggu peninjauan Mentor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isWarning) {
                            "$pendingCount kehadiran dan laporan menunggu peninjauan, sudah mencapai batas pengingat."
                        } else {
                            "$pendingCount kehadiran dan laporan sudah dikirim. Tidak ada tindakan yang perlu kamu lakukan."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor,
                        lineHeight = 18.sp
                    )
                }
            }

            // Progress Section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$pendingCount hari menunggu",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isWarning) titleColor else subtitleColor
                    )
                    Text(
                        text = if (isWarning) "Batas $threshold hari tercapai" else "Batas pengingat $threshold hari",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = subtitleColor
                    )
                }

                LinearProgressIndicator(
                    progress = { progressValue },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = progressTrackColor
                )
            }

            // Warning State Action Button & Footer
            if (isWarning) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSnoozeMentorReminder()
                        Toast.makeText(context, "Pengingat mentor ditunda 1 hari", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFFB45309) else Color(0xFFD97706)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tunda 1 hari",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Footer text: Pengingat otomatis diulang tiap ${intervalText}.
                Text(
                    text = "Pengingat otomatis diulang tiap $intervalText.",
                    style = MaterialTheme.typography.labelSmall,
                    color = subtitleColor
                )
            }
        }
    }
}

private val ZONE_WIB = ZoneId.of("Asia/Jakarta")
private val INDONESIAN_HERO_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale("id", "ID"))

private fun formatHeroDate(dateStr: String?): String {
    if (!dateStr.isNullOrBlank()) {
        try {
            val date = LocalDate.parse(dateStr)
            return date.format(INDONESIAN_HERO_DATE_FORMATTER)
        } catch (_: Exception) {
            return dateStr
        }
    }
    return LocalDate.now(ZONE_WIB).format(INDONESIAN_HERO_DATE_FORMATTER)
}

private fun calculateRemainingTime(): Pair<String, Long> {
    val now = ZonedDateTime.now(ZONE_WIB)
    val deadline = now.toLocalDate().atTime(23, 59, 59).atZone(ZONE_WIB)
    val duration = Duration.between(now, deadline)
    if (duration.isNegative || duration.isZero) {
        return Pair("0j 0m", 60_000L)
    }
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    val seconds = duration.seconds % 60
    return if (hours > 0) {
        Pair("${hours}j ${minutes}m", 30_000L)
    } else {
        Pair("${minutes}m ${seconds}d", 1_000L)
    }
}
