package com.justdeax.composeStopwatch.ui.dialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.justdeax.composeStopwatch.R

@Composable
fun DisplayAutoStartDialog(
    isPortrait: Boolean,
    onDismiss: () -> Unit,
    startStopwatch: () -> Unit
) {
    val context = LocalContext.current
    var timeLeft by remember { mutableIntStateOf(3) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            kotlinx.coroutines.delay(1000L)
            timeLeft--
        } else if (!isFinished) {
            isFinished = true
            startStopwatch()
        }
    }

    AlertDialog(
        modifier = if (isPortrait)
            Modifier
        else
            Modifier.fillMaxWidth(0.6f),
        properties = if (isPortrait)
            DialogProperties()
        else
            DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(context.getString(R.string.auto_start_sw)) },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(context.getString(R.string.auto_start_sw_desc_1))
                Text(
                    text = timeLeft.toString(),
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(context.getString(R.string.auto_start_sw_desc_2))
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
                Text(text = context.getString(R.string.cancel), fontSize = 20.sp)
            }
        },
        onDismissRequest = onDismiss
    )
}