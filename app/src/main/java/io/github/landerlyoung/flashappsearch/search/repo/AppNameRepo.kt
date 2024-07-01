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
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds

/**
 * <pre>
 * Author: landerlyoung@gmail.com
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

    data class AppInfo(
        val packageName: String,
        val name: CharSequence,
        val pinyin: PinyinSequence
    )

    // packageName -> AppInfo
    private val appNamePinyinMapper: Map<String, AppInfo> by lazy {
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

            val mapper = HashMap<String, AppInfo>()

            intersect.forEach {
                val record = allDbInfo[it.first]!!
                if (record.pinyin != null) {
                    mapper[record.packageName] =
                        AppInfo(record.packageName, record.appName, record.pinyin)
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
                    mapper[it.packageName] = AppInfo(it.packageName, it.appName, it.pinyin)
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

    private var lastSearchInputs: List<Input>? = null

    // package -> name -> score
    private var lastSearchResult: Sequence<Pair<AppInfo, Double>>? =
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
    fun allApps(): LiveData<List<AppInfo>?> {
        return object : ComputableLiveData<List<AppInfo>>() {
            override fun compute(): List<AppInfo> {
                return appNamePinyinMapper.values.toList()
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

    /**
     * @return <package, name, pinyin>
     */
    private fun findApps(keys: List<Input>): Sequence<AppInfo> =
        if (lastSearchInputs != null && keys.startWith(lastSearchInputs!!)) {
            Log.i(TAG, "getAllApps result from $keys")
            lastSearchResult!!.map {
                it.first
            }
        } else {
            appNamePinyinMapper.entries.asSequence().map { it.value }
        }

    /**
     * @return <package name, label>
     */
    @SuppressLint("RestrictedApi")
    fun queryApp(keys: List<Input>): LiveData<List<AppInfo>?> {
        return object :
            ComputableLiveData<List<AppInfo>>(App.serialExecutors()) {
            override fun compute(): List<AppInfo> {
                if (keys.isEmpty()) {
                    return listOf()
                }
                return time("queryApp") {
                    performQueryApps(keys)
                }
            }
        }.liveData
    }


    private fun performQueryApps(keys: List<Input>): List<AppInfo> {
        val result = queryBasedOnCache(keys)

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "search result keys:$keys list:${result.toList()}")
        }

        var count = 0
        return result.fold(mutableListOf()) { list, pkg ->
            if (BuildConfig.DEBUG && count++ < 4) {
                Log.i(TAG, pkg.toString())
            }
            list.add(pkg.first)
            list
        }
    }

    private fun queryBasedOnCache(keys: List<Input>): Sequence<Pair<AppInfo, Double>> {
        synchronized(this@AppNameRepo) {
            return findApps(keys).map {
                it to MatchScoreCalculator.calculateMatchResult(keys, it.pinyin)
            }.filter {
                keys.isNotEmpty() && it.second > 0
            }.sortedByDescending {
                it.second
            }.also {
                lastSearchInputs = keys
                lastSearchResult = it
            }
        }
    }
}
