package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hutchgroup.elog.beans.DiagnosticIndicatorBean;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.DiagnosticMalfunction;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class EventDB {

    // Created By: Deepak Sharma
    // Created Date: 05 August 2016
    // Purpose: get last event date
    public static String getLastEventDate(int driverId) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        String lastDateTime = Utility.getPreviousDate(-15);
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventDateTime from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventDateTime>? order by EventDateTime desc LIMIT 1", new String[]{Integer.toString(driverId), lastDateTime});


            if (cursor.moveToNext()) {
                lastDateTime = cursor.getString(0);
            }
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::previousDutyStatusGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return lastDateTime;
    }

    // Created By: Minh Tran
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static EventBean previousDutyStatusGet(int driverId, String eventDatetime) {

        EventBean bean = new EventBean();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventDateTime,EventType ,EventCode from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventDateTime<? and EventType in (1,3) and EventRecordStatus=1 order by EventDateTime desc LIMIT 1", new String[]{Integer.toString(driverId), eventDatetime});


            if (cursor.moveToNext()) {
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
            }
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::previousDutyStatusGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return bean;
    }


    // Created By: Deepak Sharma
    // Created Date: 20 Oct 2016
    // Purpose: get last duty status of onscreen user
    public static int getCurrentDutyStatus(int driverId) {

        int status = 1;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventDateTime,EventType ,EventCode from " +
                            MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventType in (1,3) and EventRecordStatus=1 order by EventDateTime desc LIMIT 1"
                    , new String[]{Integer.toString(driverId)});


            if (cursor.moveToNext()) {
                int eventType = cursor.getInt(cursor.getColumnIndex("EventType"));
                int eventCode = cursor.getInt(cursor.getColumnIndex("EventCode"));
                if (eventType == 1)
                    status = eventCode;
                else
                    status = eventCode + 4;
            }
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::previousDutyStatusGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return status;
    }

    // Created By: Minh Tran
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static boolean getMissingDataFg(int driverId) {

        boolean status = false;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventCode from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventType=1 and EventCode>=3 and EventRecordStatus=1 and Latitude=? order by EventDateTime desc LIMIT 1", new String[]{Integer.toString(driverId), "X"});


            if (cursor.moveToNext()) {
                status = true;
            }
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::previousDutyStatusGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get Engine hours and odometer reading when power on
    public static void getEngineHourOdometerSincePowerOn(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select OdometerReading ,EngineHour from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                            " where EventType=6 and EventCode<=2 order by EventDateTime desc LIMIT 1"
                    , null);
            if (cursor.moveToNext()) {
                Utility.OdometerReadingSincePowerOn = cursor.getString(0);
                Utility.EngineHourSincePowerOn = cursor.getString(1);
            } else {
                Utility.OdometerReadingSincePowerOn = CanMessages.OdometerReading;
                Utility.EngineHourSincePowerOn = CanMessages.EngineHours;
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(EventDB.class.getName() + "::getEngineHourOdometerSincePowerOn Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 15 January 2015
    // Purpose: get event sequence id for new record.
    public static int getEventSequenceId() {
        int id = 1;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select max(EventSequenceId) from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where VehicleId=?", new String[]{Utility.vehicleId + ""});
            if (cursor.moveToNext()) {
                id = cursor.getInt(0) + 1;
            }

        } catch (Exception exe) {
            Log.i("EventDB", "Cannot get Event Sequence Id");
            LogFile.write(EventDB.class.getName() + "::getEventSequenceId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return id;
    }

    // Created By: Deepak Sharma
    // Created Date: 27 January 2015
    // Purpose: get event id if exists
    private static int getEventId(int driverId, String eventDateTime) {
        int id = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventDateTime=?", new String[]{driverId + "", eventDateTime});
            if (cursor.moveToNext()) {
                id = cursor.getInt(0);
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::getEventId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return id;
    }

    // Created By: Deepak Sharma
    // Created Date: 27 January 2015
    // Purpose: get event id by created date if exists
    private static int getEventId(String createdDatetime, int driverId) {
        int id = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and CreatedDate=?", new String[]{driverId + "", createdDatetime});
            if (cursor.moveToNext()) {
                id = cursor.getInt(0);
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::getEventId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return id;
    }

    // Created By: Deepak Sharma
    // Created Date: 27 January 2015
    // Purpose: get event id by created date and eventrecordstatus if exists
    public static int getEventId(int driverId, String createdDatetime, int eventRecordStatus) {
        int id = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and CreatedDate=? and EventRecordStatus=?", new String[]{driverId + "", createdDatetime, eventRecordStatus + ""});
            if (cursor.moveToNext()) {
                id = cursor.getInt(0);
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::getEventId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return id;
    }

    // Created By: Deepak Sharma
    // Created Date: 27 January 2015
    // Purpose: get event id by created date if exists
    public static int getEventId(String createdDatetime, int driverId, int eventRecordOrigin) {
        int id = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and CreatedDate=? and EventRecordOrigin=?", new String[]{driverId + "", createdDatetime, eventRecordOrigin + ""});
            if (cursor.moveToNext()) {
                id = cursor.getInt(0);
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::getEventId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return id;
    }

    // Created By: Minh Tran
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static ArrayList<EventBean> EventGet(int driverId) {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,EventDateTime,EventType ,EventCode ,EventCodeDescription,EventRecordOrigin, EventRecordStatus, Latitude ,Longitude ,LocationDescription,OdometerReading from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=?", new String[]{Integer.toString(driverId)});
            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventRecordOrigin(cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                bean.setEventRecordStatus(cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setChecked(false);
                list.add(bean);
            }

            Collections.sort(list, EventBean.dateDesc);
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::EventGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    public static ArrayList<EventBean> EventGetByLogId(int driverId, int logId) {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,DailyLogId,EventDateTime,EventType ,EventCode ,EventCodeDescription,EventRecordOrigin, EventRecordStatus, Latitude ,Longitude ,LocationDescription,OdometerReading,EngineHour,AccumulatedVehicleMiles" +
                    ",ElaspsedEngineHour,ShippingDocumentNo,CoDriverId,Annotation,DistanceSinceLastValidCoordinate,VehicleId,DiagnosticCode,TimeZoneOffsetUTC,TrailerNo,MotorCarrier,MalfunctionIndicatorFg, DataDiagnosticIndicatorFg from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId in (?) and DailyLogId=? and DailyLogId!=0 order by EventDateTime desc, EventSequenceId desc", new String[]{Integer.toString(driverId), Integer.toString(logId)});
            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setDailyLogId(cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventRecordOrigin(cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                bean.setEventRecordStatus(cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setAnnotation(cursor.getString(cursor.getColumnIndex("Annotation")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                bean.setEngineHour(cursor.getString(cursor.getColumnIndex("EngineHour")));
                bean.setAccumulatedVehicleMiles(cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles")));
                bean.setElaspsedEngineHour(cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setShippingDocumentNo(cursor.getString(cursor.getColumnIndex("ShippingDocumentNo")));
                bean.setCoDriverId(cursor.getInt(cursor.getColumnIndex("CoDriverId")));

                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
                bean.setDiagnosticCode(cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
                bean.setTimeZoneOffsetUTC(cursor.getString(cursor.getColumnIndex("TimeZoneOffsetUTC")));
                bean.setTrailerNo(cursor.getString(cursor.getColumnIndex("TrailerNo")));
                bean.setMotorCarrier(cursor.getString(cursor.getColumnIndex("MotorCarrier")));
                bean.setMalfunctionIndicatorFg(cursor.getInt(cursor.getColumnIndex("MalfunctionIndicatorFg")));
                bean.setDataDiagnosticIndicatorFg(cursor.getInt(cursor.getColumnIndex("DataDiagnosticIndicatorFg")));

                bean.setDistanceSinceLastValidCoordinate(cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate")));
                bean.setDriverId(driverId);
                bean.setChecked(false);
                list.add(bean);
            }

            Collections.sort(list, EventBean.dateDesc);
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::EventGetByLogId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    public static ArrayList<EventBean> DiagnosticMalFunctionEventGetByCode(int eventcode) {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,EventDateTime,EventType ,EventCode ,EventCodeDescription,EventRecordOrigin, EventRecordStatus, Latitude ,Longitude ,LocationDescription,OdometerReading,ShippingDocumentNo,DiagnosticCode from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where EventType=7 and EventCode=? Order by EventDateTime desc", new String[]{Integer.toString(eventcode)});
            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventRecordOrigin(cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                bean.setEventRecordStatus(cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setShippingDocumentNo(cursor.getString(cursor.getColumnIndex("ShippingDocumentNo")));
                bean.setDiagnosticCode(cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
                bean.setChecked(false);
                list.add(bean);
            }

            Collections.sort(list, EventBean.dateDesc);
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::DiagnosticMalFunctionEventGetByCode Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    // Created By: Minh Tran
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static ArrayList<EventBean> DutyStatusChangedEventGetByLogId(int driverId, int logId) {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,DailyLogId,EventDateTime,EventType ,EventCode ,EventCodeDescription,EventRecordOrigin, EventRecordStatus, Latitude ,Longitude ,LocationDescription,Annotation, OdometerReading,EngineHour" +
                    ",AccumulatedVehicleMiles,ElaspsedEngineHour ,ShippingDocumentNo,CoDriverId,CreatedDate,DistanceSinceLastValidCoordinate,VehicleId,DiagnosticCode,TimeZoneOffsetUTC,TrailerNo,MotorCarrier,MalfunctionIndicatorFg, DataDiagnosticIndicatorFg from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId =? and DailyLogId=? and EventRecordStatus=1 and DailyLogId!=0 and EventType in (1,3) and EventCode!=0", new String[]{Integer.toString(driverId), Integer.toString(logId)});


            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();

                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setDailyLogId(cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventRecordOrigin(cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                bean.setEventRecordStatus(cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setAnnotation(cursor.getString(cursor.getColumnIndex("Annotation")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                bean.setEngineHour(cursor.getString(cursor.getColumnIndex("EngineHour")));
                bean.setAccumulatedVehicleMiles(cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles")));
                bean.setElaspsedEngineHour(cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setShippingDocumentNo(cursor.getString(cursor.getColumnIndex("ShippingDocumentNo")));
                bean.setCoDriverId(cursor.getInt(cursor.getColumnIndex("CoDriverId")));

                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
                bean.setDiagnosticCode(cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
                bean.setTimeZoneOffsetUTC(cursor.getString(cursor.getColumnIndex("TimeZoneOffsetUTC")));
                bean.setTrailerNo(cursor.getString(cursor.getColumnIndex("TrailerNo")));
                bean.setMotorCarrier(cursor.getString(cursor.getColumnIndex("MotorCarrier")));
                bean.setMalfunctionIndicatorFg(cursor.getInt(cursor.getColumnIndex("MalfunctionIndicatorFg")));
                bean.setDataDiagnosticIndicatorFg(cursor.getInt(cursor.getColumnIndex("DataDiagnosticIndicatorFg")));

                bean.setDistanceSinceLastValidCoordinate(cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate")));
                bean.setDriverId(driverId);
                bean.setChecked(false);
                bean.setCreatedDate(cursor.getString(cursor.getColumnIndex("CreatedDate")));
                list.add(bean);
            }

            Collections.sort(list, EventBean.dateDesc);
        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::DutyStatusChangedEventGetByLogId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 12 March 2015
    // Purpose: get events to calculate total distance
    public static ArrayList<EventBean> TotalDistanceGetByLogId(int logId) {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select EventType,EventCode ,OdometerReading, EngineHour from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DailyLogId=? and EventType<=3 and EventRecordStatus=1 order by EventDateTime", new String[]{Integer.toString(logId)});
            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();
                int eventType = cursor.getInt(cursor.getColumnIndex("EventType"));
                int eventCode = cursor.getInt(cursor.getColumnIndex("EventCode"));
                bean.setEventType(eventType);
                bean.setEventCode(eventCode);
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                bean.setEngineHour(cursor.getString(cursor.getColumnIndex("EngineHour")));
                list.add(bean);
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::TotalDistanceGetByLogId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static ArrayList<EventBean> EventUnAssignedGet() {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,EventDateTime,EventType ,EventCode ,EventCodeDescription,Latitude ,Longitude ,LocationDescription,OdometerReading,DailyLogId from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on e.DriverId=a.AccountId and a.AccountType=2 where EventRecordStatus=1 and EventType!=7 order by EventDateTime desc", null);

            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setDailyLogId(cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                bean.setChecked(false);
                list.add(bean);
            }

        } catch (Exception exe) {
            Log.i("EventDB", "Error:" + exe.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUnAssignedGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static boolean UnIdentifiedEventFg() {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id  from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " e join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " a on e.DriverId=a.AccountId and a.AccountType=2 where EventRecordStatus=1" +
                    " and EventType!=7 Limit 1", null);

            if (cursor.moveToNext()) {
                status = true;
            }

        } catch (Exception exe) {
            Log.i("EventDB", "Error:" + exe.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUnAssignedGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static ArrayList<EventBean> EventEditRequestedGet(int driverId) {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,EventDateTime,EventType ,EventCode ,EventCodeDescription,Latitude ,Longitude ,LocationDescription,OdometerReading,CreatedDate,DailyLogId from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where DriverId=? and EventRecordStatus=3 and EventRecordOrigin=3", new String[]{driverId + ""});
            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setCreatedDate(cursor.getString(cursor.getColumnIndex("CreatedDate")));
                bean.setDailyLogId(cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                bean.setChecked(false);
                bean.setEventRecordOrigin(3);
                bean.setEventRecordStatus(3);
                list.add(bean);
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::EventEditRequestedGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static EventBean EventGetById(int id) {
        EventBean bean = new EventBean();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,EventType ,EventCode ,EventCodeDescription ,OdometerReading ,EngineHour ,EventRecordOrigin ,EventRecordStatus " +
                    ",EventDateTime ,Latitude ,Longitude ,LocationDescription ,DailyLogId ,CreatedBy ,CreatedDate ,ModifiedBy ,ModifiedDate ,StatusId ,SyncFg,DistanceSinceLastValidCoordinate,AccumulatedVehicleMiles ,ElaspsedEngineHour" +
                    ",MalfunctionIndicatorFg ,DataDiagnosticIndicatorFg,DiagnosticCode,MotorCarrier,ShippingDocumentNo,TrailerNo,TimeZoneOffsetUTC,Annotation,CoDriverId,VehicleId from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where _id=?", new String[]{id + ""});
            if (cursor.moveToNext()) {
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setEventType(cursor.getInt(cursor.getColumnIndex("EventType")));
                bean.setEventCode(cursor.getInt(cursor.getColumnIndex("EventCode")));
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("OdometerReading")));
                bean.setEngineHour(cursor.getString(cursor.getColumnIndex("EngineHour")));
                bean.setEventRecordOrigin(cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                bean.setEventRecordStatus(cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocationDescription(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setDailyLogId(cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                bean.setCreatedBy(cursor.getInt(cursor.getColumnIndex("CreatedBy")));
                bean.setCreatedDate(cursor.getString(cursor.getColumnIndex("CreatedDate")));
                bean.setModifiedBy(cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                bean.setModifiedDate(cursor.getString(cursor.getColumnIndex("ModifiedDate")));
                bean.setStatusId(cursor.getInt(cursor.getColumnIndex("StatusId")));
                bean.setSyncFg(cursor.getInt(cursor.getColumnIndex("SyncFg")));
                bean.setDistanceSinceLastValidCoordinate(cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate")));
                bean.setAccumulatedVehicleMiles(cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles")));
                bean.setElaspsedEngineHour(cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour")));

                bean.setMalfunctionIndicatorFg(cursor.getInt(cursor.getColumnIndex("MalfunctionIndicatorFg")));
                bean.setDataDiagnosticIndicatorFg(cursor.getInt(cursor.getColumnIndex("DataDiagnosticIndicatorFg")));
                bean.setDiagnosticCode(cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
                bean.setMotorCarrier(cursor.getString(cursor.getColumnIndex("MotorCarrier")));
                bean.setShippingDocumentNo(cursor.getString(cursor.getColumnIndex("ShippingDocumentNo")));
                bean.setTrailerNo(cursor.getString(cursor.getColumnIndex("TrailerNo")));
                bean.setTimeZoneOffsetUTC(cursor.getString(cursor.getColumnIndex("TimeZoneOffsetUTC")));
                bean.setCoDriverId(cursor.getInt(cursor.getColumnIndex("CoDriverId")));
                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::EventGetById Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return bean;
    }

    public static void EventCreateWithLocation(String eventDateTime, int eventType, int eventCode, String eventDescription, int eventOrigin, int eventStatus, int dailyLogId, int driverId, String location, String annotation) {
        try {
            EventBean bean = new EventBean();

            bean.setOnlineEventId(0);
            bean.setDriverId(driverId);
            bean.setEventSequenceId(getEventSequenceId());
            bean.setEventType(eventType);
            bean.setEventCode(eventCode);
            bean.setEventCodeDescription(eventDescription);
            bean.setEventRecordOrigin(eventOrigin);
            bean.setEventRecordStatus(eventStatus);
            bean.setDailyLogId(dailyLogId);
            bean.setOdometerReading(Double.valueOf(CanMessages.OdometerReading).intValue() + "");
            bean.setEngineHour(String.format("%.1f", Double.valueOf(CanMessages.EngineHours)));
            bean.setEventDateTime(eventDateTime);
            if (Utility.currentLocation.getLatitude() < 0) {
                bean.setLatitude(Utility.currentLocation.getLatitude() == -1 ? "0" : "-2");
                bean.setLongitude(Utility.currentLocation.getLongitude() == -1 ? "0" : "-2");
            } else {
                bean.setLatitude(eventType == 3 && eventCode == 1 ? String.format("%.1f", Utility.currentLocation.getLatitude()) : String.format("%.2f", Utility.currentLocation.getLatitude()));
                bean.setLongitude(eventType == 3 && eventCode == 1 ? String.format("%.1f", Utility.currentLocation.getLongitude()) : String.format("%.2f", Utility.currentLocation.getLongitude()));
            }
            bean.setLocationDescription(location);
            bean.setCreatedBy(driverId);
            bean.setCreatedDate(Utility.getCurrentDateTime());
            bean.setModifiedBy(driverId);
            bean.setModifiedDate(Utility.getCurrentDateTime());
            bean.setStatusId(1);
            bean.setSyncFg(0);
            bean.setVehicleId(Utility.vehicleId);
            bean.setMotorCarrier(Utility.CarrierName);
            bean.setTimeZoneOffsetUTC(Utility.TimeZoneOffsetUTC);
            bean.setAnnotation(annotation);

            // set default values
            bean.setDistanceSinceLastValidCoordinate("0");
            bean.setAccumulatedVehicleMiles("0");
            bean.setElaspsedEngineHour("0");
            bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
            bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
            bean.setShippingDocumentNo(Utility.ShippingNumber);
            bean.setTrailerNo(Utility.TrailerNumber);
            bean.setDiagnosticCode("");

            if (Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate().equals("0")) {
                Utility.currentLocation.setOdometerReadingSinceLastValidCoordinate(CanMessages.OdometerReading);
            }

            //-----------------------------------------//
            if (eventType <= 3) {
                bean.setDistanceSinceLastValidCoordinate((Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate()).intValue()) + "");
                bean.setAccumulatedVehicleMiles((Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.OdometerReadingSincePowerOn).intValue()) + "");
                bean.setElaspsedEngineHour(String.format("%.1f", Double.valueOf(CanMessages.EngineHours) - Double.valueOf(Utility.EngineHourSincePowerOn)));
                bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
                bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
                //MainActivity.textToSpeech.speak(eventDescription, TextToSpeech.QUEUE_ADD, null);
            } else if (eventType == 6) {
                bean.setDistanceSinceLastValidCoordinate((Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate()).intValue()) + "");
            } else if (eventType == 7) {
                bean.setDiagnosticCode(Utility.DiagnosticCode);
            }

            int coDriverId = 0;
            if (Utility.user1.getAccountId() == driverId) {
                coDriverId = Utility.user2.getAccountId();
            } else {
                coDriverId = Utility.user1.getAccountId();
            }
            bean.setCoDriverId(coDriverId);

            EventSave(bean);

        } catch (Exception e) {
            Log.i("EventDB", "EventCreate Error:" + e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventCreate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }

    }

    public static void EventCreateManually(String eventDateTime, int eventType, int eventCode, String eventDescription, int eventOrigin, int eventStatus, int dailyLogId, int driverId, String location, String annotation) {
        try {
            EventBean bean = new EventBean();

            bean.setOnlineEventId(0);
            bean.setDriverId(driverId);
            bean.setEventSequenceId(getEventSequenceId());
            bean.setEventType(eventType);
            bean.setEventCode(eventCode);
            bean.setEventCodeDescription(eventDescription);
            bean.setEventRecordOrigin(eventOrigin);
            bean.setEventRecordStatus(eventStatus);
            bean.setDailyLogId(dailyLogId);
            bean.setOdometerReading(Double.valueOf(CanMessages.OdometerReading).intValue() + "");
            bean.setEngineHour(String.format("%.1f", Double.valueOf(CanMessages.EngineHours)));
            bean.setEventDateTime(eventDateTime);

//            if (Utility.currentLocation.getLatitude() < 0) {
//                bean.setLatitude(Utility.currentLocation.getLatitude() == -1 ? "M" : "E");
//                bean.setLongitude(Utility.currentLocation.getLongitude() == -1 ? "M" : "E");
//            } else {
//                bean.setLatitude(eventType == 3 && eventCode == 1 ? String.format("%.1f", Utility.currentLocation.getLatitude()) : String.format("%.2f", Utility.currentLocation.getLatitude()));
//                bean.setLongitude(eventType == 3 && eventCode == 1 ? String.format("%.1f", Utility.currentLocation.getLongitude()) : String.format("%.2f", Utility.currentLocation.getLongitude()));
//            }
            //latitude and longitude must set to M because this is manually event 0 means M as we have decimal column for latitude and longitude
            bean.setLatitude("0");
            bean.setLongitude("0");
            bean.setLocationDescription(location);
            bean.setCreatedBy(driverId);
            bean.setCreatedDate(Utility.getCurrentDateTime());
            bean.setModifiedBy(driverId);
            bean.setModifiedDate(Utility.getCurrentDateTime());
            bean.setStatusId(1);
            bean.setSyncFg(0);
            bean.setVehicleId(Utility.vehicleId);
            bean.setMotorCarrier(Utility.CarrierName);
            bean.setTimeZoneOffsetUTC(Utility.TimeZoneOffsetUTC);
            bean.setAnnotation(annotation);

            // set default values
            bean.setDistanceSinceLastValidCoordinate("0");
            bean.setAccumulatedVehicleMiles("0");
            bean.setElaspsedEngineHour("0");
            bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
            bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
            bean.setShippingDocumentNo(Utility.ShippingNumber);
            bean.setTrailerNo(Utility.TrailerNumber);
            bean.setDiagnosticCode("");
            //-----------------------------------------//
            if (eventType <= 3) {
                bean.setDistanceSinceLastValidCoordinate((Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate()).intValue()) + "");
                bean.setAccumulatedVehicleMiles((Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.OdometerReadingSincePowerOn).intValue()) + "");
                bean.setElaspsedEngineHour(String.format("%.1f", Double.valueOf(CanMessages.EngineHours) - Double.valueOf(Utility.EngineHourSincePowerOn)));
                bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
                bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
                //MainActivity.textToSpeech.speak(eventDescription, TextToSpeech.QUEUE_ADD, null);
            } else if (eventType == 6) {
                bean.setDistanceSinceLastValidCoordinate((Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate()).intValue()) + "");
            } else if (eventType == 7) {
                bean.setDiagnosticCode(Utility.DiagnosticCode);
            }

            int coDriverId = 0;
            if (Utility.user1.getAccountId() == driverId) {
                coDriverId = Utility.user2.getAccountId();
            } else {
                coDriverId = Utility.user1.getAccountId();
            }
            bean.setCoDriverId(coDriverId);
            EventSave(bean);
        } catch (Exception e) {
            Log.i("EventDB", "EventCreate Error:" + e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventCreate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }

    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: create events
    public static void EventCreate(String eventDateTime, int eventType, int eventCode, String eventDescription, int eventOrigin, int eventStatus, int dailyLogId, int driverId, String annotation) {
        try {
            EventBean bean = new EventBean();

            bean.setOnlineEventId(0);
            bean.setDriverId(driverId);
            bean.setEventSequenceId(getEventSequenceId());
            bean.setEventType(eventType);
            bean.setEventCode(eventCode);
            bean.setEventCodeDescription(eventDescription);
            bean.setEventRecordOrigin(eventOrigin);
            bean.setEventRecordStatus(eventStatus);
            bean.setDailyLogId(dailyLogId);
            bean.setOdometerReading(Double.valueOf(CanMessages.OdometerReading).intValue() + "");
            bean.setEngineHour(String.format("%.1f", Double.valueOf(CanMessages.EngineHours)));
            bean.setEventDateTime(eventDateTime);
            // while event created automatically we should enter X if malfunction is not occurred
            if (Utility.currentLocation.getLatitude() < 0) {
                bean.setLatitude(Utility.currentLocation.getLatitude() + "");
                bean.setLongitude(Utility.currentLocation.getLongitude() + "");
            } else {
                bean.setLatitude(eventType == 3 && eventCode == 1 ? String.format("%.1f", Utility.currentLocation.getLatitude()) : String.format("%.2f", Utility.currentLocation.getLatitude()));
                bean.setLongitude(eventType == 3 && eventCode == 1 ? String.format("%.1f", Utility.currentLocation.getLongitude()) : String.format("%.2f", Utility.currentLocation.getLongitude()));
            }

            bean.setLocationDescription(Utility.currentLocation.getLocationDescription() == null ? "" : Utility.currentLocation.getLocationDescription());
            bean.setCreatedBy(driverId);
            bean.setCreatedDate(Utility.getCurrentDateTime());
            bean.setModifiedBy(driverId);
            bean.setModifiedDate(Utility.getCurrentDateTime());
            bean.setStatusId(1);
            bean.setSyncFg(0);
            bean.setVehicleId(Utility.vehicleId);
            bean.setMotorCarrier(Utility.CarrierName);
            bean.setTimeZoneOffsetUTC(Utility.TimeZoneOffsetUTC);
            bean.setAnnotation(annotation);

            // set default values
            bean.setDistanceSinceLastValidCoordinate("0");
            bean.setAccumulatedVehicleMiles("0");
            bean.setElaspsedEngineHour("0");
            bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
            bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
            bean.setShippingDocumentNo(Utility.ShippingNumber);
            bean.setTrailerNo(Utility.TrailerNumber);
            bean.setDiagnosticCode("");

            //-----------------------------------------//
            if (eventType <= 3) {
                bean.setDistanceSinceLastValidCoordinate((Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate()).intValue()) + "");
                bean.setAccumulatedVehicleMiles((Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.OdometerReadingSincePowerOn).intValue()) + "");
                bean.setElaspsedEngineHour(String.format("%.1f", Double.valueOf(CanMessages.EngineHours) - Double.valueOf(Utility.EngineHourSincePowerOn)));
                bean.setMalfunctionIndicatorFg(Utility.malFunctionIndicatorFg ? 1 : 0);
                bean.setDataDiagnosticIndicatorFg(Utility.dataDiagnosticIndicatorFg ? 1 : 0);
                //MainActivity.textToSpeech.speak(eventDescription, TextToSpeech.QUEUE_ADD, null);
            } else if (eventType == 6) {
                bean.setDistanceSinceLastValidCoordinate((Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate()).intValue()) + "");
            } else if (eventType == 7) {
                bean.setDiagnosticCode(Utility.DiagnosticCode);
            }

            int coDriverId = 0;
            if (Utility.user1.getAccountId() == driverId) {
                coDriverId = Utility.user2.getAccountId();
            } else {
                coDriverId = Utility.user1.getAccountId();
            }
            bean.setCoDriverId(coDriverId);

            EventSave(bean);
            if (eventType == 1 && eventCode >= 3 && eventOrigin == 1 && eventStatus == 1 && Utility.currentLocation.getLatitude() == -1) {
                if (!DiagnosticIndicatorBean.MissingElementDiagnosticFg) {
                    DiagnosticIndicatorBean.MissingElementDiagnosticFg = true;
                    // save malfunction for Missing element Diagnostic event
                    DiagnosticMalfunction.saveDiagnosticIndicatorByCode("3", 3, "MissingElementDiagnosticFg");
                }
            }

        } catch (Exception e) {
            Log.i("EventDB", "EventCreate Error:" + e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventCreate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }

    }

    // Created By: Deepak Sharma
    // Created Date: 27 January 2015
    // Purpose: add or update multiple events in database from web
    public static int EventSave(ArrayList<EventBean> arrBean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        int eventId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (EventBean bean : arrBean) {
                ContentValues values = new ContentValues();
                // in case edit request from server
                if (bean.getEventRecordOrigin() == 3) {

                    eventId = getEventId(bean.getCreatedDate(), bean.getDriverId(), bean.getEventRecordOrigin());
                } else {
                    eventId = getEventId(bean.getCreatedDate(), bean.getDriverId());
                }
                values.put("OnlineEventId", bean.getOnlineEventId());

                values.put("EventRecordOrigin", bean.getEventRecordOrigin());
                values.put("EventRecordStatus", bean.getEventRecordStatus());
                if (eventId == 0) {
                    values.put("DriverId", bean.getDriverId());
                    values.put("VehicleId", bean.getVehicleId());
                    values.put("EventSequenceId", bean.getEventSequenceId());
                    values.put("EventType", bean.getEventType());
                    values.put("EventCode", bean.getEventCode());
                    values.put("EventCodeDescription", bean.getEventCodeDescription());
                    values.put("OdometerReading", bean.getOdometerReading());
                    values.put("EngineHour", bean.getEngineHour());
                    values.put("EventDateTime", bean.getEventDateTime());
                    values.put("Latitude", bean.getLatitude());
                    values.put("Longitude", bean.getLongitude());
                    values.put("LocationDescription", bean.getLocationDescription());
                    values.put("DailyLogId", bean.getDailyLogId());
                    values.put("CreatedBy", bean.getCreatedBy());
                    values.put("CreatedDate", bean.getCreatedDate());
                    values.put("StatusId", 1);
                    values.put("SyncFg", 1);
                    values.put("DistanceSinceLastValidCoordinate", bean.getDistanceSinceLastValidCoordinate());
                    values.put("MalfunctionIndicatorFg", bean.getMalfunctionIndicatorFg());
                    values.put("DiagnosticCode", bean.getDiagnosticCode());
                    values.put("DataDiagnosticIndicatorFg", bean.getDataDiagnosticIndicatorFg());
                    values.put("Annotation", bean.getAnnotation());
                    values.put("AccumulatedVehicleMiles", bean.getAccumulatedVehicleMiles());
                    values.put("ElaspsedEngineHour", bean.getElaspsedEngineHour());
                    values.put("MotorCarrier", bean.getMotorCarrier());
                    values.put("ShippingDocumentNo", bean.getShippingDocumentNo());
                    values.put("TrailerNo", bean.getTrailerNo());
                    values.put("TimeZoneOffsetUTC", bean.getTimeZoneOffsetUTC());
                    values.put("CoDriverId", bean.getCoDriverId());
                    values.put("CheckSum", bean.getCheckSumWeb());


                    eventId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT,
                            "_id,ModifiedBy,ModifiedDate", values);
                } else {
                    if (bean.getEventRecordOrigin() == 3) {
                        values.put("EventDateTime", bean.getEventDateTime());
                    }
                    values.put("CheckSum", bean.getCheckSum());
                    //122 if (bean.getDriverId() == Utility.unIdentifiedDriverId)
                    //122     LogFile.write(EventDB.class.getName() + "::EventSave save list event: " + "create checksum " + bean.getCheckSum(), LogFile.USER_INTERACTION, LogFile.NOLOGIN_LOG);
                    //123 if (bean.getDriverId() != Utility.unIdentifiedDriverId)
                    //123     LogFile.write(EventDB.class.getName() + "::EventSave save list event: " + "create checksum " + bean.getCheckSum(), LogFile.USER_INTERACTION, LogFile.DRIVEREVENT_LOG);
                    database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                            " _id= ?",
                            new String[]{eventId + ""});
                }

            }
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return eventId;
    }


    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update events in database
    public static int EventSave(EventBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        int eventId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("OnlineEventId", bean.getOnlineEventId());
            values.put("DriverId", bean.getDriverId());
            values.put("VehicleId", bean.getVehicleId());
            values.put("EventSequenceId", bean.getEventSequenceId());
            values.put("EventType", bean.getEventType());
            values.put("EventCode", bean.getEventCode());
            values.put("EventCodeDescription", bean.getEventCodeDescription());
            values.put("OdometerReading", bean.getOdometerReading());
            values.put("EngineHour", bean.getEngineHour());
            values.put("EventRecordOrigin", bean.getEventRecordOrigin());
            values.put("EventRecordStatus", bean.getEventRecordStatus());
            values.put("EventDateTime", bean.getEventDateTime());
            values.put("Latitude", bean.getLatitude());
            values.put("Longitude", bean.getLongitude());
            values.put("LocationDescription", bean.getLocationDescription());
            values.put("DailyLogId", bean.getDailyLogId());
            values.put("CreatedBy", bean.getCreatedBy());
            values.put("CreatedDate", bean.getCreatedDate());
            values.put("StatusId", bean.getStatusId());
            values.put("SyncFg", bean.getSyncFg());

            values.put("DistanceSinceLastValidCoordinate", bean.getDistanceSinceLastValidCoordinate());
            values.put("MalfunctionIndicatorFg", bean.getMalfunctionIndicatorFg());
            values.put("DiagnosticCode", bean.getDiagnosticCode());
            values.put("DataDiagnosticIndicatorFg", bean.getDataDiagnosticIndicatorFg());
            values.put("Annotation", bean.getAnnotation());
            values.put("AccumulatedVehicleMiles", bean.getAccumulatedVehicleMiles());
            values.put("ElaspsedEngineHour", bean.getElaspsedEngineHour());
            values.put("MotorCarrier", bean.getMotorCarrier());
            values.put("ShippingDocumentNo", bean.getShippingDocumentNo());
            values.put("TrailerNo", bean.getTrailerNo());
            values.put("TimeZoneOffsetUTC", bean.getTimeZoneOffsetUTC());
            values.put("CheckSum", bean.getCheckSum());
            values.put("CoDriverId", bean.getCoDriverId());
            eventId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT,
                    "_id,ModifiedBy,ModifiedDate", values);

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Log.i("EventDB", "Finally Error: " + e.getMessage());
                Utility.printError(e.getMessage());
            }
        }
        return eventId;
    }

    public static boolean EventUpdate(String eventDateTime, int recordOrigin, int status, int driverId, int dailyLogId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            int id = getEventId(driverId, eventDateTime);

            ContentValues values = new ContentValues();
            values.put("EventRecordOrigin", recordOrigin);
            values.put("EventRecordStatus", status);
            values.put("DailyLogId", dailyLogId);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{id + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    public static boolean EventUpdate(int id, int status, int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("EventRecordStatus", status);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());

            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{id + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    public static boolean EventUpdate(int id, int status, int driverId, int dailyLogId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            // values.put("EventRecordOrigin", recordOrigin);
            values.put("EventRecordStatus", status);
            values.put("DailyLogId", dailyLogId);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{id + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    public static boolean EventTransfer(int eventId, int driverId, int coDriverId, String logDate) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int dailyLogId = 0;
        boolean recordStatus = false;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where LogDate=? and driverId=?"
                    , new String[]{logDate.toString(), coDriverId + ""});
            if (cursor.moveToNext()) {
                dailyLogId = cursor.getInt(0);
            }

            ContentValues values = new ContentValues();

            values.put("DailyLogId", dailyLogId);
            values.put("CoDriverId", driverId);
            values.put("DriverId", coDriverId);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{eventId + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    public static boolean EventUpdate(int id, int recordOrigin, int status, int driverId, int dailyLogId, String shipId, String trailerId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("EventRecordOrigin", recordOrigin);
            values.put("EventRecordStatus", status);
            values.put("DailyLogId", dailyLogId);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("ShippingDocumentNo", shipId);
            values.put("TrailerNo", trailerId);
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{id + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }

    public static boolean EventUpdate(int id, int recordOrigin, int status, int driverId, int dailyLogId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean recordStatus = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("EventRecordOrigin", recordOrigin);
            values.put("EventRecordStatus", status);
            values.put("DailyLogId", dailyLogId);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " _id= ?",
                    new String[]{id + ""});
            recordStatus = true;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(EventDB.class.getName() + "::EventUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return recordStatus;
    }


    public static boolean EventCopy(int eventId, int origin, int status, int driverId, int dailyLogId) {
        boolean recordStatus = false;
        try {
            EventBean bean = EventGetById(eventId);
            bean.setEventSequenceId(getEventSequenceId());
            bean.setEventRecordOrigin(origin);
            bean.setEventRecordStatus(status);
            bean.setDriverId(driverId);
            bean.setDailyLogId(dailyLogId);
            bean.setCreatedDate(Utility.getCurrentDateTime());
            bean.setSyncFg(0);
            EventSave(bean);
            recordStatus = true;
        } catch (Exception e) {
            LogFile.write(EventDB.class.getName() + "::EventCopy Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {

        }
        return recordStatus;
    }


    // Created By: Deepak Sharma
    // Created Date: 15 January 2015
    // Purpose: get events unAssignedEvent
    public static JSONArray GetEventSync(int logId) {
        JSONArray array = new JSONArray();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,DriverId ,OnlineEventId ,EventSequenceId ,EventType ,EventCode ,EventCodeDescription ,OdometerReading ,EngineHour," +
                    " EventRecordOrigin ,EventRecordStatus ,EventDateTime ,Latitude ,Longitude ,LocationDescription ,DailyLogId ,CreatedBy ,CreatedDate ,ModifiedBy " +
                    ",ModifiedDate ,StatusId ,SyncFg,VehicleId,CheckSum,DistanceSinceLastValidCoordinate ,MalfunctionIndicatorFg ,DataDiagnosticIndicatorFg" +
                    " ,DiagnosticCode ,AccumulatedVehicleMiles ,ElaspsedEngineHour ,MotorCarrier,ShippingDocumentNo,TrailerNo ,TimeZoneOffsetUTC,Annotation,CoDriverId from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where SyncFg=? and DailyLogId=?", new String[]{"0", logId + ""});
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();

                obj.put("VEHICLEID", cursor.getInt(cursor.getColumnIndex("VehicleId")));
                obj.put("EVENTID", cursor.getInt(cursor.getColumnIndex("_id")));
                obj.put("EVENTSEQUENCEID", cursor.getInt(cursor.getColumnIndex("EventSequenceId")));
                obj.put("EVENTTYPE", cursor.getInt(cursor.getColumnIndex("EventType")));
                obj.put("EVENTCODE", cursor.getInt(cursor.getColumnIndex("EventCode")));
                String description = cursor.getString(cursor.getColumnIndex("EventCodeDescription"));

                obj.put("EVENTCODEDESCRIPTION", description);
                obj.put("ODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("OdometerReading"))));
                obj.put("ENGINEHOUR", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineHour"))));
                obj.put("EVENTRECORDORIGIN", cursor.getInt(cursor.getColumnIndex("EventRecordOrigin")));
                obj.put("EVENTRECORDSTATUS", cursor.getInt(cursor.getColumnIndex("EventRecordStatus")));
                obj.put("EVENTDATETIME", Utility.getDateTimeForServer(cursor.getString(cursor.getColumnIndex("EventDateTime"))));
                try {
                    obj.put("LATITUDE", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Latitude"))));
                    obj.put("LONGITUDE", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Longitude"))));
                } catch (Exception exe) {

                    obj.put("LATITUDE", "0");
                    obj.put("LONGITUDE", "0");
                }
                obj.put("LOCATIONDESCRIPTION", cursor.getString(cursor.getColumnIndex("LocationDescription")));
                obj.put("EVENTCREATEDBY", cursor.getInt(cursor.getColumnIndex("CreatedBy")));
                obj.put("EVENTCREATEDDATE", Utility.getDateTimeForServer(cursor.getString(cursor.getColumnIndex("CreatedDate"))));
                obj.put("MODIFIEDBY", cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                String modifiedDate = cursor.getString(cursor.getColumnIndex("ModifiedDate"));
                modifiedDate = (modifiedDate == null || modifiedDate.isEmpty()) ? "1970-01-01 00:00:00" : modifiedDate;
                obj.put("MODIFIEDDATE", Utility.getDateTimeForServer(modifiedDate));
                obj.put("DRIVERID", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("DAILYLOGID", cursor.getInt(cursor.getColumnIndex("DailyLogId")));
                obj.put("Annotation", cursor.getString(cursor.getColumnIndex("Annotation")));
                obj.put("CheckSum", cursor.getString(cursor.getColumnIndex("CheckSum")));

                double distanceSince = 0d;
                double accumulatedVehicleMiles = 0d;
                double elaspsedEngineHour = 0d;

                try {
                    distanceSince = Double.parseDouble(cursor.getString(cursor.getColumnIndex("DistanceSinceLastValidCoordinate")));
                    accumulatedVehicleMiles = Double.parseDouble(cursor.getString(cursor.getColumnIndex("AccumulatedVehicleMiles")));
                    elaspsedEngineHour = Double.parseDouble(cursor.getString(cursor.getColumnIndex("ElaspsedEngineHour")));
                } catch (Exception exe) {
                }

                obj.put("DistanceSinceLastValidCoordinate", distanceSince);
                obj.put("AccumulatedVehicleMiles", accumulatedVehicleMiles);
                obj.put("ElaspsedEngineHour", elaspsedEngineHour);
                obj.put("MalfunctionIndicatorFg", cursor.getInt(cursor.getColumnIndex("MalfunctionIndicatorFg")));
                obj.put("DataDiagnosticIndicatorFg", cursor.getInt(cursor.getColumnIndex("DataDiagnosticIndicatorFg")));
                obj.put("DiagnosticCode", cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
                obj.put("MotorCarrier", cursor.getString(cursor.getColumnIndex("MotorCarrier")));
                obj.put("ShippingDocumentNo", cursor.getString(cursor.getColumnIndex("ShippingDocumentNo")));
                obj.put("TrailerNo", cursor.getString(cursor.getColumnIndex("TrailerNo")));
                obj.put("TimeZoneOffsetUTC", cursor.getString(cursor.getColumnIndex("TimeZoneOffsetUTC")));
                obj.put("CoDriverId", cursor.getString(cursor.getColumnIndex("CoDriverId")));
                array.put(obj);
            }

        } catch (Exception exe) {
            Log.e("EventList", exe.getMessage());
            LogFile.write(EventDB.class.getName() + "::GetEventSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return array;
    }


    // Created By: Deepak Sharma
    // Created Date: 17 July 2016
    // Purpose: get unidentified driving time
    public static int getUnidentifiedTime(String startDate) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int time = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            String query = "select EventDateTime,case when EventType=1 and EventCode=3 then 1 else 0 end DrivingFg from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " where ((EventType=1 and EventCode>=3 and DriverId=?) or (EventType=5 and EventCode=1)) and EventDateTime>=? and EventRecordStatus=1 order by EventDateTime";
            cursor = database.rawQuery(query, new String[]{Utility.unIdentifiedDriverId + "", startDate});
            String eventDate = "";
            int drivingFg = 0;
            while (cursor.moveToNext()) {
                drivingFg = cursor.getInt(cursor.getColumnIndex("DrivingFg"));
                String date = cursor.getString(cursor.getColumnIndex("EventDateTime"));
                // if login record
                if (drivingFg == 0) {
                    if (!eventDate.isEmpty()) {
                        time += Utility.getDiffMins(eventDate, date) * 60;
                        eventDate = "";
                    }
                } else {
                    if (eventDate.isEmpty())
                        eventDate = date;
                }

            }

            if (!eventDate.isEmpty()) {
                time += Utility.getDiffMins(eventDate, Utility.getCurrentDateTime()) * 60;
            }

        } catch (Exception exe) {
            LogFile.write(EventDB.class.getName() + "::getUnidentifiedTime Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return time;
    }


}
