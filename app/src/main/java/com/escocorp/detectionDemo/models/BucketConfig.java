package com.escocorp.detectionDemo.models;

import android.os.Parcel;

import com.google.gson.reflect.TypeToken;

public class BucketConfig implements IBucketConfig {
    private int teeth = 0;
    private int shrouds = 0;
    private int wingShrouds = 0;
    private int bucketMonitors = 0;
    private String image;

    public BucketConfig(){}

    public BucketConfig(int teeth, int shrouds, int wingShrouds, int bucketMonitors) {
        this.teeth = teeth;
        this.shrouds = shrouds;
        this.wingShrouds = wingShrouds;
        this.bucketMonitors = bucketMonitors;
    }

    protected BucketConfig(Parcel in) {
        teeth = in.readInt();
        shrouds = in.readInt();
        wingShrouds = in.readInt();
        bucketMonitors = in.readInt();
        image = in.readString();
    }

    public int getTeeth() {
        return teeth;
    }

    public void setTeeth(int teeth) {
        this.teeth = teeth;
    }

    public int getShrouds() {
        return shrouds;
    }

    public void setShrouds(int shrouds) {
        this.shrouds = shrouds;
    }

    public int getWingShrouds() {
        return wingShrouds;
    }

    public void setWingShrouds(int wingShrouds) {
        this.wingShrouds = wingShrouds;
    }

    public int getBucketMonitors() {
        return bucketMonitors;
    }

    public void setBucketMonitors(int bucketMonitors) {
        this.bucketMonitors = bucketMonitors;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getTeeth());
        dest.writeInt(getShrouds());
        dest.writeInt(getWingShrouds());
        dest.writeInt(getBucketMonitors());
        dest.writeString(getImage());
    }

    public static final Creator<BucketConfig> CREATOR = new Creator<BucketConfig>() {
        @Override
        public BucketConfig createFromParcel(Parcel in) {
            return new BucketConfig(in);
        }

        @Override
        public BucketConfig[] newArray(int size) {
            return new BucketConfig[size];
        }
    };

    public static TypeToken<BucketConfig> getTypeToken() {
        return new TypeToken<BucketConfig>(){};
    }

}
