package com.justdeax.composeStopwatch.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justdeax.composeStopwatch.R
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AutoStartDialog(
    isPortrait: Boolean,
    onDismiss: () -> Unit,
    startStopwatch: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(3) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            kotlinx.coroutines.delay(1000L.milliseconds)
            timeLeft--
        } else if (!isFinished) {
            isFinished = true
            startStopwatch()
            onDismiss()
        }
    }

    BaseDialog(
        isPortrait = isPortrait,
        title = { Text(stringResource(R.string.auto_start_sw_title)) },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(stringResource(R.string.auto_start_sw_desc_1))
                Text(
                    text = timeLeft.toString(),
                    fontSize = 90.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.auto_start_sw_desc_2))
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(60.dp)
            ) {
                Text(text = stringResource(R.string.cancel), fontSize = 20.sp)
            }
        },
        onDismissRequest = onDismiss
    )
}