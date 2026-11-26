# Proguard rules for MagangHub Android

# Preserve line numbers and source file attributes for actionable crash stacktraces
-keepattributes SourceFile,LineNumberTable

# Gson serialization / deserialization protection
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class id.codes.maganghub.data.model.** { *; }

# WorkManager Worker instantiation via reflection
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Jetpack Glance AppWidget receiver protection
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver

# OkHttp & Okio warnings suppression
-dontwarn okhttp3.**
-dontwarn okio.**
