package com.escocorp.detectionDemo.database;

import org.json.JSONObject;

import java.util.TimeZone;

/**
 * Created by Magical on 7/7/2015.
 */
public class BTLostTooth {
    public long eventTimestamp;
    public String eventTZ;

    public String emitterId;
    public String eventType;

    // Bluetooth raw data
    public String btDeviceMac;
    String btDeviceName;
    public String btDeviceStatus;
    public int btRssi;

    public BTLostTooth() {}

    public BTLostTooth(String deviceMac, String deviceName, int rssi) {
        this.emitterId = "emitter_1";
        this.eventTZ = TimeZone.getDefault().getID();
        this.eventTimestamp = System.currentTimeMillis();
        this.eventType = "bt";

        this.btDeviceMac = deviceMac;
        this.btDeviceName = deviceName;
        this.btRssi = rssi;
        this.btDeviceStatus = "lost";
    }

    /**
     * * Return event as raw json
     * * @return
     * * @throws Exception
     * */
    public JSONObject getJSON() throws Exception {
        JSONObject object = new JSONObject();
        // event header
        object.put("version", 1);
        object.put("emitterId", this.emitterId);
        object.put("eventTimestamp", this.eventTimestamp);
        object.put("eventTZ", this.eventTZ);
        object.put("eventType", this.eventType);

        // event payload (BT data)
        object.put("btDeviceMac", this.btDeviceMac);
        object.put("btDeviceName", this.btDeviceName);
        object.put("btDeviceStatus", this.btDeviceStatus);
        object.put("btRssi", this.btRssi);

        return object;
        }
}
