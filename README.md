
 [中文](CHINESE_README.md)
 
# FitCloudPro-SDK-Android Document

This document guides Android developers to integrate `FitCloudPro-SDK-Android` in `Android 4.4` and above, mainly for some key usage examples. For more detailed APIs, please refer to the JavaDoc documentation.

## 1. Import SDK

Import the `libraryCore_vx.x.x.aar` and `libraryDfu_vx.x.x.aar` into the project, generally copy them to the `libs` directory, and then set them in the build.gradle in the module as follows:

```
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    ...

    //RxJava2 and RxAndroid
    implementation 'io.reactivex.rxjava2:rxjava:2.2.19'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'

    //RxAndroidBle
    implementation 'com.polidea.rxandroidble2:rxandroidble:1.11.0'

    //lib core function
    implementation(name: 'libraryCore_v1.0.2', ext: 'aar')

    //lib dfu function. Optional. If your app need dfu function.
    implementation(name: 'libraryDfu_v1.0.0', ext: 'aar')
    
    ...
}

```

## 2. Permission Settings

```
<!--In most cases, you need to ensure that the device supports BLE.-->
<uses-feature
    android:name="android.hardware.bluetooth_le"
    android:required="true"/>

<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>

<!--Android 6.0 and above. Bluetooth scanning requires one of the following two permissions. You need to apply at run time.-->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>

<!--Optional. If your app need dfu function.-->
<uses-permission android:name="android.permission.INTERNET"/>
```

## 3. Initialization

You can directly make your own `Application` class inherit `WristbandApplication`

```
public class MyApplication extends WristbandApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        WristbandApplication.setDebugEnable(true);
    }
}

```

Or initialize it in the `onCreate` method.

```
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        WristbandApplication.init(this);
        WristbandApplication.setDebugEnable(true);
    }
}
```

## 4. Scan Device

You can use the system's own API to scan for Bluetooth devices, or you can use [RxAndroidBle](https://github.com/Polidea/RxAndroidBle) to scan devices as in the example. Please refer to the Android development manual for using the system API. Use RxAndroidBle as follows. For details, please refer to [RxAndroidBle](https://github.com/Polidea/RxAndroidBle) or sample project:

```
//Do not create an RxBleClient instance yourself, please get it like this.
RxBleClient mRxBleClient = WristbandApplication.getRxBleClient();

ScanSettings scanSettings = new ScanSettings.Builder()
.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build();
                            
Disposable mScanDisposable = mRxBleClient.scanBleDevices(scanSettings)
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
                            
```

## 5. Connect Device

The API related to the connection of the bracelet is as follows. For details, refer to the `WristbandManager` class in JavaDoc, and use the reference sample project.

```
void connect(@NonNull String address, @NonNull String userIdentity, boolean bindOrLogin,
                 boolean sex, int age, float height, float weight);

void connect(@NonNull BluetoothDevice device, @NonNull String userIdentity, boolean bindOrLogin
            , boolean sex, int age, float height, float weight);

void connect(@NonNull RxBleDevice device, @NonNull String userIdentity, boolean bindOrLogin
            , boolean sex, int age, float height, float weight);

void close();

Observable<ConnectionState> observerConnectionState();

Observable<ConnectionError> observerConnectionError();

boolean isConnected();

boolean isBindOrLogin();

WristbandConfig getWristbandConfig();

RxBleDevice getRxBleDevice();

String getConnectedAddress();

BluetoothDevice getConnectedDevice();
```

Use the `connect` method to connect the bracelet, use the `close` method to disconnect, use the `observerConnectionState` method to listen for the connection state, and use the `observerConnectionError` method to listen for exceptions during the connection process for additional processing and troubleshooting. Use the `isConnected` method to determine if the connection has been established.

After the connection is successful, you can use the `isBindOrLogin` method to determine whether the connection is in bind mode or login mode. Use the `getWristbandConfig` method to get the configuration information of the bracelet.

Use `getRxBleDevice`, `getConnectedAddress`, `getConnectedDevice` to get the device that is connecting or connected.

> Note: The parameter `isBindOrLogin` in the `connect` method, that is, when the connection bracelet is selected, whether to use the bind mode or the login mode. There are some differences between bind and login. For a new user, when you connect the bracelet for the first time, you need to select the bind mode. After the bind is successful, the next time you connect the bracelet, you need to select the login mode.

> If the current user ID of the bracelet is 1000, then when you try to login with a user with ID 1001, the login will fail. Whether a user has been bound or not, there is no record inside the SDK, you need to handle this logic yourself.

> If the currently bound user ID is 1000, try to use the user ID to be re-bound for 1001, and the binding can still be successful. Each time the binding is successful, the bracelet will clear the previous user data, including all data such as exercise, sleep, heart rate.

## 6. Bracelet Function And API

### 6.1、Bracelet Configuration

