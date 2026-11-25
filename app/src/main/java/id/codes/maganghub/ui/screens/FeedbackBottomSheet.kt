package id.codes.maganghub.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.codes.maganghub.data.model.FeedbackType
import id.codes.maganghub.ui.theme.DangerRed
import id.codes.maganghub.ui.theme.KemnakerSky
import id.codes.maganghub.ui.theme.PureWhite

/**
 * Modal bottom sheet allowing users to submit bug reports, feature suggestions, or feedback.
 * Conforms to the visual specification in the Figma mockup.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackBottomSheet(
    isOpen: Boolean,
    isSubmitting: Boolean,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSubmit: (type: FeedbackType, message: String) -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedType by remember { mutableStateOf(FeedbackType.BUG) }
    var message by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    val sheetBg = if (isDark) Color(0xFF131D31) else MaterialTheme.colorScheme.surface
    val inputBg = if (isDark) Color(0xFF0B1220) else Color(0xFFF8FAFC)
    val inputBorder = if (isDark) Color(0xFF3B4B6B) else Color(0xFFCBD5E1)

    ModalBottomSheet(
        onDismissRequest = {
            if (!isSubmitting) {
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Kirim masukan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Bantu kami memperbaiki app ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Section: Jenis Masukan
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Jenis masukan",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeedbackType.entries.forEach { type ->
                        val selected = selectedType == type
                        val chipBg = if (selected) Color(0xFFDBEAFE) else (if (isDark) Color(0xFF0B1220) else Color(0xFFEEF2F7))
                        val chipText = if (selected) Color(0xFF1E3A8A) else (if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B))
                        val chipBorder = if (selected) null else BorderStroke(1.dp, if (isDark) Color(0xFF3B4B6B) else Color(0xFF94A3B8))

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = chipBg,
                            border = chipBorder,
                            onClick = { selectedType = type }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = chipText
                                )
                            }
                        }
                    }
                }
            }

            // Section: Ceritakan masukanmu
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ceritakan masukanmu",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = {
                        Text(
                            text = "Contoh: pengingat tidak muncul saat HP terkunci.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputBg,
                        unfocusedContainerColor = inputBg,
                        disabledContainerColor = inputBg,
                        focusedBorderColor = KemnakerSky,
                        unfocusedBorderColor = inputBorder,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 6
                )
            }

            // Info row: Versi app dan tipe HP ikut terkirim otomatis.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Versi app dan tipe HP ikut terkirim otomatis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Error message display if any
            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = DangerRed,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            // Actions row: [Batal] [Kirim]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF3B4B6B) else Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = "Batal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        if (message.isNotBlank() && !isSubmitting) {
                            onSubmit(selectedType, message)
                        }
                    },
                    enabled = message.isNotBlank() && !isSubmitting,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KemnakerSky,
                        contentColor = PureWhite,
                        disabledContainerColor = KemnakerSky.copy(alpha = 0.5f),
                        disabledContentColor = PureWhite.copy(alpha = 0.5f)
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = PureWhite
                        )
                    } else {
                        Text(
                            text = "Kirim",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
