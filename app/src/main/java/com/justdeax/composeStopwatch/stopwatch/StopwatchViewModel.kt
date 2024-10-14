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
import com.justdeax.composeStopwatch.util.TAG
import com.justdeax.composeStopwatch.util.toFormatString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.LinkedList

class StopwatchViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
    private var elapsedMsBeforePause = 0L
    private var startTime = 0L
    val theme = dataStoreManager.getTheme().asLiveData()
    val tapOnClock = dataStoreManager.getTapOnClock().asLiveData()
    val notificationEnabled = dataStoreManager.notificationEnabled().asLiveData()
    val lockAwakeEnabled = dataStoreManager.lockAwakeEnabled().asLiveData()

    fun changeTheme(themeCode: Int) = viewModelScope.launch {
        dataStoreManager.changeTheme(themeCode)
    }

    fun changeTapOnClock(tapType: Int) = viewModelScope.launch {
        dataStoreManager.changeTapOnClock(tapType)
    }

    fun changeNotificationEnabled(enabled: Boolean) = viewModelScope.launch {
        dataStoreManager.changeNotificationEnabled(enabled)
    }

    fun changeLockAwakeEnabled(enabled: Boolean) = viewModelScope.launch {
        dataStoreManager.changeLockAwakeEnabled(enabled)
    }

    fun saveStopwatch() {
        if (!notificationEnabled.value!!)
            viewModelScope.launch {
                dataStoreManager.saveStopwatch(
                    StopwatchState(
                        elapsedMsBeforePause,
                        startTime,
                        isRunning.value!!
                    )
                )
            }
    }

    fun restoreStopwatch() {
        viewModelScope.launch {
            Log.d(TAG, "restoreStopwatch: -1")

            Log.d(TAG, laps.toString() + "restoreStopwatch ++")

            val laps = dataStoreManager.restoreLaps().first()
            this@StopwatchViewModel.laps.value = if (laps.isNotEmpty())
                LinkedList(Json.decodeFromString<List<Lap>>(laps))
            else LinkedList()

            Log.d(TAG, laps + "restoreStopwatch --")

            dataStoreManager.restoreStopwatch().collect { restoredState ->
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
            }
        }
        Log.d(TAG, "restoreStopwatch: --")

    }

    fun startResume() {
        if (isStarted.value == true && isRunning.value == true) return
        isStarted.value = true
        isRunning.value = true
        viewModelScope.launch(Dispatchers.Main) {
            if (startTime == 0L) startTime = System.currentTimeMillis()
            saveStopwatch()
            while (isRunning.value!!) {
                elapsedMs.postValue((System.currentTimeMillis() - startTime) + elapsedMsBeforePause)
                val seconds = elapsedMs.value!! / 1000
                if (elapsedSec.value != seconds) elapsedSec.postValue(seconds)
                delay(10)
            }
        }
    }

    fun pause() {
        isRunning.value = false
        elapsedMsBeforePause = elapsedMs.value!!
        startTime = 0L
    }

    fun reset() {
        isStarted.value = false
        isRunning.value = false
        elapsedMs.value = 0L
        elapsedSec.value = 0L
        elapsedMsBeforePause = 0L
        laps.value!!.clear()
        previousLapDelta.value = 1L
        viewModelScope.launch {
            dataStoreManager.resetStopwatch()
            viewModelScope.coroutineContext.cancelChildren()
        }
    }

    fun hardReset() {
        pause()
        viewModelScope.launch {
            delay(10)
            reset()
        }
    }

    fun addLap() {
        viewModelScope.launch(Dispatchers.Main) {
            val deltaLap = if (laps.value!!.isEmpty())
                elapsedMs.value!!
            else
                elapsedMs.value!! - laps.value!!.first.elapsedTime
            val deltaLapString = "+ ${deltaLap.toFormatString()}"
            val newLaps = LinkedList(laps.value!!)
            newLaps.addFirst(Lap(laps.value!!.size + 1, elapsedMs.value!!, deltaLapString))
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