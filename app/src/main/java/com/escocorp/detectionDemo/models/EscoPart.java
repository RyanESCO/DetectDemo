package com.escocorp.detectionDemo.models;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.escocorp.detectionDemo.Point3D;
import com.escocorp.detectionDemo.R;
import com.escocorp.detectionDemo.custom.DemoHelper;
import com.escocorp.detectionDemo.custom.PartsDefinitions;

public class EscoPart implements Parcelable {
    public String id= "";
    private String name= "";
    public String details= "";
    public String address = "";
    public String commissionDate = "";
    public String usageHours = "";

    public String description = "";
    public String agileNumber ="";

    public int drawableID;

    public String location = "";
    public String PMID;
    public int rssi;
    public int battery;
    public float temperature;
    public int counter = 999;
    public float maxAcceleration;
    public int partType;

    public static String SENSOR_NAME_TYPE_DELIMITER = "::";


    // sensor data
    public Point3D acceleration = new Point3D(0,0,0);

    // group id
    public String group = "";

    public int flag = Color.WHITE;

    public EscoPart(String id, String name) {
        this.id = id;
        this.name = name;
        setRandomDataForDemo();
        setDrawableID();
    }

    private void setRandomDataForDemo(){
        //this.details = DemoHelper.generateRandomStringFromArray(DemoHelper.dummyPartDescriptions);
        this.commissionDate = DemoHelper.generateRandomStringFromArray(DemoHelper.commissionDates);
        this.usageHours = DemoHelper.generateRandomStringFromArray(DemoHelper.usageOptions);
        this.location = DemoHelper.generateRandomStringFromArray(DemoHelper.locations);
        //this.PMID = DemoHelper.generateRandomPMID();
        //this.PMID = "U45S";
    }

    public EscoPart(){
        setRandomDataForDemo();
    }

    public String getName() {
        //strip off the sensor type from the useable name
        if (null!=name && name.contains(SENSOR_NAME_TYPE_DELIMITER)){
            return name.substring(0,name.indexOf(SENSOR_NAME_TYPE_DELIMITER));
        }
        return name;
    }

    private void setDrawableID(){
        switch(this.PMID){
            case("N1S"):
                this.drawableID = R.drawable.n1s;
                break;
            case("U45S"):
                this.drawableID = R.drawable.u45s;
                break;
            case("U20T"):
                this.drawableID = R.drawable.u20t;
                break;
            case("N3R"):
                this.drawableID = R.drawable.n3r;
                break;
            case("TAC50X345-4R"):
                this.drawableID = R.drawable.tac50x345_4r;
                break;
            case("TAW40X600"):
                this.drawableID = R.drawable.taw40x600;
                break;
            default:
                this.drawableID = R.drawable.n1s;
                break;

        }

        if(partType==PartsDefinitions.TYPE_POD){
            this.drawableID = R.drawable.bucket;
        }

    }

    public void setName(String name){
        int pos;
        this.name = name;
        if (null!=name && (pos = name.indexOf(SENSOR_NAME_TYPE_DELIMITER))>0){
            final String typeStr = name.substring(pos+2, name.length());
            if (typeStr.equals("(SHROUD)")){
                partType = PartsDefinitions.TYPE_SHROUD;
            } else if (typeStr.equals("(WINGSHROUD)")) {
                partType = PartsDefinitions.TYPE_SHROUD;
            } else if (typeStr.equals("(BUCKET)")) {
                partType = PartsDefinitions.TYPE_POD;
            } else if (typeStr.equals("(DEV)")) {
                partType = PartsDefinitions.TYPE_DEV;
            } else {
                partType = PartsDefinitions.TYPE_POINT;
            }
        }

        switch (getName()){
            case "ESCO#00076":
                this.PMID = "N3R";
                this.details = "POINT";
                this.description = "Nemisys N3 Rock Point";
                this.agileNumber = "4212935";
                break;
            case "ESCO#00083":
                this.PMID = "N3R";
                this.details = "POINT";
                this.description = "Nemisys N3 Rock Point";
                this.agileNumber = "4212935";
                break;
            case "ESCO#00090":
                this.PMID = "SN# RH102303";
                this.details = "";
                this.description = "Hoe, Komatsu PC300/350/360/390, 2.19yd";
                this.agileNumber = "4196782";
                break;
            case "ESCO#00056":
                this.PMID = "N1S";
                this.details = "POINT";
                this.description = "Nemisys N1 Standard Point";
                this.agileNumber = "4210500";
                break;
            case "ESCO#00093":
                this.PMID = "U20T";
                this.details = "POINT";
                this.description = "Ultralok Twin Pick Point";
                this.agileNumber = "4183099";
                break;
            case "ESCO#00092":
                this.PMID = "TAC50X345-4R";
                this.details = "SHROUD";
                this.description = "Toplok Lip Shroud, RH";
                this.agileNumber = "5125187";
                break;
            case "ESCO#00091":
                this.PMID = "TAW40X600";
                this.details = "SHROUD";
                this.description = "Toplok Wing Shroud";
                this.agileNumber = "4134557";
                break;
            case "ESCO#00094":
                this.PMID = "N1S";
                this.details = "POINT";
                this.description = "Nemisys N1 Standard Point";
                this.agileNumber = "4210500";
                break;
            case "ESCO#00999":
                this.PMID = "RH102303";
                this.details = "HOE";
                this.description = "Komatsu PC350 Hoe Bucket";
                this.agileNumber = "4196782";
                break;
            default:
                this.PMID = "N1S";
                this.details = "POINT";
                this.description = "Nemisys N1 Standard Point";
                this.agileNumber = "4210501";
                break;

        }
        setDrawableID();
    }

    @Override
    public String toString() {
        return name;
    }



    protected EscoPart(Parcel in) {
        id = in.readString();
        name = in.readString();
        details = in.readString();
        address = in.readString();
        commissionDate = in.readString();
        usageHours = in.readString();
        drawableID = in.readInt();
        location = in.readString();
        PMID = in.readString();
        rssi = in.readInt();
        battery = in.readInt();
        temperature = in.readFloat();
        counter = in.readInt();
        maxAcceleration = in.readFloat();
        partType = in.readInt();
        acceleration = (Point3D) in.readValue(Point3D.class.getClassLoader());
        group = in.readString();
        flag = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(details);
        dest.writeString(address);
        dest.writeString(commissionDate);
        dest.writeString(usageHours);
        dest.writeInt(drawableID);
        dest.writeString(location);
        dest.writeString(PMID);
        dest.writeInt(rssi);
        dest.writeInt(battery);
        dest.writeFloat(temperature);
        dest.writeInt(counter);
        dest.writeFloat(maxAcceleration);
        dest.writeInt(partType);
        dest.writeValue(acceleration);
        dest.writeString(group);
        dest.writeInt(flag);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<EscoPart> CREATOR = new Parcelable.Creator<EscoPart>() {
        @Override
        public EscoPart createFromParcel(Parcel in) {
            return new EscoPart(in);
        }

        @Override
        public EscoPart[] newArray(int size) {
            return new EscoPart[size];
        }
    };
}