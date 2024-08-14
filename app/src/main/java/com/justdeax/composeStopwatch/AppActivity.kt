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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.justdeax.composeStopwatch.stopwatch.DisplayActions
import com.justdeax.composeStopwatch.stopwatch.DisplayAppName
import com.justdeax.composeStopwatch.stopwatch.DisplayButton
import com.justdeax.composeStopwatch.stopwatch.DisplayButtonInLandscape
import com.justdeax.composeStopwatch.stopwatch.DisplayLaps
import com.justdeax.composeStopwatch.stopwatch.DisplayTime
import com.justdeax.composeStopwatch.stopwatch.StopwatchService
import com.justdeax.composeStopwatch.stopwatch.StopwatchViewModel
import com.justdeax.composeStopwatch.stopwatch.StopwatchViewModelFactory
import com.justdeax.composeStopwatch.ui.theme.DarkColorScheme
import com.justdeax.composeStopwatch.ui.theme.LightColorScheme
import com.justdeax.composeStopwatch.ui.theme.Typography
import com.justdeax.composeStopwatch.util.DataStoreManager
import com.justdeax.composeStopwatch.util.Lap
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
        setContent { AppPreviewScreen() }
    }

    override fun onStop() {
        super.onStop()
        if (!viewModel.notificationEnabled.value!!) viewModel.saveStopwatch()
    }

    @Preview(showSystemUi = true, showBackground = true)
    @Composable
    fun AppPreviewScreen() {
        var additionalActionsShow by remember { mutableStateOf(false) }
        val theme by viewModel.theme.observeAsState(0)
        val tapOnClock by viewModel.tapOnClock.observeAsState(0)
        val notificationEnabled by viewModel.notificationEnabled.observeAsState(true)
        val context = LocalContext.current
        val configuration = LocalConfiguration.current
        val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        val colorScheme = when (theme) {
            1 -> LightColorScheme
            2 -> DarkColorScheme
            else -> { // == 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (isSystemInDarkTheme())
                        dynamicDarkColorScheme(context)
                    else
                        dynamicLightColorScheme(context)
                } else {
                    if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
                }
            }
        }

        @Composable
        fun StopwatchScreen(isRunning: Boolean, elapsedMs: Long, elapsedSec: Long, laps: List<Lap>) {
            MaterialTheme(colorScheme = colorScheme, typography = Typography) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LaunchedEffect(Unit) {
                        if (elapsedMs == 0L) additionalActionsShow = true
                    }

                    if (isPortrait) {
                        Column(modifier = Modifier.padding(innerPadding)) {
                            DisplayAppName(
                                Modifier
                                    .animateContentSize()
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(21.dp, 18.dp),
                                this@AppActivity,
                                additionalActionsShow
                            )
                            DisplayTime(
                                if (laps.isEmpty()) Modifier
                                    .animateContentSize()
                                    .fillMaxWidth()
                                    .weight(1F)
                                else Modifier
                                    .animateContentSize()
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp),
                                true,
                                elapsedSec,
                                elapsedMs
                            ) { clickOnClock(tapOnClock, isRunning, notificationEnabled) }
                            DisplayLaps(
                                if (laps.isEmpty()) Modifier
                                    .animateContentSize()
                                else Modifier
                                    .animateContentSize()
                                    .weight(1F),
                                laps
                            )
                            DisplayActions(
                                Modifier
                                    .animateContentSize()
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(8.dp, 8.dp, 8.dp, 14.dp),
                                this@AppActivity,
                                true,
                                additionalActionsShow,
                                { newState -> viewModel.changeTheme(newState)},
                                theme,
                                { newState -> viewModel.changeTapOnClock(newState) },
                                tapOnClock,
                                { newState -> viewModel.changeNotificationEnabled(newState) },
                                notificationEnabled
                            )
                            DisplayButton(
                                this@AppActivity,
                                isRunning,
                                elapsedMs != 0L,
                                additionalActionsShow,
                                showAdditionals = { newState -> additionalActionsShow = newState },
                                notificationEnabled
                            )
                        }
                    } else {
                        Row(modifier = Modifier.padding(innerPadding)) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                DisplayTime(
                                    if (laps.isEmpty()) Modifier
                                        .animateContentSize()
                                        .fillMaxWidth()
                                        .weight(1F)
                                    else Modifier
                                        .animateContentSize()
                                        .fillMaxWidth()
                                        .heightIn(min = 100.dp),
                                    laps.isNotEmpty(),
                                    elapsedSec,
                                    elapsedMs
                                ) { clickOnClock(tapOnClock, isRunning, notificationEnabled) }
                                DisplayLaps(
                                    if (laps.isEmpty()) Modifier
                                        .animateContentSize()
                                    else Modifier
                                        .animateContentSize()
                                        .weight(1F),
                                    laps
                                )
                            }
                            Row {
                                DisplayActions(
                                    Modifier
                                        .animateContentSize()
                                        .fillMaxHeight()
                                        .wrapContentWidth()
                                        .padding(8.dp, 8.dp, 8.dp, 14.dp),
                                    this@AppActivity,
                                    false,
                                    additionalActionsShow,
                                    { newState -> viewModel.changeTheme(newState)},
                                    theme,
                                    { newState -> viewModel.changeTapOnClock(newState) },
                                    tapOnClock,
                                    { newState -> viewModel.changeNotificationEnabled(newState) },
                                    notificationEnabled
                                )
                                DisplayButtonInLandscape(
                                    this@AppActivity,
                                    isRunning,
                                    elapsedMs != 0L,
                                    additionalActionsShow,
                                    showAdditionals = { newState -> additionalActionsShow = newState },
                                    notificationEnabled
                                )
                            }
                        }
                    }
                }
            }
        }

        if (notificationEnabled) {
            val isRunning by StopwatchService.isRunningI.observeAsState(false)
            val elapsedMs by StopwatchService.elapsedMsI.observeAsState(0L)
            val elapsedSec by StopwatchService.elapsedSecI.observeAsState(0L)
            val laps by StopwatchService.lapsI.observeAsState(LinkedList())
            StopwatchScreen(isRunning, elapsedMs, elapsedSec, laps)
        } else {
            val isRunning by viewModel.isRunningI.observeAsState(false)
            val elapsedMs by viewModel.elapsedMsI.observeAsState(0L)
            val elapsedSec by viewModel.elapsedSecI.observeAsState(0L)
            val laps by viewModel.lapsI.observeAsState(LinkedList())
            StopwatchScreen(isRunning, elapsedMs, elapsedSec, laps)
        }
    }

    fun commandService(serviceState: StopwatchService.State) {
        val context = this
        val intent = Intent(context, StopwatchService::class.java)
        intent.action = serviceState.name
        context.startService(intent)
    }

    private fun clickOnClock(tapType: Int, isRunning: Boolean, notificationEnabled: Boolean) {
        when (tapType) {
            1 -> {
                if (isRunning) {
                    if (notificationEnabled) commandService(StopwatchService.State.PAUSE)
                    else viewModel.pause()
                } else {
                    if (notificationEnabled) commandService(StopwatchService.State.START_RESUME)
                    else viewModel.startResume()
                }
            }
            2 -> {
                if (isRunning) {
                    if (notificationEnabled) commandService(StopwatchService.State.ADD_LAP)
                    else viewModel.addLap()
                } else {
                    if (notificationEnabled) commandService(StopwatchService.State.START_RESUME)
                    else viewModel.startResume()
                }
            }
            3 -> {
                if (isRunning) {
                    if (notificationEnabled)
                        lifecycleScope.launch {
                            commandService(StopwatchService.State.PAUSE)
                            delay(20)
                            commandService(StopwatchService.State.RESET)
                        }
                    else
                        lifecycleScope.launch {
                            viewModel.pause()
                            delay(20)
                            viewModel.reset()
                        }
                } else {
                    if (notificationEnabled) commandService(StopwatchService.State.START_RESUME)
                    else viewModel.startResume()
                }
            }
        }
    }
}