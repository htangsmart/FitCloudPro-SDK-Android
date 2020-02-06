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