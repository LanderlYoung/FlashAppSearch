package io.github.landerlyoung.flashappsearch.search.repo

//import androidx.lifecycle.LiveDataReactiveStreams
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.ComputableLiveData
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.github.landerlyoung.flashappsearch.App
import io.github.landerlyoung.flashappsearch.BuildConfig
import io.github.landerlyoung.flashappsearch.search.model.AppInfoDataBase
import io.github.landerlyoung.flashappsearch.search.model.AppInfoEntity
import io.github.landerlyoung.flashappsearch.search.model.Input
import io.github.landerlyoung.flashappsearch.search.utils.time
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

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
        time("pinyinConverter") {
            PinyinConverter()
        }
    }

    // packageName -> (name -> pinyin)
    private val appNamePinyinMapper: Map<String, Pair<CharSequence, String>> by lazy {
        time("appNamePinyinMapper") {
            val appInfoDao = AppInfoDataBase.createDb(context).appInfoDao()
            val allDbInfo = time("allDbInfo") {
                appInfoDao.allAppInfo()
                    .fold(HashMap<String, AppInfoEntity>()) { acc, appInfoEntity ->
                        acc[appInfoEntity.packageName] = appInfoEntity
                        acc
                    }
            }

            val pm = context.packageManager
            val allPackages = time("allPackages") {
                pm.getInstalledPackages(0)
                    .fold(HashMap<String, PackageInfo>()) { acc, packageInfo ->
                        acc[packageInfo.packageName] = packageInfo
                        acc
                    }
            }

            val dbAppNames = allDbInfo.values.fold(HashSet<Pair<String, Long>>()) { acc, entity ->
                acc.add(entity.packageName to entity.lastUpdated)
                acc
            }
            val appNames =
                allPackages.values.fold(HashSet<Pair<String, Long>>()) { acc, packageInfo ->
                    acc.add(packageInfo.packageName to packageInfo.lastUpdateTime)
                    acc
                }

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
                val label =
                    context.packageManager.getApplicationLabel(pkgInfo.applicationInfo).toString()

                val pinyin = if (pm.getLaunchIntentForPackage(pkgInfo.packageName) == null) {
                    null
                } else {
                    pinyinConverter.hanzi2Pinyin(label)
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

    private val fibos = DoubleArray(64)

    init {
        fibos[fibos.size - 1] = 1.toDouble()
        fibos[fibos.size - 2] = 2.toDouble()
        for (i in (0..fibos.size - 3).reversed()) {
            fibos[i] = fibos[i + 1] + fibos[i + 2]
        }
    }

    private fun indexMultiplier(index: Int): Double {
        if (index >= fibos.size) return 0.1
        return fibos[index]
    }

    /**
     * calculate how good input matches pinyinName
     */
    internal fun calculateMatchResult(input: List<Input>, pinyinName: String): Double {
        var inputIndex = 0
        var matches = 0.0

        var pinyinIndex = 0
        var wordIndex = 0
        var charIndex = 0
        var matchedWord = false
        while (inputIndex < input.size && pinyinIndex < pinyinName.length) {
            when (pinyinName[pinyinIndex]) {
                PinyinConverter.PINYIN_SPLITTER_CHAR -> {
                    wordIndex++
                    charIndex = 0
                    matchedWord = false
                }

                PinyinConverter.PINYIN_SPLITTER_MULTI_CHAR -> {
                    if (matchedWord) {
                        // 该字的某个读音已经匹配了，忽略多音字的其他音节
                        pinyinIndex++
                        while (pinyinIndex < pinyinName.length &&
                            pinyinName[pinyinIndex] != PinyinConverter.PINYIN_SPLITTER_CHAR
                        ) {
                            pinyinIndex++
                        }
                        continue
                    }
                    charIndex = 0
                }

                in input[inputIndex].keySets -> {
                    matchedWord = true
                    // 计算得分
                    var score = indexMultiplier(pinyinIndex + charIndex)
                    if (charIndex == 0) {
                        // 首字母权重增加
                        score *= 2
                    }
                    matches += score
                    inputIndex++
                }
            }
            pinyinIndex++
        }

        return if (inputIndex < input.size && pinyinIndex == pinyinName.length) {
            // exhausted
            0.0
        } else {
            if (pinyinIndex == pinyinName.length || pinyinName.indexOf(
                    PinyinConverter.PINYIN_SPLITTER_CHAR,
                    pinyinIndex
                ) == -1
            ) {
                // 完整匹配，有加分
                matches * 1.5
            } else {
                matches
            }
        }
    }

    private var lastSearchInputs: List<Input>? = null

    // package -> name -> score
    private var lastSearchResult: Sequence<Triple<String, Pair<CharSequence, String>, Double>>? =
        null

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
     * @return <package name, label>
     */
    @SuppressLint("RestrictedApi")
    fun allApps(): LiveData<List<Pair<String, CharSequence>>?> {
        return object : ComputableLiveData<List<Pair<String, CharSequence>>>() {
            override fun compute(): List<Pair<String, CharSequence>> {
                return appNamePinyinMapper.entries
                    .map { it.key to it.value.first }
            }
        }.liveData
    }

    private fun flowTest() {
        val f = flow { emit(0) }
        runBlocking {
            f.collect {

            }
            f.collectLatest { }
        }
        Observable.just(0)
    }

    private fun Disposable.attachToLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    this@attachToLifecycle.dispose()
                }
            }
        })
    }



    data class AppInfo(
        val packageName: String,
        val appName: String,
        val namePinyin: List<Pinyin>
    )

    /**
     * @return <package, name, pinyin>
     */
    private fun findApps(keys: List<Input>): Sequence<Pair<String, Pair<CharSequence, String>>> =
        if (lastSearchInputs != null && keys.startWith(lastSearchInputs!!)) {
            Log.i(TAG, "getAllApps result from $keys")
            lastSearchResult!!.map {
                it.first to it.second
            }.asSequence()
        } else {
            appNamePinyinMapper.entries
                .asSequence().map { it.key to it.value }
        }

    /**
     * @return <package name, label>
     */
    @SuppressLint("RestrictedApi")
    fun queryApp(keys: List<Input>): LiveData<List<Pair<String, CharSequence>>?> {
        return object :
            ComputableLiveData<List<Pair<String, CharSequence>>>(App.serialExecutors()) {
            override fun compute(): List<Pair<String, CharSequence>> {
                if (keys.isEmpty()) {
                    return listOf()
                }
                return time("queryApp") {
                    performQueryApps(keys)
                }
            }
        }.liveData
    }


    private fun performQueryApps(keys: List<Input>): List<Pair<String, CharSequence>> {
        val result = queryBasedOnCache(keys)

        var count = 0
        return result.fold(mutableListOf()) { list, pkg ->
            if (BuildConfig.DEBUG && count++ < 4) {
                Log.i(TAG, pkg.toString())
            }
            list.add(pkg.first to pkg.second.first)
            list
        }
    }

    private fun queryBasedOnCache(keys: List<Input>): Sequence<Triple<String, Pair<CharSequence, String>, Double>> {
        synchronized(this@AppNameRepo) {
            return findApps(keys).map {
                Triple(
                    it.first,
                    it.second,
                    calculateMatchResult(keys, it.second.second)
                )
            }.filter {
                keys.isNotEmpty() && it.third > 0
            }.sortedByDescending {
                it.third
            }.also {
                lastSearchInputs = keys
                lastSearchResult = it
            }
        }
    }
}
