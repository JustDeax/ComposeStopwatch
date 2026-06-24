package com.justdeax.composeStopwatch.util

import java.util.Locale

//GENERAL ------------------------------------

fun Long.cutToMs() = String.format(Locale.US, "%02d", (this % 1000)).substring(0, 2)

//PORTRAIT -----------------------------------

fun Long.formatSecondsWithoutHours(): String {
    val seconds = this % 60
    val minutes = this / 60 % 60
    val result = if (this >= 3600)
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    else if (minutes != 0L)
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    else
        String.format(Locale.US, "%d", seconds)
    return "$result."
}

fun Long.cutToHours() = "${this / 60 / 60}h"

//LANDSCAPE ----------------------------------

fun Long.formatSecondsWithHours(): String {
    val seconds = this % 60
    val minutes = this / 60 % 60
    val hours = this / 60 / 60
    val result = if (hours != 0L)
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    else if (minutes != 0L)
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    else
        String.format(Locale.US, "%d", seconds)
    return "$result."
}

//FOR LAPS AND NOTIFICATION ------------------

fun Long.formatSecondsFull(): String {
    val seconds = this % 60
    val minutes = this / 60 % 60
    val hours = this / 60 / 60
    return if (hours != 0L)
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    else
        String.format(Locale.US, "%d:%02d", minutes, seconds)
}

fun Long.formatSecondsFullWithMs() = (this / 1000).formatSecondsFull() + "." + this.cutToMs()
