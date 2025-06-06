package com.justdeax.composeStopwatch

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.justdeax.composeStopwatch.stopwatch.StopwatchService
import com.justdeax.composeStopwatch.stopwatch.StopwatchViewModel
import com.justdeax.composeStopwatch.stopwatch.StopwatchViewModelFactory
import com.justdeax.composeStopwatch.ui.DisplayActions
import com.justdeax.composeStopwatch.ui.DisplayAppName
import com.justdeax.composeStopwatch.ui.DisplayButton
import com.justdeax.composeStopwatch.ui.DisplayButtonInLandscape
import com.justdeax.composeStopwatch.ui.DisplayLaps
import com.justdeax.composeStopwatch.ui.DisplayTime
import com.justdeax.composeStopwatch.ui.dialog.DisplayAutoStartDialog
import com.justdeax.composeStopwatch.ui.theme.DarkColorScheme
import com.justdeax.composeStopwatch.ui.theme.ExtraDarkColorScheme
import com.justdeax.composeStopwatch.ui.theme.LightColorScheme
import com.justdeax.composeStopwatch.ui.theme.Typography
import com.justdeax.composeStopwatch.util.DataStoreManager
import com.justdeax.composeStopwatch.util.Lap
import com.justdeax.composeStopwatch.util.StopwatchAction
import com.justdeax.composeStopwatch.util.commandService
import java.util.LinkedList

class AppActivity : ComponentActivity() {
    private val viewModel: StopwatchViewModel by viewModels {
        StopwatchViewModelFactory(DataStoreManager(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        setContent { AppScreen() }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            )
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
    }

    @Composable
    fun AppScreen() {
        val notificationEnabled: Boolean by viewModel.notificationEnabled.observeAsState(true)
        if (notificationEnabled) {
            val isStarted by StopwatchService.isStartedI.observeAsState(false)
            val isRunning by StopwatchService.isRunningI.observeAsState(false)
            val elapsedMs by StopwatchService.elapsedMsI.observeAsState(0L)
            val elapsedSec by StopwatchService.elapsedSecI.observeAsState(0L)
            val laps by StopwatchService.lapsI.observeAsState(LinkedList())
            val previousLapDelta by StopwatchService.previousLapDelta.observeAsState(0L)
            StopwatchScreen(
                true, isStarted, isRunning, elapsedMs, elapsedSec, laps, previousLapDelta
            )
        } else {
            LaunchedEffect(Unit) { viewModel.restoreStopwatch() }
            val isStarted by viewModel.isStartedI.observeAsState(false)
            val isRunning by viewModel.isRunningI.observeAsState(false)
            val elapsedMs by viewModel.elapsedMsI.observeAsState(0L)
            val elapsedSec by viewModel.elapsedSecI.observeAsState(0L)
            val laps by viewModel.lapsI.observeAsState(LinkedList())
            val previousLapDelta by viewModel.previousLapDelta.observeAsState(0L)
            StopwatchScreen(
                false, isStarted, isRunning, elapsedMs, elapsedSec, laps, previousLapDelta
            )
        }
    }

    @Composable
    fun StopwatchScreen(
        notificationEnabled: Boolean,
        isStarted: Boolean,
        isRunning: Boolean,
        elapsedMs: Long,
        elapsedSec: Long,
        laps: List<Lap>,
        previousLapDelta: Long
    ) {
        var additionalActionsShow by remember { mutableStateOf(false) }
        var autoStartEnabledNow by remember { mutableStateOf(false) }

        val theme by viewModel.theme.observeAsState(0)
        val tapOnClock by viewModel.tapOnClock.observeAsState(0)
        val lockAwakeEnabled by viewModel.lockAwakeEnabled.observeAsState(false)
        val vibrationEnabled by viewModel.vibrationEnabled.observeAsState(false)
        val autoStartEnabled by viewModel.autoStartEnabled.observeAsState(false)

        val configuration = LocalConfiguration.current
        val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val isDarkTheme = isSystemInDarkTheme()
        val colorScheme = remember(theme, isDarkTheme) {
            when (theme) {
                1 -> LightColorScheme
                2 -> DarkColorScheme
                3 -> ExtraDarkColorScheme
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (isDarkTheme) dynamicDarkColorScheme(this)
                        else dynamicLightColorScheme(this)
                    } else {
                        if (isDarkTheme) DarkColorScheme else LightColorScheme
                    }
                }
            }
        }

