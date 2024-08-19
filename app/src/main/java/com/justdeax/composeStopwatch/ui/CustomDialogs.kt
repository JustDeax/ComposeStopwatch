package com.justdeax.composeStopwatch.ui
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties

@Composable
fun RadioDialog(
    title: String,
    desc: String,
    isPortrait: Boolean,
    defaultIndex: Int,
    setSelectedIndex: (Int) -> Unit,
    options: Array<String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }

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
            val (currentOption, onOptionSelected) = remember {
                mutableStateOf(options[defaultIndex])
            }

            LaunchedEffect(Unit) {
                selectedIndex = defaultIndex
            }

            LazyColumn(Modifier.selectableGroup()) {
                item {
                    Text(
                        modifier = Modifier.padding(bottom = 4.dp),
                        text = desc,
                        fontSize = 14.sp
                    )
                }
                itemsIndexed(options) { index, text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 4.dp)
                            .selectable(
                                selected = (text == currentOption),
                                onClick = {
                                    onOptionSelected(text)
                                    selectedIndex = index
                                },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            modifier = Modifier.padding(16.dp, 0.dp),
                            selected = (text == currentOption),
                            onClick = null
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button({
                onConfirm()
                setSelectedIndex(selectedIndex)
            }) {
                Text(confirmText)
            }
        },
        onDismissRequest = onDismiss
    )
}

@Composable
fun SimpleDialog(
    title: String,
    desc: String,
    isPortrait: Boolean,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    onDismiss: () -> Unit
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
                    .padding(16.dp)
            ) {
                Text(desc)
            }
               },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        onDismissRequest = onDismiss
    )
}

@Composable
fun OkayDialog(
    title: String,
    desc: String,
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
                Text(
                    text = desc,
                    style = MaterialTheme.typography.titleMedium
                )
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