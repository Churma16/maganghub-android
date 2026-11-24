# Proguard rules for MagangHub Android
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class id.codes.maganghub.data.model.** { *; }
