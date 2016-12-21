package com.hutchgroup.elog.beans;

/**
 * Created by Deepak on 11/30/2016.
 */

public class AlertBean {
    int _id, SyncFg, Duration, Scores, DriverId;
    String AlertName, AlertDateTime, AlertCode, currentValue,Threshold;


    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getSyncFg() {
        return SyncFg;
    }

    public void setSyncFg(int syncFg) {
        SyncFg = syncFg;
    }

    public String getAlertName() {
        return AlertName;
    }

    public void setAlertName(String alertName) {
        AlertName = alertName;
    }

    public String getAlertDateTime() {
        return AlertDateTime;
    }

    public void setAlertDateTime(String alertDateTime) {
        AlertDateTime = alertDateTime;
    }

    public String getAlertCode() {
        return AlertCode;
    }

    public void setAlertCode(String alertCode) {
        AlertCode = alertCode;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int duration) {
        Duration = duration;
    }

    public int getScores() {
        return Scores;
    }

    public void setScores(int scores) {
        Scores = scores;
    }

    public int getDriverId() {
        return DriverId;
    }

    public void setDriverId(int driverId) {
        DriverId = driverId;
    }

    public String getThreshold() {
        return Threshold;
    }

    public void setThreshold(String threshold) {
        Threshold = threshold;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }
}
