package com.escocorp.detectionDemo.fragments;

import android.app.Activity;
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
import com.escocorp.detectionDemo.models.EscoPart;

/**
 * A fragment representing a single Part detail screen.
 * This fragment is either contained in a {@link DetectionActivity}
 * in two-pane mode (on tablets) or a {@link PartDetailActivity}
 * on handsets.
 */
public class PartDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    private EscoPart mItem;
    public TextView mDescription;
    public TextView mAgileNumber;
    public TextView mSensorName;

    public PartDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {

            mItem = getArguments().getParcelable(ARG_ITEM_ID);

            if(mItem==null){
                mItem = getActivity().getIntent().getParcelableExtra(ARG_ITEM_ID);
            }

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

        if(mItem==null) {
            return null;
        }

        View rootView;

        if(mItem.partType== PartsDefinitions.TYPE_POD){
            rootView = inflater.inflate(R.layout.part_detail_bucket, container, false);

        } else {
            rootView = inflater.inflate(R.layout.part_detail, container, false);

            ImageView imageViewPartImage = (ImageView) rootView.findViewById(R.id.imageViewPartImage);
            TextView textViewDeviceName = (TextView) rootView.findViewById(R.id.textViewDeviceName);
            TextView textViewProductType = (TextView) rootView.findViewById(R.id.textViewProductType);
            TextView textViewInstallationDate = (TextView) rootView.findViewById(R.id.textViewInstallationDate);
            TextView textViewUsage = (TextView) rootView.findViewById(R.id.textViewUsage);

/*            textViewDeviceName.setText(mItem.PMID);
            textViewProductType.setText(mItem.details);
            textViewInstallationDate.setText(mItem.commissionDate);
            textViewUsage.setText(mItem.usageHours);
            imageViewPartImage.setImageDrawable(getActivity().getDrawable(mItem.drawableID));*/
        }

        mDescription = (TextView) rootView.findViewById(R.id.textViewPartDescription);
        mAgileNumber = (TextView) rootView.findViewById(R.id.textViewAgileNumber);
        mSensorName = (TextView) rootView.findViewById(R.id.textViewSensorNumber);

/*        mDescription.setText(mItem.description);
        mAgileNumber.setText(mItem.agileNumber);
        mSensorName.setText(mItem.getName());*/

        return rootView;
    }
}
