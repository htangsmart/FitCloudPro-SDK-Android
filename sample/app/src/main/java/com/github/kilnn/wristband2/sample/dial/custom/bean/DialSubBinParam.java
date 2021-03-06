package com.github.kilnn.wristband2.sample.dial.custom.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class DialSubBinParam implements Parcelable {
    private byte dialType;
    private int dialNum;
    private int binVersion;
    private byte binFlag;
    private String imgUrl;//缩略图的url
    private String deviceImgUrl;//设备缩略图

    public DialSubBinParam() {
    }

    protected DialSubBinParam(Parcel in) {
        dialType = in.readByte();
        dialNum = in.readInt();
        binVersion = in.readInt();
        binFlag = in.readByte();
        imgUrl = in.readString();
        deviceImgUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(dialType);
        dest.writeInt(dialNum);
        dest.writeInt(binVersion);
        dest.writeByte(binFlag);
        dest.writeString(imgUrl);
        dest.writeString(deviceImgUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DialSubBinParam> CREATOR = new Creator<DialSubBinParam>() {
        @Override
        public DialSubBinParam createFromParcel(Parcel in) {
            return new DialSubBinParam(in);
        }

        @Override
        public DialSubBinParam[] newArray(int size) {
            return new DialSubBinParam[size];
        }
    };

    public byte getDialType() {
        return dialType;
    }

    public void setDialType(byte dialType) {
        this.dialType = dialType;
    }

    public int getDialNum() {
        return dialNum;
    }

    public void setDialNum(int dialNum) {
        this.dialNum = dialNum;
    }

    public int getBinVersion() {
        return binVersion;
    }

    public void setBinVersion(int binVersion) {
        this.binVersion = binVersion;
    }

    public byte getBinFlag() {
        return binFlag;
    }

    public void setBinFlag(byte binFlag) {
        this.binFlag = binFlag;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getDeviceImgUrl() {
        return deviceImgUrl;
    }

    public void setDeviceImgUrl(String deviceImgUrl) {
        this.deviceImgUrl = deviceImgUrl;
    }
}
