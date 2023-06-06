package com.github.kilnn.wristband2.sample.dial.custom

import android.net.Uri
import com.github.kilnn.wristband2.sample.MyApplication
import com.github.kilnn.wristband2.sample.R
import com.github.kilnn.wristband2.sample.dial.entity.DialCustom
import com.github.kilnn.wristband2.sample.dial.entity.DialInfoComplex
import com.github.kilnn.wristband2.sample.dial.task.DialParam
import com.github.kilnn.wristband2.sample.utils.Utils
import com.htsmart.wristband2.dial.DialDrawer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.withContext

/**
 * 不支持自定义升级的异常
 */
class UnSupportCustomException : Exception()

private class SupportStyle constructor(var styleName: String, var resId: Int)

class TaskGetDialCustomCompat {

    private val apiClient = MyApplication.getApiClient()
    private val context = MyApplication.getInstance()

    suspend fun execute(param: DialParam): DialCustomCompat {
        return withContext(Dispatchers.IO) {
            if (param.isGUI) {
                val list: List<DialInfoComplex> = apiClient.getDialCustomGUI(param.lcd, param.toolVersion).awaitFirst()
                if (list.isEmpty()) {//数组不能为空
                    throw UnSupportCustomException()
                }
                val dialInfo = list[0]//取第一个对象
                val components = dialInfo.components
                if (components.isNullOrEmpty()) {//里面需要有组件
                    throw UnSupportCustomException()
                }
                val urls = components[0].urls
                if (urls.isNullOrEmpty()) {//组件里需要有有样式
                    throw UnSupportCustomException()
                }
                val styleBaseOnWidth = param.shape.width() * 2//新的GUI协议自定义表盘，图片都是放大两倍，所以就是Shape.width()*2
                val styles = ArrayList<DialCustomCompat.Style>(urls.size)
                for (styleIndex in urls.indices) {
                    val url = urls[styleIndex]
                    styles.add(DialCustomCompat.Style(styleIndex, Uri.parse(url), styleBaseOnWidth, dialInfo.binUrl, dialInfo.binSize))
                }

                DialCustomCompat(
                    Uri.parse(dialInfo.previewImgUrl),//用服务器图片作为预览背景
                    styles
                )
            } else {
                val list: List<DialCustom> = apiClient.getDialCustom(param.lcd, param.toolVersion).awaitFirst()
                if (list.isEmpty()) throw UnSupportCustomException()

                //按照下面的顺序显示在界面
                val supportList = listOf(
                    SupportStyle("White", R.drawable.dial_style1),
                    SupportStyle("Black", R.drawable.dial_style2),
                    SupportStyle("Yellow", R.drawable.dial_style3),
                    SupportStyle("Green", R.drawable.dial_style4),
                    SupportStyle("Gray", R.drawable.dial_style5)
                )
                val styles: MutableList<DialCustomCompat.Style> = ArrayList(supportList.size)
                for (support in supportList) {
                    for (dial in list) {
                        if (support.styleName == dial.styleName) {
                            styles.add(
                                DialCustomCompat.Style(
                                    styles.size,
                                    Utils.getUriFromDrawableResId(context, support.resId),
                                    DialDrawer.STYLE_BASE_ON_WIDTH,//旧协议的自定义表盘，样式基于800像素设计的
                                    dial.binUrl,
                                    dial.binSize
                                )
                            )
                            break
                        }
                    }
                }

                if (styles.isEmpty()) throw UnSupportCustomException()

                DialCustomCompat(
                    Utils.getUriFromDrawableResId(context, R.drawable.dial_default_bg),//用本地图片作为预览背景
                    styles
                )
            }
        }
    }
}