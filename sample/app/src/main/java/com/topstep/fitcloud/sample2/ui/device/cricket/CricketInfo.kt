package com.topstep.fitcloud.sample2.ui.device.cricket

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntDef
import com.squareup.moshi.JsonClass
import com.topstep.fitcloud.sdk.v2.model.special.cricket.FcCricketLiveMatch

@JsonClass(generateAdapter = true)
class CricketInfo(
    matchId: Long,
    matchName: String,
    matchTime: Long,
    team1Id: Int,
    team2Id: Int
) : FcCricketLiveMatch(
    matchId, matchName, matchTime, team1Id, team2Id
), Parcelable {

    @IntDef(
        State.UPCOMING,
        State.LIVE,
        State.RESULT,
    )
    @Retention(AnnotationRetention.RUNTIME)
    annotation class State {
        companion object {
            const val UPCOMING = 0
            const val LIVE = 1
            const val RESULT = 2
        }
    }

    @State
    var state: Int = State.UPCOMING

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt()
    ) {
        setTeam1Data(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt())
        setTeam2Data(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt())
        setBatsman1Data(parcel.readString()!!, parcel.readInt(), parcel.readInt())
        setBatsman2Data(parcel.readString()!!, parcel.readInt(), parcel.readInt())
        setBowlerData(parcel.readString()!!, parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt())
        setInningsData(parcel.readInt(), parcel.readFloat(), parcel.readFloat())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(matchId)
        parcel.writeString(matchName)
        parcel.writeLong(matchTime)
        parcel.writeInt(team1Id)
        parcel.writeInt(team2Id)
        parcel.writeInt(team1Runs)
        parcel.writeInt(team1Wickets)
        parcel.writeInt(team1Overs)
        parcel.writeInt(team1Balls)
        parcel.writeInt(team2Runs)
        parcel.writeInt(team2Wickets)
        parcel.writeInt(team2Overs)
        parcel.writeInt(team2Balls)

        parcel.writeString(batsman1Name)
        parcel.writeInt(batsman1Runs)
        parcel.writeInt(batsman1Balls)

        parcel.writeString(batsman2Name)
        parcel.writeInt(batsman2Runs)
        parcel.writeInt(batsman2Balls)

        parcel.writeString(bowlerName)
        parcel.writeInt(bowlerOvers)
        parcel.writeInt(bowlerBalls)
        parcel.writeInt(bowlerRuns)
        parcel.writeInt(bowlerWickets)

        parcel.writeInt(innings)
        parcel.writeFloat(crr)
        parcel.writeFloat(rrr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CricketInfo> {
        override fun createFromParcel(parcel: Parcel): CricketInfo {
            return CricketInfo(parcel)
        }

        override fun newArray(size: Int): Array<CricketInfo?> {
            return arrayOfNulls(size)
        }
    }

}