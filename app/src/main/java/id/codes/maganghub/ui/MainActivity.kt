package id.codes.maganghub.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import id.codes.maganghub.MagangHubApp
import id.codes.maganghub.background.AttendanceAlarmScheduler
import id.codes.maganghub.background.NotificationHelper
import id.codes.maganghub.ui.screens.DashboardScreen
import id.codes.maganghub.ui.screens.LoginDialog
import id.codes.maganghub.ui.screens.SettingsScreen
import id.codes.maganghub.ui.theme.KemnakerSky
import id.codes.maganghub.ui.theme.KemnakerSkyContainer
import id.codes.maganghub.ui.theme.MagangHubTheme
import id.codes.maganghub.ui.viewmodel.AttendanceViewModel
import id.codes.maganghub.ui.viewmodel.AttendanceViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: AttendanceViewModel by viewModels {
        AttendanceViewModelFactory(MagangHubApp.instance.attendanceRepository)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Trigger notification channel creation once permission response received
        NotificationHelper.createNotificationChannels(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize notification channels
        NotificationHelper.createNotificationChannels(applicationContext)

        // Request runtime permission for notifications on Android 13+ (API 33+)
        requestNotificationPermissionIfNeeded()

        // Ensure background alarm schedule is active
        AttendanceAlarmScheduler.scheduleNextAlarm(applicationContext)

        setContent {
            MagangHubTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(viewModel: AttendanceViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == 0) "Presensi MagangHub" else "Pengaturan Presensi",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    if (selectedTab == 0 && uiState.isLoggedIn) {
                        IconButton(
                            onClick = { viewModel.checkAttendanceNow(silent = false) },
                            enabled = !uiState.isChecking
                        ) {
                            if (uiState.isChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = KemnakerSky
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Perbarui Presensi",
                                    tint = KemnakerSky
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KemnakerSky,
                        selectedTextColor = KemnakerSky,
                        indicatorColor = KemnakerSkyContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                    label = { Text("Pengaturan") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = KemnakerSky,
                        selectedTextColor = KemnakerSky,
                        indicatorColor = KemnakerSkyContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> DashboardScreen(
                uiState = uiState,
                onCheckNow = { viewModel.checkAttendanceNow(silent = false) },
                onOpenLoginDialog = { viewModel.setLoginDialogOpen(true) },
                formatLastSync = { viewModel.formatLastSync(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            1 -> SettingsScreen(
                uiState = uiState,
                onUpdateReminderSettings = { viewModel.updateReminderSettings(it) },
                onOpenLoginDialog = { viewModel.setLoginDialogOpen(true) },
                onLogout = { viewModel.logout() },
                onTestNotification = {
                    viewModel.testNotification(MagangHubApp.instance.applicationContext)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }

        // Shared Login Dialog overlay
        LoginDialog(
            isOpen = uiState.isLoginDialogOpen,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            onDismiss = { viewModel.setLoginDialogOpen(false) },
            onLogin = { username, password ->
                viewModel.login(username, password)
            }
        )
    }
}
