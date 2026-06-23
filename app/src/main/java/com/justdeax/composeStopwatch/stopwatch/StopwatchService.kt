package com.justdeax.composeStopwatch.stopwatch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.justdeax.composeStopwatch.AppActivity
import com.justdeax.composeStopwatch.R
import com.justdeax.composeStopwatch.util.Lap
import com.justdeax.composeStopwatch.util.StopwatchAction
import com.justdeax.composeStopwatch.util.fullFormatSeconds
import com.justdeax.composeStopwatch.util.toFormatString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList
import kotlin.time.Duration.Companion.milliseconds

class StopwatchService : LifecycleService() {
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentStartResume: PendingIntent
    private lateinit var intentPause: PendingIntent
    private lateinit var intentStop: PendingIntent
    private lateinit var intentAddLap: PendingIntent
    private var elapsedMsBeforePause = 0L
    private var startTime = 0L
    private var timerJob: Job? = null

    private val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    else
        PendingIntent.FLAG_UPDATE_CURRENT

    private fun setupNotification() {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.stopwatch_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(notificationChannel)

        elapsedSec.observe(this) { elapsedSeconds ->
            if (isRunning.value == true)
                notificationManager.notify(
                    NOTIFICATION_ID, getNotification(elapsedSeconds.fullFormatSeconds())
                )
        }

        pendingIntent = PendingIntent.getActivity(
            this, 1, Intent(this, AppActivity::class.java), flag
        )
        intentStartResume = createPendingIntent(2, StopwatchAction.START_RESUME)
        intentPause = createPendingIntent(3, StopwatchAction.PAUSE)
        intentStop = createPendingIntent(4, StopwatchAction.RESET)
        intentAddLap = createPendingIntent(5, StopwatchAction.ADD_LAP)
    }

    private fun createPendingIntent(code: Int, action: StopwatchAction) = PendingIntent.getService(
        this,
        code,
        Intent(this, StopwatchService::class.java).also {
            it.action = action.name
        },
        flag
    )

    private fun getNotification(time: String): Notification {
        val currentLaps = laps.value ?: LinkedList()
        val lapItem = if (currentLaps.isEmpty()) "" else {
            val lastLap = currentLaps.first()
            val elapsedTime = lastLap.elapsedTime.toFormatString()
            val lastLapText = getString(R.string.last_lap)
            "$lastLapText: ${lastLap.index} $elapsedTime | ${lastLap.deltaLap}"
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setOngoing(true)
            .setAutoCancel(false)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)

        if (isRunning.value == true)
            builder.setContentTitle(time)
                .setContentText(lapItem)
                .addAction(R.drawable.round_pause_24, getString(R.string.pause), intentPause)
                .addAction(R.drawable.round_add_circle_24, getString(R.string.add_lap), intentAddLap)
        else
            builder.setContentTitle("$time ${getString(R.string.stopwatch_paused)}")
                .setContentText(lapItem)
                .addAction(R.drawable.round_play_arrow_24, getString(R.string.resume), intentStartResume)
                .addAction(R.drawable.round_stop_24, getString(R.string.stop), intentStop)

        return builder.build()
    }

    override fun onCreate() {
        super.onCreate()
        setupNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                StopwatchAction.START_RESUME.name -> startResume()
                StopwatchAction.PAUSE.name -> pause()
                StopwatchAction.RESET.name -> reset()
                StopwatchAction.HARD_RESET.name -> hardReset()
                StopwatchAction.ADD_LAP.name -> addLap()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startResume() {
        if (isStarted.value == true && isRunning.value == true && timerJob?.isActive == true) return
        isStarted.value = true
        isRunning.value = true
        timerJob?.cancel()
        timerJob = lifecycleScope.launch(Dispatchers.Main) {
            if (startTime == 0L) startTime = System.currentTimeMillis()
            while (isRunning.value == true) {
                val currentMs = (System.currentTimeMillis() - startTime) + elapsedMsBeforePause
                elapsedMs.value = currentMs
                val seconds = currentMs / 1000
                if (elapsedSec.value != seconds) elapsedSec.value = seconds
                delay(10.milliseconds)
            }
        }

        val initialTime = (elapsedMsBeforePause / 1000).fullFormatSeconds()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                getNotification(initialTime),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, getNotification(initialTime))
        }
    }

    private fun pause() {
        isRunning.value = false
        timerJob?.cancel()
        elapsedMsBeforePause = elapsedMs.value ?: 0L
        startTime = 0L
        notificationManager.notify(
            NOTIFICATION_ID, getNotification(elapsedMs.value?.toFormatString() ?: "00:00:00")
        )
    }

    private fun reset() {
        isStarted.value = false
        isRunning.value = false
        timerJob?.cancel()
        elapsedMs.value = 0L
        elapsedSec.value = 0L
        elapsedMsBeforePause = 0L
        laps.value?.clear()
        previousLapDelta.value = 1L
        lifecycleScope.coroutineContext.cancelChildren()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun hardReset() {
        pause()
        lifecycleScope.launch {
            delay(10.milliseconds)
            reset()
        }
    }

    private fun addLap() {
        lifecycleScope.launch(Dispatchers.Main) {
            val currentElapsed = elapsedMs.value ?: 0L
            val currentLaps = laps.value ?: LinkedList()

            val deltaLap = if (currentLaps.isEmpty())
                currentElapsed
            else
                currentElapsed - currentLaps.first().elapsedTime

            val deltaLapString = "+ ${deltaLap.toFormatString()}"
            currentLaps.addFirst(Lap(currentLaps.size + 1, currentElapsed, deltaLapString))
            laps.value = currentLaps
            previousLapDelta.value = deltaLap

            notificationManager.notify(
                NOTIFICATION_ID, getNotification(currentElapsed.toFormatString())
            )
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1488
        private const val NOTIFICATION_CHANNEL_ID = "stopwatch_service_channel"

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
}