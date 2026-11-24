package id.codes.maganghub.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.codes.maganghub.background.NotificationHelper
import id.codes.maganghub.data.model.ReminderSettings
import id.codes.maganghub.ui.theme.BorderSlate
import id.codes.maganghub.ui.theme.CardBorderLight
import id.codes.maganghub.ui.theme.DangerRed
import id.codes.maganghub.ui.theme.HadirGreenLight
import id.codes.maganghub.ui.theme.HadirGreenText
import id.codes.maganghub.ui.theme.KemnakerSky
import id.codes.maganghub.ui.theme.KemnakerSkyContainer
import id.codes.maganghub.ui.theme.KemnakerSkyOnContainer
import id.codes.maganghub.ui.theme.PureWhite
import id.codes.maganghub.ui.theme.Slate100
import id.codes.maganghub.ui.theme.Slate400
import id.codes.maganghub.ui.theme.Slate600
import id.codes.maganghub.ui.theme.Slate900
import id.codes.maganghub.ui.viewmodel.AttendanceUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Section 1: Account
        SettingsSectionHeader(title = "Akun Kemnaker", icon = Icons.Default.Person)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (uiState.isLoggedIn) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = uiState.username.ifBlank { "Pengguna Kemnaker" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(2.dp))
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

                        // "Keluar dari akun" in red (0xFFDC2626)
                        OutlinedButton(
                            onClick = { showLogoutConfirm = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DangerRed
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed)
                        ) {
                            Text(
                                text = "Keluar dari akun",
                                color = DangerRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Belum terhubung ke akun Monev Kemnaker.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
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

        // Section 2: Reminder Configuration
        // Bell icon in soft blue square
        SettingsSectionHeader(title = "Pengaturan Pengingat", icon = Icons.Default.Notifications)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Switch with Kemnaker Sky Blue
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aktifkan Pengingat Spam",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900
                        )
                        Text(
                            text = "Kirim notifikasi berkala pukul 16:00 - 23:59 WIB jika belum presensi & logbook",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                    Switch(
                        checked = settings.isEnabled,
                        onCheckedChange = { checked ->
                            onUpdateReminderSettings(settings.copy(isEnabled = checked))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = KemnakerSky,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = BorderSlate
                        )
                    )
                }

                if (settings.isEnabled) {
                    Divider(color = BorderSlate.copy(alpha = 0.6f))

                    // Jam Mulai Pengingat
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Jam Mulai Pengingat (WIB):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Slate900
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 16, 17, 18).forEach { hour ->
                                FilterChip(
                                    selected = settings.startHour == hour,
                                    onClick = {
                                        onUpdateReminderSettings(settings.copy(startHour = hour))
                                    },
                                    label = { Text("$hour:00 WIB") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KemnakerSkyContainer,
                                        selectedLabelColor = KemnakerSkyOnContainer
                                    )
                                )
                            }
                        }
                    }

                    Divider(color = BorderSlate.copy(alpha = 0.6f))

                    // Interval Pengingat
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Interval Pengingat:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Slate900
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 30, 45, 60).forEach { interval ->
                                FilterChip(
                                    selected = settings.intervalMinutes == interval,
                                    onClick = {
                                        onUpdateReminderSettings(settings.copy(intervalMinutes = interval))
                                    },
                                    label = { Text("$interval Menit") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KemnakerSkyContainer,
                                        selectedLabelColor = KemnakerSkyOnContainer
                                    )
                                )
                            }
                        }
                    }

                    Divider(color = BorderSlate.copy(alpha = 0.6f))

                    // Suara Notifikasi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Slate600)
                            Text(
                                text = "Bunyi Notifikasi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate900
                            )
                        }
                        Switch(
                            checked = settings.soundEnabled,
                            onCheckedChange = { checked ->
                                onUpdateReminderSettings(settings.copy(soundEnabled = checked))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = KemnakerSky,
                                uncheckedThumbColor = Slate400,
                                uncheckedTrackColor = BorderSlate
                            )
                        )
                    }

                    // Getar Notifikasi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = Slate600)
                            Text(
                                text = "Getar Notifikasi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate900
                            )
                        }
                        Switch(
                            checked = settings.vibrationEnabled,
                            onCheckedChange = { checked ->
                                onUpdateReminderSettings(settings.copy(vibrationEnabled = checked))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = KemnakerSky,
                                uncheckedThumbColor = Slate400,
                                uncheckedTrackColor = BorderSlate
                            )
                        )
                    }
                }
            }
        }

        // Section 3: Diagnostic & Testing
        SettingsSectionHeader(title = "Pengujian & Akses Cepat", icon = Icons.Default.NotificationsActive)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onTestNotification,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp), tint = KemnakerSky)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uji Notifikasi Sekarang", color = Slate900)
                }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(NotificationHelper.URL_MONEV_DASHBOARD)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp), tint = KemnakerSky)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buka Monev MagangHub Portal", color = Slate900)
                }
            }
        }

        // Section 4: About
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Slate100.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "MagangHub Presensi Checker v1.0.0",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900
                )
                Text(
                    text = "Aplikasi otomatisasi pengecekan kehadiran dan pengingat harian logbook magang terintegrasi Kemnaker SSO OAuth2.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Konfirmasi Keluar", fontWeight = FontWeight.Bold, color = Slate900) },
            text = { Text("Apakah Anda yakin ingin menghapus kredensial tersimpan dan keluar dari akun?") },
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
                    Text("Batal", color = Slate600)
                }
            }
        )
    }
}

/**
 * Section Header with soft blue square icon container
 */
@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(KemnakerSkyContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = KemnakerSky,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
    }
}
