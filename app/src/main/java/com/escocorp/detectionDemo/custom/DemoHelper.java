package com.escocorp.detectionDemo.custom;

import java.util.Random;

public class DemoHelper {

    public static final String[] locations = new String[] {
            //"North East Lay Down Yard",
            //"South Lay Down Yard #3",
            "Shovel #44"};
    public static final String[] usageOptions = new String[] {
            "135 Hours",
            "600 Hours",
            "0 Hours",
            "147 Hours",
            "151 Hours",
            "135 Hours"};
    public static final String[] commissionDates = new String[] {
            "January 13, 2016",
            "December 13, 2015",
            "January 14, 2016",
            "January 11, 2016",
            "January 13, 2016"};
    public static final String[] dummyPartDescriptions = new String[] {
            "92TKTVP Point",
            "112TKTS Point",
            "N7V2 Point",
            "U65AP Point",
            "U55H_12C Point"};

    public static String generateRandomStringFromArray(String[] stringArray){

        Random random = new Random();
        int randomInt = random.nextInt(stringArray.length);
        return stringArray[randomInt];
    }

    public static String generateRandomPMID(){
        Random random = new Random();
        int randomInt = random.nextInt(5);
        return dummyPartDescriptions[randomInt];
    }
}
