package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.DTCBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Dev-1 on 11/3/2016.
 */

public class DTCDB {

    // Created By: Deepak Sharma
    // Created Date: 3 November 2016
    // Purpose: add or update dtc code in database
    public static boolean Save(ArrayList<DTCBean> lst) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (int i = 0; i < lst.size(); i++) {
                DTCBean bean = lst.get(i);
                Cursor cursor = database.rawQuery("select DateTime from "
                        + MySQLiteOpenHelper.TABLE_DTC
                        + " where spn=? and fmi=? and Occurrence=? order by DateTime desc LIMIT 1", new String[]{bean.getSpn() + "", bean.getFmi() + "", bean.getOccurence() + ""});

                boolean isExist = false;
                if (cursor.moveToFirst()) {
                    String dtcDate = Utility.dateOnlyStringGet(cursor.getString(0));
                    isExist = dtcDate.equals(Utility.getCurrentDate());
                }
                cursor.close();

                if (!isExist) {
                    values.put("DateTime", bean.getDateTime());
                    values.put("spn", bean.getSpn());
                    values.put("Protocol", bean.getProtocol());
                    values.put("spnDescription", bean.getSpnDescription());
                    values.put("fmi", bean.getFmi());
                    values.put("fmiDescription", bean.getFmiDescription());
                    values.put("Occurrence", bean.getOccurence());
                    values.put("SyncFg", 0);
                    values.put("status", bean.getStatus());
                    database.insert(MySQLiteOpenHelper.TABLE_DTC,
                            "_id", values);
                }

            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(DTCDB.class.getName() + "::DTCDB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: check duplicate account
    public static ArrayList<DTCBean> getDTCCode() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        ArrayList<DTCBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select DateTime, spn, Protocol, spnDescription ,fmi ,fmiDescription ,Occurrence, status from "
                            + MySQLiteOpenHelper.TABLE_DTC + " Where DateTime>=?"
                    , new String[]{Utility.getCurrentDate()});
            Utility.dtcList.clear();
            while (cursor.moveToNext()) {
                DTCBean dtcBean = new DTCBean();
                dtcBean.setSpn(cursor.getInt(cursor.getColumnIndex("spn")));
                dtcBean.setSpnDescription(cursor.getString(cursor.getColumnIndex("spnDescription")));
                dtcBean.setFmi(cursor.getInt(cursor.getColumnIndex("fmi")));
                dtcBean.setFmiDescription(cursor.getString(cursor.getColumnIndex("fmiDescription")));
                dtcBean.setDateTime(cursor.getString(cursor.getColumnIndex("DateTime")));
                dtcBean.setProtocol(cursor.getString(cursor.getColumnIndex("Protocol")));
                dtcBean.setOccurence(cursor.getInt(cursor.getColumnIndex("Occurrence")));
                dtcBean.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
                Utility.dtcList.add(dtcBean);
                list.add(dtcBean);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DTCDB.class.getName() + "::getDTCCode Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
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

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: check duplicate account
    public static JSONArray getDTCCodeSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select DateTime, spn, Protocol, spnDescription ,fmi ,fmiDescription ,Occurrence, status from "
                            + MySQLiteOpenHelper.TABLE_DTC + " Where SyncFg=0"
                    , null);

            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("SPN", cursor.getInt(cursor.getColumnIndex("spn")));
                obj.put("SpnDescription", cursor.getString(cursor.getColumnIndex("spnDescription")));
                obj.put("FMI", cursor.getInt(cursor.getColumnIndex("fmi")));
                obj.put("FmiDescription", cursor.getString(cursor.getColumnIndex("fmiDescription")));
                obj.put("DTCDateTime", Utility.getDateTimeForServer(cursor.getString(cursor.getColumnIndex("DateTime"))));
                obj.put("Protocol", cursor.getString(cursor.getColumnIndex("Protocol")));
                obj.put("Occurrence", cursor.getInt(cursor.getColumnIndex("Occurrence")));
                obj.put("DTCStatus", cursor.getInt(cursor.getColumnIndex("status")));
                obj.put("VehicleId", Utility.vehicleId);
                obj.put("DriverId", Utility.onScreenUserId);
                array.put(obj);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DTCDB.class.getName() + "::getDTCCodeSync Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
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

    // Created By: Deepak Sharma
    // Created Date: 27 July 2016
    // Purpose: update DVIR for web sync
    public static JSONArray DTCSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        int inspectionId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_DTC, values,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DTCDB.class.getName() + "::DVIRSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
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
}
