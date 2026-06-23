package com.justdeax.composeStopwatch.util

import android.os.VibrationEffect
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