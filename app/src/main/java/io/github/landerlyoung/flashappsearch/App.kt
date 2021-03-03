package io.github.landerlyoung.flashappsearch

import android.annotation.SuppressLint
import android.app.Application
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

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
        private val _executors = ThreadPoolExecutor(
            4, 4, 1, TimeUnit.MINUTES, LinkedBlockingDeque<Runnable>()
        ).apply {
            allowCoreThreadTimeOut(true)
        }

        fun executors() = AsyncTask.SERIAL_EXECUTOR!!
//        fun executors() = _executors

        lateinit var app: App
            private set

        val context: Context
            get() = app
    }
}