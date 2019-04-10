
# FitCloudPro-SDK-Android集成文档

 [TOC]

该文档为指导Android开发人员在Android 4.4及以上系统中集成FitCloudPro-SDK-Android，主要为一些关键的使用示例，更详细API，请参考JavaDoc文档。

## 一、导入SDK
将libraryCore-release_xx_xxxx_x.aar和libraryDfu-release_xx_xxxx_x.aar导入工程，一般复制到libs目录下，然后在module中的build.gradle中如下设置：
```
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    ...

    //RxJava2 and RxAndroid
    implementation 'io.reactivex.rxjava2:rxjava:2.2.0'
    implementation 'io.reactivex.rxjava2:rxandroid:2.0.2'

    //RxAndroidBle
    implementation 'com.polidea.rxandroidble2:rxandroidble:1.7.0'

    //lib core function
    implementation(name: 'libraryCore-release_19_0405_1', ext: 'aar')

    //lib dfu function. Optional. If your app need dfu function.
    implementation(name: 'libraryDfu-release_19_0405_1', ext: 'aar')
    
    ...
}

```

## 二、权限设置

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

## 三、初始化

你可以直接让你自己的`Application`类继承`WristbandApplication`

```
public class MyApplication extends WristbandApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        WristbandApplication.setDebugEnable(true);
    }
}

```
或者在`onCreate`方法中初始化它。

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

## 四、扫描设备

