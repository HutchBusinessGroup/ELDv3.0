package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.GpsSignalBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import java.util.ArrayList;

/**
 * Created by Vaneet.Sethi on 4/6/2016.
 */
public class TrackingDB {

    public static void addGpsSignal(String signal) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("signal", signal);
            database.insert(MySQLiteOpenHelper.TABLE_GpsLocation, "_id", values);

        } catch (Exception e) {
            e.printStackTrace();
            LogFile.write(TrackingDB.class.getName() + "::addGpsSignal Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
    }

    public static long removeGpsSignal(String id) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        long res = -1;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String[] ids = id.split(",");
            for (String _id : ids) {
                res = database.delete(MySQLiteOpenHelper.TABLE_GpsLocation,
                        "_id=?", new String[]{_id});
            }

        } catch (Exception e) {
            System.out.println("removeGpsSignal");
            e.printStackTrace();
            LogFile.write(TrackingDB.class.getName() + "::removeGpsSignal Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return res;
    }

    public static ArrayList<GpsSignalBean> getGpsSignalList() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<GpsSignalBean> lstSignal = new ArrayList<GpsSignalBean>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,signal from "
                            + MySQLiteOpenHelper.TABLE_GpsLocation + " LIMIT 50"
                    , null);

            while (cursor.moveToNext()) {
                GpsSignalBean objBean = new GpsSignalBean();
                objBean.set_id(cursor.getInt(0));
                objBean.set_gpsSignal(cursor.getString(1));
                lstSignal.add(objBean);
            }

        } catch (Exception e) {
            // TODO: handle exception
            LogFile.write(TrackingDB.class.getName() + "::getGpsSignalList Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return lstSignal;
    }
}
