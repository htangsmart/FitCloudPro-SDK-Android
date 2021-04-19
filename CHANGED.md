# 2021-04-19
## 发布libraryCore_v1.1.2.aar
1.自定义表盘添加新的LCD类型。
2.添加压力功能。当`WristbandVersion#isPressureEnabled()`为true时，手环支持压力功能。
3.添加表盘组件功能。详细参考`6.7.5 表盘组件功能`
4.添加闹钟和日程改变的消息。`WristbandManager#MSG_CHANGE_ALARM`,`WristbandManager#MSG_CHANGE_SCHEDULE`
5.添加允许手环设置日程的指令.`WristbandManager#setAllowWristbandChangeSchedule(boolean allow)`

# 2021-03-05
## 发布libraryCore_v1.1.1.aar
1.自定义表盘添加新的LCD类型。
2.多表盘推送功能修改。详细参考开发文档`6.7.3、多表盘升级`，以及sample功能表盘升级示例。

## 发布libraryDfu_v1.0.3.aar
1. 升级原厂OTA库

# 2021-01-23
## 发布libraryCore_v1.1.0.aar
1.添加多表盘推送功能，详细参考开发文档`6.7.1、多表盘升级`，以及javadoc文档。

# 2021-01-11
## 发布libraryCore_v1.0.9.aar
1.添加锁屏设置，当`WristbandVersion#isExtLockScreen()`为true时，可使用`WristbandManager#setLockScreen`设置锁屏和解锁
2.添加Hike和YouTube提醒，当`WristbandVersion#isExtAncsHikeYouTube()`为true时，支持这两个提醒
3.添加日程设置，当`WristbandVersion#isExtSchedule()`为true时，可以使用`WristbandManager#setScheduleList`和`WristbandManager#requestScheduleList`设置和获取日程。用法基本和`WristbandAlarm`一样。

## 发布libraryDfu_v1.0.2.aar
1. 升级原厂OTA库，支持新的手环芯片类型


# 2020-11-19
## 发布libraryCore_v1.0.8.aar
1.解决消息通知标志位偶尔不正确的bug
2.解决拍照消息没有正确处理，导致`WristbandManager#observerWristbandMessage`无法接收此消息的bug
3.DfuManager#start方法支持传入Uri，兼容Android10分区存储无法获取外部文件路径的问题
4.自定义表盘添加支持新的表盘LCD类型，自定义表盘Shape支持圆角设置
5.添加天气推送开关的设置。当`WristbandVersion#isExtWeatherSwitch()`为true时，可使用`FunctionConfig#FLAG_WEATHER_SWITCH`来开启或关闭手环上的天气推送。在APP无法获取天气时，可使用`WristbandManager#setWeatherException()`来提示手环。
6.添加洗手提醒设置`HandWashingReminderConfig`，当`WristbandVersion#isExtHandWashingReminder()`为true时，此设置有效。
7.手环语言设置添加阿尔尼亚语.
8.健康检测和久坐提醒中的间隔值范围加大.
9.添加手环调试辅助信息的获取

# 2020-08-10
## 发布libraryCore_v1.0.7.aar
1.`HealthyConfig`添加时间间隔设置，当`WristbandVersion#isExtHealthyConfigInterval()`为true时，此设置有效。
2.`SedentaryConfig`添加时间间隔设置，当`WristbandVersion#isExtSedentaryConfigInterval()`为true时，此设置有效。
3.`WristbandManager#setWeather`添加天气预报设置，当`WristbandVersion#isExtWeatherForecast()`为true时，此设置有效。
	
# 2020-07-29
1.更新自定义表盘sample和开发文档`6.10、自定义表盘`

# 2020-07-17
## 发布libraryCore_v1.0.6.aar
1.添加测试方法`WristbandManager#requestSleepRawForTest()`获取睡眠原始数据，方便调式分析
2.最近一次测量请求`WristbandManager#requestLatestHealthy()`加上温度数据。


# 2020-06-22
## 发布libraryCore_v1.0.5.aar
1.添加女性健康设置`WomenHealthyConfig`。
2.添加防护提醒设置`ProtectionReminderConfig`。
3.添加手环退出拍照界面的消息`WristbandManager#MSG_CAMERA_EXIT`
4.添加设置manufacturerId为0xfe的6字节的自定义广播数据功能，`WristbandManager#setCustomAdvertising(byte[])`
5.解决体温测量为负值时，但是解析数据出错的bug

