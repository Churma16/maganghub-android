# Proguard rules for MagangHub Android

# Preserve line numbers and source file attributes for actionable crash stacktraces
-keepattributes SourceFile,LineNumberTable

# Gson serialization / deserialization protection
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclasseswithmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    <init>(...);
}
-keep class id.churma.maganghubcompanion.data.model.** { *; }
-keep class id.churma.maganghubcompanion.data.api.AttendanceItem { *; }
-keep class id.churma.maganghubcompanion.data.api.AttendancesResponse { *; }
-keep class id.churma.maganghubcompanion.data.api.GithubRelease { *; }
-keep class id.churma.maganghubcompanion.data.api.GithubAsset { *; }

# WorkManager Worker instantiation via reflection
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Jetpack Glance AppWidget receiver protection
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver

# Suppress compile-time annotations from Google Tink / security-crypto
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**

# OkHttp & Okio warnings suppression
-dontwarn okhttp3.**
-dontwarn okio.**
