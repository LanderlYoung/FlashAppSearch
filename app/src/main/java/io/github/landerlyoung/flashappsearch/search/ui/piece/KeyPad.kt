package com.example.androiddevchallenge.ui.piece

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.landerlyoung.flashappsearch.search.model.Input
import io.github.landerlyoung.flashappsearch.search.model.T9

/*
 * ```
 * Author: taylorcyang@tencent.com
 * Date:   2021-03-04
 * Time:   16:29
 * Life with Passion, Code with Creativity.
 * ```
 */

val t9Width = 72.dp
val t9Height = 64.dp

val keyColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) {
        Color.White
    } else {
        Color.Black
    }

@ExperimentalFoundationApi
@Composable
fun KeyPadT9(
    onKey: (Input) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        KeyPadRow(
            T9.k1,
            T9.k2,
            T9.k3,
            onKey
        )
        KeyPadRow(
            T9.k4,
            T9.k5,
            T9.k6,
            onKey
        )
        KeyPadRow(
            T9.k7,
            T9.k8,
            T9.k9,
            onKey
        )
        Row {
            val size = Modifier.size(t9Width, t9Height)

            Spacer(Modifier.weight(2f))
            KeyIcon(key = T9.kHash, color = keyColor, size.clickable {
            })

            Spacer(Modifier.weight(1f))
            KeyIcon(key = T9.k0, color = keyColor, size.clickable {
                onKey(T9.k0)
            })
            Spacer(Modifier.weight(1f))
            KeyIcon(key = T9.kDelete, color = keyColor, size.combinedClickable(
                onClick = {
                    onKey(T9.kDelete)
                },
                onLongClick = {
                    onClear()
                }
            ))
            Spacer(Modifier.weight(2f))
        }
    }
}

@Composable
private fun KeyPadRow(
    key1: Input,
    key2: Input,
    key3: Input,
    onKey: (Input) -> Unit,
) {
    val size = Modifier.size(t9Width, t9Height)

    Row {
        Spacer(Modifier.weight(2f))
        KeyIcon(key = key1, color = keyColor, size.clickable {
            onKey(key1)
        })
        Spacer(Modifier.weight(1f))
        KeyIcon(key = key2, color = keyColor, size.clickable {
            onKey(key2)
        })
        Spacer(Modifier.weight(1f))
        KeyIcon(key = key3, color = keyColor, size.clickable {
            onKey(key3)
        })
        Spacer(Modifier.weight(2f))
    }
}

@Preview
@Composable
@ExperimentalFoundationApi
private fun previewKeypadT9() {
    MaterialTheme {
        KeyPadT9(onKey = { }, onClear = { }, modifier = Modifier.fillMaxSize())
    }
}