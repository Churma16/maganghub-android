package id.codes.maganghub.ui.screens

import id.codes.maganghub.BuildConfig
import id.codes.maganghub.data.model.ThemeMode

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import id.codes.maganghub.background.NotificationHelper
import id.codes.maganghub.data.model.ReminderSettings
import id.codes.maganghub.ui.theme.DangerRed
import id.codes.maganghub.ui.theme.HadirGreen
import id.codes.maganghub.ui.theme.HadirGreenLight
import id.codes.maganghub.ui.theme.HadirGreenText
import id.codes.maganghub.ui.theme.KemnakerSky
import id.codes.maganghub.ui.theme.KemnakerSkyContainer
import id.codes.maganghub.ui.theme.KemnakerSkyOnContainer
import id.codes.maganghub.ui.theme.PureWhite
import id.codes.maganghub.ui.theme.Slate400
import id.codes.maganghub.ui.theme.Slate900
import id.codes.maganghub.ui.viewmodel.AttendanceUiState

@Composable
private fun DefaultAvatarPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(KemnakerSkyContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = KemnakerSkyOnContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SettingsScreen(
    uiState: AttendanceUiState,
    onUpdateReminderSettings: (ReminderSettings) -> Unit,
    onOpenLoginDialog: () -> Unit,
    onLogout: () -> Unit,
    onTestNotification: () -> Unit,
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
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = KemnakerSky
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
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = KemnakerSky
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
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
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

                    // Full-width "Keluar dari akun" button
                    OutlinedButton(
                        onClick = { showLogoutConfirm = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DangerRed
                        ),
                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "Keluar dari akun",
                            color = DangerRed,
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
                            colors = ButtonDefaults.buttonColors(containerColor = KemnakerSky)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Masuk Akun Kemnaker")
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
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
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

                // 3-Option Segmented Control
                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) {
                        Color(0xFF0B1422)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ThemeSegmentButton(
                            modifier = Modifier.weight(1f),
                            selected = uiState.themeMode == ThemeMode.SYSTEM,
                            icon = Icons.Outlined.DesktopWindows,
                            label = "Sistem",
                            onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                        )
                        ThemeSegmentButton(
                            modifier = Modifier.weight(1f),
                            selected = uiState.themeMode == ThemeMode.LIGHT,
                            icon = Icons.Outlined.LightMode,
                            label = "Terang",
                            onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                        )
                        ThemeSegmentButton(
                            modifier = Modifier.weight(1f),
                            selected = uiState.themeMode == ThemeMode.DARK,
                            icon = Icons.Outlined.DarkMode,
                            label = "Gelap",
                            onClick = { viewModel.setThemeMode(ThemeMode.DARK) }
                        )
                    }
                }

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
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
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
                                        color = if (isIgnoringBatteryOptimizations) HadirGreen else DangerRed,
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
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
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

        // 4. Pengingat Card: Jam mulai chips, Interval chips, 5 toggle switches
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
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

                val isDark = isSystemInDarkTheme()

                // Jam Mulai (WIB)
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
                            val chipBg = when {
                                selected && isDark -> PureWhite
                                selected && !isDark -> KemnakerSky
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                            val chipText = when {
                                selected && isDark -> Slate900
                                selected && !isDark -> PureWhite
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(chipBg)
                                    .then(
                                        if (!selected) Modifier.border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                            RoundedCornerShape(8.dp)
                                        ) else Modifier
                                    )
                                    .clickable {
                                        onUpdateReminderSettings(settings.copy(startHour = hour))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
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
                            val chipBg = when {
                                selected && isDark -> PureWhite
                                selected && !isDark -> KemnakerSky
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                            val chipText = when {
                                selected && isDark -> Slate900
                                selected && !isDark -> PureWhite
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(chipBg)
                                    .then(
                                        if (!selected) Modifier.border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                            RoundedCornerShape(8.dp)
                                        ) else Modifier
                                    )
                                    .clickable {
                                        onUpdateReminderSettings(settings.copy(intervalMinutes = interval))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
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

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Switch 1: Pengingat berulang
                ReminderToggleRow(
                    title = "Pengingat berulang",
                    subtitle = "Notifikasi berkala sampai 23:59 jika belum presensi dan logbook",
                    checked = settings.isEnabled,
                    onCheckedChange = { checked ->
                        onUpdateReminderSettings(settings.copy(isEnabled = checked))
                    }
                )

                // Switch 2: Pengingat pagi (08:00 WIB)
                ReminderToggleRow(
                    title = "Pengingat pagi",
                    subtitle = "Satu notifikasi pukul 08:00 sebagai awal hari",
                    checked = settings.morningReminderEnabled,
                    onCheckedChange = { checked ->
                        onUpdateReminderSettings(settings.copy(morningReminderEnabled = checked))
                    }
                )

                // Switch 3: Lewati hari libur
                ReminderToggleRow(
                    title = "Lewati hari libur",
                    subtitle = "Tidak mengingatkan di hari libur atau off",
                    checked = settings.skipHolidays,
                    onCheckedChange = { checked ->
                        onUpdateReminderSettings(settings.copy(skipHolidays = checked))
                    }
                )

                // Switch 4: Bunyi notifikasi
                ReminderToggleRow(
                    title = "Bunyi notifikasi",
                    subtitle = "Putar suara saat pengingat muncul",
                    checked = settings.soundEnabled,
                    onCheckedChange = { checked ->
                        onUpdateReminderSettings(settings.copy(soundEnabled = checked))
                    }
                )

                // Switch 5: Getar notifikasi
                ReminderToggleRow(
                    title = "Getar notifikasi",
                    subtitle = "Getarkan perangkat saat pengingat muncul",
                    checked = settings.vibrationEnabled,
                    onCheckedChange = { checked ->
                        onUpdateReminderSettings(settings.copy(vibrationEnabled = checked))
                    }
                )
            }
        }

        // 5. Footer: MagangHub Presensi Checker v1.0.0 with explanatory text
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "MagangHub Presensi Checker v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Aplikasi otomatisasi pengecekan kehadiran dan pengingat harian logbook magang terintegrasi Kemnaker SSO OAuth2.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = {
                Text(
                    text = "Konfirmasi Keluar",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus kredensial tersimpan dan keluar dari akun?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Keluar dari akun", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                checkedTrackColor = KemnakerSky,
                uncheckedThumbColor = Slate400,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * Single segmented pill item for theme mode selection.
 */
@Composable
private fun ThemeSegmentButton(
    modifier: Modifier = Modifier,
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val backgroundColor = if (selected) {
        if (isDark) Color.White else MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    val contentColor = if (selected) {
        if (isDark) Color(0xFF0F1E36) else MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}
