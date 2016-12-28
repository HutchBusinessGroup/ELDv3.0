package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.AxleBean;
import com.hutchgroup.elog.beans.VehicleBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import java.util.ArrayList;

/**
 * Created by SAMSUNG on 28-12-2016.
 */

public class VehicleDB {

    // Created By: Deepak Sharma
    // Created Date: 28 December 2016
    // Purpose: check duplicate vehicle
    private static int checkDuplicate(int vehicleId) {
        int recordId = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select VehicleId from "
                    + MySQLiteOpenHelper.TABLE_TRAILER
                    + " where VehicleId=?", new String[]{vehicleId + ""});
            if (cursor.moveToFirst()) {
                recordId = cursor.getInt(0);

            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + "::checkDuplicate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                // TODO: handle exception
            }
        }

        return recordId;
    }

    // Created By: Deepak Sharma
    // Created Date: 28 December 2016
    // Purpose: add or update vehicles in database
    public static boolean Save(ArrayList<VehicleBean> lst) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (int i = 0; i < lst.size(); i++) {
                VehicleBean bean = lst.get(i);
                values.put("VehicleId", bean.getVehicleId());
                values.put("UnitNo", bean.getUnitNo());
                values.put("PlateNo", bean.getPlateNo());
                values.put("TotalAxle", bean.getTotalAxle());
                Cursor cursor = database.rawQuery("select VehicleId from "
                        + MySQLiteOpenHelper.TABLE_TRAILER
                        + " where VehicleId=?", new String[]{bean.getVehicleId() + ""});
                int vehicleId = 0;
                if (cursor.moveToNext()) {
                    vehicleId = cursor.getInt(0);
                }
                cursor.close();

                if (vehicleId == 0) {
                    database.insert(MySQLiteOpenHelper.TABLE_TRAILER,
                            null, values);

                } else {
                    database.update(MySQLiteOpenHelper.TABLE_TRAILER, values,
                            " VehicleId= ?",
                            new String[]{bean.getVehicleId() + ""});
                }
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(VehicleDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 28 December 2016
    // Purpose: add or update Axle info in database
    public static boolean SaveAxleInfo(ArrayList<AxleBean> lst) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (int i = 0; i < lst.size(); i++) {
                AxleBean bean = lst.get(i);
                values.put("axleId", bean.getAxleId());
                values.put("VehicleId", bean.getVehicleId());
                values.put("axleNo", bean.getAxleNo());
                values.put("axlePosition", bean.getAxlePosition());
                values.put("doubleTireFg", (bean.isDoubleTireFg() ? 1 : 0));
                values.put("frontTireFg", (bean.isFrontTireFg() ? 1 : 0));
                values.put("PowerUnitFg", (bean.isPowerUnitFg() ? 1 : 0));
                values.put("sensorIds", bean.getSensorIds());
                values.put("pressures", bean.getPressures());
                values.put("temperatures", bean.getTemperatures());

                Cursor cursor = database.rawQuery("select axleId from "
                        + MySQLiteOpenHelper.TABLE_AXLE_INFO
                        + " where axleId=?", new String[]{bean.getAxleId() + ""});
                int axleId = 0;
                if (cursor.moveToNext()) {
                    axleId = cursor.getInt(0);
                }

                cursor.close();

                if (axleId == 0) {
                    database.insert(MySQLiteOpenHelper.TABLE_AXLE_INFO,
                            null, values);

                } else {
                    database.update(MySQLiteOpenHelper.TABLE_AXLE_INFO, values,
                            " axleId= ?",
                            new String[]{bean.getAxleId() + ""});
                }
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(VehicleDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }
        return status;
    }
}
