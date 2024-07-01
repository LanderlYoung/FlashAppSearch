package io.github.landerlyoung.flashappsearch.search.ui.piece

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/*
 * ```
 * Author: landerlyoung@gmail.com
 * Date:   2021-03-05
 * Time:   15:43
 * Life with Passion, Code with Creativity.
 * ```
 */


private val LightColors = lightColors(
    background = Color.Transparent
)

private val DarkColors = darkColors(
    background = Color.Transparent
)

@Composable
fun Theme(content: @Composable () -> Unit) {
    val ripple = rememberRipple(bounded = false, color = Color.Unspecified)
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    ) {
        CompositionLocalProvider(
            LocalIndication provides ripple
        ) {
            content()
        }
    }
}