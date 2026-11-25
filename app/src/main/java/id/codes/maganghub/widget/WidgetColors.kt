package id.codes.maganghub.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import id.codes.maganghub.data.model.ThemeMode

data class WidgetColorTokens(
    val cardBackground: Color,
    val cardBorder: Color,
    val accentBar: Color,
    val inlineIcon: Color,
    val titleText: Color,
    val subtextNormal: Color,
    val subtextWarning: Color,
    val rightValue: Color,
    val rightLabel: Color,
    val actionButtonBg: Color,
    val actionButtonIcon: Color,
    val badgeBg: Color,
    val badgeBorder: Color,
    val badgeText: Color,
    val footerDivider: Color,
    val footerText: Color,
)

object WidgetColors {

    fun getTokens(status: WidgetStatus, themeMode: ThemeMode, context: Context? = null): WidgetColorTokens {
        val isDark = when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> {
                if (context != null) {
                    val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    nightMode == Configuration.UI_MODE_NIGHT_YES
                } else {
                    true
                }
            }
        }

        return if (isDark) getDarkTokens(status) else getLightTokens(status)
    }

    fun getDarkTokens(status: WidgetStatus): WidgetColorTokens {
        return when (status) {
            WidgetStatus.SUDAH_ABSEN -> WidgetColorTokens(
                cardBackground = Color(0xFF0D2818),
                cardBorder = Color(0xFF064E3B),
                accentBar = Color(0xFF10B981),
                inlineIcon = Color(0xFF34D399),
                titleText = Color(0xFFFFFFFF),
                subtextNormal = Color(0xFFD1FAE5).copy(alpha = 0.75f),
                subtextWarning = Color(0xFFFCD34D),
                rightValue = Color(0xFF6EE7B7),
                rightLabel = Color(0xFFA7F3D0).copy(alpha = 0.60f),
                actionButtonBg = Color.Transparent,
                actionButtonIcon = Color.Transparent,
                badgeBg = Color(0x26F59E0B),
                badgeBorder = Color(0x4DFBBF24),
                badgeText = Color(0xFFFCD34D),
                footerDivider = Color(0xFF064E3B).copy(alpha = 0.6f),
                footerText = Color(0xFFA7F3D0).copy(alpha = 0.65f)
            )
            WidgetStatus.BELUM_ABSEN -> WidgetColorTokens(
                cardBackground = Color(0xFF251A0A),
                cardBorder = Color(0xFF78350F),
                accentBar = Color(0xFFF59E0B),
                inlineIcon = Color(0xFFFBBF24),
                titleText = Color(0xFFFFFFFF),
                subtextNormal = Color(0xFFFEF3C7).copy(alpha = 0.70f),
                subtextWarning = Color(0xFFFCD34D),
                rightValue = Color(0xFFFCD34D),
                rightLabel = Color(0x99FEF3C7),
                actionButtonBg = Color(0xFFFBBF24),
                actionButtonIcon = Color(0xFF020617),
                badgeBg = Color.Transparent,
                badgeBorder = Color.Transparent,
                badgeText = Color.Transparent,
                footerDivider = Color(0xFF78350F).copy(alpha = 0.6f),
                footerText = Color(0xFFFEF3C7).copy(alpha = 0.65f)
            )
            WidgetStatus.MENDESAK -> WidgetColorTokens(
                cardBackground = Color(0xFF2A0E14),
                cardBorder = Color(0xFF881337),
                accentBar = Color(0xFFF43F5E),
                inlineIcon = Color(0xFFFB7185),
                titleText = Color(0xFFFFFFFF),
                subtextNormal = Color(0xFFFFE4E6).copy(alpha = 0.75f),
                subtextWarning = Color(0xFFFCD34D),
                rightValue = Color(0xFFFDA4AF),
                rightLabel = Color(0x99FFE4E6),
                actionButtonBg = Color(0xFFF43F5E),
                actionButtonIcon = Color(0xFFFFFFFF),
                badgeBg = Color.Transparent,
                badgeBorder = Color.Transparent,
                badgeText = Color.Transparent,
                footerDivider = Color(0xFF881337).copy(alpha = 0.6f),
                footerText = Color(0xFFFFE4E6).copy(alpha = 0.65f)
            )
            WidgetStatus.HARI_LIBUR -> WidgetColorTokens(
                cardBackground = Color(0xFF131B2E),
                cardBorder = Color(0xFF1E293B),
                accentBar = Color(0xFF6366F1),
                inlineIcon = Color(0xFFA5B4FC),
                titleText = Color(0xFFF1F5F9),
                subtextNormal = Color(0xFF94A3B8),
                subtextWarning = Color(0xFFFCD34D),
                rightValue = Color(0xFFA5B4FC),
                rightLabel = Color(0xFF94A3B8),
                actionButtonBg = Color.Transparent,
                actionButtonIcon = Color.Transparent,
                badgeBg = Color(0x26F59E0B),
                badgeBorder = Color(0x4DFBBF24),
                badgeText = Color(0xFFFCD34D),
                footerDivider = Color(0xFF1E293B).copy(alpha = 0.6f),
                footerText = Color(0xFF94A3B8)
            )
            WidgetStatus.SESI_BERAKHIR -> WidgetColorTokens(
                cardBackground = Color(0xFF1E293B),
                cardBorder = Color(0xFF334155),
                accentBar = Color(0xFF64748B),
                inlineIcon = Color(0xFF94A3B8),
                titleText = Color(0xFFF1F5F9),
                subtextNormal = Color(0xFF94A3B8),
                subtextWarning = Color(0xFF94A3B8),
                rightValue = Color(0xFF94A3B8),
                rightLabel = Color(0xFF94A3B8),
                actionButtonBg = Color(0xFF334155),
                actionButtonIcon = Color(0xFFFFFFFF),
                badgeBg = Color.Transparent,
                badgeBorder = Color.Transparent,
                badgeText = Color.Transparent,
                footerDivider = Color(0xFF334155).copy(alpha = 0.6f),
                footerText = Color(0xFF94A3B8)
            )
        }
    }

    fun getLightTokens(status: WidgetStatus): WidgetColorTokens {
        return when (status) {
            WidgetStatus.SUDAH_ABSEN -> WidgetColorTokens(
                cardBackground = Color(0xFFECFDF5),
                cardBorder = Color(0xFFA7F3D0),
                accentBar = Color(0xFF10B981),
                inlineIcon = Color(0xFF059669),
                titleText = Color(0xFF022C22),
                subtextNormal = Color(0xFF065F46).copy(alpha = 0.80f),
                subtextWarning = Color(0xFFB45309),
                rightValue = Color(0xFF047857),
                rightLabel = Color(0xFF047857).copy(alpha = 0.70f),
                actionButtonBg = Color.Transparent,
                actionButtonIcon = Color.Transparent,
                badgeBg = Color(0xFFFEF3C7),
                badgeBorder = Color(0xFFFCD34D),
                badgeText = Color(0xFF92400E),
                footerDivider = Color(0xFFA7F3D0).copy(alpha = 0.7f),
                footerText = Color(0xFF047857).copy(alpha = 0.70f)
            )
            WidgetStatus.BELUM_ABSEN -> WidgetColorTokens(
                cardBackground = Color(0xFFFFFBEB),
                cardBorder = Color(0xFFFDE68A),
                accentBar = Color(0xFFF59E0B),
                inlineIcon = Color(0xFFD97706),
                titleText = Color(0xFF451A03),
                subtextNormal = Color(0xFF92400E).copy(alpha = 0.80f),
                subtextWarning = Color(0xFFB45309),
                rightValue = Color(0xFFB45309),
                rightLabel = Color(0x9992400E),
                actionButtonBg = Color(0xFFF59E0B),
                actionButtonIcon = Color(0xFFFFFFFF),
                badgeBg = Color.Transparent,
                badgeBorder = Color.Transparent,
                badgeText = Color.Transparent,
                footerDivider = Color(0xFFFDE68A).copy(alpha = 0.7f),
                footerText = Color(0xFF92400E).copy(alpha = 0.70f)
            )
            WidgetStatus.MENDESAK -> WidgetColorTokens(
                cardBackground = Color(0xFFFFF1F2),
                cardBorder = Color(0xFFFECDD3),
                accentBar = Color(0xFFF43F5E),
                inlineIcon = Color(0xFFE11D48),
                titleText = Color(0xFF4C0519),
                subtextNormal = Color(0xFF9F1239).copy(alpha = 0.80f),
                subtextWarning = Color(0xFFB45309),
                rightValue = Color(0xFFBE123C),
                rightLabel = Color(0x999F1239),
                actionButtonBg = Color(0xFFE11D48),
                actionButtonIcon = Color(0xFFFFFFFF),
                badgeBg = Color.Transparent,
                badgeBorder = Color.Transparent,
                badgeText = Color.Transparent,
                footerDivider = Color(0xFFFECDD3).copy(alpha = 0.7f),
                footerText = Color(0xFF9F1239).copy(alpha = 0.70f)
            )
            WidgetStatus.HARI_LIBUR -> WidgetColorTokens(
                cardBackground = Color(0xFFF8FAFC),
                cardBorder = Color(0xFFCBD5E1),
                accentBar = Color(0xFF6366F1),
                inlineIcon = Color(0xFF4F46E5),
                titleText = Color(0xFF1E293B),
                subtextNormal = Color(0xFF64748B),
                subtextWarning = Color(0xFFB45309),
                rightValue = Color(0xFF4F46E5),
                rightLabel = Color(0xFF64748B),
                actionButtonBg = Color.Transparent,
                actionButtonIcon = Color.Transparent,
                badgeBg = Color(0xFFFEF3C7),
                badgeBorder = Color(0xFFFCD34D),
                badgeText = Color(0xFF92400E),
                footerDivider = Color(0xFFCBD5E1).copy(alpha = 0.7f),
                footerText = Color(0xFF64748B)
            )
            WidgetStatus.SESI_BERAKHIR -> WidgetColorTokens(
                cardBackground = Color(0xFFF8FAFC),
                cardBorder = Color(0xFFCBD5E1),
                accentBar = Color(0xFF64748B),
                inlineIcon = Color(0xFF64748B),
                titleText = Color(0xFF1E293B),
                subtextNormal = Color(0xFF64748B),
                subtextWarning = Color(0xFF64748B),
                rightValue = Color(0xFF64748B),
                rightLabel = Color(0xFF64748B),
                actionButtonBg = Color(0xFF475569),
                actionButtonIcon = Color(0xFFFFFFFF),
                badgeBg = Color.Transparent,
                badgeBorder = Color.Transparent,
                badgeText = Color.Transparent,
                footerDivider = Color(0xFFCBD5E1).copy(alpha = 0.7f),
                footerText = Color(0xFF64748B)
            )
        }
    }
}
