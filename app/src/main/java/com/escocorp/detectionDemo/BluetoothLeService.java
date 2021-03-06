package com.escocorp.detectionDemo;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import com.escocorp.detectionDemo.bluetooth.BLECommand;
import com.escocorp.detectionDemo.database.PartData;
import com.escocorp.detectionDemo.models.Point3D;
import com.escocorp.detectionDemo.models.ScanRecord;
import com.escocorp.detectionDemo.models.Sensor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

// A service that interacts with the BLE device via the Android BLE API.
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    public static final int SCAN_LENGTH = 125;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_LOSS_DETECTED = 1;
    public static final int STATE_NORMAL = 2;
    public static final int STATE_VIEWING = 3;
    public static final int STATE_FAILED_CONNECTION = 3;

    public final static String EXTRA_VALUE = "com.rivetry.bluetooth.EXTRA_VALUE";

    public final static String ACTION_GATT_CONNECTED =
            "com.rivetry.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.rivetry.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_ERROR_CONNECTING =
            "com.rivetry.bluetooth.le.ACTION_GATT_ERROR";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.rivetry.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.rivetry.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.rivetry.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_CHARACTERISTIC_READ =
            "com.rivetry.bluetooth.le.ACTION_CHARACTERISTIC_READ";

    Handler mHandler = new Handler();
    DeviceScanCallback mScanCallback;
    private ScanSettings scanSettings;
    private List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
    private ArrayList<String> macAddressFilterList = new ArrayList<>();

    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;

    private ConcurrentHashMap<String, BluetoothGatt> mGatts;
    private final LinkedList<BLECommand> mCommandQueue = new LinkedList<BLECommand>();
    //Command Operation executor - will only run one at a time
    Executor mCommandExecutor = Executors.newSingleThreadExecutor();
    //Semaphore lock to coordinate command executions, to ensure only one is
    //currently started and waiting on a response.
    Semaphore mCommandLock = new Semaphore(1,true);

    private final IBinder mBluetoothQueueServiceLocalBinder = new BluetoothQueueServiceLocalBinder();
    private int mConnectionState;

    //The main callback to handle bluetooth gatt client notifications
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Context context = getApplicationContext();
            Log.d("RCD","Connection State Change Detected");
            super.onConnectionStateChange(gatt, status, newState);
            final String address = gatt.getDevice().getAddress();
            if (status == 133){//undocumented android gatt error !!!
                Log.d("RCD","Undocumented GATT error");
                mConnectionState = STATE_DISCONNECTED;
                sendBroadcast(ACTION_GATT_DISCONNECTED, address, ACTION_GATT_ERROR_CONNECTING);
                dequeueCommand();
                close(address);
                Log.e(TAG, "Error connecting to BLE device");
            }else
            if(newState == BluetoothGatt.STATE_CONNECTED){
                Log.d("RCD","BluetoothGatt.STATE_NORMAL");
                mConnectionState = STATE_NORMAL;
                if(status == BluetoothGatt.GATT_SUCCESS) {
                    mGatts.put(address, gatt);
                    sendBroadcast(ACTION_GATT_CONNECTED, address);
                    dequeueCommand();
                    close(address);
                }else{
                    close(address);
                }
            } else if(newState == BluetoothGatt.STATE_DISCONNECTED){
                Log.d("RCD","BluetoothGatt.STATE_DISCONNECTED");
                mConnectionState = STATE_DISCONNECTED;
                sendBroadcast(ACTION_GATT_DISCONNECTED, address);
                close(address);
            }
            else{
                Log.d("RCD","ELSE case");
                close(address);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            final String address = gatt.getDevice().getAddress();
            sendBroadcast(ACTION_GATT_CONNECTED, address);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if(characteristic.getUuid().equals(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG)){
                sendBroadcast(ACTION_CHARACTERISTIC_READ,characteristic.getStringValue(0));
                dequeueCommand();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }
    };
    private int numCycles;

    @Override
    public IBinder onBind(Intent intent) {
        initialize();
        return mBluetoothQueueServiceLocalBinder; //Locally-bound only
    }

    public class BluetoothQueueServiceLocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    private boolean initialize(){
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        mGatts = new ConcurrentHashMap<>();

        numCycles = 0;

        //clearConnections();

        //init scan filter
        ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(
                PartData.initialMacAddressArray[0])
                .build();
        scanFilters.add(filter);

/*        filter = new ScanFilter.Builder().setDeviceAddress(
                PartData.initialMacAddressArray[1])
                .build();
        scanFilters.add(filter);
        filter = new ScanFilter.Builder().setDeviceAddress(
                PartData.initialMacAddressArray[2])
                .build();
        scanFilters.add(filter);
        filter = new ScanFilter.Builder().setDeviceAddress(
                PartData.initialMacAddressArray[3])
                .build();
        scanFilters.add(filter);
        filter = new ScanFilter.Builder().setDeviceAddress(
                PartData.initialMacAddressArray[4])
                .build();
        scanFilters.add(filter);*/

        return true;
    }

