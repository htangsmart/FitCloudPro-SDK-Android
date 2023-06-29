package com.topstep.fitcloud.sample2.data.entity

import androidx.annotation.IntDef
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.topstep.fitcloud.sample2.data.entity.SportRecordEntity.SportMainAttr.Companion.CALORIES
import com.topstep.fitcloud.sample2.data.entity.SportRecordEntity.SportMainAttr.Companion.DISTANCE
import com.topstep.fitcloud.sample2.data.entity.SportRecordEntity.SportMainAttr.Companion.STEP
import com.topstep.fitcloud.sample2.utils.room.TimeConverter
import com.topstep.fitcloud.sample2.utils.room.UUIDConverter
import java.util.*

@Entity
data class SportRecordEntity(
    val userId: Long,

    @PrimaryKey
    @field:TypeConverters(UUIDConverter::class)
    val sportId: UUID,

    @field:TypeConverters(TimeConverter::class)
    val time: Date,

    val duration: Int,
    val distance: Float,
    val calorie: Float,
    val step: Int,
    val climb: Float,
    val sportType: Int,

    /**
     * If gpsId is not null, So I think this data is incomplete because its associated GPS data has not yet been synchronized from the device.
     */
    val gpsId: String? = null,
) {

    /**
     * The sport launch type.
     */
    @IntDef(
        SportLaunchType.DEVICE,
        SportLaunchType.DEVICE_APP,
        SportLaunchType.APP,
        SportLaunchType.APP_DEVICE,
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class SportLaunchType {
        companion object {
            /**
             * Sport launch by device self
             */
            const val DEVICE = 0

            /**
             * Device launch APP start sport
             */
            const val DEVICE_APP = 1

            /**
             * Sport launch by app self
             */
            const val APP = 2

            /**
             * App launch Device start sport
             */
            const val APP_DEVICE = 3
        }
    }

    /**
     * The device main attribute.
     *
     * Such as sport Outdoor walking's main attribute is [DISTANCE]
     */
    @IntDef(
        DISTANCE,
        STEP,
        CALORIES,
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class SportMainAttr {
        companion object {
            const val DISTANCE = 0
            const val STEP = 1
            const val CALORIES = 2
        }
    }

    companion object {
        private const val SPORT_MASK_OD_RUN = 0x05 //Outdoor running
        private const val SPORT_MASK_ID_RUN = 0x09 //Indoor Running
        private const val SPORT_MASK_OD_WALK = 0x0d //Outdoor walking
        private const val SPORT_MASK_MOUNTAINEERING = 0x11 //Mountaineering
        private const val SPORT_MASK_ID_WALK = 0x5d//Indoor walking

        @SportLaunchType
        fun getSportLaunchType(sportType: Int): Int {
            return if (sportType <= 0) SportLaunchType.DEVICE else (sportType - 1) % 4
        }

        /**
         * Obtain which type of sport belongs to
         */
        fun getSportMask(sportType: Int): Int {
            return if (sportType <= 0) 0 else sportType - getSportLaunchType(sportType)
        }

        @SportMainAttr
        fun getSportMainAttr(sportType: Int): Int {
            return when (getSportMask(sportType)) {
                SPORT_MASK_OD_RUN, SPORT_MASK_ID_RUN, SPORT_MASK_OD_WALK, SPORT_MASK_ID_WALK -> {
                    DISTANCE
                }
                SPORT_MASK_MOUNTAINEERING -> {
                    STEP
                }
                else -> {
                    CALORIES
                }
            }
        }
    }
}

@Entity
data class SportGpsEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long,

    /**
     * Which [SportRecordEntity] does it belong to
     */
    @field:TypeConverters(UUIDConverter::class)
    val sportId: UUID,

    val duration: Int,
    val lng: Double,
    val lat: Double,
    val altitude: Float,
    val satellites: Int,
    val isRestart: Boolean,
)