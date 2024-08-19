package com.justdeax.composeStopwatch.stopwatch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.justdeax.composeStopwatch.util.DataStoreManager

class StopwatchViewModelFactory(
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StopwatchViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return StopwatchViewModel(dataStoreManager) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}