package com.escocorp.detectionDemo.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.escocorp.detectionDemo.BluetoothLeService;
import com.escocorp.detectionDemo.IDeviceSelectionListener;
import com.escocorp.detectionDemo.IFeatureSelectionListener;
import com.escocorp.detectionDemo.IPairingsListenerActivity;
import com.escocorp.detectionDemo.models.Bucket;
import com.escocorp.detectionDemo.models.IMachineFeature;
import com.escocorp.detectionDemo.models.ISensor;
import com.escocorp.detectionDemo.models.MachineFeature;
import com.escocorp.detectionDemo.models.Sensor;

import java.util.ArrayList;
import java.util.List;

public class PairingsController implements IDeviceSelectionListener, IFeatureSelectionListener {

    private Bucket pairingModel;
    private ISensor selectedDevice = null;
    private List<RecyclerView.Adapter> changeListeners = new ArrayList<>();
    private ArrayList<IMachineFeature> queuedArrayList = new ArrayList<>();
    private final IPairingsListenerActivity adapterManager;

    public static final String PAIRING_DATA = "pairingData";
    public static final int STATE_INIT = 0;
    public static final int STATE_WAITING = 1;
    public static final int STATE_SCANNING = 2;
    public static final int STATE_PROCESSING = 3;
    public static final int STATE_CONNECTING = 4;
    public static final int STATE_CONNECTED = 5;
    public static final int STATE_CANCELLED = 6;

    public int getPositionQueued() {
        return positionQueued;
    }

    public void setPositionQueued(int positionQueued) {
        this.positionQueued = positionQueued;
    }

    private int positionQueued;

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    private int currentState;

    public PairingsController(IPairingsListenerActivity adapterManager){
        pairingModel = new Bucket();
        this.adapterManager = adapterManager;
        currentState = STATE_INIT;
    }

    private void notifyChangeListeners(){
        for (int idx=0;idx<changeListeners.size();idx++){
            changeListeners.get(idx).notifyDataSetChanged();
        }
    }

    public Bucket getPairingModel(){
        return pairingModel;
    }


    public void setPairingModel(Bucket pairingModel){
        this.pairingModel = pairingModel;
    }

/*    @Override
    public void deviceSelected(ISensor device) {
        selectedDevice = device;
    }

    @Override
    public void deviceUnSelected(String deviceAddress) {
        selectedDevice = null;
    }

    @Override
    public void deviceUnpaired(String deviceAddress) {
        updatePairConnectionState(deviceAddress, BluetoothLeService.STATE_DISCONNECTED);
    }

    @Override
    public void featureUnpaired(String deviceAddress) {
        updatePairConnectionState(deviceAddress, BluetoothLeService.STATE_DISCONNECTED);
    }*/

    @Override
    public void addChangeListener(RecyclerView.Adapter pairingsChangeListener) {
        changeListeners.add(pairingsChangeListener);
    }

    @Override
    public void toggleFeatureState(IMachineFeature feature, Context context) {
        /*Bucket previousConfigBucket = ConfigHelper.loadBucketConfig(context);
        int previousConfigState = BluetoothLeService.STATE_DISCONNECTED;
        ISensor previousSensor = new Sensor();

        for (IMachineFeature f:previousConfigBucket.getFeatures()){
            if (f.getPosition()==feature.getPosition()&&f.getFeatureType()==feature.getFeatureType()){
                previousConfigState = f.getState();
                previousSensor = f.getSensor();

            }

        }

        if (feature.isBLEComponent()) {

            if(feature.getState()==MachineFeature.STATE_LOSS_DETECTED){
                feature.setState(previousConfigState);
                if(previousSensor!=null){
                    feature.setSensor(previousSensor);
                }

            } else {
                feature.setState(MachineFeature.STATE_LOSS_DETECTED);
                feature.setSensor(null);

            }

            notifyChangeListeners();

        }*/

    }


    public void assignPosition(Sensor device, int position){
        /*int position = 0;
        for (IMachineFeature f:pairingModel.getFeatures()){
            if (device.getName().equals(f.getName())){
              position = f.getPosition();
            }
        }*/

        IMachineFeature feature = pairingModel.getFeatures().get(position);

        if (feature.isBLEComponent()) {
            feature.setSensor(device);
            feature.setState(MachineFeature.STATE_ASSIGNED);
            notifyChangeListeners();
        }

    }


    ///used to fill in Sensor Data after pre-initializing for MinEXPO demo
    public void updateSensorData(Sensor device){
        /*int position;
        //iterate through model to find position
        IMachineFeature feature = pairingModel.getFeatures().get(position);
        if (feature.isBLEComponent()) {
            //check for existing pairings
            for (IMachineFeature f:pairingModel.getFeatures()){
                if (f.getSensor()==device){
                    //it already exists so exit the select feature operation
                    return;
                }
            }
            feature.setSensor(device);
            feature.setState(MachineFeature.STATE_ASSIGNED);
            //adapterManager.connectToDevice(getPairingModel(), device.getMacAddress());
            notifyChangeListeners();
        }*/



    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(PAIRING_DATA, pairingModel);
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        if (null!=savedInstanceState) {
            final Bucket parcelableBucket = savedInstanceState.getParcelable(PAIRING_DATA);
            if (null!=parcelableBucket){
                setPairingModel(parcelableBucket);
            }
        }
    }

    @Override
    public boolean containsDeviceAddress(String address) {
        return pairingModel.containsAddress(address);
    }

    @Override
    public int getDeviceState(String address) {
        if (null!=pairingModel.getFeature(address)) {
            return pairingModel.getFeature(address).getState();
        } else {
            return BluetoothLeService.STATE_DISCONNECTED;
        }
    }

    @Override
    public void setDeviceState(String address, int state){
        if (null!=pairingModel.getFeature(address)) {
            pairingModel.getFeature(address).setState(state);
        }
    }

}
