package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.AlertBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

/**
 * Created by Deepak on 11/30/2016.
 */

public class AlertDB {
    public static boolean Save(AlertBean bean) {
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
            values.put("Duration", 0);
            values.put("Scores", 0);
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
