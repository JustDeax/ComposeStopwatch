package com.justdeax.composeStopwatch.ui.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

@Composable
fun BaseDialog(
    isPortrait: Boolean,
    title: @Composable (() -> Unit)?,
    text: @Composable (() -> Unit)?,
    confirmButton: @Composable (() -> Unit),
    dismissButton: @Composable (() -> Unit)? = null,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        modifier = if (isPortrait)
            Modifier
        else
            Modifier.fillMaxWidth(0.6f),
        properties = if (isPortrait)
            DialogProperties()
        else
            DialogProperties(usePlatformDefaultWidth = false),
        title = title,
        text = text,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        onDismissRequest = onDismissRequest
    )
}