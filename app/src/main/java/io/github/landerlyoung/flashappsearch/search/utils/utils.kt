package io.github.landerlyoung.flashappsearch.search.utils

import android.util.Log

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