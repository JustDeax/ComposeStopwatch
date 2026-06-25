package com.justdeax.composeStopwatch.util

import android.os.VibrationEffect
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

val startResumeVibration: VibrationEffect? = VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
val pauseVibration: VibrationEffect? = VibrationEffect.createOneShot(200, 80)
val resetVibration: VibrationEffect? = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
val addLapVibration: VibrationEffect? = VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1)

val fadeInAnim = fadeIn(tween(Duration.animLong))
val fadeOutAnim = fadeOut(tween(Duration.animMedium))

val slideInMinusAnim = slideInVertically(tween(Duration.animLong)) { -Dimens.iconButtonSize }
val slideOutMinusAnim = slideOutVertically(tween(Duration.animLong)) { -Dimens.iconButtonSize }

val slideInPlusAnim = slideInVertically(tween(Duration.animLong)) { +Dimens.iconButtonSize }
val slideOutPlusAnim = slideOutVertically(tween(Duration.animLong)) { +Dimens.iconButtonSize }

object Duration {
    const val animMini = 200
    const val animShort = 250
    const val animMedium = 300
    const val animLong = 500
}

object Dimens {
    const val iconButtonSize = 80
    const val iconOutlineButtonSize = 64
    const val iconButtonSpace = 170
    const val iconButtonMinSize = 130
    const val iconButtonMaxSize = iconButtonSize * 2 + 170
    const val bottomButtonsPadding = 50
    const val topButtonsPadding = 32
    const val generalButtonHeight = bottomButtonsPadding + topButtonsPadding + iconButtonSize + iconOutlineButtonSize
}