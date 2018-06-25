package io.github.landerlyoung.flashappsearch.search.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.LruCache
import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.landerlyoung.flashappsearch.R
import io.github.landerlyoung.flashappsearch.databinding.ActivityAppSearchBinding
import io.github.landerlyoung.flashappsearch.databinding.AppInfoBinding
import io.github.landerlyoung.flashappsearch.search.model.Input
import io.github.landerlyoung.flashappsearch.search.model.T9
import io.github.landerlyoung.flashappsearch.search.repo.AppNameRepo

class AppSearchActivity : AppCompatActivity() {
    val inputText = MutableLiveData<CharSequence>()
    private val resultApps = Transformations.switchMap(inputText, {
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

    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityAppSearchBinding>(
                this,
                R.layout.activity_app_search
        ).let {
            it.setLifecycleOwner(this)
            it.ui = this
            initRecyclerView(it.appList)
        }

        AppNameRepo.quickInit(this)

        resultApps.observe(this, Observer {
            adapter.setData(it ?: listOf())
        })
    }

    private fun initRecyclerView(rv: RecyclerView) {
        adapter = Adapter(this)
        val lm = GridLayoutManager(this, 5)
        rv.layoutManager = lm
        rv.adapter = adapter
    }
}

private class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        DataBindingUtil.inflate<AppInfoBinding>(
                LayoutInflater.from(parent.context),
                R.layout.app_info, parent, false
        ).root
) {
    val binding = DataBindingUtil.getBinding<AppInfoBinding>(itemView)!!

    fun setData(info: Pair<Drawable?, CharSequence?>) {
        binding.appIcon.setImageDrawable(info.first)
        binding.appLabel.text = info.second
    }
}

private class Adapter(context: Context) : RecyclerView.Adapter<VH>() {
    private val data = mutableListOf<String>()
    private val appInfoCache = object : LruCache<String, Pair<Drawable?, CharSequence?>>(60) {
        val packageManager = context.packageManager
        override fun create(key: String): Pair<Drawable?, CharSequence?> {
            val info = packageManager.getApplicationInfo(key, 0)
            return info?.let {
                Pair(packageManager.getApplicationIcon(key),
                        packageManager.getApplicationLabel(it))

            } ?: Pair<Drawable?, CharSequence?>(null, null)
        }
    }

    fun setData(list: List<String>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.setData(appInfoCache[data[position]])
    }
}
