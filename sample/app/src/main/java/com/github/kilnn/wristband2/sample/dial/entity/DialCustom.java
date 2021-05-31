package com.github.kilnn.wristband2.sample.dial.entity;

import android.net.Uri;

public class DialCustom {

    private String binUrl;
    private String styleName;//样式名
    private Uri styleUri;

    public String getBinUrl() {
        return binUrl;
    }

    public void setBinUrl(String binUrl) {
        this.binUrl = binUrl;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public Uri getStyleUri() {
        return styleUri;
    }

    public void setStyleUri(Uri styleUri) {
        this.styleUri = styleUri;
    }
}
