package com.justdeax.composeStopwatch.ui
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.justdeax.composeStopwatch.R

@SuppressLint("BatteryLife")
@Composable
fun BatteryOptimizationCheck() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = context.packageName
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) showDialog = true
    }

    if (showDialog) {
        SimpleDialog(
            context.getString(R.string.ignore_battery),
            context.getString(R.string.ignore_battery_desc),
            context.getString(R.string.grant), {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
                showDialog = false
            },
            context.getString(R.string.cancel), { showDialog = false}
        )
    }
}