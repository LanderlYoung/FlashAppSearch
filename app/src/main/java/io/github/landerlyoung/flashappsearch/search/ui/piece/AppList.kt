package io.github.landerlyoung.flashappsearch.search.ui.piece

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.landerlyoung.flashappsearch.search.ui.AppIconFetcher
import io.github.landerlyoung.flashappsearch.search.utils.DrawablePainter

/*
 * ```
 * Author: taylorcyang@tencent.com
 * Date:   2021-03-04
 * Time:   17:40
 * Life with Passion, Code with Creativity.
 * ```
 */


val appItemWidth = 74.dp

@ExperimentalFoundationApi
@Composable
fun AppItem(
    appPackageName: String,
    appName: String,
    iconFetcher: AppIconFetcher,
    onAppClick: (packageName: String) -> Unit = {},
    onAppLongClick: (packageName: String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .width(appItemWidth)
            .combinedClickable(
                onClick = { onAppClick(appPackageName) },
                onLongClick = { onAppLongClick(appPackageName) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        val drawable by iconFetcher.queryAppIcon(appPackageName)
            .observeAsState(null)

        val size = 48.dp

        Image(
            painter = DrawablePainter(drawable),
            contentDescription = appName,
            modifier = Modifier.size(size, size)
        )

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
    iconFetcher: AppIconFetcher,
    modifier: Modifier = Modifier,
    onAppClick: (packageName: String) -> Unit = {},
    onAppLongClick: (packageName: String) -> Unit = {},
) {
    LazyVerticalGrid(
        cells = GridCells.Adaptive(appItemWidth + 16.dp),
        modifier = modifier
    ) {
        items(appPackageNames) { (packageName, name) ->
            AppItem(packageName, name.toString(), iconFetcher, onAppClick, onAppLongClick)
        }
    }
}

@ExperimentalFoundationApi
@Preview
@Composable
fun previewAppItem() {
    val context = LocalContext.current
    AppItem(
        appPackageName = context.packageName,
        appName = context.applicationInfo.name,
        iconFetcher = AppIconFetcher(context)
    )
}