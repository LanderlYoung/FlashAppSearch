package io.github.landerlyoung.flashappsearch.search.repo

import android.annotation.SuppressLint
import android.arch.lifecycle.ComputableLiveData
import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import io.github.landerlyoung.flashappsearch.App
import io.github.landerlyoung.flashappsearch.BuildConfig
import io.github.landerlyoung.flashappsearch.search.model.AppInfoDataBase
import io.github.landerlyoung.flashappsearch.search.model.AppInfoEntity
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
    private val appNamePinyinMapper: Map<String, Pair<CharSequence, String>> by lazy {
        time("appNamePinyinMapper") {
            val appInfoDao = AppInfoDataBase.createDb(context).appInfoDao()
            val allDbInfo = time("allDbInfo") {
                appInfoDao.allAppInfo().fold(HashMap<String, AppInfoEntity>(), { acc, appInfoEntity ->
                    acc[appInfoEntity.packageName] = appInfoEntity
                    acc
                })
            }

            val pm = context.packageManager
            val allPackages = time("allPackages") {
                pm.getInstalledPackages(0).fold(HashMap<String, PackageInfo>(), { acc, packageInfo ->
                    acc[packageInfo.packageName] = packageInfo
                    acc
                })
            }

            val dbAppNames = allDbInfo.values.fold(HashSet<Pair<String, Long>>(), { acc, entity ->
                acc.add(entity.packageName to entity.lastUpdated)
                acc
            })
            val appNames = allPackages.values.fold(HashSet<Pair<String, Long>>(), { acc, packageInfo ->
                acc.add(packageInfo.packageName to packageInfo.lastUpdateTime)
                acc
            })

            val intersect = dbAppNames.intersect(appNames)
            val delete = HashSet(dbAppNames).also { it.removeAll(intersect) }
            val added = HashSet(appNames).also { it.removeAll(intersect) }

            val mapper = HashMap<String, Pair<CharSequence, String>>()

            intersect.forEach {
                val record = allDbInfo[it.first]!!
                if (record.pinyin != null) {
                    mapper[record.packageName] = record.appName to record.pinyin
                }
            }

            val newRecords = added.map {
                val pkgInfo = allPackages[it.first]!!
                val label = context.packageManager.getApplicationLabel(pkgInfo.applicationInfo).toString()

                val pinyin = if (pm.getLaunchIntentForPackage(pkgInfo.packageName) == null) {
                    null
                } else {
                    pinyinConverter.hanzi2Pinyin(label).toLowerCase()
                }
                AppInfoEntity(it.first, label, pinyin, pkgInfo.lastUpdateTime)
            }

            newRecords.forEach {
                if (it.pinyin != null) {
                    mapper[it.packageName] = it.appName to it.pinyin
                }
            }

            App.executors().execute {
                val deleteList = delete.map {
                    allDbInfo[it.first]!!
                }
                appInfoDao.delete(deleteList)
                appInfoDao.saveAllAppInfo(newRecords)

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "deleteList:$deleteList\nnewRecords:$newRecords")
                }
            }
            mapper
        }
    }

    fun quickInit(context: Context) {
        this.context = context.applicationContext
        // init first
        App.executors().execute {
            // init
            Log.i(TAG, "app count ${appNamePinyinMapper.size}")
        }
    }

    /**
     * calculate how good input matches pinyinName
     */
    internal fun calculateMatchResult(input: List<Input>, pinyinName: String): Double {
        var score = 0.0
        if (pinyinName.length > 2 && !pinyinName.isBlank()) {
            var inputIndex = 0
            var pinyinIndex = 1
            var matches = 0.0
            var lastUnMatch = 0

            fun indexMultiplier(index: Int): Double {
                return 8.0 / (index + 4) + 1
            }

            while (inputIndex < input.size && pinyinIndex < pinyinName.length) {
                if (pinyinName[pinyinIndex] in input[inputIndex].keys) {
                    inputIndex++
                    pinyinIndex++
                    matches += indexMultiplier(pinyinIndex)
                } else {
                    if (pinyinName[lastUnMatch] == PinyinConverter.PINYIN_SPLITTER_CHAR
                        && pinyinName[pinyinIndex] == PinyinConverter.PINYIN_SPLITTER_CHAR
                    ) {
                        // last whole pinyin char is matched

                        val len = pinyinIndex - lastUnMatch - 1
                        matches += indexMultiplier(lastUnMatch + 1) * len
                    }
                    lastUnMatch = pinyinIndex
                    pinyinIndex++
                }
            }

            score = if (inputIndex < input.size && pinyinIndex == pinyinName.length) {
                // exhausted
                0.0
            } else {
                matches * 100 / pinyinName.length
            }
        }
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "pinyin=$pinyinName score=$score")
        }
        return score
    }

    private var lastSearchInputs: List<Input>? = null
    // package -> name -> score
    private var lastSearchResult: Sequence<Triple<String, Pair<CharSequence, String>, Double>>? = null

    private fun <T> List<T>.startWith(other: List<T>): Boolean {
        val len = size
        val olen = other.size
        if (olen in 1..len) {
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
                Log.i(TAG, "getAllApps result result from $keys")
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

            val result =
                getAllApps(keys).map {
                    Triple(it.first, it.second, calculateMatchResult(keys, it.second.second))
                }.filter {
                    it.third > 0
                }.sortedByDescending {
                    it.third
                }

            synchronized(this) {
                lastSearchInputs = keys
                lastSearchResult = result
            }

            return time("queryApp $keys") {
                Log.i(TAG, "queryApp $keys")
                var count = 0
                result.fold(mutableListOf()) { list, pkg ->
                    if (BuildConfig.DEBUG && count++ < 4) {
                        Log.i(TAG, pkg.toString())
                    }
                    list.add(pkg.first to pkg.second.first)
                    list
                }
            }
        }
    }.liveData
}
