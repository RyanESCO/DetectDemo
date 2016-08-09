package com.escocorp.detectionDemo.models;

import android.bluetooth.le.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

import com.escocorp.detectionDemo.DeviceScanCallback;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class Sensor implements ISensor, Parcelable {

    public static String SENSOR_NAME_TYPE_DELIMITER = "::";

    @SerializedName("mac_address")
    private String macAddress;
    //don't serialize following variables
    private transient String name;
    private transient int bluetoothType;
    private transient int featureType = 0;
    private int rssi;
    private int averageRssi;
    private ArrayList<Integer> rssiHistory = new ArrayList<>();

    public Sensor(){}

    /*public Sensor(BluetoothDevice bleDevice){
        this.macAddress = bleDevice.getAddress();
        this.bluetoothType = bleDevice.getType();
        setName(bleDevice.getName());
    }*/

    /*//TO DO: remove constructor, used for testing
    public Sensor(String address, String name, int signalStrength){
        this.macAddress = address;
        this.name = name;
        setRssi(signalStrength);
    }*/

    public Sensor(ScanResult result){
        this.macAddress = result.getDevice().getAddress();
        setName(result.getDevice().getName());
        setByteString(result.getScanRecord().getBytes());

        if(name==null){
            name="UNKNOWN";
        }
        setRssi(result.getRssi());

    }

    public void setRssi(int rssi){
        this.rssi = rssi;
        rssiHistory.add(rssi);
        averageRssi = calculateAverageRssi();
    }

    public int calculateAverageRssi(){
        int sum = 0;
        for(Integer i:rssiHistory){
            sum = sum + i;
        }
        if(rssiHistory.size()==0 || sum==0){
            return -100;
        }
        return sum/rssiHistory.size();
    }

    public int getAverageRssi(){
        return averageRssi;
    }

    public int getRssi(){
        return rssi;
    }

    public static TypeToken<Sensor> getTypeToken() {
        return new TypeToken<Sensor>(){};
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String mac_address) {
        this.macAddress = mac_address;
    }

    public String getName() {
        //strip off the sensor type from the useable name
        if (null!=name && name.contains(SENSOR_NAME_TYPE_DELIMITER)){
            return name.substring(0,name.indexOf(SENSOR_NAME_TYPE_DELIMITER));
        }
        return name;
    }

    public void setByteString(byte[] bytes){
        int length;
        String type = "";
        String bytesString = "";
        String bytesSubString = "";
        int currentPos = 0;
        String name = "";

        for(int i = 0; i < bytes.length;i++){
            bytesString += String.valueOf(bytes[i]) + " ";
        }

        while (currentPos < bytes.length) {
            String lengthString = String.valueOf(bytes[currentPos]);
            length = bytes[currentPos++] & 0xFF;
            if (length == 0) {
                break;
            }
            // Note the length includes the length of the field type itself.
            int dataLength = length - 1;
            // fieldType is unsigned int.
            String fieldTypeString = String.valueOf(bytes[currentPos]);
            int fieldType = bytes[currentPos++] & 0xFF;
            switch (fieldType) {
                case DeviceScanCallback.DATA_TYPE_FLAGS:
                    type = "Flags";
                    break;
                case DeviceScanCallback.DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL:
                case DeviceScanCallback.DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE:
                    type = "Service UUID 16 Bit";
                    break;
                case DeviceScanCallback.DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL:
                case DeviceScanCallback.DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE:
                    type = "Service UUID 32 Bit";
                    break;
                case DeviceScanCallback.DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL:
                case DeviceScanCallback.DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE:
                    type = "Service UUID 128 Bit";
                    break;
                case DeviceScanCallback.DATA_TYPE_LOCAL_NAME_SHORT:
                case DeviceScanCallback.DATA_TYPE_LOCAL_NAME_COMPLETE:
                    name = new String(DeviceScanCallback.extractBytes(bytes, currentPos, dataLength));
                    this.name = name;
                    type = "Local Name: " + name;
                    break;
                case DeviceScanCallback.DATA_TYPE_TX_POWER_LEVEL:
                    type = "TX POWER LEVEL";
                    break;
                case DeviceScanCallback.DATA_TYPE_SERVICE_DATA:
                    type = "Service Data";
                    break;
                case DeviceScanCallback.DATA_TYPE_MANUFACTURER_SPECIFIC_DATA:
                    type = "Manufacturing Specific Data";
                    /*if(Integer.parseInt(lengthString)<5 ){
                        this.wakeState = WAKE_STATE_ASLEEP;
                    } else {
                        this.wakeState = WAKE_STATE_ACTIVE;
                    }*/
                    break;
                default:
                    // Just ignore, we don't handle such data type.
                    break;
            }

            bytesSubString = type + "\nLength: " + lengthString + "\nData: ";

            int endPos = currentPos + dataLength;
            while (currentPos < endPos){
                bytesSubString += (String.valueOf(bytes[currentPos++])+" ");
            }

            bytesString += ("\n" + bytesSubString + "\n");
        }
    }

    public void setName(String name) {
        int pos;
        this.name = name;
        if (null!=name && (pos = name.indexOf(SENSOR_NAME_TYPE_DELIMITER))>0){
            final String typeStr = name.substring(pos+2, name.length());
            if (typeStr.equals("(SHROUD)")){
                setFeatureType(MachineFeature.FEATURE_TYPE_SHROUD);
            } else if (typeStr.equals("(WSHROUD)")) {
                setFeatureType(MachineFeature.FEATURE_TYPE_WING_SHROUD);
            } else if (typeStr.equals("(BUCKET)")) {
                setFeatureType(MachineFeature.FEATURE_TYPE_BUCKET_MONITOR);
            } else {
                setFeatureType(MachineFeature.FEATURE_TYPE_TOOTH);
            }
        }

    }

    @Override
    public int getType() {
        return bluetoothType;
    }

    @Override
    public void setType(int bluetoothType) {
        this.bluetoothType = bluetoothType;
    }

    @Override
    public int getFeatureType() {
        return featureType;
    }

    @Override
    public void setFeatureType(int featureType) {
        this.featureType = featureType;
    }

    protected Sensor(Parcel in) {
        macAddress = in.readString();
        name = in.readString();
        bluetoothType = in.readInt();
        featureType = in.readInt();
        rssi = in.readInt();
        averageRssi = in.readInt();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(macAddress);
        dest.writeString(name);
        dest.writeInt(bluetoothType);
        dest.writeInt(featureType);
        dest.writeInt(rssi);
        dest.writeInt(averageRssi);
    }

    @SuppressWarnings("unused")
    public static final Creator<Sensor> CREATOR = new Creator<Sensor>() {
        @Override
        public Sensor createFromParcel(Parcel in) {
            return new Sensor(in);
        }

        @Override
        public Sensor[] newArray(int size) {
            return new Sensor[size];
        }
    };
}
