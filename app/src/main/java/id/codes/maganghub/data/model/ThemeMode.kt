package id.codes.maganghub.data.model

/**
 * Theme display mode selection for MagangHub.
 */
enum class ThemeMode(val value: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromValue(value: String?): ThemeMode =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: SYSTEM
    }
}
