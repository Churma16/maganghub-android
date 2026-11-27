package id.codes.maganghub.widget

import android.content.Context
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
        try {
            val appContext = context.applicationContext ?: context
            MagangHubWidget().updateAll(appContext)
            Log.d(TAG, "MagangHub widget successfully updated")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update MagangHub widget", e)
        }
    }
}
