package com.github.kilnn.wristband2.sample.dial.custom.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class DialRequestParam implements Parcelable {

    private String hardwareInfo;
    private int lcd;
    private String toolVersion;
    private List<DialSubBinParam> subBinParams;

    public DialRequestParam() {

    }

    protected DialRequestParam(Parcel in) {
        hardwareInfo = in.readString();
        lcd = in.readInt();
        toolVersion = in.readString();
        subBinParams = in.createTypedArrayList(DialSubBinParam.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hardwareInfo);
        dest.writeInt(lcd);
        dest.writeString(toolVersion);
        dest.writeTypedList(subBinParams);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DialRequestParam> CREATOR = new Creator<DialRequestParam>() {
        @Override
        public DialRequestParam createFromParcel(Parcel in) {
            return new DialRequestParam(in);
        }

        @Override
        public DialRequestParam[] newArray(int size) {
            return new DialRequestParam[size];
        }
    };

    public String getHardwareInfo() {
        return hardwareInfo;
    }

    public void setHardwareInfo(String hardwareInfo) {
        this.hardwareInfo = hardwareInfo;
    }

    public int getLcd() {
        return lcd;
    }

    public void setLcd(int lcd) {
        this.lcd = lcd;
    }

    public String getToolVersion() {
        return toolVersion;
    }

    public void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }

    public List<DialSubBinParam> getSubBinParams() {
        return subBinParams;
    }

    public void setSubBinParams(List<DialSubBinParam> subBinParams) {
        this.subBinParams = subBinParams;
    }

    @NonNull
    @Override
    public String toString() {
        return "[ DialRequestParam :" +
                "    hardwareInfo = " + hardwareInfo +
                "    lcd = " + lcd +
                "    toolVersion = " + toolVersion +
                " ]";
    }

}
