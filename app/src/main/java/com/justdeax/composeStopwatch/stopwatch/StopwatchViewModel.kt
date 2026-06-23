package com.justdeax.composeStopwatch.stopwatch

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.justdeax.composeStopwatch.util.DataStoreManager
import com.justdeax.composeStopwatch.util.Lap
import com.justdeax.composeStopwatch.util.StopwatchState
import com.justdeax.composeStopwatch.util.toFormatString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.LinkedList
import kotlin.time.Duration.Companion.milliseconds

class StopwatchViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
    private var elapsedMsBeforePause = 0L
    private var startTime = 0L
    val tapOnClock = dataStoreManager.getTapOnClock().asLiveData()
    val autoStartEnabled = dataStoreManager.autoStartEnabled().asLiveData()
    val vibrationEnabled = dataStoreManager.vibrationEnabled().asLiveData()
    val notificationEnabled = dataStoreManager.notificationEnabled().asLiveData()
    val lockAwakeEnabled = dataStoreManager.lockAwakeEnabled().asLiveData()
    val lockAwakeFirstTimeEnabled = dataStoreManager.lockAwakeEnabledFirstTime().asLiveData()
    val theme = dataStoreManager.getTheme().asLiveData()
    val firstBoot = dataStoreManager.firstBoot().asLiveData()

    fun changeTapOnClock(tapType: Int) = viewModelScope.launch {
        dataStoreManager.changeTapOnClock(tapType)
    }

    fun changeAutoStartEnabled(enabled: Boolean) = viewModelScope.launch {
        dataStoreManager.changeAutoStartEnabled(enabled)
    }

    fun changeVibrationEnabled(enabled: Boolean) = viewModelScope.launch {
        dataStoreManager.changeVibrationEnabled(enabled)
    }

    fun changeNotificationEnabled(enabled: Boolean) = viewModelScope.launch {
        try {
            Log.w("TAG", "changeNotificationEnabled from <: ${notificationEnabled.value}")
            dataStoreManager.changeNotificationEnabled(enabled)
            Log.w("TAG", "changeNotificationEnabled to >: ${notificationEnabled.value}")
        } catch (e: Exception) {
            Log.e("TAG", "Error changing notification enabled", e)
        }
    }

    fun changeTheme(themeCode: Int) = viewModelScope.launch {
        dataStoreManager.changeTheme(themeCode)
    }

    fun changeLockAwakeEnabled(enabled: Boolean) = viewModelScope.launch {
        dataStoreManager.changeLockAwakeEnabled(enabled)
    }

    fun changeLockAwakeFirstTimeEnabled(enabled: Boolean) = viewModelScope.launch {
        dataStoreManager.changeLockAwakeFirstTimeEnabled(enabled)
    }

    fun disableFirstBoot() = viewModelScope.launch {
        dataStoreManager.changeFirstBoot(false)
    }

    private suspend fun saveStopwatch() {
        if (notificationEnabled.value == false) {
            dataStoreManager.saveStopwatch(
                StopwatchState(
                    elapsedMsBeforePause,
                    startTime,
                    isRunning.value ?: false
                )
            )
        }
    }

    private var restoreJob: Job? = null

    fun restoreStopwatch() {
        restoreJob?.cancel()
        restoreJob = viewModelScope.launch {
            try {
                val lapsData = dataStoreManager.restoreLaps().first()
                this@StopwatchViewModel.laps.value = if (lapsData.isNotEmpty())
                    LinkedList(Json.decodeFromString<List<Lap>>(lapsData))
                else LinkedList()
                
                val restoredState = dataStoreManager.restoreStopwatch().first()
                elapsedMsBeforePause = restoredState.elapsedMsBeforePause
                startTime = restoredState.startTime
                isRunning.value = restoredState.isRunning
                if (isRunning.value == true) {
                    val currentTime = System.currentTimeMillis()
                    elapsedMsBeforePause += currentTime - startTime
                    startTime = currentTime
                    startResume()
                } else {
                    elapsedMs.value = elapsedMsBeforePause
                    elapsedSec.value = elapsedMsBeforePause / 1000
                }
                isStarted.value = elapsedMsBeforePause != 0L
            } catch (e: Exception) {
                Log.e("TAG", "Error restoring stopwatch", e)
            }
        }
    }

    private var timerJob: Job? = null

    fun startResume() {
        if (isStarted.value == true && isRunning.value == true && timerJob?.isActive == true) return
        isStarted.value = true
        isRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch(Dispatchers.Main) {
            if (startTime == 0L) startTime = System.currentTimeMillis()
            saveStopwatch()
            while (isRunning.value == true) {
                val currentMs = (System.currentTimeMillis() - startTime) + elapsedMsBeforePause
                elapsedMs.value = currentMs
                val seconds = currentMs / 1000
                if (elapsedSec.value != seconds) elapsedSec.value = seconds
                delay(10.milliseconds)
            }
        }
    }

    fun pause() {
        isRunning.value = false
        timerJob?.cancel()
        elapsedMsBeforePause = elapsedMs.value ?: 0L
        startTime = 0L
        viewModelScope.launch {
            saveStopwatch()
        }
    }

    fun reset() {
        isStarted.value = false
        isRunning.value = false
        timerJob?.cancel()
        restoreJob?.cancel()
        elapsedMs.value = 0L
        elapsedSec.value = 0L
        elapsedMsBeforePause = 0L
        laps.value?.clear()
        previousLapDelta.value = 1L
        viewModelScope.launch {
            dataStoreManager.resetStopwatch()
        }
    }

    fun hardReset() {
        pause()
        viewModelScope.launch {
            delay(10.milliseconds)
            reset()
        }
    }

    fun addLap() {
        viewModelScope.launch(Dispatchers.Main) {
            val currentElapsed = elapsedMs.value ?: 0L
            val deltaLap = if (laps.value?.isEmpty() == true)
                currentElapsed
            else
                currentElapsed - (laps.value?.firstOrNull()?.elapsedTime ?: 0L)
            
            val deltaLapString = "+ ${deltaLap.toFormatString()}"
            val currentLaps = laps.value ?: LinkedList()
            val newLaps = LinkedList(currentLaps)
            newLaps.addFirst(Lap(newLaps.size + 1, currentElapsed, deltaLapString))
            laps.value = newLaps
            previousLapDelta.value = deltaLap
            dataStoreManager.saveLaps(Json.encodeToString(newLaps.toList()))
        }
    }

    private val isStarted = MutableLiveData(false)
    private val isRunning = MutableLiveData(false)
    private val elapsedMs = MutableLiveData(0L)
    private val elapsedSec = MutableLiveData(0L)
    private val laps = MutableLiveData<LinkedList<Lap>>(LinkedList())

    val isStartedI: LiveData<Boolean> get() = isStarted
    val isRunningI: LiveData<Boolean> get() = isRunning
    val elapsedMsI: LiveData<Long> get() = elapsedMs
    val elapsedSecI: LiveData<Long> get() = elapsedSec
    val lapsI: LiveData<LinkedList<Lap>> get() = laps

    val previousLapDelta = MutableLiveData(1L)
}