/*    //used for testing, clear stray connections that were not disconnected properly
    public void clearConnections(){
        mBluetoothAdapter.disable();
        mBluetoothAdapter.enable();
    }*/

    @Override
    public void onDestroy(){
        for (String address:mGatts.keySet()){
            disconnect(address);
        }
        super.onDestroy();
    }


    public void setScanFilter(ArrayList<String> list){
        this.macAddressFilterList = list;
    }
    //Function to scan for advertising BLE devices to connect to
    public boolean scanForDevices(boolean on){


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(on){
                //Toast.makeText(getApplicationContext(),"start scan",Toast.LENGTH_SHORT).show();

                /*//for Debugging
                if(mBluetoothAdapter.getBluetoothLeScanner()==null){
                    Log.d("RCD","can't get the scanner");
                    Log.d("RCD","enabled? " + String.valueOf(mBluetoothAdapter.isEnabled()));
                    if(!mBluetoothAdapter.isEnabled()) mBluetoothAdapter.enable();
                    return startScanUsingOldApi(on);

                }*/

                //Turn scanning on
                if(!isScanningDevices()){
                    //Log.d("RCD","scanning started");

                    ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
                    scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                    scanSettings = scanSettingsBuilder.build();

                    mScanCallback = new DeviceScanCallback(this);
                    //Look for Devices with a standard DEVICE_INFORMATION service

                    //in case bluetooth is off, turn it on
                    if(!mBluetoothAdapter.isEnabled()) mBluetoothAdapter.enable();
                    //mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
                    mBluetoothAdapter.getBluetoothLeScanner().startScan(scanFilters, scanSettings, mScanCallback);
                    //Log.d("RCD","scanning for " + String.valueOf(SCAN_LENGTH)+ " milliseconds");
                    //Ensure we won't Scan forever (save battery)
                    numCycles ++;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(isScanningDevices()){
                                scanForDevices(false);
                                //Log.d("RCD","scanning stopped");
                                //let listeners know the scan is done
                                sendBroadcast(DeviceScanCallback.DEVICE_SCAN_COMPLETE);
                            } else {
                                Log.d("BT TEST","scan again");
                                //stop previous scan
                                if(mScanCallback!=null){
                                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback); //stopLeScan(mScanCallback);
                                    mScanCallback = null;
                                }

                                scanForDevices(true);
                            }

                        }
                    }, SCAN_LENGTH);

                }
            }else{
                //Turn scanning off
                //Toast.makeText(getApplicationContext(),"stop scan",Toast.LENGTH_SHORT).show();
                if(isScanningDevices() && mBluetoothAdapter.getBluetoothLeScanner()!=null){
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback); //stopLeScan(mScanCallback);
                    mScanCallback = null;
                }
            }
            return true;

        } else {
            return startScanUsingOldApi(on);

        }




    }

    public BluetoothAdapter.LeScanCallback mScanCallBack = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if(!macAddressFilterList.contains(device.getAddress())) return;
                final Sensor sensor = new Sensor(device, scanRecord);
                String deviceName = sensor.getName();
                Point3D accel = Sensor.extractAccelFromScanRecord(scanRecord);
                String AccelerationString = "";
                if(accel!=null) {
                    AccelerationString = String.valueOf(accel.x) + ":" +
                            String.valueOf(accel.y) + ":" +
                            String.valueOf(accel.z);
                }

                //Send a broadcast to main part of the app to alert it to new found device
                final Intent broadcast = new Intent(DeviceScanCallback.DEVICE_SCAN_RESULT);
                broadcast.putExtra(DeviceScanCallback.EXTRA_ADDRESS, device.getAddress());
                broadcast.putExtra(DeviceScanCallback.EXTRA_DEVICE, sensor);
                broadcast.putExtra("Acceleration_String",AccelerationString);
                broadcast.putExtra("name",deviceName);
                Log.d("RACE THROW","startScanUsingOldApi: " + deviceName);
                broadcast.putExtra(DeviceScanCallback.RSSI,rssi);
                sendBroadcast(broadcast);

            }
        };

    public boolean startScanUsingOldApi(boolean on){
        if(on){

            if(!mBluetoothAdapter.isEnabled()) mBluetoothAdapter.enable();

            mBluetoothAdapter.startLeScan(mScanCallBack);
        } else {
            mBluetoothAdapter.stopLeScan(mScanCallBack);

        }

        return true;
    }

    public boolean isScanningDevices(){
        return mScanCallback != null;
    }

    public boolean connect(String address){
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        if (mGatts.containsKey(address)) {
            close(address);
        }

        final BLECommand command = new BLEConnectCommand(this, device);
        queueCommand(command);

        return true;
    }

    public void disconnect(String address){
        if (address == null) return;

        close(address);
    }

    public void close(String address) {
        BluetoothGatt gatt = mGatts.get(address);
        if(gatt != null) {
            gatt.getDevice().getBondState();
            gatt.disconnect();
            gatt.close();
            Log.d("RCD","Closing " + address);
        }
        mGatts.remove(address);
    }

    public boolean isGattConnected(String address){
        if (null==mGatts.get(address) || null==mGatts.get(address).getDevice()){
            return false;
        }
        return mGatts.get(address).getConnectionState(mGatts.get(address).getDevice())==BluetoothGatt.STATE_CONNECTED;
    }

    public void queueCommand(BLECommand command){
        synchronized (mCommandQueue) {
            mCommandQueue.push(command);  //Add to end of stack
            //Schedule a new runnable to process that command (one command at a time executed only)
            ExecuteCommandRunnable runnable = new ExecuteCommandRunnable(command);
            mCommandExecutor.execute(runnable);
        }
    }

    //Remove the current command from the queue, and release the lock
    //signalling the next queued command (if any) that it can start
    public void dequeueCommand(){
        mCommandQueue.pop();
        mCommandLock.release();
    }

    private void sendBroadcast(String intentAction, String address){
        final Intent intent = new Intent(intentAction);
        intent.putExtra(DeviceScanCallback.EXTRA_ADDRESS, address);
        sendBroadcast(intent);
    }

    private void sendBroadcast(String intentAction, String address, String errMsg){
        final Intent intent = new Intent(intentAction);
        intent.putExtra(DeviceScanCallback.EXTRA_ADDRESS, address);
        intent.putExtra(DeviceScanCallback.EXTRA_ERROR_MSG, errMsg);
        sendBroadcast(intent);
    }

    private void sendBroadcast(String intentAction){
        sendBroadcast(new Intent(intentAction));
    }


