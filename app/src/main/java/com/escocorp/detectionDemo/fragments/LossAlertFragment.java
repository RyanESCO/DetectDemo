package com.escocorp.detectionDemo.fragments;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.escocorp.detectionDemo.R;
import com.escocorp.detectionDemo.activities.DetectionActivity;

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

        View rootView = inflater.inflate(R.layout.alarm_layout, container, false);

        TextView acceptButton = (TextView) rootView.findViewById(R.id.accept_button);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DetectionActivity)getActivity()).reset();
            }
        });

        String titleString= getResources().getString(R.string.dialog_loss_alert_title);
        String messageString= getResources().getString(R.string.dialog_loss_alert_message);


        ImageView alarmView = (ImageView) rootView.findViewById(R.id.imageViewDialog);

        alarmView.setBackgroundResource(R.drawable.alarm_animation);
        AnimationDrawable anim = (AnimationDrawable) alarmView.getBackground();
        anim.start();

        TextView title = (TextView) rootView.findViewById(R.id.dialogTitleTextView);
        TextView message = (TextView) rootView.findViewById(R.id.dialogMessageTextView);


        title.setText(titleString);
        message.setText(messageString);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
