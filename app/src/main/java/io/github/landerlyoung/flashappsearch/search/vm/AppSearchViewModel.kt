package io.github.landerlyoung.flashappsearch.search.vm

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.arch.core.executor.ArchTaskExecutor
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
    private val appInfoCache = LruCache<String, Drawable?>(20)

    private val _input = MutableLiveData<MutableList<Input>>()

    @Suppress("UNCHECKED_CAST")
    val input: LiveData<List<Input>>
        get() = _input as LiveData<List<Input>>

    val showAllApps = MutableLiveData(false)

    val resultApps = switchMapMulti(showAllApps, _input) { show, input ->
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
        showAllApps.value = false
        _input.value?.let {
            it.add(key)
            _input.value = it
        }
    }

    fun backspace() {
        showAllApps.value = false
        _input.value?.let {
            if (it.isNotEmpty()) {
                it.removeAt(it.size - 1)
            }
            _input.value = it
        }
    }

    fun clear() {
        showAllApps.value = false
        _input.value?.let {
            it.clear()
            _input.value = it
        }
    }

    private fun fetchAppInfo(key: String): Drawable {
        val packageManager = getApplication<Application>().packageManager
        return packageManager.getApplicationIcon(key)
    }

    @SuppressLint("RestrictedApi")
    fun queryAppIcon(packageName: String): LiveData<Drawable?> {
        val result = MutableLiveData<Drawable?>()
        val data = appInfoCache[packageName]
        if (data != null) {
            result.value = data
        } else {
            ArchTaskExecutor.getIOThreadExecutor().execute {
                val info = fetchAppInfo(packageName)
                appInfoCache.put(packageName, info)
                result.postValue(info)
            }
        }
        return result
    }
}
