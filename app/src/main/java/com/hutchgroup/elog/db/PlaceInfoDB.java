package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.PlaceBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import java.util.ArrayList;

/**
 * Created by deepak.sharma on 3/23/2016.
 */
public class PlaceInfoDB {
    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: check duplicate account
    private static int checkDuplicate(int featureId) {
        int recordId = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select FEATURE_ID from "
                    + MySQLiteOpenHelper.TABLE_PLACE
                    + " where FEATURE_ID=?", new String[]{featureId + ""});
            if (cursor.moveToFirst()) {
                recordId = cursor.getInt(0);

            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(PlaceInfoDB.class.getName() + "::checkDuplicate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
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
    // Created Date: 14 April 2016
    // Purpose: add or update carrier in database
    public static boolean Save(ArrayList<PlaceBean> lst) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (int i = 0; i < lst.size(); i++) {
                PlaceBean bean = lst.get(i);
                values.put("FEATURE_ID", bean.getFEATURE_ID());
                values.put("FEATURE_NAME", bean.getFEATURE_NAME());
                values.put("FEATURE_CLASS", bean.getFEATURE_CLASS());
                values.put("STATE_ALPHA", bean.getSTATE_ALPHA());
                values.put("STATE_NUMERIC", bean.getSTATE_NUMERIC());
                values.put("COUNTY_NAME", bean.getCOUNTY_NAME());
                values.put("COUNTY_NUMERIC", bean.getCOUNTY_NUMERIC());
                values.put("PRIMARY_LAT_DMS", bean.getPRIMARY_LAT_DMS());
                values.put("PRIM_LONG_DMS", bean.getPRIM_LONG_DMS());
                values.put("PRIM_LAT_DEC", bean.getPRIM_LAT_DEC());
                values.put("PRIM_LONG_DEC", bean.getPRIM_LONG_DEC());
                values.put("SOURCE_LAT_DMS", bean.getSOURCE_LAT_DMS());
                values.put("SOURCE_LONG_DMS", bean.getSOURCE_LONG_DMS());
                values.put("SOURCE_LAT_DEC", bean.getSOURCE_LAT_DEC());
                values.put("SOURCE_LONG_DEC", bean.getSOURCE_LONG_DEC());
                values.put("ELEV_IN_M", bean.getELEV_IN_M());
                values.put("ELEV_IN_FT", bean.getELEV_IN_FT());
                values.put("MAP_NAME", bean.getMAP_NAME());
                values.put("DATE_CREATED", bean.getDATE_CREATED());
                values.put("DATE_EDITED", bean.getDATE_EDITED());

                database.insert(MySQLiteOpenHelper.TABLE_PLACE,
                        null, values);


            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(PlaceInfoDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }
        return status;

    }
}
