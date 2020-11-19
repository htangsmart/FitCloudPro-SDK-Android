package com.github.kilnn.wristband2.sample.dial.custom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.BuildConfig;
import com.github.kilnn.wristband2.sample.MyApplication;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.dfu.FileUtils;
import com.github.kilnn.wristband2.sample.dial.custom.bean.DialCustom;
import com.github.kilnn.wristband2.sample.dial.custom.util.DialUtils;
import com.github.kilnn.wristband2.sample.net.GlobalApiClient;
import com.github.kilnn.wristband2.sample.utils.AndPermissionHelper;
import com.github.kilnn.wristband2.sample.widget.DataLceView;
import com.google.android.material.tabs.TabLayout;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;
import com.htsmart.wristband2.bean.DialBinInfo;
import com.htsmart.wristband2.dial.DialDrawer;
import com.htsmart.wristband2.dial.DialView;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class DialCustomActivity extends BaseActivity {
    private final String TAG = "DialCustomActivity";

    static {
        DialView.setEngine(new MyDialViewEngine());
    }

    private WristbandManager mWristbandManager = WristbandApplication.getWristbandManager();
    private GlobalApiClient mApiClient = MyApplication.getApiClient();

    @BindView(R.id.lce_view) DataLceView mDataLceView;
    @BindView(R.id.dial_view) DialView mDialView;
    @BindView(R.id.tab_layout) TabLayout mTabLayout;
    @BindView(R.id.view_pager) ViewPager mViewPager;

    private DialGridView mBgGridView;
    private DialGridView mStyleGridView;//样式列表决定该界面是否显示
    private DialGridView mPositionGridView;

    private DialDrawer.Shape mShape;
    private List<DialCustom> mDialCustoms;
    private List<Uri> mBackgroundUris;
    private List<DialDrawer.Position> mPositions = DialGridData.loadPositions();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dial_custom);
        ButterKnife.bind(this);
        getSupportActionBar().setTitle(R.string.dial_custom_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setOffscreenPageLimit(3);
        mBgGridView = new DialGridView(this, true);
        mBgGridView.getAdapter().setListener(mBgListener);
        mStyleGridView = new DialGridView(this, false);
        mStyleGridView.getAdapter().setListener(mStyleListener);
        mPositionGridView = new DialGridView(this, false);
        mPositionGridView.getAdapter().setListener(mPositionListener);
        mViewPager.setAdapter(new DialPagerAdapter(mBgGridView, mStyleGridView, mPositionGridView));
        mDataLceView.setLoadingListener(new DataLceView.LoadingListener() {
            @Override
            public void lceLoad() {
                refresh();
            }
        });
        refresh();
    }

    private int[] mBgGridViewLocation = new int[2];

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN && mBgGridView.getAdapter().isDeleteMode()) {
            mBgGridView.getLocationInWindow(mBgGridViewLocation);
            float left = mBgGridViewLocation[0];
            float right = left + mBgGridView.getWidth();
            float top = mBgGridViewLocation[1];
            float bottom = top + mBgGridView.getHeight();
            float x = ev.getX();
            float y = ev.getY();
            if (x < left || x > right || y < top || y > bottom) {
                //点击事件不在mBgGridView内部
                mBgGridView.getAdapter().exitDeleteMode();
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private DialGridView.Listener mBgListener = new DialGridView.Listener() {
        @Override
        public void onSelect(DialGridData data, int position) {
            adjustDatas();
        }

        @Override
        public void onDelete(DialGridData data, int position) {
            Uri uri = data.getBackgroundUri();
            if (mBackgroundUris != null) {
                mBackgroundUris.remove(uri);
            }
            File file = FileUtils.getFile(DialCustomActivity.this, uri);
            if (file != null) {
                file.delete();
            }
            adjustDatas();
        }

        @Override
        public void onAddClick() {
            //添加图片
            if (mShape == null) {
                Log.w(TAG, "mBgListener onAddClick : mShape=null");
                return;
            }
            mNewAvatarUri = null;//clear
            AndPermissionHelper.cameraRequest(DialCustomActivity.this, new AndPermissionHelper.AndPermissionHelperListener1() {
                @Override
                public void onSuccess() {
                    Matisse.from(DialCustomActivity.this)
                            .choose(MimeType.ofImage())
                            .capture(true)
                            .captureStrategy(new CaptureStrategy(false, BuildConfig.FileAuthorities))
                            .countable(false)
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                            .thumbnailScale(0.85f)
                            .imageEngine(new MatisseGlideEngine())
                            .forResult(REQUEST_CODE_CHOOSE);
                }
            });
        }
    };

    private static final int REQUEST_CODE_CHOOSE = 1;
    private static final int REQUEST_CODE_CROP_PHOTO = 2;
    private Uri mNewAvatarUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE) {
            if (resultCode == Activity.RESULT_OK) {
                boolean success = false;
                if (data != null) {
                    List<Uri> uris = Matisse.obtainResult(data);
                    if (uris != null && uris.size() > 0 && uris.get(0) != null) {
                        Uri originalFileUri = uris.get(0);
                        mNewAvatarUri = DialGridData.getOutputBackgroundUri(this, mShape);
                        if (mNewAvatarUri != null) {
                            Intent intent = DialGridData.getCropIntent(originalFileUri, mNewAvatarUri, mShape);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            try {
                                startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
                                success = true;
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (!success) {
                    toast(R.string.photo_select_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_CROP_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                File file = FileUtils.getFile(DialCustomActivity.this, mNewAvatarUri);
                if (mNewAvatarUri != null && file != null && file.exists()) {
                    if (mBackgroundUris == null) {
                        mBackgroundUris = new ArrayList<>();
                    }
                    //得到新图片
                    mBackgroundUris.add(mNewAvatarUri);
                    adjustDatas();
                } else {
                    toast(R.string.photo_select_failed);
                }
            }
        }
    }

    private DialGridView.Listener mStyleListener = new DialGridView.Listener() {
        @Override
        public void onSelect(DialGridData data, int position) {
            adjustDatas();
        }
    };

    private DialGridView.Listener mPositionListener = new DialGridView.Listener() {
        @Override
        public void onSelect(DialGridData data, int position) {
            adjustDatas();
        }
    };

    private Uri mSelectBackgroundUri;
    private DialCustom mSelectDialCustom;

    private void adjustDatas() {
        if (mBackgroundUris == null || mBackgroundUris.size() <= 0) {
            //没有背景图片，使用默认的图片
            mSelectBackgroundUri = DialUtils.getUriFromDrawableResId(DialCustomActivity.this, R.drawable.dial_default_bg);
        } else {
            int selectPosition = mBgGridView.getAdapter().getSelectPosition();
            if (selectPosition >= mBackgroundUris.size()) {
                selectPosition = 0;
            }
            mSelectBackgroundUri = mBackgroundUris.get(selectPosition);
        }

        if (mDialCustoms == null || mDialCustoms.size() <= 0) {
            throw new IllegalStateException();
        } else {
            int selectPosition = mStyleGridView.getAdapter().getSelectPosition();
            if (selectPosition >= mDialCustoms.size()) {
                selectPosition = 0;
            }
            mSelectDialCustom = mDialCustoms.get(selectPosition);
        }

        //位置的数据不会变，直接获取即可
        int selectPosition = mPositionGridView.getAdapter().getSelectPosition();
        DialDrawer.Position selectStylePosition = mPositions.get(selectPosition);

        //背景数据
        if (mBackgroundUris == null || mBackgroundUris.size() <= 0) {
            mBgGridView.getAdapter().setDatas(null);
        } else {
            List<DialGridData> backgroundList = new ArrayList<>(mBackgroundUris.size());
            for (Uri uri : mBackgroundUris) {
                backgroundList.add(new DialGridData(uri, mSelectDialCustom.getStyleUri(), selectStylePosition));
            }
            mBgGridView.getAdapter().setDatas(backgroundList);
        }
        mBgGridView.getAdapter().notifyDataSetChanged();

        //样式数据
        List<DialGridData> styleList = new ArrayList<>(mDialCustoms.size());
        for (DialCustom custom : mDialCustoms) {
            styleList.add(new DialGridData(mSelectBackgroundUri, custom.getStyleUri(), selectStylePosition));
        }
        mStyleGridView.getAdapter().setDatas(styleList);
        mStyleGridView.getAdapter().notifyDataSetChanged();

        //位置数据
        List<DialGridData> positionList = new ArrayList<>(mPositions.size());
        for (DialDrawer.Position position : mPositions) {
            positionList.add(new DialGridData(mSelectBackgroundUri, mSelectDialCustom.getStyleUri(), position));
        }
        mPositionGridView.getAdapter().setDatas(positionList);
        mPositionGridView.getAdapter().notifyDataSetChanged();

        mDialView.setBackgroundSource(mSelectBackgroundUri);
        mDialView.setStyleSource(mSelectDialCustom.getStyleUri());
        mDialView.setStylePosition(selectStylePosition);
    }

    @OnClick({R.id.btn_set})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_set:
                DialCustomDialogFragment.DialCustomParam param = new DialCustomDialogFragment.DialCustomParam();
                param.binUrl = mSelectDialCustom.getBinUrl();
                param.backgroundUri = mSelectBackgroundUri;
                param.styleUri = mSelectDialCustom.getStyleUri();
                param.shape = mShape;
                param.scaleType = mDialView.getBackgroundScaleType();
                param.position = mDialView.getStylePosition();
                DialCustomDialogFragment.newInstance(param)
                        .show(getSupportFragmentManager(), null);
                break;
        }
    }

    private volatile int mDeviceLcd;

    @SuppressLint("CheckResult")
    private void refresh() {
        //TODO 请求之前，最好检测下设备是否连接(WristbandManager#isConnected())，并且支持表盘升级(WristbandVersion#isExtDialUpgrade())

        DialBinInfo dialBinInfo=new DialBinInfo();
        dialBinInfo.setLcd(4);
        dialBinInfo.setToolVersion("1.4");
        mWristbandManager
                //Request device dial bin info
                .requestDialBinInfo()
                .onErrorReturnItem(dialBinInfo)
                .flatMapPublisher(new Function<DialBinInfo, Publisher<List<DialCustom>>>() {
                    @Override
                    public Publisher<List<DialCustom>> apply(DialBinInfo dialBinInfo) throws Exception {
                        mDeviceLcd = dialBinInfo.getLcd();
                        return mApiClient.getDialCustom(dialBinInfo.getLcd(), dialBinInfo.getToolVersion());
                    }
                })
                //Request service Support Dial
                .map(new Function<List<DialCustom>, List<DialCustom>>() {
                    @Override
                    public List<DialCustom> apply(List<DialCustom> dialCustoms) throws Exception {
                        //Filter local Support dial
                        return DialUtils.filterSupportStyles(DialCustomActivity.this, dialCustoms);
                    }
                })

                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                .doOnSubscribe(new Consumer<Subscription>() {
                    @Override
                    public void accept(Subscription subscription) throws Exception {
                        mDataLceView.lceShowLoading();
                    }
                })

                .subscribe(new Consumer<List<DialCustom>>() {
                    @Override
                    public void accept(List<DialCustom> dialCustoms) throws Exception {
                        mShape = DialDrawer.Shape.createFromLcd(mDeviceLcd);
                        mDialCustoms = dialCustoms;
                        if (mDialCustoms.size() <= 0) {
                            //TODO 没有数据，可能是后台未添加表盘，提示"暂不支持此手环升级"
                            mDataLceView.lceShowError(R.string.ds_dial_error_none_style);
                        } else {
                            //TODO 不支持的Shape类型，可能是APP或SDK尚未支持此形状的表盘，提示"暂不支持此手环升级，请尝试升级至最新版本"
                            if (mShape == null) {
                                mDataLceView.lceShowError(R.string.ds_dial_error_none_shape);
                            } else {
                                mDataLceView.lceShowContent();
                                //加载所有数据
                                mBgGridView.getAdapter().setShape(mShape);
                                mStyleGridView.getAdapter().setShape(mShape);
                                mPositionGridView.getAdapter().setShape(mShape);
                                mDialView.setShape(mShape);
                                mBackgroundUris = DialGridData.loadBackgrounds(DialCustomActivity.this, mShape);
                                adjustDatas();
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        //TODO 判断异常类型，区分是由于"手环未连接"，"手环不支持表盘升级"，还是"网络请求失败"等原因，根据不同原因可以显示不同的提示。
                        mDataLceView.lceShowError(R.string.tip_load_error);
                    }
                });
    }

}