In the SDK, `WristbandConfig` is used as the entity class of the bracelet configuration information, which contains all the configuration information and function parameters of the bracelet. After the bracelet is successfully connected, use `WristbandManager#getWristbandConfig()` to get the cached configuration information. You can also re-request the bracelet with `WristbandManager#requestWristbandConfig()`. `WristbandConfig` contains the following information:

1. `WristbandVersion` : Firmware information
2. `NotificationConfig` : Notification configuration
3. `BloodPressureConfig` : Blood pressure configuration
4. `DrinkWaterConfig` : Drink water reminder configuration
5. `FunctionConfig` : Accessibility function configuration
6. `HealthyConfig` : Real-time detection configuration of health data
7. `SedentaryConfig` : Sedentary reminder configuration
8. `PageConfig` : Bracelet page configuration
9. `TurnWristLightingConfig` : Light up the wrist configuration
10. `WarnHeartRateConfig` Heart rate warning config
11. `WarnBloodPressureConfig` Blood pressure warning config
12. `NotDisturbConfig` DND config

After getting the `WristbandConfig`, you can get the corresponding bytecode through `WristbandConfig#getBytes()` and cache it locally. The instance can then be regenerated by `WristbandConfig#newInstance(byte[])`.

If you want to get the bytecode of a configuration in `WristbandConfig`, you can also use the `getBytes()` method of the configuration, such as `PageConfig#getBytes()`.

#### 6.1.1、WristbandVersion
The information in WristbandVersion is mainly divided into three parts:

1. Version information of hardware, firmware, flash, etc., used for version judgment in future firmware upgrades.

```
private String project;
private String hardware;
private String patch;
private String flash;
private String app;
private String serial;
private String extension;
```

2. Function module information, used to determine the functions supported by the bracelet.

The compatibility of some functional modules has been handled in the SDK. Please refer to the detailed documents of each module when you need to use them externally.

```
private boolean heartRateEnable;
private boolean oxygenEnabled;
private boolean bloodPressureEnabled;
private boolean respiratoryRateEnabled;
private boolean weatherEnabled;
private boolean ecgEnabled;
private boolean sportEnabled;
private boolean wechatSportEnabled;
private boolean platform8762CEnabled;
private boolean dynamicHeartRateEnabled;
private boolean extHidePageConfig;
private boolean extAncsEmail;
private boolean extAncsViberTelegram;
private boolean extStepExtra;
private boolean extWarnHeartRate;
private boolean extWarnBloodPressure;
private boolean extAncsExtra1;
private boolean extDialUiUpgrade;
private boolean extNotDisturb;
private boolean extLatestHealthy;
private boolean extTpUpgrade;
private boolean extNewNotificationFormat;
private boolean extNewSleepFormat;
private boolean extChangeConfigItself;
private boolean extMockEcg;
```

3. Page support information, used to determine the page that can be displayed on the bracelet, combined with `PageConfig`. Refer specifically to the usage of `PageConfig`.

```
private int pageSupport;
```

#### 6.1.2、NotificationConfig
Configure the type of message notification that the bracelet can receive and display. This config defines multiple types of notifications. But not all notifications are supported on the wristband.

The following notifications are supported on all bracelets：`FLAG_TELEPHONE`,`FLAG_SMS`,`FLAG_QQ`,`FLAG_WECHAT`,`FLAG_FACEBOOK`,`FLAG_PINTEREST`,`FLAG_WHATSAPP`,`FLAG_LINE`,`FLAG_KAKAO`,`FLAG_OTHERS_APP`

The following notifications are supported when `WristbandVersion#isExtAncsEmail()` is true：`FLAG_EMAIL`

The following notifications are supported when `WristbandVersion#isExtAncsViberTelegram()` is true：`FLAG_TELEGRAM`,`FLAG_VIBER`

The following notifications are supported when `WristbandVersion#isExtAncsExtra1()` is true：`FLAG_TWITTER`,`FLAG_LINKEDIN`,`FLAG_INSTAGRAM`,`FLAG_FACEBOOK_MESSENGER`,`FLAG_SKYPE`,`FLAG_SNAPCHAT`

The following notifications are just definitions and are not implemented on the wristband：`FLAG_CALENDAR`

In Android development, `BroadcastReceiver` is generally used to monitor calls and text messages, and` NotificationListenerService` is used to obtain notifications from third-party apps and parse messages. Because `Notification` involves the compatibility of many different versions of the system, many apps will send the same notification repeatedly, so pay attention to filtering the duplicate notifications sent by the same APP during development. And it is not recommended to use `FLAG_OTHERS_APP` to support many APP types that are not supported by the bracelet. This may cause you to send many notifications and cause the bracelet to keep vibrating.

#### 6.1.3、BloodPressureConfig
Set the reference blood pressure range so that the wristband can measure blood pressure more accurately. Where `BloodPressureConfig#isPrivateModel()` is similar to `isEnabled`, True is on, and false is off.

