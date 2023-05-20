package com.topstep.fitcloud.sample2.ui.device.bind

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.ItemOtherDeviceDataBinding

class OtherDevicesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var bondedInfo = SizeCount()
    var bonded: List<ScanDevice>? = null
        set(value) {
            field = value
            bondedInfo.upgrade(value)
        }

    private var connectedInfo = SizeCount()
    var connected: List<ScanDevice>? = null
        set(value) {
            field = value
            connectedInfo.upgrade(value)
        }

    var listener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_TYPE_TITLE -> TitleViewHolder(layoutInflater.inflate(R.layout.item_other_device_title, parent, false))
            ITEM_TYPE_NONE -> NoneViewHolder(layoutInflater.inflate(R.layout.item_other_device_none, parent, false))
            ITEM_TYPE_DATA -> DataViewHolder(ItemOtherDeviceDataBinding.inflate(layoutInflater, parent, false))
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TitleViewHolder) {
            if (position == 0) {
                holder.setTitle(R.string.device_bonded)
            } else {
                holder.setTitle(R.string.device_connected)
            }
        } else if (holder is DataViewHolder) {
            val device = if (position < bondedInfo.count) {
                //data in bonded
                bonded?.get(position - 1)!!
            } else {
                //data in connected
                connected?.get(position - bondedInfo.count - 1)!!
            }
            holder.bind(device)
            holder.itemView.setOnClickListener {
                listener?.onItemClick(device)
            }
        }
    }

    override fun getItemCount(): Int {
        return bondedInfo.count + connectedInfo.count
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < bondedInfo.count) {
            if (position == 0) {
                ITEM_TYPE_TITLE
            } else {
                if (bondedInfo.size == 0) {
                    ITEM_TYPE_NONE
                } else {
                    ITEM_TYPE_DATA
                }
            }
        } else {
            if (position - bondedInfo.count == 0) {
                ITEM_TYPE_TITLE
            } else {
                if (connectedInfo.size == 0) {
                    ITEM_TYPE_NONE
                } else {
                    ITEM_TYPE_DATA
                }
            }
        }
    }

    interface Listener {
        fun onItemClick(device: ScanDevice)
    }

    private class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setTitle(@StringRes resId: Int) {
            (itemView as TextView).setText(resId)
        }
    }

    private class NoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private class DataViewHolder(val viewBind: ItemOtherDeviceDataBinding) : RecyclerView.ViewHolder(viewBind.root) {
        fun bind(device: ScanDevice) {
            viewBind.tvName.text = if (device.name.isNullOrEmpty()) {
                DeviceBindFragment.UNKNOWN_DEVICE_NAME
            } else {
                device.name
            }
            viewBind.tvAddress.text = device.address
        }
    }

    private class SizeCount(
        var size: Int = 0,
        var count: Int = 2//There is no data initially, so it is 2(1 title view and 1 empty view)
    ) {
        fun upgrade(list: List<*>?) {
            size = list?.size ?: 0
            count = if (size == 0) {
                //When there is no actual data, display 1 title view and 1 empty view
                2
            } else {
                //When there is data, add 1 more title view
                size + 1
            }
        }
    }

    companion object {
        private const val ITEM_TYPE_TITLE = 0
        private const val ITEM_TYPE_NONE = 1
        private const val ITEM_TYPE_DATA = 2
        fun devices(devices: Collection<BluetoothDevice>?): List<ScanDevice>? {
            if (devices.isNullOrEmpty()) return null
            val list = ArrayList<ScanDevice>()
            for (device in devices) {
                list.add(ScanDevice(device.address, device.name, 0))
            }
            return list
        }
    }
}