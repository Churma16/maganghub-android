package id.codes.maganghub.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import id.codes.maganghub.background.NotificationHelper
import id.codes.maganghub.ui.theme.KemnakerSky

/**
 * Reusable legal consent disclaimer text linking to Terms and Privacy Policy.
 * Conforms to the visual specification in the login mockup.
 */
@Composable
fun LegalConsentText(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val linkColor = KemnakerSky

    val annotatedText = buildAnnotatedString {
        append("Dengan masuk, kamu menyetujui ")
        pushStringAnnotation(tag = "TERMS", annotation = NotificationHelper.URL_TERMS)
        withStyle(
            style = SpanStyle(
                color = linkColor,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Syarat dan Ketentuan")
        }
        pop()
        append(" serta ")
        pushStringAnnotation(tag = "PRIVACY", annotation = NotificationHelper.URL_PRIVACY_POLICY)
        withStyle(
            style = SpanStyle(
                color = linkColor,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Kebijakan Privasi")
        }
        pop()
        append(".")
    }

    ClickableText(
        text = annotatedText,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            annotatedText.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
        }
    )
}
