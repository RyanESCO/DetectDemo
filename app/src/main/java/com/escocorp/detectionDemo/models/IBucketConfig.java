package com.escocorp.detectionDemo.models;

import android.os.Parcelable;

public interface IBucketConfig extends Parcelable {

    int getTeeth();

    void setTeeth(int teeth);

    int getShrouds();

    void setShrouds(int shrouds);

    int getWingShrouds();

    void setWingShrouds(int wingShrouds);

    int getBucketMonitors();

    void setBucketMonitors(int bucketMonitors);

    String getImage();

    void setImage(String image);
}
