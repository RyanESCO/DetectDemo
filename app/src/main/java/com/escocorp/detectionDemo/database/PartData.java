package com.escocorp.detectionDemo.database;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Ryan Dymock on 8/9/2016.
 */
public class PartData {

    /*public static ArrayList<DemoParts> partsArrayList = new ArrayList<DemoParts>();*/

    public static int getNumParts(){
        return initialDeviceNameArray.length;
    }

    public static int getImageId(Context context, String imageName) {
        return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
    }

    public static void initializeSensorData(Context context){
        SharedPreferences prefs = context.getSharedPreferences("sensorArray",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        for(int i = 0; i < getNumParts(); i ++){
            editor.putString(initialDeviceNameArray[i], initialMacAddressArray[i]);
        }

        editor.commit();

    }



    public static final String[] initialDeviceNameArray = {
            "ESCO#00222",
            "ESCO#00191",
            "ESCO#00232",
            "ESCO#00218",
            "ESCO#00238",
            "ESCO#00243",
            "ESCO#00245"};

    public static final String[] initialMacAddressArray = {
            "00:07:80:15:2E:32",
            "00:07:80:15:2E:11",
            "00:07:80:15:41:EA",
            "00:07:80:C0:6E:70",
            "00:07:80:15:2E:1F",
            "00:07:80:15:2E:3E",
            "00:07:80:C0:6E:56"};

    public static final String[] imageArray = {
            "point_hq",
            "shroud_hq",
            "point_hq",
            "shroud_hq",
            "point_hq",
            "lower_wing_shroud_hq",
            "wing_shroud_hq"
    };

    public static final String[] productTypeArray = {
            "N3R Point",
            "Shroud",
            "N3R Point",
            "Shroud",
            "N3R Point",
            "Wing Shroud",
            "Wing Shroud"};

    public static final String[] installationDateArray = {
            "9/26/2016",
            "9/12/2016",
            "9/26/2016",
            "9/12/2016",
            "9/26/2016",
            "8/31/2016",
            "8/31/2016"
            };
    
    public static final String[] usageArray= {
            "8",
            "180",
            "8",
            "180",
            "8",
            "260",
            "260"};

/*    public static final String[] initialDeviceNameArray = {
            "ESCO#00191",
            "ESCO#00238",
            "ESCO#00243",
            "ESCO#00245",
            "ESCO#00248"};

    public static final String[] initialMacAddressArray = {
            "00:07:80:15:2E:11",
            "00:07:80:15:2E:1F",
            "00:07:80:15:2E:3E",
            "00:07:80:15:6E:56",
            "00:07:80:15:37:C9"};

    public static final String[] productTypeArray = {
            "Wing Shroud",
            "N3R Point",
            "Shroud",
            "N3R Point",
            "Shroud",
            "Wing Shroud"};

    public static final String[] installationDateArray = {
            "Installed August 21, 2016",
            "Installed August 29, 2016",
            "Installed August 21, 2016",
            "Installed August 21, 2016",
            "Installed August 21, 2016"
    };

    public static final String[] usageArray= {
            "180 Hours",
            "133 Hours",
            "180 Hours",
            "133 Hours",
            "180 Hours"};*/

}
