package com.topstep.fitcloud.sample2.ui.sync

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.data.entity.EcgRecordEntity
import com.topstep.fitcloud.sample2.databinding.FragmentEcgBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import com.topstep.fitcloud.sample2.ui.widget.EcgView
import com.topstep.fitcloud.sample2.utils.viewbinding.viewBinding
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class EcgFragment : BaseFragment(R.layout.fragment_ecg) {

    private val viewBind: FragmentEcgBinding by viewBinding()
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val syncDataRepository = Injector.getSyncDataRepository()
    private val authedUserId = Injector.requireAuthedUserId()
    private lateinit var adapter: DataListAdapter<EcgRecordEntity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = DataListAdapter(object : DataListAdapter.ValueFormat<EcgRecordEntity> {
            override fun format(context: Context, obj: EcgRecordEntity): String {
                return timeFormat.format(obj.time)
            }
        })
        adapter.sources = runBlocking { syncDataRepository.queryEcg(authedUserId) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.listener = object : DataListAdapter.Listener<EcgRecordEntity> {
            override fun onItemClick(item: EcgRecordEntity) {
                updateEcgDetail(item)
            }
        }
        viewBind.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewBind.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        viewBind.recyclerView.adapter = adapter
    }

    private fun updateEcgDetail(record: EcgRecordEntity) {
        viewBind.ecgView.clearData()
        viewBind.ecgView.mode = EcgView.MODE_NORMAL
        viewBind.ecgView.samplingRate = record.samplingRate
        viewBind.ecgView.setDataType(record.type)
        viewBind.ecgView.addDataAndScrollToLast(record.getIntArrays())
        viewBind.tvTime.text = timeFormat.format(record.time)
        if (record.type == EcgRecordEntity.Type.TI) {
            viewBind.tvSpeed.visibility = View.VISIBLE
            viewBind.tvAmplitude.visibility = View.VISIBLE
            viewBind.tvSpeed.text = getString(R.string.ecg_speed, viewBind.ecgView.speed)
            viewBind.tvAmplitude.text = getString(R.string.ecg_amplitude, viewBind.ecgView.amplitude)
        } else {
            viewBind.tvSpeed.visibility = View.GONE
            viewBind.tvAmplitude.visibility = View.GONE
        }
    }
}