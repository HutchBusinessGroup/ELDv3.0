package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;
import android.util.Log;

import com.hutchgroup.elog.beans.MessageBean;
import com.hutchgroup.elog.beans.TripInspectionBean;
import com.hutchgroup.elog.beans.UserBean;
import com.hutchgroup.elog.beans.VersionInformationBean;
import com.hutchgroup.elog.common.ChatClient;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import org.json.JSONObject;

import java.util.ArrayList;

public class VersionInformationDB {

    public static VersionInformationBean CreateVersion(boolean autoDownloadFg, boolean autoUpdateFg, String currentVersion, String downloadDate, boolean downloadFg, boolean liveFg,
                                                       String previousVersion, String serialNo, String updateArchiveName, String updateDate, String updateUrl, boolean updatedFg, String versionDate) {
        VersionInformationBean bean = new VersionInformationBean();
        bean.setAutoDownloadFg(autoDownloadFg);
        bean.setAutoUpdateFg(autoUpdateFg);
        bean.setCurrentVersion(currentVersion);
        bean.setDownloadDate(downloadDate);
        bean.setDownloadFg(downloadFg);
        bean.setLiveFg(liveFg);
        bean.setPreviousVersion(previousVersion);
        bean.setSerialNo(serialNo);
        bean.setUpdateArchiveName(updateArchiveName);
        bean.setUpdateDate(updateDate);
        bean.setUpdateUrl(updateUrl);
        bean.setUpdatedFg(updatedFg);
        bean.setVersionDate(versionDate);

        Save(bean);
        return bean;

    }

    public static void Save(VersionInformationBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        int versionId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select CurrentVersion from " + MySQLiteOpenHelper.TABLE_VERSION_INFORMATION + " order by _id desc Limit 1", null);
            if (cursor.moveToFirst()) {
                String version = cursor.getString(0);
                if (bean.getCurrentVersion().equals(version)) {
                    versionId = 1;
                }
            }
            ContentValues values = new ContentValues();

            values.put("AutoDownloadFg", bean.getAutoDownloadFg() ? 1 : 0);
            values.put("AutoUpdateFg", bean.getAutoUpdateFg() ? 1 : 0);
            values.put("CurrentVersion", bean.getCurrentVersion());
            values.put("DownloadDate", bean.getDownloadDate());
            values.put("DownloadFg", bean.getDownloadFg() ? 1 : 0);
            values.put("LiveFg", bean.getLiveFg() ? 1 : 0);
            values.put("PreviousVersion", bean.getPreviousVersion());
            values.put("SerialNo", bean.getSerialNo());
            values.put("UpdateArchiveName", bean.getUpdateArchiveName());
            values.put("UpdateDate", bean.getUpdateDate());
            values.put("UpdateUrl", bean.getUpdateUrl());
            values.put("UpdatedFg", bean.getUpdatedFg() ? 1 : 0);
            values.put("VersionDate", bean.getVersionDate());
            if (versionId == 0)
                versionId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_VERSION_INFORMATION,
                        "_id", values);
        } catch (Exception e) {
            Log.e(VersionInformationDB.class.getName(), "::Save Error:" + e.getMessage());
            LogFile.write(VersionInformationDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(VersionInformationDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
    }

    public static VersionInformationBean getVersionInformation() {
        VersionInformationBean bean = new VersionInformationBean();

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id, AutoDownloadFg, AutoUpdateFg, CurrentVersion, DownloadDate, DownloadFg, LiveFg, PreviousVersion, SerialNo, " +
                    "UpdateArchiveName, UpdateDate, UpdateUrl, UpdatedFg, VersionDate, SyncFg from " + MySQLiteOpenHelper.TABLE_VERSION_INFORMATION + " order by _id desc Limit 1", null);
            if (cursor.moveToFirst()) {

                bean.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setAutoDownloadFg(cursor.getInt(cursor.getColumnIndex("AutoDownloadFg")) == 1 ? true : false);
                bean.setAutoUpdateFg(cursor.getInt(cursor.getColumnIndex("AutoUpdateFg")) == 1 ? true : false);
                bean.setCurrentVersion(cursor.getString(cursor.getColumnIndex("CurrentVersion")));
                bean.setDownloadDate(cursor.getString(cursor.getColumnIndex("DownloadDate")));
                bean.setDownloadFg(cursor.getInt(cursor.getColumnIndex("DownloadFg")) == 1 ? true : false);
                bean.setLiveFg(cursor.getInt(cursor.getColumnIndex("LiveFg")) == 1 ? true : false);
                bean.setPreviousVersion(cursor.getString(cursor.getColumnIndex("PreviousVersion")));
                bean.setSerialNo(cursor.getString(cursor.getColumnIndex("SerialNo")));
                bean.setUpdateArchiveName(cursor.getString(cursor.getColumnIndex("UpdateArchiveName")));
                bean.setUpdateDate(cursor.getString(cursor.getColumnIndex("UpdateDate")));
                bean.setUpdateUrl(cursor.getString(cursor.getColumnIndex("UpdateUrl")));
                bean.setUpdatedFg(cursor.getInt(cursor.getColumnIndex("UpdatedFg")) == 1 ? true : false);
                bean.setVersionDate(cursor.getString(cursor.getColumnIndex("VersionDate")));
            }

        } catch (Exception e) {
            LogFile.write(VersionInformationDB.class.getName() + "::getVersionInformation Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(VersionInformationDB.class.getName() + "::getVersionInformation close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return bean;
    }

}
