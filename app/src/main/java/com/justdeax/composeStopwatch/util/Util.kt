package com.justdeax.composeStopwatch.util
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Immutable
import com.justdeax.composeStopwatch.stopwatch.StopwatchService
import kotlinx.serialization.Serializable
import java.util.Locale

const val TAG = "DEAX_TAG"

//GENERAL ------------------------------------

fun Long.cutToMs()
    = String.format(Locale.US, "%02d", (this % 1000)).substring(0, 2)

//PORTRAIT -----------------------------------

fun Long.formatSeconds(): String {
    val seconds = this % 60
    val minutes = this / 60 % 60
    val result = if (this >= 3600)
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    else if (minutes != 0L)
        String.format(Locale.US, "%01d:%02d", minutes, seconds)
    else
        String.format(Locale.US, "%01d", seconds)
    return "$result."
}

fun Long.getHours() = "${this/60/60}"

//LANDSCAPE ----------------------------------

fun Long.formatSecondsWithHours(): String {
    val seconds = this % 60
    val minutes = this / 60 % 60
    val hours = this / 60 / 60
    val result = if (hours != 0L)
        String.format(Locale.US, "%01d:%02d:%02d", hours, minutes, seconds)
    else if (minutes != 0L)
        String.format(Locale.US, "%01d:%02d", minutes, seconds)
    else
        String.format(Locale.US, "%01d", seconds)
    return "$result."
}

//FOR LAPS AND NOTIFICATION ------------------

fun Long.fullFormatSeconds(): String {
    val seconds = this % 60
    val minutes = this / 60 % 60
    val hours = this / 60 / 60
    return if (hours != 0L)
        String.format(Locale.US, "%01d:%02d:%02d", hours, minutes, seconds)
    else
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

fun Long.toFormatString()
    = (this / 1000).fullFormatSeconds() + "." + this.cutToMs()

//STOPWATCH DATA AND ACTION ------------------

@Immutable
@Serializable
data class Lap(
    val index: Int,
    val elapsedTime: Long,
    val deltaLap: String
)

fun Context.commandService(serviceState: StopwatchAction) {
    val intent = Intent(this, StopwatchService::class.java)
    intent.action = serviceState.name
    this.startService(intent)
}

enum class StopwatchAction {
    START_RESUME, PAUSE, RESET, HARD_RESET, ADD_LAP
}

data class StopwatchState(
    val elapsedMsBeforePause: Long,
    val startTime: Long,
    val isRunning: Boolean
)