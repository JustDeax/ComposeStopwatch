package com.justdeax.composeStopwatch.ui
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.justdeax.composeStopwatch.AppActivity
import com.justdeax.composeStopwatch.R
import com.justdeax.composeStopwatch.stopwatch.StopwatchService
import com.justdeax.composeStopwatch.ui.theme.Copper
import com.justdeax.composeStopwatch.ui.theme.Gold
import com.justdeax.composeStopwatch.ui.theme.Iron
import com.justdeax.composeStopwatch.ui.theme.Silver
import com.justdeax.composeStopwatch.util.Lap
import com.justdeax.composeStopwatch.util.StopWatchState
import com.justdeax.composeStopwatch.util.displayMs
import com.justdeax.composeStopwatch.util.formatSeconds
import com.justdeax.composeStopwatch.util.toFormatString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DisplayTime(
    modifier: Modifier,
    miniClock: Boolean,
    isPausing: Boolean,
    seconds: Long,
    milliseconds: Long
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val blinkAnimation by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .alpha(if (isPausing) blinkAnimation else 1f)
                .wrapContentWidth()
                .wrapContentHeight()
        ) {
            if (miniClock) {
                Text(
                    text = "${formatSeconds(seconds)}.",
                    fontSize = 60.sp,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = displayMs(milliseconds),
                    fontSize = 40.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.offset(y = 30.dp)
                )
            } else {
                Text(
                    text = "${formatSeconds(seconds)}.",
                    fontSize = 90.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = displayMs(milliseconds),
                    fontSize = 60.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.offset(y = 45.dp)
                )
            }
        }
    }
}

@Composable
fun DisplayLaps(
    modifier: Modifier,
    laps: List<Lap>,
    elapsedMs: Long
) {
    val heightAnimation by animateFloatAsState(
        targetValue = if (laps.isEmpty()) 0.0001f else 1f,
        animationSpec = tween(500),
        label = ""
    )
    Row(modifier = modifier.fillMaxHeight(heightAnimation)) {
        LazyColumn {
            if (laps.isNotEmpty())
                item {
                    val deltaLap = "+ ${(elapsedMs - laps.first().elapsedTime).toFormatString()}"
                    LapItem("+", MaterialTheme.colorScheme.onBackground, elapsedMs, deltaLap)
                }
            items(laps, key = { laps[it.index-1].index }) { (index, elapsedTime, deltaLap) ->
                val indexColor = when (index) {
                    1 -> Gold
                    2 -> Silver
                    3 -> Copper
                    else -> Iron
                }
                LapItem(index.toString(), indexColor, elapsedTime, deltaLap)
            }
        }
    }
}

@Composable
fun LapItem(indexText: String, indexColor: Color, elapsedTime: Long, deltaLap: String) {
    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = indexText,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = indexColor
        )
        Text(
            modifier = Modifier.weight(2f),
            text = elapsedTime.toFormatString(),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Normal
        )
        Text(
            modifier = Modifier.weight(2f),
            text = deltaLap,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.End,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayAppName(
    modifier: Modifier,
    activity: AppActivity,
    show: Boolean
) {
    val helpDraw = painterResource(R.drawable.round_help_outline_24)
    var showAboutApp by remember { mutableStateOf(false) }

    androidx.compose.animation.AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -40 },
        exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { -40 }
    ) {
        Row(
            modifier = modifier.clickable(
                remember { MutableInteractionSource() }, null
            ) { showAboutApp = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = activity.getString(R.string.app_name),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                modifier = Modifier.size(24.dp),
                painter = helpDraw,
                contentDescription = activity.getString(R.string.about_app),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    if (showAboutApp) {
        ModalBottomSheet(
            onDismissRequest = { showAboutApp = false },
            sheetState = sheetState,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp, 2.dp)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = activity.getString(R.string.about_app),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = activity.getString(R.string.about_app_desc),
                        style = MaterialTheme.typography.titleMedium
                    )
                    val annotatedString = buildAnnotatedString {
                        append(activity.getString(R.string.about_app_desc_a))
                        withStyle(
                            style = SpanStyle(
                                color = Color.Blue,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(" " + activity.getString(R.string.app_author))
                        }
                        append(activity.getString(R.string.about_app_desc_v))
                        append(" " + activity.getString(R.string.app_version))
                    }
                    Text(
                        text = annotatedString,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JustDeax"))
                            activity.startActivity(intent)
                        }
                    )
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    onClick = {
                        scope
                            .launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible)
                                    showAboutApp = false
                            }
                    }
                ) {
                    Text(activity.getString(R.string.ok))
                }
            }
        }
    }
}

