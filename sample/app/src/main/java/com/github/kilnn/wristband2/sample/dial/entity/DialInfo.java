package com.github.kilnn.wristband2.sample.dial.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(primaryKeys = {"projectNum", "dialNum"})
public class DialInfo implements Parcelable {

    @NonNull
    private String projectNum;
    private int lcd;
    private String toolVersion;

    private int dialNum;
    private int binVersion;
    private String imgUrl;//缩略图的url
    private String deviceImgUrl;//设备缩略图
    private String binUrl;//bin文件下载地址url
    private String name;//样式名称
    private int downloadCount;//下载次数
    private long binSize;//bin文件大小

    public DialInfo() {
    }


    protected DialInfo(Parcel in) {
        projectNum = in.readString();
        lcd = in.readInt();
        toolVersion = in.readString();
        dialNum = in.readInt();
        binVersion = in.readInt();
        imgUrl = in.readString();
        deviceImgUrl = in.readString();
        binUrl = in.readString();
        name = in.readString();
        downloadCount = in.readInt();
        binSize = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(projectNum);
        dest.writeInt(lcd);
        dest.writeString(toolVersion);
        dest.writeInt(dialNum);
        dest.writeInt(binVersion);
        dest.writeString(imgUrl);
        dest.writeString(deviceImgUrl);
        dest.writeString(binUrl);
        dest.writeString(name);
        dest.writeInt(downloadCount);
        dest.writeLong(binSize);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DialInfo> CREATOR = new Creator<DialInfo>() {
        @Override
        public DialInfo createFromParcel(Parcel in) {
            return new DialInfo(in);
        }

        @Override
        public DialInfo[] newArray(int size) {
            return new DialInfo[size];
        }
    };

    public String getProjectNum() {
        return projectNum;
    }

    public void setProjectNum(String projectNum) {
        this.projectNum = projectNum;
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

    public String getBinUrl() {
        return binUrl;
    }

    public void setBinUrl(String binUrl) {
        this.binUrl = binUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public long getBinSize() {
        return binSize;
    }

    public void setBinSize(long binSize) {
        this.binSize = binSize;
    }
}
