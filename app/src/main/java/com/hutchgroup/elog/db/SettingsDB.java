package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;
import android.util.Log;

import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.beans.MessageBean;
import com.hutchgroup.elog.beans.SettingsBean;
import com.hutchgroup.elog.beans.TripInspectionBean;
import com.hutchgroup.elog.beans.UserBean;
import com.hutchgroup.elog.common.ChatClient;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import org.json.JSONObject;

import java.util.ArrayList;

public class SettingsDB {
    public static SettingsBean CreateSettings() {
        SettingsBean bean = new SettingsBean();

        int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        Log.i("Settings", "Save for driverId=" + driverId);
        bean.setDriverId(driverId);
        bean.setTimeZone(Utility._appSetting.getTimeZone());
        bean.setDefaultRule(Utility._appSetting.getDefaultRule());
        bean.setGraphLine(Utility._appSetting.getGraphLine());
        bean.setColorLineUS(Utility._appSetting.getColorLineUS());
        bean.setColorLineCanada(Utility._appSetting.getColorLineCanada());
        bean.setTimeFormat(Utility._appSetting.getTimeFormat());
        bean.setViolationReading(Utility._appSetting.getViolationReading());
        bean.setViolationOnGrid(Utility._appSetting.getViolationOnGrid());
        bean.setMessageReading(Utility._appSetting.getMessageReading());
        bean.setStartTime(Utility._appSetting.getStartTime());
        bean.setOrientation(Utility._appSetting.getOrientation());
        bean.setVisionMode(Utility._appSetting.getVisionMode());
        bean.setCopyTrailer(Utility._appSetting.getCopyTrailer());
        bean.setShowViolation(Utility._appSetting.getShowViolation());
        bean.setSyncTime(Utility._appSetting.getSyncTime());
        bean.setAutomaticRuleChange(Utility._appSetting.getAutomaticRuleChange());
        bean.setFontSize(Utility._appSetting.getFontSize());
        bean.setDutyStatusReading(Utility._appSetting.getDutyStatusReading());

        Save(bean);

        return bean;
    }


