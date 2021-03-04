package com.example.androiddevchallenge.ui.piece

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.getValue
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.livedata.observeAsState
import io.github.landerlyoung.flashappsearch.search.ui.AppIconFetcher

/*
 * ```
 * Author: taylorcyang@tencent.com
 * Date:   2021-03-04
 * Time:   17:40
 * Life with Passion, Code with Creativity.
 * ```
 */


@Composable
fun AppItem(appPackageName: String, appName: String, iconFetcher: AppIconFetcher) {
    Column(
        modifier = Modifier.width(74.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(4.dp))

        val drawable by iconFetcher.queryAppIcon(appPackageName)
            .observeAsState(null)

        val size = 64.dp
        val sizePx = LocalDensity.current.run {
            size.toPx()
        }
        Canvas(
            modifier = Modifier.size(size, size)
        ) {
            if (drawable != null) {
                drawable?.setBounds(0, 0, sizePx.toInt(), sizePx.toInt())
                drawable?.draw(drawContext.canvas.nativeCanvas)
            }
        }

        Spacer(Modifier.height(2.dp))
        Text(
            text = appName,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
    }
}

@ExperimentalFoundationApi
@Composable
fun AppList(
    appPackageNames: List<Pair<String, CharSequence>>,
    iconFetcher: AppIconFetcher
) {
    LazyVerticalGrid(cells = GridCells.Adaptive(74.dp)) {
        items(appPackageNames) { (packageName, name) ->
            item {
                AppItem(packageName, name.toString(), iconFetcher)
            }
        }
    }
}