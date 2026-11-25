package id.codes.maganghub.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Brand: Kemnaker Sky Blue
val KemnakerSky = Color(0xFF0284C7)
val KemnakerSkyContainer = Color(0xFFE0F2FE)
val KemnakerSkyOnContainer = Color(0xFF0369A1)

// Neutral & Slate Palette
val Slate900 = Color(0xFF0F172A)
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)
val PureWhite = Color(0xFFFFFFFF)
val BorderSlate = Color(0xFFD1D5DB) // Upgraded to Gray-300 / Fluent Stroke 1 for clear card separation
val TeamsCanvasLight = Color(0xFFF3F4F6) // Microsoft Teams & Fluent Canvas (Anti-Glare Gray 96)

// Success / Hadir: Deep Emerald Green (Button), Mint Container (Badge/Circle), Text
val HadirGreen = Color(0xFF00875A)          // Button Deep Emerald Green
val HadirGreenDark = Color(0xFF166534)      // Text Deep Green
val HadirGreenLight = Color(0xFFDCFCE7)     // Mint Container (Badge/Circle)
val HadirGreenText = Color(0xFF166534)      // Badge Text

// Warning / Belum Hadir: Amber
val WarningAmber = Color(0xFFD97706)
val WarningAmberContainer = Color(0xFFFEF3C7)
val WarningAmberText = Color(0xFF92400E)

val BelumHadirAmber = WarningAmber
val BelumHadirAmberContainer = WarningAmberContainer
val BelumHadirAmberText = WarningAmberText

// Danger / Error: Red
val DangerRed = Color(0xFFDC2626)
val DangerRedContainer = Color(0xFFFEE2E2)
val DangerRedText = Color(0xFF991B1B)

val BelumHadirRed = DangerRed
val BelumHadirRedContainer = DangerRedContainer
val BelumHadirRedText = DangerRedText

// Purpose-First UI Hero Status Card Colors - Dark Mode
val HeroIncompleteContainer = Color(0xFF38141E)
val HeroIncompleteBorder = Color(0xFF5C1D2E)
val HeroIncompleteBadge = Color(0xFFE11D48)

val HeroCompleteContainer = Color(0xFF0F2E22)
val HeroCompleteBorder = Color(0xFF16593F)
val HeroCompleteBadge = Color(0xFF059669)

// Purpose-First UI Hero Status Card Colors - Light Mode (Harmonized, Anti-Glare)
val HeroIncompleteContainerLight = Color(0xFFFFF1F2) // Soft Rose 50
val HeroIncompleteBorderLight = Color(0xFFFECDD3)    // Rose 200
val HeroIncompleteTextLight = Color(0xFF9F1239)      // Rose 800
val HeroIncompleteTitleLight = Color(0xFF881337)     // Rose 900

val HeroCompleteContainerLight = Color(0xFFECFDF5)   // Soft Emerald 50
val HeroCompleteBorderLight = Color(0xFFA7F3D0)      // Emerald 200
val HeroCompleteTextLight = Color(0xFF065F46)        // Emerald 800
val HeroCompleteTitleLight = Color(0xFF064E3B)       // Emerald 900

// Info Box: Sky
val InfoSky = KemnakerSky                    // 0xFF0284C7
val InfoSkyContainer = Color(0xFFF0F9FF)
val InfoSkyBorder = Color(0xFFBAE6FD)
val InfoSkyText = Color(0xFF0369A1)

// Hari Libur / Off-day: Sky
val LiburSky = KemnakerSky
val LiburSkyContainer = KemnakerSkyContainer
val LiburSkyText = KemnakerSkyOnContainer

// Neutral Card & Surface Borders
val CardBorderLight = BorderSlate            // 0xFFE2E8F0
val CardBorderDark = Color(0xFF334155)

// Light Theme Palette
val PrimaryLight = KemnakerSky               // 0xFF0284C7
val OnPrimaryLight = PureWhite               // 0xFFFFFFFF
val PrimaryContainerLight = KemnakerSkyContainer // 0xFFE0F2FE
val OnPrimaryContainerLight = KemnakerSkyOnContainer // 0xFF0369A1

val SecondaryLight = Slate600                // 0xFF475569
val OnSecondaryLight = PureWhite
val SecondaryContainerLight = Slate100       // 0xFFF1F5F9
val OnSecondaryContainerLight = Slate900     // 0xFF0F172A

val BackgroundLight = TeamsCanvasLight       // Microsoft Teams anti-glare canvas: 0xFFF3F4F6
val OnBackgroundLight = Slate900             // Primary Slate-900: 0xFF0F172A
val SurfaceLight = PureWhite                 // Pure White: 0xFFFFFFFF
val OnSurfaceLight = Slate900                // Primary Slate-900: 0xFF0F172A
val SurfaceVariantLight = Color(0xFFE5E7EB)  // Soft Gray-200: 0xFFE5E7EB
val OnSurfaceVariantLight = Slate600         // Secondary Slate-600: 0xFF475569
val OutlineLight = BorderSlate               // Outline/Border: 0xFFD1D5DB

// Dark Theme Palette
val PrimaryDark = Color(0xFF38BDF8)          // Sky-400
val OnPrimaryDark = Color(0xFF082F49)
val PrimaryContainerDark = Color(0xFF075985)
val OnPrimaryContainerDark = KemnakerSkyContainer

val SecondaryDark = Slate400                 // 0xFF94A3B8
val OnSecondaryDark = Slate900               // 0xFF0F172A
val SecondaryContainerDark = Color(0xFF1E293B)
val OnSecondaryContainerDark = Slate100

val BackgroundDark = Color(0xFF0F172A)       // Slate-900
val OnBackgroundDark = Slate50               // 0xFFF8FAFC
val SurfaceDark = Color(0xFF1E293B)          // Slate-800
val OnSurfaceDark = Slate50                  // 0xFFF8FAFC
val SurfaceVariantDark = Color(0xFF334155)
val OnSurfaceVariantDark = Slate400          // 0xFF94A3B8
val OutlineDark = Color(0xFF475569)
