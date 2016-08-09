package com.escocorp.detectionDemo.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.escocorp.detectionDemo.FeatureImageHelper;
import com.escocorp.detectionDemo.R;
import com.escocorp.detectionDemo.adapters.MachineFeatureAdapter;
import com.escocorp.detectionDemo.models.IMachineFeature;
import com.escocorp.detectionDemo.models.MachineFeature;

public class BucketLayout extends LinearLayout {

    private static double scaleRatioHeight = 0.75;
    private static double scaleRatioWidth = 1.0;
    private static double wingShroudRatio = 0.07;
    private static double bucketMonitorRatio = 0.1;

    private int mMaxChildWidth = 0;
    private int mMaxChildHeight = 0;
    private int curveAmount = 10;

    private int toothHeight = 0;
    private int shroudHeight = 0;
    private int wingShroudHeight = 0;
    private int wingShroudWidth = 0;
    private int bucketMonitorDimen = 0;
    private int bucketMonitorOffset = 60;

    private int bucketHeight = 0;
    private int bucketWidth = 0;
    private int bucketTop = 0;

    private int paddingLeft = getPaddingLeft();
    private int paddingTop = getPaddingTop();
    private int paddingRight = getPaddingRight();
    private int paddingBottom = getPaddingBottom();

    private int contentWidth = getWidth() - paddingLeft - paddingRight;
    private int contentHeight = getHeight() - paddingTop - paddingBottom;

    private boolean isShovelOperaterLayout = false;

    private MachineFeatureAdapter adapter;
    private Drawable bucketDrawable;
    private Drawable bucketDrawableFacing;
    private AdapterObserver adapterObserver;

    private LinearLayout toothShroudLayout;
    private GridLayout wingShroudLayout;
    private LinearLayout bucketMonitorLayout;

    public BucketLayout(Context context) {
        this(context, null);
    }

