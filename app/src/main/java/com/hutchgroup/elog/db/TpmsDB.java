package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.TPMSBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import java.util.ArrayList;

/**
 * Created by Deepak on 12/21/2016.
 */

public class TpmsDB {

    private static boolean Save(ArrayList<TPMSBean> list) {

        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
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
