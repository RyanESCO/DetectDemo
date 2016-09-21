package com.escocorp.detectionDemo.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.escocorp.detectionDemo.BluetoothLeService;
import com.escocorp.detectionDemo.DeviceScanCallback;
import com.escocorp.detectionDemo.IPairingsListenerActivity;
import com.escocorp.detectionDemo.R;
import com.escocorp.detectionDemo.adapters.MachineFeatureAdapter;
import com.escocorp.detectionDemo.adapters.PairingsController;
import com.escocorp.detectionDemo.custom.HalfBucketLayout;
import com.escocorp.detectionDemo.custom.IconSpinnerProgressDialog;
import com.escocorp.detectionDemo.custom.PartDetailViewPager;
import com.escocorp.detectionDemo.database.PartData;
import com.escocorp.detectionDemo.fragments.PartDetailFragment;
import com.escocorp.detectionDemo.fragments.SensorDialogFragment;
import com.escocorp.detectionDemo.models.Bucket;
import com.escocorp.detectionDemo.models.BucketConfig;
import com.escocorp.detectionDemo.models.IBucketConfig;
import com.escocorp.detectionDemo.models.IMachineFeature;
import com.escocorp.detectionDemo.models.Pod;
import com.escocorp.detectionDemo.models.Point3D;
import com.escocorp.detectionDemo.models.Sensor;
import com.escocorp.detectionDemo.models.Shroud;
import com.escocorp.detectionDemo.models.Tooth;
import com.escocorp.detectionDemo.models.WingShroud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DetectionActivity extends AppCompatActivity implements IPairingsListenerActivity, SensorDialogFragment.SensorDialogFragmentListener{

    HalfBucketLayout shovelLayout;
    private ImageView led;
    private TextView mResetButton;
    private ImageView mHiddenResetButton;
    private TextView mChronoToggle;
    private LinearLayout mChronoView;

    PartDetailFragment activeFragment;
    private PartDetailViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;

    //Debugging variables
    Chronometer c1, c2, c3, c4, c0;

    Chronometer [] chronometers = new Chronometer[5];

    private HashMap<String, Sensor> map;
    protected IconSpinnerProgressDialog progressDialog;
    private MachineFeatureAdapter mMachineFeatureAdapter;
    private PairingsController mPairingsController;

    Preference.OnPreferenceChangeListener mListener;

    private String[] deviceNameArray;
    private String[] macAddressArray;
    //ArrayList<EscoPart> mParts;
    private HashMap<String, String> allDevicesMap;  //used to populate full BT list for BT chooser dialog

    public static final int MAX_SCAN_CYCLES = 5;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String SAVED_STATE = "saved_State";
    public static final String ACTION_BLUETOOTH_SVC_BOUND = "com.escocorp.ACTION_BLUETOOTH_SVC_BOUND";
    private int mScrollState = ViewPager.SCROLL_STATE_IDLE;
    private boolean mLossDetected = false;
    public int numCycles;

    private ViewPager.OnPageChangeListener listener;

    boolean mBound = false;

    PartDetailFragment mFragment;

    protected BluetoothLeService mBluetoothLeService = null;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            final BluetoothLeService.BluetoothQueueServiceLocalBinder binder =
                    (BluetoothLeService.BluetoothQueueServiceLocalBinder) service;
            mBluetoothLeService = binder.getService();
            sendBroadcast(new Intent(DetectionActivity.ACTION_BLUETOOTH_SVC_BOUND));
            Log.d("RCD","Service Bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            mBound = false;
        }
    };

    private final BroadcastReceiver mBlueToothServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DetectionActivity.ACTION_BLUETOOTH_SVC_BOUND.equals(action)) {
                //Toast.makeText(getApplicationContext(),"SERVICE BOUND",Toast.LENGTH_SHORT).show();
                mBound = true;
                resetPairingModel();  //reset pairing model and send current MAP to service
                beginScanning();
            }
        }
    };

    public HashMap<String, String> getAllDevicesMap(){
        return allDevicesMap;
    }

    private final BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("DISPLAY_PART_DATA_INTENT")){

                int position = intent.getIntExtra("position",0);
                String name = intent.getStringExtra("name");

                //Toast.makeText(context,"feature clicked: " + name + " at position " + String.valueOf(position),Toast.LENGTH_SHORT).show();

                onPartSelected(position);


            }

            if(intent.getAction().equals(DeviceScanCallback.SIMULATED_LOSS_DETECTED)){
                String name = intent.getStringExtra("name");
                /*String xValues = intent.getStringExtra("x_accel");
                String yValues = intent.getStringExtra("y_accel");
                String zValues = intent.getStringExtra("z_accel");*/
                if(map.size()==0 || getSupportFragmentManager().findFragmentByTag("LOSS_ALERT_FRAGMENT")!=null){
                    return;
                }
                Sensor lostSensor = map.get(name);
                if(null!=lostSensor){

                    Log.d("RCD",lostSensor.getName() + ": DETECTED LOST");
                    alertLoss(lostSensor);
                }

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detection_demo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        PartData.initializeSensorData(this);
        initializeDebugging();

        mListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("RCD","PREF CHANGED");
                return false;
            }
        };

        mResetButton = (TextView) findViewById(R.id.textViewReset);
        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

        mHiddenResetButton = (ImageView) findViewById(R.id.logo);
        mHiddenResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"RESETTING",Toast.LENGTH_SHORT).show();
                reset();
            }
        });

        mChronoToggle = (TextView) findViewById(R.id.textViewMonitoringStatus);
        mChronoView = (LinearLayout) findViewById(R.id.chronoLayout);

        mChronoToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mChronoView.getVisibility()==View.INVISIBLE){
                    mChronoView.setVisibility(View.VISIBLE);
                } else {
                    mChronoView.setVisibility(View.INVISIBLE);
                }


            }
        });

        map = new HashMap<>();
        allDevicesMap = new HashMap<>();
        numCycles = 0;

        //mParts = new ArrayList<EscoPart>();
        //mPartsAdapter = new SimpleItemRecyclerViewAdapter(mParts);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(SAVED_STATE)) {
            //(savedInstanceState.getParcelableArrayList(SAVED_STATE));
        }

        mPager = (PartDetailViewPager) findViewById(R.id.viewPager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setScrollDurationFactor(5);
        mPager.setOffscreenPageLimit(7);
        mPager.setPageMargin(5);

        listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                changeViewingPart(position);
                activeFragment = (PartDetailFragment) mPagerAdapter.getItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state==ViewPager.SCROLL_STATE_DRAGGING){
                    changeViewingPart(-1);
                    mScrollState = state;
                }
                //not used  changePartState(position, BluetoothLeService.STATE_NORMAL);
            }
        };

        mPager.addOnPageChangeListener(listener);

        led = (ImageView) findViewById(R.id.led);

        shovelLayout  = (HalfBucketLayout) findViewById(R.id.shovelLayout);

        mPairingsController = new PairingsController(this);
        mMachineFeatureAdapter = new MachineFeatureAdapter(this, mPairingsController);
        initBucketModel(new BucketConfig(3, 2, 2, 0));

        deviceNameArray = new String[PartData.getNumParts()];
        macAddressArray = new String[PartData.getNumParts()];

        initializePairingModelForDemo();
        shovelLayout.setAdapter(mMachineFeatureAdapter);
        mMachineFeatureAdapter.notifyDataSetChanged();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @TargetApi(Build.VERSION_CODES.M)
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }

                });
                builder.show();
            }
        }

        progressDialog = new IconSpinnerProgressDialog(this);
        progressDialog.setIndeterminate(true);

    }

    public BluetoothLeService getBLEService(){
        return mBluetoothLeService;
    }

    public void changePartState(int position, int state){
        if(position==-1){
            return;
        }
        mPairingsController.setDeviceState(macAddressArray[position],state);
        mMachineFeatureAdapter.notifyDataSetChanged();

    }

    public void changeViewingPart(int position){
        for(int i = 0 ; i < PartData.getNumParts();i++){
            changePartState(i,BluetoothLeService.STATE_NORMAL);
        }
        if(position!=-1){
            changePartState(position,BluetoothLeService.STATE_VIEWING);
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
       /* if(!bluetoothLEScanner.check())
            finish();*/

        IntentFilter localBroadcastFilter = new IntentFilter();
        localBroadcastFilter.addAction("DISPLAY_PART_DATA_INTENT");
        localBroadcastFilter.addAction(DeviceScanCallback.SIMULATED_LOSS_DETECTED);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mLocalBroadcastReceiver,localBroadcastFilter);

        final IntentFilter btIntentFilter = new IntentFilter();
        btIntentFilter.addAction(DeviceScanCallback.DEVICE_SCAN_RESULT);
        btIntentFilter.addAction(DeviceScanCallback.DEVICE_SCAN_COMPLETE);
        registerReceiver(mDeviceScanReceiver, btIntentFilter);


        final IntentFilter serviceBoundIntentFilter = new IntentFilter();
        serviceBoundIntentFilter.addAction(DetectionActivity.ACTION_BLUETOOTH_SVC_BOUND);

        registerReceiver(mBlueToothServiceReceiver, serviceBoundIntentFilter);

        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    public void onPartSelected(int position){
        if(mLossDetected){
            return;
        }
        mPager.setVisibility(View.VISIBLE);

        if(position!=mPager.getCurrentItem()){
            changePartState(mPager.getCurrentItem(),BluetoothLeService.STATE_NORMAL);
        }

        mPager.setCurrentItem(position);
        changePartState(position,BluetoothLeService.STATE_VIEWING);
        if (activeFragment != null) activeFragment.resetDisplay();

    }
    private void initBucketModel(IBucketConfig config){

        final Bucket bucket = new Bucket();

        bucket.setId(UUID.randomUUID().toString());
        bucket.setName("fifteenTooth");
        bucket.setOrientationType(0);
        bucket.setDepth(0);
        bucket.setHeight(0);
        bucket.setWidth(0);

        final ArrayList<IMachineFeature> features = new ArrayList<>();

        Tooth tooth;
        Shroud shroud;
        WingShroud wShroud;
        Pod pod;
        String id;
        for (int idx=0;idx<config.getTeeth();idx++){
            if(idx>0){
                id = UUID.randomUUID().toString();
                shroud = new Shroud(idx);
                shroud.setId(String.valueOf(id));
                features.add(shroud);
            }
            id = UUID.randomUUID().toString();
            tooth = new Tooth(idx);
            tooth.setId(String.valueOf(id));
            features.add(tooth);
        }
        final int numberWShrouds = config.getWingShrouds();
        for (int idx=0;idx<numberWShrouds;idx++){
            id = UUID.randomUUID().toString();
            int pos = features.size();
            wShroud = new WingShroud(idx);
            wShroud.setId(String.valueOf(id));
            features.add( wShroud);
        }

        for (int idx=0;idx<config.getBucketMonitors();idx++){
            id = UUID.randomUUID().toString();
            //put the bucketMonitors last in the feature list
            pod = new Pod(idx);
            pod.setId(String.valueOf(id));
            features.add(pod);
        }

        bucket.setFeatures(features);

        mPairingsController.setPairingModel(bucket);
        mMachineFeatureAdapter.notifyDataSetChanged();
        //mLeDeviceListAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add the "scan" option to the menu
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_reset:
                reset();
                return true;
            /*case R.id.action_fake_loss:
                mPairingsController.setDeviceState("00:07:80:15:37:B0",BluetoothLeService.STATE_NORMAL);
                mMachineFeatureAdapter.notifyDataSetChanged();*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDialogPositiveClick(SensorDialogFragment dialog, String newDeviceName, String newMacAddress) {

        String oldDeviceName = dialog.getOldDeviceName();
        int selectedPosition = getIndexfromArray(oldDeviceName, deviceNameArray);

        if(map.containsKey(newDeviceName)){

            //check if the same position was pressed

            int swapPosition = getIndexfromArray(newDeviceName, deviceNameArray);

            if (selectedPosition == swapPosition){
                Toast.makeText(this, "That sensor is already in that position",Toast.LENGTH_SHORT).show();
                return;
            }
            String macAddressInSelectedPosition = macAddressArray[selectedPosition];

            //swap places in macAddressArray and DeviceNameArray
            deviceNameArray[swapPosition] = oldDeviceName;
            macAddressArray[swapPosition] = macAddressInSelectedPosition;

            deviceNameArray[selectedPosition] = newDeviceName;
            macAddressArray[selectedPosition] = newMacAddress;

            PartDetailFragment fragment1 = mPagerAdapter.getItemByName(oldDeviceName);
            PartDetailFragment fragment2 = mPagerAdapter.getItemByName(newDeviceName);

            fragment1.changeAssignedSensor(newDeviceName);
            fragment2.changeAssignedSensor(oldDeviceName);

            Sensor device1 = new Sensor(newDeviceName);
            device1.setMacAddress(newMacAddress);

            Sensor device2 = new Sensor(oldDeviceName);
            device2.setMacAddress(macAddressInSelectedPosition);

            mPairingsController.assignPosition(device1, selectedPosition);
            mPairingsController.assignPosition(device2, swapPosition);

        } else {

            map.remove(oldDeviceName);

            //swap out info in macAddressArray and DeviceNameArray

            deviceNameArray[selectedPosition] = newDeviceName;
            macAddressArray[selectedPosition] = newMacAddress;

            Sensor device = new Sensor(newDeviceName);
            device.setMacAddress(newMacAddress);
            map.put(newDeviceName,device);

            mPairingsController.assignPosition(device, selectedPosition);

            PartDetailFragment fragment = mPagerAdapter.getItemByName(oldDeviceName);
            fragment.changeAssignedSensor(newDeviceName);
        }

        mPagerAdapter.notifyDataSetChanged();
        activeFragment = (PartDetailFragment) mPagerAdapter.getItem(selectedPosition);

    }

    private int getIndexfromArray(String input, String[] stringArray){
        for (int i=0; i < stringArray.length;i++){
            if(stringArray[i].equals(input)){
                return i;
            }

        }
        return -1;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private List<PartDetailFragment> fragmentList = new ArrayList<>();

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(fragmentList.size()<=position) {
                PartDetailFragment fragment = new PartDetailFragment();
                Bundle args = new Bundle();
                args.putInt(PartDetailFragment.ARG_ITEM_SELECTED, position);
                fragment.setArguments(args);
                fragmentList.add(position,fragment);
                return fragment;
            } else {
                return fragmentList.get(position);
            }
        }


        @Override
        public int getCount() {
            return PartData.getNumParts();
        }

        public PartDetailFragment getItemByName(String deviceName) {
            if(deviceName==null){
                return null;
            }
            for(int i =0; i < fragmentList.size();i++){
                if (deviceName.equals(fragmentList.get(i).getDeviceName())){
                    return fragmentList.get(i);
                }
            }
            return null;
        }
    }

    public void reset(){
        //to prevent multiple reset calls from starting duplicate scans
        if(!mLossDetected){
            return;
        }

        mLossDetected = false;
        stopScanning();
        if(activeFragment!=null) activeFragment.resetDisplay();
        resetPairingModel();
        beginScanning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScanning();
        unregisterReceiver(mDeviceScanReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadcastReceiver);
        unregisterReceiver(mBlueToothServiceReceiver);

        if(mBound) unbindService(mServiceConnection);
    }

    private void beginScanning() {
        if(mBound){
            led.setImageResource(R.drawable.green_led);
            mBluetoothLeService.scanForDevices(true);
        } else {

        }

    }

    private void stopScanning(){
        if(mBound){
            led.setImageResource(R.drawable.red_led);
            mBluetoothLeService.scanForDevices(false);
        }
    }

    @Override
    public void connectToDevice(Bucket pairingsMap, String bleAddress) {

    }

    private void initializePairingModelForDemo(){

        //Bucket pairingModel = mPairingsController.getPairingModel();
        int totalPositions = PartData.getNumParts();
        for(int position = 0; position < totalPositions; position++){
        //for(int position = 0; position < 1; position++){
            Sensor device = new Sensor(PartData.initialDeviceNameArray[position]);
            device.setMacAddress(PartData.initialMacAddressArray[position]);
            //IMachineFeature machineFeature = pairingModel.getFeatures().get(position);
            mPairingsController.assignPosition(device, position);
            mPairingsController.setDeviceState(device.getMacAddress(),BluetoothLeService.STATE_NORMAL);
            map.put(device.getName(),device);

            macAddressArray[position] = device.getMacAddress();
            deviceNameArray[position] = device.getName();

        }
        mMachineFeatureAdapter.notifyDataSetChanged();
    }

    private void resetPairingModel(){
        //Bucket pairingModel = mPairingsController.getPairingModel();
        map.clear();
        int totalPositions = PartData.getNumParts();
        ArrayList<String> macAddressList=  new ArrayList<>();
        for(int position = 0; position < totalPositions; position++){
            Sensor device = new Sensor(deviceNameArray[position]);
            device.setMacAddress(macAddressArray[position]);
            //IMachineFeature machineFeature = pairingModel.getFeatures().get(position);
            mPairingsController.assignPosition(device, position);
            mPairingsController.setDeviceState(device.getMacAddress(),BluetoothLeService.STATE_NORMAL);
            map.put(device.getName(),device);
            macAddressList.add(device.getMacAddress());
        }
        mMachineFeatureAdapter.notifyDataSetChanged();
        mBluetoothLeService.setScanFilter(macAddressList);

    }

    private void initializeDebugging(){
        c0 = (Chronometer) findViewById(R.id.chrono0);
        c0.start();
        c1 = (Chronometer) findViewById(R.id.chrono1);
        c1.start();
        c2 = (Chronometer) findViewById(R.id.chrono2);
        c2.start();
        c3 = (Chronometer) findViewById(R.id.chrono3);
        c3.start();
        c4 = (Chronometer) findViewById(R.id.chrono4);
        c4.start();

        chronometers = new Chronometer[]{c0, c1,c2,c3, c4};


    }

    public void alertLoss(Sensor lostSensor){
        Log.d("RCD","SEPARATION DETECTED");
        int position = getPositionFromMacAddress(lostSensor.getMacAddress());

        if(position >4){
            //don't alert wing shroud loss at MinEXPO
            return;
        }
        mLossDetected = true;
        mPager.setCurrentItem(getPositionFromMacAddress(lostSensor.getMacAddress()));
        stopScanning();
        resetPairingModel();
        changePartState(position,BluetoothLeService.STATE_LOSS_DETECTED);
        mPager.setVisibility(View.VISIBLE);
        mResetButton.setVisibility(View.VISIBLE);
        activeFragment = (PartDetailFragment) mPagerAdapter.getItem(position);
        activeFragment.alertLoss();

        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        tg.startTone(ToneGenerator.TONE_PROP_PROMPT);
        MediaPlayer player = MediaPlayer.create(this,R.raw.sep_detect);
        player.start();

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }

            }
        });
    }

    public int getPositionFromMacAddress(String macAddress){
        for(int i=0; i < PartData.getNumParts();i++){
            if(macAddress.equals(macAddressArray[i])){
                return i;
            }
        }
        return -1;
    }

    private final BroadcastReceiver mDeviceScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action){
                case DeviceScanCallback.DEVICE_SCAN_COMPLETE:
                    /*Log.d("BT","scan complete");
                    numCycles++;

                    if(numCycles < MAX_SCAN_CYCLES){
                        Log.d("BT","begin another scan");
                        beginScanning();
                    } else {
                        //cycle the Service Scan and callback
                        stopScanning();
                        numCycles = 0;
                        beginScanning();
                    }*/

                    break;
                case DeviceScanCallback.DEVICE_SCAN_RESULT:

                    if(mLossDetected){
                        return;
                    }
                    int RSSI = intent.getIntExtra(DeviceScanCallback.RSSI,-50);
                    String deviceName = intent.getStringExtra("name");
                    Log.d("RACE CATCH","Received: " + deviceName);

                    if(deviceName==null || !map.containsKey(deviceName)){
                        return;
                    }

                    ScanResult result = intent.getParcelableExtra(DeviceScanCallback.EXTRA_SCAN_RESULT);
                    PartDetailFragment fragment = mPagerAdapter.getItemByName(deviceName);
                    if(fragment!=null) fragment.addChartDataPoint(RSSI);

                    Sensor sensor = intent.getParcelableExtra(DeviceScanCallback.EXTRA_DEVICE);
                    Sensor sensorToModify = map.get(deviceName);

                    if(result!=null){
                        sensor.updateSensor(result,context);
                        sensorToModify.updateSensor(result,context);
                    } else {
                        String accelerationString = intent.getStringExtra("Acceleration_String");
                        if(accelerationString==null){
                            return;
                        }
                        String[] accelerationArray = accelerationString.split(":");
                        try{
                            Point3D acceleration = new Point3D(
                                    Double.parseDouble(accelerationArray[0]),
                                    Double.parseDouble(accelerationArray[1]),
                                    Double.parseDouble(accelerationArray[2]));

                            sensor.updateSensor(RSSI, acceleration, context);
                            sensorToModify.updateSensor(RSSI, acceleration, context);
                        } catch (NumberFormatException e){
                            e.printStackTrace();
                        }

                    }

                    if(!allDevicesMap.containsKey(deviceName)&&deviceName.startsWith("ESCO")){
                        allDevicesMap.put(deviceName,sensor.getMacAddress());
                    }

                    int position = getPositionFromMacAddress(sensor.getMacAddress());

                    if(position < 5 && position > -1){
                        long elapsedMillis = SystemClock.elapsedRealtime() - chronometers[position].getBase();
                        if(elapsedMillis>4500){
                            //Log.d("RCD","delay: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
                        }
                        chronometers[position].setBase(SystemClock.elapsedRealtime());
                        //Log.d("RCD","delay: " + String.valueOf(elapsedMillis) + "ms");

                    }

                    break;

                default:

                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLeService = null;
    }



}
