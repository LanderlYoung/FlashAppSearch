package io.github.landerlyoung.flashappsearch.search.vm

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.util.LruCache
import io.github.landerlyoung.flashappsearch.search.model.Input
import io.github.landerlyoung.flashappsearch.search.repo.AppNameRepo

/**
 * <pre>
 * Author: taylorcyang@tencent.com
 * Date:   2018-06-25
 * Time:   20:45
 * Life with Passion, Code with Creativity.
 * </pre>
 */
class AppSearchViewModel(app: Application) : AndroidViewModel(app) {
    private val appInfoCache = LruCache<String, Drawable?>(60)

    private val _input = MutableLiveData<MutableList<Input>>()

    val input: LiveData<List<Input>>
        get() = _input as LiveData<List<Input>>

    val resultApps = Transformations.switchMap(_input, {
        AppNameRepo.queryApp(it)
    })!!

    init {
        AppNameRepo.quickInit(app)
        _input.value = mutableListOf()
    }

    fun input(key: Input) {
        _input.value?.let {
            it.add(key)
            _input.value = it
        }
    }

    fun backspace() {
        _input.value?.let {
            if (it.isNotEmpty()) {
                it.removeAt(it.size - 1)
            }
            _input.value = it
        }
    }

    fun clear() {
        _input.value?.let {
            it.clear()
            _input.value = it
        }
    }

    private fun fetchAppInfo(key: String): Drawable? {
        val packageManager = getApplication<Application>().packageManager
        val info = packageManager.getApplicationInfo(key, 0)
        return info?.let {
            packageManager.getApplicationIcon(key)

        }
    }

    @SuppressLint("RestrictedApi")
    fun queryAppInfo(packageName: String): LiveData<Drawable?> {
        val result = MutableLiveData<Drawable?>()
        val data = appInfoCache[packageName]
        if (data != null) {
            result.value = data
        } else {
            AsyncTask.SERIAL_EXECUTOR.execute {
                val info = fetchAppInfo(packageName)
                appInfoCache.put(packageName, info)
                result.postValue(info)
            }
        }
        return result
    }
}