The systolic pressure setting range is generally 50-200mmhg, and the diastolic pressure setting range is generally 20-120mmhg.

#### 6.1.4、DrinkWaterConfig
Used to remind users to drink water on time. The bracelet will alert the user to drink water at intervals between the set start time and end time.
`setInterval` sets the interval time, the range is [30,180] minutes
`setStart` sets the start time, the range is [00: 00,23: 59].for example, time 11:30 is converted to an int value, 11 × 60 + 30 = 690
`setEnd` sets the end time, the range is [00: 00,23: 59].

#### 6.1.5、FunctionConfig
Configure some simple features of the bracelet.
1. Wearing method (`FLAG_WEAR_WAY`), true for the right hand, false for the left and right
2. Strengthen the measurement (`FLAG_STRENGTHEN_TEST`), true is on, false is off
3. Twelve-hour system (`FLAG_HOUR_STYLE`), true is twelve-hour system, false is twenty-four hour system
4. Length unit (`FLAG_LENGTH_UNIT`), true is imperial, false is metric
5. Temperature unit (`FLAG_TEMPERATURE_UNIT`), true is Fahrenheit, and false is Celsius

#### 6.1.6、HealthyConfig
Used to configure real-time monitoring of health data, this setting will affect heart rate, blood pressure, blood oxygen, respiratory rate and other data. Heart rate, blood pressure, blood oxygen, and respiratory rate data monitor the user's health status and generate data for a set period of time. The resulting data can be obtained by synchronizing the data process.

#### 6.1.7、SedentaryConfig
Used to configure whether to alert the user when they are sedentary, and set the start and end times, as well as the DND settings.
Do Not Disturb If enabled, the fixed DND time is 12:00-2:00.

#### 6.1.8、PageConfig
PageConfig is used to configure the interface displayed on the watch. Before setting, it is best to check `WristbandVersion#isExtHidePageConfig()` first, some of the bracelet UI is special, it is not recommended to set the page configuration.
Then check if the bracelet supports a page `WristbandVersion#isPageSupport(int flag)`.

For details, please refer to the sample project.

#### 6.1.9、TurnWristLightingConfig
Turn the wristscreen settings

#### 6.1.10、WarnHeartRateConfig
Heart rate warning config. This feature is only supported when the `WristbandVersion#isExtWarnHeartRate()` is true. You can set early warning values for heart rate during exercise and heart rate during rest.

#### 6.1.11、WarnBloodPressureConfig
Blood pressure warning config. This feature is only supported when the `WristbandVersion#isExtWarnBloodPressure()` is true.Upper and lower limits can be set separately for systolic and diastolic blood pressure.

#### 6.1.12、NotDisturbConfig
DND config. This feature is only supported when the `WristbandVersion#isExtNotDisturb()` is true. Can set up DND all day, or set a certain period of DND

### 6.2、Alarm setting
The bracelet only supports 5 alarm clocks. Each alarm clock has the `alarmId` in `WristbandAlarm` as the unique flag, so the value of `alarmId` is 0-4.
The time information of the alarm clock is year, month, day, hour, minute.

The repetition period of the alarm is marked with `repeat`. If `repeat` is 0, it means no repetition, then it will only take effect once at the set time. If `repeat` is not 0, the year, month, and day will be ignored, and it will take effect multiple times at some point in the set day. Whether the alarm is turned on or not using `enable`. It's worth noting that if `repeat` is 0 and the time set is less than the current time, then you should force `enable` to be false.

You can set a label for the alarm clock setting, but the length of the label cannot exceed 32 bytes, and the excess will be ignored.

Request alarms with `WristbandManager#requestAlarmList()`.
Set alarms by `WristbandManager#setAlarmList(@Nullable List<WristbandAlarm> alarmList)`. It's important to note that you must set all the alarms you want to save at the same time, so you need to pass in a List here. If only one alarm is set, all other alarm information will be lost.

### 6.3、Notification
Use `WristbandManager#sendWristbandNotification(WristbandNotification notification) ` to send a message notification to the bracelet.

`WristbandNotification` is the message entity sent to the bracelet. You can send a variety of different message notifications to the bracelet, such as QQ, WeChat, Facebook, etc. Specific reference to the JavaDoc documentation.

If you want to send a certain type of message notification, you first need to ensure that the message notification of the bracelet is configured in `NotificationConfig`, and the notification of this type is enabled. Otherwise, even if the bracelet receives a message notification, it will not vibrate.

The configuration items in `NotificationConfig` are not one-to-one corresponding to the message types in `WristbandNotification`. This is because there are three types of notifications for phone types: incoming calls, answering and hanging up.

