package io.github.landerlyoung.flashappsearch.search.repo

import android.annotation.SuppressLint
import android.arch.lifecycle.ComputableLiveData
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
object AppNameRepo {
    private val pinyinConverter = PinyinConverter()

    // packageName -> pinyin
    private val appNamePinyinMapper by lazy {
        val begin = System.currentTimeMillis()
        App.context.packageManager.getInstalledApplications(0)?.map {
            val label = App.context.packageManager.getApplicationLabel(it)
            it.packageName to pinyinConverter.hanzi2Pinyin(label)
        }?.associate { it } ?: mapOf()
    }

    /**
     * calculate how good input match pinyinName, ranged [0, 100]
     */
    private fun caculateMatchResult(input: List<Input>, pinyinName: String): Int {
        // todo: need optimized
        val matches = input.flatMap { it.keys.asList() }
                .count { it in pinyinName }

        return matches * 100 / pinyinName.length
    }

    @SuppressLint("RestrictedApi")
    fun queryApp(keys: List<Input>) = object : ComputableLiveData<List<Pair<String, Int>>>() {
        override fun compute() = appNamePinyinMapper.entries.map {
            it.key to caculateMatchResult(keys, it.value)
        }.filter {
            it.second > 0
        }.sortedByDescending {
            it.second
        }
    }.liveData
}
