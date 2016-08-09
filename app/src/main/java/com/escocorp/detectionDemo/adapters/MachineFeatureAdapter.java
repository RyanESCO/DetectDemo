package com.escocorp.detectionDemo.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.escocorp.detectionDemo.DeviceScanCallback;
import com.escocorp.detectionDemo.IFeatureSelectionListener;
import com.escocorp.detectionDemo.R;
import com.escocorp.detectionDemo.models.Bucket;
import com.escocorp.detectionDemo.models.IMachineFeature;
import com.escocorp.detectionDemo.models.ISensor;
import com.escocorp.detectionDemo.models.MachineFeature;

import java.util.List;

public class MachineFeatureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = MachineFeatureAdapter.class.getSimpleName();

    public static final int TOOTH_TYPE_VH = 0;
    public static final int SHROUD_TYPE_VH = 1;
    public static final int WING_SHROUD_TYPE_VH = 2;
    public static final int BUCKET_MONITOR_TYPE_VH = 3;

    private Context context;
    private final Resources resources;
    private IFeatureSelectionListener pairingsController;

    public MachineFeatureAdapter(Context context, IFeatureSelectionListener pairingsController){
        super();
        this.context = context;
        this.pairingsController = pairingsController;
        this.pairingsController.addChangeListener(this);

        resources = context.getResources();
    }

    public int getItemCount(){
        return pairingsController.getPairingModel().getFeatures().size();
    }

    @Override
    public int getItemViewType (int position){
        final IMachineFeature machineComponent = pairingsController.getPairingModel().getFeatures().get(position);

        switch(machineComponent.getFeatureType()){
            case MachineFeature.FEATURE_TYPE_TOOTH:
                return TOOTH_TYPE_VH;
            case MachineFeature.FEATURE_TYPE_SHROUD:
                return SHROUD_TYPE_VH;
            case MachineFeature.FEATURE_TYPE_WING_SHROUD:
                return WING_SHROUD_TYPE_VH;
            case MachineFeature.FEATURE_TYPE_BUCKET_MONITOR:
                return BUCKET_MONITOR_TYPE_VH;
        }

        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v;
        MachineComponentViewHolder viewHolder = null;

        switch(viewType){
            case TOOTH_TYPE_VH:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tooth, parent, false);
                viewHolder = new MachineComponentViewHolder(v);
                break;
            case SHROUD_TYPE_VH:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shroud, parent, false);
                viewHolder = new MachineComponentViewHolder(v);
                break;
            case WING_SHROUD_TYPE_VH:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wing_shroud, parent, false);
                viewHolder = new MachineComponentViewHolder(v);
                break;
            case BUCKET_MONITOR_TYPE_VH:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bucket_monitor, parent, false);
                viewHolder = new MachineComponentViewHolder(v);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ((MachineComponentViewHolder) holder).bind(context, position);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public void onSaveInstanceState(Bundle outState) {

    }

    public void onViewStateRestored(Bundle savedInstanceState) {

    }

    public int getFeatureTypeCount(int featureType){
        int retVal = 0;

        if (null!=pairingsController.getPairingModel() && null!=pairingsController.getPairingModel().getFeatures()) {
            final List<IMachineFeature> features = pairingsController.getPairingModel().getFeatures();
            for (int idx = 0; idx < features.size(); idx++) {
                if(features.get(idx).getFeatureType()==featureType){
                    retVal+=1;
                }
            }
        }
        return retVal;
    }

    public IMachineFeature getComponent(int position) {
        final Bucket pairingModel = pairingsController.getPairingModel();
        if (null!=pairingModel && pairingModel.getFeatures().size()>0) {
            return pairingModel.getFeatures().get(position);
        }else{
            return null;
        }
    }

    private void handleFeatureClick(int position, boolean isDetecting){
        final Bucket pairingModel = pairingsController.getPairingModel();
        final IMachineFeature machineFeature = pairingModel.getFeatures().get(position);
        int currentState = machineFeature.getState();

        if(!isDetecting){
            pairingsController.toggleFeatureState(machineFeature,context);
            return;
        } else if (isDetecting && currentState ==2){
            pairingsController.toggleFeatureState(machineFeature,context);
        }

        beginProximityDetection(machineFeature, position);

    }

    private void beginProximityDetection(IMachineFeature machineFeature, int position){
        //pairingsController.featureAssigned(machineFeature);
        final Intent beginScanIntent = new Intent(DeviceScanCallback.BEGIN_SCAN);
        beginScanIntent.putExtra("position",position);
        LocalBroadcastManager.getInstance(context).sendBroadcast(beginScanIntent);


    }
/*    private boolean unpairDevice(IMachineFeature machineFeature){
        boolean retVal = false;
        if (null!=machineFeature.getSensor()) {
            pairingsController.featureUnpaired(machineFeature.getSensor().getMacAddress());
            retVal = true;
        }
        return retVal;
    }*/

    /*private void createAndShowDisconnectDialog(final IMachineFeature machineFeature) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Would you like to disconnect the selected feature?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                unpairDevice(machineFeature);
                machineFeature.setSensor(null);
                machineFeature.setState(BluetoothLeService.STATE_DISCONNECTED);
                notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }*/

    public IMachineFeature getItem(int position) {
        return pairingsController.getPairingModel().getFeatures().get(position);
    }

    class MachineComponentViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public ImageButton button;

        public MachineComponentViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(final Context context, final int position) {
            final Bucket pairingModel = pairingsController.getPairingModel();
            //final Bucket oldPairingModel = ConfigHelper.loadBucketConfig(context);
            if (null!=pairingModel) {
                final IMachineFeature machineFeature = pairingModel.getFeatures().get(position);
                //final IMachineFeature oldMachineFeature = oldPairingModel.getFeatures().get(position);
                //final String oldFeatureName = oldMachineFeature.getName();
                if (machineFeature != null && machineFeature.isBLEComponent()) {
                    final ISensor btDevice = machineFeature.getSensor();
                    title = (TextView) itemView.findViewById(R.id.mc_name);
                    button = (ImageButton) itemView.findViewById(R.id.mc_button);

                    if (null != btDevice) {
                        final String address = btDevice.getMacAddress();
                        if (null != pairingModel && pairingModel.containsAddress(address)) {
                            final int state = machineFeature.getState();
                            String featureName = machineFeature.getName();
                            switch (state) {
                                case MachineFeature.STATE_QUEUED:
                                    //featureName = resources.getString(R.string.action_connecting);
                                    featureName = title.getText().charAt(0) + ":" + machineFeature.getPosition();
                                    break;
                                case MachineFeature.STATE_ASSIGNED:
                                    if (null!=btDevice.getName()){
                                        featureName = btDevice.getName();
                                    }
                                    break;
                                default:
                                    featureName = title.getText().charAt(0) + ":" + machineFeature.getPosition();
                            }
                            machineFeature.setName(featureName);
                        }
                    }

                    title.setText(machineFeature.getName());
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                            /*Boolean isDetectingMode = prefs.getString("mode", RegistrationHelper.MODE_PRESELECTION)
                                    .equals(RegistrationHelper.MODE_DETECTION);*/

                            int featureType = pairingsController.getPairingModel().getFeatures().get(position).getFeatureType();
                            prefs.edit().putInt("feature_type",featureType).commit();
                            //handleFeatureClick(position,isDetectingMode);

                        }
                    });

                }
            }
        }
    }
}
