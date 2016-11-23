package com.hutchgroup.elog.beans;

import android.app.usage.UsageEvents;
import android.util.Log;

import com.hutchgroup.elog.common.Ascii;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.UserDB;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class EventBean implements Serializable {

    String DistanceSinceLastValidCoordinate;
    String Annotation;
    String AccumulatedVehicleMiles;
    String ElaspsedEngineHour;
    String MotorCarrier;
    String ShippingDocumentNo = "";
    String violation, violationMintes, violationTitle, violationExplanation;
    int coDriverId;

    public String getTrailerNo() {
        return TrailerNo;
    }

    public void setTrailerNo(String trailerNo) {
        TrailerNo = trailerNo;
    }

    String TrailerNo;
    String TimeZoneOffsetUTC;


    public String getCheckSumWeb() {
        return CheckSumWeb;
    }

    public void setCheckSumWeb(String checkSumWeb) {
        CheckSumWeb = checkSumWeb;
    }

    String CheckSumWeb;
    int MalfunctionIndicatorFg, DataDiagnosticIndicatorFg;

    public String getDiagnosticCode() {
        return DiagnosticCode;
    }

    public void setDiagnosticCode(String diagnosticCode) {
        DiagnosticCode = diagnosticCode;
    }

    String DiagnosticCode;

    public String getDistanceSinceLastValidCoordinate() {
        return DistanceSinceLastValidCoordinate;
    }

    public void setDistanceSinceLastValidCoordinate(String distanceSinceLastValidCoordinate) {
        DistanceSinceLastValidCoordinate = distanceSinceLastValidCoordinate;
    }

    public String getAnnotation() {
        return Annotation;
    }

    public void setAnnotation(String annotation) {
        Annotation = annotation;
    }

    public String getAccumulatedVehicleMiles() {
        return AccumulatedVehicleMiles;
    }

    public void setAccumulatedVehicleMiles(String accumulatedVehicleMiles) {
        AccumulatedVehicleMiles = accumulatedVehicleMiles;
    }

    public String getElaspsedEngineHour() {
        return ElaspsedEngineHour;
    }

    public void setElaspsedEngineHour(String elaspsedEngineHour) {
        ElaspsedEngineHour = elaspsedEngineHour;
    }

    public String getMotorCarrier() {
        return MotorCarrier;
    }

    public void setMotorCarrier(String motorCarrier) {
        MotorCarrier = motorCarrier;
    }

    public String getShippingDocumentNo() {
        return ShippingDocumentNo;
    }

    public void setShippingDocumentNo(String shippingDocumentNo) {
        ShippingDocumentNo = shippingDocumentNo;
    }

    public String getTimeZoneOffsetUTC() {
        return TimeZoneOffsetUTC;
    }

    public void setTimeZoneOffsetUTC(String timeZoneOffsetUTC) {
        TimeZoneOffsetUTC = timeZoneOffsetUTC;
    }

    public int getMalfunctionIndicatorFg() {
        return MalfunctionIndicatorFg;
    }

    public void setMalfunctionIndicatorFg(int malfunctionIndicatorFg) {
        MalfunctionIndicatorFg = malfunctionIndicatorFg;
    }

    public int getDataDiagnosticIndicatorFg() {
        return DataDiagnosticIndicatorFg;
    }

    public void setDataDiagnosticIndicatorFg(int dataDiagnosticIndicatorFg) {
        DataDiagnosticIndicatorFg = dataDiagnosticIndicatorFg;
    }

    int _id;

    public int getDriverId() {
        return DriverId;
    }

    public void setDriverId(int driverId) {
        DriverId = driverId;
    }

    int DriverId;
    int OnlineEventId;
    int EventSequenceId;
    int EventType;
    int EventCode;
    int EventRecordOrigin;
    int EventRecordStatus;
    int DailyLogId;
    int CreatedBy;
    int ModifiedBy;
    int StatusId;
    int SyncFg;
    String EventCodeDescription;
    String OdometerReading;
    String EngineHour;
    String EventDateTime;
    String Latitude;
    String Longitude;
    String LocationDescription = "";
    String CreatedDate;

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    Boolean isChecked;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getOnlineEventId() {
        return OnlineEventId;
    }

    public void setOnlineEventId(int onlineEventId) {
        OnlineEventId = onlineEventId;
    }

    public int getEventSequenceId() {
        return EventSequenceId;
    }

    public void setEventSequenceId(int eventSequenceId) {
        EventSequenceId = eventSequenceId;
    }

    public int getEventType() {
        return EventType;
    }

    public void setEventType(int eventType) {
        EventType = eventType;
    }

    public int getEventCode() {
        return EventCode;
    }

    public void setEventCode(int eventCode) {
        EventCode = eventCode;
    }

    public int getEventRecordOrigin() {
        return EventRecordOrigin;
    }

    public void setEventRecordOrigin(int eventRecordOrigin) {
        EventRecordOrigin = eventRecordOrigin;
    }

    public int getEventRecordStatus() {
        return EventRecordStatus;
    }

    public void setEventRecordStatus(int eventRecordStatus) {
        EventRecordStatus = eventRecordStatus;
    }

    public int getDailyLogId() {
        return DailyLogId;
    }

    public void setDailyLogId(int dailyLogId) {
        DailyLogId = dailyLogId;
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

    public String getEventCodeDescription() {
        return EventCodeDescription;
    }

    public void setEventCodeDescription(String eventCodeDescription) {
        EventCodeDescription = eventCodeDescription;
    }

    public String getOdometerReading() {
        return OdometerReading;
    }

    public void setOdometerReading(String odometerReading) {
        OdometerReading = odometerReading;
    }

    public String getEngineHour() {
        return EngineHour;
    }

    public void setEngineHour(String engineHour) {
        EngineHour = engineHour;
    }

    public String getEventDateTime() {
        return EventDateTime;
    }

    public void setEventDateTime(String eventDateTime) {
        EventDateTime = eventDateTime;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getLocationDescription() {
        return LocationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        LocationDescription = locationDescription;
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

    String ModifiedDate;

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    int vehicleId;

    public String getViolation() {
        return violation;
    }

    public void setViolation(String violation) {
        this.violation = violation;
    }

    public String getViolationMintes() {
        return violationMintes;
    }

    public void setViolationMintes(String violationMintes) {
        this.violationMintes = violationMintes;
    }

    public String getCheckSum() {
        int sum = 0;
        sum += Ascii.getSum(Integer.toString(EventType));
        sum += Ascii.getSum(Integer.toString(EventCode));
        sum += Ascii.getSum(Utility.parseDate(EventDateTime));
        sum += Ascii.getSum(Utility.timeOnlyGet(EventDateTime));
        sum += Ascii.getSum(OdometerReading);
        sum += Ascii.getSum(EngineHour);
        sum += Ascii.getSum(Latitude);
        sum += Ascii.getSum(Longitude);
        sum += Ascii.getSum(Utility.UnitNo);
        String userName = UserDB.getUserName(DriverId);

        sum += Ascii.getSum(userName);

        return Ascii.calculateCheckSum(sum);
    }

    /*Comparator for sorting the list by dateDesc*/
    public static Comparator<EventBean> dateDesc = new Comparator<EventBean>() {
        public int compare(EventBean s1, EventBean s2) {
            int date1 = 0;
            int date2 = 0;
            try {

                date1 = (int) (Utility.parse(s1.getEventDateTime()).getTime() / (1000));
                date2 = (int) (Utility.parse(s2.getEventDateTime()).getTime() / (1000));

            } catch (Exception exe) {
            }

            return date2 - date1;
        }
    };

    public int getCoDriverId() {
        return coDriverId;
    }

    public void setCoDriverId(int coDriverId) {
        this.coDriverId = coDriverId;
    }

    public String getViolationTitle() {
        return violationTitle;
    }

    public void setViolationTitle(String violationTitle) {
        this.violationTitle = violationTitle;
    }

    public String getViolationExplanation() {
        return violationExplanation;
    }

    public void setViolationExplanation(String violationExplanation) {
        this.violationExplanation = violationExplanation;
    }
}
