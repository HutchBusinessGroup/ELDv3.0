package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.TrailerBean;
import com.hutchgroup.elog.beans.VehicleBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import java.util.ArrayList;

/**
 * Created by SAMSUNG on 30-12-2016.
 */

public class TrailerDB {

    // Created By: Deepak Sharma
    // Created Date: 30 December 2016
    // Purpose: hook trailer info
    public static boolean hook(TrailerBean bean) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("hookedFg", 1);
            values.put("hookDate", bean.getHookDate());
            values.put("startOdometer", bean.getStartOdometer());
            values.put("TrailerId", bean.getTrailerId());
            values.put("driverId", bean.getDriverId());
            values.put("latitude1", bean.getLatitude1());
            values.put("longitude1", bean.getLongitude1());
            values.put("SyncFg", 0);
            database.insert(MySQLiteOpenHelper.TABLE_TRAILER_STATUS,
                    "_id,unhookDate,endOdometer,latitude2,longitude2,modifiedBy", values);

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(TrailerDB.class.getName() + "::hook Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 30 December 2016
    // Purpose: add trailer info
    public static boolean unhook(TrailerBean bean) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("hookedFg", 0);
            values.put("unhookDate", bean.getUnhookDate());
            values.put("endOdometer", bean.getEndOdometer());
            values.put("modifiedBy", bean.getDriverId());
            values.put("latitude2", bean.getLatitude2());
            values.put("longitude2", bean.getLongitude2());
            values.put("SyncFg", 0);
            Cursor cursor = database.rawQuery("select _id from "
                    + MySQLiteOpenHelper.TABLE_TRAILER_STATUS
                    + " where TrailerId=? order by _id desc LIMIT 1", new String[]{bean.getTrailerId() + ""});
            if (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                database.update(MySQLiteOpenHelper.TABLE_TRAILER_STATUS, values,
                        " _id= ?",
                        new String[]{id + ""});
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(TrailerDB.class.getName() + "::hook Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }
        return status;
    }

    public static ArrayList<String> getHookedTrailer() {
        ArrayList<String> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            //Compulsory Power Unit
            list.add(Utility.vehicleId + "");
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            cursor = database.rawQuery("select TrailerId from "
                            + MySQLiteOpenHelper.TABLE_TRAILER_STATUS + " Where hookedFg=1 order by _id"
                    , null);
            while (cursor.moveToNext()) {
                list.add(cursor.getString(0));
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DTCDB.class.getName() + "::AxleInfoGet Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                // TODO: handle exception
            }
        }
        return list;
    }
}
