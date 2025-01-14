package com.pavlov.MyShadowGallery.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.theme.uiComponents.MyStyledDialogWithTitle

@Composable
fun LanguageSelectionDialog(onDismiss: () -> Unit, onLanguageSelected: (String) -> Unit) {
    val languageOptions = listOf("Русский", "English", "汉语", "Español")
    val languageCodes = listOf("ru", "en", "zh", "es")

    MyStyledDialogWithTitle(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.language_option),
                style = MaterialTheme.typography.h6,
            )
        },
        content = {
            Column {
                languageOptions.forEachIndexed { index, language ->
                    TextButton(onClick = {
                        onLanguageSelected(languageCodes[index])
                        onDismiss()
                    }) {
                        Text(text = language, style = MaterialTheme.typography.body1)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    )
}