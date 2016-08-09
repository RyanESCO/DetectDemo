package com.escocorp.detectionDemo.custom;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.escocorp.detectionDemo.R;

public class IconSpinnerProgressDialog extends ProgressDialog {

    private Drawable drawable;
    private ImageView icon;

    public IconSpinnerProgressDialog(Context context) {
        this(context, R.style.TransparentProgressDialog);
    }

    public IconSpinnerProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setIndeterminate(true);
        this.setCancelable(false);
        setContentView(R.layout.dialog_icon_spinner_progress);
        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(getContext().getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
        icon = (ImageView)findViewById(R.id.progressIcon);
        if (null!=drawable) {
            icon.setImageDrawable(this.drawable);
        }
    }

    public void setProgressDrawable(Drawable drawable){
        this.drawable = drawable;
        if (null!=drawable && null!=icon) {
            icon.setImageDrawable(this.drawable);
        }
    }


}
