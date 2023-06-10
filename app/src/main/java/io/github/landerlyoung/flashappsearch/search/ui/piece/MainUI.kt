package io.github.landerlyoung.flashappsearch.search.ui.piece

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import io.github.landerlyoung.flashappsearch.search.model.Input
import io.github.landerlyoung.flashappsearch.search.ui.AppIconFetcher
import io.github.landerlyoung.flashappsearch.search.vm.AppSearchViewModel

/*
 * ```
 * Author: taylorcyang@tencent.com
 * Date:   2021-03-05
 * Time:   11:26
 * Life with Passion, Code with Creativity.
 * ```
 */

@ExperimentalFoundationApi
@Composable
fun FlashAppSearch(vm: AppSearchViewModel) {
    val context = LocalContext.current
    val iconFetcher = remember { AppIconFetcher(context) }

    val showAllApp by vm.showAllApps.observeAsState(initial = false)
    val appList by vm.resultApps.observeAsState()

    // val inputs by vm.input.observeAsState()
    var inputs by remember { mutableStateOf<List<Input>?>(null, neverEqualPolicy()) }
    vm.input.observe(LocalLifecycleOwner.current) { list ->
        inputs = list
    }

    Scaffold(
        topBar = {
            SearchBar(
                showAllApps = showAllApp,
                inputs = inputs ?: emptyList(),
                onClickShowAll = { vm.showAllApp() })
        },
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            AppList(
                appPackageNames = appList?.map { it.packageName to it.name } ?: emptyList(),
                iconFetcher = iconFetcher,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onAppClick = {
                    context.packageManager.getLaunchIntentForPackage(it)?.let {
                        context.startActivity(it)
                    }
                },
                onAppLongClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", it, null)
                    intent.data = uri
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))
            KeyPadT9(
                onKey = { vm.input(it) },
                onClear = { vm.clear() },
                onBackspace = { vm.backspace() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
