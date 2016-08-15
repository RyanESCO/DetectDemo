package com.escocorp.detectionDemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.escocorp.detectionDemo.models.EscoPart;
import com.escocorp.detectionDemo.models.Point3D;

import java.util.HashMap;

public class BluetoothLEScanner implements BluetoothAdapter.LeScanCallback {

    ScanListener mListener;

    private BluetoothAdapter mBluetoothAdapter;
    private ProgressDialog mProgress;
    Activity mActivity;
    Handler mHandler = new Handler();
    // bluetooth device list
    public HashMap<String, EscoPart> mBeacons = new HashMap<String, EscoPart>();

    private static final int SCAN_TIME = 1250;
    private static final int STOP_SCAN_TIME = 0;

    //public RangeAlert mRangeAlert = new RangeAlert();


    public BluetoothLEScanner(Activity activity) {
        mActivity = activity;
                /*
         * Bluetooth in Android 4.3 is accessed via the BluetoothManager, rather than
         * the old static BluetoothAdapter.getInstance()
         */
        BluetoothManager manager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

                /*
         * A progress dialog will be needed while the connection process is
         * taking place
         */
        mProgress = new ProgressDialog(mActivity);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);
    }

    public boolean check() {
               /*
         * We need to enforce that Bluetooth is first enabled, and take the
         * user to settings to enable it if they have not done so.
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivity(enableBtIntent);
            return false;
        }

        /*
         * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
         * from installing on these devices, but this will allow test devices or other
         * sideloads to report whether or not the feature exists.
         */
        if (!mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mActivity, "No LE Support.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void stop() {
        //Cancel any scans in progress
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        mBluetoothAdapter.stopLeScan(this);

        //Disconnect from any active tag connection
        mBeacons.clear();
    }

    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };
    private Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            startScan();
        }
    };

    public void startScan() {
        mBluetoothAdapter.startLeScan(this);
    //    mActivity.setProgressBarIndeterminateVisibility(true);

        mHandler.postDelayed(mStopRunnable, SCAN_TIME);
    }

    public void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
    //    mActivity.setProgressBarIndeterminateVisibility(false);

        if(mListener != null)
            mListener.onScanned();

        // rescan
        mHandler.postDelayed(mStartRunnable, STOP_SCAN_TIME);
    }

    /* BluetoothAdapter.LeScanCallback */

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i("BluetoothLEScanner", "New LE Device: " + device.getName() + " @ " + rssi);

        final int DATA_ID = 65535;

        /*
         * We are looking for SensorTag devices only, so validate the name
         * that each device reports before adding it to our collection
         */

        // process bluetooth advertisement data
        ScanRecord record = ScanRecord.parseFromBytes(scanRecord);
        Log.v("Device Info", record.toString());
        EscoPart part = null;
        // get or setup part and update values
        if(mBeacons.containsKey(device.getAddress()))
            part = mBeacons.get(device.getAddress());
        else
        {
            // make sure device can be scanned successfully without any errors with  specific data
            if(record.getDeviceName() == null ||
                    record.getManufacturerSpecificData() == null ||
                    record.getManufacturerSpecificData(DATA_ID) == null)
                return;

            // add new device to list
            part = new EscoPart();
            part.setName(record.getDeviceName());
            part.address = device.getAddress();
            mBeacons.put(part.address, part);

            // TODO
            //mRangeAlert.add(part.address, part.acceleration);
        }
        part.rssi = rssi;

        try {
            // get bluetooth data associated with a specific id
            byte data[] = record.getManufacturerSpecificData(DATA_ID);
            if(data == null) {
                // could not retrieve data from id
                throw new Exception();
            }

            // same as if statement just nicer structure

            switch(data.length) {
                case 13:
                    part.temperature = (data[0] & 0xff) - 60;
                    part.battery = data[1];
                    part.counter = data[8];
                    Log.d("RCD","Beacon Counter: " + part.counter);
                    part.maxAcceleration = getMaxAcceleration(data, 9);
                    Log.d("RCD","Max A = " + part.maxAcceleration);

                    break;
                case 8: // format [temp, batt, x, x, y, y, z, z]
                    // new sensor
                    part.temperature = (data[0] & 0xff) - 60;
                    part.battery = data[1];
                    break;
                case 3: // format [batt, 0, temp]
                    // old sensor
                    part.temperature = (float)(data[2] & 0xff) / 10.0f;
                    part.battery = data[0];
                default:
                    return;
            }

            try {
                // accel
                Point3D acceleration = getAcceleration(data, 2);
                part.acceleration.x = acceleration.x;
                part.acceleration.y = acceleration.y;
                part.acceleration.z = acceleration.z;
                if(part.getName().equals("ESCO#TEST0::(TEST0)")){
                    Log.d("RCD","Live A = " + part.acceleration.x + ", " + part.acceleration.y + ", " + part.acceleration.z);
                }
            } catch(Exception ex) {
                // could not get sensor data
                throw new Exception();
            }
        } catch(Exception ex) {
            // TODO reset data when a error occured
            part.battery = 0;
            part.temperature = 0;

            part.acceleration.x = 0;
            part.acceleration.y = 0;
            part.acceleration.z = 0;
        }

        LogData(part);
    }

    private static int toUnsignedByte(byte value) {
        return (value & 0xff);
    }

    private static Point3D getAcceleration(byte[] data, int offset) {
        final float SCALE = (float) 4096.0;

        int x = (data[offset + 1] << 8) + data[offset + 0];
        int y = (data[offset + 3] << 8) + data[offset + 2];
        int z = (data[offset + 5] << 8) + data[offset + 4];

        // scale coords
        return new Point3D((x / SCALE) * -1, y / SCALE, (z / SCALE) * -1);
    }

    private static float getMaxAcceleration(byte[]data, int offset) {
        final float SCALE = (float) 4096.0;

        int x = (data[offset + 1] << 8) + data[offset + 0];

        // scale
        return (x / SCALE);
    }

    public void setListener(ScanListener listener) {
        mListener = listener;
    }

    // date/time log file variables
    String mCurrentDate = DateTime.getDateStamp();
    int increment = 1;
    String oldName;

    // log file variables
    String filename = "ESCO-Data-new-rcd-2";

    void LogData(EscoPart part) {

        if(mBeacons.size() == 0) return;

        if(!mCurrentDate.equals(DateTime.getDateStamp())) {
            mCurrentDate = DateTime.getDateStamp();
            filename = oldName + "_"  + increment;
            increment++;
        }

        //Log row layout
        StringBuilder builder = new StringBuilder();
        // device information
        builder.append(part.address + ",");

        String string = String.format("%d,%.1f,%d,", part.battery, part.temperature, part.rssi);
        builder.append(string);
        // sensor information
        string = String.format("%.1f,%.1f,%.1f,", part.acceleration.x, part.acceleration.y, part.acceleration.z);
        builder.append(string);
        if(part.counter!=999 ){
            string = String.valueOf(part.counter);
            builder.append(string);
        }
        Log.d("RCD",builder.toString());
        //Logger.v(filename, builder.toString());
    }
}



