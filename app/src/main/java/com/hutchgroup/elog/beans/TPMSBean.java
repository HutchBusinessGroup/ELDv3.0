package com.hutchgroup.elog.beans;

/**
 * Created by Deepak on 12/21/2016.
 */

public class TPMSBean {
    //"(_id ,SensorId,Temperature Integer,Pressure Integer,Voltage text,CreatedDate text,ModifiedDate text,DriverId INTEGER,VehicleId INTEGER,SyncFg INTEGER)";
    int _id, temperature, pressure, driverId, tireNo;
    String SensorId, Voltage, CreatedDate, ModifiedDate;
    boolean isNew;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public String getSensorId() {
        return SensorId;
    }

    public void setSensorId(String sensorId) {
        SensorId = sensorId;
    }

    public String getVoltage() {
        return Voltage;
    }

    public void setVoltage(String voltage) {
        Voltage = voltage;
    }

    public String getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public String getModifiedDate() {
        return ModifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        ModifiedDate = modifiedDate;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public int getTireNo() {
        return tireNo;
    }

    public void setTireNo(int tireNo) {
        this.tireNo = tireNo;
    }
}
