package io.github.landerlyoung.flashappsearch.search.repo

import android.annotation.SuppressLint
import android.arch.lifecycle.ComputableLiveData
import android.content.Context
import android.util.Log
import io.github.landerlyoung.flashappsearch.App
import io.github.landerlyoung.flashappsearch.search.model.Input
import io.github.landerlyoung.flashappsearch.search.utils.time

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

    // packageName -> (name -> pinyin)
    private val appNamePinyinMapper by lazy {
        time("appNamePinyinMapper") {
            val pm = context.packageManager
            pm.getInstalledApplications(0)
                    ?.asSequence()
                    ?.filter {
                        pm.getLaunchIntentForPackage(it.packageName) != null
                    }?.map {
                        val label = context.packageManager.getApplicationLabel(it)
                        it.packageName to (label to pinyinConverter.hanzi2Pinyin(label).toLowerCase())
                    }?.associate { it } ?: mapOf()
        }
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
     * calculate how good input matches pinyinName, ranged [0, 100]
     */
    internal fun calculateMatchResult(input: List<Input>, pinyinName: String): Double {
        if (pinyinName.isBlank() || pinyinName.length < 2) {
            return 0.0
        }

        // todo: need optimized
        var i = 0
        var j = 1
        var matches = 0.0

        var lastUnMatch = 0

        fun indexMultiplier(index: Int): Double {
            return 8.0 / (index + 4) + 1
        }

        while (i < input.size && j < pinyinName.length) {
            if (pinyinName[j] in input[i].keys) {
                i++
                j++
                matches += indexMultiplier(i)
            } else {
                if (pinyinName[lastUnMatch] == PinyinConverter.PINYIN_SPLITTER_CHAR
                        && pinyinName[j] == PinyinConverter.PINYIN_SPLITTER_CHAR) {
                    // last whole pinyin char is matched

                    val len = j - lastUnMatch - 1
                    matches += indexMultiplier(lastUnMatch + 1) * len
                }
                lastUnMatch = j
                j++
            }
        }

        if (i < input.size && j == pinyinName.length) {
            // exhausted
            return 0.0
        }

        return matches * 100 / pinyinName.length
    }

    private var lastSearchInputs: List<Input>? = null
    // package -> name -> score
    private var lastSearchResult: Sequence<Triple<String, Pair<CharSequence, String>, Double>>? = null

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

    /**
     * @return <package, name, pinyin>
     */
    fun getAllApps(keys: List<Input>): Sequence<Pair<String, Pair<CharSequence, String>>> =
            synchronized(this) {
                if (lastSearchInputs != null && keys.startWith(lastSearchInputs!!)) {
                    lastSearchResult!!.map {
                        it.first to it.second
                    }.asSequence()
                } else {
                    appNamePinyinMapper.entries
                            .asSequence().map { it.key to it.value }
                }
            }

    @SuppressLint("RestrictedApi")
    fun queryApp(keys: List<Input>) = object : ComputableLiveData<List<Pair<String, CharSequence>>>() {
        override fun compute(): List<Pair<String, CharSequence>> {
            if (keys.isEmpty()) {
                return listOf()
            }

            val result = time("queryApp") {
                getAllApps(keys).map {
                    Triple(it.first, it.second, calculateMatchResult(keys, it.second.second))
                }.filter {
                    it.third > 0
                }.sortedByDescending {
                    it.third
                }
            }

            synchronized(this) {
                lastSearchInputs = keys
                lastSearchResult = result
            }

            Log.i(TAG, "queryApp $keys")
            result.take(4).forEach {
                Log.i(TAG, it.toString())
            }

            return result.fold(mutableListOf<Pair<String, CharSequence>>()) { list, pkg ->
                list.add(pkg.first to pkg.second.first)
                list
            }.also {
            }
        }
    }.liveData
}
