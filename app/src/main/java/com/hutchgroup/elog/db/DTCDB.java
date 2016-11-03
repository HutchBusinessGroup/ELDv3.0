package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.DTCBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import java.util.ArrayList;

/**
 * Created by Dev-1 on 11/3/2016.
 */

public class DTCDB {

    // Created By: Deepak Sharma
    // Created Date: 14 April 2016
    // Purpose: add or update carrier in database
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
                        + " where spn=? order by DateTime desc LIMIT 1", new String[]{bean.getSpn() + ""});

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

}
