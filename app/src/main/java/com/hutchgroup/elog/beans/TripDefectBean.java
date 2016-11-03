package com.hutchgroup.elog.beans;

/**
 * Created by Deepak.Sharma on 10/23/2015.
 */
public class TripDefectBean {
    public int getDefectId() {
        return defectId;
    }

    public void setDefectId(int defectId) {
        this.defectId = defectId;
    }

    public String getDefect() {
        return defect;
    }

    public void setDefect(String defect) {
        this.defect = defect;
    }

    private int defectId;
    private String defect;
}
