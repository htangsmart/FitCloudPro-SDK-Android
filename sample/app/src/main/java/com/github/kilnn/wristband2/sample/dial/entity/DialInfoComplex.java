package com.github.kilnn.wristband2.sample.dial.entity;

import androidx.annotation.Nullable;

import java.util.List;

public class DialInfoComplex {
    public static class Component {
        @Nullable
        private List<String> urls;//组件的图片url

        @Nullable
        public List<String> getUrls() {
            return urls;
        }

        public void setUrls(@Nullable List<String> urls) {
            this.urls = urls;
        }
    }

    private int dialNum;
    private int lcd;
    private String toolVersion;
    private String binUrl;
    private int binVersion;
    private String imgUrl;
    private String deviceImgUrl;
    private String name;
    private int downloadCount;
    private String type;// basic: 第一版表盘   gui: 新版gui表盘  gui-custom：新版gui的自定义表盘
    private int hasComponent;     // 0 无组件，1 有组件
    private String previewImgUrl;//组件预览背景图
    private long binSize;//bin文件大小

    @Nullable
    private List<Component> components;

    public int getDialNum() {
        return dialNum;
    }

    public void setDialNum(int dialNum) {
        this.dialNum = dialNum;
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

    public String getPreviewImgUrl() {
        return previewImgUrl;
    }

    public void setPreviewImgUrl(String previewImgUrl) {
        this.previewImgUrl = previewImgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBinUrl() {
        return binUrl;
    }

    public void setBinUrl(String binUrl) {
        this.binUrl = binUrl;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getHasComponent() {
        return hasComponent;
    }

    public void setHasComponent(int hasComponent) {
        this.hasComponent = hasComponent;
    }

    @Nullable
    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(@Nullable List<Component> components) {
        this.components = components;
    }

    public long getBinSize() {
        return binSize;
    }

    public void setBinSize(long binSize) {
        this.binSize = binSize;
    }
}
