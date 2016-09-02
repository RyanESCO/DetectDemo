package com.escocorp.detectionDemo.fragments;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.escocorp.detectionDemo.R;
import com.escocorp.detectionDemo.activities.DetectionActivity;
import com.escocorp.detectionDemo.models.DemoPart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
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

    private DemoPart demoPart;

    private LineChart mChart;
    private LineData data;
    private FrameLayout mLossFrameLayout;
    private View mRootView;

    public PartDetailFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LifeCycle","onCreateView");

        if (getArguments().containsKey(ARG_ITEM_SELECTED)) {

            itemSelected = getArguments().getInt(ARG_ITEM_SELECTED);

            demoPart = new DemoPart(itemSelected);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("LifeCycle","onCreateView");

        mRootView = inflater.inflate(R.layout.fragment_part_detail, container, false);

        ImageView imageViewPartImage = (ImageView) mRootView.findViewById(R.id.imageViewPartImage);
        TextView textViewDeviceName = (TextView) mRootView.findViewById(R.id.textViewDeviceName);
        TextView textViewProductType = (TextView) mRootView.findViewById(R.id.textViewProductType);
        TextView textViewInstallationDate = (TextView) mRootView.findViewById(R.id.textViewInstallationDate);
        TextView textViewUsage = (TextView) mRootView.findViewById(R.id.textViewUsage);
        TextView textViewPosition = (TextView) mRootView.findViewById(R.id.textViewPosition);
        mLossFrameLayout = (FrameLayout) mRootView.findViewById(R.id.frameLayoutLoss);

        mDescription = (TextView) mRootView.findViewById(R.id.textViewPartDescription);
        mAgileNumber = (TextView) mRootView.findViewById(R.id.textViewAgileNumber);
        mSensorName = (TextView) mRootView.findViewById(R.id.textViewSensorNumber);

        textViewDeviceName.setText(demoPart.getDeviceName());
        textViewProductType.setText(demoPart.getProductType());
        textViewInstallationDate.setText(demoPart.getInstallationDate());
        textViewUsage.setText(demoPart.getUsage());
        textViewPosition.setText(String.valueOf(itemSelected));

        mChart = (LineChart) mRootView.findViewById(R.id.chart1);
        mChart.setDescription("Sensor Heartbeat");
        mChart.setNoDataTextDescription("Waiting for Device Data Broadcast");

        initializeChart();

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("LifeCycle","onResume");
    }

    private void initializeChart(){
        Log.d("LifeCycle","initializeChart");
        data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data); //add empty data

        YAxis yAxisLeft = mChart.getAxis(YAxis.AxisDependency.LEFT);
        YAxis yAxisRight = mChart.getAxis(YAxis.AxisDependency.RIGHT);
        XAxis xAxis = mChart.getXAxis();

        mChart.setDrawBorders(true);
        mChart.setAutoScaleMinMaxEnabled(false);

        xAxis.setGranularity(1f);
        xAxis.setAxisMaxValue(10f);
        xAxis.setDrawLabels(false);
        yAxisLeft.setDrawLabels(false);

        yAxisLeft.setEnabled(true);
        yAxisRight.setEnabled(true);
        mChart.setDrawGridBackground(false);
        yAxisLeft.setDrawGridLines(false);
        yAxisRight.setDrawGridLines(false);
        xAxis.setDrawGridLines(false);
        yAxisRight.setDrawLabels(false);

        yAxisLeft.setAxisMaxValue(0f);
        yAxisLeft.setAxisMinValue(-100f);

        mChart.setTouchEnabled(false);

    }

    public void addChartDataPoint(int rssi){
        if(data==null){
            return;  //temporary workaround for null pointer error
        }
        ILineDataSet set = data.getDataSetByIndex(0);
        // set.addEntry(...); // can be called as well

        if (set == null) {
            set = createSet();
            data.addDataSet(set);
        }

        if(set.getEntryCount() > 10){
            Log.d("RCDcount","greater than 10");
            set = rippleData(set);
            set.addEntry(new Entry(10,(float)rssi));
        } else {
            set.addEntry(new Entry(set.getEntryCount(), (float) rssi));
        }

        Log.d("RCDcount","number of points: " + String.valueOf(set.getEntryCount()));
        data.removeDataSet(0);
        data.addDataSet(set);

        //data.addEntry(new Entry(set.getEntryCount(), (float) rssi), 0);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        mChart.notifyDataSetChanged();

        // limit the number of visible entries
        //mChart.setVisibleXRangeMaximum(10);

        mChart.invalidate();
    }

    //method used as workaround because realtime charting was not working
    private ILineDataSet rippleData(ILineDataSet set) {
        ILineDataSet rippledSet = createSet();

        for(int j = 1; j < set.getEntryCount();j++){
            rippledSet.addEntry(new Entry(j-1,(float)set.getEntryForIndex(j).getY()));
        }

        return rippledSet;

    }

    public void alertLoss(){
        mLossFrameLayout.setVisibility(View.VISIBLE);
        mChart.setVisibility(View.GONE);
        buildAlertLayout();

    }

    public void buildAlertLayout(){
        TextView acceptButton = (TextView) mRootView.findViewById(R.id.accept_button);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DetectionActivity)getActivity()).reset();
            }
        });

        String titleString= getResources().getString(R.string.dialog_loss_alert_title);
        String messageString= getResources().getString(R.string.dialog_loss_alert_message);

        ImageView alarmView = (ImageView) mRootView.findViewById(R.id.imageViewDialog);

        alarmView.setBackgroundResource(R.drawable.alarm_animation);
        AnimationDrawable anim = (AnimationDrawable) alarmView.getBackground();
        anim.start();

        TextView title = (TextView) mRootView.findViewById(R.id.dialogTitleTextView);
        TextView message = (TextView) mRootView.findViewById(R.id.dialogMessageTextView);

        title.setText(titleString);
        message.setText(messageString);
    }


    public void resetDisplay(){
        Log.d("RCD","reset display");
        mLossFrameLayout.setVisibility(View.INVISIBLE);
        mChart.setVisibility(View.VISIBLE);

        //reset data
        /*data = new LineData();
        ILineDataSet set = createSet();
        data.addDataSet(set);
        mChart.invalidate();*/
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "LIVE");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setCircleRadius(1f);
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
        resetDisplay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
