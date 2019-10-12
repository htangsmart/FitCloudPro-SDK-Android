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