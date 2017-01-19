package com.hutchgroup.elog.beans;

import java.io.Serializable;
import java.util.ArrayList;

public class SettingsBean implements Serializable {
    private int Unit = 2; //1-Metric, 2-Imperial
    private int id;
    private float timeZone;
    private int defaultRule;
    private int graphLine; //0: current time, 1: end
    private int colorLineUS;
    private int colorLineCanada;
    private int timeFormat = 0; //0: 12 hrs, 1: 24hrs
    private int violationReading = 1; //0: disable, 1: enable (default)
    private int messageReading; //0: disable, 1: enable (default)
    private String startTime = "12"; //always 12AM
    private int orientation = 0; //0: auto, 1: port, 2: landscape
    private int visionMode; //0: day, 1: night
    //private int brightness;
    private int copyTrailer = 1; //0 disable, 1 enable
    private int showViolation = 0; //0: off, 1: on
    private int syncTime = AppSettings.SYNC5; //5, 10, 20, 30, 60 mins
    private int automaticRuleChange = 0;
    private int violationOnGrid; //0: off, 1: on
    private int fontSize = 1; //0: small, 1: normal, 2: large
    private int driverId;
    private int dutyStatusReading = 1; //0: disable, 1: enable (default)

    public int getDutyStatusReading() {
        return dutyStatusReading;
    }

    public void setDutyStatusReading(int value) {
        dutyStatusReading = value;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int value) {
        fontSize = value;
    }


    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int value) {
        driverId = value;
    }

    public int getViolationOnGrid() {
        return violationOnGrid;
    }

    public void setViolationOnGrid(int value) {
        violationOnGrid = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int value) {
        id = value;
    }

    public float getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(float value) {
        timeZone = value;
    }

    public int getDefaultRule() {
        return defaultRule;
    }

    public void setDefaultRule(int value) {
        defaultRule = value;
    }

    public int getGraphLine() {
        return graphLine;
    }

    public void setGraphLine(int value) {
        graphLine = value;
    }

    public int getColorLineUS() {
        return colorLineUS;
    }

    public void setColorLineUS(int value) {
        colorLineUS = value;
    }

    public int getColorLineCanada() {
        return colorLineCanada;
    }

    public void setColorLineCanada(int value) {
        colorLineCanada = value;
    }

    public int getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(int value) {
        timeFormat = value;
    }

    public int getViolationReading() {
        return violationReading;
    }

    public void setViolationReading(int value) {
        violationReading = value;
    }

    public int getMessageReading() {
        return messageReading;
    }

    public void setMessageReading(int value) {
        messageReading = value;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int value) {
        orientation = value;
    }

    public int getVisionMode() {
        return visionMode;
    }

    public void setVisionMode(int value) {
        visionMode = value;
    }

//	public int getBrightness() {
//		return brightness;
//	}
//
//	public void setBrightness(int value) {
//		brightness = value;
//	}

    public int getCopyTrailer() {
        return copyTrailer;
    }

    public void setCopyTrailer(int value) {
        copyTrailer = value;
    }

    public int getShowViolation() {
        return showViolation;
    }

    public void setShowViolation(int value) {
        showViolation = value;
    }

    public int getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(int value) {
        syncTime = value;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String value) {
        startTime = value;
    }

    public int getAutomaticRuleChange() {
        return automaticRuleChange;
    }

    public void setAutomaticRuleChange(int value) {
        automaticRuleChange = value;
    }

    public int getUnit() {
        return Unit;
    }

    public void setUnit(int unit) {
        Unit = unit;
    }
}
