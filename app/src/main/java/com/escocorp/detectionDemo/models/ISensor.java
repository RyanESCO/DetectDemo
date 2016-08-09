package com.escocorp.detectionDemo.models;

import android.os.Parcelable;

public interface ISensor extends Parcelable {
    String getMacAddress();
    void setMacAddress(String macAddress);
    String getName();
    void setName(String name);
    int getType();
    void setType(int type);
    int getFeatureType();
    void setFeatureType(int featureType);
}
