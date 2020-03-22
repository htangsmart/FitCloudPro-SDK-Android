package com.github.kilnn.wristband2.sample;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.kilnn.wristband2.sample.utils.AndPermissionHelper;
import com.github.kilnn.wristband2.sample.utils.Utils;
import com.htsmart.wristband2.WristbandApplication;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * MainActivity. Be used for scan bluetooth device
 */
public class MainActivity extends BaseActivity {

    public static final String EXTRA_DEVICE = "device";

    /**
     * Views and Datas
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DeviceListAdapter mAdapter;
    private RxBleClient mRxBleClient;
    private Disposable mScanDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRxBleClient = WristbandApplication.getRxBleClient();
        initView();
        startScanning();
    }

    private void initView() {
        mSwipeRefreshLayout = findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startScanning();
            }
        });

        ListView listView = findViewById(R.id.list_view);
        mAdapter = new DeviceListAdapter();
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ScanResult result = (ScanResult) mAdapter.getItem(i);
                BluetoothDevice device = result.getBleDevice().getBluetoothDevice();
                Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
                intent.putExtra(EXTRA_DEVICE, device);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_scan);
        boolean scanning = mScanDisposable != null && !mScanDisposable.isDisposed();
        if (scanning) {
            menuItem.setTitle(R.string.action_stop_scan);
        } else {
            menuItem.setTitle(R.string.action_scan);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_scan) {
            boolean scanning = mScanDisposable != null && !mScanDisposable.isDisposed();
            if (scanning) {
                stopScanning();
            } else {
                startScanning();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Start scan
     */
    private void startScanning() {
        mAdapter.clear();
        if (Utils.checkLocationForBle(this)) {
            AndPermissionHelper.blePermissionRequest(this, new AndPermissionHelper.AndPermissionHelperListener1() {
                @Override
                public void onSuccess() {
                    ScanSettings scanSettings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build();

                    mSwipeRefreshLayout.setRefreshing(true);
                    invalidateOptionsMenu();

                    mScanDisposable = mRxBleClient.scanBleDevices(scanSettings)
                            .subscribe(new Consumer<ScanResult>() {
                                @Override
                                public void accept(ScanResult scanResult) throws Exception {
                                    mAdapter.add(scanResult);
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    stopScanning();
                                }
                            });
                }
            });
        }
    }

    /**
     * Stop scan
     */
    private void stopScanning() {
        if (mScanDisposable != null)
            mScanDisposable.dispose();
        mSwipeRefreshLayout.setRefreshing(false);
        invalidateOptionsMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScanning();
    }
}
