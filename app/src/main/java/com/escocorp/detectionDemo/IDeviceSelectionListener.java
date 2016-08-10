package com.escocorp.detectionDemo;

import android.support.v7.widget.RecyclerView;

public interface IDeviceSelectionListener {

    //void deviceSelected(ISensor device);
    //void deviceUnSelected(String deviceAddress);
    //void deviceUnpaired(String deviceAddress);
    void addChangeListener(RecyclerView.Adapter pairingsChangeListener);
    boolean containsDeviceAddress(String address);
    int getDeviceState(String address);
    void setDeviceState(String address, int state);
}
