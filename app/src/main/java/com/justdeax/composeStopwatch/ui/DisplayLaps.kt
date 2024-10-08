package com.justdeax.composeStopwatch.ui
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justdeax.composeStopwatch.ui.theme.Copper
import com.justdeax.composeStopwatch.ui.theme.DarkColorScheme
import com.justdeax.composeStopwatch.ui.theme.Gold
import com.justdeax.composeStopwatch.ui.theme.Iron
import com.justdeax.composeStopwatch.ui.theme.Silver
import com.justdeax.composeStopwatch.util.Lap
import com.justdeax.composeStopwatch.util.toFormatString

@Composable
fun DisplayLaps(
    modifier: Modifier,
    laps: List<Lap>,
    elapsedMs: Long
) {
    val heightAnimation by animateFloatAsState(
        targetValue = if (laps.isEmpty()) 0.0001f else 1f,
        animationSpec = tween(500),
        label = ""
    )
    Row(modifier = modifier.fillMaxHeight(heightAnimation)) {
        LazyColumn {
            if (laps.isNotEmpty())
                item {
                    val deltaLap = "+ ${(elapsedMs - laps.first().elapsedTime).toFormatString()}"
                    LapItem("+", MaterialTheme.colorScheme.onBackground, elapsedMs, deltaLap)
                }
            items(laps, key = { laps[it.index-1].index }) { (index, elapsedTime, deltaLap) ->
                val indexColor = when (index) {
                    1 -> Gold
                    2 -> Silver
                    3 -> Copper
                    else -> Iron
                }
                LapItem(index.toString(), indexColor, elapsedTime, deltaLap)
            }
        }
    }
}

@Composable
fun LapItem(indexText: String, indexColor: Color, elapsedTime: Long, deltaLap: String) {
    val newDensity = Density(LocalDensity.current.density, fontScale = 1f)
    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        CompositionLocalProvider(LocalDensity provides newDensity) {
            val textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp
            )
            Text(
                modifier = Modifier.weight(1f),
                text = indexText,
                style = textStyle,
                fontWeight = FontWeight.Bold,
                color = indexColor
            )
            Text(
                modifier = Modifier.weight(2f),
                text = elapsedTime.toFormatString(),
                style = textStyle,
                fontWeight = FontWeight.Normal
            )
            Text(
                modifier = Modifier.weight(2f),
                text = deltaLap,
                style = textStyle,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DisplayLapsPreview() {
    MaterialTheme(colorScheme = DarkColorScheme) {
        DisplayLaps(
            modifier = Modifier
                .padding(8.dp, 0.dp)
                .fillMaxWidth(),
            laps = listOf(Lap(1, 1000L, "+ 1.00")),
            elapsedMs = 2000L
        )
    }
}