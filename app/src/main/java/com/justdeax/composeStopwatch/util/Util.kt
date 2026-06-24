package com.justdeax.composeStopwatch.util

import android.os.VibrationEffect
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Lap(
    val index: Int,
    val elapsedTime: Long,
    val deltaLap: String
)

data class StopwatchState(
    val elapsedMsBeforePause: Long,
    val startTime: Long,
    val isRunning: Boolean
)

enum class StopwatchAction {
    START_RESUME, PAUSE, RESET, HARD_RESET, ADD_LAP
}

val startResumeVibration: VibrationEffect? = VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
val pauseVibration: VibrationEffect? = VibrationEffect.createOneShot(200, 80)
val resetVibration: VibrationEffect? = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
val addLapVibration: VibrationEffect? = VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1)

val enterAnimation =
    fadeIn(tween(Duration.animLong)) +
            slideInVertically(tween(500)) { -Dimens.iconButtonSize }
val exitAnimation =
    fadeOut(tween(Duration.animMedium)) +
            slideOutVertically(tween(300)) { -Dimens.iconButtonSize }