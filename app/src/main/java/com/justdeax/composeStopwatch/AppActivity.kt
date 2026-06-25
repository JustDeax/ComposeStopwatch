package com.justdeax.composeStopwatch

import android.content.Intent
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.justdeax.composeStopwatch.stopwatch.StopwatchService
import com.justdeax.composeStopwatch.stopwatch.StopwatchViewModel
import com.justdeax.composeStopwatch.stopwatch.StopwatchViewModelFactory
import com.justdeax.composeStopwatch.ui.DisplayActions
import com.justdeax.composeStopwatch.ui.DisplayAppName
import com.justdeax.composeStopwatch.ui.DisplayButtons
import com.justdeax.composeStopwatch.ui.DisplayLaps
import com.justdeax.composeStopwatch.ui.DisplayTime
import com.justdeax.composeStopwatch.ui.dialog.AutoStartDialog
import com.justdeax.composeStopwatch.ui.dialog.OkayDialog
import com.justdeax.composeStopwatch.ui.theme.DarkColorScheme
import com.justdeax.composeStopwatch.ui.theme.ExtraDarkColorScheme
import com.justdeax.composeStopwatch.ui.theme.LightColorScheme
import com.justdeax.composeStopwatch.ui.theme.Typography
import com.justdeax.composeStopwatch.util.DataStoreManager
import com.justdeax.composeStopwatch.util.Dimens
import com.justdeax.composeStopwatch.util.Lap
import com.justdeax.composeStopwatch.util.StopwatchAction
import com.justdeax.composeStopwatch.util.addLapVibration
import com.justdeax.composeStopwatch.util.pauseVibration
import com.justdeax.composeStopwatch.util.resetVibration
import com.justdeax.composeStopwatch.util.startResumeVibration
import java.util.LinkedList

class AppActivity : ComponentActivity() {
    private val viewModel: StopwatchViewModel by viewModels {
        StopwatchViewModelFactory(DataStoreManager(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        setContent { AppScreen() }
    }

    private fun requestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )

    }

