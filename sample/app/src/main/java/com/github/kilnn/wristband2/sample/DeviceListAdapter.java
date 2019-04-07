package com.github.kilnn.wristband2.sample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.polidea.rxandroidble2.scan.ScanResult;

import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends BaseAdapter {

    private List<ScanResult> mDatas;

    public DeviceListAdapter() {
        mDatas = new ArrayList<>(10);
    }

    public void clear() {
        mDatas.clear();
        notifyDataSetChanged();
    }

    public void add(ScanResult scanResult) {
        int existIndex = -1;
        for (int i = 0; i < mDatas.size(); i++) {
            ScanResult r = mDatas.get(i);
            if (r.getBleDevice().getMacAddress().equals(scanResult.getBleDevice().getMacAddress())) {
                existIndex = i;
                break;
            }
        }
        if (existIndex == -1) {
            mDatas.add(scanResult);
        } else {
            mDatas.set(existIndex, scanResult);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_list, parent, false);
            holder = new ViewHolder();
            holder.tv_address = convertView.findViewById(R.id.tv_address);
            holder.tv_name = convertView.findViewById(R.id.tv_name);
            holder.tv_rssi = convertView.findViewById(R.id.tv_rssi);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ScanResult result = mDatas.get(position);
        holder.tv_address.setText("MAC : " + result.getBleDevice().getMacAddress());
        holder.tv_name.setText("NAME : " + result.getBleDevice().getName());
        holder.tv_rssi.setText("RSSI : " + result.getRssi());
        return convertView;
    }

    private class ViewHolder {
        TextView tv_address;
        TextView tv_name;
        TextView tv_rssi;
    }
}
