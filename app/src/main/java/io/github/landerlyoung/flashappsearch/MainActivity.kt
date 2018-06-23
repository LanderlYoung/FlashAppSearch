package io.github.landerlyoung.flashappsearch

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.github.landerlyoung.flashappsearch.search.repo.PinyinConverter
import io.github.landerlyoung.flashappsearch.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            PinyinConverter().apply {
                fun test(str: String) {
                    Log.i("MainActivity", "$str: ${hanzi2Pinyin(str)} ")
                }
                test("头重脚轻")
                test("HelloWorld")
                test("Hello你World好，世界")
                test("北极光")
            }
        }
    }
}
