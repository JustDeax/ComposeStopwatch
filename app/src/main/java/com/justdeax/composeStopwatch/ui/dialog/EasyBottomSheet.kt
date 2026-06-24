package com.justdeax.composeStopwatch.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasyBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    show: Boolean,
    onDismissRequest: () -> Unit,
    sheetContent: @Composable () -> Unit
) {
    if (show) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            modifier = modifier
        ) {
            val scrollState = rememberScrollState()
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(12.dp, 0.dp)
            ) {
                sheetContent()
            }
        }
    }
}