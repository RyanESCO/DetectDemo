package com.escocorp.detectionDemo.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.escocorp.detectionDemo.activities.PartDetailActivity;
import com.escocorp.detectionDemo.activities.DetectionActivity;
import com.escocorp.detectionDemo.R;
import com.escocorp.detectionDemo.custom.PartsDefinitions;
import com.escocorp.detectionDemo.models.DemoPart;
import com.escocorp.detectionDemo.models.EscoPart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

/**
 * A fragment representing a single Part detail screen.
 * This fragment is either contained in a {@link DetectionActivity}
 * in two-pane mode (on tablets) or a {@link PartDetailActivity}
 * on handsets.
 */
public class PartDetailFragment extends Fragment {

    public static final String ARG_ITEM_SELECTED = "item_selected";

    private int itemSelected;
    public TextView mDescription;
    public TextView mAgileNumber;
    public TextView mSensorName;

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

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                //appBarLayout.setTitle(mItem.content);
            }
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

        addChartDataPoint(-66);
        addChartDataPoint(-76);
        addChartDataPoint(-56);
        return rootView;
    }

    private void initializeChart(){
        data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data); //add empty data

        YAxis yAxisLeft = mChart.getAxis(YAxis.AxisDependency.LEFT);
        YAxis yAxisRight = mChart.getAxis(YAxis.AxisDependency.RIGHT);
        XAxis xAxis = mChart.getXAxis();
        mChart.setAutoScaleMinMaxEnabled(false);
        //yAxisLeft.setAxisMaxValue(12f);
        //yAxisLeft.setAxisMinValue(0);
        //XAxis xAxis = mChart.getAxis(XAxisPosition.TOP);

        xAxis.setGranularity(1f);
        xAxis.setAxisMaxValue(10f);
        xAxis.setDrawLabels(false);
        yAxisLeft.setDrawLabels(false);

        yAxisLeft.setEnabled(true);
        yAxisRight.setEnabled(false);
        mChart.setDrawGridBackground(false);
        //mChart.setVisibleYRange(0f,-100f, YAxis.AxisDependency.LEFT);

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
        // mChart.moveViewTo(data.getXValCount()-7, 55f,
        // AxisDependency.LEFT);
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

    }
}
