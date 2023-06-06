package com.github.kilnn.wristband2.sample;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.github.kilnn.wristband2.sample.activemsg.ActiveMsgActivity;
import com.github.kilnn.wristband2.sample.alarm.AlarmListActivity;
import com.github.kilnn.wristband2.sample.configs.ConfigsActivity;
import com.github.kilnn.wristband2.sample.dfu.DfuActivity;
import com.github.kilnn.wristband2.sample.dial.component.DialComponentActivity;
import com.github.kilnn.wristband2.sample.dial.library.DialLibraryActivity;
import com.github.kilnn.wristband2.sample.mock.DbMock;
import com.github.kilnn.wristband2.sample.mock.User;
import com.github.kilnn.wristband2.sample.mock.UserMock;
import com.github.kilnn.wristband2.sample.realtimedata.RealTimeDataActivity;
import com.github.kilnn.wristband2.sample.sportpush.SportPushActivity;
import com.github.kilnn.wristband2.sample.syncdata.SyncDataActivity;
import com.github.kilnn.wristband2.sample.syncdata.db.SyncDataDao;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;
import com.htsmart.wristband2.bean.ConnectionError;
import com.htsmart.wristband2.bean.ConnectionState;
import com.htsmart.wristband2.bean.WristbandConfig;
import com.htsmart.wristband2.bean.WristbandNotification;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

@SuppressWarnings("ResultOfMethodCallIgnored")
@SuppressLint("CheckResult")
public class ConnectActivity extends BaseActivity {

    private static final String ACTION_RECONNECT = BuildConfig.APPLICATION_ID + ".action.reconnect";
    private static final String ACTION_DIAL_COMPONENT = BuildConfig.APPLICATION_ID + ".action.dialcomponent";

    private static final String TAG = "ConnectActivity";

    private WristbandManager mWristbandManager = WristbandApplication.getWristbandManager();
    private User mUser = UserMock.getLoginUser();

    private TextView mStateTv;
    private Button mConnectBtn;

    private BluetoothDevice mBluetoothDevice;
    private Disposable mStateDisposable;
    private Disposable mErrorDisposable;
    private ConnectionState mState = ConnectionState.DISCONNECTED;

    private SyncDataDao mSyncDataDao = MyApplication.getSyncDataDb().dao();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        mStateTv = findViewById(R.id.state_tv);
        mConnectBtn = findViewById(R.id.connect_btn);

        mBluetoothDevice = getIntent().getParcelableExtra(MainActivity.EXTRA_DEVICE);

