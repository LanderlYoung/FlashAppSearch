package com.example.androiddevchallenge.ui.piece

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.landerlyoung.flashappsearch.R
import io.github.landerlyoung.flashappsearch.search.model.Input
import io.github.landerlyoung.flashappsearch.search.model.T9

/*
 * ```
 * Author: taylorcyang@tencent.com
 * Date:   2021-03-04
 * Time:   17:07
 * Life with Passion, Code with Creativity.
 * ```
 */


@Composable
fun SearchBar(
    showAllApps: Boolean,
    inputs: List<Input>,
    onClickShowAll: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(8.dp)
            .background(
                if (isSystemInDarkTheme()) {
                    Color.White
                } else {
                    Color.Black
                }.copy(alpha = 0.2f), RoundedCornerShape(16.dp)
            ),
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        when {
            showAllApps || inputs.isEmpty() -> {
                Text(
                    text =
                    if (showAllApps) "全部显示" else "搜索应用",
                    modifier = Modifier
                        .weight(1f)
                )
            }
            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    val size = Modifier
                        .fillMaxHeight()
                        .aspectRatio(0.7f, true)
                        .padding(0.dp, 2.dp)
                    inputs.forEach { key ->
                        KeyIcon(
                            key = key,
                            color = keyColor,
                            modifier = size
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(R.drawable.ic_apps),
            contentDescription = null,
            modifier =
            Modifier
                .size(44.dp, 44.dp)
                .padding(4.dp)
                .clickable(onClick = onClickShowAll)
        )
        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Preview
@Composable
private fun previewKeypadT9() {
    MaterialTheme {
        SearchBar(showAllApps = false, inputs = listOf(
            T9.k1,
            T9.k2,
            T9.k3,
            T9.k4,
            T9.k5,
        ), onClickShowAll = { /*TODO*/ })
    }
}