package com.escocorp.detectionDemo;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class RangeAlert {
    private HashMap<String, Point3D> mRangeValues = new HashMap<String, Point3D>();
    private Point3D mRangeAverage;

    public final double threshold = 0.40f;

    private Set<String> exclude = new TreeSet<String>();

    public void add(String address, Point3D point) {
        mRangeValues.put(address, point);
    }


    public void remove(String address) {
        mRangeAverage = null;
        mRangeValues.remove(address);
        exclude.remove(address);
    }

    public void removeAll() {
        mRangeValues.clear();
        mRangeAverage = null;

        exclude.clear();
    }

    public void exclude(String address) {
        exclude.add(address);
    }

    // TODO change name
    public void include(String address) {
        exclude.remove(address);
    }

    public void reset() {
        exclude.clear();
    }

    public boolean isExcluded(String address) {
        return exclude.contains(address);
    }

    public Point3D getRangeAverage() {
        return mRangeAverage;
    }

    public void average() {
        final int size = mRangeValues.size() - exclude.size();
        if(size <= 0) return;

        Point3D average = new Point3D(0,0,0);

        // only include non excluded values to average
        for(String key : mRangeValues.keySet()) {
            if(!isExcluded(key)) {
                Point3D data = mRangeValues.get(key);
                average.x += data.x;
                average.y += data.y;
                average.z += data.z;
            }
        }
        average.x /= size;
        average.y /= size;
        average.z /= size;

        mRangeAverage = average;
    }

    public boolean checkAddressOutOfRange(String address) {
        if(mRangeAverage == null || exclude.contains(address)) {
            return false;
        }
        final Point3D point = mRangeValues.get(address);
        if(point == null)
            return false;

        return checkPointOutRange(point);
    }

    public boolean checkOutOfRange() {
        if(mRangeAverage == null) {
            return false;
        }

        return exclude.size() > 0;
      /*  for(Point3D point : mRangeValues.values()) {
            if(checkPointOutRange(point))
                return true;
        }*/

        //return false;
    }

    boolean checkPointOutRange(final Point3D point) {
        return     checkOutOfRange(point.x, mRangeAverage.x, threshold)
                || checkOutOfRange(point.y, mRangeAverage.y, threshold)
                || checkOutOfRange(point.z, mRangeAverage.z, threshold);
    }

    static boolean checkOutOfRange(double value, double range, double threshold) {
        if( value > (range + threshold) || value < (range - threshold) ) {
            return true;
        }
        return false;
    }
}
