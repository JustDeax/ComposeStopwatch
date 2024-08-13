package com.justdeax.composeStopwatch.stopwatch
import android.os.Build
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.justdeax.composeStopwatch.AppActivity
import com.justdeax.composeStopwatch.R
import com.justdeax.composeStopwatch.ui.IconButton
import com.justdeax.composeStopwatch.ui.IconButton2
import com.justdeax.composeStopwatch.ui.OkayDialog
import com.justdeax.composeStopwatch.ui.OutlineIconButton
import com.justdeax.composeStopwatch.ui.RadioDialog
import com.justdeax.composeStopwatch.ui.SimpleDialog
import com.justdeax.composeStopwatch.ui.theme.Copper
import com.justdeax.composeStopwatch.ui.theme.Gold
import com.justdeax.composeStopwatch.ui.theme.Iron
import com.justdeax.composeStopwatch.ui.theme.Silver
import com.justdeax.composeStopwatch.util.Lap
import com.justdeax.composeStopwatch.util.displayMs
import com.justdeax.composeStopwatch.util.formatSeconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DisplayTime(
    modifier: Modifier,
    elapsedSec: Long,
    elapsedMs: Long,
    clickOnClock: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { clickOnClock() }
        ) {
            Text(
                text = "${formatSeconds(elapsedSec)}.",
                fontSize = 50.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(modifier = Modifier.offset(y = 20.dp),
                text = displayMs(elapsedMs),
                fontSize = 40.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun DisplayLaps(
    modifier: Modifier,
    laps: List<Lap>
) {
    Row(modifier = modifier) {
        LazyColumn {
            items(laps, key = { laps[it.index-1].index }) { (index, elapsedTime, deltaLap) ->
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                ) {
                    val indexColor = when (index) {
                        1 -> Gold
                        2 -> Silver
                        3 -> Copper
                        else -> Iron
                    }
                    Text(
                        modifier = Modifier.weight(1f),
                        text = index.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = indexColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        modifier = Modifier.weight(2f),
                        text = "${formatSeconds(elapsedTime / 1000)}.${displayMs(elapsedTime)}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        modifier = Modifier.weight(2f),
                        text = deltaLap,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DisplayActions(
    modifier: Modifier,
    activity: AppActivity,
    isPortrait: Boolean,
    show: Boolean,
    changeTheme: (Int) -> Unit,
    themeCode: Int,
    changeTapOnClock: (Int) -> Unit,
    tapOnClock: Int,
    toggleNotification: (Boolean) -> Unit,
    notificationEnabled: Boolean
) {
    val multiStopwatchDraw = painterResource(R.drawable.round_casino_24)
    val themeDraw = painterResource(R.drawable.round_invert_colors_24)
    val tapOnClockDraw = painterResource(R.drawable.round_adjust_24)
    val turnOffNotifDraw = painterResource(R.drawable.round_notifications_24)
    val turnOnNotifDraw = painterResource(R.drawable.round_notifications_none_24)

    var showMultiStopwatchDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showTapOnClockDialog by remember { mutableStateOf(false) }
    var showResetStopwatchDialog by remember { mutableStateOf(false) }

    @Composable
    fun actionDialogs() {
        if (show) {
            OutlineIconButton(
                onClick = {
                    activity.viewModel.saveStopwatch()
                    showMultiStopwatchDialog = true
                },
                painter = multiStopwatchDraw,
                contentDesc = activity.getString(R.string.multi_stopwatch)
            )
            OutlineIconButton(
                onClick = {
                    activity.viewModel.saveStopwatch()
                    showThemeDialog = true
                },
                painter = themeDraw,
                contentDesc = activity.getString(R.string.theme)
            )
            OutlineIconButton(
                onClick = {
                    activity.viewModel.saveStopwatch()
                    showTapOnClockDialog = true
                },
                painter = tapOnClockDraw,
                contentDesc = activity.getString(R.string.tap_on_clock)
            )
            OutlineIconButton(
                onClick = {
                    activity.viewModel.saveStopwatch()
                    showResetStopwatchDialog = true
                },
                painter = if (notificationEnabled) turnOffNotifDraw else turnOnNotifDraw,
                contentDesc = activity.getString(R.string.turn_off_notif)
            )
        }
    }

    if (showMultiStopwatchDialog) {
        OkayDialog(
            title = activity.getString(R.string.multi_stopwatch_not_available),
            desc = activity.getString(R.string.multi_stopwatch_not_available_desc),
            confirmText = activity.getString(R.string.ok),
            onConfirm = { showMultiStopwatchDialog = false }
        )
    }
    if (showThemeDialog) {
        RadioDialog(
            title = activity.getString(R.string.change_theme),
            desc = "",
            defaultIndex = themeCode,
            options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                activity.resources.getStringArray(R.array.theme12)
            else
                activity.resources.getStringArray(R.array.theme),
            setSelectedIndex = { newState -> changeTheme(newState)},
            onDismiss = { showThemeDialog = false },
            onConfirm = { showThemeDialog = false },
            confirmText = activity.getString(R.string.apply)
        )
    }
    if (showTapOnClockDialog) {
        RadioDialog(
            title = activity.getString(R.string.change_tap_on_clock),
            desc = activity.getString(R.string.change_tap_on_clock_desc),
            defaultIndex = tapOnClock,
            options = activity.resources.getStringArray(R.array.tap_on_clock),
            setSelectedIndex = { newState -> changeTapOnClock(newState)},
            onDismiss = { showTapOnClockDialog = false },
            onConfirm = { showTapOnClockDialog = false },
            confirmText = activity.getString(R.string.apply)
        )
    }
    if (showResetStopwatchDialog) {
        if (notificationEnabled && StopwatchService.elapsedMsI.value!! == 0L) {
            toggleNotification(false)
        } else if (!notificationEnabled && activity.viewModel.elapsedMsI.value!! == 0L) {
            toggleNotification(true)
        } else {
            SimpleDialog(
                activity.getString(R.string.reset_stopwatch),
                if (notificationEnabled)
                    activity.getString(R.string.reset_stopwatch_desc_disable)
                else
                    activity.getString(R.string.reset_stopwatch_desc_enable),
                activity.getString(R.string.ok), {
                    if (notificationEnabled)
                        activity.lifecycleScope.launch {
                            activity.commandService(StopwatchService.State.PAUSE)
                            delay(20)
                            activity.commandService(StopwatchService.State.RESET)
                            toggleNotification(false)
                            showResetStopwatchDialog = false
                        }
                    else
                        activity.lifecycleScope.launch {
                            activity.viewModel.pause()
                            delay(20)
                            activity.viewModel.reset()
                            toggleNotification(true)
                            showResetStopwatchDialog = false
                        }
                },
                activity.getString(R.string.cancel), { showResetStopwatchDialog = false }
            )
        }
    }

    if (isPortrait)
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) { actionDialogs() }
    else
        FlowColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.SpaceEvenly
        ) { actionDialogs() }
}

@Composable
fun DisplayButton(
    activity: AppActivity,
    isRunning: Boolean,
    timerStarted: Boolean,
    showAdditional: Boolean,
    showAdditionals: (Boolean) -> Unit,
    notificationEnabled: Boolean
) {
    val startDrawable = painterResource(R.drawable.round_play_arrow_24)
    val pauseDrawable = painterResource(R.drawable.round_pause_24)
    val stopDrawable = painterResource(R.drawable.round_stop_24)
    val addLapsDrawable = painterResource(R.drawable.round_add_circle_24)
    val additionalsDrawable = painterResource(R.drawable.round_grid_view_24)
    val startButtonWidth by animateIntAsState(
        targetValue = if (timerStarted) 120 else 300,
        animationSpec = keyframes { durationMillis = 250 },
        label = ""
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(top = 20.dp, bottom = 50.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.animation.AnimatedVisibility(
                visible = timerStarted,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row {
                    IconButton(
                        onClick = { showAdditionals(!showAdditional) },
                        painter = additionalsDrawable,
                        contentDesc = activity.getString(R.string.additional_action)
                    )
                    Spacer(modifier = Modifier.width(170.dp))
                    if (isRunning)
                        IconButton(
                            onClick = {
                                if (notificationEnabled)
                                    activity.commandService(StopwatchService.State.ADD_LAP)
                                else
                                    activity.viewModel.addLap()
                            },
                            painter = addLapsDrawable,
                            contentDesc = activity.getString(R.string.add_lap)
                        )
                    else
                        IconButton(
                            onClick = {
                                if (notificationEnabled)
                                    activity.commandService(StopwatchService.State.RESET)
                                else
                                    activity.viewModel.reset()
                                showAdditionals(true)
                            },
                            painter = stopDrawable,
                            contentDesc = activity.getString(R.string.stop)
                        )
                }
            }
            IconButton(
                width = startButtonWidth,
                onClick = {
                    if (isRunning) {
                        if (notificationEnabled) activity.commandService(StopwatchService.State.PAUSE)
                        else activity.viewModel.pause()
                    } else {
                        if (!timerStarted) showAdditionals(false)
                        if (notificationEnabled) activity.commandService(StopwatchService.State.START_RESUME)
                        else activity.viewModel.startResume()
                    }
                },
                painter = if (isRunning) pauseDrawable else startDrawable,
                contentDesc =
                    if (isRunning) activity.getString(R.string.pause)
                    else activity.getString(R.string.resume)
            )
        }
    }
}

@Composable
fun DisplayButtonInLandscape(
    activity: AppActivity,
    isRunning: Boolean,
    timerStarted: Boolean,
    showAdditional: Boolean,
    showAdditionals: (Boolean) -> Unit,
    notificationEnabled: Boolean
) {
    val startDrawable = painterResource(R.drawable.round_play_arrow_24)
    val pauseDrawable = painterResource(R.drawable.round_pause_24)
    val stopDrawable = painterResource(R.drawable.round_stop_24)
    val addLapsDrawable = painterResource(R.drawable.round_add_circle_24)
    val additionalsDrawable = painterResource(R.drawable.round_grid_view_24)
    val startButtonHeight by animateIntAsState(
        targetValue = if (timerStarted) 120 else 300,
        animationSpec = keyframes { durationMillis = 250 },
        label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(IntrinsicSize.Min)
            .padding(start = 20.dp, end = 50.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.animation.AnimatedVisibility(
                visible = timerStarted,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    IconButton(
                        onClick = { showAdditionals(!showAdditional) },
                        painter = additionalsDrawable,
                        contentDesc = activity.getString(R.string.additional_action)
                    )
                    Spacer(modifier = Modifier.height(170.dp))
                    if (isRunning)
                        IconButton(
                            onClick = {
                                if (notificationEnabled)
                                    activity.commandService(StopwatchService.State.ADD_LAP)
                                else
                                    activity.viewModel.addLap()
                            },
                            painter = addLapsDrawable,
                            contentDesc = activity.getString(R.string.add_lap)
                        )
                    else
                        IconButton(
                            onClick = {
                                if (notificationEnabled)
                                    activity.commandService(StopwatchService.State.RESET)
                                else
                                    activity.viewModel.reset()
                                showAdditionals(true)
                            },
                            painter = stopDrawable,
                            contentDesc = activity.getString(R.string.stop)
                        )
                }
            }
            IconButton2(
                height = startButtonHeight,
                onClick = {
                    if (isRunning) {
                        if (notificationEnabled) activity.commandService(StopwatchService.State.PAUSE)
                        else activity.viewModel.pause()
                    } else {
                        if (!timerStarted) showAdditionals(false)
                        if (notificationEnabled) activity.commandService(StopwatchService.State.START_RESUME)
                        else activity.viewModel.startResume()
                    }
                },
                painter = if (isRunning) pauseDrawable else startDrawable,
                contentDesc =
                    if (isRunning) activity.getString(R.string.pause)
                    else activity.getString(R.string.resume)
            )
        }
    }
}