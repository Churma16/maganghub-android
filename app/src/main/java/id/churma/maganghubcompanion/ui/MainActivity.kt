package id.churma.maganghubcompanion.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import id.churma.maganghubcompanion.MagangHubApp
import id.churma.maganghubcompanion.data.model.LegalDocumentType
import id.churma.maganghubcompanion.data.model.ThemeMode
import id.churma.maganghubcompanion.background.AttendanceAlarmScheduler
import id.churma.maganghubcompanion.background.NotificationHelper
import id.churma.maganghubcompanion.ui.screens.DashboardScreen
import id.churma.maganghubcompanion.ui.screens.FeedbackBottomSheet
import id.churma.maganghubcompanion.ui.screens.LegalDocumentBottomSheet
import id.churma.maganghubcompanion.ui.screens.LoginDialog
import id.churma.maganghubcompanion.ui.screens.SettingsScreen
import id.churma.maganghubcompanion.ui.screens.TestNotificationBottomSheet
import id.churma.maganghubcompanion.ui.theme.KemnakerSky
import id.churma.maganghubcompanion.ui.theme.NavActivePillBg
import id.churma.maganghubcompanion.ui.theme.NavActivePillIcon
import id.churma.maganghubcompanion.ui.theme.MagangHubTheme
import id.churma.maganghubcompanion.ui.viewmodel.AttendanceViewModel
import id.churma.maganghubcompanion.ui.viewmodel.AttendanceViewModelFactory

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_FROM_WIDGET = "FROM_WIDGET"
    }

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

        // Handle entry point triggers (e.g. opened from home screen widget) only on fresh start
        if (savedInstanceState == null) {
            handleWidgetIntent(intent)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (uiState.themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MagangHubTheme(darkTheme = darkTheme) {
                MainContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent(intent)
    }

    private fun handleWidgetIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_FROM_WIDGET, false) == true) {
            intent.removeExtra(EXTRA_FROM_WIDGET)
            viewModel.onOpenedFromWidget()
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
    val selectedTab by viewModel.selectedTab.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == 0) "Logbook MagangHub" else "Pengaturan",
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
                                    contentDescription = "Perbarui Logbook",
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
            Column {
                Divider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavActivePillIcon,
                            selectedTextColor = NavActivePillIcon,
                            indicatorColor = NavActivePillBg,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                        label = { Text("Pengaturan") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavActivePillIcon,
                            selectedTextColor = NavActivePillIcon,
                            indicatorColor = NavActivePillBg,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> DashboardScreen(
                uiState = uiState,
                onCheckNow = { viewModel.checkAttendanceNow(silent = false) },
                onOpenLoginDialog = { viewModel.setLoginDialogOpen(true) },
                onOpenPrivacyPolicy = { viewModel.showLegalDocument(LegalDocumentType.PRIVACY_POLICY) },
                onOpenTerms = { viewModel.showLegalDocument(LegalDocumentType.TERMS_OF_SERVICE) },
                onNavigateToSettings = { viewModel.selectTab(1) },
                onDismissMessage = { viewModel.clearMessages() },
                onSnoozeMentorReminder = { viewModel.snoozeMentorReminderOneDay() },
                formatLastSync = { viewModel.formatLastSync(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            1 -> SettingsScreen(
                uiState = uiState,
                onUpdateReminderSettings = { viewModel.updateReminderSettings(it) },
                onSetThemeMode = { viewModel.setThemeMode(it) },
                onOpenLoginDialog = { viewModel.setLoginDialogOpen(true) },
                onOpenPrivacyPolicy = { viewModel.showLegalDocument(LegalDocumentType.PRIVACY_POLICY) },
                onOpenTerms = { viewModel.showLegalDocument(LegalDocumentType.TERMS_OF_SERVICE) },
                onLogout = { viewModel.logout() },
                onTestNotification = {
                    viewModel.testNotification(MagangHubApp.instance.applicationContext)
                },
                isPreviewBuild = viewModel.isPreviewOrDebugBuild,
                onOpenTestNotificationSheet = { viewModel.setTestNotificationSheetOpen(true) },
                onOpenFeedback = { viewModel.setFeedbackSheetOpen(true) },
                onCheckForUpdates = { viewModel.checkForUpdates() },
                formatLastUpdateCheck = { viewModel.formatLastUpdateCheck(it) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }

        // Shared Login Dialog overlay
        key(uiState.isLoggedIn) {
            LoginDialog(
                isOpen = uiState.isLoginDialogOpen,
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                onDismiss = { viewModel.setLoginDialogOpen(false) },
                onOpenPrivacyPolicy = { viewModel.showLegalDocument(LegalDocumentType.PRIVACY_POLICY) },
                onOpenTerms = { viewModel.showLegalDocument(LegalDocumentType.TERMS_OF_SERVICE) },
                onLogin = { username, password ->
                    viewModel.login(username, password)
                }
            )
        }

        // Shared Feedback Bottom Sheet overlay
        FeedbackBottomSheet(
            isOpen = uiState.isFeedbackSheetOpen,
            isSubmitting = uiState.isSubmittingFeedback,
            errorMessage = uiState.errorMessage,
            onDismiss = { viewModel.setFeedbackSheetOpen(false) },
            onSubmit = { type, message ->
                viewModel.submitFeedback(type, message)
            }
        )

        // Shared In-App Legal Document Bottom Sheet overlay
        LegalDocumentBottomSheet(
            documentType = uiState.activeLegalDocument,
            onDismiss = { viewModel.dismissLegalDocument() }
        )

        // Preview QA: Test Notification State Bottom Sheet overlay
        if (uiState.isTestNotificationSheetOpen) {
            TestNotificationBottomSheet(
                onDismiss = { viewModel.setTestNotificationSheetOpen(false) },
                onTriggerDailyReminder = {
                    viewModel.testDailyReminderNotification(MagangHubApp.instance.applicationContext)
                    viewModel.setTestNotificationSheetOpen(false)
                },
                onTriggerMentorReminder = {
                    viewModel.testMentorReviewNotification(MagangHubApp.instance.applicationContext)
                    viewModel.setTestNotificationSheetOpen(false)
                },
                onTriggerSuccess = {
                    viewModel.testSuccessNotification(MagangHubApp.instance.applicationContext)
                    viewModel.setTestNotificationSheetOpen(false)
                }
            )
        }
    }
}
