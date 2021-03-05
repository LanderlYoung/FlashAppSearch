package io.github.landerlyoung.flashappsearch.search.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import io.github.landerlyoung.flashappsearch.search.ui.piece.FlashAppSearch
import io.github.landerlyoung.flashappsearch.search.vm.AppSearchViewModel

class AppSearchActivity : AppCompatActivity() {

    lateinit var viewModel: AppSearchViewModel

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AppSearchViewModel::class.java]

        setContent {
            MaterialTheme() {
                    FlashAppSearch(vm = viewModel, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
