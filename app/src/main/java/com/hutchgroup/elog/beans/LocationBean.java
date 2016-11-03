package com.hutchgroup.elog.beans;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class LocationBean {
    double Latitude, Longitude;
    String locationDescription="";
    String validLocationDate;

    public String getValidLocationDate() {
        return validLocationDate;
    }

    public void setValidLocationDate(String validLocationDate) {
        this.validLocationDate = validLocationDate;
    }

    public String getEngineHoursSinceLastValidCoordinate() {
        return EngineHoursSinceLastValidCoordinate;
    }

    public void setEngineHoursSinceLastValidCoordinate(String engineHoursSinceLastValidCoordinate) {
        EngineHoursSinceLastValidCoordinate = engineHoursSinceLastValidCoordinate;
    }

    String EngineHoursSinceLastValidCoordinate;

    public String getOdometerReadingSinceLastValidCoordinate() {
        return OdometerReadingSinceLastValidCoordinate;
    }

    public void setOdometerReadingSinceLastValidCoordinate(String odometerReadingSinceLastValidCoordinate) {
        OdometerReadingSinceLastValidCoordinate = odometerReadingSinceLastValidCoordinate;
    }

    String OdometerReadingSinceLastValidCoordinate = "0";

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }
}
