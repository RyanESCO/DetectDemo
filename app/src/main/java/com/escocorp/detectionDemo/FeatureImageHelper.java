package com.escocorp.detectionDemo;

import com.escocorp.detectionDemo.models.MachineFeature;

public class FeatureImageHelper {

    public static int getGraphicId(int featureType, int state, boolean directionUp){
        switch (state){
            case BluetoothLeService.STATE_LOSS_DETECTED:
                return getLossDetectedGraphic(featureType, directionUp);
            case BluetoothLeService.STATE_NORMAL:
                return getNormalGraphicId(featureType, directionUp);
            case BluetoothLeService.STATE_VIEWING:
                return getViewingGraphicId(featureType, directionUp);
            default:
                return getDisconnectedGraphicId(featureType, directionUp);
        }
    }

    private static int getDisconnectedGraphicId(int featureType, boolean directionUp) {
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

    private static int getNormalGraphicId(int featureType, boolean directionUp) {
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

    private static int getViewingGraphicId(int featureType, boolean directionUp) {
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

    private static int getLossDetectedGraphic(int featureType, boolean directionUp) {
        switch (featureType){
            case MachineFeature.FEATURE_TYPE_TOOTH:
                return directionUp? R.drawable.tooth_error:R.drawable.tooth_error_down;
            case MachineFeature.FEATURE_TYPE_SHROUD:
                return directionUp? R.drawable.shroud_error:R.drawable.shroud_error_down;
            case MachineFeature.FEATURE_TYPE_WING_SHROUD:
                return directionUp? R.drawable.wing_shroud_error:R.drawable.wing_shroud_error_down;
            case MachineFeature.FEATURE_TYPE_BUCKET_MONITOR:
                return directionUp? R.drawable.bucket_monitor_error:R.drawable.bucket_monitor_error;
            default:
                return -1;
        }
    }


}