        val vibrator = remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VibratorManager::class.java)
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
        }
        val startResumeVibration = VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
        val pauseVibration = VibrationEffect.createOneShot(200, 80)
        val resetVibration = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
        val addLapVibration = VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1)

        LaunchedEffect(lockAwakeEnabled) {
            if (lockAwakeEnabled)
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            else
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        LaunchedEffect(autoStartEnabled) {
            autoStartEnabledNow = autoStartEnabled
        }

        MaterialTheme(colorScheme = colorScheme, typography = Typography) {
            Scaffold(Modifier.fillMaxSize()) { innerPadding ->
                if (!isStarted && autoStartEnabledNow)
                    DisplayAutoStartDialog(
                        isPortrait = isPortrait,
                        onDismiss = { autoStartEnabledNow = false },
                        startStopwatch = {
                            notificationEnabled.startResume()
                            if (vibrationEnabled && vibrator != null) {
                                vibrator.cancel()
                                vibrator.vibrate(startResumeVibration)
                            }
                        }
                    )

                if (isPortrait) {
                    Column(Modifier.padding(innerPadding)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.TopStart
                        ) {
                            DisplayAppName(
                                Modifier.padding(21.dp, 16.dp),
                                !isStarted
                            )
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                DisplayTime(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                        .heightIn(min = 100.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            clickOnClock(tapOnClock, isRunning, notificationEnabled)
                                            if (vibrationEnabled && vibrator != null) {
                                                vibrator.cancel()
                                                if (isRunning) when (tapOnClock) {
                                                    1 -> vibrator.vibrate(pauseVibration)
                                                    2 -> vibrator.vibrate(addLapVibration)
                                                    3 -> vibrator.vibrate(resetVibration)
                                                } else vibrator.vibrate(startResumeVibration)
                                            }
                                        },
                                    true,
                                    isStarted && !isRunning,
                                    elapsedSec,
                                    elapsedMs,
                                    laps,
                                    previousLapDelta
                                )
                                DisplayLaps(
                                    Modifier
                                        .padding(8.dp, 0.dp)
                                        .fillMaxWidth(),
                                    laps,
                                    elapsedMs
                                )
                            }
                        }
                        DisplayActions(
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(8.dp, 8.dp, 8.dp, 14.dp),
                            true,
                            isStarted,
                            !isStarted || additionalActionsShow,
                            tapOnClock,
                            { newState -> viewModel.changeTapOnClock(newState) },
                            notificationEnabled,
                            { viewModel.changeNotificationEnabled(!notificationEnabled) },
                            { viewModel.hardReset() },
                            theme,
                            { newState -> viewModel.changeTheme(newState) },
                            lockAwakeEnabled,
                            { viewModel.changeLockAwakeEnabled(!lockAwakeEnabled) },
                            vibrationEnabled,
                            { viewModel.changeVibrationEnabled(!vibrationEnabled) },
                            autoStartEnabled,
                            { viewModel.changeAutoStartEnabled(!autoStartEnabled) }
                        )
                        DisplayButton(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 50.dp),
                            isStarted,
                            isRunning,
                            { additionalActionsShow = !additionalActionsShow },
                            {
                                if (additionalActionsShow) additionalActionsShow = false
                                notificationEnabled.reset()
                                if (vibrationEnabled && vibrator != null) {
                                    vibrator.cancel()
                                    vibrator.vibrate(resetVibration)
                                }
                            },
                            {
                                notificationEnabled.startResume()
                                if (vibrationEnabled && vibrator != null) {
                                    vibrator.cancel()
                                    vibrator.vibrate(startResumeVibration)
                                }
                            },
                            {
                                notificationEnabled.pause()
                                if (vibrationEnabled && vibrator != null) {
                                    vibrator.cancel()
                                    vibrator.vibrate(pauseVibration)
                                }
                            },
                            {
                                notificationEnabled.addLap()
                                if (vibrationEnabled && vibrator != null) {
                                    vibrator.cancel()
                                    vibrator.vibrate(addLapVibration)
                                }
                            }
                        )
                    }
                } else {
                    Row(Modifier.padding(innerPadding)) {
                        DisplayButtonInLandscape(
                            Modifier
                                .fillMaxHeight()
                                .padding(start = 50.dp, end = 20.dp),
                            isStarted,
                            isRunning,
                            { additionalActionsShow = !additionalActionsShow },
                            {
                                if (additionalActionsShow) additionalActionsShow = false
                                notificationEnabled.reset()
                                if (vibrationEnabled && vibrator != null) {
                                    vibrator.cancel()
                                    vibrator.vibrate(resetVibration)
                                }
                            },
                            {
                                notificationEnabled.startResume()
                                if (vibrationEnabled && vibrator != null) {
                                    vibrator.cancel()
                                    vibrator.vibrate(startResumeVibration)
                                }
                            },
                            {
                                notificationEnabled.pause()
                                if (vibrationEnabled && vibrator != null) {
                                    vibrator.cancel()
                                    vibrator.vibrate(pauseVibration)
                                }
                            },
                            {
                                notificationEnabled.addLap()
                                if (vibrationEnabled && vibrator != null) {
                                    vibrator.cancel()
                                    vibrator.vibrate(addLapVibration)
                                }
                            }
                        )
                        DisplayActions(
                            Modifier
                                .fillMaxHeight()
                                .wrapContentWidth()
                                .padding(14.dp, 8.dp, 8.dp, 8.dp),
                            false,
                            isStarted,
                            !isStarted || additionalActionsShow,
                            tapOnClock,
                            { newState -> viewModel.changeTapOnClock(newState) },
                            notificationEnabled,
                            { viewModel.changeNotificationEnabled(!notificationEnabled) },
                            { notificationEnabled.hardReset() },
                            theme,
                            { newState -> viewModel.changeTheme(newState) },
                            lockAwakeEnabled,
                            { viewModel.changeLockAwakeEnabled(!lockAwakeEnabled) },
                            vibrationEnabled,
                            { viewModel.changeVibrationEnabled(!vibrationEnabled) },
                            autoStartEnabled,
                            { viewModel.changeAutoStartEnabled(!autoStartEnabled) }
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            DisplayAppName(
                                Modifier.padding(21.dp, 16.dp),
                                !isStarted
                            )
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                DisplayTime(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                        .heightIn(min = 100.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            clickOnClock(tapOnClock, isRunning, notificationEnabled)
                                            if (vibrationEnabled && vibrator != null) {
                                                vibrator.cancel()
                                                if (isRunning) when (tapOnClock) {
                                                    1 -> vibrator.vibrate(pauseVibration)
                                                    2 -> vibrator.vibrate(addLapVibration)
                                                    3 -> vibrator.vibrate(resetVibration)
                                                } else vibrator.vibrate(startResumeVibration)
                                            }
                                        },
                                    false,
                                    isStarted && !isRunning,
                                    elapsedSec,
                                    elapsedMs,
                                    laps,
                                    previousLapDelta
                                )
                                DisplayLaps(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp, 0.dp),
                                    laps,
                                    elapsedMs
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun clickOnClock(tapType: Int, isRunning: Boolean, notificationEnabled: Boolean) {
        if (isRunning) when (tapType) {
            1 -> notificationEnabled.pause()
            2 -> notificationEnabled.addLap()
            3 -> notificationEnabled.hardReset()
        } else notificationEnabled.startResume()
    }

    private fun Boolean.startResume() {
        if (this) commandService(StopwatchAction.START_RESUME)
        else viewModel.startResume()
    }

    private fun Boolean.pause() {
        if (this) commandService(StopwatchAction.PAUSE)
        else viewModel.pause()
    }

    private fun Boolean.addLap() {
        if (this) commandService(StopwatchAction.ADD_LAP)
        else viewModel.addLap()
    }

    private fun Boolean.reset() {
        if (this) commandService(StopwatchAction.RESET)
        else viewModel.reset()
    }

    private fun Boolean.hardReset() {
        if (this) commandService(StopwatchAction.HARD_RESET)
        else viewModel.hardReset()
    }
}