        mStateDisposable = mWristbandManager.observerConnectionState()
                .subscribe(new Consumer<ConnectionState>() {
                    @Override
                    public void accept(ConnectionState connectionState) throws Exception {
                        if (connectionState == ConnectionState.DISCONNECTED) {
                            if (mWristbandManager.getRxBleDevice() == null) {
                                mStateTv.setText(R.string.state_active_disconnect);
                            } else {
                                if (mState == ConnectionState.CONNECTED) {
                                    mStateTv.setText(R.string.state_passive_disconnect);
                                } else {
                                    mStateTv.setText(R.string.state_connect_failed);
                                }
                            }
                            updateConnectBtn(true, true);
                        } else if (connectionState == ConnectionState.CONNECTED) {
                            mStateTv.setText(R.string.state_connect_success);
                            updateConnectBtn(false, true);
                            DbMock.setUserBind(ConnectActivity.this, mBluetoothDevice, mUser);
                            if (mWristbandManager.isBindOrLogin()) {
                                //If connect with bind mode, clear Today Step Data
                                toast(R.string.toast_connect_bind_tips);
                                mSyncDataDao.clearTodayStep();
                            } else {
                                toast(R.string.toast_connect_login_tips);
                            }

                            if (System.currentTimeMillis() - dialComponentTime < 60 * 1000) {
                                //Connect back within 1 minute, then setDialComponents
                                dialComponentTime = 0;
                                mWristbandManager.setDialComponents(
                                        dialComponentSpaceIndex,
                                        new byte[]{(byte) dialComponentStyleIndex}
                                ).onErrorComplete().subscribe();
                            }
                        } else {
                            mStateTv.setText(R.string.state_connecting);
                            updateConnectBtn(true, false);
                        }
                        mState = connectionState;
                    }
                });
        mErrorDisposable = mWristbandManager.observerConnectionError()
                .subscribe(new Consumer<ConnectionError>() {
                    @Override
                    public void accept(ConnectionError connectionError) throws Exception {
                        Log.w(TAG, "Connect Error occur and retry:" + connectionError.isRetry(), connectionError.getThrowable());
                    }
                });

        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWristbandManager.isConnected()) {
                    mWristbandManager.close();
                } else {
                    connect();
                }
            }
        });
        connect();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RECONNECT);
        filter.addAction(ACTION_DIAL_COMPONENT);
        registerReceiver(mReceiver, filter);
    }

    private long dialComponentTime = 0;
    private int dialComponentSpaceIndex = 0;
    private int dialComponentStyleIndex = 0;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_RECONNECT.equals(intent.getAction())) {
                connect();
            } else if (ACTION_DIAL_COMPONENT.equals(intent.getAction())) {
                //Save dial component params, at setting it after reconnect
                dialComponentTime = System.currentTimeMillis();
                dialComponentSpaceIndex = intent.getIntExtra("spaceIndex", 0);
                dialComponentStyleIndex = intent.getIntExtra("styleIndex", 0);
            }
        }
    };

    private void connect() {
        boolean isBind = DbMock.isUserBind(this, mBluetoothDevice, mUser);

        //If previously bind, use login mode
        //If haven't  bind before, use bind mode
        Log.d(TAG, "Connect device:" + mBluetoothDevice.getAddress() + " with user:" + mUser.getId()
                + " use " + (isBind ? "Login" : "Bind") + " mode");

        mWristbandManager.connect(mBluetoothDevice, String.valueOf(mUser.getId()), !isBind
                , mUser.isSex(), mUser.getAge(), mUser.getHeight(), mUser.getWeight());
    }

    private void updateConnectBtn(boolean showConnectText, boolean enable) {
        mConnectBtn.setText(showConnectText ? R.string.action_connect : R.string.action_disconnect);
        mConnectBtn.setEnabled(enable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStateDisposable.dispose();
        mErrorDisposable.dispose();
        mWristbandManager.close();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_connect, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_clear_bind) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_clear_bind_title)
                    .setMessage(R.string.dialog_clear_bind_msg)
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.action_sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DbMock.clearUserBind(ConnectActivity.this, mBluetoothDevice);
                        }
                    }).create().show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 1.configs
     */
    public void configs(View view) {
        startActivity(new Intent(this, ConfigsActivity.class));
    }

    /**
     * 2.alarm
     */
    public void alarm(View view) {
        startActivity(new Intent(this, AlarmListActivity.class));
    }

    /**
     * 3.Notification
     */
    public void notification(View view) {
        //This is a mock QQ notification.
        //In practice, you should use 'NotificationListenerService' to monitor QQ notification.
        WristbandNotification notification = new WristbandNotification();
        notification.setType(WristbandNotification.TYPE_QQ);
        notification.setContent("Dean:Hello!!!");
        if (mWristbandManager.isConnected()) {
            mWristbandManager.sendWristbandNotification(notification)
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
        } else {
            toast(R.string.toast_device_disconnected);
        }
    }

    /**
     * 4.Wristband Active Message
     */
    public void active_message(View view) {
        startActivity(new Intent(this, ActiveMsgActivity.class));
    }

    /**
     * 5.Real time data
     */
    public void real_time_data(View view) {
        startActivity(new Intent(this, RealTimeDataActivity.class));
    }

    /**
     * 6.Sync data
     */
    public void sync_data(View view) {
        startActivity(new Intent(this, SyncDataActivity.class));
    }

    /**
     * 7.DFU
     */
    public void dfu(View view) {
        startActivity(new Intent(this, DfuActivity.class));
    }

    /**
     * 8.Simple Command
     */
    public void simple_command(View view) {
        toast(R.string.tip_simple);
//        mWristbandManager.setUserInfo()
//        mWristbandManager.setExerciseTarget()
//        mWristbandManager.requestBattery()
//        mWristbandManager.findWristband()
//        mWristbandManager.resetWristband()
//        mWristbandManager.turnOffWristband()
//        mWristbandManager.restartWristband()
//        mWristbandManager.userUnBind()
//        mWristbandManager.setLanguage()
//        mWristbandManager.setWeather()
    }

    /**
     * 9.Dial library
     */
    public void dial_library(View view) {
        startActivity(new Intent(this, DialLibraryActivity.class));
    }

    /**
     * 10.Dial component
     */
    public void dial_component(View view) {
        startActivity(new Intent(this, DialComponentActivity.class));
    }

    /**
     * 11.Sport push
     */
    public void sport_push(View view) {
        if (mWristbandManager.isConnected()) {
            WristbandConfig config = mWristbandManager.getWristbandConfig();
            if (config.getWristbandVersion().isSportPushEnabled()) {
                startActivity(new Intent(this, SportPushActivity.class));
            } else {
                toast("UnSupport this function!!!");
            }
        } else {
            toast(R.string.device_disconnected);
        }
    }

    public static void sendReconnectAction(Context context) {
        context.sendBroadcast(new Intent(ACTION_RECONNECT));
    }

    public static void sendDialComponentAction(Context context, int spaceIndex, int styleIndex) {
        Intent intent = new Intent(ACTION_DIAL_COMPONENT);
        intent.putExtra("spaceIndex", spaceIndex);
        intent.putExtra("styleIndex", styleIndex);
        context.sendBroadcast(intent);
    }
}
