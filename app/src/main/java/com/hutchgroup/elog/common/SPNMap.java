package com.hutchgroup.elog.common;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jake Whiteley on 3/24/2015.
 */


public class SPNMap {
    /* package */ static String[] map;
    /* package */ static String[] fmi = {
            "Above Normal Operational Range - Severe",
            "Below Normal Operational Range - Severe",
            "Data Erratic, or Incorrect",
            "Voltage Above Normal",
            "Voltage Below Normal",
            "Current Below Normal",
            "Current Above Normal",
            "Mechanical System Not Responding",
            "Abnormal Frequency, Pulse Width, or Period",
            "Abnormal Update Rate",
            "Abnormal Rate of Change",
            "Root Cause Unknown",
            "Bad Intelligent Device or Component",
            "Out of Calibration",
            "Special Instructions",
            "Above Normal Operational Range - Least",
            "Above Normal Operational Range - Moderate",
            "Below Normal Operational Range - Least",
            "Below Normal Operational Range - Moderate",
            "Received Network Data In Error",
            "Reserved",
            "Reserved",
            "Reserved",
            "Reserved",
            "Reserved",
            "Reserved",
            "Reserved",
            "Reserved",
            "Reserved",
            "Reserved",
            "Reserved",
            "Condition Exists"
    };


    /* package */
    public SPNMap(Context context) {
        try {
            BufferedReader stream = new BufferedReader(new InputStreamReader(context.getAssets().open("SPNMap.txt")));
            map = new String[7576];
            String str;
            Matcher m;
            Pattern p = Pattern.compile(",");
            while ((str = stream.readLine()) != null) {
                String[] items = p.split(str);
                map[Integer.parseInt(items[0])] = items[1];
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}