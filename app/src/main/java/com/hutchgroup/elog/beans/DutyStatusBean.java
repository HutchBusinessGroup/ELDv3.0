package com.hutchgroup.elog.beans;

import com.hutchgroup.elog.common.Utility;

import java.util.Comparator;

/**
 * Created by Deepak.Sharma on 7/15/2015.
 */
public class DutyStatusBean {
    public boolean isStatusFg() {
        return statusFg;
    }

    public void setStatusFg(boolean statusFg) {
        this.statusFg = statusFg;
    }

    boolean statusFg;
    private long dailyLogId;

    public long getDailyLogId() {
        return dailyLogId;
    }

    public void setDailyLogId(long dailyLogId) {
        this.dailyLogId = dailyLogId;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public int getLastOdometer() {
        return lastOdometer;
    }

    public void setLastOdometer(int lastOdometer) {
        this.lastOdometer = lastOdometer;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    private int driverId;
    private int status;
    private int totalMinutes;
    private int lastOdometer;
    private int createdBy;
    private int modifiedBy;
    private int statusId;

    public int getSyncFg() {
        return syncFg;
    }

    public void setSyncFg(int syncFg) {
        this.syncFg = syncFg;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    private int syncFg;
    private String startTime;
    private String endTime;
    private String createdDate;

    public String getDutyStatusTime() {
        return dutyStatusTime;
    }

    public void setDutyStatusTime(String dutyStatusTime) {
        this.dutyStatusTime = dutyStatusTime;
    }

    private String dutyStatusTime;

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public int getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    private String modifiedDate;

    public int getPersonalUse() {
        return personalUse;
    }

    public void setPersonalUse(int personalUse) {
        this.personalUse = personalUse;
    }

    private int personalUse;
    /*Comparator for sorting the list by dateAsc*/
    public static Comparator<DutyStatusBean> dateAsc = new Comparator<DutyStatusBean>() {
        public int compare(DutyStatusBean s1, DutyStatusBean s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (Utility.parse(s1.getStartTime()).getTime() / (1000));
                date2 = (int) (Utility.parse(s2.getStartTime()).getTime() / (1000));

            } catch (Exception exe) {
            }

            return date1 - date2;
        }
    };

    /*Comparator for sorting the list by dateDesc*/
    public static Comparator<DutyStatusBean> dateDesc = new Comparator<DutyStatusBean>() {
        public int compare(DutyStatusBean s1, DutyStatusBean s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (Utility.parse(s1.getStartTime()).getTime() / (1000));
                date2 = (int) (Utility.parse(s2.getStartTime()).getTime() / (1000));

            } catch (Exception exe) {
            }

            return date2 - date1;
        }
    };
}