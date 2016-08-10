package com.escocorp.detectionDemo;

import com.escocorp.detectionDemo.models.Bucket;

public interface IPairingsListenerActivity {

    void connectToDevice(final Bucket pairingsMap, final String bleAddress);

}
