package io.github.landerlyoung.flashappsearch.search.repo

import android.util.Log
import android.util.LruCache
import androidx.annotation.WorkerThread
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
        private val chineseRegex = "[\\u3400-\\uD87E\\uDDD4]".toRegex(RegexOption.IGNORE_CASE)
        private val nonCharRegex = "([^\\da-zA-Z|>]|\\s)+".toRegex()

        const val PINYIN_SPLITTER_CHAR = '|'
        const val PINYIN_SPLITTER = PINYIN_SPLITTER_CHAR.toString()

        // 多音字
        const val PINYIN_SPLITTER_MULTI_CHAR = '>'
        const val PINYIN_SPLITTER_MULTI = PINYIN_SPLITTER_MULTI_CHAR.toString()

        private val TAG = "PinyinConverter"
    }

    private val db = PinyinDataBase.createDb(App.context)
    private val dao = db.pinyinDao()

    private val pinyinCache = object : LruCache<String, Pinyin>(1024) {
        override fun create(key: String?): Pinyin? {
            return key?.let {
                dao.queryPinyin(it).foldRight(mutableListOf<String>()) { pinyin, hs ->
                    hs.add(pinyin)
                    hs
                }.let {
                    if (it.isEmpty()) {
                        Log.w(TAG, "can't find pinyin for $key")
                        null
                    } else {
                        Pinyin(it)
                    }
                }
            }
        }
    }

    /**
     * convert chinese character sequence to [PinyinSequence]
     */
    @WorkerThread
    fun hanzi2Pinyin(hanzi: CharSequence): PinyinSequence? {
        var mr: MatchResult? = chineseRegex.matchEntire(hanzi) ?: return null
        val list = mutableListOf<Pinyin>()
        while (mr != null) {
            pinyinCache[mr.value]?.let {
                list.add(it)
            }
        }
        return PinyinSequence(list)
    }
}

data class PinyinSequence(val pinyin: List<Pinyin>)

/**
 * 一个字的拼音，可能是多音字
 */
data class Pinyin(val readings: List<String>)