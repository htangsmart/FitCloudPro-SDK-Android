# [Wiki](https://github.com/htangsmart/FitCloudPro-SDK-Android/wiki)

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
