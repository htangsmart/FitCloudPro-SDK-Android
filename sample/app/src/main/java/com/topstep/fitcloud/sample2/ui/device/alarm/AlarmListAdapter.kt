package com.topstep.fitcloud.sample2.ui.device.alarm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.ItemAlarmListBinding
import com.topstep.fitcloud.sample2.utils.FormatterUtil
import com.topstep.fitcloud.sdk.v2.model.settings.FcAlarm

class AlarmListAdapter(private val helper: AlarmHelper) : RecyclerView.Adapter<AlarmListAdapter.ItemViewHolder>() {

    var listener: Listener? = null

    var sources: List<FcAlarm>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemAlarmListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val alarm = sources?.get(position) ?: return
        val context = holder.itemView.context

        if (helper.is24HourFormat(context)) {
            holder.viewBind.tvAmPm.visibility = View.GONE
            holder.viewBind.tvTime.text = FormatterUtil.hmm(alarm.hour, alarm.minute)
        } else {
            holder.viewBind.tvAmPm.visibility = View.VISIBLE
            var hour = alarm.hour
            if (hour < 12) { //AM
                holder.viewBind.tvAmPm.setText(R.string.ds_alarm_am)
                if (hour == 0) {
                    hour = 12
                }
            } else {//PM
                holder.viewBind.tvAmPm.setText(R.string.ds_alarm_pm)
                if (hour > 12) {
                    hour -= 12
                }
            }
            holder.viewBind.tvTime.text = FormatterUtil.hmm(hour, alarm.minute)
        }
        holder.viewBind.tvLabel.text = alarm.label
        holder.viewBind.tvRepeat.text = helper.repeatToSimpleStr(context, alarm.repeat)
        holder.viewBind.switchIsEnabled.setOnCheckedChangeListener(null)
        holder.viewBind.switchIsEnabled.isChecked = alarm.isEnabled
        holder.viewBind.switchIsEnabled.setOnCheckedChangeListener { _, isChecked -> //Copy the array, excluding the alarm to be deleted
            listener?.onItemModified(holder.bindingAdapterPosition, alarm.clone().apply {
                isEnabled = isChecked
            })
        }
        holder.viewBind.imgDelete.setOnClickListener {
            listener?.onItemDelete(holder.bindingAdapterPosition)
        }
        holder.viewBind.layoutContent.setOnClickListener {
            listener?.onItemClick(holder.bindingAdapterPosition, alarm)
        }
    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    interface Listener {
        fun onItemModified(position: Int, alarmModified: FcAlarm)
        fun onItemClick(position: Int, alarm: FcAlarm)
        fun onItemDelete(position: Int)
    }

    class ItemViewHolder(val viewBind: ItemAlarmListBinding) : RecyclerView.ViewHolder(viewBind.root)

}