package com.justdeax.composeStopwatch.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.justdeax.composeStopwatch.R
import com.justdeax.composeStopwatch.ui.dialog.EasyBottomSheet
import com.justdeax.composeStopwatch.ui.theme.DarkColorScheme
import com.justdeax.composeStopwatch.ui.theme.Hypertext
import com.justdeax.composeStopwatch.util.fadeInAnim
import com.justdeax.composeStopwatch.util.fadeOutAnim
import com.justdeax.composeStopwatch.util.slideInMinusAnim
import com.justdeax.composeStopwatch.util.slideOutMinusAnim

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayAppName(
    modifier: Modifier,
    isPortrait: Boolean,
    show: Boolean
) {
    val context = LocalContext.current
    val helpDraw = painterResource(R.drawable.round_help_outline_24)

    var showAboutApp by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = if (isPortrait)
            Alignment.TopStart
        else
            Alignment.TopEnd
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = show,
            enter = fadeInAnim + slideInMinusAnim,
            exit = fadeOutAnim + slideOutMinusAnim
        ) {
            Row(
                modifier = Modifier.clickable(
                    remember { MutableInteractionSource() }, null
                ) { showAboutApp = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge
                )
                Icon(
                    painter = helpDraw,
                    contentDescription = stringResource(R.string.about_app),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }

    val sheetState = rememberModalBottomSheetState()
    EasyBottomSheet(
        sheetState = sheetState,
        show = showAboutApp,
        onDismissRequest = { showAboutApp = false }
    ) {
        Text(
            text = stringResource(R.string.about_app),
            style = MaterialTheme.typography.titleLarge
        )
        val authorString = buildAnnotatedString {
            append(stringResource(R.string.about_app_desc_a))
            withStyle(
                style = SpanStyle(
                    color = Hypertext,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(stringResource(R.string.app_author))
            }
        }
        val repoString = buildAnnotatedString {
            append(stringResource(R.string.about_app_desc_r))
            withStyle(
                style = SpanStyle(
                    color = Hypertext,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(stringResource(R.string.app_repo))
            }
            append(stringResource(R.string.about_app_desc_v))
            append(stringResource(R.string.app_version))
        }
        Text(
            text = authorString,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, "https://github.com/JustDeax".toUri())
                context.startActivity(intent)
            }
        )
        Text(
            text = repoString,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, "https://github.com/JustDeax/ComposeStopwatch".toUri())
                context.startActivity(intent)
            }
        )
        Text(
            text = stringResource(R.string.about_app_desc),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DisplayAppNamePreview() {
    MaterialTheme(colorScheme = DarkColorScheme) {
        DisplayAppName(
            modifier = Modifier,
            true,
            show = true
        )
    }
}