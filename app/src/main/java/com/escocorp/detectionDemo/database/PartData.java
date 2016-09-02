package com.escocorp.detectionDemo.database;

import com.escocorp.detectionDemo.models.DemoPart;

import java.util.ArrayList;

/**
 * Created by Ryan Dymock on 8/9/2016.
 */
public class PartData {

    /*public static ArrayList<DemoParts> partsArrayList = new ArrayList<DemoParts>();*/

    public static int getNumParts(){
        return deviceNameArray.length;
    }

    public static int getPositionFromMacAddress(String macAddress){
        for(int i=0; i < getNumParts();i++){
            if(macAddress.equals(macAddressArray[i])){
                return i;
            }
        }
        return -1;
    }

    public static final String[] deviceNameArray = {
            "ESCO#00248",
            "ESCO#00191",
            "ESCO#00243",
            "ESCO#00998",
            "ESCO#00214",
            "ESCO#00235",
            "ESCO#00999"};

    public static final String[] macAddressArray = {
            "00:07:80:15:37:C9",
            "00:07:80:15:2E:11",
            "00:07:80:15:2E:3E",
            "00:07:80:15:XX:XX",
            "00:07:80:15:2E:3D",
            "00:07:80:15:37:B0",
            "00:07:80:15:ZZ:ZZ"};

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

/*    public static final String[] deviceNameArray = {
            "ESCO#00191",
            "ESCO#00238",
            "ESCO#00243",
            "ESCO#00245",
            "ESCO#00248"};

    public static final String[] macAddressArray = {
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
