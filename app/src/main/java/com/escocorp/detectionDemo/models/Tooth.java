package com.escocorp.detectionDemo.models;

import android.os.Parcel;

public class Tooth extends MachineFeature {

    public Tooth(int position){
        super();
        this.setFeatureType(MachineFeature.FEATURE_TYPE_TOOTH);
        setPosition(position);
    }

    @Override
    public boolean isBLEComponent() {
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
