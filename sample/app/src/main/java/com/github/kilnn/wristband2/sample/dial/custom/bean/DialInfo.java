package com.github.kilnn.wristband2.sample.dial.custom.bean;

public class DialInfo {

    private int dialNum;
    private int binVersion;
    private String imgUrl;//缩略图的url
    private String deviceImgUrl;//设备缩略图
    private String binUrl;//bin文件下载地址url
    private String name;//样式名称
    private int downloadCount;//下载次数

    public DialInfo() {
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
}