    public static void Save(SettingsBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        int settingsId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("TimeZone", bean.getTimeZone());
            values.put("DefaultRule", bean.getDefaultRule());
            values.put("GraphLine", bean.getGraphLine());
            values.put("ColorLineUS", bean.getColorLineUS());
            values.put("ColorLineCanada", bean.getColorLineCanada());
            values.put("TimeFormat", bean.getTimeFormat());
            values.put("ViolationReading", bean.getViolationReading());
            values.put("MessageReading", bean.getMessageReading());
            values.put("StartTime", bean.getStartTime());
            values.put("Orientation", bean.getOrientation());
            values.put("VisionMode", bean.getVisionMode());
            values.put("CopyTrailer", bean.getCopyTrailer());
            values.put("ShowViolation", bean.getShowViolation());
            values.put("SyncTime", bean.getSyncTime());
            values.put("AutomaticRuleChange", bean.getAutomaticRuleChange());
            values.put("ViolationOnGrid", bean.getViolationOnGrid());
            values.put("FontSize", bean.getFontSize());
            values.put("DriverId", bean.getDriverId());
            values.put("DutyStatusReading", bean.getDutyStatusReading());

            int driverId = bean.getDriverId();
            settingsId = getSettingsId(bean.getDriverId());
            if (settingsId == 0) {
                settingsId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_SETTINGS,
                        "_id", values);
            } else {
                database.update(MySQLiteOpenHelper.TABLE_SETTINGS, values,
                        " _id= ?",
                        new String[]{settingsId + ""});
            }
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            Log.e(SettingsDB.class.getName(), "::Save Error:" + e.getMessage());
            LogFile.write(SettingsDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(SettingsDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
    }

    public static SettingsBean getSettings(int driverId) {
        SettingsBean bean = new SettingsBean();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();


            cursor = database.rawQuery("select _id, TimeZone, DefaultRule, GraphLine, ColorLineUS, ColorLineCanada, TimeFormat, ViolationReading, ViolationOnGrid, MessageReading, " +
                    "StartTime, Orientation, VisionMode, CopyTrailer, ShowViolation, SyncTime, AutomaticRuleChange, FontSize, DutyStatusReading from " + MySQLiteOpenHelper.TABLE_SETTINGS + " where DriverId=?",  new String[]{Integer.toString(driverId)});
            if (cursor.getCount() == 0) {
                Log.i("SettingsDB", "Get nothing from Settings table");
                CreateSettings(); //save default bean
                bean.setTimeZone(AppSettings.getTimeZone());
                bean.setDefaultRule(AppSettings.getDefaultRule());
                bean.setGraphLine(AppSettings.getGraphLine());
                bean.setColorLineUS(AppSettings.getColorLineUS());
                bean.setColorLineCanada(AppSettings.getColorLineCanada());
                bean.setTimeFormat(AppSettings.getTimeFormat());
                bean.setViolationReading(AppSettings.getViolationReading());
                bean.setMessageReading(AppSettings.getMessageReading());
                bean.setStartTime(AppSettings.getStartTime());
                bean.setOrientation(AppSettings.getOrientation());
                bean.setVisionMode(AppSettings.getVisionMode());
                bean.setCopyTrailer(AppSettings.getCopyTrailer());
                bean.setShowViolation(AppSettings.getShowViolation());
                bean.setSyncTime(AppSettings.getSyncTime());
                bean.setAutomaticRuleChange(AppSettings.getAutomaticRuleChange());
                bean.setViolationOnGrid(AppSettings.getViolationOnGrid());
                bean.setFontSize(AppSettings.getFontSize());
                bean.setDutyStatusReading(AppSettings.getDutyStatusReading());
            } else {
                if (cursor.moveToLast()) {

                    bean.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                    bean.setTimeZone(cursor.getFloat(cursor.getColumnIndex("TimeZone")));
                    bean.setDefaultRule(cursor.getInt(cursor.getColumnIndex("DefaultRule")));
                    bean.setGraphLine(cursor.getInt(cursor.getColumnIndex("GraphLine")));
                    bean.setColorLineUS(cursor.getInt(cursor.getColumnIndex("ColorLineUS")));
                    bean.setColorLineCanada(cursor.getInt(cursor.getColumnIndex("ColorLineCanada")));
                    bean.setTimeFormat(cursor.getInt(cursor.getColumnIndex("TimeFormat")));
                    bean.setViolationReading(cursor.getInt(cursor.getColumnIndex("ViolationReading")));
                    bean.setMessageReading(cursor.getInt(cursor.getColumnIndex("MessageReading")));
                    bean.setStartTime(cursor.getString(cursor.getColumnIndex("StartTime")));
                    bean.setOrientation(cursor.getInt(cursor.getColumnIndex("Orientation")));
                    bean.setVisionMode(cursor.getInt(cursor.getColumnIndex("VisionMode")));
                    bean.setCopyTrailer(cursor.getInt(cursor.getColumnIndex("CopyTrailer")));
                    bean.setShowViolation(cursor.getInt(cursor.getColumnIndex("ShowViolation")));
                    bean.setSyncTime(cursor.getInt(cursor.getColumnIndex("SyncTime")));
                    bean.setAutomaticRuleChange(cursor.getInt(cursor.getColumnIndex("AutomaticRuleChange")));
                    bean.setViolationOnGrid(cursor.getInt(cursor.getColumnIndex("ViolationOnGrid")));
                    bean.setFontSize(cursor.getInt(cursor.getColumnIndex("FontSize")));
                    bean.setDutyStatusReading(cursor.getInt(cursor.getColumnIndex("DutyStatusReading")));
                }
            }
        } catch (Exception e) {
            LogFile.write(SettingsDB.class.getName() + "::getSettings Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(SettingsDB.class.getName() + "::getSettings close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return bean;
    }

    public static int getSettingsId(int driverId) {
        int id = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_SETTINGS + " where DriverId=?",  new String[]{Integer.toString(driverId)});
            if (cursor.moveToLast()) {
                id = cursor.getInt(0);
            }

        } catch (Exception exe) {
            LogFile.write(SettingsDB.class.getName() + "::getSettingsId Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return id;
    }

}