Because Android does not support ANCS, so these notification messages need to be captured by yourself. Like monitor phone rings and hangs up, listen to the SMS. Other third-party notifications such as QQ, WeChat messages, etc., consider using `NotificationListenerService` to capture.

### 6.4、Bracelet active request message
At some point, the bracelet will actively send some messages to complete certain functions. These messages are listened to by `WristbandManager#observerWristbandMessage()`. The message types are as follows:
```
MSG_WEATHER;
MSG_FIND_PHONE;
MSG_HUNG_UP_PHONE;

MSG_CAMERA_TAKE_PHOTO;
MSG_CAMERA_WAKE_UP

MSG_MEDIA_PLAY_PAUSE
MSG_MEDIA_NEXT
MSG_MEDIA_PREVIOUS
MSG_MEDIA_VOLUME_UP
MSG_MEDIA_VOLUME_DOWN   

MSG_CHANGE_CONFIG_ITSELF
```
#### 6.4.1、MSG_WEATHER
This message is used for the bracelet to request weather. The current bracelet does not have this feature. The APP needs to send the weather to the bracelet at the right time, such as when the bracelet is connected, and when the weather information is sent, the weather information is sent to the bracelet.

#### 6.4.2、MSG_FIND_PHONE
This message is used to request a phone to find the phone. If the APP needs this function, after receiving the message, you can vibrate the phone or play the prompt audio.

#### 6.4.3、MSG_HUNG_UP_PHONE
This message is used to request the phone to hang up. If the APP needs this function, it needs to hang up the phone after receiving the message.

#### 6.4.4、MSG_TAKE_PHOTO
This message is used to request a photo from the bracelet. If the APP needs this function, after receiving the message, it needs to call the APP to take a photo. The camera function implemented by the bracelet does not control the camera of the Android system. You must implement the camera function yourself.
This function needs to be used together with `WristbandManager#setCameraStatus(boolean enterCameraApp)`. When entering the camera interface, call `WristbandManager#setCameraStatus(true)` to notify the bracelet that it is ready to take control of the camera. At this time, the bracelet is shaken, the bracelet will send a MSG_TAKE_PHOTO message, and then the photo will be completed.
When exiting the camera, be sure to call `WristbandManager#setCameraStatus(false)` to notify the bracelet to exit the photo control.

#### 6.4.5、MSG_CAMERA_WAKE_UP
This message is used to wake up the APP phone camera

#### 6.4.6、MSG_MEDIA_PLAY_PAUSE
This message is used to control play or pause the phone audio

#### 6.4.7、MSG_MEDIA_NEXT
This message is used to control the APP to play the next audio.

#### 6.4.8、MSG_MEDIA_PREVIOUS
This message is used to control the APP to play the previous audio.

#### 6.4.9、MSG_MEDIA_VOLUME_UP
This message is used to control the APP to increase the volume.

#### 6.4.10、MSG_MEDIA_VOLUME_DOWN
This message is used to control the APP to reduce the volume.

#### 6.4.11, MSG_CHANGE_CONFIG_ITSELF
If `WristbandVersion#isExtChangeConfigItself()` is true, it means that the bracelet can change some configurations by itself. When the bracelet changes the configuration, it will actively send this message.

### 6.5、Real-time data measurement

The SDK supports a variety of real-time data testing, but whether it is effective or not depends on whether the module has this function module. Use `WristbandVersion` to detect the presence of this function module in the bracelet and perform some real-time data measurement.

#### 6.5.1、Heart rate, blood oxygen, blood pressure, respiratory rate
Start the measurement with `WristbandManager#openHealthyRealTimeData(int healthyType)`. But before starting, you need to check if this module is supported in `WristbandVersion`, the corresponding relationship is as follows:
```
WristbandVersion#isHeartRateEnabled() --> WristbandManager#HEALTHY_TYPE_HEART_RATE
WristbandVersion#isOxygenEnabled() --> WristbandManager#HEALTHY_TYPE_OXYGEN
WristbandVersion#isBloodPressureEnabled() --> WristbandManager#HEALTHY_TYPE_BLOOD_PRESSURE
WristbandVersion#isRespiratoryRateEnabled() --> WristbandManager#HEALTHY_TYPE_RESPIRATORY_RATE
```
You can start a single measurement, such as using `HEALTHY_TYPE_HEART_RATE`, or you can start at the same time.
Multiple measurements, such as `HEALTHY_TYPE_HEART_RATE|HEALTHY_TYPE_OXYGEN`.

