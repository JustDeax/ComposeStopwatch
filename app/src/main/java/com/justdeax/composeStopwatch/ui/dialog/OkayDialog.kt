package com.justdeax.composeStopwatch.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OkayDialog(
    title: String,
    isPortrait: Boolean,
    confirmText: String,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    BaseDialog(
        isPortrait = isPortrait,
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