package com.hutchgroup.elog.beans;

/**
 * Created by SAMSUNG on 30-12-2016.
 */

public class TrailerBean {

    String startOdometer, endOdometer, hookDate, unhookDate, latitude1, latitude2, longitude1, longitude2;
    int trailerId, driverId, syncFg,hookedFg;

    public String getStartOdometer() {
        return startOdometer;
    }

    public void setStartOdometer(String startOdometer) {
        this.startOdometer = startOdometer;
    }


    public String getHookDate() {
        return hookDate;
    }

    public void setHookDate(String hookDate) {
        this.hookDate = hookDate;
    }

    public String getUnhookDate() {
        return unhookDate;
    }

    public void setUnhookDate(String unhookDate) {
        this.unhookDate = unhookDate;
    }

    public String getLatitude1() {
        return latitude1;
    }

    public void setLatitude1(String latitude1) {
        this.latitude1 = latitude1;
    }

    public String getLatitude2() {
        return latitude2;
    }

    public void setLatitude2(String latitude2) {
        this.latitude2 = latitude2;
    }

    public String getLongitude1() {
        return longitude1;
    }

    public void setLongitude1(String longitude1) {
        this.longitude1 = longitude1;
    }

    public String getLongitude2() {
        return longitude2;
    }

    public void setLongitude2(String longitude2) {
        this.longitude2 = longitude2;
    }

    public int getTrailerId() {
        return trailerId;
    }

    public void setTrailerId(int trailerId) {
        this.trailerId = trailerId;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public int getSyncFg() {
        return syncFg;
    }

    public void setSyncFg(int syncFg) {
        this.syncFg = syncFg;
    }

    public int getHookedFg() {
        return hookedFg;
    }

    public void setHookedFg(int hookedFg) {
        this.hookedFg = hookedFg;
    }

    public String getEndOdometer() {
        return endOdometer;
    }

    public void setEndOdometer(String endOdometer) {
        this.endOdometer = endOdometer;
    }
}
