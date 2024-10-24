package com.justdeax.composeStopwatch.util
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Immutable
import com.justdeax.composeStopwatch.stopwatch.StopwatchService
import kotlinx.serialization.Serializable

const val TAG = "DEAX_TAG"

//STOPWATCH DATA AND ACTION ------------------

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

fun Context.commandService(serviceState: StopwatchAction) {
    val intent = Intent(this, StopwatchService::class.java)
    intent.action = serviceState.name
    this.startService(intent)
}

enum class StopwatchAction {
    START_RESUME, PAUSE, RESET, HARD_RESET, ADD_LAP
}