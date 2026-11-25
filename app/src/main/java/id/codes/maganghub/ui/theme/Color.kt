package id.codes.maganghub.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// 1. Dasar Layar (Theme Base Palette)
// ==========================================
val BackgroundDark = Color(0xFF0B1220)       // Background layar gelap
val BackgroundLight = Color(0xFFF4F7FB)      // Background layar terang

val SurfaceDark = Color(0xFF162033)         // Card / nav bawah gelap
val SurfaceLight = Color(0xFFFFFFFF)        // Card / nav bawah terang

val OutlineDark = Color(0xFF263450)         // Border card, garis pemisah gelap
val OutlineLight = Color(0xFFD5DDEA)        // Border card, garis pemisah terang

val TextPrimaryDark = Color(0xFFF1F5F9)     // Teks utama gelap
val TextPrimaryLight = Color(0xFF0F172A)    // Teks utama terang

val TextSecondaryDark = Color(0xFFA8B3C7)   // Teks sekunder gelap
val TextSecondaryLight = Color(0xFF475569)  // Teks sekunder terang

// Link, ikon aksen, cincin hari ini
val AccentSkyDark = Color(0xFF38BDF8)
val AccentSkyLight = Color(0xFF0369A1)

// Border dan ikon tombol outline
val OutlineButtonBorderDark = Color(0xFF3B4B6B)
val OutlineButtonBorderLight = Color(0xFF94A3B8)
val OutlineButtonIconDark = Color(0xFFE2E8F0)
val OutlineButtonIconLight = Color(0xFF334155)

// Background segmented control
val SegmentedTrackDark = Color(0xFF0B1220)
val SegmentedTrackLight = Color(0xFFEEF2F7)

// ==========================================
// 2. Kartu status "Belum lengkap" (Merah)
// ==========================================
val HeroIncompleteBgDark = Color(0xFF3A1620)
val HeroIncompleteBorderDark = Color(0xFF7F2A3B)
val HeroIncompleteDateDark = Color(0xFFFDA4AF)
val HeroIncompleteIconDark = Color(0xFFBE123C)
val HeroIncompleteDescDark = Color(0xFFE2E8F0)
val HeroIncompleteTitleDark = Color(0xFFFFFFFF)

val HeroIncompleteBgLight = Color(0xFFFFE4E6)
val HeroIncompleteBorderLight = Color(0xFFFDA4AF)
val HeroIncompleteDateLight = Color(0xFF9F1239)
val HeroIncompleteIconLight = Color(0xFFBE123C)
val HeroIncompleteDescLight = Color(0xFF1E293B)
val HeroIncompleteTitleLight = Color(0xFF9F1239)

// Backwards compatibility aliases
val HeroIncompleteContainer = HeroIncompleteBgDark
val HeroIncompleteBorder = HeroIncompleteBorderDark
val HeroIncompleteBadge = HeroIncompleteIconDark
val HeroIncompleteContainerLight = HeroIncompleteBgLight
val HeroIncompleteTextLight = HeroIncompleteDescLight

// ==========================================
// 3. Kartu status "Sudah lengkap" (Hijau)
// ==========================================
val HeroCompleteBgDark = Color(0xFF0F2E22)
val HeroCompleteBorderDark = Color(0xFF1F6B4A)
val HeroCompleteDateDark = Color(0xFF86EFAC)
val HeroCompleteIconDark = Color(0xFF15803D)
val HeroCompleteDescDark = Color(0xFFE2E8F0)
val HeroCompleteTitleDark = Color(0xFFFFFFFF)

val HeroCompleteBgLight = Color(0xFFDCFCE7)
val HeroCompleteBorderLight = Color(0xFF86EFAC)
val HeroCompleteDateLight = Color(0xFF166534)
val HeroCompleteIconLight = Color(0xFF15803D)
val HeroCompleteDescLight = Color(0xFF166534)
val HeroCompleteTitleLight = Color(0xFF14532D)

// Backwards compatibility aliases
val HeroCompleteContainer = HeroCompleteBgDark
val HeroCompleteBorder = HeroCompleteBorderDark
val HeroCompleteBadge = HeroCompleteIconDark
val HeroCompleteContainerLight = HeroCompleteBgLight
val HeroCompleteTextLight = HeroCompleteDescLight

// ==========================================
// 4. Panel sisa waktu (di dalam kartu status)
// ==========================================
val MetricPanelDark = Color(0x47000000)      // rgba(0,0,0,0.28)
val MetricLabelDark = Color(0xFFCBD5E1)

val MetricPanelLight = Color(0xBFFFFFFF)     // rgba(255,255,255,0.75)
val MetricLabelLight = Color(0xFF475569)

// ==========================================
// 5. Badge, ikon, dan riwayat 7 hari
// ==========================================
val StatusSuccessBgDark = Color(0xFF14532D)
val StatusSuccessTextDark = Color(0xFFBBF7D0)
val StatusSuccessIconDark = Color(0xFF86EFAC)

