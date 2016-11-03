package com.hutchgroup.elog.beans;

/**
 * Created by Dev-1 on 7/20/2016.
 */
public class GPSData {
    public static int PostRoadSpeed = 0;
    public static long LastStatusTime = System.currentTimeMillis();
    // time in minutes
    public static int DrivingTimeRemaining;
    public static int WorkShiftRemaining;
    public static int TimeRemaining70;
    public static int TimeRemaining120;
    public static int TimeRemainingUS70;
    public static int CurrentStatusRemaining;
    public static int TimeRemainingReset;
    public static int ETATimeRemaining;
    public static int CurrentStatus = 1;
    public static String StatusCode = "1";
    public static int ACPowerFg;
    public static int TripInspectionCompletedFg;
    public static int NoHOSViolationFgFg = 1;
    public static int CellOnlineFg;

    public static int RoamingFg;
    public static int DTCOnFg;
    public static int WifiOnFg;
    public static int TPMSWarningOnFg;


}
