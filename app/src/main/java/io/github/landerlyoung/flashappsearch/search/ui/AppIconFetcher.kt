package io.github.landerlyoung.flashappsearch.search.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/*
 * ```
 * Author: landerlyoung@gmail.com
 * Date:   2021-03-04
 * Time:   17:57
 * Life with Passion, Code with Creativity.
 * ```
 */

class AppIconFetcher(private val context: Context) {
    private val appInfoCache = LruCache<String, Drawable?>(20)
    private fun fetchAppInfo(key: String): Drawable {
        val packageManager = context.packageManager
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