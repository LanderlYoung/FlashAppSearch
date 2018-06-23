package io.github.landerlyoung.flashappsearch.search.repo

import android.support.annotation.WorkerThread
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
    }

    private val db = PinyinDataBase.createDb(App.context)
    private val dao = db.pinyinDao()

    val pinyinCache = object : LruCache<String, String>(1024) {
        override fun create(key: String?): String? {
            return key?.let {
                dao.queryPinyin(it).foldRight(StringBuilder()) { entity, sb ->
                    sb.append(entity.pinyin)
                }.let {
                    if (it.isEmpty()) {
                        null
                    } else {
                        it.toString()
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