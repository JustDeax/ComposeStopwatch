package com.justdeax.composeStopwatch
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
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
import androidx.lifecycle.lifecycleScope
import com.justdeax.composeStopwatch.ui.DisplayActions
import com.justdeax.composeStopwatch.ui.DisplayAppName
import com.justdeax.composeStopwatch.ui.DisplayButton
import com.justdeax.composeStopwatch.ui.DisplayButtonInLandscape
import com.justdeax.composeStopwatch.ui.DisplayLaps
import com.justdeax.composeStopwatch.ui.DisplayTime
import com.justdeax.composeStopwatch.stopwatch.StopwatchService
import com.justdeax.composeStopwatch.stopwatch.StopwatchViewModel
import com.justdeax.composeStopwatch.stopwatch.StopwatchViewModelFactory
import com.justdeax.composeStopwatch.ui.theme.DarkColorScheme
import com.justdeax.composeStopwatch.ui.theme.ExtraDarkColorScheme
import com.justdeax.composeStopwatch.ui.theme.LightColorScheme
import com.justdeax.composeStopwatch.ui.theme.Typography
import com.justdeax.composeStopwatch.util.DataStoreManager
import com.justdeax.composeStopwatch.util.Lap
import com.justdeax.composeStopwatch.util.StopWatchState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList

class AppActivity : ComponentActivity() {
    val viewModel: StopwatchViewModel by viewModels {
        StopwatchViewModelFactory(DataStoreManager(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppScreen() }
    }

    override fun onStop() {
        super.onStop()
        if (!viewModel.notificationEnabled.value!!) viewModel.saveStopwatch()
    }

    @Composable
    fun AppScreen() {
        val notificationEnabled by viewModel.notificationEnabled.observeAsState(true)
        if (notificationEnabled) {
            val isStarted by StopwatchService.isStartedI.observeAsState(false)
            val isRunning by StopwatchService.isRunningI.observeAsState(false)
            val elapsedMs by StopwatchService.elapsedMsI.observeAsState(0L)
            val elapsedSec by StopwatchService.elapsedSecI.observeAsState(0L)
            val laps by StopwatchService.lapsI.observeAsState(LinkedList())
            StopwatchScreen(true, isStarted, isRunning, elapsedMs, elapsedSec, laps)
        } else {
            LaunchedEffect(Unit) {
                viewModel.restoreStopwatch()
            }
            val isStarted by viewModel.isStartedI.observeAsState(false)
            val isRunning by viewModel.isRunningI.observeAsState(false)
            val elapsedMs by viewModel.elapsedMsI.observeAsState(0L)
            val elapsedSec by viewModel.elapsedSecI.observeAsState(0L)
            val laps by viewModel.lapsI.observeAsState(LinkedList())
            StopwatchScreen(false, isStarted, isRunning, elapsedMs, elapsedSec, laps)
        }
    }

    @Composable
    fun StopwatchScreen(
        notificationEnabled: Boolean,
        isStarted: Boolean,
        isRunning: Boolean,
        elapsedMs: Long,
        elapsedSec: Long,
        laps: List<Lap>
    ) {
        var additionalActionsShow by remember { mutableStateOf(false) }
        val theme by viewModel.theme.observeAsState(0)
        val tapOnClock by viewModel.tapOnClock.observeAsState(0)
        val configuration = LocalConfiguration.current
        val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val colorScheme = when (theme) {
            1 -> LightColorScheme
            2 -> DarkColorScheme
            3 -> ExtraDarkColorScheme
            else -> { // == 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (isSystemInDarkTheme())
                        dynamicDarkColorScheme(this)
                    else
                        dynamicLightColorScheme(this)
                } else {
                    if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
                }
            }
        }

        MaterialTheme(colorScheme = colorScheme, typography = Typography) {
            Scaffold(Modifier.fillMaxSize()) { innerPadding ->
                LaunchedEffect(Unit) {
                    if (elapsedMs == 0L) additionalActionsShow = true
                }
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
                                this@AppActivity,
                                true,
                                !isStarted
                            )
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                DisplayTime(
                                    Modifier
                                        .animateContentSize()
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                        .heightIn(min = 100.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { clickOnClock(tapOnClock, isRunning, notificationEnabled) },
                                    true,
                                    isStarted && !isRunning,
                                    elapsedSec,
                                    elapsedMs
                                )
                                DisplayLaps(
                                    Modifier
                                        .padding(4.dp, 0.dp)
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
                            this@AppActivity,
                            true,
                            additionalActionsShow,
                            notificationEnabled
                        )
                        DisplayButton(
                            this@AppActivity,
                            isStarted,
                            isRunning,
                            additionalActionsShow,
                            showAdditionals = { newState -> additionalActionsShow = newState },
                            notificationEnabled
                        )
                    }
                } else {
                    Row(Modifier.padding(innerPadding)) {
                        DisplayButtonInLandscape(
                            this@AppActivity,
                            isStarted,
                            isRunning,
                            additionalActionsShow,
                            showAdditionals = { newState -> additionalActionsShow = newState },
                            notificationEnabled
                        )
                        DisplayActions(
                            Modifier
                                .fillMaxHeight()
                                .wrapContentWidth()
                                .padding(14.dp, 8.dp, 8.dp, 8.dp),
                            this@AppActivity,
                            false,
                            additionalActionsShow,
                            notificationEnabled
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            DisplayAppName(
                                Modifier.padding(21.dp, 16.dp),
                                this@AppActivity,
                                false,
                                !isStarted
                            )
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                DisplayTime(
                                    Modifier
                                        .animateContentSize()
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                        .heightIn(min = 100.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) { clickOnClock(tapOnClock, isRunning, notificationEnabled) },
                                    laps.isNotEmpty(),
                                    isStarted && !isRunning,
                                    elapsedSec,
                                    elapsedMs
                                )
                                DisplayLaps(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(26.dp, 0.dp),
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

    fun commandService(serviceState: StopWatchState) {
        val context = this
        val intent = Intent(context, StopwatchService::class.java)
        intent.action = serviceState.name
        context.startService(intent)
    }

    private fun clickOnClock(tapType: Int, isRunning: Boolean, notificationEnabled: Boolean) {
        when (tapType) {
            1 -> {
                if (isRunning) {
                    if (notificationEnabled) commandService(StopWatchState.PAUSE)
                    else viewModel.pause()
                } else {
                    if (notificationEnabled) commandService(StopWatchState.START_RESUME)
                    else viewModel.startResume()
                }
            }
            2 -> {
                if (isRunning) {
                    if (notificationEnabled) commandService(StopWatchState.ADD_LAP)
                    else viewModel.addLap()
                } else {
                    if (notificationEnabled) commandService(StopWatchState.START_RESUME)
                    else viewModel.startResume()
                }
            }
            3 -> {
                if (isRunning) {
                    if (notificationEnabled)
                        lifecycleScope.launch {
                            commandService(StopWatchState.PAUSE)
                            delay(20)
                            commandService(StopWatchState.RESET)
                        }
                    else
                        lifecycleScope.launch {
                            viewModel.pause()
                            delay(20)
                            viewModel.reset()
                        }
                } else {
                    if (notificationEnabled) commandService(StopWatchState.START_RESUME)
                    else viewModel.startResume()
                }
            }
        }
    }
}