你可以使用系统自带API扫描蓝牙设备，也可以和示例中一样，使用[RxAndroidBle](https://github.com/Polidea/RxAndroidBle)扫描设备。使用系统API请参考Android开发手册，使用RxAndroidBle如下，具体请参考[RxAndroidBle](https://github.com/Polidea/RxAndroidBle)或者sample工程：

```
//不要自己创建RxBleClient实例，请如下获取。
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

## 五、连接设备

与手环连接相关的API如下，详情参考JavaDoc中WristbandManager类，具体使用参考sample工程。

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

使用`connect`方法连接手环，使用`close`方法断开连接，使用`observerConnectionState`方法监听连接的状态，使用`observerConnectionError`方法监听连接过程中的异常，以便于进行额外的处理和问题排查。使用`isConnected`方法判断连接是否已经建立。

连接成功后，可以使用`isBindOrLogin`方法判断此次连接是使用绑定模式还是登陆模式。使用`getWristbandConfig`方法可以获取手环的配置信息。

使用`getRxBleDevice`,`getConnectedAddress`,`getConnectedDevice`可以获取正在连接或者已经连接的设备。


> 注意：connect方法中参数isBindOrLogin, 即选择连接手环时，是使用绑定模式，还是登陆模式。绑定和登录两者有一些差别，对于一个新的用户，第一次连接手环时，你需要选择绑定操作。绑定成功之后，下一次连接手环时，你需要选择登录操作。

> 如果手环当前绑定的用户ID是1000，那么当你尝试使用ID为1001的用户进行登录时，登录会失败。某个用户是否绑定过，SDK内部并没有记录，你需要自己去处理这个逻辑。

> 如果当前绑定的用户ID为1000，尝试使用用户ID为1001重新绑定，仍然可以绑定成功。每次绑定成功，手环将清除之前的用户数据，包括运动、睡眠、心率等所有数据。

## 六、手环功能和对应API介绍

### 6.1、手环配置

在SDK中，`WristbandConfig`作为手环配置信息的实体类，里面包含了手环所有的配置信息和功能参数。在手环连接成功后，使用`WristbandManager#getWristbandConfig()`获取缓存的配置信息，也可以使用`WristbandManager#requestWristbandConfig()`重新请求手环获取。`WristbandConfig`包含以下信息：

1. WristbandVersion 固件信息
2. NotificationConfig 通知配置
3. BloodPressureConfig 血压配置
4. DrinkWaterConfig 喝水提醒配置
5. FunctionConfig 辅助功能配置
6. HealthyConfig 健康数据的实时检测配置
7. SedentaryConfig 久坐提醒配置
8. PageConfig 手环页面配置
9. TurnWristLightingConfig 翻腕亮屏配置

在获取到`WristbandConfig`之后，你可以在通过`WristbandConfig#getBytes()`获取对应的字节码，缓存到本地。之后可以通过`WristbandConfig#newInstance(byte[])`重新生成实例。

如果想要获取`WristbandConfig`中某一项配置的字节码，同样可以使用该项配置的`getBytes()`方法，如`PageConfig#getBytes()`.

#### 6.1.1、WristbandVersion

WristbandVersion里的信息主要分为三部分：

1.硬件、固件、flash等版本信息，用于以后固件升级时的版本判断。

```
private String project;
private String hardware;
private String patch;
private String flash;
private String app;
private String serial;
private String extension;
```

2.功能模块信息，用于判断手环所支持的功能。

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

```

3.页面支持信息，用于判断手环上可显示的页面，结合PageConfig使用。具体参考PageConfig的用法。

```
private int pageSupport;
```

#### 6.1.2、NotificationConfig
配置手环能够接收并显示的的消息通知类型。其中`NotificationConfig.FLAG_EMAIL`,`NotificationConfig.FLAG_TELEGRAM`,`NotificationConfig.FLAG_VIBER`在某些手环可能不支持，你需要判断`WristbandVersion`中的功能支持，如下：
```
if (mWristbandConfig.getWristbandVersion().isExtAncsEmail()) {
                cb_email.setChecked(config.isFlagEnable(NotificationConfig.FLAG_EMAIL));
            } else {
                cb_email.setVisibility(View.GONE);
            }

            if (mWristbandConfig.getWristbandVersion().isExtAncsViberTelegram()) {
                cb_telegram.setChecked(config.isFlagEnable(NotificationConfig.FLAG_TELEGRAM));
                cb_viber.setChecked(config.isFlagEnable(NotificationConfig.FLAG_VIBER));
            } else {
                cb_telegram.setVisibility(View.GONE);
                cb_viber.setVisibility(View.GONE);
            }
```

#### 6.1.3、BloodPressureConfig
配置用户血压参考范围，用于手环检测用户血压后，对血压数值进行修正，以便于更加合理。其中`BloodPressureConfig#isPrivateModel()`类似于`isEnabled`，True为开启，false为关闭。

#### 6.1.4、DrinkWaterConfig
用于提醒用户按时喝水。手环将在设定的起始时间和结束时间内，按照间隔提醒用户喝水。

#### 6.1.5、FunctionConfig
配置手环部分简单的功能。
1. 佩戴方式(FLAG_WEAR_WAY)，true为右手佩戴，false为左右佩戴
2. 加强测量(FLAG_STRENGTHEN_TEST)，true为开启，false为关闭
3. 十二小时制(FLAG_HOUR_STYLE)，true为十二小时制，false为二十四小时制
4. 长度单位(FLAG_LENGTH_UNIT)，true为英制，false为公制
5. 温度单位(FLAG_TEMPERATURE_UNIT)，true为华氏摄氏度，false为摄氏度

#### 6.1.6、HealthyConfig
用于配置健康数据的实时监测，这个设定将影响心率、血压、血氧等数据。心率、血压、血氧这三部分数据会在设定的时间段内监测用户健康状态，并产生数据。产生的数据可以通过同步数据过程获得。

#### 6.1.7、SedentaryConfig
用于配置是否在用户久坐的时候提醒用户，并设置开始和结束时间，以及免打扰设置。
免打扰如果开启，那么固定的免打扰时间为12:00-2:00。

#### 6.1.8、PageConfig
PageConfig用于配置手表上的显示的界面。在设置之前，最好先检查`WristbandVersion#isExtHidePageConfig()`，部分手环UI特殊，不建议设置页面配置。
然后在检查手环是否支持某个页面`WristbandVersion#isPageSupport(int flag)`.

具体可以参考sample工程。

#### 6.1.9、TurnWristLightingConfig
翻腕亮屏设置

### 6.2、闹钟设置
手环只支持5个闹钟，每一个闹钟以`WristbandAlarm`中的`alarmId`作为唯一标志，所以`alarmId`的值为0-4。
闹钟的时间信息为 年(year)，月(month)，日(day)，时(hour)，分(minute)。

闹钟的重复周期使用`repeat`来标志。如果`repeat`为0，表示不重复，那么它只会在设置的时刻生效一次。如果`repeat`不为0，那么年、月、日会被忽略，它会在设置的某天的某个时刻多次生效。闹钟是否开启使用`enable`来表示。值得注意的是，如果`repeat`为0，并且设置的时间小于当前时间，那么你应该强制的认为`enable`为false。

通过`WristbandManager#requestAlarmList()`来请求闹钟，
通过`WristbandManager#setAlarmList(@Nullable List<WristbandAlarm> alarmList)`来设置闹钟。需要注意的是，你必须同时设置所有你希望保存的闹钟，所以这里需要传入的是一个List。如果只设置一个闹钟，那么其他的闹钟信息将全部丢失。

### 6.3、消息通知
使用`WristbandManager#sendWristbandNotification(WristbandNotification notification) `可以对手环发送消息通知.

`WristbandNotification`为发送给手环的消息实体。你可以给手环发送多种不同的消息通知，如QQ，微信，Facebook等。具体的参考JavaDoc文档。

如果你要发送某一个类型的消息通知，那么首先需要保证手环的消息通知配置`NotificationConfig`中，该类型的通知已经启用。否则手环即使收到了消息通知，也不会震动提示。

`NotificationConfig`中的配置项，并不是和`WristbandNotification`中消息类型一一对应。这是因为电话类型的通知有3种类型：来电，接听和挂断。

因为Android本身并不支持ANCS，所以这些通知消息需要自己捕获。如监听电话的响铃和挂断，监听短信。其他第三方通知如QQ，微信消息等，则考虑使用`NotificationListenerService`来完成。

### 6.4、手环主动请求消息
在某些时候，手环会主动发送一些消息，来完成某些特定的功能。通过`WristbandManager#observerWristbandMessage()`来监听这些消息，消息类型如下：
```
MSG_WEATHER;
MSG_FIND_PHONE;
MSG_HUNG_UP_PHONE;
MSG_TAKE_PHOTO;
```
#### 6.4.1、MSG_WEATHER
此消息用于手环请求天气。目前手环并无此功能。APP需要自己在合适的时机向手环发送天气，比如在手环连接时，和天气信息发送改变时，向手环发送天气信息。

#### 6.4.2、MSG_FIND_PHONE
此消息用于手环请求查找手机。APP如果需要此功能，在接受到该消息后，可以震动手机或者播放提示音频。

#### 6.4.3、MSG_HUNG_UP_PHONE
此消息用于手环请求挂断电话，APP如果需要此功能，在接受到该消息后，需要挂断手机的电话。

#### 6.4.4、MSG_TAKE_PHOTO
此消息用于手环请求拍照，APP如果需要此功能，在接受到该消息后，需要调用APP拍照。手环实现的拍照功能并不能控制Android系统的相机，你必须自己实现相机拍照功能。
此功能需要结合`WristbandManager#setCameraStatus(boolean enterCameraApp)`一起使用，在进入相机界面，调用`WristbandManager#setCameraStatus(true)`通知手环已经准备好拍照控制。此时晃动手环，手环就会发送MSG_TAKE_PHOTO消息，然后完成拍照。
在退出相机的时候，务必调用`WristbandManager#setCameraStatus(false)`通知手环退出拍照控制。

### 6.5、实时数据测量

SDK支持多种实时数据的测试，但是是否有效，还要取决于手环是否有该项功能模块。使用`WristbandVersion`检测手环中该功能模块是否存在，在进行某个实时数据的测量。

#### 6.5.1、心率，血氧，血压，呼吸频率
使用`WristbandManager#openHealthyRealTimeData(int healthyType)`启动测量。但是在启动之前，你需要检测`WristbandVersion`中是否支持此模块，对应关系如下：
```
WristbandVersion#isHeartRateEnabled() --> WristbandManager#HEALTHY_TYPE_HEART_RATE
WristbandVersion#isOxygenEnabled() --> WristbandManager#HEALTHY_TYPE_OXYGEN
WristbandVersion#isBloodPressureEnabled() --> WristbandManager#HEALTHY_TYPE_BLOOD_PRESSURE
WristbandVersion#isRespiratoryRateEnabled() --> WristbandManager#HEALTHY_TYPE_RESPIRATORY_RATE
```
你可以启动单个测量，如使用`HEALTHY_TYPE_HEART_RATE`，也可以同时启动
多个测量，如`HEALTHY_TYPE_HEART_RATE|HEALTHY_TYPE_OXYGEN`。

启动测量后，你可以主动结束测量(Disposable#dispose())，或者等一段时间(约2分钟)，手环也会自动结束，请注意测量结束的处理，具体参考sample工程。

> 注意：测量返回结果可能包含无效的数据值。如启动了心率测量，返回结果中心率值有可能为0，所以你需要过滤掉无效的数据，并且其他值未开启测量的值，如血氧可能不为0，但是不具备参考意义。

#### 6.5.2、心电
如果`WristbandVersion#isEcgEnabled()`为true，那么代表手环支持心电测量，使用`WristbandManager#openHealthyRealTimeDataopenEcgRealTimeData()`就启动心电测量。启动测量后，返回的第一包数据为采样率，之后的数据为心电值。
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

### 6.6、数据同步
数据同步功能指获取手环上存储的各个不同功能模块的数据，获取成功后，手环上将删除这些数据(除了当天总数据)。
SDK支持同步的数据如下：
```
SyncDataParser#TYPE_STEP
SyncDataParser#TYPE_SLEEP
SyncDataParser#TYPE_HEART_RATE
SyncDataParser#TYPE_OXYGEN
SyncDataParser#TYPE_BLOOD_PRESSURE
SyncDataParser#TYPE_RESPIRATORY_RATE
SyncDataParser#TYPE_SPORT
SyncDataParser#TYPE_ECG
SyncDataParser#TYPE_TOTAL_DATA
```
其中步数和睡眠是必定存在的，其他功能模块则取决于手环是否支持。使用`WristbandVersion`可以检测手环中该功能模块是否存在。

使用`WristbandManager#syncData()`同步数据，此方法将获取到原始的byte数据，根据不同的数据类型，使用`SyncDataParser`中的解析方法获取到各模块数据。
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
使用`WristbandManager#observerSyncDataState()`可以监听同步的状态。也可以使用`WristbandManager#isSyncingData()`简单判断是否正在同步。
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


### 6.7、DFU升级
使用DfuManager可以对手表硬件进行升级。DfuManager所完成的工作如下：

 1. 检查DFU文件是否正确。如果传入文件url，会自动下载，请确保app拥有网络权限，访问存储权限。
 2. 进入DFU模式。
 3. 搜索DFU设备。
 4. 发送升级数据包。
 5. 升级成功/失败。

具体细节，请参考javaDoc文档和sample工程。

### 6.8、其他简单指令
#### 6.8.1、设置用户信息
`WristbandManager#setUserInfo(boolean sex, int age, float height, float weight)`。

当连接手环的时候，已经传入用户信息，如果用户信息有更新，那么可以调用该方法来更新用户信息。

#### 6.8.2、设置运动目标
`WristbandManager#setExerciseTarget(int step, int distance, int calorie)`

#### 6.8.3、请求电量
`WristbandManager#requestBattery()`

#### 6.8.4、查找手环
`WristbandManager#findWristband()`，手环接受消息后会震动提示。

#### 6.8.5、手环恢复出厂设置
`WristbandManager#resetWristband()`

#### 6.8.6、手环关机
`WristbandManager#turnOffWristband()`

#### 6.8.7、手环重启
`WristbandManager#restartWristband()`

#### 6.8.8、用户解绑
`WristbandManager#userUnBind()`，解除绑定后，下次连接时需要使用Bind模式。参考 `五、连接设备`

#### 6.8.9、设置语言
`WristbandManager#setLanguage(byte languageType)`，
```
0x01 简体中文
0x02 繁体中文
0x03 英语
0x04 德语
0x05 俄语
0x06 西班牙语
0x07 葡萄牙语
0x08 法语
0x09 日语
0xff 如果不是上面9种语言，设置为0xff
```

#### 6.8.10、设置天气
`WristbandManager#setWeather(int currentTemperature, int lowTemperature, int highTemperature, int weatherCode, String city)`，

在使用设置天气之前，需要保证`WristbandVersion#isWeatherEnable()`为true，即手环支持天气功能。

手环支持的天气代码如下：

```
0x00 未知
0x01 晴天
0x02 多云
0x03 阴天
0x04 阵雨
0x05 雷阵雨、雷阵雨伴有冰雹
0x06 小雨
0x07 中雨、大雨、暴雨
0x08 雨加雪、冻雨
0x09 小雪
0x0a 大雪、暴雪
0x0b 沙尘暴、浮尘
0x0c 雾、雾霾
```
一般用户从第三方平台获取的天气代码与上述列表不一致，需要自己对应转换一下在设置到手环。