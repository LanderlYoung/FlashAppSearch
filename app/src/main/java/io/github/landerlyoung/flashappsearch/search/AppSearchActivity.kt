package io.github.landerlyoung.flashappsearch.search

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.github.landerlyoung.flashappsearch.R
import io.github.landerlyoung.flashappsearch.databinding.ActivityAppSearchBinding
import io.github.landerlyoung.flashappsearch.search.model.Input
import io.github.landerlyoung.flashappsearch.search.model.T9
import io.github.landerlyoung.flashappsearch.search.repo.AppNameRepo

class AppSearchActivity : AppCompatActivity() {
    val inputText = MutableLiveData<CharSequence>()
    val resultApps = Transformations.switchMap(inputText, {
        AppNameRepo.queryApp(it.map {
            when (it) {
                '0' -> T9.k0
                '1' -> T9.k1
                '2' -> T9.k2
                '3' -> T9.k3
                '4' -> T9.k4
                '5' -> T9.k5
                '6' -> T9.k6
                '7' -> T9.k7
                '8' -> T9.k8
                '9' -> T9.k9
                else -> Input.emptyInput
            }
        })
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityAppSearchBinding>(
                this,
                R.layout.activity_app_search
        ).let {
            it.ui = this
        }

        resultApps.observe(this, Observer {
            Log.i("AppSearchActivity", it.toString())
        })
    }
}
