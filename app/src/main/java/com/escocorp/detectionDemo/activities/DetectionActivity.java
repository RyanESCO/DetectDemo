package com.escocorp.detectionDemo.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.escocorp.detectionDemo.BluetoothLEScanner;
import com.escocorp.detectionDemo.R;
import com.escocorp.detectionDemo.ScanListener;
import com.escocorp.detectionDemo.custom.IconSpinnerProgressDialog;
import com.escocorp.detectionDemo.fragments.PartDetailFragment;
import com.escocorp.detectionDemo.models.EscoPart;

import java.util.ArrayList;
import java.util.HashMap;

public class DetectionActivity extends AppCompatActivity {

    private boolean mTwoPane;
    private boolean isScanning = false;
    BluetoothLEScanner bluetoothLEScanner;
    ArrayList<EscoPart> mParts;
    //SimpleItemRecyclerViewAdapter mPartsAdapter;
    private HashMap<String, EscoPart> mBeacons;
    protected IconSpinnerProgressDialog progressDialog;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String SAVED_STATE = "saved_State";

    PartDetailFragment mFragment;
    //InventorySummaryFragment mSummaryFragment;

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

        //mSummaryFragment = new InventorySummaryFragment();
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.inventorySummaryFrameLayout, mSummaryFragment)
                    .commit();
        }*/

        /*FloatingActionButton fabStart = (FloatingActionButton) findViewById(R.id.fabStart);
        FloatingActionButton fabStop = (FloatingActionButton) findViewById(R.id.fabStop);*/

/*        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isScanning){
                    Toast.makeText(getApplicationContext(),"Scan Stopped", Toast.LENGTH_SHORT).show();
                    bluetoothLEScanner.stop();
                    isScanning = false;
                }
                else {
                    Toast.makeText(getApplicationContext(),"Scanning", Toast.LENGTH_SHORT).show();
                    bluetoothLEScanner.startScan();
                    isScanning = true;

                }
                //updateFAB();

            }
        };
        fabStart.setOnClickListener(listener);
        fabStop.setOnClickListener(listener);*/

        View recyclerView = findViewById(R.id.part_list);
        assert recyclerView != null;
        //setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.part_detail_container) != null) {
            mTwoPane = true;
        }

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

        bluetoothLEScanner = new BluetoothLEScanner(this);
        bluetoothLEScanner.setListener(new ScanListener() {
            @Override
            public void onScanned() {
                // update and log data after scan
                //updateList();

            }
        });

        mBeacons = bluetoothLEScanner.mBeacons;

        //bluetoothLEScanner.startScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!bluetoothLEScanner.check())
            finish();
    }

    /*private void updateFAB(){
        FloatingActionButton fabStop = (FloatingActionButton) findViewById(R.id.fabStop);
        FloatingActionButton fabStart = (FloatingActionButton) findViewById(R.id.fabStart);
        if(isScanning){
            fabStop.setVisibility(View.VISIBLE);
            fabStart.setVisibility(View.INVISIBLE);
        } else{
            fabStop.setVisibility(View.INVISIBLE);
            fabStart.setVisibility(View.VISIBLE);
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add the "scan" option to the menu
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        //state.putParcelableArrayList(SAVED_STATE, (ArrayList<? extends Parcelable>) mPartsAdapter.mValues);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_reset:
                /*Toast.makeText(this,"Reset", Toast.LENGTH_SHORT).show();
                clearFragment();
                bluetoothLEScanner.stop();
                isScanning = false;
                updateFAB();
                mSummaryFragment.clearList();
                mPartsAdapter.clear();
                mPartsAdapter.notifyDataSetChanged();*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void clearFragment(){
        if (mTwoPane&&mFragment!=null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(mFragment)
                    .commit();
        }
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
}
