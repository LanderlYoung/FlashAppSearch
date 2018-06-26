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
        val start = System.currentTimeMillis()
        val pm = context.packageManager
        val v = pm.getInstalledApplications(0)
                ?.asSequence()
                ?.filter {
                    pm.getLaunchIntentForPackage(it.packageName) != null
                }?.map {
                    val label = context.packageManager.getApplicationLabel(it)
                    it.packageName to (label to pinyinConverter.hanzi2Pinyin(label).toLowerCase())
                }?.associate { it } ?: mapOf()

        Log.v(TAG, "init appNamePinyinMapper cost time ${System.currentTimeMillis() - start}ms")

        v
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

        if (i < input.size && j == pinyinName.length) {
            // exhausted
            return 0
        }

        return matches * 100 / pinyinName.length
    }

    private var lastSearchInputs: List<Input>? = null
    private var lastSearchResult: List<Triple<String, String, Int>>? = null

    fun <T> List<T>.startWith(other: List<T>): Boolean {
        val len = size
        val olen = other.size
        if (len >= olen) {
            for (i in 0 until olen) {
                if (other[i] != this[i]) {
                    return false
                }
            }
            return true
        } else {
            return false
        }
    }

    fun getAllApps(keys: List<Input>) =
            synchronized(this) {
                if (lastSearchInputs != null && keys.startWith(lastSearchInputs!!)) {
                    lastSearchResult!!.map {
                        it.first to it.second
                    }.asSequence()
                } else {
                    appNamePinyinMapper.entries
                            .asSequence()
                            .map { it.key to it.value.second }
                }
            }

    @SuppressLint("RestrictedApi")
    fun queryApp(keys: List<Input>) = object : ComputableLiveData<List<String>>() {
        override fun compute(): List<String> {
            if (keys.isEmpty()) {
                return listOf()
            }

            val result = getAllApps(keys).map {
                Triple(it.first, it.second, calculateMatchResult(keys, it.second))
            }.filter {
                it.third > 0
            }.sortedByDescending {
                it.third
            }.fold(mutableListOf<Triple<String, String, Int>>()) { list, pkg ->
                list.add(pkg)
                list
            }

            synchronized(this) {
                lastSearchInputs = keys
                lastSearchResult = result
            }

            Log.i(TAG, result.toString())
            return result.map { it.first }
        }
    }.liveData
}
