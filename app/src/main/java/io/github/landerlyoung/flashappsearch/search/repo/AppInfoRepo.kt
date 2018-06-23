package io.github.landerlyoung.flashappsearch.search.repo

import android.annotation.SuppressLint
import android.arch.core.executor.ArchTaskExecutor
import android.util.Log
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
object AppInfoRepo {
    private const val TAG = "AppInfoRepo"

    @SuppressLint("RestrictedApi")
    fun prepareAppInfo() {
        ArchTaskExecutor.getIOThreadExecutor().execute {
            val db = PinyinDataBase.createDb(App.context)
            val dao = db.pinyinDao()

            Log.i(TAG, dao.queryPinyin("你").toString())
            Log.i(TAG, dao.queryPinyin("好").toString())
            Log.i(TAG, dao.queryPinyin("重").toString())
        }
    }
}