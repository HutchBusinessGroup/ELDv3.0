package com.hutchgroup.elog.beans;

/**
 * Created by Deepak on 11/30/2016.
 */

public class AlertBean {
    int _id, SyncFg;
    String AlertName, AlertDateTime, AlertCode;

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
}
