package com.justdeax.composeStopwatch.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.justdeax.composeStopwatch.R
import com.justdeax.composeStopwatch.ui.theme.DarkColorScheme
import com.justdeax.composeStopwatch.util.Dimens
import com.justdeax.composeStopwatch.util.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DisplayButtons(
    modifier: Modifier,
    isPortrait: Boolean,
    isStarted: Boolean,
    isRunning: Boolean,
    additionalActionShow: Boolean,
    showHideAdditional: () -> Unit,
    reset: () -> Unit,
    startResume: () -> Unit,
    pause: () -> Unit,
    addLap: () -> Unit,
) {
    val startDrawable = painterResource(R.drawable.round_play_arrow_24)
    val pauseDrawable = painterResource(R.drawable.round_pause_24)
    val stopDrawable = painterResource(R.drawable.round_stop_24)
    val addLapsDrawable = painterResource(R.drawable.round_add_circle_24)
    val additionalDrawable = painterResource(R.drawable.round_grid_view_24)
    
    val buttonSizeAnimation by animateIntAsState(
        targetValue = if (isStarted) Dimens.iconButtonMinSize else Dimens.iconButtonMaxSize,
        animationSpec = keyframes { durationMillis = Duration.animShort },
        label = ""
    )

    @Composable
    fun InnerButtons() {
        val scope = rememberCoroutineScope()
        var addLapButtonClicked by remember { mutableStateOf(false) }

        IconButton(
            onClick = { showHideAdditional() },
            painter = additionalDrawable,
            contentDesc = stringResource(R.string.additional_action),
            state = additionalActionShow && isStarted
        )
        Spacer(
            if (isPortrait)
                Modifier.width(Dimens.iconButtonSpace.dp)
            else
                Modifier.height(Dimens.iconButtonSpace.dp))
        IconButton(
            onClick = {
                if (isRunning) {
                    addLap()
                    if (!addLapButtonClicked) {
                        scope.launch {
                            addLapButtonClicked = true
                            delay(Duration.animShort.milliseconds)
                            addLapButtonClicked = false
                        }
                    }
                } else {
                    reset()
                }
                      },
            painter = if (isRunning) addLapsDrawable else stopDrawable,
            contentDesc = stringResource(if (isRunning) R.string.add_lap else R.string.stop),
            state = addLapButtonClicked
        )
    }

    @Composable
    fun Buttons() {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isStarted,
                enter = EnterTransition.None,
                exit = fadeOut(tween(Duration.animLong))
            ) {
                if (isPortrait)
                    Row {
                        InnerButtons()
                    }
                else
                    Column {
                        InnerButtons()
                    }
            }
            IconButton(
                width = if (isPortrait) buttonSizeAnimation else Dimens.iconButtonSize,
                height = if (isPortrait) Dimens.iconButtonSize else buttonSizeAnimation,
                onClick = {
                    if (isRunning) pause()
                    else startResume()
                },
                painter = if (isRunning) pauseDrawable else startDrawable,
                contentDesc =
                    if (isRunning) stringResource(R.string.pause)
                    else stringResource(R.string.resume),
                state = !isRunning && isStarted
            )
        }
    }

    if (isPortrait)
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center
        ) {
            Buttons()
        }
    else
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.Center
        ) {
            Buttons()
        }
}

@Preview(showBackground = true)
@Composable
fun DisplayButtonsPreview() {
    MaterialTheme(colorScheme = DarkColorScheme) {
        DisplayButtons(
            Modifier,
            isPortrait = true,
            isStarted = true,
            isRunning = true,
            additionalActionShow = false,
            showHideAdditional = { },
            reset = { },
            startResume = { },
            pause = { },
            addLap = { }
        )
    }
}