package com.escocorp.detectionDemo.models;

import com.escocorp.detectionDemo.BluetoothLeService;
import com.google.gson.annotations.SerializedName;

public abstract class MachineFeature implements IMachineFeature {

    public static final int FEATURE_TYPE_TOOTH = 0;
    public static final int FEATURE_TYPE_SHROUD = 1;
    public static final int FEATURE_TYPE_WING_SHROUD = 2;
    public static final int FEATURE_TYPE_BUCKET_MONITOR = 3;

    public static final int STATE_UNASSIGNED = 0;
    public static final int STATE_QUEUED = 1;
    public static final int STATE_ASSIGNED = 2;

    private String id;
    private String name;
    @SerializedName("feature_type")
    private int featureType = -1;
    private int position = 0;
    private ISensor sensor;
    @SerializedName("lossDetectionParams")
    private LossDetectionParams params;
    private int state = BluetoothLeService.STATE_DISCONNECTED;

    public MachineFeature(){
        params = new LossDetectionParams();
    }
    @Override
    public int getFeatureType() {
        return featureType;
    }

    @Override
    public void setFeatureType(int type) {
        this.featureType = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ISensor getSensor() {
        return sensor;
    }

    public void setSensor(ISensor sensor) {
        this.sensor = sensor;
        params = new LossDetectionParams();
        if (null==this.sensor){
            name=getName();
        }
    }

    @Override
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        if (null==sensor) {
            switch (getFeatureType()) {
                case MachineFeature.FEATURE_TYPE_TOOTH:
                    this.name = "TOOTH-" + (getPosition()+1);
                    break;
                case MachineFeature.FEATURE_TYPE_SHROUD:
                    this.name = "SHROUD-" + (getPosition());
                    break;
                case MachineFeature.FEATURE_TYPE_WING_SHROUD:
                    this.name = "WINGSHROUD-" + (getPosition()+1);
                    break;
                case MachineFeature.FEATURE_TYPE_BUCKET_MONITOR:
                    this.name = "MONITOR-" + (getPosition()+1);
                    break;
                default:
                    this.name = "NA";
            }
        }else if (null!=sensor.getName()){
            this.name = sensor.getName();
        }
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}
