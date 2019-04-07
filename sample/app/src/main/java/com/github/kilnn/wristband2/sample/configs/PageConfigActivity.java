package com.github.kilnn.wristband2.sample.configs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.utils.ViewHolderUtils;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;
import com.htsmart.wristband2.bean.WristbandConfig;
import com.htsmart.wristband2.bean.WristbandVersion;
import com.htsmart.wristband2.bean.config.PageConfig;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Page config.
 */
public class PageConfigActivity extends BaseActivity {

    private static final int[] PAGE_FLAGS = new int[]{
            PageConfig.FLAG_TIME,
            PageConfig.FLAG_STEP,
            PageConfig.FLAG_DISTANCE,
            PageConfig.FLAG_CALORIES,
            PageConfig.FLAG_SLEEP,
            PageConfig.FLAG_HEART_RATE,
            PageConfig.FLAG_OXYGEN,
            PageConfig.FLAG_BLOOD_PRESSURE,
            PageConfig.FLAG_WEATHER,
            PageConfig.FLAG_FIND_PHONE,
            PageConfig.FLAG_ID,
            PageConfig.FLAG_STOP_WATCH,
    };

    private WristbandManager mWristbandManager = WristbandApplication.getWristbandManager();

    private String[] PAGE_NAMES = null;
    private PageConfig mPageConfig;
    private List<PageConfigItem> mDatas;
    private PageConfigAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_config);
        PAGE_NAMES = getResources().getStringArray(R.array.page_names);

        TextView tv_page_config_suggest = findViewById(R.id.tv_page_config_suggest);
        ListView listView = findViewById(R.id.list_view);

        WristbandConfig wristbandConfig = mWristbandManager.getWristbandConfig();
        if (wristbandConfig != null) {
            WristbandVersion wristbandVersion = wristbandConfig.getWristbandVersion();

            if (wristbandVersion.isExtHidePageConfig()) {
                //Because of the special UI of the wristband, it is not recommended to configure the page.
                tv_page_config_suggest.setVisibility(View.VISIBLE);
            } else {
                listView.setVisibility(View.VISIBLE);
                mDatas = new ArrayList<>();
                mAdapter = new PageConfigAdapter();
                listView.setAdapter(mAdapter);

                PageConfig pageConfig = wristbandConfig.getPageConfig();

                for (int i = 0; i < PAGE_FLAGS.length; i++) {
                    int flag = PAGE_FLAGS[i];
                    if (wristbandVersion.isPageSupport(flag)) {
                        PageConfigItem item = new PageConfigItem();
                        item.flag = flag;
                        item.name = PAGE_NAMES[i];
                        item.enable = pageConfig.isFlagEnable(flag);
                        mDatas.add(item);
                    }
                }
                mAdapter.notifyDataSetChanged();

                mPageConfig = pageConfig;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("CheckResult")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            if (mPageConfig != null && mDatas != null && mDatas.size() > 0) {
                for (PageConfigItem pageConfigItem : mDatas) {
                    mPageConfig.setFlagEnable(pageConfigItem.flag, pageConfigItem.enable);
                }

                mWristbandManager.setPageConfig(mPageConfig)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                toast(R.string.operation_success);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.e("sample", "", throwable);
                                toast(R.string.operation_failed);
                            }
                        });
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class PageConfigItem {
        String name;
        int flag;
        boolean enable;
    }

    private class PageConfigAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_page_config, viewGroup, false);
            }

            TextView textView = ViewHolderUtils.get(view, R.id.text_view);
            CheckBox checkBox = ViewHolderUtils.get(view, R.id.check_box);

            textView.setText(mDatas.get(i).name);

            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(mDatas.get(i).enable);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mDatas.get(i).enable = b;
                }
            });
            return view;
        }
    }

}
