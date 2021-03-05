package io.github.landerlyoung.flashappsearch.search.ui.piece

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import io.github.landerlyoung.flashappsearch.search.model.Input
import java.util.*

/*
 * ```
 * Author: taylorcyang@tencent.com
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
        .toUpperCase(Locale.US)

    // https://stackoverflow.com/questions/63971569/androidautosizetexttype-in-jetpack-compose
    Column(modifier = modifier) {
        Spacer(Modifier.weight(1f))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(5f)
        ) {
            val fontSize = calculateFontSize(maxWidth, maxHeight)

            Text(
                text = first,
                color = color,
                fontSize = LocalDensity.current.run { fontSize.toSp() },
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.weight(0.5f))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
        ) {
            val fontSize = calculateFontSize(maxWidth, maxHeight)

            Text(
                text = second,
                color = color.convert(ColorSpaces.Srgb).copy(alpha = 0.8f),
                fontSize = LocalDensity.current.run { fontSize.toSp() },
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center,
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
fun previewKeyIcon() {
    MaterialTheme {
        KeyIcon(
            key = Input("2abc"),
            color = Color.Black,
            Modifier.size(
                200.dp, 100.dp
            )
        )
    }
}