    @Composable
    fun AppScreen() {
        val notificationEnabled by viewModel.notificationEnabled.observeAsState(true)
        val isStarted by (if (notificationEnabled) StopwatchService.isStartedI else viewModel.isStartedI).observeAsState(false)
        val isRunning by (if (notificationEnabled) StopwatchService.isRunningI else viewModel.isRunningI).observeAsState(false)
        val elapsedMs by (if (notificationEnabled) StopwatchService.elapsedMsI else viewModel.elapsedMsI).observeAsState(0L)
        val elapsedSec by (if (notificationEnabled) StopwatchService.elapsedSecI else viewModel.elapsedSecI).observeAsState(0L)
        val laps by (if (notificationEnabled) StopwatchService.lapsI else viewModel.lapsI).observeAsState(LinkedList())
        val previousLapDelta by (if (notificationEnabled) StopwatchService.previousLapDelta else viewModel.previousLapDelta).observeAsState(0L)

        if (!notificationEnabled)
            LaunchedEffect(Unit) { viewModel.restoreStopwatch() }

        StopwatchScreen(
            notificationEnabled, isStarted, isRunning, elapsedMs, elapsedSec, laps, previousLapDelta
        )
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

        val tapOnClock by viewModel.tapOnClock.observeAsState(0)
        val autoStartEnabled by viewModel.autoStartEnabled.observeAsState(false)
        val vibrationEnabled by viewModel.vibrationEnabled.observeAsState(false)
        val theme by viewModel.theme.observeAsState(0)
        val lockAwakeEnabled by viewModel.lockAwakeEnabled.observeAsState(false)
        val lockAwakeFirstTimeEnabled by viewModel.lockAwakeFirstTimeEnabled.observeAsState(true)
        val firstBoot by viewModel.firstBoot.observeAsState(true)

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                getSystemService(VibratorManager::class.java)?.defaultVibrator
            else
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        LaunchedEffect(autoStartEnabled) {
            autoStartEnabledNow = autoStartEnabled
        }
        LaunchedEffect(lockAwakeEnabled) {
            val keepScreenOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            if (lockAwakeEnabled)
                window.addFlags(keepScreenOn)
            else
                window.clearFlags(keepScreenOn)
        }

        @Composable
        fun FinalDisplayAppName() {
            DisplayAppName(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(21.dp, 16.dp),
                isPortrait = isPortrait,
                show = !isStarted
            )
        }

        @Composable
        fun FinalDisplayTime() {
            DisplayTime(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .heightIn(min = 100.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        clickOnClock(
                            tapOnClock,
                            isRunning,
                            notificationEnabled,
                            vibrationEnabled,
                            vibrator
                        )
                        if (additionalActionsShow) additionalActionsShow = false
                    },
                isPortrait = isPortrait,
                isPausing = isStarted && !isRunning,
                seconds = elapsedSec,
                milliseconds = elapsedMs,
                laps = laps,
                previousLapDelta = previousLapDelta
            )
        }

        @Composable
        fun FinalDisplayLaps(modifier: Modifier) {
            DisplayLaps(
                modifier = modifier.fillMaxWidth(),
                laps = laps,
                elapsedMs = elapsedMs
            )
        }

        @Composable
        fun FinalDisplayActions(modifier: Modifier) {
            DisplayActions(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                show = !isStarted || additionalActionsShow,
                isPortrait = isPortrait,
                isStarted = isStarted,
                tapOnClock = tapOnClock,
                changeTapOnClock = { newState -> viewModel.changeTapOnClock(newState) },
                vibrationEnabled = vibrationEnabled,
                changeVibrationEnabled = { viewModel.changeVibrationEnabled(!vibrationEnabled) },
                autoStartEnabled = autoStartEnabled,
                changeAutoStartEnabled = { viewModel.changeAutoStartEnabled(!autoStartEnabled) },
                notificationEnabled = notificationEnabled,
                changeNotificationEnabled = { viewModel.changeNotificationEnabled(!notificationEnabled) },
                pauseStopwatch = { pause(notificationEnabled, vibrationEnabled, vibrator) },
                resetStopwatch = {
                    reset(notificationEnabled, vibrationEnabled, vibrator)
                    if (additionalActionsShow) additionalActionsShow = false
                },
                theme = theme,
                changeTheme = { newState -> viewModel.changeTheme(newState) },
                lockAwakeEnabled = lockAwakeEnabled,
                changeLockAwakeEnabled = { viewModel.changeLockAwakeEnabled(!lockAwakeEnabled) },
                lockAwakeFirstTimeEnabled = lockAwakeFirstTimeEnabled,
                changeLockAwakeFirstTimeEnabled = { viewModel.changeLockAwakeFirstTimeEnabled(!lockAwakeFirstTimeEnabled)}
            )
        }

        @Composable
        fun FinalDisplayButtons(modifier: Modifier) {
            DisplayButtons(
                modifier = modifier,
                isPortrait = isPortrait,
                isStarted = isStarted,
                isRunning = isRunning,
                additionalActionShow = additionalActionsShow,
                showHideAdditional = { additionalActionsShow = !additionalActionsShow },
                reset = {
                    reset(notificationEnabled, vibrationEnabled, vibrator)
                    if (additionalActionsShow) additionalActionsShow = false
                },
                startResume = { startResume(notificationEnabled, vibrationEnabled, vibrator) },
                pause = { pause(notificationEnabled, vibrationEnabled, vibrator) },
                addLap = { addLap(notificationEnabled, vibrationEnabled, vibrator) }
            )
        }

        MaterialTheme(colorScheme = colorScheme, typography = Typography) {
            Scaffold(Modifier.fillMaxSize()) { innerPadding ->
                if (firstBoot) {
                    OkayDialog(
                        title = stringResource(R.string.changelogs),
                        isPortrait = isPortrait,
                        confirmText = stringResource(R.string.ok),
                        onConfirm = { viewModel.disableFirstBoot() }
                    ) {
                        Text(
                            text = stringResource(R.string.changelogs_desc),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                if (!isStarted && autoStartEnabledNow)
                    AutoStartDialog(
                        isPortrait = isPortrait,
                        onDismiss = { autoStartEnabledNow = false },
                        startStopwatch = { startResume(notificationEnabled, vibrationEnabled, vibrator) }
                    )

                if (isPortrait) {
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        FinalDisplayAppName()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = if (laps.size <= 1) Dimens.generalButtonHeight.dp else 0.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            FinalDisplayTime()
                            FinalDisplayLaps(
                                modifier = Modifier.padding(8.dp, 0.dp)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {
                            FinalDisplayActions(
                                modifier = Modifier.padding(8.dp, 0.dp)
                            )
                            FinalDisplayButtons(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = Dimens.topButtonsPadding.dp,
                                        bottom = Dimens.bottomButtonsPadding.dp
                                    )
                            )
                        }
                    }
                } else {
                    Row(Modifier.padding(innerPadding)) {
                        FinalDisplayButtons(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(
                                    start = Dimens.bottomButtonsPadding.dp,
                                    end = Dimens.topButtonsPadding.dp
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f)
                        ) {
                            FinalDisplayActions(
                                Modifier.padding(0.dp, 8.dp),
                            )
                            FinalDisplayAppName()
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                FinalDisplayTime()
                                FinalDisplayLaps(
                                    modifier = Modifier.padding(32.dp, 0.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun clickOnClock(tapType: Int, isRunning: Boolean, notificationEnabled: Boolean, vibrationEnabled: Boolean, vibrator: Vibrator?) {
        if (isRunning) when (tapType) {
            1 -> pause(notificationEnabled, vibrationEnabled, vibrator)
            2 -> addLap(notificationEnabled, vibrationEnabled, vibrator)
            3 -> hardReset(notificationEnabled, vibrationEnabled, vibrator)
        } else startResume(notificationEnabled, vibrationEnabled, vibrator)
    }

    private fun startResume(notificationEnabled: Boolean, vibrationEnabled: Boolean, vibrator: Vibrator?) {
        commandVibration(vibrationEnabled, vibrator, startResumeVibration)
        if (notificationEnabled) commandService(StopwatchAction.START_RESUME)
        else viewModel.startResume()
    }

    private fun pause(notificationEnabled: Boolean, vibrationEnabled: Boolean, vibrator: Vibrator?) {
        commandVibration(vibrationEnabled, vibrator, pauseVibration)
        if (notificationEnabled) commandService(StopwatchAction.PAUSE)
        else viewModel.pause()
    }

    private fun addLap(notificationEnabled: Boolean, vibrationEnabled: Boolean, vibrator: Vibrator?) {
        commandVibration(vibrationEnabled, vibrator, addLapVibration)
        if (notificationEnabled) commandService(StopwatchAction.ADD_LAP)
        else viewModel.addLap()
    }

    private fun reset(notificationEnabled: Boolean, vibrationEnabled: Boolean, vibrator: Vibrator?) {
        commandVibration(vibrationEnabled, vibrator, resetVibration)
        if (notificationEnabled) commandService(StopwatchAction.RESET)
        else viewModel.reset()
    }

    private fun hardReset(notificationEnabled: Boolean, vibrationEnabled: Boolean, vibrator: Vibrator?) {
        commandVibration(vibrationEnabled, vibrator, resetVibration)
        if (notificationEnabled) commandService(StopwatchAction.HARD_RESET)
        else viewModel.hardReset()
    }

    private fun commandVibration(enabled: Boolean, vibrator: Vibrator?, vibe: VibrationEffect?) {
        if (enabled && vibrator != null) {
            vibrator.cancel()
            vibrator.vibrate(vibe)
        }
    }

    private fun commandService(serviceState: StopwatchAction) {
        val intent = Intent(this, StopwatchService::class.java)
        intent.action = serviceState.name
        this.startService(intent)
    }
}