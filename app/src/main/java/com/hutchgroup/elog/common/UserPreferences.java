package com.hutchgroup.elog.common;

public class UserPreferences {
    private static String timeZone;
    private static int currentRule;
    private static String unitNo;
    private static String startTime;

    public static String getTimeZone() {
        return timeZone;
    }

    public static void setTimeZone(String value) {
        timeZone = value;
    }

    public static int getCurrentRule() {
        return currentRule;
    }

    public static void setCurrentRule(int rule) {
        currentRule = rule;
    }

    public static String getUnitNo() {
        return unitNo;
    }

    public static void setUnitNo(String value) {
        unitNo = value;
    }

    public static String getStartTime() {
        return startTime;
    }

    public static void setStartTime(String time) {
        startTime = time;
    }

}

