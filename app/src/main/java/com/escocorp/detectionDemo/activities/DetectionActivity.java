package com.escocorp.detectionDemo.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.escocorp.detectionDemo.BluetoothLEScanner;
import com.escocorp.detectionDemo.BluetoothLeService;
import com.escocorp.detectionDemo.DeviceScanCallback;
import com.escocorp.detectionDemo.IPairingsListenerActivity;
import com.escocorp.detectionDemo.R;
import com.escocorp.detectionDemo.ScanListener;
import com.escocorp.detectionDemo.adapters.MachineFeatureAdapter;
import com.escocorp.detectionDemo.adapters.PairingsController;
import com.escocorp.detectionDemo.custom.HalfBucketLayout;
import com.escocorp.detectionDemo.custom.IconSpinnerProgressDialog;
import com.escocorp.detectionDemo.fragments.LossAlertFragment;
import com.escocorp.detectionDemo.fragments.PartDetailFragment;
import com.escocorp.detectionDemo.models.Bucket;
import com.escocorp.detectionDemo.models.BucketConfig;
import com.escocorp.detectionDemo.models.EscoPart;
import com.escocorp.detectionDemo.models.IBucketConfig;
import com.escocorp.detectionDemo.models.IMachineFeature;
import com.escocorp.detectionDemo.models.Pod;
import com.escocorp.detectionDemo.models.Shroud;
import com.escocorp.detectionDemo.models.Tooth;
import com.escocorp.detectionDemo.models.WingShroud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DetectionActivity extends AppCompatActivity implements IPairingsListenerActivity{

    private boolean mTwoPane;
    private boolean isScanning = false;
    HalfBucketLayout shovelLayout;
    BluetoothLEScanner bluetoothLEScanner;
    ArrayList<EscoPart> mParts;
    //SimpleItemRecyclerViewAdapter mPartsAdapter;
    private HashMap<String, EscoPart> mBeacons;
    protected IconSpinnerProgressDialog progressDialog;
    private MachineFeatureAdapter mMachineFeatureAdapter;
    private PairingsController mPairingsController;

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

    private final BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("DISPLAY_PART_DATA_INTENT")){

                int position = intent.getIntExtra("position",0);
                String name = intent.getStringExtra("name");

                Toast.makeText(context,"feature clicked: " + name + " at position " + String.valueOf(position),Toast.LENGTH_SHORT).show();

                onPartSelected(position);


            }

            if(intent.getAction().equals(DeviceScanCallback.SIMULATED_LOSS_DETECTED)){
                alertLoss();

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

        mParts = new ArrayList<EscoPart>();
        //mPartsAdapter = new SimpleItemRecyclerViewAdapter(mParts);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(SAVED_STATE)) {
            //(savedInstanceState.getParcelableArrayList(SAVED_STATE));
        }

        View recyclerView = findViewById(R.id.part_list);
        assert recyclerView != null;
        //setupRecyclerView((RecyclerView) recyclerView);

        shovelLayout  = (HalfBucketLayout) findViewById(R.id.shovelLayout);

        mPairingsController = new PairingsController(this);
        mMachineFeatureAdapter = new MachineFeatureAdapter(this, mPairingsController);
        initBucketModel(new BucketConfig(3, 2, 2, 0));
        shovelLayout.setAdapter(mMachineFeatureAdapter);
        mMachineFeatureAdapter.notifyDataSetChanged();

        final Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

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
    }

    public void onPartSelected(int position){

        //alertLoss();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.pop_enter,R.anim.pop_exit);

        PartDetailFragment fragment = new PartDetailFragment();
        Bundle args = new Bundle();
        args.putInt(PartDetailFragment.ARG_ITEM_SELECTED, position);
        fragment.setArguments(args);
        ft.replace(R.id.part_detail_container, fragment, "DETAIL_FRAG")
                .commitAllowingStateLoss();
    }
    private void initBucketModel(IBucketConfig config){

        final Bucket bucket = new Bucket();

        bucket.setId(UUID.randomUUID().toString());
        bucket.setName("bucket1");
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
            //put the first set of wingshrouds first in the feature list
            if (idx<numberWShrouds/2){
                wShroud = new WingShroud(idx);
                wShroud.setId(String.valueOf(id));
                features.add(idx, wShroud);
            } else {
                //put the second set of wingshrouds last in the feature list
                int pos = features.size();
                wShroud = new WingShroud(idx);
                wShroud.setId(String.valueOf(id));
                features.add(pos, wShroud);
            }
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
                Toast.makeText(this,"Reset", Toast.LENGTH_SHORT).show();
                removeFragment();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void removeFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.alert,R.anim.pop_exit);

        Fragment lossFragment = getSupportFragmentManager().findFragmentByTag("LOSS_ALERT_FRAGMENT");
        Fragment detailFragment = getSupportFragmentManager().findFragmentByTag("DETAIL_FRAG");

        if(!(lossFragment==null)) ft.remove(getSupportFragmentManager().findFragmentByTag("LOSS_ALERT_FRAGMENT"));
        if(!(detailFragment==null)) ft.remove(getSupportFragmentManager().findFragmentByTag("DETAIL_FRAG"));
        ft.commit();


    }

    public void showProgressDialog(){
        progressDialog.show();
    }

    public void hideProgressDialog(){
        progressDialog.hide();
    }

    public void setProgressDialogIcon(Drawable drawable){
        progressDialog.setProgressDrawable(drawable);
    }

    @Override
    public void connectToDevice(Bucket pairingsMap, String bleAddress) {

    }

    public void alertLoss(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.alert,R.anim.pop_exit);

        LossAlertFragment fragment = new LossAlertFragment();
        Bundle args = new Bundle();
        args.putInt(LossAlertFragment.ARG_ITEM_DETECTED, 1);
        fragment.setArguments(args);
        ft.replace(R.id.part_detail_container, fragment, "LOSS_ALERT_FRAGMENT")
                .commitAllowingStateLoss();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }
}
