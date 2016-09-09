package com.escocorp.detectionDemo.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.escocorp.detectionDemo.R;
import com.escocorp.detectionDemo.models.Sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ryan Dymock on 9/8/2016.
 */
public class SensorDialogFragment extends DialogFragment {

    BLEDevicesAdapter adapter = null;
    RecyclerView recyclerView;
    public String oldDeviceName;

    HashMap<String,String> allDevicesMap = new HashMap<>();

    public interface SensorDialogFragmentListener {
        void onDialogPositiveClick(SensorDialogFragment dialog, String deviceName, String macAddress);
    }

    private SensorDialogFragmentListener mListener;
    private Sensor mSensor = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        final View view = inflater.inflate(R.layout.bluetooth_chooser_dialog_layout, null);

        builder.setView(view)
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //mListener.onDialogNegativeClick(SensorDialogFragment.this);
                    }
                });

        //initialize recycler view with bluetooth options
        adapter = new BLEDevicesAdapter(allDevicesMap);
        recyclerView = (RecyclerView) view.findViewById(R.id.sensor_list_recyler_view);
        recyclerView.setAdapter(adapter);

        //start scanning and fill recyclerview on receive

        //set up on clicklistener in view holder

        return builder.create();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey("all_devices")) {
            allDevicesMap = (HashMap<String,String>)getArguments().getSerializable("all_devices");
        }

        if (getArguments().containsKey("old_device_name")) {
            oldDeviceName = getArguments().getString("old_device_name");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the SensorDialogFragmentListener so we can send events to the host
            mListener = (SensorDialogFragmentListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement SensorDialogFragmentListener");
        }
    }

    private void selectSensor(String deviceName, String macAddress){
        adapter.notifyDataSetChanged();
        mListener.onDialogPositiveClick(SensorDialogFragment.this, deviceName, macAddress);
        getDialog().dismiss();
    }

    public String getOldDeviceName(){
        return oldDeviceName;
    }

    class BLEDevicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private HashMap<String, String> map = new HashMap<>();
        private ArrayList<String> sortedDeviceNames = new ArrayList<>();

        public BLEDevicesAdapter(){}

        public BLEDevicesAdapter(HashMap<String, String> hashMap){
            if (null!=hashMap) {
                this.map = hashMap;
                createSortedList();
            }
        }

        private void createSortedList(){
            for(String key:map.keySet()){
                sortedDeviceNames.add(key);
            }

            Collections.sort(sortedDeviceNames, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.compareToIgnoreCase(s2);
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BLEDevicesViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.bluetooth_device_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((BLEDevicesViewHolder)holder).bind(getContext(), position);
        }

        @Override
        public int getItemCount() {
            return map.size();
        }

        public class BLEDevicesViewHolder extends RecyclerView.ViewHolder {

            protected TextView deviceName;
            protected TextView macAddress;
            protected LinearLayout layout;

            public BLEDevicesViewHolder(View itemView) {
                super(itemView);
                deviceName = (TextView) itemView.findViewById(R.id.textViewDeviceName);
                macAddress = (TextView) itemView.findViewById(R.id.textViewMacAddress);
                layout = (LinearLayout) itemView.findViewById(R.id.bluetooth_data_layout);
            }

            public void bind(Context context, final int position) {
                final String deviceNameString = sortedDeviceNames.get(position);
                final String macAddressString = map.get(deviceNameString);

                if (macAddress != null && deviceName!=null) {
                    deviceName.setText(deviceNameString);
                    macAddress.setText(macAddressString);
                }

                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectSensor(deviceNameString, macAddressString);
                    }
                });
            }

        }
    }
}
