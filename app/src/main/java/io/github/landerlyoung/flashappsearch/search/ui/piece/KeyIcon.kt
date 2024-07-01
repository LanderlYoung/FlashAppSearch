package io.github.landerlyoung.flashappsearch.search.ui.piece

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.min
import io.github.landerlyoung.flashappsearch.search.model.Input
import java.util.Locale

/*
 * ```
 * Author: landerlyoung@gmail.com
 * Date:   2021-03-04
 * Time:   15:22
 * Life with Passion, Code with Creativity.
 * ```
 */

@Composable
fun KeyIcon(
    key: Input,
    color: Color,
    modifier: Modifier = Modifier
) {
    val first = key.keys[0].toString()
    val second = key.keys.subList(1, key.keys.size)
        .joinToString(separator = "")
        .lowercase()

    // https://stackoverflow.com/questions/63971569/androidautosizetexttype-in-jetpack-compose
    Column(modifier = modifier) {
        Spacer(Modifier.weight(1f))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(5f)
        ) {
            val fontSize = calculateFontSize(maxWidth, maxHeight)
            val fontSizeSp = LocalDensity.current.run { fontSize.toSp() }

            Text(
                text = first,
                color = color,
                fontSize = fontSizeSp,
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center,
                lineHeight = fontSizeSp,
            )
        }

        Spacer(Modifier.weight(0.5f))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
        ) {
            val fontSize = calculateFontSize(maxWidth, maxHeight)
            val fontSizeSp = LocalDensity.current.run { fontSize.toSp() }

            Text(
                text = second,
                color = color.convert(ColorSpaces.Srgb).copy(alpha = 0.8f),
                fontSize = fontSizeSp,
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center,
                lineHeight = fontSizeSp,
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

private fun calculateFontSize(width: Dp, height: Dp): Dp {
    return min(width * 1.7f, height) * 0.8f
}

@Preview
@Composable
fun PreviewKeyIcon() {
    MaterialTheme {
        KeyIcon(
            key = Input("2abc"),
            color = Color.Black,
            Modifier.size(
                20.dp, 10.dp
            )
        )
    }
}

enum class SurfaceState {
    Pressed,
    Released
}

@Preview
@Composable
fun PressedSurface() {
    val (pressed, onPress) = remember { mutableStateOf(false) }
    val transition = updateTransition(
        targetState = if (pressed) SurfaceState.Pressed else SurfaceState.Released,
        label = "changeSize"
    )

    val width by transition.animateDp(label = "Width") { state ->
        when (state) {
            SurfaceState.Released -> 20.dp
            SurfaceState.Pressed -> 50.dp
        }
    }
    val surfaceColor by transition.animateColor(label = "Height") { state ->
        when (state) {
            SurfaceState.Released -> Color.Blue
            SurfaceState.Pressed -> Color.Red
        }
    }
    val selectedAlpha by transition.animateFloat(label = "Alpha") { state ->
        when (state) {
            SurfaceState.Released -> 0.5f
            SurfaceState.Pressed -> 1f
        }
    }

    Row {
        Surface(
            color = surfaceColor.copy(alpha = selectedAlpha),
            modifier = Modifier
                .toggleable(value = pressed, onValueChange = onPress)
                .size(height = 50.dp, width = width)
        ) {}

        Text("Haha")
    }
}
