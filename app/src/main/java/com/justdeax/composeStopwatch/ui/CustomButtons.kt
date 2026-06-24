package com.justdeax.composeStopwatch.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.justdeax.composeStopwatch.util.Dimens
import com.justdeax.composeStopwatch.util.Duration

@Composable
fun IconButton(width: Int = Dimens.iconButtonSize, height: Int = Dimens.iconButtonSize, state: Boolean, painter: Painter, contentDesc: String, onClick: () -> Unit) {
    val buttonCornerAnimation by animateIntAsState(
        targetValue = if (state) 12 else 99,
        animationSpec = tween(durationMillis = Duration.animMini, easing = LinearEasing),
        label = ""
    )
    Button(
        modifier = Modifier
            .width(width.dp)
            .height(height.dp),
        onClick = onClick,
        shape = RoundedCornerShape(buttonCornerAnimation.dp)
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDesc,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun IconOutlineButton(modifier: Modifier, painter: Painter, contentDesc: String, onClick: () -> Unit) {
    OutlinedButton(
        modifier = modifier
            .width(90.dp)
            .height(Dimens.iconOutlineButtonSize.dp)
            .padding(4.dp),
        onClick = onClick,
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDesc,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}