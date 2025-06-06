package com.justdeax.composeStopwatch.ui

import android.os.Build
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justdeax.composeStopwatch.R
import com.justdeax.composeStopwatch.ui.dialog.OkayDialog
import com.justdeax.composeStopwatch.ui.dialog.RadioDialog
import com.justdeax.composeStopwatch.ui.dialog.SimpleDialog
import com.justdeax.composeStopwatch.ui.theme.DarkColorScheme
import kotlinx.coroutines.delay

@Composable
fun DisplayActions(
    modifier: Modifier,
    isPortrait: Boolean,
    isStarted: Boolean,
    show: Boolean,
    tapOnClock: Int,
    changeTapOnClock: (Int) -> Unit,
    notificationEnabled: Boolean,
    changeNotificationEnabled: () -> Unit,
    hardReset: () -> Unit,
    theme: Int,
    changeTheme: (Int) -> Unit,
    lockAwakeEnabled: Boolean,
    changeLockAwakeEnabled: () -> Unit,
    vibrationEnabled: Boolean,
    changeVibrationEnabled: () -> Unit,
    autoStartEnabled: Boolean,
    changeAutoStartEnabled: () -> Unit
) {
    val context = LocalContext.current
    val settingsDraw = painterResource(R.drawable.round_settings_24)
    val turnOffNotifDraw = painterResource(R.drawable.round_notifications_24)
    val turnOnNotifDraw = painterResource(R.drawable.round_notifications_none_24)
    val themeDraw = painterResource(R.drawable.round_invert_colors_24)
    val unlockAwake = painterResource(R.drawable.round_lock_outline_24)
    val lockAwake = painterResource(R.drawable.round_lock_24)

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showResetStopwatchDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLockAwakeDialog by remember { mutableStateOf(false) }

    @Composable
    fun actionDialogs(modifier: Modifier) {
        OutlineIconButton(
            modifier = modifier,
            onClick = { showSettingsDialog = true },
            painter = settingsDraw,
            contentDesc = context.getString(R.string.stopwatch_settings)
        )
        OutlineIconButton(
            modifier = modifier,
            onClick = { showResetStopwatchDialog = true },
            painter = if (notificationEnabled) turnOffNotifDraw else turnOnNotifDraw,
            contentDesc = context.getString(R.string.turn_off_notif)
        )
        OutlineIconButton(
            modifier = modifier,
            onClick = { showThemeDialog = true },
            painter = themeDraw,
            contentDesc = context.getString(R.string.theme)
        )
        OutlineIconButton(
            modifier = modifier,
            onClick = { showLockAwakeDialog = true },
            painter = if (lockAwakeEnabled) lockAwake else unlockAwake,
            contentDesc = context.getString(R.string.lock_awake)
        )
    }

    if (showSettingsDialog) {
        var showTapOnClockDialog by remember { mutableStateOf(false) }
        OkayDialog(
            title = context.getString(R.string.stopwatch_settings),
            content = {
                SettingsRow(
                    context.getString(R.string.change_tap_on_clock),
                    tapOnClock.toString()
                ) {
                    showTapOnClockDialog = true
                }
                SettingsRow(
                    context.getString(R.string.auto_start_sw),
                    if (autoStartEnabled) "ON" else "OFF"
                ) {
                    changeAutoStartEnabled()
                }
                SettingsRow(
                    context.getString(R.string.turn_on_vibration),
                    if (vibrationEnabled) "ON" else "OFF"
                ) {
                    changeVibrationEnabled()
                }
            },
            isPortrait = isPortrait,
            confirmText = context.getString(R.string.ok),
            onConfirm = { showSettingsDialog = false }
        )
        if (showTapOnClockDialog)
            RadioDialog(
                title = context.getString(R.string.change_tap_on_clock),
                desc = context.getString(R.string.change_tap_on_clock_desc),
                isPortrait = isPortrait,
                defaultIndex = tapOnClock,
                options = context.resources.getStringArray(R.array.tap_on_clock),
                setSelectedIndex = { newState -> changeTapOnClock(newState) },
                onDismiss = { showTapOnClockDialog = false },
                confirmText = context.getString(R.string.apply),
                onConfirm = { showTapOnClockDialog = false }
            )
    }
    if (showResetStopwatchDialog) {
        if (isStarted)
            SimpleDialog(
                title = context.getString(R.string.reset_stopwatch),
                desc = if (notificationEnabled)
                    context.getString(R.string.reset_stopwatch_desc_disable)
                else
                    context.getString(R.string.reset_stopwatch_desc_enable),
                isPortrait = isPortrait,
                confirmText = context.getString(R.string.ok),
                onConfirm = {
                    hardReset()
                    changeNotificationEnabled()
                    showResetStopwatchDialog = false
                },
                dismissText = context.getString(R.string.cancel),
                onDismiss = { showResetStopwatchDialog = false }
            )
        else
            changeNotificationEnabled()
    }
    if (showThemeDialog) {
        RadioDialog(
            title = context.getString(R.string.change_theme),
            isPortrait = isPortrait,
            desc = context.getString(R.string.change_theme_desc),
            defaultIndex = theme,
            options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                context.resources.getStringArray(R.array.theme12)
            else
                context.resources.getStringArray(R.array.theme),
            setSelectedIndex = { newState -> changeTheme(newState) },
            onDismiss = { showThemeDialog = false },
            confirmText = context.getString(R.string.apply),
            onConfirm = { showThemeDialog = false }
        )
    }
    if (showLockAwakeDialog) {
        LaunchedEffect(Unit) {
            changeLockAwakeEnabled()
            delay(1500)
            showLockAwakeDialog = false
        }

        OkayDialog(
            title = context.getString(R.string.lock_awake_mode),
            content = {
                Text(
                    text = if (lockAwakeEnabled)
                        context.getString(R.string.lock_awake_mode_desc_enable)
                    else
                        context.getString(R.string.lock_awake_mode_desc_disable),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            isPortrait = isPortrait,
            confirmText = context.getString(R.string.ok),
            onConfirm = { showLockAwakeDialog = false }
        )
    }

    if (isPortrait)
        androidx.compose.animation.AnimatedVisibility(
            visible = show,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 80 },
            exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { 80 }
        ) {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                actionDialogs(Modifier.weight(1f))
            }
        }
    else
        androidx.compose.animation.AnimatedVisibility(
            visible = show,
            enter = fadeIn(tween(500)) + slideInHorizontally(tween(500)) { -80 },
            exit = fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -80 }
        ) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                actionDialogs(Modifier.weight(1f))
            }
        }
}

@Composable
fun SettingsRow(text: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(10.dp, 12.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            fontSize = 20.sp
        )
        Text(
            text = value,
            fontSize = 21.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DisplayActionsPreview() {
    MaterialTheme(colorScheme = DarkColorScheme) {
        DisplayActions(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp, 8.dp, 8.dp, 14.dp),
            isPortrait = true,
            isStarted = true,
            show = true,
            tapOnClock = 0,
            changeTapOnClock = { },
            notificationEnabled = false,
            changeNotificationEnabled = { },
            hardReset = { },
            theme = 0,
            changeTheme = { },
            lockAwakeEnabled = false,
            changeLockAwakeEnabled = { },
            vibrationEnabled = false,
            changeVibrationEnabled = { },
            autoStartEnabled = false,
            changeAutoStartEnabled = { }
        )
    }
}