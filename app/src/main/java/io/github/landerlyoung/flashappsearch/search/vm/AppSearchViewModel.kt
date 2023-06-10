package io.github.landerlyoung.flashappsearch.search.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.landerlyoung.flashappsearch.search.model.Input
import io.github.landerlyoung.flashappsearch.search.repo.AppNameRepo
import io.github.landerlyoung.flashappsearch.search.utils.switchMapMulti

/**
 * <pre>
 * Author: taylorcyang@tencent.com
 * Date:   2018-06-25
 * Time:   20:45
 * Life with Passion, Code with Creativity.
 * </pre>
 */
class AppSearchViewModel(app: Application) : AndroidViewModel(app) {
    private val _input = MutableLiveData<MutableList<Input>>()

    @Suppress("UNCHECKED_CAST")
    val input: LiveData<List<Input>>
        get() = _input as LiveData<List<Input>>

    private val _showAllApps = MutableLiveData(false)
    val showAllApps: LiveData<Boolean>
        get() = _showAllApps

    val resultApps = switchMapMulti(_showAllApps, _input) { show, input ->
        if (show!!) {
            AppNameRepo.allApps()
        } else {
            AppNameRepo.queryApp(input!!)
        }
    }

    init {
        AppNameRepo.quickInit(app)
        _input.value = mutableListOf()
    }

    fun input(key: Input) {
        _showAllApps.value = false
        _input.value?.let {
            it.add(key)
            _input.value = it
        }
    }

    fun backspace() {
        _showAllApps.value = false
        _input.value?.let {
            if (it.isNotEmpty()) {
                it.removeAt(it.size - 1)
                _input.value = it
            }
        }
    }

    fun clear() {
        _showAllApps.value = false
        _input.value?.let {
            it.clear()
            _input.value = it
        }
    }

    fun showAllApp() {
        _showAllApps.value = true
    }
}
