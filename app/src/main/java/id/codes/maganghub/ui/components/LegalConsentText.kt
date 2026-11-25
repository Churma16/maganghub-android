package id.codes.maganghub.ui.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import id.codes.maganghub.ui.theme.KemnakerSky

/**
 * Reusable legal consent disclaimer text linking to Terms and Privacy Policy.
 * Triggers in-app bottom sheet callbacks without opening an external browser.
 */
@Composable
fun LegalConsentText(
    modifier: Modifier = Modifier,
    onOpenTerms: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {}
) {
    val linkColor = KemnakerSky

    val annotatedText = buildAnnotatedString {
        append("Dengan masuk, kamu menyetujui ")
        pushStringAnnotation(tag = "TERMS", annotation = "terms")
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
        pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
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

    @Suppress("DEPRECATION")
    ClickableText(
        text = annotatedText,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                .firstOrNull()?.let {
                    onOpenTerms()
                }
            annotatedText.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                .firstOrNull()?.let {
                    onOpenPrivacyPolicy()
                }
        }
    )
}
