package com.escocorp.detectionDemo.models;

import android.os.Parcelable;

public interface IMachineFeature extends Parcelable {

    int getFeatureType();
    void setFeatureType(int featureType);
    boolean isBLEComponent();
    ISensor getSensor();
    void setSensor(ISensor sensor);
    int getPosition();
    void setPosition(int position);
    void setState(int state);
    int getState();
    String getName();
    void setName(String name);
    String getId();
    void setId(String id);

}
