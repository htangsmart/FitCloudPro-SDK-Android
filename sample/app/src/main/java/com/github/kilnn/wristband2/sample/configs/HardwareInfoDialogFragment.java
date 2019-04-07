package com.github.kilnn.wristband2.sample.configs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.utils.ListViewSectionedAdapter;
import com.github.kilnn.wristband2.sample.utils.ViewHolderUtils;
import com.htsmart.wristband2.bean.WristbandVersion;
import com.htsmart.wristband2.bean.config.PageConfig;

import java.util.ArrayList;
import java.util.List;


public class HardwareInfoDialogFragment extends AppCompatDialogFragment {

    private static final String EXTRA_INFO = "HardwareInfo";

    public static HardwareInfoDialogFragment newInstance(WristbandVersion version) {
        HardwareInfoDialogFragment fragment = new HardwareInfoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_INFO, new String(version.getBytes()));
        fragment.setArguments(bundle);
        return fragment;
    }

    private static List<GroupInfo> createInfoData(WristbandVersion version) {
        List<GroupInfo> groupInfos = new ArrayList<>();
        {
            GroupInfo info = new GroupInfo("Raw Info");
            groupInfos.add(info);
            info.addItem("project:" + version.getProject());
            info.addItem("hardware:" + version.getHardware());
            info.addItem("pageSupport:" + WristbandVersion.get_version_page_support(version.getRawVersion()));
            info.addItem("patch:" + version.getPatch());
            info.addItem("flash:" + version.getFlash());
            info.addItem("app:" + version.getApp());
            info.addItem("serial:" + version.getSerial());
            info.addItem("extension:" + version.getExtension());
        }

        {
            GroupInfo info = new GroupInfo("Module Support");
            groupInfos.add(info);
            info.addItem("Heart Rate:" + (version.isHeartRateEnabled() ? "Y" : "N"));
            info.addItem("Oxygen:" + (version.isOxygenEnabled() ? "Y" : "N"));
            info.addItem("Blood Pressure:" + (version.isBloodPressureEnabled() ? "Y" : "N"));
            info.addItem("Respiratory Rate:" + (version.isRespiratoryRateEnabled() ? "Y" : "N"));
            info.addItem("Weather:" + (version.isWeatherEnabled() ? "Y" : "N"));
            info.addItem("Ecg:" + (version.isEcgEnabled() ? "Y" : "N"));
            info.addItem("Sport:" + (version.isSportEnabled() ? "Y" : "N"));
            info.addItem("WeRun:" + (version.isWechatSportEnabled() ? "Y" : "N"));
            info.addItem("Platform 8762C:" + (version.isPlatform8762CEnabled() ? "Y" : "N"));
            info.addItem("Dynamic Heart Rate:" + (version.isDynamicHeartRateEnabled() ? "Y" : "N"));
        }

        {
            GroupInfo info = new GroupInfo("Page Support");
            groupInfos.add(info);
            info.addItem("Time:" + (version.isPageSupport(PageConfig.FLAG_TIME) ? "Y" : "N"));
            info.addItem("Step:" + (version.isPageSupport(PageConfig.FLAG_STEP) ? "Y" : "N"));
            info.addItem("Distance:" + (version.isPageSupport(PageConfig.FLAG_DISTANCE) ? "Y" : "N"));
            info.addItem("Calories:" + (version.isPageSupport(PageConfig.FLAG_CALORIES) ? "Y" : "N"));
            info.addItem("Sleep:" + (version.isPageSupport(PageConfig.FLAG_SLEEP) ? "Y" : "N"));
            info.addItem("Heart Rate:" + (version.isPageSupport(PageConfig.FLAG_HEART_RATE) ? "Y" : "N"));
            info.addItem("Oxygen:" + (version.isPageSupport(PageConfig.FLAG_OXYGEN) ? "Y" : "N"));
            info.addItem("Blood Pressure:" + (version.isPageSupport(PageConfig.FLAG_BLOOD_PRESSURE) ? "Y" : "N"));
            info.addItem("Weather:" + (version.isPageSupport(PageConfig.FLAG_WEATHER) ? "Y" : "N"));
            info.addItem("Find Phone:" + (version.isPageSupport(PageConfig.FLAG_FIND_PHONE) ? "Y" : "N"));
            info.addItem("ID:" + (version.isPageSupport(PageConfig.FLAG_ID) ? "Y" : "N"));
            info.addItem("StopWatch:" + (version.isPageSupport(PageConfig.FLAG_STOP_WATCH) ? "Y" : "N"));
        }

        {
            GroupInfo info = new GroupInfo("Extension Function");
            groupInfos.add(info);
            info.addItem("Hide Page Config:" + (version.isExtHidePageConfig() ? "Y" : "N"));
            info.addItem("ANCS Email:" + (version.isExtAncsEmail() ? "Y" : "N"));
            info.addItem("ANCS Viber&Telegram:" + (version.isExtAncsViberTelegram() ? "Y" : "N"));
        }

        return groupInfos;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String info = "";
        if (getArguments() != null) {
            info = getArguments().getString(EXTRA_INFO);
        }
        WristbandVersion version = null;
        if (!TextUtils.isEmpty(info)) {
            try {
                version = WristbandVersion.newInstance(info.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (version == null) return super.onCreateDialog(savedInstanceState);
        List<GroupInfo> groupInfos = createInfoData(version);

        return new AlertDialog.Builder(getContext())
                .setAdapter(new InnerAdapter(groupInfos), null)
                .create();
    }

    private static class GroupInfo {
        private String title;
        private List<String> items;

        private GroupInfo(String title) {
            this.title = title;
            items = new ArrayList<>();
        }

        private void addItem(String string) {
            items.add(string);
        }
    }

    private static class InnerAdapter extends ListViewSectionedAdapter {
        private List<GroupInfo> groupInfos;

        public InnerAdapter(List<GroupInfo> groupInfos) {
            this.groupInfos = groupInfos;
        }

        @Override
        protected int getSectionCount() {
            return groupInfos.size();
        }

        @Override
        protected boolean hasFooterInSection(int section) {
            return false;
        }

        @Override
        protected int getItemCountInSection(int section) {
            return groupInfos.get(section).items.size();
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int viewType = getItemViewType(position);
            final int section = positionToSection(position);
            GroupInfo info = groupInfos.get(section);

            if (viewType == VIEW_TYPE_HEADER) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_hardware_info_title, parent, false);
                }
                TextView tv_name = ViewHolderUtils.get(convertView, R.id.tv_name);
                tv_name.setText(info.title);
            } else if (viewType == VIEW_TYPE_ITEM) {
                int index = positionToSectionIndex(position);
                if (convertView == null) {
                    convertView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_hardware_info_item, parent, false);
                }
                TextView tv_name = ViewHolderUtils.get(convertView, R.id.tv_name);
                TextView tv_des = ViewHolderUtils.get(convertView, R.id.tv_des);
                String[] str = info.items.get(index).split(":");

                tv_name.setText(str[0]);
                tv_des.setText(str[1]);
            }
            return convertView;
        }
    }
}
