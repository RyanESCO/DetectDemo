package com.escocorp.detectionDemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.escocorp.detectionDemo.models.Point3D;
import com.escocorp.detectionDemo.models.Sensor;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class DeviceScanCallback extends ScanCallback {

    //Broadcast Intent Constants
    public final static String BEGIN_SCAN = "com.rivetry.esco.bluetooth.BEGIN_SCAN";
    public final static String DEVICE_SCAN_COMPLETE = "com.rivetry.esco.bluetooth.DEVICE_SCAN_COMPLETE";
    public final static String DEVICE_SCAN_CANCEL = "com.rivetry.esco.bluetooth.DEVICE_SCAN_CANCEL";
    public final static String DEVICE_SCAN_RESULT = "com.rivetry.esco.bluetooth.DEVICE_SCAN_RESULT";
    public final static String QR_RESULT = "com.rivetry.esco.bluetooth.QR_RESULT";
    public final static String DEVICE_IN_PROXIMITY = "com.rivetry.esco.bluetooth.DEVICE_IN_PROXIMITY";
    public final static String EXTRA_ADDRESS = "com.rivetry.esco.bluetooth.EXTRA_ADDRESS";
    public final static String EXTRA_NAME = "com.rivetry.esco.bluetooth.EXTRA_NAME";
    public final static String EXTRA_DEVICE = "com.rivetry.esco.bluetooth.EXTRA_DEVICE";
    public final static String EXTRA_ERROR_MSG = "com.rivetry.esco.bluetooth.ERROR_MESSAGE";
    public final static String EXTRA_SCAN_RESULT = "com.rivetry.esco.bluetooth.EXTRA_SCAN_RESULT";
    public static final String PART_ASSIGNED = "com.rivetry.esco.bluetooth.PART_ASSIGNED" ;
    public static final String RSSI = "com.rivetry.esco.bluetooth.RSSI";
    public static final String SIMULATED_LOSS_DETECTED = "com.rivetry.esco.bluetooth.SIMULATED_LOSS_DETECTED";

    public static final int DATA_TYPE_FLAGS = 0x01;
    public static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02;
    public static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03;
    public static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04;
    public static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05;
    public static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06;
    public static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07;
    public static final int DATA_TYPE_LOCAL_NAME_SHORT = 0x08;
    public static final int DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09;
    public static final int DATA_TYPE_TX_POWER_LEVEL = 0x0A;
    public static final int DATA_TYPE_SERVICE_DATA = 0x16;
    public static final int DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF;

    public static byte[] extractBytes(byte[] scanRecord, int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(scanRecord, start, bytes, 0, length);
        return bytes;
    }

    public static Point3D getAcceleration(byte[] data, int offset) {
        final float SCALE = (float) 4096.0;

        int x = (data[offset + 1] << 8) + data[offset + 0];
        int y = (data[offset + 3] << 8) + data[offset + 2];
        int z = (data[offset + 5] << 8) + data[offset + 4];

        // scale coords
        return new Point3D((x / SCALE) * -1, y / SCALE, (z / SCALE) * -1);
    }

    final ArrayList<String> mDiscoveredDeviceAddresses = new ArrayList<String>();
    final HashMap<String, Sensor> mSensorMap = new HashMap<>();
    final Context mContext;

    public DeviceScanCallback(Context context){
        mContext = context;
    }

    @Override
    public void onScanResult (int callbackType, ScanResult result) {
        //Log.d("RCD","OnScanResult");

        final Gson gson = new Gson();
        String payload = gson.toJson(result);
        //Log.d("RCD - payload",payload);

        String timeStamp = new SimpleDateFormat("HH.mm.ss.SSS").format(new java.util.Date());
        Log.d("BT TEST",timeStamp+","+result.getDevice().getName());

        final BluetoothDevice device = result.getDevice();
        //we only care about BLE so pare out other bluetooth types
        if(device.getType()==BluetoothDevice.DEVICE_TYPE_LE||device.getType()==BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
            /*final ISensor sensor = new Sensor(result.getDevice());
            mDiscoveredDevices.add(sensor.getMacAddress());
            //Send a broadcast to main part of the app to alert it to new found device
            final Intent broadcast = new Intent(DEVICE_SCAN_RESULT);
            broadcast.putExtra(EXTRA_ADDRESS, sensor.getMacAddress());
            broadcast.putExtra(EXTRA_DEVICE, sensor);
            mContext.sendBroadcast(broadcast);*/

            final Sensor sensor = new Sensor(result);
            String deviceName = sensor.getName();
            Log.d("RCD1","OnScanResult " + deviceName);
            Log.d("RCD",sensor.getName() + " at " + String.valueOf(sensor.getRssi()) + "db");

            if(!mSensorMap.containsKey(sensor.getName())) {
                mSensorMap.put(sensor.getName(),sensor);
            }
            else {
                Sensor sensorToUpdate = mSensorMap.get(sensor.getName());
                sensorToUpdate.setRssi(result.getRssi());
            }

            if(!mDiscoveredDeviceAddresses.contains(device.getAddress())){
                mDiscoveredDeviceAddresses.add(sensor.getMacAddress());
            }

            //Send a broadcast to main part of the app to alert it to new found device
            final Intent broadcast = new Intent(DEVICE_SCAN_RESULT);
            broadcast.putExtra(EXTRA_ADDRESS, sensor.getMacAddress());
            broadcast.putExtra(EXTRA_DEVICE, sensor);
            broadcast.putExtra(EXTRA_SCAN_RESULT,result);
            broadcast.putExtra("name",sensor.getName());
            broadcast.putExtra(RSSI,result.getRssi());
            mContext.sendBroadcast(broadcast);
        }

    }
}
