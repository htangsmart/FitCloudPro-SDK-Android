package com.github.kilnn.wristband2.sample.sportpush.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class SportBinItem implements Parcelable {

    private int sportType;
    private String iconUrl;
    private String binUrl;

    public SportBinItem() {
    }

    public SportBinItem(int sportType, String iconUrl, String binUrl) {
        this.sportType = sportType;
        this.iconUrl = iconUrl;
        this.binUrl = binUrl;
    }

    protected SportBinItem(Parcel in) {
        sportType = in.readInt();
        iconUrl = in.readString();
        binUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sportType);
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

    public int getSportType() {
        return sportType;
    }

    public void setSportType(int sportType) {
        this.sportType = sportType;
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
