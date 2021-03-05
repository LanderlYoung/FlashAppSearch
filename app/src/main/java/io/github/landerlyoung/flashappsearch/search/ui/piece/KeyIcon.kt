package com.example.androiddevchallenge.ui.piece

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.landerlyoung.flashappsearch.search.model.Input
import java.util.*
import kotlin.math.min

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

    var baseSize by remember { mutableStateOf(0f) }

    // https://stackoverflow.com/questions/63971569/androidautosizetexttype-in-jetpack-compose
    Column(
        modifier = modifier.onSizeChanged { (width, height) ->
            baseSize = min(width * 1.7f, height.toFloat())
        }
    ) {

        val firstLevelSize = baseSize * 5 / 10
        val secondLevelSize = baseSize * 2 / 10

        Spacer(Modifier.weight(1f))

        Text(
            text = first,
            color = color,
            fontSize = LocalDensity.current.run {
                firstLevelSize.toSp()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.weight(0.2f))

        Text(
            text = second,
            color = color.convert(ColorSpaces.Srgb).copy(alpha = 0.8f),
            fontSize = LocalDensity.current.run {
                secondLevelSize.toSp()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.weight(1f))
    }
}

@Preview
@Composable
fun previewKeyIcon() {
    MaterialTheme {
        KeyIcon(
            key = Input("2abc"),
            color = Color.Black,
            Modifier.size(
                200.dp, 500.dp
            )
        )
    }
}

