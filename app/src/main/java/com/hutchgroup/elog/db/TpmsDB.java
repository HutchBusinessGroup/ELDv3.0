package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.TPMSBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Deepak on 12/21/2016.
 */

public class TpmsDB {

    // Created By: Deepak Sharma
    // Created Date: 22 December 2016
    // Purpose: update TPMS for web sync
    public static JSONArray TpmsSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_TPMS, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(TpmsDB.class.getName() + "::TpmsSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
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
    public static JSONArray getTPMSDataSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select _id,SensorId,Temperature,Pressure,Voltage,CreatedDate,ModifiedDate ,DriverId ,VehicleId ,SyncFg from "
                            + MySQLiteOpenHelper.TABLE_TPMS + " Where SyncFg=0"
                    , null);
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("SensorId", cursor.getString(cursor.getColumnIndex("SensorId")));
                obj.put("Temperature", cursor.getInt(cursor.getColumnIndex("Temperature")));
                obj.put("Pressure", cursor.getInt(cursor.getColumnIndex("Pressure")));
                double voltage = 0d;
                try {
                    voltage = Double.parseDouble(cursor.getString(cursor.getColumnIndex("Voltage")));
                } catch (Exception exe) {
                }
                obj.put("Voltage", voltage);
                obj.put("CreatedDate", cursor.getString(cursor.getColumnIndex("CreatedDate")));
                obj.put("ModifiedDate", cursor.getString(cursor.getColumnIndex("ModifiedDate")));
                obj.put("VehicleId", cursor.getInt(cursor.getColumnIndex("VehicleId")));
                obj.put("DriverId", cursor.getInt(cursor.getColumnIndex("DriverId")));
                array.put(obj);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(TpmsDB.class.getName() + "::getTpmsDataSync Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
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

    public static boolean Save(ArrayList<TPMSBean> list) {

        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            for (TPMSBean bean : list) {

                ContentValues values = new ContentValues();

                values.put("SensorId", bean.getSensorId());
                values.put("ModifiedDate", bean.getModifiedDate());
                if (bean.isNew()) {
                    values.put("Temperature", bean.getTemperature());
                    values.put("Pressure", bean.getPressure());
                    values.put("Voltage", bean.getVoltage());
                    values.put("CreatedDate", bean.getCreatedDate());
                    values.put("VehicleId", Utility.vehicleId);
                    values.put("DriverId", bean.getDriverId());
                    values.put("SyncFg", 0);
                    database.insert(MySQLiteOpenHelper.TABLE_TPMS,
                            "_id", values);
                } else {
                    database.update(MySQLiteOpenHelper.TABLE_ALERT, values,
                            " CreatedDate= ? and SensorId=?",
                            new String[]{bean.getCreatedDate(), bean.getSensorId()});
                }
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(TpmsDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }

        return status;
    }

    public static boolean Save(TPMSBean bean) {

        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();

            values.put("SensorId", bean.getSensorId());
            values.put("ModifiedDate", bean.getModifiedDate());
            if (bean.isNew()) {
                values.put("Temperature", bean.getTemperature());
                values.put("Pressure", bean.getPressure());
                values.put("Voltage", bean.getVoltage());
                values.put("CreatedDate", bean.getCreatedDate());
                values.put("VehicleId", Utility.vehicleId);
                values.put("DriverId", bean.getDriverId());
                values.put("SyncFg", 0);
                database.insert(MySQLiteOpenHelper.TABLE_TPMS,
                        "_id", values);
            } else {
                database.update(MySQLiteOpenHelper.TABLE_ALERT, values,
                        " CreatedDate= ? and SensorId=?",
                        new String[]{bean.getCreatedDate(), bean.getSensorId()});
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(TpmsDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }

        return status;
    }
}
