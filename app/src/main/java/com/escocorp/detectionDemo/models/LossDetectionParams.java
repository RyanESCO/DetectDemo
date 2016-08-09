package com.escocorp.detectionDemo.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ryan Dymock on 7/20/2016.
 */

public class LossDetectionParams implements Parcelable {
    private int cacheSize = 20;
    private int minCacheSize = 10;
    private int dtMin = 1;
    private int maxTimeSeconds = 300;
    private double pCrit = 0.99;
    private int maxAlarmCount = 3;
    private boolean annunciateOn = true;

    public LossDetectionParams(){
       annunciateOn = true;

    }

    protected LossDetectionParams(Parcel in) {
        cacheSize = in.readInt();
        minCacheSize = in.readInt();
        dtMin = in.readInt();
        maxTimeSeconds = in.readInt();
        pCrit = in.readDouble();
        maxAlarmCount = in.readInt();
        annunciateOn = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(cacheSize);
        dest.writeInt(minCacheSize);
        dest.writeInt(dtMin);
        dest.writeInt(maxTimeSeconds);
        dest.writeDouble(pCrit);
        dest.writeInt(maxAlarmCount);
        dest.writeByte((byte) (annunciateOn ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Creator<LossDetectionParams> CREATOR = new Creator<LossDetectionParams>() {
        @Override
        public LossDetectionParams createFromParcel(Parcel in) {
            return new LossDetectionParams(in);
        }

        @Override
        public LossDetectionParams[] newArray(int size) {
            return new LossDetectionParams[size];
        }
    };
}