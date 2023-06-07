package com.topstep.fitcloud.sample2

import android.content.res.Configuration
import androidx.multidex.MultiDexApplication
import com.github.kilnn.tool.system.SystemUtil
import com.topstep.fitcloud.sample2.utils.FormatterUtil

class MyApplication : MultiDexApplication() {

    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        fitCloudSDKInit(this)
        FormatterUtil.init(SystemUtil.getSystemLocal(this))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //配置改变，SystemLocale可能改变，重新设置下
        FormatterUtil.init(SystemUtil.getSystemLocal(this))
    }

}