val StatusSuccessBgLight = Color(0xFFDCFCE7)
val StatusSuccessTextLight = Color(0xFF166534)
val StatusSuccessIconLight = Color(0xFF166534)

val StatusInfoBgDark = Color(0xFF0C4A6E)
val StatusInfoTextDark = Color(0xFFBAE6FD)
val StatusInfoIconDark = Color(0xFFBAE6FD)

val StatusInfoBgLight = Color(0xFFE0F2FE)
val StatusInfoTextLight = Color(0xFF075985)
val StatusInfoIconLight = Color(0xFF075985)

val StatusHolidayBorderDark = Color(0xFF52627F)
val StatusHolidayIconDark = Color(0xFFA8B3C7)

val StatusHolidayBorderLight = Color(0xFF94A3B8)
val StatusHolidayIconLight = Color(0xFF64748B)

// ==========================================
// 6. Chip, toggle, dan bahaya
// ==========================================
val ChipUnselectedTextDark = Color(0xFFE2E8F0)
val ChipUnselectedBorderDark = Color(0xFF3B4B6B)

val ChipUnselectedTextLight = Color(0xFF1E293B)
val ChipUnselectedBorderLight = Color(0xFF94A3B8)

val ToggleOffDark = Color(0xFF3B4B6B)
val ToggleOffLight = Color(0xFF64748B)

val LogoutConfirmBgDark = Color(0xFF3A1620)
val LogoutConfirmBorderDark = Color(0xFF7F2A3B)
val LogoutBtnTextDark = Color(0xFFFDA4AF)
val LogoutBtnBorderDark = Color(0xFFBE3A55)

val LogoutConfirmBgLight = Color(0xFFFFE4E6)
val LogoutConfirmBorderLight = Color(0xFFFDA4AF)
val LogoutBtnTextLight = Color(0xFF9F1239)
val LogoutBtnBorderLight = Color(0xFFBE123C)

// ==========================================
// 7. Sama di kedua tema
// ==========================================
val PrimaryCta = Color(0xFF0369A1)          // Tombol utama (CTA) dan toggle hidup
val OnPrimaryCta = Color(0xFFFFFFFF)        // Teks di atas tombol utama

val ChipSelectedBg = Color(0xFFDBEAFE)      // Chip terpilih, background
val ChipSelectedText = Color(0xFF1E3A8A)    // Chip terpilih, teks

val NavActivePillBg = Color(0xFFDBEAFE)     // Pil tab nav aktif, background
val NavActivePillIcon = Color(0xFF1D4ED8)   // Pil tab nav aktif, ikon

val DangerConfirmBtn = Color(0xFFBE123C)    // Tombol "Ya, keluar"
val ToggleKnob = Color(0xFFFFFFFF)          // Knob toggle

// Legacy brand aliases
val KemnakerSky = PrimaryCta
val KemnakerSkyContainer = NavActivePillBg
val KemnakerSkyOnContainer = NavActivePillIcon

val HadirGreen = Color(0xFF15803D)
val HadirGreenDark = Color(0xFF166534)
val HadirGreenLight = StatusSuccessBgLight
val HadirGreenText = StatusSuccessTextLight

val DangerRed = Color(0xFFBE123C)
val DangerRedContainer = Color(0xFFFFE4E6)
val DangerRedText = Color(0xFF9F1239)

val PureWhite = Color(0xFFFFFFFF)
val Slate900 = TextPrimaryLight
val Slate600 = TextSecondaryLight
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)
val BorderSlate = OutlineLight

val WarningAmber = Color(0xFFD97706)
val WarningAmberContainer = Color(0xFFFEF3C7)
val WarningAmberText = Color(0xFF92400E)

// M3 ColorScheme assignments
val PrimaryLight = AccentSkyLight
val OnPrimaryLight = PureWhite
val PrimaryContainerLight = NavActivePillBg
val OnPrimaryContainerLight = NavActivePillIcon

val SecondaryLight = TextSecondaryLight
val OnSecondaryLight = PureWhite
val SecondaryContainerLight = Slate100
val OnSecondaryContainerLight = TextPrimaryLight

val OnBackgroundLight = TextPrimaryLight
val OnSurfaceLight = TextPrimaryLight
val SurfaceVariantLight = SegmentedTrackLight
val OnSurfaceVariantLight = TextSecondaryLight

val PrimaryDark = AccentSkyDark
val OnPrimaryDark = Color(0xFF082F49)
val PrimaryContainerDark = Color(0xFF0C4A6E)
val OnPrimaryContainerDark = NavActivePillBg

val SecondaryDark = TextSecondaryDark
val OnSecondaryDark = TextPrimaryLight
val SecondaryContainerDark = Color(0xFF162033)
val OnSecondaryContainerDark = TextPrimaryDark

val OnBackgroundDark = TextPrimaryDark
val OnSurfaceDark = TextPrimaryDark
val SurfaceVariantDark = Color(0xFF263450)
val OnSurfaceVariantDark = TextSecondaryDark
