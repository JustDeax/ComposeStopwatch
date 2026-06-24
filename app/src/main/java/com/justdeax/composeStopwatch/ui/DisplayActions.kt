package com.justdeax.composeStopwatch.ui

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justdeax.composeStopwatch.R
import com.justdeax.composeStopwatch.ui.dialog.OkayDialog
import com.justdeax.composeStopwatch.ui.dialog.RadioDialog
import com.justdeax.composeStopwatch.ui.dialog.SimpleDialog
import com.justdeax.composeStopwatch.ui.theme.DarkColorScheme
import com.justdeax.composeStopwatch.ui.theme.Typography
import com.justdeax.composeStopwatch.util.enterAnimation
import com.justdeax.composeStopwatch.util.exitAnimation

@Composable
fun DisplayActions(
    modifier: Modifier,
    show: Boolean,
    isPortrait: Boolean,
    isStarted: Boolean,
    tapOnClock: Int,
    changeTapOnClock: (Int) -> Unit,
    vibrationEnabled: Boolean,
    changeVibrationEnabled: () -> Unit,
    autoStartEnabled: Boolean,
    changeAutoStartEnabled: () -> Unit,
    notificationEnabled: Boolean,
    changeNotificationEnabled: () -> Unit,
    pauseStopwatch: () -> Unit,
    resetStopwatch: () -> Unit,
    theme: Int,
    changeTheme: (Int) -> Unit,
    lockAwakeEnabled: Boolean,
    changeLockAwakeEnabled: () -> Unit,
    lockAwakeFirstTimeEnabled: Boolean,
    changeLockAwakeFirstTimeEnabled: () -> Unit
) {
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
    fun actionButtons(modifier: Modifier) {
        IconOutlineButton(
            modifier = modifier,
            onClick = { showSettingsDialog = true },
            painter = settingsDraw,
            contentDesc = stringResource(R.string.stopwatch_settings)
        )
        IconOutlineButton(
            modifier = modifier,
            onClick = {
                if (isStarted) {
                    pauseStopwatch()
                    showResetStopwatchDialog = true
                } else {
                    changeNotificationEnabled()
                }
                      },
            painter = if (notificationEnabled) turnOffNotifDraw else turnOnNotifDraw,
            contentDesc = stringResource(R.string.turn_off_notif)
        )
        IconOutlineButton(
            modifier = modifier,
            onClick = { showThemeDialog = true },
            painter = themeDraw,
            contentDesc = stringResource(R.string.theme)
        )
        IconOutlineButton(
            modifier = modifier,
            onClick = {
                changeLockAwakeEnabled()
                if (lockAwakeFirstTimeEnabled)
                    showLockAwakeDialog = true
                      },
            painter = if (lockAwakeEnabled) lockAwake else unlockAwake,
            contentDesc = stringResource(R.string.lock_awake)
        )
    }

    if (showSettingsDialog) {
        var showTapOnClockDialog by remember { mutableStateOf(false) }
        OkayDialog(
            title = stringResource(R.string.stopwatch_settings),
            isPortrait = isPortrait,
            confirmText = stringResource(R.string.ok),
            onConfirm = { showSettingsDialog = false }
        ) {
            SettingsRow(
                stringResource(R.string.change_tap_on_clock),
                { showTapOnClockDialog = true }
            ) {
                Text(
                    text = tapOnClock.toString(),
                    style = Typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            SettingsRow(
                stringResource(R.string.auto_start_sw),
                { changeAutoStartEnabled() }
            ) {
                Switch(
                    autoStartEnabled,
                    { _ -> changeAutoStartEnabled() }
                )
            }
            SettingsRow(
                stringResource(R.string.turn_on_vibration),
                { changeVibrationEnabled() }
            ) {
                Switch(
                    vibrationEnabled,
                    { _ -> changeVibrationEnabled() }
                )
            }
        }
        if (showTapOnClockDialog)
            RadioDialog(
                title = stringResource(R.string.change_tap_on_clock),
                desc = stringResource(R.string.change_tap_on_clock_desc),
                isPortrait = isPortrait,
                defaultIndex = tapOnClock,
                options = stringArrayResource(R.array.tap_on_clock),
                setSelectedIndex = { newState -> changeTapOnClock(newState) },
                onDismiss = { showTapOnClockDialog = false },
                confirmText = stringResource(R.string.apply),
                onConfirm = { showTapOnClockDialog = false }
            )
    }
    if (showResetStopwatchDialog) {
        SimpleDialog(
            title = stringResource(R.string.reset_stopwatch),
            desc = if (notificationEnabled)
                stringResource(R.string.reset_stopwatch_desc_disable)
            else
                stringResource(R.string.reset_stopwatch_desc_enable),
            isPortrait = isPortrait,
            confirmText = stringResource(R.string.ok),
            onConfirm = {
                resetStopwatch()
                changeNotificationEnabled()
                showResetStopwatchDialog = false
            },
            dismissText = stringResource(R.string.cancel),
            onDismiss = { showResetStopwatchDialog = false }
        )
    }
    if (showThemeDialog) {
        RadioDialog(
            title = stringResource(R.string.change_theme),
            isPortrait = isPortrait,
            desc = stringResource(R.string.change_theme_desc),
            defaultIndex = theme,
            options = stringArrayResource(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) R.array.theme12
                else R.array.theme),
            setSelectedIndex = { newState -> changeTheme(newState) },
            onDismiss = { showThemeDialog = false },
            confirmText = stringResource(R.string.apply),
            onConfirm = { showThemeDialog = false }
        )
    }
    if (showLockAwakeDialog) {
        OkayDialog(
            title = stringResource(R.string.lock_awake_mode),
            isPortrait = isPortrait,
            confirmText = stringResource(R.string.ok),
            onConfirm = {
                if (lockAwakeFirstTimeEnabled)
                    changeLockAwakeFirstTimeEnabled()
                showLockAwakeDialog = false
            }
        ) {
            Text(
                text = stringResource(R.string.lock_awake_mode_desc_enable),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    if (isPortrait)
        androidx.compose.animation.AnimatedVisibility(
            visible = show,
            enter = enterAnimation,
            exit = exitAnimation
        ) {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                actionButtons(Modifier.weight(1f))
            }
        }
    else
        androidx.compose.animation.AnimatedVisibility(
            visible = show,
            enter = enterAnimation,
            exit = exitAnimation
        ) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                actionButtons(Modifier.weight(1f))
            }
        }
}

@Composable
fun SettingsRow(text: String, onClick: () -> Unit, content: @Composable (RowScope.() -> Unit)) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(10.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            fontSize = 20.sp
        )
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun DisplayActionsPreview() {
    MaterialTheme(colorScheme = DarkColorScheme) {
        DisplayActions(
            Modifier,
            show = true,
            isPortrait = true,
            isStarted = true,
            tapOnClock = 0,
            changeTapOnClock = { },
            vibrationEnabled = false,
            changeVibrationEnabled = { },
            autoStartEnabled = false,
            changeAutoStartEnabled = { },
            notificationEnabled = false,
            changeNotificationEnabled = { },
            pauseStopwatch = { },
            resetStopwatch = { },
            theme = 0,
            changeTheme = { },
            lockAwakeEnabled = false,
            changeLockAwakeEnabled = { },
            lockAwakeFirstTimeEnabled = true,
            changeLockAwakeFirstTimeEnabled = { }
        )
    }
}