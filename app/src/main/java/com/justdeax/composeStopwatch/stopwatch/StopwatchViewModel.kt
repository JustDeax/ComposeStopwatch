package com.justdeax.composeStopwatch.stopwatch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.justdeax.composeStopwatch.util.DataStoreManager
import com.justdeax.composeStopwatch.util.Lap
import com.justdeax.composeStopwatch.util.StopwatchState
import com.justdeax.composeStopwatch.util.displayMs
import com.justdeax.composeStopwatch.util.formatSeconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
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

    init { restoreStopwatch() }

    fun changeTheme(themeCode: Int) {
        viewModelScope.launch {
            dataStoreManager.changeTheme(themeCode)
        }
    }

    fun changeTapOnClock(tapType: Int) {
        viewModelScope.launch {
            dataStoreManager.changeTapOnClock(tapType)
        }
    }

    fun changeNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.changeNotificationEnabled(enabled)
        }
    }

    fun saveStopwatch() {
        viewModelScope.launch {
            dataStoreManager.saveStopwatch(
                StopwatchState(
                    elapsedMsBeforePause,
                    startTime,
                    isRunning.value!!,
                    laps.value?.let {
                        if (it.isNotEmpty()) Json.encodeToString(it.toList()) else ""
                    } ?: ""
                )
            )
        }
    }

    private fun restoreStopwatch() {
        viewModelScope.launch {
            dataStoreManager.restoreStopwatch().collect { restoredState ->
                elapsedMsBeforePause = restoredState.elapsedMsBeforePause
                startTime = restoredState.startTime
                laps.value =
                    if (restoredState.laps.isNotEmpty())
                        LinkedList(Json.decodeFromString<List<Lap>>(restoredState.laps))
                    else LinkedList()
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
            }
        }
    }

    fun startResume() {
        isRunning.value = true
        viewModelScope.launch(Dispatchers.IO) {
            if (startTime == 0L) startTime = System.currentTimeMillis()
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
        viewModelScope.launch { dataStoreManager.resetStopwatch() }
        isRunning.value = false
        elapsedMs.value = 0L
        elapsedSec.value = 0L
        elapsedMsBeforePause = 0L
        laps.value!!.clear()
        viewModelScope.coroutineContext.cancelChildren()
    }

    fun addLap() {
        viewModelScope.launch(Dispatchers.Main) {
            val deltaLap = if (laps.value!!.isEmpty())
                elapsedMs.value!!
            else
                elapsedMs.value!! - laps.value!!.first.elapsedTime
            val deltaLapString = "+${formatSeconds(deltaLap / 1000)}.${displayMs(deltaLap)}"
            val newLaps = LinkedList(laps.value!!)
            newLaps.addFirst(Lap(laps.value!!.size + 1, elapsedMs.value!!, deltaLapString))
            laps.value = newLaps
        }
    }

    private val isRunning = MutableLiveData(false)
    private val elapsedMs = MutableLiveData(0L)
    private val elapsedSec = MutableLiveData(0L)
    private val laps = MutableLiveData<LinkedList<Lap>>(LinkedList())

    val isRunningI: LiveData<Boolean> get() = isRunning
    val elapsedMsI: LiveData<Long> get() = elapsedMs
    val elapsedSecI: LiveData<Long> get() = elapsedSec
    val lapsI: LiveData<LinkedList<Lap>> get() = laps
}