    public BucketLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BucketLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BucketLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOrientation(LinearLayout.VERTICAL);
        super.setWillNotDraw(false);
        adapterObserver = new AdapterObserver();
        configureViews();
        init(attrs, defStyleAttr);
    }

    public void setBucketDirection(boolean isUp){
        isShovelOperaterLayout = isUp;
        configureViews();
        super.requestLayout();
//        invalidate();
    }

    public void rotateBucketDirection(){
        setBucketDirection(!isShovelOperaterLayout);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BucketLayout, defStyle, 0);

        if (a.hasValue(R.styleable.BucketLayout_bucketDrawable)) {
            bucketDrawable = a.getDrawable(R.styleable.BucketLayout_bucketDrawable);
        }

        if (a.hasValue(R.styleable.BucketLayout_isBucketOperaterLayout)){
            isShovelOperaterLayout = a.getBoolean(R.styleable.BucketLayout_isBucketOperaterLayout, false);
        }

        a.recycle();
    }


    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        mMaxChildWidth = 0;
        mMaxChildHeight = 0;

        //measure toothShroudLayout
        View v = getChildAt(0);
        v.measure(toothShroudLayout.getWidth(), toothShroudLayout.getHeight());

        //measure wingShroudLayout
        v = getChildAt(1);
        v.measure(wingShroudLayout.getWidth(), wingShroudLayout.getHeight());

        //measure wingShroudLayout
        v = getChildAt(2);
        v.measure(bucketMonitorLayout.getWidth(), bucketMonitorLayout.getHeight());

        //measure toothShroudLayout children
        int count = toothShroudLayout.getChildCount();
        if (count>0) {
            int parentWidthSpec = MeasureSpec.getSize(bucketWidth) / count;
            for (int idx = 0; idx < count; idx++) {
                View child = toothShroudLayout.getChildAt(idx);
                if (child.getVisibility() == GONE) {
                    continue;
                }

                int wSpec = MeasureSpec.makeMeasureSpec(contentWidth / count, MeasureSpec.AT_MOST);
                int hSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(toothHeight), MeasureSpec.AT_MOST);
                if ((Integer) child.getTag(R.id.tag_bucket_view_type) == MachineFeature.FEATURE_TYPE_SHROUD) {
                    hSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(shroudHeight), MeasureSpec.AT_MOST);
                }
                child.measure(wSpec, hSpec);
            }
        }

        //measure wingShroudLayout children
        count = wingShroudLayout.getChildCount();
        if (count>0) {
            for (int idx = 0; idx < count; idx++) {
                View child = wingShroudLayout.getChildAt(idx);
                if (child.getVisibility() == GONE) {
                    continue;
                }

                int wSpec = MeasureSpec.makeMeasureSpec(wingShroudWidth, MeasureSpec.AT_MOST);
                int hSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(wingShroudHeight), MeasureSpec.AT_MOST);
                child.measure(wSpec, hSpec);

            }
        }

        //measure bucketMonitorLayout children
        count = bucketMonitorLayout.getChildCount();
        if (count>0) {
            for (int idx = 0; idx < count; idx++) {
                View child = bucketMonitorLayout.getChildAt(idx);
                if (child.getVisibility() == GONE) {
                    continue;
                }

                int wSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(bucketMonitorDimen), MeasureSpec.AT_MOST);
                int hSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(bucketMonitorDimen), MeasureSpec.AT_MOST);
                child.measure(wSpec, hSpec);

            }
        }

        //set the final measurements
        setMeasuredDimension(resolveSize(mMaxChildWidth, widthSpec), resolveSize(mMaxChildHeight, heightSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();

        contentWidth = getWidth() - paddingLeft - paddingRight;
        contentHeight = getHeight() - paddingTop - paddingBottom;

        bucketHeight = (int)(contentHeight*scaleRatioHeight);
        bucketWidth = (int)(getWidth()*scaleRatioWidth);
        bucketTop = (contentHeight-bucketHeight);

        toothHeight = (contentHeight/6)*2;
        shroudHeight = contentHeight/5;

        int sectionHeight = toothHeight+((toothShroudLayout.getChildCount()/2) * curveAmount);
//        int sectionTop = paddingTop;//bucketTop - Double.valueOf(toothHeight * 0.7).intValue();

        if (isShovelOperaterLayout){
            int sectionTop = bucketTop - Double.valueOf(toothHeight * 0.7).intValue();
            toothShroudLayout.layout(0, sectionTop, contentWidth, sectionTop + sectionHeight);
            wingShroudLayout.setRowCount(wingShroudLayout.getChildCount() / 2);
            wingShroudLayout.layout(0, toothShroudLayout.getBottom(), contentWidth, toothShroudLayout.getBottom() + sectionHeight);
            bucketMonitorLayout.layout(0, wingShroudLayout.getBottom(), contentWidth, wingShroudLayout.getBottom() + sectionHeight);
            //need to customize the layout for the teeth/shrouds
            renderLipLayoutUp(toothShroudLayout.getChildCount());
            //need to customuze the layout for the wingshrouds
            renderSidesLayoutUp(wingShroudLayout.getChildCount());
            //need to customize the layout for the bucketmonitors
            renderBackLayoutUp(bucketMonitorLayout.getChildCount());
        } else {
            int sectionTop = paddingTop;
            bucketMonitorLayout.layout(0, sectionTop, contentWidth, (sectionTop + sectionHeight/2));
            wingShroudLayout.setRowCount(wingShroudLayout.getChildCount() / 2);
            wingShroudLayout.layout(0, bucketMonitorLayout.getBottom(), contentWidth, bucketMonitorLayout.getBottom() + sectionHeight);
            toothShroudLayout.layout(0, wingShroudLayout.getBottom(), contentWidth, wingShroudLayout.getBottom() + sectionHeight);
            //need to customize the layout for the teeth/shrouds
            renderLipLayoutDown(toothShroudLayout.getChildCount());
            //need to customuze the layout for the wingshrouds
            renderSidesLayoutDown(wingShroudLayout.getChildCount());
            //need to customize the layout for the bucketmonitors
            renderBackLayoutDown(bucketMonitorLayout.getChildCount());
        }
    }

    private void renderLipLayoutUp(int childCount){

        final int curveCount = childCount/2;//find the mid point

        int itemWidth = childCount>0?bucketWidth/childCount:bucketWidth;
        int leftPos = ((contentWidth/2)-(bucketWidth/2))+paddingLeft;
        int featurePosBaseline = toothShroudLayout.getBottom();

        View child;
        for (int idx=0; idx<childCount;idx++){
            if (null!=adapter) {
                //curve the teeth/shrouds slightly to make the visual
                //more interesting/realistic
                if (idx<curveCount && idx!=0){
                    featurePosBaseline-=curveAmount;
                } else if (idx>childCount-curveCount){
                    featurePosBaseline+=curveAmount;
                }
                //use all the calculated values to layout out the teeth/shrouds
                child = toothShroudLayout.getChildAt(idx);
                int type = (Integer)child.getTag(R.id.tag_bucket_view_type);
                if (type == MachineFeature.FEATURE_TYPE_TOOTH) {
                    child.layout(leftPos, featurePosBaseline-toothHeight, leftPos + itemWidth, featurePosBaseline);
                } else if (type == MachineFeature.FEATURE_TYPE_SHROUD) {
                    int shroudTop = featurePosBaseline-shroudHeight;
                    child.layout(leftPos, shroudTop, leftPos + itemWidth, shroudTop + wingShroudHeight);
                }
                leftPos += itemWidth;
            }
        }
    }

    private void renderSidesLayoutUp(int childCount){
        int leftPos = 0;
        wingShroudHeight = wingShroudLayout.getHeight() / 2;
        if (wingShroudLayout.getRowCount()>1) {
            wingShroudHeight = wingShroudLayout.getHeight() / wingShroudLayout.getRowCount();
        }
        wingShroudWidth = Double.valueOf(contentWidth*wingShroudRatio).intValue();
        int top = 0;
        View child;
        for (int idx=0; idx<childCount;idx++){
            if (null!=adapter) {
                //use all the calculated values to layout out the teeth/shrouds
                child = wingShroudLayout.getChildAt(idx);
                int type = (Integer)child.getTag(R.id.tag_bucket_view_type);
                if (type == MachineFeature.FEATURE_TYPE_WING_SHROUD) {
                    final ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
                    if (idx%2==1){
                        leftPos = contentWidth-wingShroudWidth;
                    } else {
                        leftPos = 0;
                    }
                    if (idx>1){
                        int row = idx/2;
                        top = row*wingShroudHeight;
                    }
                    child.layout(leftPos, top, leftPos + wingShroudWidth, top+wingShroudHeight);
                }
            }
        }
    }

    private void renderBackLayoutUp(int childCount){
        bucketMonitorDimen = Double.valueOf(bucketMonitorRatio*contentWidth).intValue();
        int leftPos = ((contentWidth/2)-((bucketMonitorDimen*childCount)/2));
        int top = 0;

        View child;
        for (int idx=0; idx<childCount;idx++){
            if (null!=adapter) {
                //use all the calculated values to layout out the teeth/shrouds
                child = bucketMonitorLayout.getChildAt(idx);
                int type = (Integer)child.getTag(R.id.tag_bucket_view_type);
                if (type == MachineFeature.FEATURE_TYPE_BUCKET_MONITOR) {
                    child.layout(leftPos, top, leftPos + bucketMonitorDimen, top + bucketMonitorDimen);
                }
                leftPos += bucketMonitorDimen;
            }
        }
    }

    private void renderLipLayoutDown(int childCount){

        final int curveCount = childCount/2;//find the mid point

        int itemWidth = childCount>0?bucketWidth/childCount:bucketWidth;
        int leftPos = ((contentWidth/2)-(bucketWidth/2))+paddingLeft;
        int featurePosBaseline = 0;//toothShroudLayout.getTop();//.getBottom();

        View child;
        for (int idx=0; idx<childCount;idx++){
            if (null!=adapter) {
                //curve the teeth/shrouds slightly to make the visual
                //more interesting/realistic
                if (idx<curveCount && idx!=0){
                    featurePosBaseline+=curveAmount;
                } else if (idx>childCount-curveCount){
                    featurePosBaseline-=curveAmount;
                }
                //use all the calculated values to layout out the teeth/shrouds
                child = toothShroudLayout.getChildAt(idx);
                int type = (Integer)child.getTag(R.id.tag_bucket_view_type);
                if (type == MachineFeature.FEATURE_TYPE_TOOTH) {
                    child.layout(leftPos, featurePosBaseline, leftPos + itemWidth, featurePosBaseline+toothHeight);
                } else if (type == MachineFeature.FEATURE_TYPE_SHROUD) {
                    child.layout(leftPos, featurePosBaseline, leftPos + itemWidth, featurePosBaseline + wingShroudHeight);
                }
                leftPos += itemWidth;
            }
        }
    }

    private void renderSidesLayoutDown(int childCount){
        int leftPos = 0;
        wingShroudHeight = wingShroudLayout.getHeight() / 2;
        if (wingShroudLayout.getRowCount()>1) {
            wingShroudHeight = wingShroudLayout.getHeight() / wingShroudLayout.getRowCount();
        }
        wingShroudWidth = Double.valueOf(contentWidth*wingShroudRatio).intValue();
        int top = 0;
        View child;
        for (int idx=0; idx<childCount;idx++){
            if (null!=adapter) {
                //use all the calculated values to layout out the teeth/shrouds
                child = wingShroudLayout.getChildAt(idx);
                int type = (Integer)child.getTag(R.id.tag_bucket_view_type);
                if (type == MachineFeature.FEATURE_TYPE_WING_SHROUD) {
                    final ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
                    if (idx%2==1){
                        leftPos = contentWidth-wingShroudWidth;
                    } else {
                        leftPos = 0;
                    }
                    if (idx>1){
                        int row = idx/2;
                        top = row*wingShroudHeight;
                    }
                    child.layout(leftPos, top, leftPos + wingShroudWidth, top+wingShroudHeight);
                }
            }
        }
    }

    private void renderBackLayoutDown(int childCount){
        bucketMonitorDimen = Double.valueOf(bucketMonitorRatio*contentWidth).intValue();
        int leftPos = ((contentWidth/2)-((bucketMonitorDimen*childCount)/2));
        int top = bucketMonitorLayout.getHeight()-bucketMonitorDimen;
        //int top = bucketMonitorLayout.getHeight();

        View child;
        for (int idx=0; idx<childCount;idx++){
            if (null!=adapter) {
                //use all the calculated values to layout out the teeth/shrouds
                child = bucketMonitorLayout.getChildAt(idx);
                int type = (Integer)child.getTag(R.id.tag_bucket_view_type);
                if (type == MachineFeature.FEATURE_TYPE_BUCKET_MONITOR) {
                    child.layout(leftPos, top, leftPos + bucketMonitorDimen, top + bucketMonitorDimen);
                }
                leftPos += bucketMonitorDimen;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int leftPos = ((contentWidth/2)-(bucketWidth/2))+paddingLeft;

        final Drawable drawable = bucketDrawable;

        if (drawable != null) {
            drawable.setBounds(leftPos, bucketTop, leftPos + bucketWidth, bucketTop + bucketHeight);
            canvas.save();
            if (!isShovelOperaterLayout) {
                canvas.rotate(180, getWidth() / 2, getHeight() / 2);
            }
            drawable.draw(canvas);
            canvas.restore();
        }
    }

    /*private void rotateSubElements(Canvas canvas, int fromDegrees, int toDegrees) {
        int count = toothShroudLayout.getChildCount();
        for (int idx=0;idx<count;idx++){
            final View view = toothShroudLayout.getChildAt(idx);
            final TextView textView = (TextView)view.findViewById(R.id.mc_name);
            if (null!=textView){
                final RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees, textView.getWidth()/2, textView.getHeight()/2);
                rotate.setDuration(100);
                rotate.setFillAfter(true);
                textView.startAnimation(rotate);
            }

        }
        count = wingShroudLayout.getChildCount();
        for (int idx=0;idx<count;idx++){
            final View view = wingShroudLayout.getChildAt(idx);
            final TextView textView = (TextView)view.findViewById(R.id.mc_name);
            if (null!=textView){
                final RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees, textView.getWidth()/2, textView.getHeight()/2);
                rotate.setDuration(100);
                rotate.setFillAfter(true);
                textView.startAnimation(rotate);
            }

        }
        count = bucketMonitorLayout.getChildCount();
        for (int idx=0;idx<count;idx++){
            final View view = bucketMonitorLayout.getChildAt(idx);
            final TextView textView = (TextView)view.findViewById(R.id.mc_name);
            if (null!=textView){
                final RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees, textView.getWidth()/2, textView.getHeight()/2);
                rotate.setDuration(100);
                rotate.setFillAfter(true);
                textView.startAnimation(rotate);
            }

        }

    }*/

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        this.adapter.unregisterAdapterDataObserver(adapterObserver);
        adapterObserver = null;
    }

    public void setAdapter(MachineFeatureAdapter adapter) {
        this.adapter = adapter;
        this.adapter.registerAdapterDataObserver(adapterObserver);
        super.requestLayout();
    }

    public MachineFeatureAdapter getAdapter() {
        return adapter;
    }

    private void configureViews() {
        this.removeAllViews();
        toothShroudLayout = new LinearLayout(getContext());
        toothShroudLayout.setOrientation(LinearLayout.HORIZONTAL);
        wingShroudLayout = new GridLayout(getContext());
        wingShroudLayout.setColumnCount(2);
        bucketMonitorLayout = new LinearLayout(getContext());
        bucketMonitorLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(bucketMonitorLayout, 0);
        addView(wingShroudLayout, 1);
        addView(toothShroudLayout, 2);
        if (isShovelOperaterLayout){
            configureUpView();
        } else {
            configureDownView();
        }
    }

    private void configureUpView() {
        if (null!=getAdapter()) {
            for (int idx = 0; idx<getAdapter().getItemCount(); idx++) {
                final IMachineFeature component = adapter.getComponent(idx);
                final int type = adapter.getItemViewType(idx);
                final RecyclerView.ViewHolder viewHolder = adapter.createViewHolder(this, type);
                adapter.bindViewHolder(viewHolder, idx);
                final View child = viewHolder.itemView;
                child.setTag(R.id.tag_bucket_view_type, type);
                if (component.getFeatureType() == MachineFeature.FEATURE_TYPE_WING_SHROUD) {
                    final GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    if (wingShroudLayout.getChildCount() % 2 == 1) {
                        params.setGravity(Gravity.RIGHT);
                    } else {
                        params.setGravity(Gravity.LEFT);
                    }
                    child.setLayoutParams(params);
                    wingShroudLayout.addView(child);
                } else if (component.getFeatureType() == MachineFeature.FEATURE_TYPE_BUCKET_MONITOR) {
                    bucketMonitorLayout.addView(child);
                } else {
                    toothShroudLayout.addView(child);
                }

                final int state = component.getState();
                int graphic = FeatureImageHelper.getGraphicId(component.getFeatureType(), component.getState(), true);
                int color = getResources().getColor(R.color.default_text_color);
                switch (state) {
                    case MachineFeature.STATE_QUEUED:
                        color = getResources().getColor(R.color.default_text_color);
                        break;
                    case MachineFeature.STATE_ASSIGNED:
                        color = getResources().getColor(R.color.white);
                        break;
                    default:
                        color = getResources().getColor(R.color.default_text_color);
                }
                ((TextView) child.findViewById(R.id.mc_name)).setTextColor(color);
                ((ImageButton) child.findViewById(R.id.mc_button)).setImageDrawable(getResources().getDrawable(graphic));
            }
        }
    }

    private void configureDownView() {
        if (null!=getAdapter()) {
            for (int idx = getAdapter().getItemCount()-1; idx>=0; idx--) {
                final IMachineFeature component = adapter.getComponent(idx);
                final int type = adapter.getItemViewType(idx);
                final RecyclerView.ViewHolder viewHolder = adapter.createViewHolder(this, type);
                adapter.bindViewHolder(viewHolder, idx);
                final View child = viewHolder.itemView;
                child.setTag(R.id.tag_bucket_view_type, type);
                if (component.getFeatureType() == MachineFeature.FEATURE_TYPE_WING_SHROUD) {
                    final GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    if (wingShroudLayout.getChildCount() % 2 == 1) {
                        params.setGravity(Gravity.RIGHT);
                    } else {
                        params.setGravity(Gravity.LEFT);
                    }
                    child.setLayoutParams(params);
                    wingShroudLayout.addView(child);
                } else if (component.getFeatureType() == MachineFeature.FEATURE_TYPE_BUCKET_MONITOR) {
                    bucketMonitorLayout.addView(child);
                } else {
                    toothShroudLayout.addView(child);
                }

                final int state = component.getState();
                int graphic = FeatureImageHelper.getGraphicId(component.getFeatureType(), component.getState(), false);
                int color = getResources().getColor(R.color.default_text_color);
                switch (state) {
                    case MachineFeature.STATE_QUEUED:
                        color = getResources().getColor(R.color.default_text_color);
                        break;
                    case MachineFeature.STATE_ASSIGNED:
                        color = getResources().getColor(R.color.white);
                        break;
                    default:
                        color = getResources().getColor(R.color.default_text_color);
                }
                ((TextView) child.findViewById(R.id.mc_name)).setTextColor(color);
                ((ImageButton) child.findViewById(R.id.mc_button)).setImageDrawable(getResources().getDrawable(graphic));
            }
        }
    }

    class AdapterObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            configureViews();
        }
    }
}
