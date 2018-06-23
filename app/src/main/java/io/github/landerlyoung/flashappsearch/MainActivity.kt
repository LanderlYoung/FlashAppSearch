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
                packageManager.getInstalledApplications(0)?.forEach {
                    val label = packageManager.getApplicationLabel(it)
                    Log.i("MainActivity", it.packageName +
                            ":" + label +
                            "->" + hanzi2Pinyin(label)
                    )
                }
            }
        }
    }
}
