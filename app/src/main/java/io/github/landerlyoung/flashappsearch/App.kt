package io.github.landerlyoung.flashappsearch

import android.app.Application
import android.content.Context

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
        app = this
    }

    companion object {
        private lateinit var app: App

        val context: Context
            get() = app
    }
}