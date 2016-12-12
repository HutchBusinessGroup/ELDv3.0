package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.AlertBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Deepak on 11/30/2016.
 */

public class AlertDB {

    // Created By: Deepak Sharma
    // Created Date: 12 December 2016
    // Purpose: update Alert for web sync
    public static JSONArray AlertSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_ALERT, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(AlertDB.class.getName() + "::AlertSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return array;
    }

    // Created By: Deepak Sharma
    // Created Date: 12 December 2016
    // Purpose: get Alert for web sync
    public static JSONArray getAlertSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select _id,AlertCode,AlertName,AlertDateTime,Duration,Scores ,DriverId ,VehicleId ,SyncFg from "
                            + MySQLiteOpenHelper.TABLE_ALERT + " Where SyncFg=0"
                    , null);

            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("AlertDateTime", cursor.getString(cursor.getColumnIndex("AlertDateTime")));
                obj.put("AlertCode", cursor.getString(cursor.getColumnIndex("AlertCode")));
                obj.put("AlertName", cursor.getString(cursor.getColumnIndex("AlertName")));
                obj.put("Duration", cursor.getInt(cursor.getColumnIndex("Duration")));
                obj.put("Scores", cursor.getInt(cursor.getColumnIndex("Scores")));
                obj.put("VehicleId", cursor.getInt(cursor.getColumnIndex("VehicleId")));
                obj.put("DriverId", cursor.getInt(cursor.getColumnIndex("DriverId")));
                array.put(obj);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(AlertDB.class.getName() + "::getAlertSync Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                // TODO: handle exception
            }
        }
        return array;
    }

    public static boolean Save(String code, String name, String date, int score, int duration, int driverId) {
        AlertBean bean = new AlertBean();
        bean.setAlertCode("SpeedVL");
        bean.setAlertName("Speed Violation");
        bean.setAlertDateTime(Utility.getCurrentDateTime());
        bean.setScores(score);
        bean.setDuration(duration);
        bean.setDriverId(driverId);
        return AlertDB.Save(bean);
    }

    private static boolean Save(AlertBean bean) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("AlertCode", bean.getAlertCode());
            values.put("AlertName", bean.getAlertName());
            values.put("AlertDateTime", bean.getAlertDateTime());
            values.put("Duration", bean.getDuration());
            values.put("Scores", bean.getScores());
            values.put("DriverId", bean.getDriverId());
            values.put("VehicleId", Utility.vehicleId);
            values.put("SyncFg", 0);
            database.insert(MySQLiteOpenHelper.TABLE_ALERT,
                    "_id", values);
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(AlertDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }
        return status;

    }

    public static boolean Update(String code, int duration, int score) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select _id from "
                    + MySQLiteOpenHelper.TABLE_ALERT
                    + " where AlertCode=? order by _id desc LIMIT 1", new String[]{code});

            if (cursor.moveToFirst()) {

                ContentValues values = new ContentValues();

                values.put("Duration", duration);
                values.put("Scores", score);
                values.put("SyncFg", 0);

                int id = cursor.getInt(0);
                database.update(MySQLiteOpenHelper.TABLE_ALERT, values,
                        " _id= ?",
                        new String[]{id + ""});
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(AlertDB.class.getName() + "::Update Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            cursor.close();
            database.close();
            helper.close();
        }
        return status;

    }
}
