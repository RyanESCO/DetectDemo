package com.escocorp.detectionDemo.models;

import android.os.Parcel;

public class Shroud extends MachineFeature {

    public Shroud(int position){
        super();
        this.setFeatureType(MachineFeature.FEATURE_TYPE_SHROUD);
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
