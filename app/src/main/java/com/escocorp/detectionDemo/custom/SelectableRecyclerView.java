package com.escocorp.detectionDemo.custom;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class SelectableRecyclerView extends RecyclerView {

    private int selectedIndex = -1;
    private int previouslySelectedIndex = -1;

    public SelectableRecyclerView(Context context) {
        super(context);
    }

    public SelectableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setSelectedIndex(int index){
        refreshSelectedItem(index);
    }

    public int getSelectedIndex(){
        return selectedIndex;
    }

    private void refreshSelectedItem(int selectedIndex){
        if (selectedIndex>=0){
            if (previouslySelectedIndex == selectedIndex){
                this.selectedIndex = -1;
            } else {
                this.selectedIndex = selectedIndex;
            }
        }
        this.previouslySelectedIndex = this.selectedIndex;
    }

}
