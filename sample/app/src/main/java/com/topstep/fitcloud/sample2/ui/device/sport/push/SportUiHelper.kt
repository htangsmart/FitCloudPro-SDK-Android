package com.topstep.fitcloud.sample2.ui.device.sport.push

import android.content.Context
import com.topstep.fitcloud.sample2.BuildConfig
import com.topstep.fitcloud.sample2.model.sport.push.SportPacket
import timber.log.Timber
import java.util.*

class SportUiHelper {
    val categoryAll = 0
    private val categoryCommon = 1
    private val categoryDanceGymnastics = 2
    private val categoryFight = 3
    private val categoryEquipmentBar = 4
    private val categoryBall = 5
    private val categoryWaterGlide = 6
    private val categoryWaistCore = 7
    private val categoryCasualFlexible = 8

    fun getCategories(): IntArray {
        return intArrayOf(
            categoryAll,
            categoryCommon,
            categoryDanceGymnastics,
            categoryFight,
            categoryEquipmentBar,
            categoryBall,
            categoryWaterGlide,
            categoryWaistCore,
            categoryCasualFlexible,
        )
    }

    fun getCategoryName(context: Context, category: Int): String? {
        val name = "sport_ui_category_00$category"
        return getStringFromResourceName(context, name)
    }

    private fun getStringFromResourceName(context: Context, name: String): String? {
        val resources = context.resources
        try {
            val resId = resources.getIdentifier(name, "string", BuildConfig.APPLICATION_ID)
            if (resId != 0) {
                return resources.getString(resId)
            }
        } catch (e: Exception) {
            Timber.w(e)
        }
        return null
    }

    fun getCategorySports(category: Int): IntArray {
        return when (category) {
            //常见
            categoryCommon -> intArrayOf(
                0,//户外骑行
                1,//户外跑步
                2,//室内跑步
                3,//户外健走
                4,//登山
                23,//室内走路
                33,//徒步
                67,//爬楼
                79,//踏步
                24,//室内骑行
                12,//跳绳
            )
            //舞蹈/体操
            categoryDanceGymnastics -> intArrayOf(
                26,//舞蹈
                48,//拉丁舞
                49,//街舞
                51,//芭蕾
                63,//健身操
                64,//团体操
                81,//体操
                112,//民族舞
            )
            //搏击
            categoryFight -> intArrayOf(
                50,//自由搏击
                65,//搏击操
                66,//击剑
                71,//拳击
                72,//跆拳道
                73,//空手道
                78,//摔跤
                80,//太极
                83,//武术
                97,//柔道
            )
            //器械/杠
            categoryEquipmentBar -> intArrayOf(
                9,//椭圆机
                13,//划船机
                40, //漫步机
                57,//钓鱼
                58,//飞盘
                87,//单杠
                88,//双杠
                90,//飞镖
                91,//射箭
                106,//引体向上
                110,//跳高
                113,//打猎
                114,//射击
                102,//跑步机
            )
            //球类
            categoryBall -> intArrayOf(
                5, //篮球
                7, //羽毛球
                8, //足球
                11,//乒乓球
                17,//网球
                18,//棒球
                19,//橄榄球
                20,//板球
                31,//排球
                34,//曲棍球
                37,//垒球
                52,//澳式足球
                53,//保龄球
                54,//壁球
                55,//冰壶
                68,//美式橄榄球
                70,//匹克球
                75,//手球
                86,//长曲棍球
                93,//毽球
                94,//冰球
                28,//高尔夫
            )
            //水上/滑行
            categoryWaterGlide -> intArrayOf(
                6,//游泳
                35,//划船
                39,//滑雪
                56,//单板滑雪
                59,//高山滑雪
                61,//滑冰
                89,//轮滑
                99,//滑板
                100,//平衡车
                101,//溜旱冰
                103,//跳水
                104,//冲浪
                105,//浮潜
            )
            //腰腹/核心
            categoryWaistCore -> intArrayOf(
                22,//力量训练
                29,//跳远
                30,//仰卧起坐
                36,//HIIT
                45,//功能性训练
                46,//体能训练
                47,//混合有氧
                60,//核心训练
                82,//田径
                95,//腰腹训练
                107,//俯卧撑
                108,//平板支撑
                109,//攀岩
            )
            //休闲/柔韧
            categoryCasualFlexible -> intArrayOf(
                10,//瑜伽
                16,//自由训练
                27,//呼啦圈
                32,//跑酷
                38,//越野跑
                41,//整理放松
                42,//交叉训练
                43,//普拉提
                44,//交叉配合
                62,//健身游戏
                69,//泡沫轴筋膜放松
                74,//柔韧度
                76,//手摇车
                77,//舒缓冥想类运动
                84,//休闲运动
                85,//雪上运动
                92,//骑马
                96,//最大摄氧量测试
                98,//蹦床
                111,//蹦极
                115,//马拉松
            )
            else -> throw IllegalArgumentException()
        }
    }

    fun getTypeName(context: Context, item: SportPacket): String {
        val name = "sport_ui_type_${String.format(Locale.US, "%03d", item.sportUiType)}"
        return getStringFromResourceName(context, name) ?: item.sportUiName
    }

}