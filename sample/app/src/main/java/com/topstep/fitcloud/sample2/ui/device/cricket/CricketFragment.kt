package com.topstep.fitcloud.sample2.ui.device.cricket

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.topstep.fitcloud.sample2.MyApplication
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.FragmentCricketBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.di.internal.SingleInstance
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.await
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CricketFragment : BaseFragment(R.layout.fragment_cricket), CricketDialogFragment.Listener {

    private fun getJsonAdapter(): JsonAdapter<List<CricketInfo>> {
        val type = Types.newParameterizedType(List::class.java, CricketInfo::class.java)
        return moshi.adapter(type)
    }

    private val deviceManager = Injector.getDeviceManager()
    private val viewBind: FragmentCricketBinding by viewBinding()
    private val moshi: Moshi = SingleInstance.moshi

    //Read data from caches
    private val sources = runBlocking {
        try {
            getJsonAdapter().readFile(MyApplication.instance, CACHE_FILE)
        } catch (e: Exception) {
            //do nothing
            null
        }
    }?.toMutableList() ?: ArrayList()

    private val adapter = CricketAdapter(sources)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBind.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewBind.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        viewBind.recyclerView.adapter = adapter

        viewBind.btnAdd.clickTrigger {
            CricketDialogFragment.newInstance().show(childFragmentManager, null)
        }
        viewBind.btnSave.clickTrigger {
            lifecycleScope.launch {
                try {
                    saveData()
                    lifecycleScope.launchWhenStarted {
                        promptToast.showInfo("Success")
                    }
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        lifecycleScope.launchWhenStarted {
                            promptToast.showInfo("Fail")
                        }
                    }
                }
            }
        }
        adapter.listener = object : CricketAdapter.Listener {
            override fun onItemClick(item: CricketInfo, position: Int) {
                CricketDialogFragment.newInstance(item, position).show(childFragmentManager, null)
            }
        }
    }

    private suspend fun saveData() {
        val selected = adapter.selected.toList()
        //先发送Upcoming
        val matches = ArrayList<CricketInfo>()
        for (i in selected.indices) {
            val item = sources.getOrNull(selected[i])
            if (item != null && item.state == CricketInfo.State.UPCOMING) {
                matches.add(item)
            }
        }
        if (matches.isNotEmpty()) {
            deviceManager.specialFeature.setCricketUpcomingMatches(matches).await()
            Timber.i("send upcoming matches")
            Timber.i(getJsonAdapter().toJson(matches))
        }

        //在发送Live
        matches.clear()
        for (i in selected.indices) {
            val item = sources.getOrNull(selected[i])
            if (item != null && item.state == CricketInfo.State.LIVE) {
                matches.add(item)
            }
        }
        if (matches.isNotEmpty()) {
            deviceManager.specialFeature.setCricketLiveMatches(matches).await()
            Timber.i("send live matches")
            Timber.i(getJsonAdapter().toJson(matches))
        }

        //在发送Result
        matches.clear()
        for (i in selected.indices) {
            val item = sources.getOrNull(selected[i])
            if (item != null && item.state == CricketInfo.State.RESULT) {
                matches.add(item)
            }
        }
        if (matches.isNotEmpty()) {
            deviceManager.specialFeature.setCricketMatchesResults(matches).await()
            Timber.i("send result matches")
            Timber.i(getJsonAdapter().toJson(matches))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.listener = null
    }

    override fun onDialogAdd(info: CricketInfo) {
        sources.add(info)
        adapter.notifyDataSetChanged()
        getJsonAdapter().writeFile(requireContext(), CACHE_FILE, sources)
    }

    override fun onDialogEdit(info: CricketInfo, position: Int) {
        sources[position] = info
        adapter.notifyDataSetChanged()
        getJsonAdapter().writeFile(requireContext(), CACHE_FILE, sources)
    }

    override fun onDialogDelete(position: Int) {
        sources.removeAt(position)
        adapter.notifyDataSetChanged()
        getJsonAdapter().writeFile(requireContext(), CACHE_FILE, sources)
    }

    companion object {
        private const val CACHE_FILE = "cricket_cache_file"
        val timeFormat = SimpleDateFormat("yyyy-M-d H:m", Locale.US)
    }
}