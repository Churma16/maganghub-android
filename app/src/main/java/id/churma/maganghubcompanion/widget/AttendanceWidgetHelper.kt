package id.churma.maganghubcompanion.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AttendanceWidgetHelper {

    private const val TAG = "AttendanceWidgetHelper"

    fun updateWidgetAsync(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            updateWidget(context)
        }
    }

    suspend fun updateWidget(context: Context) {
        val appContext = context.applicationContext ?: context

        // 1. Direct Glance updateAll to recompose widget content
        try {
            MagangHubWidget().updateAll(appContext)
            Log.d(TAG, "MagangHub widget successfully updated via Glance")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update MagangHub widget via Glance", e)
        }

        // 2. Explicit AppWidgetManager broadcast to force home screen launcher host redraw
        try {
            val appWidgetManager = AppWidgetManager.getInstance(appContext)
            val componentName = ComponentName(appContext, AttendanceWidgetReceiver::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    component = componentName
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                appContext.sendBroadcast(intent)
                Log.d(TAG, "Dispatched ACTION_APPWIDGET_UPDATE broadcast to ${appWidgetIds.size} widgets")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dispatch explicit update broadcast", e)
        }
    }
}
