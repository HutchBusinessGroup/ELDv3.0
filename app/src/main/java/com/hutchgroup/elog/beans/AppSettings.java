package com.hutchgroup.elog.beans;

import java.io.Serializable;
import java.util.ArrayList;

public class AppSettings implements Serializable {
    public enum AppOrientation {AUTO, PORTRAIT, LANSCAPE}

    public enum AppTimeFormat {HR12, HR24}

    public static int SYNC5 = 5;
    public static int SYNC10 = 10;
    public static int SYNC20 = 20;
    public static int SYNC30 = 30;
    public static int SYNC60 = 60;
    private static int Unit = 2;  //1-Metric, 2-Imperial
    private static int id;
    private static float timeZone;
    private static int defaultRule;
    private static int graphLine = 1; //0: end, 1: current
    private static int colorLineUS = 0xFF0000FF;
    private static int colorLineCanada = 0xFF008000;
    private static int timeFormat = 0; //0: 12 hrs, 1: 24hrs
    private static int violationReading = 1; //0: disable, 1: enable (default)
    private static int messageReading = 1; //0: disable, 1: enable (default)
    private static String startTime = "12"; //always 12AM
    private static int orientation = 0; //0: auto, 1: port, 2: landscape
    private static int visionMode = 0; //0: day, 1: night
    //private int brightness;
    private static int copyTrailer = 1; //0 disable, 1 enable
    private static int showViolation = 0; //0: off, 1: on
    private static int syncTime = SYNC5; //5, 10, 20, 30, 60 mins
    private static int automaticRuleChange = 0; //0: off, 1: on
    private static int violationOnGrid = 1; //0: off, 1: on
    private static int fontSize = 1; //0: small, 1: normal, 2: large
    private static int driverId;

    private static int dutyStatusReading = 0; //0: disable, 1: enable (default)

    public static int getDutyStatusReading() {
        return dutyStatusReading;
    }

    public static void setDutyStatusReading(int value) {
        dutyStatusReading = value;
    }

    public static int getFontSize() {
        return fontSize;
    }

    public static void setFontSize(int value) {
        fontSize = value;
    }

    public static int getDriverId() {
        return driverId;
    }

    public static void setDriverId(int value) {
        driverId = value;
    }

    public static int getViolationOnGrid() {
        return violationOnGrid;
    }

    public static void setViolationOnGrid(int value) {
        violationOnGrid = value;
    }

    public static int getId() {
        return id;
    }

    public static void setId(int value) {
        id = value;
    }

    public static float getTimeZone() {
        return timeZone;
    }

    public static void setTimeZone(float value) {
        timeZone = value;
    }

    public static int getDefaultRule() {
        return defaultRule;
    }

    public static void setDefaultRule(int value) {
        defaultRule = value;
    }

    public static int getGraphLine() {
        return graphLine;
    }

    public static void setGraphLine(int value) {
        graphLine = value;
    }

    public static int getColorLineUS() {
        return colorLineUS;
    }

    public static void setColorLineUS(int value) {
        colorLineUS = value;
    }

    public static int getColorLineCanada() {
        return colorLineCanada;
    }

    public static void setColorLineCanada(int value) {
        colorLineCanada = value;
    }

    public static int getTimeFormat() {
        return timeFormat;
    }

    public static void setTimeFormat(int value) {
        timeFormat = value;
    }

    public static int getViolationReading() {
        return violationReading;
    }

    public static void setViolationReading(int value) {
        violationReading = value;
    }

    public static int getMessageReading() {
        return messageReading;
    }

    public static void setMessageReading(int value) {
        messageReading = value;
    }

    public static int getOrientation() {
        return orientation;
    }

    public static void setOrientation(int value) {
        orientation = value;
    }

    public static int getVisionMode() {
        return visionMode;
    }

    public static void setVisionMode(int value) {
        visionMode = value;
    }

//	public int getBrightness() {
//		return brightness;
//	}
//
//	public void setBrightness(int value) {
//		brightness = value;
//	}

    public static int getCopyTrailer() {
        return copyTrailer;
    }

    public static void setCopyTrailer(int value) {
        copyTrailer = value;
    }

    public static int getShowViolation() {
        return showViolation;
    }

    public static void setShowViolation(int value) {
        showViolation = value;
    }

    public static int getSyncTime() {
        return syncTime;
    }

    public static void setSyncTime(int value) {
        syncTime = value;
    }

    public static String getStartTime() {
        return startTime;
    }

    public static void setStartTime(String value) {
        startTime = value;
    }

    public static int getAutomaticRuleChange() {
        return automaticRuleChange;
    }

    public static void setAutomaticRuleChange(int value) {
        automaticRuleChange = value;
    }

    public static int getUnit() {
        return Unit;
    }

    public static void setUnit(int unit) {
        Unit = unit;
    }
}
