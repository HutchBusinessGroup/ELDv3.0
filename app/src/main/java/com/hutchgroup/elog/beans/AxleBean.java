package com.hutchgroup.elog.beans;

/**
 * Created by Deepak on 12/23/2016.
 */

public class AxleBean {
    int axleId, axleNo, vehicleId, tireNo, axlePosition;
    boolean doubleTireFg, frontTireFg,PowerUnitFg;
    double lowPressure, highPressure, pressure, lowTemperature, highTemperature, temperature;
    double pressure1, pressure2, pressure3, pressure4;
    double temperature1, temperature2, temperature3, temperature4;
    String sensorIds,Pressures,Temperatures;

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getTireNo() {
        return tireNo;
    }

    public void setTireNo(int tireNo) {
        this.tireNo = tireNo;
    }

    public int getAxlePosition() {
        return axlePosition;
    }

    public void setAxlePosition(int axlePosition) {
        this.axlePosition = axlePosition;
    }

    public boolean isDoubleTireFg() {
        return doubleTireFg;
    }

    public void setDoubleTireFg(boolean doubleTireFg) {
        this.doubleTireFg = doubleTireFg;
    }

    public boolean isFrontTireFg() {
        return frontTireFg;
    }

    public void setFrontTireFg(boolean frontTireFg) {
        this.frontTireFg = frontTireFg;
    }

    public double getLowPressure() {
        return lowPressure;
    }

    public void setLowPressure(double lowPressure) {
        this.lowPressure = lowPressure;
    }

    public double getHighPressure() {
        return highPressure;
    }

    public void setHighPressure(double highPressure) {
        this.highPressure = highPressure;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getLowTemperature() {
        return lowTemperature;
    }

    public void setLowTemperature(double lowTemperature) {
        this.lowTemperature = lowTemperature;
    }

    public double getHighTemperature() {
        return highTemperature;
    }

    public void setHighTemperature(double highTemperature) {
        this.highTemperature = highTemperature;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getPressure1() {
        return pressure1;
    }

    public void setPressure1(double pressure1) {
        this.pressure1 = pressure1;
    }

    public double getPressure2() {
        return pressure2;
    }

    public void setPressure2(double pressure2) {
        this.pressure2 = pressure2;
    }

    public double getPressure3() {
        return pressure3;
    }

    public void setPressure3(double pressure3) {
        this.pressure3 = pressure3;
    }

    public double getPressure4() {
        return pressure4;
    }

    public void setPressure4(double pressure4) {
        this.pressure4 = pressure4;
    }

    public double getTemperature1() {
        return temperature1;
    }

    public void setTemperature1(double temperature1) {
        this.temperature1 = temperature1;
    }

    public double getTemperature2() {
        return temperature2;
    }

    public void setTemperature2(double temperature2) {
        this.temperature2 = temperature2;
    }

    public double getTemperature3() {
        return temperature3;
    }

    public void setTemperature3(double temperature3) {
        this.temperature3 = temperature3;
    }

    public double getTemperature4() {
        return temperature4;
    }

    public void setTemperature4(double temperature4) {
        this.temperature4 = temperature4;
    }

    public String getSensorIds() {
        return sensorIds;
    }

    public void setSensorIds(String sensorIds) {
        this.sensorIds = sensorIds;
    }

    public int getAxleNo() {
        return axleNo;
    }

    public void setAxleNo(int axleNo) {
        this.axleNo = axleNo;
    }

    public boolean isPowerUnitFg() {
        return PowerUnitFg;
    }

    public void setPowerUnitFg(boolean powerUnitFg) {
        PowerUnitFg = powerUnitFg;
    }

    public String getPressures() {
        return Pressures;
    }

    public void setPressures(String pressures) {
        Pressures = pressures;
    }

    public String getTemperatures() {
        return Temperatures;
    }

    public void setTemperatures(String temperatures) {
        Temperatures = temperatures;
    }

    public int getAxleId() {
        return axleId;
    }

    public void setAxleId(int axleId) {
        this.axleId = axleId;
    }
}
