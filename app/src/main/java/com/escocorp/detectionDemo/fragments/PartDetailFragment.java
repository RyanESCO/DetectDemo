package com.escocorp.detectionDemo.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.escocorp.detectionDemo.BluetoothLeService;
import com.escocorp.detectionDemo.DeviceScanCallback;
import com.escocorp.detectionDemo.activities.DetectionActivity;
import com.escocorp.detectionDemo.R;
import com.escocorp.detectionDemo.models.DemoPart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class PartDetailFragment extends Fragment {

    public static final String ARG_ITEM_SELECTED = "item_selected";

    private int itemSelected;
    public TextView mDescription;
    public TextView mAgileNumber;
    public TextView mSensorName;

    BluetoothLeService mBLEService;

    private DemoPart demoPart;

    private LineChart mChart;
    private LineData data;

    public PartDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_SELECTED)) {

            itemSelected = getArguments().getInt(ARG_ITEM_SELECTED);

            demoPart = new DemoPart(itemSelected);

        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.part_detail, container, false);

        ImageView imageViewPartImage = (ImageView) rootView.findViewById(R.id.imageViewPartImage);
        TextView textViewDeviceName = (TextView) rootView.findViewById(R.id.textViewDeviceName);
        TextView textViewProductType = (TextView) rootView.findViewById(R.id.textViewProductType);
        TextView textViewInstallationDate = (TextView) rootView.findViewById(R.id.textViewInstallationDate);
        TextView textViewUsage = (TextView) rootView.findViewById(R.id.textViewUsage);

        mDescription = (TextView) rootView.findViewById(R.id.textViewPartDescription);
        mAgileNumber = (TextView) rootView.findViewById(R.id.textViewAgileNumber);
        mSensorName = (TextView) rootView.findViewById(R.id.textViewSensorNumber);

        textViewDeviceName.setText(demoPart.getDeviceName());
        textViewProductType.setText(demoPart.getProductType());
        textViewInstallationDate.setText(demoPart.getInstallationDate());
        textViewUsage.setText(demoPart.getUsage());

        mChart = (LineChart) rootView.findViewById(R.id.chart1);
        mChart.setDescription("Sensor Heartbeat");
        mChart.setNoDataTextDescription("Waiting for Device Data Broadcast");

        initializeChart();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        final IntentFilter serviceBoundIntentFilter = new IntentFilter();
        serviceBoundIntentFilter.addAction(DetectionActivity.ACTION_BLUETOOTH_SVC_BOUND);

        final IntentFilter btIntentFilter = new IntentFilter();
        btIntentFilter.addAction(DeviceScanCallback.DEVICE_SCAN_RESULT);

        getActivity().registerReceiver(mBlueToothServiceReceiver, serviceBoundIntentFilter);
        getActivity().registerReceiver(mDeviceScanReceiver, btIntentFilter);

        mBLEService = ((DetectionActivity)getActivity()).getBLEService();

        //diabled for now
        //mBLEService.scanForDevices(true);
    }

    private final BroadcastReceiver mBlueToothServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DetectionActivity.ACTION_BLUETOOTH_SVC_BOUND.equals(action)) {
                Toast.makeText(getContext(),"SERVICE BOUND",Toast.LENGTH_SHORT).show();
                //scanLeDevice(true);
            }
        }
    };

    private final BroadcastReceiver mDeviceScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action){
                case DeviceScanCallback.DEVICE_SCAN_COMPLETE:
                    Log.d("RCD1","scan complete");

                    break;
                case DeviceScanCallback.DEVICE_SCAN_RESULT:
                    int RSSI = intent.getIntExtra(DeviceScanCallback.RSSI,-50);
                    String deviceName = intent.getStringExtra("name");
                    if (deviceName.equals(demoPart.getDeviceName())) {
                        addChartDataPoint(RSSI);
                        Log.d("RCD1","match: " + deviceName);
                    }

                    break;

                default:

                    break;
            }
        }
    };

    private void initializeChart(){
        data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data); //add empty data

        YAxis yAxisLeft = mChart.getAxis(YAxis.AxisDependency.LEFT);
        YAxis yAxisRight = mChart.getAxis(YAxis.AxisDependency.RIGHT);
        XAxis xAxis = mChart.getXAxis();
        mChart.setAutoScaleMinMaxEnabled(false);

        xAxis.setGranularity(1f);
        xAxis.setAxisMaxValue(10f);
        xAxis.setDrawLabels(false);
        yAxisLeft.setDrawLabels(false);

        yAxisLeft.setEnabled(true);
        yAxisRight.setEnabled(false);
        mChart.setDrawGridBackground(false);

        mChart.setTouchEnabled(false);

    }

    public void addChartDataPoint(int rssi){
        ILineDataSet set = data.getDataSetByIndex(0);
        // set.addEntry(...); // can be called as well

        if (set == null) {
            set = createSet();
            data.addDataSet(set);
        }

        data.addEntry(new Entry(set.getEntryCount(), (float) rssi), 0);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        mChart.notifyDataSetChanged();

        // limit the number of visible entries
        mChart.setVisibleXRangeMaximum(10);

        // move to the latest entry
        mChart.moveViewToX(data.getEntryCount());

        // this automatically refreshes the chart (calls invalidate())
        //mChart.invalidate();
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.RED);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mBlueToothServiceReceiver);
        getActivity().unregisterReceiver(mDeviceScanReceiver);
        mBLEService.scanForDevices(false);

    }
}
