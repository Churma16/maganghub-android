package id.codes.maganghub.ui.screens

import id.codes.maganghub.BuildConfig
import id.codes.maganghub.data.model.ThemeMode

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import id.codes.maganghub.background.NotificationHelper
import id.codes.maganghub.data.model.ReminderSettings
import id.codes.maganghub.ui.theme.ChipSelectedText
import id.codes.maganghub.ui.theme.HadirGreenLight
import id.codes.maganghub.ui.theme.HadirGreenText
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
    onOpenFeedback: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showLogoutConfirm by remember { mutableStateOf(false) }

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
                onClick = onTestNotification,
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
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(NotificationHelper.URL_MONEV_DASHBOARD)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
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

                // Jam Mulai (WIB)
                // Chips: Unselected text #E2E8F0 / #1E293B, border #3B4B6B / #94A3B8. Selected: bg #DBEAFE, text #1E3A8A
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Jam mulai (WIB)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 16, 17, 18).forEach { hour ->
                            val selected = settings.startHour == hour
                            val chipBg = if (selected) Color(0xFFDBEAFE) else (if (isDark) Color(0xFF0B1220) else Color(0xFFEEF2F7))
                            val chipText = if (selected) Color(0xFF1E3A8A) else (if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B))
                            val chipBorder = if (selected) null else BorderStroke(1.dp, if (isDark) Color(0xFF3B4B6B) else Color(0xFF94A3B8))

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = chipBg,
                                border = chipBorder,
                                onClick = {
                                    onUpdateReminderSettings(settings.copy(startHour = hour))
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$hour:00",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = chipText
                                    )
                                }
                            }
                        }
                    }
                }

                // Interval (menit)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Interval (menit)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 45, 60).forEach { interval ->
                            val selected = settings.intervalMinutes == interval
                            val chipBg = if (selected) Color(0xFFDBEAFE) else (if (isDark) Color(0xFF0B1220) else Color(0xFFEEF2F7))
                            val chipText = if (selected) Color(0xFF1E3A8A) else (if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B))
                            val chipBorder = if (selected) null else BorderStroke(1.dp, if (isDark) Color(0xFF3B4B6B) else Color(0xFF94A3B8))

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = chipBg,
                                border = chipBorder,
                                onClick = {
                                    onUpdateReminderSettings(settings.copy(intervalMinutes = interval))
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$interval",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = chipText
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(color = cardBorderColor)

                // Switch 1: Pengingat berulang
                // Toggles: Track on #0369A1, off #3B4B6B / #64748B, knob #FFFFFF
                ReminderToggleRow(
                    title = "Pengingat berulang",
                    subtitle = "Notifikasi berkala sampai 23:59 jika belum presensi dan logbook",
                    checked = settings.isEnabled,
                    isDark = isDark,
                    onCheckedChange = { checked ->
                        onUpdateReminderSettings(settings.copy(isEnabled = checked))
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
                    }
                )
            }
        }

        // 6. Kirim Masukan Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenFeedback() },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = cardBorder,
            elevation = cardElevation
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
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
        }

        // 7. Footer: Clean centered version label matching mockups
        Text(
            text = "MagangHub Logbook Companion v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center
        )

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
