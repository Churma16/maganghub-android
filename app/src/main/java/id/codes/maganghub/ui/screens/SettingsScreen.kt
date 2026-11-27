package id.codes.maganghub.ui.screens

import id.codes.maganghub.BuildConfig
import id.codes.maganghub.data.model.ThemeMode
import id.codes.maganghub.data.model.UpdateCheckState

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.lifecycle.Lifecycle
import id.codes.maganghub.ui.theme.AccentSkyDark
import id.codes.maganghub.ui.theme.AccentSkyLight
import kotlin.math.roundToInt
import androidx.lifecycle.LifecycleEventObserver
import id.codes.maganghub.background.NotificationHelper
import id.codes.maganghub.data.model.ReminderSettings
import id.codes.maganghub.ui.theme.ChipSelectedText
import id.codes.maganghub.ui.theme.HadirGreenLight
import id.codes.maganghub.ui.theme.HadirGreenText
import id.codes.maganghub.ui.theme.KemnakerSky
import id.codes.maganghub.ui.theme.NavActivePillBg
import id.codes.maganghub.ui.theme.PureWhite
import id.codes.maganghub.ui.viewmodel.AttendanceUiState

@Composable
private fun DefaultAvatarPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SettingsScreen(
    uiState: AttendanceUiState,
    onUpdateReminderSettings: (ReminderSettings) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit = {},
    onOpenLoginDialog: () -> Unit,
    onLogout: () -> Unit,
    onTestNotification: () -> Unit,
    isPreviewBuild: Boolean = false,
    onOpenTestNotificationSheet: () -> Unit = {},
    onOpenFeedback: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    formatLastUpdateCheck: (Long) -> String = { "" },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var currentToast by remember { mutableStateOf<Toast?>(null) }

    val showFeedbackToast: (String) -> Unit = { message ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        currentToast?.cancel()
        currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT).apply { show() }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentToast?.cancel()
        }
    }

    val settings = uiState.reminderSettings

    // Check battery optimization exemption dynamically and observe lifecycle resume
    val powerManager = remember(context) { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isIgnoringBatteryOptimizations = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (uiState.themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val cardBorderColor = MaterialTheme.colorScheme.outline
    val cardBorder = BorderStroke(1.dp, cardBorderColor)
    val cardElevation = CardDefaults.cardElevation(defaultElevation = 0.dp)

    // Quick action outline buttons
    val quickActionBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    val quickActionIcon = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Quick Action Buttons: side-by-side outlined buttons [🔔] Uji notifikasi and [↗] Portal Monev
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (isPreviewBuild) {
                        onOpenTestNotificationSheet()
                    } else {
                        onTestNotification()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = quickActionBorder,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = quickActionIcon
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Uji notifikasi",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedButton(
                onClick = {
                    NotificationHelper.openWebUrl(context, NotificationHelper.URL_MONEV_DASHBOARD)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = quickActionBorder,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = quickActionIcon
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Portal Monev",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 2. Account Card: Avatar, user email/username, TERHUBUNG KE SSO badge, and full-width Keluar dari akun button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = cardBorder,
            elevation = cardElevation
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (uiState.isLoggedIn) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!uiState.photoUrl.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(uiState.photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto Profil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                loading = { DefaultAvatarPlaceholder() },
                                error = { DefaultAvatarPlaceholder() }
                            )
                        } else {
                            DefaultAvatarPlaceholder()
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            val displayName = uiState.fullName.ifBlank { uiState.username.ifBlank { "Pengguna Kemnaker" } }
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (uiState.fullName.isNotBlank() && uiState.username.isNotBlank()) {
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = uiState.username,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = HadirGreenLight
                            ) {
                                Text(
                                    text = "TERHUBUNG KE SSO",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HadirGreenText
                                )
                            }
                        }
                    }

                    // Full-width "Keluar dari akun" outlined button matching tokens:
                    // Outline btn text #FDA4AF / #9F1239, border #BE3A55 / #BE123C
                    val logoutBorder = BorderStroke(1.dp, if (isDark) Color(0xFFBE3A55) else Color(0xFFBE123C))
                    val logoutTextColor = if (isDark) Color(0xFFFDA4AF) else Color(0xFF9F1239)

                    OutlinedButton(
                        onClick = { showLogoutConfirm = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = logoutTextColor
                        ),
                        border = logoutBorder
                    ) {
                        Text(
                            text = "Keluar dari akun",
                            color = logoutTextColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Belum terhubung ke akun Monev Kemnaker.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onOpenLoginDialog,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1))
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = PureWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Masuk Akun Kemnaker", color = PureWhite)
                        }
                    }
                }
            }
        }

        // 3. Tampilan Card (Theme Mode Override)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = cardBorder,
            elevation = cardElevation
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tampilan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 3-Option Segmented Control with smooth sliding animated pill
                ThemeSegmentedSlider(
                    currentMode = uiState.themeMode,
                    isDark = isDark,
                    onSelectMode = onSetThemeMode
                )

                Text(
                    text = when (uiState.themeMode) {
                        ThemeMode.SYSTEM -> "Mengikuti pengaturan tema di HP kamu."
                        ThemeMode.LIGHT -> "Tema terang selalu aktif."
                        ThemeMode.DARK -> "Tema gelap selalu aktif."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 4. Izin Latar Belakang card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = cardBorder,
            elevation = cardElevation
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (isIgnoringBatteryOptimizations) Color(0xFF15803D) else Color(0xFFBE123C),
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = "Izin Latar Belakang",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isIgnoringBatteryOptimizations) {
                            "Baterai tidak dibatasi sistem (Optimal)."
                        } else {
                            "Baterai dibatasi sistem. Ubah ke 'Tidak Dibatasi'."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isIgnoringBatteryOptimizations) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(
                                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                    Uri.parse("package:${context.packageName}")
                                ).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(fallback)
                                } catch (_: Exception) {}
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE123C))
                    ) {
                        Text(
                            text = "Izinkan",
                            color = PureWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 5. Pengingat Card: Jam mulai chips, Interval chips, 5 toggle switches
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = cardBorder,
            elevation = cardElevation
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Pengingat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Jam Mulai Kerja (WIB) with Native TimePickerDialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Jam mulai kerja (WIB)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Awal fase 'Selamat bekerja' sebelum pengingat logbook dimulai",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val workTimeText = String.format("%02d:%02d WIB", settings.workStartHour, settings.workStartMinute)
                    Surface(
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) Color(0xFF0B1220) else Color(0xFFEEF2F7),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF3B4B6B) else Color(0xFF94A3B8)),
                        onClick = {
                            val timePicker = android.app.TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    if (hour >= settings.startHour) {
                                        showFeedbackToast("Jam mulai kerja harus sebelum jam notifikasi (${settings.startHour}:00 WIB)")
                                    } else {
                                        onUpdateReminderSettings(
                                            settings.copy(
                                                workStartHour = hour,
                                                workStartMinute = minute
                                            )
                                        )
                                        showFeedbackToast(String.format("Jam mulai kerja diatur ke %02d:%02d WIB", hour, minute))
                                    }
                                },
                                settings.workStartHour,
                                settings.workStartMinute,
                                true
                            )
                            timePicker.show()
                        }
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = workTimeText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
                            )
                        }
                    }
                }

                HorizontalDivider(color = cardBorderColor)

                // Jam Mulai Pengingat Logbook (WIB) - Slider 13:00 - 20:00 WIB
                var sliderStartHour by remember(settings.startHour) {
                    mutableStateOf(settings.startHour.coerceIn(13, 20).toFloat())
                }
                SettingsSliderRow(
                    title = "Jam mulai pengingat logbook (WIB)",
                    valueLabel = String.format(Locale.getDefault(), "%02d:00 WIB", sliderStartHour.roundToInt()),
                    value = sliderStartHour,
                    onValueChange = { sliderStartHour = it.roundToInt().toFloat() },
                    onValueChangeFinished = {
                        val targetHour = sliderStartHour.roundToInt()
                        if (targetHour <= settings.workStartHour) {
                            showFeedbackToast("Jam pengingat harus sesudah jam mulai kerja (${String.format(Locale.getDefault(), "%02d:%02d", settings.workStartHour, settings.workStartMinute)} WIB)")
                            sliderStartHour = settings.startHour.toFloat()
                        } else if (targetHour != settings.startHour) {
                            onUpdateReminderSettings(settings.copy(startHour = targetHour))
                            showFeedbackToast(String.format(Locale.getDefault(), "Pengingat dimulai pukul %02d:00 WIB", targetHour))
                        }
                    },
                    valueRange = 13f..20f,
                    steps = 6,
                    minLabel = "13:00 (1 Siang)",
                    maxLabel = "20:00 (8 Malam)",
                    isDark = isDark,
                    subtitle = "Pengingat harian aktif mulai jam ini sampai 23:59 WIB"
                )

                HorizontalDivider(color = cardBorderColor)

                // Interval Pengingat Logbook - Slider 15 - 120 menit (step 15 menit)
                var sliderInterval by remember(settings.intervalMinutes) {
                    val clamped = settings.intervalMinutes.coerceIn(15, 120)
                    val stepped = (kotlin.math.round(clamped / 15f) * 15).toInt()
                    mutableStateOf(stepped.toFloat())
                }
                val currentInterval = sliderInterval.roundToInt()
                val intervalLabel = when {
                    currentInterval < 60 -> "$currentInterval menit"
                    currentInterval % 60 == 0 -> "${currentInterval / 60} jam"
                    else -> "${currentInterval / 60} jam ${currentInterval % 60} mnt"
                }
                SettingsSliderRow(
                    title = "Interval pengingat",
                    valueLabel = intervalLabel,
                    value = sliderInterval,
                    onValueChange = {
                        val snapped = (kotlin.math.round(it / 15f) * 15).coerceIn(15f, 120f)
                        sliderInterval = snapped
                    },
                    onValueChangeFinished = {
                        val targetInterval = sliderInterval.roundToInt()
                        if (targetInterval != settings.intervalMinutes) {
                            onUpdateReminderSettings(settings.copy(intervalMinutes = targetInterval))
                            val label = when {
                                targetInterval < 60 -> "$targetInterval menit"
                                targetInterval % 60 == 0 -> "${targetInterval / 60} jam"
                                else -> "${targetInterval / 60} jam ${targetInterval % 60} mnt"
                            }
                            showFeedbackToast("Interval notifikasi diatur tiap $label")
                        }
                    },
                    valueRange = 15f..120f,
                    steps = 6,
                    minLabel = "15 menit",
                    maxLabel = "2 jam",
                    isDark = isDark,
                    subtitle = "Jarak waktu pengulangan notifikasi jika belum mengisi logbook"
                )

                HorizontalDivider(color = cardBorderColor)

                // Switch 1: Pengingat berulang
                // Toggles: Track on #0369A1, off #3B4B6B / #64748B, knob #FFFFFF
                ReminderToggleRow(
                    title = "Pengingat berulang",
                    subtitle = "Notifikasi berkala sampai 23:59 jika logbook belum diisi",
                    checked = settings.isEnabled,
                    isDark = isDark,
                    onCheckedChange = { checked ->
                        onUpdateReminderSettings(settings.copy(isEnabled = checked))
                        showFeedbackToast(if (checked) "Notifikasi berulang diaktifkan" else "Notifikasi berulang dinonaktifkan")
                    }
                )

                // Switch 2: Pengingat pagi (08:00 WIB)
                ReminderToggleRow(
                    title = "Pengingat pagi",
                    subtitle = "Satu notifikasi pukul 08:00 sebagai awal hari",
                    checked = settings.morningReminderEnabled,
                    isDark = isDark,
                    onCheckedChange = { checked ->
                        onUpdateReminderSettings(settings.copy(morningReminderEnabled = checked))
                        showFeedbackToast(if (checked) "Notifikasi pagi diaktifkan" else "Notifikasi pagi dinonaktifkan")
                    }
                )

                // Switch 3: Lewati hari libur
                ReminderToggleRow(
                    title = "Lewati hari libur",
                    subtitle = "Tidak mengingatkan di hari libur atau off",
                    checked = settings.skipHolidays,
                    isDark = isDark,
                    onCheckedChange = { checked ->
                        onUpdateReminderSettings(settings.copy(skipHolidays = checked))
                        showFeedbackToast(if (checked) "Lewati hari libur diaktifkan" else "Lewati hari libur dinonaktifkan")
                    }
                )

                // Switch 4: Bunyi notifikasi
                ReminderToggleRow(
                    title = "Bunyi notifikasi",
                    subtitle = "Putar suara saat pengingat muncul",
                    checked = settings.soundEnabled,
                    isDark = isDark,
                    onCheckedChange = { checked ->
                        onUpdateReminderSettings(settings.copy(soundEnabled = checked))
                        showFeedbackToast(if (checked) "Bunyi notifikasi diaktifkan" else "Bunyi notifikasi dinonaktifkan")
                    }
                )

                // Switch 5: Getar notifikasi
                ReminderToggleRow(
                    title = "Getar notifikasi",
                    subtitle = "Getarkan perangkat saat pengingat muncul",
                    checked = settings.vibrationEnabled,
                    isDark = isDark,
                    onCheckedChange = { checked ->
                        onUpdateReminderSettings(settings.copy(vibrationEnabled = checked))
                        showFeedbackToast(if (checked) "Getar notifikasi diaktifkan" else "Getar notifikasi dinonaktifkan")
                    }
                )
            }
        }

        // 6. Pengingat Mentor Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = cardBorder,
            elevation = cardElevation
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Pengingat Approval Mentor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Beri tahu jika mentor belum meng-approve logbook",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Switch row: "Aktifkan notifikasi pengingat"
                ReminderToggleRow(
                    title = "Aktifkan notifikasi pengingat",
                    subtitle = "Kirim notifikasi saat laporan belum di-approve melewati batas hari",
                    checked = settings.mentorReminderEnabled,
                    isDark = isDark,
                    onCheckedChange = { checked ->
                        onUpdateReminderSettings(settings.copy(mentorReminderEnabled = checked))
                        showFeedbackToast(if (checked) "Notifikasi mentor diaktifkan" else "Notifikasi mentor dinonaktifkan")
                    }
                )

                if (settings.mentorReminderEnabled) {
                    HorizontalDivider(color = cardBorderColor)

                    // Batas hari belum di-approve - Slider 1..6 hari
                    var sliderThreshold by remember(settings.mentorReviewThresholdDays) {
                        mutableStateOf(settings.mentorReviewThresholdDays.coerceIn(1, 6).toFloat())
                    }
                    SettingsSliderRow(
                        title = "Batas hari belum di-approve",
                        valueLabel = "${sliderThreshold.roundToInt()} hari",
                        value = sliderThreshold,
                        onValueChange = { sliderThreshold = it.roundToInt().toFloat() },
                        onValueChangeFinished = {
                            val targetDays = sliderThreshold.roundToInt()
                            if (targetDays != settings.mentorReviewThresholdDays) {
                                onUpdateReminderSettings(settings.copy(mentorReviewThresholdDays = targetDays))
                                showFeedbackToast("Batas review mentor diatur $targetDays hari")
                            }
                        },
                        valueRange = 1f..6f,
                        steps = 4,
                        minLabel = "1 hari",
                        maxLabel = "6 hari",
                        isDark = isDark,
                        subtitle = "Kirim peringatan jika logbook belum diverifikasi setelah sekian hari"
                    )

                    HorizontalDivider(color = cardBorderColor)

                    // Ulangi pengingat setiap - Slider 1..24 jam
                    var sliderMentorHours by remember(settings.mentorRepeatIntervalMinutes) {
                        val hours = (settings.mentorRepeatIntervalMinutes / 60).coerceIn(1, 24)
                        mutableStateOf(hours.toFloat())
                    }
                    val currentMentorHours = sliderMentorHours.roundToInt()
                    val mentorHoursLabel = if (currentMentorHours == 24) "24 jam (1 hari)" else "$currentMentorHours jam"
                    SettingsSliderRow(
                        title = "Ulangi pengingat setiap",
                        valueLabel = mentorHoursLabel,
                        value = sliderMentorHours,
                        onValueChange = { sliderMentorHours = it.roundToInt().toFloat() },
                        onValueChangeFinished = {
                            val targetHours = sliderMentorHours.roundToInt()
                            val targetMinutes = targetHours * 60
                            if (targetMinutes != settings.mentorRepeatIntervalMinutes) {
                                onUpdateReminderSettings(settings.copy(mentorRepeatIntervalMinutes = targetMinutes))
                                val label = if (targetHours == 24) "24 jam (1 hari)" else "$targetHours jam"
                                showFeedbackToast("Notifikasi mentor diulang tiap $label")
                            }
                        },
                        valueRange = 1f..24f,
                        steps = 22,
                        minLabel = "1 jam",
                        maxLabel = "24 jam (1 hari)",
                        isDark = isDark,
                        subtitle = "Jeda waktu antar notifikasi pengingat review mentor"
                    )
                }

                if (isPreviewBuild && !uiState.mentorSyncDebug.isNullOrBlank()) {
                    HorizontalDivider(color = cardBorderColor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Sinkronisasi:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.mentorSyncDebug,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 7. Bantuan dan Informasi Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = cardBorder,
            elevation = cardElevation
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Bantuan dan informasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
                )

                // Row 1: Kirim masukan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFeedback() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = KemnakerSky,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Kirim masukan",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Laporkan bug atau usul fitur",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                HorizontalDivider(
                    color = cardBorderColor,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Row 2: Kebijakan Privasi
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPrivacyPolicy() }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Kebijakan Privasi",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                HorizontalDivider(
                    color = cardBorderColor,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Row 3: Syarat dan Ketentuan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTerms() }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Syarat dan Ketentuan",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 8. Tentang app Card (replaces plain footer version text)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = cardBorder,
            elevation = cardElevation
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header: title + app name + version
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Tentang app",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "MagangHub Logbook Companion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Versi ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Last checked timestamp: only shown in Awal / Idle / Error state matching Figma mockup
                if (uiState.lastUpdateCheckTimestamp > 0 &&
                    (uiState.updateCheckState is UpdateCheckState.Idle || uiState.updateCheckState is UpdateCheckState.Error)) {
                    val lastCheckedText = formatLastUpdateCheck(uiState.lastUpdateCheckTimestamp)
                    if (lastCheckedText.isNotBlank()) {
                        Text(
                            text = lastCheckedText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // State-dependent content matching the 4 mockup conditions
                when (val checkState = uiState.updateCheckState) {
                    is UpdateCheckState.Idle, is UpdateCheckState.Error -> {
                        // State 1 (Awal / Error): OutlinedButton with Refresh icon
                        OutlinedButton(
                            onClick = onCheckForUpdates,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = quickActionBorder
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isDark) KemnakerSky else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cek pembaruan",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        // Show error message if check failed
                        if (checkState is UpdateCheckState.Error) {
                            Text(
                                text = checkState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    is UpdateCheckState.Checking -> {
                        // State 2 (Memeriksa): disabled button with spinner matching mockup
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = quickActionBorder
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = if (isDark) KemnakerSky else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Memeriksa...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    is UpdateCheckState.UpToDate -> {
                        // State 3 (Sudah terbaru): green info banner with checkmark + Cek pembaruan button
                        val successBg = if (isDark) Color(0xFF14532D) else Color(0xFFDCFCE7)
                        val successFg = if (isDark) Color(0xFF86EFAC) else Color(0xFF15803D)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = successBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = successFg
                                )
                                Text(
                                    text = "Kamu sudah memakai versi terbaru.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = successFg,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = onCheckForUpdates,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = quickActionBorder
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isDark) KemnakerSky else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cek pembaruan",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    is UpdateCheckState.Available -> {
                        // State 4 (Pembaruan tersedia): deep blue info banner + Perbarui sekarang button
                        val updateInfo = checkState.updateInfo
                        val infoBg = if (isDark) Color(0xFF163E5C) else Color(0xFFDBEAFE)
                        val titleFg = if (isDark) PureWhite else Color(0xFF1E40AF)
                        val subFg = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = infoBg
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Versi ${updateInfo.versionName} tersedia",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = titleFg
                                )
                                if (updateInfo.changelog.isNotBlank()) {
                                    Text(
                                        text = "Yang baru: ${updateInfo.changelog}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = subFg
                                    )
                                }
                            }
                        }
                        Button(
                            onClick = {
                                NotificationHelper.openWebUrl(context, updateInfo.downloadUrl)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KemnakerSky,
                                contentColor = PureWhite
                            )
                        ) {
                            Text(
                                text = "Perbarui sekarang",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Logout Confirmation Dialog: Dialog bg #3A1620 / #FFE4E6, border #7F2A3B / #FDA4AF. Confirm btn: #BE123C, text #FFFFFF
    if (showLogoutConfirm) {
        val dialogBg = if (isDark) Color(0xFF3A1620) else Color(0xFFFFE4E6)
        val dialogBorder = if (isDark) Color(0xFF7F2A3B) else Color(0xFFFDA4AF)
        val dialogTitleText = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
        val dialogBodyText = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)

        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = {
                Text(
                    text = "Konfirmasi Keluar",
                    fontWeight = FontWeight.Bold,
                    color = dialogTitleText
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus kredensial tersimpan dan keluar dari akun?",
                    color = dialogBodyText
                )
            },
            containerColor = dialogBg,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.dp, dialogBorder, RoundedCornerShape(16.dp)),
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE123C))
                ) {
                    Text("Keluar dari akun", color = PureWhite, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Batal", color = if (isDark) Color(0xFFA8B3C7) else Color(0xFF475569))
                }
            }
        )
    }
}

