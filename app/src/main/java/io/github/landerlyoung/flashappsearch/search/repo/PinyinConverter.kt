package io.github.landerlyoung.flashappsearch.search.repo

import android.util.Log
import android.util.LruCache
import androidx.annotation.WorkerThread
import io.github.landerlyoung.flashappsearch.App
import io.github.landerlyoung.flashappsearch.search.model.PinyinDataBase

/**
 * <pre>
 * Author: landerlyoung@gmail.com
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
        override fun create(key: String): Pinyin? {
            return dao.queryPinyin(key).foldRight(LinkedHashSet<String>()) { pinyin, hs ->
                hs.add(pinyin)
                hs
            }.let {
                if (it.isEmpty()) {
                    Log.w(TAG, "can't find pinyin for $key")
                    null
                } else {
                    Pinyin(it.toList())
                }
            }
        }
    }

    /**
     * convert chinese character sequence to [PinyinSequence]
     * leave other characters as-is
     */
    @WorkerThread
    fun hanzi2Pinyin(hanzi: CharSequence): PinyinSequence {
        val words = hanzi.split(nonCharRegex)

        val sequence = mutableListOf<Pinyin>()

        words.forEach { word ->
            var prevIndex = 0
            var mr: MatchResult? = chineseRegex.matchAt(hanzi, 0)
            if (mr != null) {
                while (mr != null) {
                    if (prevIndex < mr.range.first) {
                        val nonHanzi = hanzi.substring(prevIndex, mr.range.first)
                        sequence.add(Pinyin(nonHanzi))
                    }
                    prevIndex = mr.range.last + 1
                    pinyinCache[mr.value]?.let {
                        sequence.add(it)
                    }
                    mr = mr.next()
                }
            } else {
                sequence.add(Pinyin(word))
            }
        }

        return PinyinSequence(sequence)
    }
}

data class PinyinSequence(val pinyin: List<Pinyin>) {
    fun serializeToString(): String =
        pinyin.joinToString(separator = PinyinConverter.PINYIN_SPLITTER) {
            it.serializeToString()
        }

    companion object {
        fun deserializeFromString(string: String): PinyinSequence =
            PinyinSequence(
                string.split(PinyinConverter.PINYIN_SPLITTER).map {
                    Pinyin.deserializeFromString(it)
                }
            )
    }
}

/**
 * 一个字的拼音，可能是多音字
 */
data class Pinyin(val readings: List<String>) {
    constructor(reading: String) : this(listOf(reading))

    fun serializeToString(): String =
        readings.joinToString(separator = PinyinConverter.PINYIN_SPLITTER_MULTI) { it }

    companion object {
        fun deserializeFromString(string: String): Pinyin =
            Pinyin(string.split(PinyinConverter.PINYIN_SPLITTER_MULTI_CHAR))
    }
}