@Composable
fun DisplayActions(
    modifier: Modifier,
    activity: AppActivity,
    isPortrait: Boolean,
    show: Boolean,
    notificationEnabled: Boolean
) {
    val tapOnClockDraw = painterResource(R.drawable.round_adjust_24)
    val turnOffNotifDraw = painterResource(R.drawable.round_notifications_24)
    val turnOnNotifDraw = painterResource(R.drawable.round_notifications_none_24)
    val themeDraw = painterResource(R.drawable.round_invert_colors_24)
    val multiStopwatchDraw = painterResource(R.drawable.round_casino_24)

    var showTapOnClockDialog by remember { mutableStateOf(false) }
    var showResetStopwatchDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showMultiStopwatchDialog by remember { mutableStateOf(false) }

    @Composable
    fun actionDialogs(modifier: Modifier) {
        OutlineIconButton(
            modifier = modifier,
            onClick = {
                activity.viewModel.saveStopwatch()
                showTapOnClockDialog = true
            },
            painter = tapOnClockDraw,
            contentDesc = activity.getString(R.string.tap_on_clock)
        )
        OutlineIconButton(
            modifier = modifier,
            onClick = {
                activity.viewModel.saveStopwatch()
                showResetStopwatchDialog = true
            },
            painter = if (notificationEnabled) turnOffNotifDraw else turnOnNotifDraw,
            contentDesc = activity.getString(R.string.turn_off_notif)
        )
        OutlineIconButton(
            modifier = modifier,
            onClick = {
                activity.viewModel.saveStopwatch()
                showThemeDialog = true
            },
            painter = themeDraw,
            contentDesc = activity.getString(R.string.theme)
        )
        OutlineIconButton(
            modifier = modifier,
            onClick = {
                activity.viewModel.saveStopwatch()
                showMultiStopwatchDialog = true
            },
            painter = multiStopwatchDraw,
            contentDesc = activity.getString(R.string.multi_stopwatch)
        )
    }

    if (showTapOnClockDialog) {
        RadioDialog(
            title = activity.getString(R.string.change_tap_on_clock),
            desc = activity.getString(R.string.change_tap_on_clock_desc),
            isPortrait = isPortrait,
            defaultIndex = activity.viewModel.tapOnClock.value!!,
            options = activity.resources.getStringArray(R.array.tap_on_clock),
            setSelectedIndex = { newState -> activity.viewModel.changeTapOnClock(newState)},
            onDismiss = { showTapOnClockDialog = false },
            onConfirm = { showTapOnClockDialog = false },
            confirmText = activity.getString(R.string.apply)
        )
    }
    if (showResetStopwatchDialog) {
        if (notificationEnabled && StopwatchService.elapsedMsI.value!! == 0L) {
            activity.viewModel.changeNotificationEnabled(false)
        } else if (!notificationEnabled && activity.viewModel.elapsedMsI.value!! == 0L) {
            activity.viewModel.changeNotificationEnabled(true)
        } else {
            SimpleDialog(
                title = activity.getString(R.string.reset_stopwatch),
                desc = if (notificationEnabled)
                    activity.getString(R.string.reset_stopwatch_desc_disable)
                else
                    activity.getString(R.string.reset_stopwatch_desc_enable),
                isPortrait = isPortrait,
                confirmText = activity.getString(R.string.ok),
                onConfirm = {
                    if (notificationEnabled)
                        activity.lifecycleScope.launch {
                            activity.commandService(StopWatchState.PAUSE)
                            delay(30)
                            activity.commandService(StopWatchState.RESET)
                            activity.viewModel.changeNotificationEnabled(false)
                            showResetStopwatchDialog = false
                        }
                    else
                        activity.lifecycleScope.launch {
                            activity.viewModel.pause()
                            delay(30)
                            activity.viewModel.reset()
                            activity.viewModel.changeNotificationEnabled(true)
                            showResetStopwatchDialog = false
                        }
                },
                dismissText = activity.getString(R.string.cancel),
                onDismiss = { showResetStopwatchDialog = false }
            )
        }
    }
    if (showThemeDialog) {
        RadioDialog(
            title = activity.getString(R.string.change_theme),
            isPortrait = isPortrait,
            desc = activity.getString(R.string.change_theme_desc),
            defaultIndex = activity.viewModel.theme.value!!,
            options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                activity.resources.getStringArray(R.array.theme12)
            else
                activity.resources.getStringArray(R.array.theme),
            setSelectedIndex = { newState -> activity.viewModel.changeTheme(newState)},
            onDismiss = { showThemeDialog = false },
            onConfirm = { showThemeDialog = false },
            confirmText = activity.getString(R.string.apply)
        )
    }
    if (showMultiStopwatchDialog) {
        OkayDialog(
            title = activity.getString(R.string.multi_stopwatch_not_available),
            content = {
                Text(
                    text = activity.getString(R.string.multi_stopwatch_not_available_desc),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            isPortrait = isPortrait,
            confirmText = activity.getString(R.string.ok),
            onConfirm = { showMultiStopwatchDialog = false }
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
            ) { actionDialogs(Modifier.weight(1f)) }
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
            ) { actionDialogs(Modifier.weight(1f)) }
        }
}

@Composable
fun DisplayButton(
    activity: AppActivity,
    isStarted: Boolean,
    isRunning: Boolean,
    showAdditional: Boolean,
    showAdditionals: (Boolean) -> Unit,
    notificationEnabled: Boolean
) {
    val startDrawable = painterResource(R.drawable.round_play_arrow_24)
    val pauseDrawable = painterResource(R.drawable.round_pause_24)
    val stopDrawable = painterResource(R.drawable.round_stop_24)
    val addLapsDrawable = painterResource(R.drawable.round_add_circle_24)
    val additionalsDrawable = painterResource(R.drawable.round_grid_view_24)
    val startButtonSizeAnimation by animateIntAsState(
        targetValue = if (isStarted) 120 else 300,
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
                visible = isStarted,
                enter = EnterTransition.None,
                exit = fadeOut(tween(500))
            ) {
                Row {
                    IconButton(
                        onClick = { showAdditionals(!showAdditional) },
                        painter = additionalsDrawable,
                        contentDesc = activity.getString(R.string.additional_action)
                    )
                    Spacer(Modifier.width(170.dp))
                    if (isRunning)
                        IconButton(
                            onClick = {
                                if (notificationEnabled)
                                    activity.commandService(StopWatchState.ADD_LAP)
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
                                    activity.commandService(StopWatchState.RESET)
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
                width = startButtonSizeAnimation,
                onClick = {
                    if (isRunning) {
                        if (notificationEnabled) activity.commandService(StopWatchState.PAUSE)
                        else activity.viewModel.pause()
                    } else {
                        if (!isStarted) showAdditionals(false)
                        if (notificationEnabled) activity.commandService(StopWatchState.START_RESUME)
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
    isStarted: Boolean,
    isRunning: Boolean,
    showAdditional: Boolean,
    showAdditionals: (Boolean) -> Unit,
    notificationEnabled: Boolean
) {
    val startDrawable = painterResource(R.drawable.round_play_arrow_24)
    val pauseDrawable = painterResource(R.drawable.round_pause_24)
    val stopDrawable = painterResource(R.drawable.round_stop_24)
    val addLapsDrawable = painterResource(R.drawable.round_add_circle_24)
    val additionalsDrawable = painterResource(R.drawable.round_grid_view_24)
    val startButtonSizeAnimation by animateIntAsState(
        targetValue = if (isStarted) 120 else 300,
        animationSpec = keyframes { durationMillis = 250 },
        label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .height(IntrinsicSize.Min)
            .padding(start = 50.dp, end = 20.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isStarted,
                enter = EnterTransition.None,
                exit = fadeOut(tween(500))
            ) {
                Column {
                    IconButton(
                        onClick = { showAdditionals(!showAdditional) },
                        painter = additionalsDrawable,
                        contentDesc = activity.getString(R.string.additional_action)
                    )
                    Spacer(Modifier.height(170.dp))
                    if (isRunning)
                        IconButton(
                            onClick = {
                                if (notificationEnabled)
                                    activity.commandService(StopWatchState.ADD_LAP)
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
                                    activity.commandService(StopWatchState.RESET)
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
                height = startButtonSizeAnimation,
                onClick = {
                    if (isRunning) {
                        if (notificationEnabled) activity.commandService(StopWatchState.PAUSE)
                        else activity.viewModel.pause()
                    } else {
                        if (!isStarted) showAdditionals(false)
                        if (notificationEnabled) activity.commandService(StopWatchState.START_RESUME)
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