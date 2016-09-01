package com.escocorp.detectionDemo.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import com.escocorp.detectionDemo.fragments.LossAlertFragment;
import com.escocorp.detectionDemo.fragments.PartDetailFragment;
import com.escocorp.detectionDemo.models.Bucket;
import com.escocorp.detectionDemo.models.BucketConfig;
import com.escocorp.detectionDemo.models.DemoPart;
import com.escocorp.detectionDemo.models.EscoPart;
import com.escocorp.detectionDemo.models.IBucketConfig;
import com.escocorp.detectionDemo.models.IMachineFeature;
import com.escocorp.detectionDemo.models.Pod;
import com.escocorp.detectionDemo.models.Sensor;
import com.escocorp.detectionDemo.models.Shroud;
import com.escocorp.detectionDemo.models.Tooth;
import com.escocorp.detectionDemo.models.WingShroud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DetectionActivity extends AppCompatActivity implements IPairingsListenerActivity{

    HalfBucketLayout shovelLayout;
    ArrayList<EscoPart> mParts;

    private HashMap<String, Sensor> map;
    protected IconSpinnerProgressDialog progressDialog;
    private MachineFeatureAdapter mMachineFeatureAdapter;
    private PairingsController mPairingsController;
    private ImageView led;

    public static final int MAX_SCAN_CYCLES = 100;
    public int numCycles;

    PartDetailFragment activeFragment;
    private PartDetailViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private ViewPager.OnPageChangeListener listener;

    private DemoPart demoPart;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String SAVED_STATE = "saved_State";

    public static final String ACTION_BLUETOOTH_SVC_BOUND = "com.escocorp.ACTION_BLUETOOTH_SVC_BOUND";

    PartDetailFragment mFragment;
    //InventorySummaryFragment mSummaryFragment;

    protected BluetoothLeService mBluetoothLeService = null;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            final BluetoothLeService.BluetoothQueueServiceLocalBinder binder =
                    (BluetoothLeService.BluetoothQueueServiceLocalBinder) service;
            mBluetoothLeService = binder.getService();
            sendBroadcast(new Intent(DetectionActivity.ACTION_BLUETOOTH_SVC_BOUND));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mBlueToothServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DetectionActivity.ACTION_BLUETOOTH_SVC_BOUND.equals(action)) {
                //Toast.makeText(getApplicationContext(),"SERVICE BOUND",Toast.LENGTH_SHORT).show();
                beginScanning();
            }
        }
    };

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
                if(map.size()==0 || getSupportFragmentManager().findFragmentByTag("LOSS_ALERT_FRAGMENT")!=null){
                    return;
                }
                Sensor lostSensor = map.get(name);
                alertLoss(lostSensor);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_part_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());


        map = new HashMap<>();
        numCycles = 0;

        mParts = new ArrayList<EscoPart>();
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

        listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //not used
            }

            @Override
            public void onPageSelected(int position) {
                mPairingsController.setDeviceState(PartData.macAddressArray[position],BluetoothLeService.STATE_VIEWING);
                mMachineFeatureAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //not used
            }
        };

        mPager.addOnPageChangeListener(listener);

        led = (ImageView) findViewById(R.id.led);

        shovelLayout  = (HalfBucketLayout) findViewById(R.id.shovelLayout);

        mPairingsController = new PairingsController(this);
        mMachineFeatureAdapter = new MachineFeatureAdapter(this, mPairingsController);
        initBucketModel(new BucketConfig(3, 2, 2, 0));
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

        if(position!=mPager.getCurrentItem()){
            mPairingsController.setDeviceState(PartData.macAddressArray[mPager.getCurrentItem()],BluetoothLeService.STATE_NORMAL);
            mMachineFeatureAdapter.notifyDataSetChanged();
        }

        mPager.setCurrentItem(position);

        /*demoPart = new DemoPart(position);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.pop_enter,R.anim.exit);

        activeFragment = new PartDetailFragment();
        Bundle args = new Bundle();
        args.putInt(PartDetailFragment.ARG_ITEM_SELECTED, position);
        activeFragment.setArguments(args);
        ft.replace(R.id.part_detail_container, activeFragment, "DETAIL_FRAG")
                .commitAllowingStateLoss();*/
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

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            PartDetailFragment fragment = new PartDetailFragment();
            Bundle args = new Bundle();
            args.putInt(PartDetailFragment.ARG_ITEM_SELECTED, position);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return PartData.getNumParts();
        }
    }

    public void reset(){
        //Toast.makeText(this,"Reset", Toast.LENGTH_SHORT).show();
        stopScanning();
        removeFragment();
        map.clear();
        initializePairingModelForDemo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mDeviceScanReceiver);
        unregisterReceiver(mBlueToothServiceReceiver);

        unbindService(mServiceConnection);
    }

    public void removeFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.alert,R.anim.pop_exit);

        Fragment lossFragment = getSupportFragmentManager().findFragmentByTag("LOSS_ALERT_FRAGMENT");
        Fragment detailFragment = getSupportFragmentManager().findFragmentByTag("DETAIL_FRAG");

        if(!(lossFragment==null)) ft.remove(getSupportFragmentManager().findFragmentByTag("LOSS_ALERT_FRAGMENT"));
        if(!(detailFragment==null)) ft.remove(getSupportFragmentManager().findFragmentByTag("DETAIL_FRAG"));
        ft.commit();

        beginScanning();


    }
    private void beginScanning() {
        led.setImageResource(R.drawable.green_led);
        mBluetoothLeService.scanForDevices(true);
    }

    private void stopScanning(){
        led.setImageResource(R.drawable.red_led);
        mBluetoothLeService.scanForDevices(false);
    }

    @Override
    public void connectToDevice(Bucket pairingsMap, String bleAddress) {

    }

    private void initializePairingModelForDemo(){

        //Bucket pairingModel = mPairingsController.getPairingModel();
        int totalPositions = PartData.deviceNameArray.length;
        for(int position = 0; position < totalPositions; position++){
            Sensor device = new Sensor(PartData.deviceNameArray[position]);
            device.setMacAddress(PartData.macAddressArray[position]);
            //IMachineFeature machineFeature = pairingModel.getFeatures().get(position);
            mPairingsController.assignPosition(device, position);
            mPairingsController.setDeviceState(device.getMacAddress(),BluetoothLeService.STATE_NORMAL);
        }

        mMachineFeatureAdapter.notifyDataSetChanged();

    }

    public void alertLoss(Sensor lostSensor){
        mPairingsController.setDeviceState(lostSensor.getMacAddress(),BluetoothLeService.STATE_LOSS_DETECTED);
        mMachineFeatureAdapter.notifyDataSetChanged();

        /*FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.alert,R.anim.pop_exit);

        LossAlertFragment fragment = new LossAlertFragment();
        Bundle args = new Bundle();
        args.putInt(LossAlertFragment.ARG_ITEM_DETECTED, 1);
        fragment.setArguments(args);
        ft.replace(R.id.part_detail_container, fragment, "LOSS_ALERT_FRAGMENT")
                .commitAllowingStateLoss();*/

        stopScanning();
        map.clear();

    }

    private final BroadcastReceiver mDeviceScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action){
                case DeviceScanCallback.DEVICE_SCAN_COMPLETE:
                    Log.d("BT","scan complete");
                    numCycles++;

                    if(numCycles < MAX_SCAN_CYCLES){
                        Log.d("BT","begin another scan");
                        beginScanning();
                    } else {
                        //cycle the Service Scan and callback
                        stopScanning();
                        numCycles = 0;
                        beginScanning();
                    }

                    break;
                case DeviceScanCallback.DEVICE_SCAN_RESULT:
                    int RSSI = intent.getIntExtra(DeviceScanCallback.RSSI,-50);
                    String deviceName = intent.getStringExtra("name");
                    ScanResult result = intent.getParcelableExtra(DeviceScanCallback.EXTRA_SCAN_RESULT);
                    if (demoPart!=null && deviceName.equals(demoPart.getDeviceName())) {
                        activeFragment.addChartDataPoint(RSSI);
                        Log.d("RCD1","match: " + deviceName);
                    }

                    Sensor sensor = (Sensor)intent.getParcelableExtra(DeviceScanCallback.EXTRA_DEVICE);
                    sensor.updateSensor(result,context);
                    if(!map.containsKey(deviceName)){
                        map.put(deviceName,sensor);
                    } else {
                        Sensor sensorToModify = map.get(deviceName);
                        sensorToModify.updateSensor(result,context);
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
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
}
