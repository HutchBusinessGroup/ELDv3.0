package com.hutchgroup.elog.beans;

import java.util.ArrayList;

/**
 * Created by Deepak.Sharma on 7/20/2015.
 */
public class DailyLogBean {
    int _id, OnlineDailyLogId, DriverId, CertifyFG, CreatedBy, ModifiedBy, StatusId, SyncFg;
    String LogDate;
    String ShippingId;
    String TrailerId;
    String Commodity;
    String StartTime;
    String StartOdometerReading;
    String EndOdometerReading;
    String CreatedDate;
    String ModifiedDate;

    public String getSignature() {
        return Signature;
    }

    public void setSignature(String signature) {
        Signature = signature;
    }

    String Signature;


    public ArrayList<EventBean> getEventList() {
        return eventList;
    }

    public void setEventList(ArrayList<EventBean> eventList) {
        this.eventList = eventList;
    }

    ArrayList<EventBean> eventList;

    public int getCertifyCount() {
        return certifyCount;
    }

    public void setCertifyCount(int certifyCount) {
        this.certifyCount = certifyCount;
    }

    int certifyCount;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getOnlineDailyLogId() {
        return OnlineDailyLogId;
    }

    public void setOnlineDailyLogId(int onlineDailyLogId) {
        OnlineDailyLogId = onlineDailyLogId;
    }

    public int getDriverId() {
        return DriverId;
    }

    public void setDriverId(int driverId) {
        DriverId = driverId;
    }

    public int getCertifyFG() {
        return CertifyFG;
    }

    public void setCertifyFG(int certifyFG) {
        CertifyFG = certifyFG;
    }

    public int getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(int createdBy) {
        CreatedBy = createdBy;
    }

    public int getModifiedBy() {
        return ModifiedBy;
    }

    public void setModifiedBy(int modifiedBy) {
        ModifiedBy = modifiedBy;
    }

    public int getStatusId() {
        return StatusId;
    }

    public void setStatusId(int statusId) {
        StatusId = statusId;
    }

    public int getSyncFg() {
        return SyncFg;
    }

    public void setSyncFg(int syncFg) {
        SyncFg = syncFg;
    }

    public String getLogDate() {
        return LogDate;
    }

    public void setLogDate(String logDate) {
        LogDate = logDate;
    }

    public String getShippingId() {
        return ShippingId;
    }

    public void setShippingId(String shippingId) {
        ShippingId = shippingId;
    }

    public String getTrailerId() {
        return TrailerId;
    }

    public void setTrailerId(String trailerId) {
        TrailerId = trailerId;
    }

    public String getCommodity() {
        return Commodity;
    }

    public void setCommodity(String commodity) {
        Commodity = commodity;
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public String getStartOdometerReading() {
        return StartOdometerReading;
    }

    public void setStartOdometerReading(String startOdometerReading) {
        StartOdometerReading = startOdometerReading;
    }

    public String getEndOdometerReading() {
        return EndOdometerReading;
    }

    public void setEndOdometerReading(String endOdometerReading) {
        EndOdometerReading = endOdometerReading;
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
}
