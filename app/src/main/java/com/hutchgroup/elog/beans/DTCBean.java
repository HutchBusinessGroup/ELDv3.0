package com.hutchgroup.elog.beans;

/**
 * Created by Dev-1 on 11/3/2016.
 */

public class DTCBean {

    int _id, status, occurence, spn, fmi;
    String DateTime, spnDescription, fmiDescription, protocol;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getOccurence() {
        return occurence;
    }

    public void setOccurence(int occurence) {
        this.occurence = occurence;
    }

    public String getDateTime() {
        return DateTime;
    }

    public void setDateTime(String dateTime) {
        DateTime = dateTime;
    }

    public int getSpn() {
        return spn;
    }

    public void setSpn(int spn) {
        this.spn = spn;
    }

    public int getFmi() {
        return fmi;
    }

    public void setFmi(int fmi) {
        this.fmi = fmi;
    }

    public String getSpnDescription() {
        return spnDescription;
    }

    public void setSpnDescription(String spnDescription) {
        this.spnDescription = spnDescription;
    }

    public String getFmiDescription() {
        return fmiDescription;
    }

    public void setFmiDescription(String fmiDescription) {
        this.fmiDescription = fmiDescription;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
