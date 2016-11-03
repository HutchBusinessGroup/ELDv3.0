package com.hutchgroup.elog.common;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;

import com.hutchgroup.elog.beans.DiagnosticIndicatorBean;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;
import com.hutchgroup.elog.db.MySQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiagnosticMalfunction {
    public static DiagnosticMalfunctionNotification mListener = null;

    public DiagnosticMalfunction() {

    }

    // Created By: Deepak Sharma
    // Created Date: 14 July 2016
    // Purpose: get active malfunction active codes
    private static String getMalfunctionCodes() {
        StringBuilder codes = new StringBuilder();
        if (DiagnosticIndicatorBean.PowerMalfunctionFg) {
            codes.append("'P',");
        }

        if (DiagnosticIndicatorBean.EngineSynchronizationMalfunctionFg) {
            codes.append("'E',");
        }

        if (DiagnosticIndicatorBean.TimingMalfunctionFg) {
            codes.append("'T',");
        }

        if (DiagnosticIndicatorBean.PositioningMalfunctionFg) {
            codes.append("'L',");
        }

        if (DiagnosticIndicatorBean.DataRecordingMalfunctionFg) {
            codes.append("'R',");
        }

        if (DiagnosticIndicatorBean.DataTransferMalfunctionFg) {
            codes.append("'S',");
        }

        if (DiagnosticIndicatorBean.OtherELDDetectedMalfunctionFg) {
            codes.append("'O',");
        }
        codes.setLength(codes.length() - 1);

        return codes.toString();
    }

    // Created By: Deepak Sharma
    // Created Date: 14 July 2016
    // Purpose: get active diagnostic active codes
    private static String getDiagnosticCodes() {
        StringBuilder codes = new StringBuilder();
        if (DiagnosticIndicatorBean.PowerDiagnosticFg) {
            codes.append("'1',");
        }

        if (DiagnosticIndicatorBean.EngineSynchronizationDiagnosticFg) {
            codes.append("'2',");
        }

        if (DiagnosticIndicatorBean.MissingElementDiagnosticFg) {
            codes.append("'3',");
        }

        if (DiagnosticIndicatorBean.DataTransferDiagnosticFg) {
            codes.append("'4',");
        }

        if (DiagnosticIndicatorBean.UnidentifiedDrivingDiagnosticFg) {
            codes.append("'5',");
        }

        if (DiagnosticIndicatorBean.OtherELDIdentifiedDiagnosticFg) {
            codes.append("'6',");
        }

        codes.setLength(codes.length() - 1);

        return codes.toString();
    }

    // Created By: Deepak Sharma
    // Created Date: 14 July 2016
    // Purpose: get active malfunction list
    public static ArrayList<EventBean> getActiveDiagnosticMalfunctionList(int eventCode) {
        ArrayList<EventBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            String codes = eventCode == 1 ? getMalfunctionCodes() : getDiagnosticCodes();
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select max(EventDateTime)  EventDateTime,EventCodeDescription, DiagnosticCode from " +
                    MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where EventType=7 and EventCode=? and DiagnosticCode in (" + codes + ") group by EventCodeDescription, DiagnosticCode Order by EventDateTime desc ", new String[]{eventCode + ""});
            while (cursor.moveToNext()) {
                EventBean bean = new EventBean();
                bean.setEventCodeDescription(cursor.getString(cursor.getColumnIndex("EventCodeDescription")));
                bean.setEventDateTime(cursor.getString(cursor.getColumnIndex("EventDateTime")));
                bean.setDiagnosticCode(cursor.getString(cursor.getColumnIndex("DiagnosticCode")));
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

    // Created By: Deepak Sharma
    // Created Date: 13 July 2016
    // Purpose: get Diagnostic and malfunction Indicator
    public static void getDiagnosticIndicator() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id, PowerDiagnosticFg , EngineSynchronizationDiagnosticFg , MissingElementDiagnosticFg , DataTransferDiagnosticFg , UnidentifiedDrivingDiagnosticFg , OtherELDIdentifiedDiagnosticFg , PowerMalfunctionFg , EngineSynchronizationMalfunctionFg , TimingMalfunctionFg , PositioningMalfunctionFg , DataRecordingMalfunctionFg , DataTransferMalfunctionFg , OtherELDDetectedMalfunctionFg " +
                    " from " + MySQLiteOpenHelper.TABLE_DIAGNOSTIC_INDICATOR, null);
            if (cursor.moveToNext()) {
                DiagnosticIndicatorBean.PowerDiagnosticFg = cursor.getInt(cursor.getColumnIndex("PowerDiagnosticFg")) == 1;
                DiagnosticIndicatorBean.EngineSynchronizationDiagnosticFg = cursor.getInt(cursor.getColumnIndex("EngineSynchronizationDiagnosticFg")) == 1;
                DiagnosticIndicatorBean.MissingElementDiagnosticFg = cursor.getInt(cursor.getColumnIndex("MissingElementDiagnosticFg")) == 1;
                DiagnosticIndicatorBean.DataTransferDiagnosticFg = cursor.getInt(cursor.getColumnIndex("DataTransferDiagnosticFg")) == 1;
                DiagnosticIndicatorBean.UnidentifiedDrivingDiagnosticFg = cursor.getInt(cursor.getColumnIndex("UnidentifiedDrivingDiagnosticFg")) == 1;
                DiagnosticIndicatorBean.OtherELDIdentifiedDiagnosticFg = cursor.getInt(cursor.getColumnIndex("OtherELDIdentifiedDiagnosticFg")) == 1;
                DiagnosticIndicatorBean.PowerMalfunctionFg = cursor.getInt(cursor.getColumnIndex("PowerMalfunctionFg")) == 1;
                DiagnosticIndicatorBean.EngineSynchronizationMalfunctionFg = cursor.getInt(cursor.getColumnIndex("EngineSynchronizationMalfunctionFg")) == 1;
                DiagnosticIndicatorBean.TimingMalfunctionFg = cursor.getInt(cursor.getColumnIndex("TimingMalfunctionFg")) == 1;
                DiagnosticIndicatorBean.PositioningMalfunctionFg = cursor.getInt(cursor.getColumnIndex("PositioningMalfunctionFg")) == 1;
                DiagnosticIndicatorBean.DataRecordingMalfunctionFg = cursor.getInt(cursor.getColumnIndex("DataRecordingMalfunctionFg")) == 1;
                DiagnosticIndicatorBean.DataTransferMalfunctionFg = cursor.getInt(cursor.getColumnIndex("DataTransferMalfunctionFg")) == 1;
                DiagnosticIndicatorBean.OtherELDDetectedMalfunctionFg = cursor.getInt(cursor.getColumnIndex("OtherELDDetectedMalfunctionFg")) == 1;
            }

            Utility.dataDiagnosticIndicatorFg = getdataDiagnosticIndicatorFg();
            Utility.malFunctionIndicatorFg = getMalfunctionIndicatorFg();
        } catch (Exception e) {
            LogFile.write(DiagnosticIndicatorBean.class.getName() + "::getDiagnosticIndicator Error:" + e.getMessage(), LogFile.DIAGNOSTIC_MALFUNCTION, LogFile.ERROR_LOG);
        } finally {
            try {

                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {
            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 13 July 2016
    // Purpose: save Diagnostic Indicator
    public static void SaveDiagnosticIndicator(String col, boolean val) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            cursor = database.rawQuery("select _id" +
                    " from " + MySQLiteOpenHelper.TABLE_DIAGNOSTIC_INDICATOR, null);
            ContentValues values = new ContentValues();
            if (cursor.moveToNext()) {
                int _id = cursor.getInt(0);
                values.put(col, val);
                database.update(MySQLiteOpenHelper.TABLE_DIAGNOSTIC_INDICATOR, values,
                        " _id= ?",
                        new String[]{_id + ""});
            } else {

                values.put("PowerDiagnosticFg", DiagnosticIndicatorBean.PowerDiagnosticFg);
                values.put("EngineSynchronizationDiagnosticFg", DiagnosticIndicatorBean.EngineSynchronizationDiagnosticFg);
                values.put("MissingElementDiagnosticFg", DiagnosticIndicatorBean.MissingElementDiagnosticFg);
                values.put("DataTransferDiagnosticFg", DiagnosticIndicatorBean.DataTransferDiagnosticFg);
                values.put("UnidentifiedDrivingDiagnosticFg", DiagnosticIndicatorBean.UnidentifiedDrivingDiagnosticFg);
                values.put("OtherELDIdentifiedDiagnosticFg", DiagnosticIndicatorBean.OtherELDIdentifiedDiagnosticFg);
                values.put("PowerMalfunctionFg", DiagnosticIndicatorBean.PowerMalfunctionFg);
                values.put("EngineSynchronizationMalfunctionFg", DiagnosticIndicatorBean.EngineSynchronizationMalfunctionFg);
                values.put("TimingMalfunctionFg", DiagnosticIndicatorBean.TimingMalfunctionFg);
                values.put("PositioningMalfunctionFg", DiagnosticIndicatorBean.PositioningMalfunctionFg);
                values.put("DataRecordingMalfunctionFg", DiagnosticIndicatorBean.DataRecordingMalfunctionFg);
                values.put("DataTransferMalfunctionFg", DiagnosticIndicatorBean.DataTransferMalfunctionFg);
                values.put("OtherELDDetectedMalfunctionFg", DiagnosticIndicatorBean.OtherELDDetectedMalfunctionFg);
                database.insertOrThrow(MySQLiteOpenHelper.TABLE_DIAGNOSTIC_INDICATOR, "_id", values);
            }

        } catch (Exception e) {
            LogFile.write(DiagnosticIndicatorBean.class.getName() + "::SaveDiagnosticIndicator Error:" + e.getMessage(), LogFile.DIAGNOSTIC_MALFUNCTION, LogFile.ERROR_LOG);
        } finally {

            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {
            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 13 July 2016
    // Purpose: save/clear Diagnostic/malfunction events
    // eventCode: 1 -- malfunction logged, 2-- malfunction cleared, 3--diagnostic event logged, 4-- diagnostic event cleared
    public static void saveDiagnosticIndicatorByCode(String diagnosticCode, int eventCode, String column) {
        try {
            Utility.DiagnosticCode = diagnosticCode;
            boolean value = false, malfunctionFg = eventCode < 3;
            int eventType = 7;
            String description = "";

            switch (eventCode) {
                case 1:
                    Utility.malFunctionIndicatorFg = true;
                    value = true;
                    description = "An ELD malfunction logged: ";

                    break;
                case 2:
                    Utility.malFunctionIndicatorFg = getMalfunctionIndicatorFg();
                    description = "An ELD malfunction cleared: ";
                    break;
                case 3:
                    Utility.dataDiagnosticIndicatorFg = true;
                    value = true;
                    description = "An ELD data diagnostic event logged: ";
                    break;
                case 4:
                    Utility.dataDiagnosticIndicatorFg = getdataDiagnosticIndicatorFg();
                    description = "An ELD data diagnostic event cleared: ";
                    break;

            }
            // update inidicator on main activity
            if (mListener != null) {
                Log.i("dd", "TimeDifference: showstart");
                mListener.onDiagnoticMalfunctionUpdated(malfunctionFg);
                Log.i("dd", "TimeDifference: show end");
            }

            description += getDescription(diagnosticCode);

            // active driver should have data diagnostic event
            int driverId = Utility.activeUserId;
            if (driverId == 0) {
                driverId = Utility.unIdentifiedDriverId;
            }

            // get daily log id
            int logId = DailyLogDB.getDailyLog(driverId, Utility.getCurrentDate());

            // if dailylog record does not exists for the day
            if (logId == 0) {
                logId = DailyLogDB.DailyLogCreate(driverId, Utility.ShippingNumber, Utility.TrailerNumber, "");
            }
            String eventDateTime = Utility.getCurrentDateTime();
            if (DiagnosticIndicatorBean.TimingMalfunctionFg) {
                eventDateTime = Utility.convertUTCToLocalDateTime(GPSTracker.gpsTime);
            }

            // create event
            EventDB.EventCreate(eventDateTime, eventType, eventCode, description, 1, 1, logId, driverId, "");

            // save diagnostic indicators
            SaveDiagnosticIndicator(column, value);


        } catch (Exception e) {
            LogFile.write(DiagnosticMalfunction.class.getName() + "::saveDiagnosticByCode Error: " + e.getMessage() + ", Column: " + column + " , " + diagnosticCode, LogFile.DIAGNOSTIC_MALFUNCTION, LogFile.ERROR_LOG);
        }

    }

    // Created By: Deepak Sharma
    // Created Date: 13 July 2016
    // Purpose: get event description by Diagnostic Code
    private static String getDescription(String diagnosticCode) {
        String description = "";
        if (diagnosticCode.equals("P")) {
            description = "Power compliance";
        } else if (diagnosticCode.equals("E")) {
            description = "Engine synchronization compliance";
        } else if (diagnosticCode.equals("T")) {
            description = "Timing compliance";
        } else if (diagnosticCode.equals("L")) {
            description = "Positioning compliance";
        } else if (diagnosticCode.equals("R")) {
            description = "Data recording compliance";
        } else if (diagnosticCode.equals("S")) {
            description = "Data transfer compliance";
        } else if (diagnosticCode.equals("O")) {
            description = "Other";
        } else if (diagnosticCode.equals("1")) {
            description = "Power data diagnostic";
        } else if (diagnosticCode.equals("2")) {
            description = "Engine synchronization data diagnostic";
        } else if (diagnosticCode.equals("3")) {
            description = "Missing required data elements data diagnostic";
        } else if (diagnosticCode.equals("4")) {
            description = "Data transfer data diagnostic";
        } else if (diagnosticCode.equals("5")) {
            description = "Unidentified driving records data diagnostic";
        } else if (diagnosticCode.equals("6")) {
            description = "Other";
        }
        return description;
    }

    // Created By: Deepak Sharma
    // Created Date: 13 July 2016
    // Purpose: get Diagnostic Indicator status
    private static boolean getdataDiagnosticIndicatorFg() {
        return (DiagnosticIndicatorBean.EngineSynchronizationDiagnosticFg || DiagnosticIndicatorBean.UnidentifiedDrivingDiagnosticFg || DiagnosticIndicatorBean.MissingElementDiagnosticFg
                || DiagnosticIndicatorBean.DataTransferDiagnosticFg || DiagnosticIndicatorBean.PowerDiagnosticFg || DiagnosticIndicatorBean.OtherELDIdentifiedDiagnosticFg);
    }

    // Created By: Deepak Sharma
    // Created Date: 13 July 2016
    // Purpose: get Diagnostic Indicator status
    private static boolean getMalfunctionIndicatorFg() {
        return (DiagnosticIndicatorBean.EngineSynchronizationMalfunctionFg || DiagnosticIndicatorBean.PositioningMalfunctionFg || DiagnosticIndicatorBean.TimingMalfunctionFg
                || DiagnosticIndicatorBean.DataRecordingMalfunctionFg || DiagnosticIndicatorBean.DataTransferMalfunctionFg || DiagnosticIndicatorBean.OtherELDDetectedMalfunctionFg || DiagnosticIndicatorBean.PowerMalfunctionFg);
    }

    public interface DiagnosticMalfunctionNotification {
        void onDiagnoticMalfunctionUpdated(boolean malfunctionFg);
    }
}

