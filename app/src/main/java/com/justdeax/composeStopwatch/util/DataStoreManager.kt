package com.justdeax.composeStopwatch.util
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("preference")

class DataStoreManager(private val context: Context) {
    companion object { //SW => STOPWATCH
        private val SW_ELAPSED_MS_BEFORE_PAUSE = longPreferencesKey("SW_ELAPSED_MS_BEFORE_PAUSE")
        private val SW_START_TIME = longPreferencesKey("SW_START_TIME")
        private val SW_IS_RUNNING = booleanPreferencesKey("SW_IS_RUNNING")
        private val SW_LAPS = stringPreferencesKey("SW_LAPS")
        private val SW_NOTIFICATION_ENABLED = booleanPreferencesKey("SW_NOTIF_ENABLED")
        private val SW_TAP_ON_CLOCK = intPreferencesKey("SW_TAP_ON_CLOCK")
        private val LOCK_AWAKE = booleanPreferencesKey("LOCK_AWAKE")
        private val APP_THEME = intPreferencesKey("APP_THEME_CODE")
    }

    suspend fun changeTheme(themeCode: Int) {
        context.dataStore.edit { set -> set[APP_THEME] = themeCode }
    }

    fun getTheme() = context.dataStore.data.map { get ->
        get[APP_THEME] ?: 0
    }

    suspend fun changeTapOnClock(tapType: Int) {
        context.dataStore.edit { set -> set[SW_TAP_ON_CLOCK] = tapType }
    }

    fun getTapOnClock() = context.dataStore.data.map { get ->
        get[SW_TAP_ON_CLOCK] ?: 1
    }

    suspend fun changeNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { set -> set[SW_NOTIFICATION_ENABLED] = enabled }
    }

    fun notificationEnabled() = context.dataStore.data.map { get ->
        get[SW_NOTIFICATION_ENABLED] ?: true
    }

    suspend fun changeLockAwakeEnabled(enabled: Boolean) {
        context.dataStore.edit { set -> set[LOCK_AWAKE] = enabled }
    }

    fun lockAwakeEnabled() = context.dataStore.data.map { get ->
        get[LOCK_AWAKE] ?: false
    }

    suspend fun saveStopwatch(stopwatchState: StopwatchState) {
        context.dataStore.edit { set ->
            set[SW_ELAPSED_MS_BEFORE_PAUSE] = stopwatchState.elapsedMsBeforePause
            set[SW_START_TIME] = stopwatchState.startTime
            set[SW_IS_RUNNING] = stopwatchState.isRunning
        }
    }

    fun restoreStopwatch() = context.dataStore.data.map { get ->
        StopwatchState(
            get[SW_ELAPSED_MS_BEFORE_PAUSE] ?: 0L,
            get[SW_START_TIME] ?: 0L,
            get[SW_IS_RUNNING] ?: false,
        )
    }

    suspend fun resetStopwatch() {
        context.dataStore.edit { set ->
            set.remove(SW_ELAPSED_MS_BEFORE_PAUSE)
            set.remove(SW_START_TIME)
            set.remove(SW_IS_RUNNING)
            set.remove(SW_LAPS)
        }
    }

    suspend fun saveLaps(laps: String) {
        context.dataStore.edit { set -> set[SW_LAPS] = laps }
    }

    fun restoreLaps() = context.dataStore.data.map { get ->
        get[SW_LAPS] ?: ""
    }
}