After starting the measurement, you can take the initiative to end the measurement (Disposable#dispose()), or wait for a while (about 2 minutes), the bracelet will also automatically end, please pay attention to the end of the measurement process, refer to the sample project.

> Note: Measurement returns may contain invalid data values. If the heart rate measurement is started, the return value may be 0, so you need to filter out the invalid data, and other values do not open the measured value, such as blood oxygen may not be 0, but it is not meaningful.

`WristbandManager#openHealthyRealTimeData(int healthyType)` The default measurement time is 2 minutes. You can use `WristbandManager#openHealthyRealTimeData(int healthyType, int minute)` to customize the measurement time. The custom time limit is 1-255 minutes.

#### 6.5.2、ECG

If `WristbandVersion#isEcgEnabled()` is true, then the wristband is supported for ECG measurements, and the ECG measurement is initiated using `WristbandManager#openHealthyRealTimeDataopenEcgRealTimeData()`. After the measurement is started, the first packet data returned is the sampling rate, and the subsequent data is the ECG value.

```
EcgData mEcgData = null;

mWristbandManager.openEcgRealTimeData()
     .subscribe(new Consumer<int[]>() {
         @Override
         public void accept(int[] ints) throws Exception {
             if (mEcgData == null) {//This is the first packet
                 mEcgData = new EcgData();
                 mEcgData.setItems(new ArrayList<Integer>(1000));
                 if (ints.length == 1) {//Sample packet
                     mEcgData.setSample(ints[0]);
                 } else {//Error packet, may be lost the sample packet.
                     mEcgData.setSample(EcgData.DEFAULT_SAMPLE);//Set a default sample
                     mEcgData.getItems().addAll(intsAsList(ints));//Add this ecg data
                 }
             } else {
                 mEcgData.getItems().addAll(intsAsList(ints));//Add this ecg data
             }
         }
     });
```
#### 6.5.3、Obtain the latest health measurement record of the wristband
If `WristbandVersion#isExtLatestHealthy()` is true, then the representative wristband supports the ability to get the most recent health measurement record. Use `WristbandManager#requestLatestHealthy()` to get it.

### 6.6、Data Synchronization
The data synchronization function refers to acquiring data of different functional modules stored on the wristband.

The data modules supported by the SDK support are as follows:

```
Step                     SyncDataParser#TYPE_STEP
Sleep                    SyncDataParser#TYPE_SLEEP
Heart Rate               SyncDataParser#TYPE_HEART_RATE
Oxygen                   SyncDataParser#TYPE_OXYGEN
Blood Pressure           SyncDataParser#TYPE_BLOOD_PRESSURE
Respiratory Rate         SyncDataParser#TYPE_RESPIRATORY_RATE
Sports                    SyncDataParser#TYPE_SPORT
Total data for today     SyncDataParser#TYPE_TOTAL_DATA
ECG                      SyncDataParser#TYPE_ECG
```
Among them, ‘Step’, ‘Sleep’ and ‘Total data for today’ must exist, and other functional modules depend on whether the bracelet is supported. Use `WristbandVersion` to check if the function module exists in the bracelet. The synchronized data flow will be synchronized in the order of 'Step', 'Sleep', 'Heart Rate', 'Oxygen', 'Blood Pressure', 'Respiratory Rate', 'Sports', 'Total data for today', 'ECG' And return the data of each module, if a module does not exist, it will be skipped.

Except for ‘Total data of today’, after the data of each module is successfully synchronized, the data of this module will be deleted on the bracelet. If a module fails to synchronize data, the subsequent synchronization process will be interrupted.

Use `WristbandManager#syncData()` to synchronize data. This method will get the original byte data. According to different data types, use the parsing method in `SyncDataParser` to get the data of each module.

```
mWristbandManager
    .syncData()
    .observeOn(Schedulers.io(), true)
    .flatMapCompletable(new Function<SyncDataRaw, CompletableSource>() {
        @Override
        public CompletableSource apply(SyncDataRaw syncDataRaw) throws Exception {
            if (syncDataRaw.getDataType() == SyncDataParser.TYPE_HEART_RATE) {
                List<HeartRateData> datas = SyncDataParser.parserHeartRateData(syncDataRaw.getDatas());
                if (datas != null && datas.size() > 0) {
                    //TODO save data
                }
            } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_BLOOD_PRESSURE) {
                List<BloodPressureData> datas = SyncDataParser.parserBloodPressureData(syncDataRaw.getDatas());
                if (datas != null && datas.size() > 0) {
                    //TODO save data
                }
            } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_OXYGEN) {
                List<OxygenData> datas = SyncDataParser.parserOxygenData(syncDataRaw.getDatas());
                if (datas != null && datas.size() > 0) {
                    //TODO save data
                }
            } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_SLEEP) {
                List<SleepData> sleepDataList = SyncDataParser.parserSleepData(syncDataRaw.getDatas());
                if (sleepDataList != null && sleepDataList.size() > 0) {
                    //TODO save data
                }
            } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_SPORT) {
                List<SportData> datas = SyncDataParser.parserSportData(syncDataRaw.getDatas(), syncDataRaw.getConfig());
                if (datas != null && datas.size() > 0) {
                    //TODO save data
                }
            } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_STEP) {
                List<StepData> datas = SyncDataParser.parserStepData(syncDataRaw.getDatas());
                if (datas != null && datas.size() > 0) {
                    //TODO save data
                }
            } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_ECG) {
                EcgData ecgData = SyncDataParser.parserEcgData(syncDataRaw.getDatas());
                if (ecgData != null) {
                    //TODO save data
                }
            } else if (syncDataRaw.getDataType() == SyncDataParser.TYPE_TOTAL_DATA) {
                TodayTotalData data = SyncDataParser.parserTotalData(syncDataRaw.getDatas());
                //TODO save data
            }
            return Completable.complete();
        }
    })
    .subscribe(new Action() {
        @Override
        public void run() throws Exception {
            Log.d("Sync", "Sync Data Success");
        }
    }, new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) throws Exception {
            Log.e("Sync", "Sync Data Failed", throwable);
        }
    });

```

Use `WristbandManager#observerSyncDataState()` to listen for the status of the synchronization. You can also use `WristbandManager#isSyncingData()` to easily determine if you are synchronizing.

```
mWristbandManager.observerSyncDataState()
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            if (integer == null) return;
            if (integer < 0) {//failed
                if (integer == WristbandManager.SYNC_STATE_FAILED_DISCONNECTED) {
                    mTvSyncState.setText(R.string.sync_data_state_failed_disconnected);
                } else if (integer == WristbandManager.SYNC_STATE_FAILED_CHECKING_ECG) {
                    mTvSyncState.setText(R.string.sync_data_state_failed_checking_ecg);
                } else if (integer == WristbandManager.SYNC_STATE_FAILED_SAVING_ECG) {
                    mTvSyncState.setText(R.string.sync_data_state_failed_saving_ecg);
                } else /*if(integer == WristbandManager.SYNC_STATE_FAILED_UNKNOWN)*/ {
                    mTvSyncState.setText(R.string.sync_data_state_failed_unknown);
                }
            } else if (integer == WristbandManager.SYNC_STATE_START) {
                mTvSyncState.setText(R.string.sync_data_state_start);
            } else if (integer == WristbandManager.SYNC_STATE_SUCCESS) {
                mTvSyncState.setText(R.string.sync_data_state_success);
            } else {
                mTvSyncState.setText(getString(R.string.sync_data_state_progress, integer));
            }
        }
    });
```

#### 6.6.1 Step
The bracelet will monitor the user's motion status throughout the day and generate step data. Synchronize the data and parse it to get `StepData`. `StepData` indicates the number of steps of the user's movement at a certain point in time, such as 2019-05-29 12:00:00 and 50 steps. Actually, it can be understood that the user has accumulated 50 steps in the 5 minute period from 2019-05-29 11:55:00 to 2019-05-29 12:00:00.

```
StepData{
   long getTimeStamp();//The data time point
   int getStep();//The number of steps at this time
   float getDistance();//The distance at this time
   float getCalories();//The calories at this time
}
```
The `StepData#getDistance()` and `StepData#getCalories()` are calculated by the bracelet according to the number of steps, and may not be supported on some old bracelets. You can detect `WristbandVersion#isExtStepExtra()`, and when it is false, you need to calculate it yourself.

1. How many days of step data will the bracelet keep?

The bracelet saves the latest 7-day step data. After each step data is successfully synchronized, the step data on the bracelet will be deleted. The next time you synchronize, you will only get the newly generated step count data.

2. What is the time interval of the step data?

The time interval is uncertain. If the user is in continuous motion, each step data will be separated by 5 minutes. If the user is intermittently moving, the interval may be greater than 5 minutes or less than 5 minutes.

3. How to calculate distance and calories based on the number of steps?

The steps for converting the number of steps to calories and distance are as follows:
```
    /**
     * Calculate the distance based on the number of steps and the step size(km)
     *
     * @param step       number of steps
     * @param stepLength step size(m)
     * @return distance(km)
     */
    public static float step2Km(int step, float stepLength) {
        return (stepLength * step) / (1000);
    }

     /**
     * Calculate calories based on distance and weight(kCal)
     *
     * @param km     distance(km)
     * @param weight weight(kg)
     * @return calories(kCal)
     */
    public static float km2Calories(float km, float weight) {
        return 0.78f * weight * km;
    }

    /**
     * Calculate the step size based on height and gender(m)
     * @param height     height(cm)
     * @param man        gender，True for male, false for female
     * @return step size(m)
     */
    public static float getStepLength(float height,boolean man) {
        float stepLength = height * (man ? 0.415f : 0.413f);
        if (stepLength < 30) {
            stepLength = 30.f;//30cm，Default minimum step size 30cm
        }
        if (stepLength > 100) {
            stepLength = 100.f;//100cm，Default maximum step size 100cm
        }
        return stepLength / 100;
    }
```
    
4. The APP caches the obtained `StepData` to the database. Why use `StepData` to accumulate the number of steps and the total number of steps on the bracelet is inconsistent?

There are two possibilities for this.
 1. The bracelet was re-bound on the day.
 
 The current design of the bracelet is to clear the data each time it is bound. If the bracelet data is generated on the  day, and synced to the APP cache. At this point, you unbind and re-bind the bracelet, and the bracelet clears the data. At this time, the total number of steps on the bracelet is 0, so it does not correspond to the data cached by the APP. The solution is to clear the `StepData` of the day when the bracelet is re-bound, so that it is consistent with the total number of steps on the bracelet.

 2. `StepData` is saved once in 5 minutes, and the number of steps in the last 5 minutes will be delayed.
 
 At present, the number of steps in the bracelet is accumulated for 5 minutes and then saved as a `StepData` data. If the accumulation time is less than 5 minutes, then `StepData` will not be generated, so the APP can not sync this data. However, the total number of steps in the bracelet is directly accumulated, so the total number of steps in the bracelet and the number of steps accumulated in `StepData` are inconsistent. If you want to consider this real-time, then consider processing  combining `TodayTotalData`. See `#### 6.6.4 Total Data for the day`.
 
#### 6.6.2 Sleep
The bracelet monitors the user's sleep state between 21:30 and 12:00 the next day and generates sleep data. Synchronize the data and parse it to get `SleepData`.

```
/**
 * Statistics of sleep on a certain day, including the duration of 3 sleep states, and detailed data
 */
SleepData{
    long getTimeStamp();//The time of the data. It is 0:0:0:0 milliseconds for a certain day.
    int getDeepSleep();//Total length of deep sleep, in seconds
    int getLightSleep();//Total length of light sleep, in seconds
    int getSoberSleep();//Total length of awake, in seconds
    List<SleepItemData> getItems();//Sleep details
}

/**
 * A certain sleep state
 */
SleepItemData {
    int getStatus();//The state of the sleep segment
    long getStartTime();//The start time of the sleep segment
    long getEndTime();//The end time of the sleep segment
}

```
`SleepData#getTimeStamp()` gets the start timestamp of a certain day, for example 2019-05-29 00:00:00:000, which can be understood as representing the sleep condition of last night. That is, the sleep condition between 2019-05-28 21:30 and 2019-05-29 12:00.

Older versions of the bracelet will return all sleep data for the day at a time, and no longer generate sleep data after exiting sleep. Because the bracelet actively determines that the user takes a long time to exit from sleep, it is likely that, for example, the user no longer sleeps at 7 am, but cannot obtain sleep data during synchronization. So the recommended approach is to call `WristbandManager # exitSleepMonitor ()` to exit sleep between 4am to 12am (the user is likely to no longer sleep during this period), and the user actively synchronizes data (as shown below). , And then synchronize data.

The new version of the bracelet will return to sleep data multiple times. You can still use `WristbandManager # exitSleepMonitor ()` method to exit sleep, the bracelet will be compatible with the version. However, it should be noted that you may need to merge externally and then display it on the interface for multiple sleeps returned on the same day.

1. How many days of sleep data will the bracelet keep?

The bracelet saves the latest 7 days of sleep data, and the sleep data on the bracelet will be deleted each time the sleep data is successfully synchronized. The next time you sync, you will only get the newly generated sleep data.


#### 6.6.3 Heart rate, blood oxygen, blood pressure, respiratory rate
The bracelet will monitor the user's physical state within the time range set by `#### 6.1.6, HealthyConfig`, generate corresponding data. Synchronize the data and parse the health data such as `HeartRateData`, `BloodPressureData`, `OxygenData`, `RespiratoryRateData`.

`HeartRateData` indicates the user's heart rate value at a certain point in time, such as 2019-05-29 12:00:00, heartbeat 72 times. Heart rate values are generally separated by about 5 minutes during the monitoring period.

`BloodPressureData`, `OxygenData`, `RespiratoryRateData` is similar to `HeartRateData`.

1. How many days of health data will the bracelet keep?

The bracelet saves the latest 7-day health data, and the health data on the bracelet will be deleted each time the health data is successfully synchronized. The next time you synchronize, you will only get the newly generated health data.

2. What is the time interval for health data?

The time interval is uncertain. Under normal circumstances, the interval is about 5 minutes.

#### 6.6.4 Sports
When the bracelet starts the sport mode, motion data is generated. Synchronize the data and parse it to get `SportData`.

```
SportData{
    long getTimeStamp();//Sport time
    int getSportType();//Sport type
    int getDuration();//Sport duration，in seconds
    float getDistance();//Sport distance, unit km
    float getCalories();//Sport calories, unit kCal
    int getSteps();//Sport steps
    List<SportItem> getItems();//item datas
}
```
The data contained in the different types of `SportData` is different.See the document for details.

If `WristbandVersion#isDynamicHeartRateEnabled()` is true, then there is heart rate data, otherwise there is no heart rate data.

1. How many days of exercise data will the bracelet keep?

After the accumulated duration of all unsynchronized motion data on the bracelet exceeds a certain value, the bracelet will delete the old motion data. After each motion data synchronization is successful, the motion data on the bracelet will be deleted. The next time you synchronize, you will only get the newly generated exercise data.

#### 6.6.5 Total data of today
The bracelet will count the data of the day as a total data, synchronize the data and parse it to get `TodayTotalData`. The data includes information such as total number of steps on the day, sleep duration, and average heart rate. `TodayTotalData` is obtained for each synchronization, but if the bracelet is re-bound, the total data will be cleared and then re-stated.

Although `TodayTotalData` contains a variety of data, it is mainly used to supplement the processing of the day's step data. The methods related to the number of steps are as follows:
```
TodayTotalData{
    int getStep();//The total number of steps in the day's exercise
    int getDistance()//Total distance of the day's exercise, in meters
    int getCalorie()//Total calories burned during the day, unit calories
    int getDeltaStep();//The step not saved to StepData
    int getDeltaDistance();//The distance not saved to StepData, unit meters
    int getDeltaCalorie();//The calories not saved to StepData, unit calories
    long getTimeStamp();//synchronised time
}
```
The `StepData` mentioned in `#### 6.6.1 Steps` is saved once every 5 minutes, and the number of steps in the last 5 minutes will be delayed. Then you can use `TodayTotalData` when displaying the total number of steps on the APP. 

#### 6.6.6 ECG
The ECG measurement started on the wristband, synchronized data and parsed to get `EcgData`.
```
EcgData{
    List<Integer> getItems();//Ecg items
    int getSample();//Sampling rate (number of ECG values per second)
    long getTimeStamp();//measure time
}
```

1. How many days of ECG data will the bracelet keep?

Only the last measured ECG value is saved on the wristband. After each ECG data synchronization is successful, the ECG data on the wristband will be deleted. The next time you synchronize, you will only get the newly generated ECG data.

### 6.7、DFU upgrade
Use `DfuManager` to upgrade firmware or dial. `WristbandVersion`,` WristbandManager#requestDialUiInfo`, `WristbandManager#requestDialBinInfo` contains information about the bracelet firmware and dial info.

Use `DfuManager#start (String uri, boolean firmwareUpgrade)` to upgrade. When upgrading the firmware, the second parameter is passed as true, and when upgrading the dial, the second parameter is passed as false.

If you use the wrong file to upgrade, it may make the bracelet unusable. Therefore, when developing upgrade functions, you must first communicate with the developer or product manager to obtain the correct upgrade package.

For details of the specific upgrade function, please refer to the javaDoc document and the sample project.

### 6.8、Other simple instructions
#### 6.8.1、Set user information
`WristbandManager#setUserInfo(boolean sex, int age, float height, float weight)`。

When the bracelet is connected, the user information has been passed in. If the user information is updated, the method can be called to update the user information.

#### 6.8.2、Set sports goals
`WristbandManager#setExerciseTarget(int step, int distance, int calorie)`

#### 6.8.3、Requested battery
`WristbandManager#requestBattery()`

#### 6.8.4、Find the bracelet
`WristbandManager#findWristband()`，The bracelet will vibrate when it accepts the message.

#### 6.8.5、Flashback factory reset
`WristbandManager#resetWristband()`

#### 6.8.6、Shutdown
`WristbandManager#turnOffWristband()`

#### 6.8.7、Restart
`WristbandManager#restartWristband()`

#### 6.8.8、User unbind
`WristbandManager#userUnBind()`，After unbinding, you need to use Bind mode for the next connection. Reference `5. Connect Device`

#### 6.8.9、Language setting
`WristbandManager#setLanguage(byte languageType)`，Specific language types are as follows：
![LanguageType](LanguageType.png)

You can get the current system language type using `Utils.getSystemLanguageType(Context context)`.


#### 6.8.10、Setting the weather
`WristbandManager#setWeather(int currentTemperature, int lowTemperature, int highTemperature, int weatherCode, String city)`，

Before using the set weather, you need to ensure that `WristbandVersion#isWeatherEnable()` is true, that is, the bracelet supports the weather function.

The weather code supported by the bracelet is as follows:

```
0x00 unknown
0x01 sunny day
0x02 partly cloudy
0x03 cloudy day
0x04 shower
0x05 Thunderstorms, thunderstorms and hail
0x06 Light rain
0x07 Moderate rain, heavy rain, heavy rain
0x08 Rain and snow, freezing rain
0x09 Light snow
0x0a Heavy snow
0x0b Sandstorm, floating dust
0x0c Fog, smog
```
The weather code obtained by the general user from the third-party platform is inconsistent with the above list, and it is necessary to convert the setting to the wristband.
