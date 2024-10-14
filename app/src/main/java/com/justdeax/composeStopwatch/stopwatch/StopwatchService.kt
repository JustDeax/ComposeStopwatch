package com.justdeax.composeStopwatch.stopwatch
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList

class StopwatchService: LifecycleService() {
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentStartResume: PendingIntent
    private lateinit var intentPause: PendingIntent
    private lateinit var intentStop: PendingIntent
    private lateinit var intentAddLap: PendingIntent
    private var elapsedMsBeforePause = 0L
    private var startTime = 0L
    private val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    else
        PendingIntent.FLAG_UPDATE_CURRENT

    private fun setupNotification() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.stopwatch_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        elapsedSec.observe(this) { elapsedSeconds ->
            if (isRunning.value!!)
                notificationManager.notify(
                    NOTIFICATION_ID,
                    getNotification(elapsedSeconds.fullFormatSeconds())
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
        val lapItem = if (laps.value!!.isEmpty()) "" else {
            val lastLap = laps.value!!.first
            val elapsedTime = lastLap.elapsedTime.toFormatString()
            val lastLapText = getString(R.string.last_lap)
            "$lastLapText: ${lastLap.index} $elapsedTime | ${lastLap.deltaLap}"
        }

        if (isRunning.value!!)
            return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(time)
                .setContentText(lapItem)
                .setContentIntent(pendingIntent)
                .addAction(
                    R.drawable.round_pause_24,
                    getString(R.string.pause),
                    intentPause
                )
                .addAction(
                    R.drawable.round_add_circle_24,
                    getString(R.string.add_lap),
                    intentAddLap
                )
                .build()
        else
            return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(time + " " + getString(R.string.stopwatch_paused))
                .setContentText(lapItem)
                .setContentIntent(pendingIntent)
                .addAction(
                    R.drawable.round_play_arrow_24,
                    getString(R.string.resume),
                    intentStartResume
                )
                .addAction(
                    R.drawable.round_stop_24,
                    getString(R.string.stop),
                    intentStop
                )
                .build()
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
                StopwatchAction.HARD_RESET.name -> { hardReset() }
                StopwatchAction.ADD_LAP.name -> addLap()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startResume() {
        if (isStarted.value == true && isRunning.value == true) return
        isStarted.value = true
        isRunning.value = true
        lifecycleScope.launch(Dispatchers.Main) {
            if (startTime == 0L) startTime = System.currentTimeMillis()
            while (isRunning.value!!) {
                elapsedMs.postValue((System.currentTimeMillis() - startTime) + elapsedMsBeforePause)
                val seconds = elapsedMs.value!! / 1000
                if (elapsedSec.value != seconds) elapsedSec.postValue(seconds)
                delay(10)
            }
        }
        startForeground(
            NOTIFICATION_ID,
            getNotification((elapsedMsBeforePause / 1000).fullFormatSeconds())
        )
    }

    private fun pause() {
        isRunning.value = false
        elapsedMsBeforePause = elapsedMs.value!!
        startTime = 0L
        notificationManager.notify(
            NOTIFICATION_ID,
            getNotification(elapsedMsBeforePause.toFormatString())
        )
    }

    private fun reset() {
        isStarted.value = false
        isRunning.value = false
        elapsedMs.value = 0L
        elapsedSec.value = 0L
        elapsedMsBeforePause = 0L
        laps.value!!.clear()
        previousLapDelta.value = 1L
        lifecycleScope.coroutineContext.cancelChildren()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun hardReset() {
        pause()
        lifecycleScope.launch {
            delay(10)
            reset()
        }
    }

    private fun addLap() {
        lifecycleScope.launch(Dispatchers.Main) {
            val deltaLap = if (laps.value!!.isEmpty())
                elapsedMs.value!!
            else
                elapsedMs.value!! - laps.value!!.first.elapsedTime
            val deltaLapString = "+ ${deltaLap.toFormatString()}"
            laps.value?.addFirst(Lap(laps.value!!.size + 1, elapsedMs.value!!, deltaLapString))
            previousLapDelta.value = deltaLap
        }
        notificationManager.notify(
            NOTIFICATION_ID,
            getNotification(elapsedMs.value!!.toFormatString())
        )
    }

    companion object {
        private const val NOTIFICATION_ID = 1448
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
