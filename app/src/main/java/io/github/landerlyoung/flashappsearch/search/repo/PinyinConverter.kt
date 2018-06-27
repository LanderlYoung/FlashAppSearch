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
        private val chineseRegex = "[\\u3400-\\uD87E\\uDDD4]".toRegex(RegexOption.IGNORE_CASE)
        private val numberRegex = "\\d".toRegex()
        private val capitalCharsRegex = "[A-Z]".toRegex()
        private val blankSpaceRegex = "\\s+".toRegex()
        private val redundantSplitterRegex = "\\|{2,}".toRegex()
        private val nonCharRegex = "[^\\da-zA-Z\\|]".toRegex()


        const val PINYIN_SPLITTER_CHAR = '|'
        const val PINYIN_SPLITTER = PINYIN_SPLITTER_CHAR.toString()

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
                }.joinToString(separator = PINYIN_SPLITTER).let {
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
    fun hanzi2Pinyin(hanzi: CharSequence): String {
        val capitalized = capitalCharsRegex.replace(hanzi) {
            PINYIN_SPLITTER + it.value
        }
        val pinyin = chineseRegex.replace(capitalized) { mr ->
            val m = mr.value
            (pinyinCache[m] ?: m) + PINYIN_SPLITTER
        }
        val stripped =  nonCharRegex.replace(pinyin, PINYIN_SPLITTER)
        val noSpce = PINYIN_SPLITTER + blankSpaceRegex.replace(stripped, PINYIN_SPLITTER) + PINYIN_SPLITTER
        return redundantSplitterRegex.replace(noSpce, PINYIN_SPLITTER)
    }
}