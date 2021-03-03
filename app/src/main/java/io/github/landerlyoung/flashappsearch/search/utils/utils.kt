package io.github.landerlyoung.flashappsearch.search.utils

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/*
 * ```
 * Author: taylorcyang@tencent.com
 * Date:   2018-06-27
 * Time:   13:43
 * Life with Passion, Code with Creativity.
 * ```
 */

inline fun <T> time(name: String, bloc: () -> T): T {
    val begin = System.currentTimeMillis()
    try {
        val result = bloc()
        Log.i("Time", "$name cost ${System.currentTimeMillis() - begin}ms")
        return result
    } catch (e: Throwable) {
        Log.i("Time", "$name failed with $e, cost ${System.currentTimeMillis() - begin}ms")
        throw  e
    }
}

fun <W, X, Y> mapMulti(
    source0: LiveData<W>,
    source: LiveData<X>,
    mapFunction: (W?, X?) -> Y
): LiveData<Y> {
    val result = MediatorLiveData<Y>()
    result.addSource(source) { x -> result.value = mapFunction(source0.value, x) }
    result.addSource(source0) { w -> result.value = mapFunction(w, source.value) }
    return result
}

fun <W, X, Y> switchMapMulti(
    source0: LiveData<W>,
    source: LiveData<X>,
    switchMapFunction: (W?, X?) -> LiveData<Y>
): LiveData<Y> {
    val result = MediatorLiveData<Y>()
    var mSource: LiveData<Y>? = null
    fun mapper() {
        val newLiveData = switchMapFunction(source0.value, source.value)
        if (mSource === newLiveData) {
            return
        }
        if (mSource != null) {
            result.removeSource(mSource!!)
        }
        mSource = newLiveData
        if (mSource != null) {
            result.addSource(
                mSource!!
            ) { y -> result.value = y }
        }
    }

    result.addSource(source0) { mapper() }
    result.addSource(source) { mapper() }
    return result
}