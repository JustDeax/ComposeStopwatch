package com.justdeax.composeStopwatch.util

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