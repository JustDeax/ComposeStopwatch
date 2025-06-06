package com.justdeax.composeStopwatch.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

@Composable
fun OkayDialog(
    title: String,
    content: @Composable () -> Unit,
    isPortrait: Boolean,
    confirmText: String,
    onConfirm: () -> Unit
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
        title = { Text(title) },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                content()
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        onDismissRequest = onConfirm
    )
}