//    final Intent intent = new Intent(DeviceConfigurationFragment.CONFIGURATION_UPDATE_INTENT);
//    intent.putExtra(BLEDeviceListAdapter.DEVICE_DATA, config);//CollectionsManager.toBundle(config));
//    LocalBroadcastManager.getInstance(baseActivity).sendBroadcast(intent);


    class BLEConnectCommand extends BLECommand {

        private final BluetoothDevice bluetoothDevice;
        private final Context context;

        public BLEConnectCommand(Context context, BluetoothDevice bluetoothDevice) {
            super();
            this.context = context;
            this.bluetoothDevice = bluetoothDevice;
        }

        public void execute(){
            mConnectionState = STATE_LOSS_DETECTED;
            bluetoothDevice.connectGatt(context, false, mGattCallback);
            Log.d(TAG, "Trying to create a new connection.");
        }
    }

    //Runnable to execute a command from the queue
    class ExecuteCommandRunnable implements Runnable{

        BLECommand mCommand;

        public ExecuteCommandRunnable(BLECommand command) {
            mCommand = command;
        }

        @Override
        public void run() {
            //Acquire semaphore lock to ensure no other operations can run until this one completed
            mCommandLock.acquireUninterruptibly();
            //Tell the command to start itself.
            mCommand.execute();
        }
    }

}

