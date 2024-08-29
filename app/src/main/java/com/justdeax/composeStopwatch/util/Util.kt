package com.justdeax.composeStopwatch.util
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.util.Locale

fun displayMs(time: Long)
    = String.format(Locale.US, "%03d", (time % 1000)).substring(0, 2)

fun formatSeconds(timeInSeconds: Long): String {
    val seconds = timeInSeconds % 60
    val minutes = timeInSeconds / 60 % 60
    val hours = timeInSeconds / 60 / 60
    return if (hours != 0L)
        String.format(Locale.US, "%01d:%02d:%02d", hours, minutes, seconds)
    else
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

fun Long.toFormatString()
    = formatSeconds(this / 1000) + "." + displayMs(this)

@Immutable
@Serializable
data class Lap(
    val index: Int,
    val elapsedTime: Long,
    val deltaLap: String
)

enum class StopWatchState {
    START_RESUME, PAUSE, RESET, ADD_LAP
}

data class StopwatchState(
    val elapsedMsBeforePause: Long,
    val startTime: Long,
    val isRunning: Boolean,
    val laps: String
)