# 2020-03-26
## 发布libraryCore_v1.0.4.aar
1.添加温度功能，类似以前的心率和血压等功能。参考开发文档`6.5、实时数据测量` 和 `6.6、数据同步`,
2.添加联系人功能，参考开发文档`6.9、联系人功能`

# 2020-03-26
## 发布libraryCore_v1.0.3.aar
## 发布libraryDfu_v1.0.1.aar
1.升级原厂OTA库

# 2020-03-21
## 发布libraryCore_v1.0.2.aar
1. 解决连续发送指令时，取消上一个指令，可能会导致下一个指令发送超时的bug
2. 解决通知消息太长，可能导致发送失败的bug
3. 添加WristbandLog，可以设置日志拦截器，控制sdk输出的日志
4. 添加WristbandManager#requestDialBinInfo()，获取表盘信息
5. 删除SleepData中深睡，浅睡和清醒的字段，可以使用SleepCalculateHelper在外部计算。因为部分手环的睡眠会分多段返回，所以把这个计算放到外部。当同一天获取到多次睡眠数据的时候，可以自己合并睡眠数据，并使用该方法计算睡眠的时长。
6. SportData新增4种运动类型.
7. WristbandVersion#isExtDialUiUpgrade()修改为isExtDialUpgrade

# 2020-02-06
## 发布libraryCore_v1.0.1.aar
1. 解决步数数据StepData单位错误的bug

# 2020-01-13
## 发布libraryCore_v1.0.0.aar
1. 自此版本开始，以版本名称命名aar包
2. 添加了对新消息通知协议的支持，WristbandNotification支持更长的name。对于开发者，不需要处理这点。
3. 添加了对新睡眠协议的支持，睡眠数据更加准确。对于开发者，不需要处理这点。
4. WristbandManager中的多个方法，添加了AceException的声明。如果发送指令给手环，手环长时间不响应时，将抛出AceException。
5. 添加了MTU的设置。使用WristbandApplication#betaSetRequestMtuEnabled(enabled)方法，可以在设备连接时，请求更大长度的MTU。这样可以加快指令的接受和发送。

## 发布libraryDfu_v1.0.0.aar
1. 自此版本开始，以版本名称命名aar包

# 2019-12-03
## 发布libraryCore-release_19_1203_1.aar
1. 解决连续指令发送时，某个指令异常导致整个指令队列阻塞的bug。第三方库RxAndroidBLE同样更新了此问题，请更新RxAndroidBLE为最新版本。
2. 优化同步数据流程指令的发送
3. 解决心率血压等健康测量无法正常结束的bug

## 发布libraryDfu-release_19_1203_1.aar
1. 优化DFU升级部分表盘无法成功的bug

# 2019-11-18
## 发布libraryCore-release_19_1118_1.aar
1. 添加获取手环最近一次健康测量数据的记录，`WristbandManager#requestLatestHealthy()`，详细内容参考开发文档`6.5.3、获取手环最近一次健康测量记录`

# 2019-11-13
## 发布libraryCore-release_19_1113_1.aar
1. 去掉`SportHR`类，添加`SportItem`类
2. 添加免打扰配置`NotDisturbConfig`

# 2019-10-13
## 发布libraryCore-release_19_1013_1.aar
1. 添加相机唤醒指令和音乐控制指令
2. 新增SDK内部计算同步数据`StepData`中距离和卡路里的功能。当 `WristbandVersion#isExtStepExtra()`为true时，SDK支持此功能。
3. 添加心率和血压预警功能。
4. 消息通知新增Snapchat类型
5. 解决启动心电测量不正常，以及不能及时收到结束心电事件的bug
6. 解决指令发送异常中断，有时候会导致后续指令无法正常发送的bug
7. 解决设备断开，指令队列不会清空的bug
8. 去掉`WristbandConfig`和`WristbandVersion`中不安全的set方法

## 发布libraryDfu-release_19_1013_1.aar
1. 升级Dfu库

# 2019-05-20
## 发布libraryCore-release_19_0520_1.aar
1. 解决睡眠数据解析错误的bug
2. 添加获取系统语言类型接口 Utils.getSystemLanguageType(Context)
3. 健康数据实时测量可以自定义时间

# 2019-04-10
## sample
1. 完善同步数据的文档和sample

# 2019-04-09
## sample
1. 完善实时数据测量的文档和sample

# 2019-04-05
## 发布libraryCore-release_19_0405_1.aar
 完成设备连接，指令发送，数据同步功能等。

## 发布libraryCore-release_19_0405_1.aar
完成固件升级功能。

## sample
1. 完善使用示例