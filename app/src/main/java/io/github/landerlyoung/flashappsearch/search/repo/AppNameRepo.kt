package io.github.landerlyoung.flashappsearch.search.repo

import android.annotation.SuppressLint
import android.arch.lifecycle.ComputableLiveData
import android.content.Context
import android.util.Log
import io.github.landerlyoung.flashappsearch.App
import io.github.landerlyoung.flashappsearch.search.model.Input

/**
 * <pre>
 * Author: taylorcyang@tencent.com
 * Date:   2018-06-24
 * Time:   21:40
 * Life with Passion, Code with Creativity.
 * </pre>
 */
@SuppressLint("StaticFieldLeak")
object AppNameRepo {
    private const val TAG = "AppNameRepo"

    @Volatile
    private lateinit var context: Context

    private val pinyinConverter by lazy {
        PinyinConverter()
    }

    // packageName -> pinyin
    private val appNamePinyinMapper by lazy {
        context.packageManager.getInstalledApplications(0)?.map {
            val label = context.packageManager.getApplicationLabel(it)
            it.packageName to pinyinConverter.hanzi2Pinyin(label)
        }?.associate { it } ?: mapOf()
    }

    fun quickInit(context: Context) {
        this.context = context.applicationContext
        // init first
        App.executors().execute {
            // init
            Log.i(TAG, appNamePinyinMapper.toString())
        }
    }

    /**
     * calculate how good input match pinyinName, ranged [0, 100]
     */
    internal fun calculateMatchResult(input: List<Input>, pinyinName: String): Int {
        // todo: need optimized
        var i = 0
        var j = 0
        var matches = 0
        while (i < input.size && j < pinyinName.length) {
            if (pinyinName[j] in input[i].keys) {
                i++
                j++
                matches++
            } else {
                j++
            }
        }

        return matches * 100 / pinyinName.length
    }

    @SuppressLint("RestrictedApi")
    fun queryApp(keys: List<Input>) = object : ComputableLiveData<List<String>>() {
        override fun compute(): List<String> {
            val result = appNamePinyinMapper.entries.map {
                Triple(it.key, it.value, calculateMatchResult(keys, it.value))
            }.filter {
                it.third > 0
            }.sortedByDescending {
                it.third
            }

            Log.i(TAG, result.toString())
            return result.map { it.first }
        }
    }.liveData
}
