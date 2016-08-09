package com.escocorp.detectionDemo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class Bucket implements Parcelable {

    private String id;
    private String name;
    @SerializedName("orientation_type")
    private int orientationType;
    private int height;
    private int width;
    private int depth;
    private ArrayList<IMachineFeature> features = new ArrayList<>();

    public Bucket(){

    }

    protected Bucket(Parcel in) {
        id = in.readString();
        name = in.readString();
        orientationType = in.readInt();
        height = in.readInt();
        width = in.readInt();
        depth = in.readInt();
        in.readList(features, MachineFeature.class.getClassLoader());
    }

    public static final Creator<Bucket> CREATOR = new Creator<Bucket>() {
        @Override
        public Bucket createFromParcel(Parcel in) {
            return new Bucket(in);
        }

        @Override
        public Bucket[] newArray(int size) {
            return new Bucket[size];
        }
    };

    public static TypeToken<Bucket> getTypeToken() {
        return new TypeToken<Bucket>(){};
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrientationType() {
        return orientationType;
    }

    public void setOrientationType(int orientationType) {
        this.orientationType = orientationType;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public ArrayList<IMachineFeature> getFeatures() {
        return features;
    }

    public void setFeatures(ArrayList<IMachineFeature> features) {
        this.features = features;
    }

    public boolean containsAddress(String address){
        for (IMachineFeature feature:features){
            if (null!=feature.getSensor() && feature.getSensor().getMacAddress().equals(address)){
                return true;
            }
        }
        return false;
    }

    public IMachineFeature getFeature(String address){
        for (IMachineFeature feature:features){
            if (null!=feature.getSensor() && feature.getSensor().getMacAddress().equals(address)){
                return feature;
            }
        }
        return null;
    }

    public IMachineFeature getFeatureById(String id){
        for (IMachineFeature feature:features){
            if (null!=feature && feature.getId().equals(id)) {
                return feature;
            }
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(orientationType);
        dest.writeInt(height);
        dest.writeInt(width);
        dest.writeInt(depth);
        dest.writeTypedList(features);
    }
}
