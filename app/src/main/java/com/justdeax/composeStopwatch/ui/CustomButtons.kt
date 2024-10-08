package com.justdeax.composeStopwatch.ui
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun OutlineIconButton(modifier: Modifier, painter: Painter, contentDesc: String, onClick: () -> Unit) {
    Box {
        OutlinedButton(
            modifier = modifier
                .width(90.dp)
                .height(60.dp)
                .padding(5.dp),
            onClick = onClick,
        ) {
            Icon(
                painter = painter,
                contentDescription = contentDesc,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun IconButton(width: Int = 70, painter: Painter, contentDesc: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .width(width.dp)
            .height(70.dp),
        onClick = onClick
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDesc,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun IconButtonInLandscape(height: Int, onClick: () -> Unit, painter: Painter, contentDesc: String) {
    Button(
        modifier = Modifier
            .height(height.dp)
            .width(70.dp),
        onClick = onClick
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDesc,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}