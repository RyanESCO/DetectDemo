package com.escocorp.detectionDemo;

import com.escocorp.detectionDemo.models.MachineFeature;

public class FeatureImageHelper {

    public static int getGraphicId(int featureType, int state, boolean directionUp){
        switch (state){
            case BluetoothLeService.STATE_QUEUED:
                return getConnectingGraphicId(featureType, directionUp);
            case BluetoothLeService.STATE_CONNECTED:
                return getConnectedGraphicId(featureType, directionUp);
            default:
                return getDisconnectedGraphicId(featureType, directionUp);
        }
    }

    private static int getDisconnectedGraphicId(int featureType, boolean directionUp) {
        switch (featureType){
            case MachineFeature.FEATURE_TYPE_TOOTH:
                return directionUp? R.drawable.tooth:R.drawable.tooth_down;
            case MachineFeature.FEATURE_TYPE_SHROUD:
                return directionUp? R.drawable.shroud:R.drawable.shroud_down;
            case MachineFeature.FEATURE_TYPE_WING_SHROUD:
                return directionUp? R.drawable.wing_shroud:R.drawable.wing_shroud_down;
            case MachineFeature.FEATURE_TYPE_BUCKET_MONITOR:
                return directionUp? R.drawable.bucket_monitor:R.drawable.bucket_monitor;
            default:
                return -1;
        }
    }

    private static int getConnectedGraphicId(int featureType, boolean directionUp) {
        switch (featureType){
            case MachineFeature.FEATURE_TYPE_TOOTH:
                return directionUp? R.drawable.tooth_connected:R.drawable.tooth_connected_down;
            case MachineFeature.FEATURE_TYPE_SHROUD:
                return directionUp? R.drawable.shroud_connected:R.drawable.shroud_connected_down;
            case MachineFeature.FEATURE_TYPE_WING_SHROUD:
                return directionUp? R.drawable.wing_shroud_connected:R.drawable.wing_shroud_connected_down;
            case MachineFeature.FEATURE_TYPE_BUCKET_MONITOR:
                return directionUp? R.drawable.bucket_monitor_connected:R.drawable.bucket_monitor_connected;
            default:
                return -1;
        }
    }

    private static int getConnectingGraphicId(int featureType, boolean directionUp) {
        switch (featureType){
            case MachineFeature.FEATURE_TYPE_TOOTH:
                return directionUp? R.drawable.tooth_connecting:R.drawable.tooth_connecting_down;
            case MachineFeature.FEATURE_TYPE_SHROUD:
                return directionUp? R.drawable.shroud_connecting:R.drawable.shroud_connecting_down;
            case MachineFeature.FEATURE_TYPE_WING_SHROUD:
                return directionUp? R.drawable.wing_shroud_connecting:R.drawable.wing_shroud_connecting_down;
            case MachineFeature.FEATURE_TYPE_BUCKET_MONITOR:
                return directionUp? R.drawable.bucket_monitor_connecting:R.drawable.bucket_monitor_connecting;
            default:
                return -1;
        }
    }


}
