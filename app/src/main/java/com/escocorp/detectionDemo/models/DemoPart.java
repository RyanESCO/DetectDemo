package com.escocorp.detectionDemo.models;

import com.escocorp.detectionDemo.database.PartData;

/**
 * Created by Ryan Dymock on 8/9/2016.
 */
public class DemoPart {
    private String deviceName;
    private String productType;
    private int position;
    private String installationDate;
    private String usage;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String name){
        deviceName = name;
    }

    public String getInstallationDate() {
        return installationDate;
    }

    public int getPosition() {
        return position;
    }

    public String getProductType() {
        return productType;
    }

    public String getUsage() {
        return usage;
    }

    public DemoPart(int index){
        this.deviceName = PartData.initialDeviceNameArray[index];
        this.productType = PartData.productTypeArray[index];
        this.installationDate = PartData.installationDateArray[index];
        this.usage = PartData.usageArray[index];
        this.position = index;
    }

    public void changeDeviceName(String newName){
        this.deviceName = newName;
    }
}
