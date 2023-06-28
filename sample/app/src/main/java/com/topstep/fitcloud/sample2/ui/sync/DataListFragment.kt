package com.topstep.fitcloud.sample2.ui.sync

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.topstep.fitcloud.sample2.R
import com.topstep.fitcloud.sample2.databinding.ItemDataListBinding
import com.topstep.fitcloud.sample2.di.Injector
import com.topstep.fitcloud.sample2.ui.base.BaseFragment
import java.text.SimpleDateFormat
import java.util.*

abstract class DataListFragment<T> : BaseFragment() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    protected val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    protected val syncDataRepository = Injector.getSyncDataRepository()
    private var selectDate: Date = Date()
    private lateinit var adapter: DataListAdapter<T>
    protected abstract val valueFormat: DataListAdapter.ValueFormat<T>
    protected open val layoutId = R.layout.fragment_data_list
    protected val authedUserId = Injector.requireAuthedUserId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = DataListAdapter(valueFormat)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutId, container, false)
    }

    protected lateinit var btnDate: Button
    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnDate = view.findViewById(R.id.btn_date)
        recyclerView = view.findViewById(R.id.recycler_view)
        btnDate.clickTrigger {
            datePicker()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter
        loadData(selectDate)
    }

    private fun datePicker() {
        val dialog = DatePickerDialog(
            requireContext(), onDateSetListener,
            selectDate.year + 1900, selectDate.month, selectDate.date
        )
        dialog.show()
    }

    private val onDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        val date = Date()
        date.year = year - 1900
        date.month = month
        date.date = dayOfMonth
        loadData(date)
    }

    private fun loadData(date: Date) {
        this.selectDate = date
        btnDate.text = dateFormat.format(date)
        adapter.sources = queryData(date)
        adapter.notifyDataSetChanged()
    }

    protected abstract fun queryData(date: Date): List<T>?
}


class DataListAdapter<T>(
    private val valueFormat: ValueFormat<T>
) : RecyclerView.Adapter<DataListAdapter.ItemViewHolder>() {

    var sources: List<T>? = null

    var listener: Listener<T>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            ItemDataListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = sources?.get(position) ?: return
        holder.viewBind.text.text = valueFormat.format(holder.itemView.context, item)

        holder.itemView.clickTrigger {
            val actionPosition = holder.bindingAdapterPosition
            if (actionPosition != RecyclerView.NO_POSITION) {
                listener?.onItemClick(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return sources?.size ?: 0
    }

    interface ValueFormat<T> {
        fun format(context: Context, obj: T): String
    }

    class ItemViewHolder(val viewBind: ItemDataListBinding) : RecyclerView.ViewHolder(viewBind.root)

    interface Listener<T> {
        fun onItemClick(item: T)
    }
}