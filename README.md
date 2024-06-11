# [Wiki](https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki)

# v3.0.1-beta03(2024-06-11)
1. Support dial function for ic type 8873 and 568x
2. Fix some ota bugs.
3. Fix the bug where sleep state may be 0
4. Add FASTRACK notification type
5. Add `FcBatteryAbility.isSupportObserve` 
6. `FcDialSpace.dialNum` may be -1, indicating that there is currently no dial in this space
7. `FcNotificationAbility` add new method to send app and telephony notification
8. Fix some other bugs.

# v3.0.1-beta02(2024-04-25)
1. Add advanced reminder function `FcRemindAbility`, which replaces the `FcSedentaryConfig` and `FcDrinkWaterConfig` functions. Old watches can still use the old API, but if `FcDeviceInfo.isSupport(FcDeviceInfo.Feature.ADVANCED_REMIND)` is true, then `FcRemindAbility` must be used
2. Add sleep nap feature if `FcDeviceInfo.isSupport(FcDeviceInfo.Feature.SLEEP_NAP)` is true, and use `SleepCalculateHelper.calculate` to get `SleepSummary` info.
3. Add ota function for ic type 8873
4. Add some customer customization features
5. Add `FcBatteryAbility`,`FcContactsAbility`,`FcNotificationAbility`,`FcFunctionAbility`
6. Delete `FcDeviceInfo.Feature.CONTACTS_100`, use `fcSDK.contactsAbility.getContactsMaxNumber()` to determine the number of contacts supported by the device

# v3.0.1-beta01(2024-04-12)
1. Starting from this version, online dependencies can be used for integration. Detailed usage reference sample project.
Add maven url in your `setting.gradle`
```
	repositories {
        google()
        mavenCentral()
        jcenter() // Warning: this repository is going to shut down soon
        ...
        maven {
            url = uri("http://120.78.153.20:8081/repository/maven-public/")
            allowInsecureProtocol = true
        }
    }
```

Add dependencies in your app `build.gradle`
```
    def weakit_version = "3.0.1-beta01"
    implementation("com.topstep.wearkit:sdk-base:$weakit_version")
    implementation("com.topstep.wearkit:sdk-fitcloud:$weakit_version")
```

2. The sdk init `FcSDK.Builder` params changed.
3. Api Change: `FlagUtil` package name changed.
4. Api Change: `BondHelper` package name changed.
5. Api Change: `FileDownloader` package name changed.
6. Api Change: `FileDownloadException` package name changed.
7. Api Change: `ProgressResult` package name changed.
8. Api Change: `BytesUtil` package name changed.
9. Api Change: `Optional` package name changed.
10. Some drawable resources package name changed from `com.topstep.fitcloud.sdk.v2.R` to `com.topstep.fitcloud.sdk.R`

# v2.0.6(2024-04-01)
1. Fix bug: EPO file download out of sequence
2. Fix bug: Remove bond fail
3. Add params `FcPriority` of `syncItem` or `syncData`
4. Add `distanceMeters` of `FcSportData`,Avoiding errors caused by float accuracy
5. Add new device LCD support


# v2.0.5(2023-09-20)
1. Add `FcBuiltInFeatures.autoSetTime` settings. You can use the automatic time setting features, or use `FcSettingsFeature.setTime` manual. By default autoSetTime is enabled.
2. Add `FcConfigFeature.refresh` to refresh configs manual.
3. Add new device LCD support
4. Add new types in `FcHabit`
5. Add `FcSettingsFeature.requestSupportScheduleTypes` to request types support in `FcSchedule`
6. Add Medal
7. Fix bug:Reduce the probability of OTA failure

# v2.0.4(2023-07-09)
1. Api add:`FcConnector.mediaControlExitSilentMode`, When you use [FcBuiltInFeatures.mediaControl], but not use [FcBuiltInFeatures.telephonyControl] or not extent [AbsPhoneStateListener],use this method to set built-in media controller to exit silent mode  when you telephony enter idle state.

# v2.0.3(2023-06-28)
1. Api Change: `FcDataFeature.requestLatestHealthData` move to `FcSettingsFeature.requestLatestHealthData`
2. Add [Cricket Match](https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki/12.Customized-features#cricket-match) feature

# v2.0.2(2023-06-26)

1. Api Change: `FcFunctionConfig.STRENGTHEN_TEST` rename to `FcFunctionConfig.ENHANCED_MEASUREMENT`
2. Api Change: `FcFunctionConfig.HOUR_STYLE` rename to `FcFunctionConfig.TIME_FORMAT`
3. Api Change: `FcFunctionConfig.WEATHER_SWITCH` rename to `FcFunctionConfig.WEATHER_DISPLAY`
4. Api Change: `FcFunctionConfig.EXERCISE_TARGET` rename to `FcFunctionConfig.EXERCISE_GOAL_DISPLAY`
5. Api Change: `FcBloodPressureConfig.isPrivateMode` rename to `FcBloodPressureConfig.isEnabled`
6. Api Change: `FcSpecialFeature.requestLanguage` move to `FcSettingsFeature.requestLanguage`
7. Api Change: `FcSpecialFeature.requestExerciseGoal` move to `FcSettingsFeature.requestExerciseGoal`

# v2.0.1(2023-06-07)

1. Fix the bug of FcDfuManager reconnect causing DFU failure when DFU is not completed
2. Fix the bug in some mobile phone Chinese simplified and traditional judgment errors
3. Optimize volume adjustment during calls

# v2.0.0(2023-05-21)
First version

# [Version 1.x.x](https://github.com/htangsmart/FitCloudPro-SDK-Android/tree/master_v1)
