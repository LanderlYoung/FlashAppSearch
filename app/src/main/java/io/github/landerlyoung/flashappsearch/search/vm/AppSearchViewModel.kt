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
import io.github.landerlyoung.flashappsearch.search.model.T9
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

    val inputText = MutableLiveData<CharSequence>()

    val resultApps = Transformations.switchMap(inputText, {
        AppNameRepo.queryApp(it.map {
            when (it) {
                '0' -> T9.k0
                '1' -> T9.k1
                '2' -> T9.k2
                '3' -> T9.k3
                '4' -> T9.k4
                '5' -> T9.k5
                '6' -> T9.k6
                '7' -> T9.k7
                '8' -> T9.k8
                '9' -> T9.k9
                else -> Input.emptyInput
            }
        })
    })!!

    init {
        AppNameRepo.quickInit(app)
    }

    fun fetchAppInfo(key: String): Drawable? {
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
