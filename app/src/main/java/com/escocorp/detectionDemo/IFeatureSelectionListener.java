package com.escocorp.detectionDemo;

import android.content.Context;
import android.hardware.Sensor;
import android.support.v7.widget.RecyclerView;

import com.escocorp.detectionDemo.models.Bucket;
import com.escocorp.detectionDemo.models.IMachineFeature;


public interface IFeatureSelectionListener {
    void toggleFeatureState(IMachineFeature feature, Context context);
    void addChangeListener(RecyclerView.Adapter pairingsChangeListener);
    void assignPosition(Sensor device, int position);
    Bucket getPairingModel();
}
