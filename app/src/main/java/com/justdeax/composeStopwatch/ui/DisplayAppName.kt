package com.justdeax.composeStopwatch.ui
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.justdeax.composeStopwatch.R
import com.justdeax.composeStopwatch.ui.dialog.EasyBottomSheet
import com.justdeax.composeStopwatch.ui.theme.DarkColorScheme
import com.justdeax.composeStopwatch.ui.theme.Hypertext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayAppName(
    modifier: Modifier,
    show: Boolean
) {
    val context = LocalContext.current
    val helpDraw = painterResource(R.drawable.round_help_outline_24)
    var showAboutApp by remember { mutableStateOf(false) }

    androidx.compose.animation.AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -40 },
        exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { -40 }
    ) {
        Row(
            modifier = modifier.clickable(
                remember { MutableInteractionSource() }, null
            ) { showAboutApp = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = context.getString(R.string.app_name),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                modifier = Modifier.size(24.dp),
                painter = helpDraw,
                contentDescription = context.getString(R.string.about_app),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }

    val sheetState = rememberModalBottomSheetState()
    EasyBottomSheet(
        sheetState = sheetState,
        show = showAboutApp,
        onDismissRequest = { showAboutApp = false },
        onButtonClick = { showAboutApp = false }
    ) {
        Text(
            text = context.getString(R.string.about_app),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = context.getString(R.string.about_app_desc),
            style = MaterialTheme.typography.titleMedium
        )
        val annotatedString = buildAnnotatedString {
            append(context.getString(R.string.about_app_desc_a) + " ")
            withStyle(
                style = SpanStyle(
                    color = Hypertext,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(context.getString(R.string.app_author))
            }
            append(context.getString(R.string.about_app_desc_v))
            append(" " + context.getString(R.string.app_version))
        }
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JustDeax"))
                context.startActivity(intent)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DisplayAppNamePreview() {
    MaterialTheme(colorScheme = DarkColorScheme) {
        DisplayAppName(
            modifier = Modifier.padding(21.dp, 16.dp),
            show = true
        )
    }
}