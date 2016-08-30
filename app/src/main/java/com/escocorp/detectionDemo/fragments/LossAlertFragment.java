package com.escocorp.detectionDemo.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.escocorp.detectionDemo.BluetoothLeService;
import com.escocorp.detectionDemo.DeviceScanCallback;
import com.escocorp.detectionDemo.R;
import com.escocorp.detectionDemo.activities.DetectionActivity;
import com.escocorp.detectionDemo.models.DemoPart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class LossAlertFragment extends Fragment {

    public static final String ARG_ITEM_DETECTED = "item_selected";

    private int itemSelected;

    public LossAlertFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_DETECTED)) {

            itemSelected = getArguments().getInt(ARG_ITEM_DETECTED);

        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.loss_alert_fragment, container, false);

        Button button = (Button) rootView.findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DetectionActivity)getActivity()).reset();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //mBLEService = ((DetectionActivity)getActivity()).getBLEService();

        //diabled for now
        //mBLEService.scanForDevices(true);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
