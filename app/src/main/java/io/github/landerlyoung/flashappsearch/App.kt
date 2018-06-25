package io.github.landerlyoung.flashappsearch

import android.annotation.SuppressLint
import android.app.Application
import android.arch.core.executor.ArchTaskExecutor
import android.arch.core.executor.TaskExecutor
import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper

/**
 * <pre>
 * Author: taylorcyang@tencent.com
 * Date:   2018-06-23
 * Time:   12:24
 * Life with Passion, Code with Creativity.
 * </pre>
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        configArch()
        app = this
    }


    @SuppressLint("RestrictedApi")
    private fun configArch() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {

            private val mainHandler = Handler(Looper.getMainLooper())

            override fun executeOnDiskIO(runnable: Runnable) {
                executors().execute(runnable)
            }

            override fun isMainThread(): Boolean {
                return Looper.myLooper() == Looper.getMainLooper()
            }

            override fun postToMainThread(runnable: Runnable) {
                mainHandler.post(runnable)
            }
        })
    }

    companion object {
        fun executors() = AsyncTask.THREAD_POOL_EXECUTOR!!

        lateinit var app: App
            private set

        val context: Context
            get() = app
    }
}