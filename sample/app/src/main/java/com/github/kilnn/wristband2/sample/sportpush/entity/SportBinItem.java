package com.github.kilnn.wristband2.sample.sportpush.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class SportBinItem implements Parcelable {

    private int sportUiType;
    private String iconUrl;
    private String binUrl;

    public SportBinItem() {
    }

    public SportBinItem(int sportUiType, String iconUrl, String binUrl) {
        this.sportUiType = sportUiType;
        this.iconUrl = iconUrl;
        this.binUrl = binUrl;
    }

    protected SportBinItem(Parcel in) {
        sportUiType = in.readInt();
        iconUrl = in.readString();
        binUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sportUiType);
        dest.writeString(iconUrl);
        dest.writeString(binUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SportBinItem> CREATOR = new Creator<SportBinItem>() {
        @Override
        public SportBinItem createFromParcel(Parcel in) {
            return new SportBinItem(in);
        }

        @Override
        public SportBinItem[] newArray(int size) {
            return new SportBinItem[size];
        }
    };

    public int getSportUiType() {
        return sportUiType;
    }

    public void setSportUiType(int sportUiType) {
        this.sportUiType = sportUiType;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getBinUrl() {
        return binUrl;
    }

    public void setBinUrl(String binUrl) {
        this.binUrl = binUrl;
    }
}
