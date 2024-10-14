package com.justdeax.composeStopwatch.ui
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justdeax.composeStopwatch.ui.theme.DarkColorScheme
import com.justdeax.composeStopwatch.util.Lap
import com.justdeax.composeStopwatch.util.cutToMs
import com.justdeax.composeStopwatch.util.formatSeconds
import com.justdeax.composeStopwatch.util.formatSecondsWithHours
import com.justdeax.composeStopwatch.util.getHours
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DisplayTime(
    modifier: Modifier,
    isPortrait: Boolean,
    isPausing: Boolean,
    seconds: Long,
    milliseconds: Long,
    laps: List<Lap>,
    previosLapDelta: Long
) {
    val firstLapDelta = if (laps.isNotEmpty()) laps.last().elapsedTime else 1
    val lastLapDelta = if (laps.isNotEmpty()) milliseconds - laps.first().elapsedTime else 0
    val textStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isPortrait) {
            StopwatchCircularProgress(
                progress = lastLapDelta / firstLapDelta.toFloat(),
                modifier = Modifier.size(300.dp),
                markerPosition = previosLapDelta / firstLapDelta.toFloat(),
                strokeWidth = 8.dp,
                markerColor = MaterialTheme.colorScheme.primary
            )
            if (seconds >= 3600L)
                Text(
                    text = seconds.getHours() + "h",
                    fontSize = 36.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 48.dp)
                        .align(Alignment.TopCenter)
                )
            TimeRow(isPausing) {
                Text(
                    text = seconds.formatSeconds(),
                    style = textStyle,
                    fontSize = 60.sp
                )
                Text(
                    text = milliseconds.cutToMs(),
                    style = textStyle,
                    fontSize = 40.sp,
                    modifier = Modifier.offset(y = 30.dp)
                )
            }
        } else {
            TimeRow(isPausing) {
                Text(
                    text = seconds.formatSecondsWithHours(),
                    style = textStyle,
                    fontSize = 90.sp,
                )
                Text(
                    text = milliseconds.cutToMs(),
                    style = textStyle,
                    fontSize = 60.sp,
                    modifier = Modifier.offset(y = 45.dp)
                )
            }
        }
    }
}

@Composable
fun StopwatchCircularProgress(
    progress: Float,
    modifier: Modifier,
    markerPosition: Float,
    strokeWidth: Dp = 8.dp,
    markerColor: Color
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.wrapContentSize()
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = modifier,
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = strokeWidth,
            trackColor = MaterialTheme.colorScheme.surfaceContainer
        )

        if (markerPosition != 1f)
            Canvas(modifier) {
                val canvasSize = size.width
                val radius = canvasSize / 2 - strokeWidth.toPx() / 2
                val angleInRadians = (360 * markerPosition - 90) * (Math.PI / 180)

                val xStart = (size.width / 2) + (radius - strokeWidth.toPx()) * cos(angleInRadians).toFloat()
                val yStart = (size.height / 2) + (radius - strokeWidth.toPx()) * sin(angleInRadians).toFloat()
                val xEnd = (size.width / 2) + (radius + strokeWidth.toPx()) * cos(angleInRadians).toFloat()
                val yEnd = (size.height / 2) + (radius + strokeWidth.toPx()) * sin(angleInRadians).toFloat()

                drawLine(
                    color = markerColor,
                    start = Offset(xStart, yStart),
                    end = Offset(xEnd, yEnd),
                    strokeWidth = strokeWidth.toPx() / 2,
                    cap = StrokeCap.Round
                )
            }
    }
}

@Composable
fun TimeRow(isPausing: Boolean, content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition("")
    val blinkAnimation by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val newDensity = Density(LocalDensity.current.density, fontScale = 1f)

    Row(
        modifier = Modifier
            .alpha(if (isPausing) blinkAnimation else 1f)
            .wrapContentSize()
    ) {
        CompositionLocalProvider(
            LocalDensity provides newDensity,
            content
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StopwatchCircularProgressPreview() {
    MaterialTheme(colorScheme = DarkColorScheme) {
        DisplayTime(
            Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .heightIn(min = 100.dp),
            isPortrait = true,
            isPausing = false,
            seconds = 102L,
            milliseconds = 102000L,
            laps = listOf(),
            previosLapDelta = 1000L
        )
    }
}