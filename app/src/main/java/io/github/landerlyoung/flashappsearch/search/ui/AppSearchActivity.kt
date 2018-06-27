package io.github.landerlyoung.flashappsearch.search.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.landerlyoung.flashappsearch.R
import io.github.landerlyoung.flashappsearch.databinding.ActivityAppSearchBinding
import io.github.landerlyoung.flashappsearch.databinding.AppInfoBinding
import io.github.landerlyoung.flashappsearch.search.vm.AppSearchViewModel

class AppSearchActivity : AppCompatActivity() {

    lateinit var viewModel: AppSearchViewModel
    private lateinit var adapter: Adapter

    val useAnimation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[AppSearchViewModel::class.java]

        DataBindingUtil.setContentView<ActivityAppSearchBinding>(
                this,
                R.layout.activity_app_search
        ).let {
            it.setLifecycleOwner(this)
            it.vm = viewModel
            initRecyclerView(it.appList)
        }

        viewModel.resultApps.observe(this, Observer { it: List<Pair<String, CharSequence>>? ->
            adapter.setData(it ?: listOf())
        })
    }

    private fun initRecyclerView(rv: RecyclerView) {
        adapter = Adapter()
        val lm = GridLayoutManager(this, 5)
        rv.layoutManager = lm
        rv.adapter = adapter
    }

    private inner class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
            DataBindingUtil.inflate<AppInfoBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.app_info, parent, false
            ).root
    ) {
        private val binding = DataBindingUtil.getBinding<AppInfoBinding>(itemView)!!
        private val packageName = MutableLiveData<String>()
        private val appInfo = Transformations.switchMap(packageName) {
            viewModel.queryAppInfo(it)
        }!!

        init {
            appInfo.observe(this@AppSearchActivity, Observer {
                binding.appIcon.setImageDrawable(it)
            })
            itemView.setOnClickListener {
                packageName.value?.let {
                    packageManager.getLaunchIntentForPackage(it)?.let {
                        startActivity(it)
                    }
                }
            }
        }

        fun setData(appInfo: Pair<String, CharSequence>) {
            this.packageName.value = appInfo.first
            binding.appLabel.text = appInfo.second
        }
    }

    private inner class Adapter : RecyclerView.Adapter<VH>() {
        private var data: List<Pair<String, CharSequence>> = listOf()

        fun setData(list: List<Pair<String, CharSequence>>) {
            val oldList = data
            val newList = list
            data = newList

            if (!useAnimation) {
                notifyDataSetChanged()
            } else {
                DiffUtil.calculateDiff(object : DiffUtil.Callback() {

                    override fun getOldListSize() = oldList.size

                    override fun getNewListSize() = newList.size

                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                            oldList[oldItemPosition].first == newList[newItemPosition].first

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                            oldList[oldItemPosition].first == newList[newItemPosition].first
                }).dispatchUpdatesTo(this)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(parent)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.setData(data[position])
        }
    }
}
