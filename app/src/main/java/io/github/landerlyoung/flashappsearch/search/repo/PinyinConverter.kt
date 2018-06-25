package io.github.landerlyoung.flashappsearch.search.repo

import android.support.annotation.WorkerThread
import android.util.Log
import android.util.LruCache
import io.github.landerlyoung.flashappsearch.App
import io.github.landerlyoung.flashappsearch.search.model.PinyinDataBase

/**
 * <pre>
 * Author: taylorcyang@tencent.com
 * Date:   2018-06-23
 * Time:   12:22
 * Life with Passion, Code with Creativity.
 * </pre>
 */
class PinyinConverter {
    companion object {
        val chineseRegex = "[\\u3400-\\uD87E\\uDDD4]".toRegex()
        val numberRegex = "\\d".toRegex()
        private val TAG = "PinyinConverter"
    }

    private val db = PinyinDataBase.createDb(App.context)
    private val dao = db.pinyinDao()

    private val pinyinCache = object : LruCache<String, String>(1024) {
        override fun create(key: String?): String? {
            return key?.let {
                dao.queryPinyin(it).foldRight(HashSet<String>()) { entity, hs ->
                    hs.add(entity.pinyin.let { numberRegex.replace(it, "") })
                    hs
                }.joinToString(separator = "").let {
                    if (it.isEmpty()) {
                        Log.w(TAG, "can't find pinyin for $key")
                        null
                    } else {
                        it
                    }
                }
            }
        }
    }

    @WorkerThread
    fun hanzi2Pinyin(hanzi: CharSequence) = chineseRegex.replace(hanzi) { mr ->
        val m = mr.value
        pinyinCache[m] ?: m
    }
}