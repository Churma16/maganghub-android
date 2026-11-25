package id.codes.maganghub.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity as actionStartActivityIntent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import id.codes.maganghub.R
import id.codes.maganghub.background.NotificationHelper
import id.codes.maganghub.ui.MainActivity

class MagangHubWidget : GlanceAppWidget() {

    companion object {
        private val SIZE_1X3 = DpSize(180.dp, 56.dp)
        private val SIZE_1X5 = DpSize(320.dp, 56.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SIZE_1X3, SIZE_1X5))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetDataSnapshot.fromPreferences(context)
        val colors = WidgetColors.getTokens(snapshot.status, snapshot.themeMode, context)

        val monevIntent = Intent(Intent.ACTION_VIEW, Uri.parse(NotificationHelper.URL_MONEV_DASHBOARD)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val openMonevAction = actionStartActivityIntent(monevIntent)
        val openAppAction = actionStartActivity<MainActivity>()

        // When SESI_BERAKHIR, tapping quick action should open the app to login to SSO, not the web browser
        val actionButtonAction = if (snapshot.status == WidgetStatus.SESI_BERAKHIR) {
            openAppAction
        } else {
            openMonevAction
        }

        provideContent {
            val currentSize = LocalSize.current
            val isFullWidth = currentSize.width >= 250.dp

            // Outer container: Card border + background + 22dp corner radius
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(colors.cardBorder))
                    .cornerRadius(22.dp)
                    .padding(1.dp)
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(colors.cardBackground))
                        .cornerRadius(21.dp)
                        .clickable(openAppAction)
                ) {
                    if (isFullWidth) {
                        WidgetLayout1x5(snapshot, colors, actionButtonAction)
                    } else {
                        WidgetLayout1x3(snapshot, colors, actionButtonAction)
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun WidgetLayout1x5(
    data: WidgetDataSnapshot,
    colors: WidgetColorTokens,
    actionButtonAction: Action
) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Zona Kiri: Garis aksen vertikal tipis (lebar 4dp, tinggi 32dp, pill)
        Box(
            modifier = GlanceModifier
                .width(4.dp)
                .height(32.dp)
                .background(ColorProvider(colors.accentBar))
                .cornerRadius(2.dp)
        ) {}

        Spacer(modifier = GlanceModifier.width(10.dp))

        // 2. Zona Tengah: Judul & Subteks kontekstual
        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val iconRes = data.getInlineIconResId()
                if (iconRes != null) {
                    Image(
                        provider = ImageProvider(iconRes),
                        contentDescription = null,
                        modifier = GlanceModifier.size(14.dp),
                        colorFilter = ColorFilter.tint(ColorProvider(colors.inlineIcon))
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                }
                Text(
                    text = data.getTitle1x5(),
                    style = TextStyle(
                        color = ColorProvider(colors.titleText),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = GlanceModifier.height(3.dp))

            val subtextColor = if (data.isSubtextWarning()) colors.subtextWarning else colors.subtextNormal
            Text(
                text = data.getSubtext1x5(),
                style = TextStyle(
                    color = ColorProvider(subtextColor),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.width(8.dp))

        // 3. Zona Kanan: Dinamis sesuai Status
        when {
            data.showActionButton() -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (data.status == WidgetStatus.BELUM_ABSEN || data.status == WidgetStatus.MENDESAK) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = data.remainingTimeText,
                                style = TextStyle(
                                    color = ColorProvider(colors.rightValue),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "tersisa",
                                style = TextStyle(
                                    color = ColorProvider(colors.rightLabel),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Spacer(modifier = GlanceModifier.width(6.dp))
                    }

                    // Safe touch target container >= 48x48 dp with inner 36x36 dp squircle
                    Box(
                        modifier = GlanceModifier
                            .size(48.dp)
                            .clickable(actionButtonAction),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .size(36.dp)
                                .background(ColorProvider(colors.actionButtonBg))
                                .cornerRadius(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_widget_arrow_outward),
                                contentDescription = "Aksi Cepat",
                                modifier = GlanceModifier.size(18.dp),
                                colorFilter = ColorFilter.tint(ColorProvider(colors.actionButtonIcon))
                            )
                        }
                    }
                }
            }

            data.showBadge1x5() -> {
                Box(
                    modifier = GlanceModifier
                        .background(ColorProvider(colors.badgeBorder))
                        .cornerRadius(8.dp)
                        .padding(1.dp)
                ) {
                    Box(
                        modifier = GlanceModifier
                            .background(ColorProvider(colors.badgeBg))
                            .cornerRadius(7.dp)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = data.getBadgeText1x5(),
                            style = TextStyle(
                                color = ColorProvider(colors.badgeText),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            else -> {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = data.formattedDate,
                        style = TextStyle(
                            color = ColorProvider(colors.rightValue),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    val label = if (data.status == WidgetStatus.HARI_LIBUR) "Hari Off" else "Lengkap"
                    Text(
                        text = label,
                        style = TextStyle(
                            color = ColorProvider(colors.rightLabel),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun WidgetLayout1x3(
    data: WidgetDataSnapshot,
    colors: WidgetColorTokens,
    actionButtonAction: Action
) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Zona Kiri: Garis aksen vertikal tipis (lebar 4dp, tinggi 32dp, pill)
        Box(
            modifier = GlanceModifier
                .width(4.dp)
                .height(32.dp)
                .background(ColorProvider(colors.accentBar))
                .cornerRadius(2.dp)
        ) {}

        Spacer(modifier = GlanceModifier.width(8.dp))

        // 2. Zona Tengah: Judul & Subteks ringkas (tanpa inline icon untuk hemat ruang)
        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = data.getTitle1x3(),
                style = TextStyle(
                    color = ColorProvider(colors.titleText),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(3.dp))

            val subtextColor = if (data.isSubtextWarning()) colors.subtextWarning else colors.subtextNormal
            Text(
                text = data.getSubtext1x3(),
                style = TextStyle(
                    color = ColorProvider(subtextColor),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }

        // 3. Zona Kanan: Tombol aksi jika diperlukan
        if (data.showActionButton()) {
            Spacer(modifier = GlanceModifier.width(4.dp))
            // Safe touch target container >= 48x48 dp with inner 32x32 dp squircle
            Box(
                modifier = GlanceModifier
                    .size(48.dp)
                    .clickable(actionButtonAction),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(32.dp)
                        .background(ColorProvider(colors.actionButtonBg))
                        .cornerRadius(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_arrow_outward),
                        contentDescription = "Aksi Cepat",
                        modifier = GlanceModifier.size(16.dp),
                        colorFilter = ColorFilter.tint(ColorProvider(colors.actionButtonIcon))
                    )
                }
            }
        }
    }
}
