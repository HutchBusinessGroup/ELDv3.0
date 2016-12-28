package com.hutchgroup.elog.beans;

/**
 * Created by SAMSUNG on 28-12-2016.
 */

public class VehicleBean {

    int vehicleId, totalAxle;
    String unitNo, plateNo;

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getTotalAxle() {
        return totalAxle;
    }

    public void setTotalAxle(int totalAxle) {
        this.totalAxle = totalAxle;
    }

    public String getUnitNo() {
        return unitNo;
    }

    public void setUnitNo(String unitNo) {
        this.unitNo = unitNo;
    }

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }
}
