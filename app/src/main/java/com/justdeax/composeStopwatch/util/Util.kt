package com.justdeax.composeStopwatch.util
import kotlinx.serialization.Serializable
import java.util.Locale

fun displayMs(elapsedTime: Long)
    = String.format(Locale.US, "%03d", (elapsedTime % 1000)).substring(0, 2)

fun formatSeconds(elapsedSeconds: Long): String {
    val seconds = elapsedSeconds % 60
    val minutes = elapsedSeconds / 60 % 60
    val hours = elapsedSeconds / 60 / 60
    return if (hours != 0L)
        String.format(Locale.US, "%01d:%02d:%02d", hours, minutes, seconds)
    else
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

//@Immutable
@Serializable
data class Lap(
    val index: Int,
    val elapsedTime: Long,
    val deltaLap: String
)

data class StopwatchState(
    val elapsedMsBeforePause: Long,
    val startTime: Long,
    val isRunning: Boolean,
    val laps: String
)