/**
 * Reusable row for reminder switches
 */
@Composable
private fun ReminderToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    isDark: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = Color(0xFF0369A1),
                uncheckedThumbColor = PureWhite,
                uncheckedTrackColor = if (isDark) Color(0xFF3B4B6B) else Color(0xFF64748B)
            )
        )
    }
}

/**
 * Modern Segmented Control with a smooth sliding animated pill background.
 */
@Composable
private fun ThemeSegmentedSlider(
    currentMode: ThemeMode,
    isDark: Boolean,
    onSelectMode: (ThemeMode) -> Unit
) {
    val trackBg = MaterialTheme.colorScheme.surfaceVariant
    val selectedIndex = when (currentMode) {
        ThemeMode.SYSTEM -> 0
        ThemeMode.LIGHT -> 1
        ThemeMode.DARK -> 2
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = trackBg,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            val pillWidth = maxWidth / 3
            val animatedOffset by animateDpAsState(
                targetValue = pillWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = 380f
                ),
                label = "pillOffset"
            )

            // Animated sliding pill
            Surface(
                modifier = Modifier
                    .width(pillWidth)
                    .height(40.dp)
                    .offset(x = animatedOffset),
                shape = RoundedCornerShape(8.dp),
                color = NavActivePillBg,
                border = BorderStroke(1.dp, Color(0xFF93C5FD).copy(alpha = 0.5f))
            ) {}

            // The 3 options row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Triple(ThemeMode.SYSTEM, Icons.Outlined.DesktopWindows, "Sistem"),
                    Triple(ThemeMode.LIGHT, Icons.Outlined.LightMode, "Terang"),
                    Triple(ThemeMode.DARK, Icons.Outlined.DarkMode, "Gelap")
                ).forEachIndexed { index, (mode, icon, label) ->
                    val selected = currentMode == mode
                    val targetTextColor = when {
                        selected -> ChipSelectedText
                        isDark -> Color(0xFFA8B3C7)
                        else -> Color(0xFF475569)
                    }
                    val textColor by animateColorAsState(
                        targetValue = targetTextColor,
                        animationSpec = tween(200),
                        label = "textColor$index"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onSelectMode(mode)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = textColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modern tactile slider row for timing and threshold configurations.
 */
@Composable
private fun SettingsSliderRow(
    title: String,
    valueLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    minLabel: String,
    maxLabel: String,
    isDark: Boolean,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isDark) Color(0xFF162033) else Color(0xFFDBEAFE),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF3B4B6B) else Color(0xFF93C5FD))
            ) {
                Text(
                    text = valueLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1E3A8A)
                )
            }
        }

        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val primaryColor = if (isDark) AccentSkyDark else AccentSkyLight
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor,
                inactiveTrackColor = if (isDark) Color(0xFF263450) else Color(0xFFE2E8F0)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "$title: $valueLabel"
                }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = minLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = maxLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
