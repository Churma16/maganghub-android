package id.churma.maganghubcompanion.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import id.churma.maganghubcompanion.background.NotificationHelper

/**
 * Trampoline activity to safely launch the Monev Kemnaker web dashboard from the Glance Widget.
 *
 * Android 14+ (API 34+) disallows creating or retrieving PendingIntents wrapping implicit Intents
 * (e.g. Intent.ACTION_VIEW without an explicit component) inside RemoteViews / AppWidgets.
 * Routing through an explicit, translucent activity guarantees clean, unblocked foreground
 * browser launch across all Android versions.
 */
class MonevWebRedirectActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.openWebUrl(this, NotificationHelper.URL_MONEV_DASHBOARD)
        finish()
    }
}
