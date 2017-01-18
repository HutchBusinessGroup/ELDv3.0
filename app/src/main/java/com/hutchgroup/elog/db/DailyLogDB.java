package com.hutchgroup.elog.db;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NavUtils;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.DailyLogBean;
import com.hutchgroup.elog.beans.DutyStatusBean;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.beans.GPSData;
import com.hutchgroup.elog.beans.RuleBean;
import com.hutchgroup.elog.bll.HourOfService;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class DailyLogDB {

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get rules for web sync
    public static JSONArray getDailyLogRuleSync(int logId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select DailyLogId ,RuleId ,RuleStartTime ,RuelEndTime from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE +
                            " where SyncFg=0 and DailyLogId=?"
                    , new String[]{logId + ""});
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("LogId", cursor.getInt(0));
                obj.put("RuleId", cursor.getInt(1));
                obj.put("RuleStartTime", Utility.getDateTimeForServer(cursor.getString(2)));
                obj.put("RuleEndTime", Utility.getDateTimeForServer(cursor.getString(3) == null ? "1970-01-01 00:00:00" : cursor.getString(3)));
                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getDailyLogRuleSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
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


    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get rules for web sync
    public static ArrayList<RuleBean> getRuleByDate(String date, int driverId, int dailyLogId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<RuleBean> ruleList = new ArrayList<>();

        try {
            String nextDay = Utility.sdf.format(Utility.addDays(new Date(), 1));
            Date selectedDate = Utility.parse(date);
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            String sql = "select * from  (select RuleId,RuleStartTime,RuelEndTime  from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE + " r join " + MySQLiteOpenHelper.TABLE_DAILYLOG + " d on" +
                    " r.DailyLogId=d._id and d.DriverId=" + driverId + " where RuleStartTime<'" + date + "'  order by RuleStartTime desc LIMIT 1)a union ";
            sql += "select RuleId,RuleStartTime,RuelEndTime  from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE + " where DailyLogId=" + dailyLogId + "  order by RuleStartTime";
            cursor = database.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                RuleBean obj = new RuleBean();
                obj.setRuleId(cursor.getInt(cursor.getColumnIndex("RuleId")));
                Date ruleDate = Utility.parse(cursor.getString(cursor.getColumnIndex("RuleStartTime")));
                if (ruleDate.before(selectedDate)) {
                    ruleDate = selectedDate;
                }

                String endDate = cursor.getString(cursor.getColumnIndex("RuelEndTime"));
                endDate = endDate == null ? nextDay : endDate;
                Date ruleEndDate = Utility.parse(endDate);
                obj.setRuleStartTime(ruleDate);
                obj.setRuleEndTime(ruleEndDate);
                ruleList.add(obj);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getDailyLogRuleSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return ruleList;
    }

    public static int getCurrentRule(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        int ruleId = 1;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();


            cursor = database.rawQuery("select RuleId from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE + " r inner join " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " d on r.DailyLogId=d._id and DriverId=?" +
                            "  order by RuleStartTime desc LIMIT 1"
                    , new String[]{Integer.toString(driverId)});
            if (cursor.moveToFirst()) {
                ruleId = cursor.getInt(0);
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCurrentRule Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return ruleId;
    }

    public static int DailyLogUserPreferenceRuleSave(int driverId, int ruleId, String ruleStartTime, String ruleEndTime) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int logId = 0;
        try {
            // update current rule in user table
            UserDB.Update("CurrentRule", ruleId + "");
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            updateRuleEndTime(driverId, ruleStartTime);
            ContentValues values = new ContentValues();

            logId = DailyLogCreate(driverId, "", "", "");
            values.put("DailyLogId", logId);
            values.put("RuleId", ruleId);
            values.put("RuleStartTime", ruleStartTime);
            values.put("SyncFg", 0);
            database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG_RULE,
                    "RuleId", values);
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogRuleSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return logId;
    }

    private static void updateRuleEndTime(int driverId, String ruleStartTime) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String sql = "select r._id  from " + MySQLiteOpenHelper.TABLE_DAILYLOG_RULE + " r join " + MySQLiteOpenHelper.TABLE_DAILYLOG + " d on" +
                    " r.DailyLogId=d._id and d.DriverId=" + driverId + " where RuleStartTime<'" + ruleStartTime + "'  order by RuleStartTime desc LIMIT 1 ";


            cursor = database.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                ContentValues values = new ContentValues();
                values.put("RuelEndTime", ruleStartTime);
                database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_RULE, values,
                        " _id= ?", new String[]{id
                                + ""});
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::updateRuleEndTime Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
    }

    public static int DailyLogRuleSave(int driverId, int ruleId, String ruleStartTime, String ruleEndTime) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        int logId = 0;
        try {
            UserDB.Update("CurrentRule", ruleId + "");

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            updateRuleEndTime(driverId, ruleStartTime);

            logId = getDailyLog(driverId, Utility.getCurrentDate());
            values.put("DailyLogId", logId);
            values.put("RuleId", ruleId);
            values.put("RuleStartTime", ruleStartTime);
            // values.put("RuelEndTime", ruleEndTime);
            values.put("SyncFg", 0);
            database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG_RULE,
                    "RuleId", values);
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogRuleSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return logId;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get codriver for web sync
    public static JSONArray getCoDriverSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select DriverId ,DriverId2 ,LoginTime ,LogoutTime  from " + MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER +
                            " where SyncFg=0"
                    , null);
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("DriverId", cursor.getInt(0));
                obj.put("DriverId2", cursor.getInt(1));
                obj.put("LoginTime", Utility.getDateTimeForServer(cursor.getString(2)));
                obj.put("LogoutTime", Utility.getDateTimeForServer(cursor.getString(3) == null ? "1970-01-01 00:00:00" : cursor.getString(3)));
                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCoDriverSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
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

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get codriver for driver
    public static int getCoDriver(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select DriverId,DriverId2 from " + MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER +
                            " where (DriverId=? or DriverId2=?) order by LoginTime desc LIMIT 1"
                    , new String[]{driverId + "", driverId + ""});
            while (cursor.moveToNext()) {
                int driverId1 = cursor.getInt(0);
                int driverId2 = cursor.getInt(1);
                id = driverId1 == driverId ? driverId2 : driverId1;

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCoDriver Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return id;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get codriver for driver
    public static String getCoDriver(int driverId, String logDate) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        String coDrivers = "";
        String coDriverIds = "";
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select cd.DriverId, U.FirstName Driver1,cd.DriverId2,U1.FirstName Driver2 from " + MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER +
                            " cd join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " U on cd.DriverId=U.AccountId " +
                            " join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " U1 on cd.DriverId2=U1.AccountId where (DriverId=? or DriverId2=?) and date(LoginTime)=?"
                    , new String[]{driverId + "", driverId + "", logDate});

            while (cursor.moveToNext()) {
                int driver1Id = cursor.getInt(0);
                String driver1 = cursor.getString(1);

                int driver2Id = cursor.getInt(2);
                String driver2 = cursor.getString(3);

                String codDriver = (driverId == driver1Id ? driver2 : driver1);
                int codDriverId = (driverId == driver1Id ? driver2Id : driver1Id);
                if (coDrivers.indexOf(codDriver) == -1) {
                    coDriverIds += codDriverId + ",";
                    coDrivers += codDriver + ",";
                }
            }
            if (coDriverIds.contains(",")) {
                coDriverIds = coDriverIds.substring(0, coDriverIds.length() - 1);
                coDrivers = coDrivers.substring(0, coDrivers.length() - 1);
            }

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCoDriver Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        if (coDriverIds.equals("") && coDrivers.equals(""))
            return "";
        return coDriverIds + "#" + coDrivers;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get codriver for driver
    public static ArrayList<DutyStatusBean> getCoDriverList(int driverId, Date logDate) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        String coDriverIds = "";
        ArrayList<DutyStatusBean> listCoDriver = new ArrayList<>();
        Date toDate = Utility.dateOnlyGet(logDate);
        Date fromDate = Utility.addDays(toDate, -15);

        String start = Utility.format(fromDate, "yyyy-MM-dd HH:mm:ss");
        String end = Utility.format(toDate, "yyyy-MM-dd HH:mm:ss");
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select cd.DriverId,cd.DriverId2,LoginTime from " + MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER +
                            " cd join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " U on cd.DriverId=U.AccountId " +
                            " join " + MySQLiteOpenHelper.TABLE_ACCOUNT + " U1 on cd.DriverId2=U1.AccountId where (DriverId=? or DriverId2=?) and LoginTime between ? and ?"
                    , new String[]{driverId + "", driverId + "", start, end});

            while (cursor.moveToNext()) {
                DutyStatusBean coDriver = new DutyStatusBean();
                int driver1Id = cursor.getInt(0);
                int driver2Id = cursor.getInt(1);
                String startTime = cursor.getString(2);
                int codDriverId = (driverId == driver1Id ? driver2Id : driver1Id);
                coDriver.setDriverId(codDriverId);
                coDriver.setStartTime(startTime);
                listCoDriver.add(coDriver);
                if (!coDriverIds.contains(codDriverId + ","))
                    coDriverIds += codDriverId + ",";

            }

            if (coDriverIds.contains(",")) {
                coDriverIds = coDriverIds.substring(0, coDriverIds.length() - 1);
            }

            HourOfService.coDriverIds = coDriverIds;

        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCoDriver Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return listCoDriver;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add codriver
    public static void AddDriver(int driver1, int driver2, int _id) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 0);
            if (_id == 0) {

                values.put("DriverId", driver1);
                values.put("DriverId2", driver2);
                values.put("LoginTime", Utility.getCurrentDateTime());
                database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER,
                        "_id", values);
            } else {
                values.put("LogoutTime", Utility.getCurrentDateTime());
                database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER, values,
                        " _id= ?", new String[]{_id
                                + ""});
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::AddDriver Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get uncertified log data
    public static ArrayList<DailyLogBean> getUncertifiedDailyLog(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<DailyLogBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id ,LogDate,ShippingId,TrailerId,StartOdometerReading,EndOdometerReading from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where driverId=? and CertifyFG=0 and LogDate!=? order by LogDate"
                    , new String[]{driverId + "", Utility.getCurrentDate()});
            while (cursor.moveToNext()) {
                DailyLogBean bean = new DailyLogBean();
                bean.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setLogDate(cursor.getString(cursor.getColumnIndex("LogDate")));
                bean.setShippingId(cursor.getString(cursor.getColumnIndex("ShippingId")));
                bean.setTrailerId(cursor.getString(cursor.getColumnIndex("TrailerId")));
                bean.setStartOdometerReading(cursor.getString(cursor.getColumnIndex("StartOdometerReading")));
                bean.setEndOdometerReading(cursor.getString(cursor.getColumnIndex("EndOdometerReading")));
                list.add(bean);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getUncertifiedDailyLog Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return list;
    }


    // Created By: Deepak Sharma
    // Created Date: 19 September 2016
    // Purpose: get uncertified log data
    public static boolean unCertifiedFg(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where driverId=? and CertifyFG=0 LIMIT 1"
                    , new String[]{driverId + ""});
            if (cursor.moveToNext()) {
                status = true;
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::unCertifiedFg Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get uncertified log data
    public static boolean getUncertifiedLogFg(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        boolean uncertify = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id ,LogDate,ShippingId,TrailerId,StartOdometerReading,EndOdometerReading from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where driverId=? and CertifyFG=0 and LogDate!=? LIMIT 1"
                    , new String[]{driverId + "", Utility.getCurrentDate()});
            if (cursor.moveToNext()) {

                uncertify = true;
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getUncertifiedDailyLog Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return uncertify;
    }


    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get count of certification
    public static int getCertifyCount(int logId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int count = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select certifyCount from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where _id =?"
                    , new String[]{logId + ""});
            if (cursor.moveToNext()) {
                count = cursor.getInt(0);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getCertifyCount Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return count;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get daily log data
    public static int getDailyLog(int driverId, String logDate) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int logId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where LogDate=? and driverId=?"
                    , new String[]{logDate.toString(), driverId + ""});
            if (cursor.moveToNext()) {
                logId = cursor.getInt(0);
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getDailyLog Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return logId;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get daily log data
    public static String getLastDailyLogDate(int driverId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        String logDate = Utility.getCurrentDate();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select LogDate from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where LogDate<? and driverId=? order by LogDate desc LIMIT 1"
                    , new String[]{logDate, driverId + ""});
            if (cursor.moveToNext()) {
                logDate = cursor.getString(0);
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getLastDailyLogDate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return logDate;
    }

    // Created By: Deepak Sharma
    // Created Date: 8 July 2016
    // Purpose: get daily log data
    public static DailyLogBean getDailyLogInfo(int driverId, String logDate) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        DailyLogBean bean = new DailyLogBean();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id,CertifyFG, ShippingId, TrailerId  from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where LogDate=? and driverId=?"
                    , new String[]{logDate.toString(), driverId + ""});
            if (cursor.moveToNext()) {
                bean.set_id(cursor.getInt(0));
                bean.setCertifyFG(cursor.getInt(1));
                bean.setShippingId(cursor.getString(2));
                bean.setTrailerId(cursor.getString(3));
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::getDailyLog Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return bean;
    }

    public static int DailyLogCreateByDate(int driverId, String date, String shippingId, String trailerId, String commodity) {
        DailyLogBean bean = new DailyLogBean();
        bean.setDriverId(driverId);
        bean.setShippingId(shippingId);
        bean.setTrailerId(trailerId);
        bean.setCommodity(commodity);
        bean.setCreatedBy(driverId);
        bean.setModifiedBy(driverId);
        bean.setStartTime("00:00:00");
        bean.setLogDate(date);
        bean.setCreatedDate(date + " 00:00:00");
        bean.setModifiedDate(date + " 00:00:00");
        return DailyLogSave(bean);
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: create daily log record
    public static int DailyLogCreate(int driverId, String shippingId, String trailerId, String commodity) {
        DailyLogBean bean = new DailyLogBean();
        bean.setDriverId(driverId);
        bean.setShippingId(shippingId);
        bean.setTrailerId(trailerId);
        bean.setCommodity(commodity);
        bean.setCreatedBy(driverId);
        bean.setModifiedBy(driverId);
        bean.setStartTime("00:00:00");
        bean.setLogDate(Utility.getCurrentDate());
        bean.setCreatedDate(Utility.getCurrentDateTime());
        bean.setModifiedDate(Utility.getCurrentDateTime());
        return DailyLogSave(bean);
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update dailylog in database
    public static int DailyLogSave(DailyLogBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int logId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("ShippingId", bean.getShippingId());
            values.put("TrailerId", bean.getTrailerId());
            values.put("Commodity", bean.getCommodity());
            values.put("EndOdometerReading", CanMessages.OdometerReading);
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            logId = getDailyLog(bean.getDriverId(), bean.getLogDate());
            if (logId == 0) {
                values.put("CertifyFG", 0);
                values.put("OnlineDailyLogId", 0);
                values.put("DriverId", bean.getDriverId());
                values.put("LogDate", bean.getLogDate());
                values.put("StartTime", bean.getStartTime());
                values.put("CreatedBy", bean.getCreatedBy());
                values.put("CreatedDate", bean.getCreatedDate());
                values.put("StartOdometerReading", CanMessages.OdometerReading);

                values.put("DrivingTimeRemaining", GPSData.DrivingTimeRemaining);
                values.put("WorkShiftTimeRemaining", GPSData.WorkShiftRemaining);
                values.put("TimeRemaining70", GPSData.TimeRemaining70);
                values.put("TimeRemaining120", GPSData.TimeRemaining120);
                values.put("TimeRemainingUS70", GPSData.TimeRemainingUS70);
                values.put("TimeRemainingReset", GPSData.TimeRemainingReset);

                logId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG,
                        "_id,ModifiedBy,ModifiedDate", values);
            } else {
                values.put("ModifiedBy", bean.getModifiedBy());
                values.put("ModifiedDate", bean.getModifiedDate());
                database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                        " _id= ?", new String[]{logId
                                + ""});
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogSave close database Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return logId;
    }

    // Created By: Deepak Sharma
    // Created Date: 27 January 2015
    // Purpose: add or update multiple dailylog in database from web
    public static int DailyLogSave(ArrayList<DailyLogBean> arrBean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        int logId = 0;
        try {

            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ArrayList<EventBean> eventBeenList = new ArrayList<>();
            for (DailyLogBean bean : arrBean) {
                ContentValues values = new ContentValues();

                values.put("OnlineDailyLogId", bean.getOnlineDailyLogId());
                values.put("StatusId", 1);
                values.put("SyncFg", 1);
                logId = getDailyLog(bean.getDriverId(), bean.getLogDate());
                if (logId == 0) {
                    values.put("ShippingId", bean.getShippingId());
                    values.put("TrailerId", bean.getTrailerId());
                    values.put("Commodity", bean.getCommodity());
                    values.put("CertifyFG", bean.getCertifyFG());
                    values.put("DriverId", bean.getDriverId());
                    values.put("LogDate", bean.getLogDate());
                    values.put("StartTime", bean.getStartTime());
                    values.put("CreatedBy", bean.getCreatedBy());
                    values.put("CreatedDate", bean.getCreatedDate());
                    values.put("StartOdometerReading", bean.getStartOdometerReading());
                    values.put("EndOdometerReading", bean.getEndOdometerReading());


                    values.put("DrivingTimeRemaining", GPSData.DrivingTimeRemaining);
                    values.put("WorkShiftTimeRemaining", GPSData.WorkShiftRemaining);
                    values.put("TimeRemaining70", GPSData.TimeRemaining70);
                    values.put("TimeRemaining120", GPSData.TimeRemaining120);
                    values.put("TimeRemainingUS70", GPSData.TimeRemainingUS70);
                    values.put("TimeRemainingReset", GPSData.TimeRemainingReset);
                    logId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_DAILYLOG,
                            "_id,ModifiedBy,ModifiedDate", values);
                } else {
                    values.put("ModifiedBy", bean.getModifiedBy());
                    values.put("ModifiedDate", Utility.getCurrentDateTime());
                    database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                            " _id= ?", new String[]{logId
                                    + ""});
                }

                for (EventBean eBean : bean.getEventList()) {
                    eBean.setDailyLogId(logId);
                    eBean.setDriverId(bean.getDriverId());
                }
                eventBeenList.addAll(bean.getEventList());
                bean.set_id(logId);
            }
            if (eventBeenList.size() > 0) {
                EventDB.EventSave(eventBeenList);
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogSave Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogSave close database Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return logId;
    }


    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update dailylog in database
    public static Boolean DailyLogCertify(String sign, int driverId, String logIds) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("Signature", sign);
            values.put("CertifyFG", 1);
            values.put("StatusId", 1);
            values.put("SyncFg", 0);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());
            String[] arrayLog = logIds.split(",");

            for (String id : arrayLog) {

                database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                        " _id = ?", new String[]{id});
            }
            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertify Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertify close database Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update dailylog in database
    public static Boolean CertifyCountUpdate(int id, int count) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("certifyCount", count);
            values.put("SyncFg", 0);

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " _id =?", new String[]{id
                            + ""});
            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::CertifyCountUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::CertifyCountUpdate close database Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update dailylog in database
    public static Boolean DailyLogCertifyRevert(int driverId, int logId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("CertifyFG", 0);
            values.put("SyncFg", 0);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());


            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " _id = ?", new String[]{logId + ""});

            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertifyRevert Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertifyRevert close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: update hours in dailylog table
    public static Boolean DailyLogHoursReCertify(int driverId, int logId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("DrivingTimeRemaining", GPSData.DrivingTimeRemaining);
            values.put("WorkShiftTimeRemaining", GPSData.WorkShiftRemaining);
            values.put("TimeRemaining70", GPSData.TimeRemaining70);
            values.put("TimeRemaining120", GPSData.TimeRemaining120);
            values.put("TimeRemainingUS70", GPSData.TimeRemainingUS70);
            values.put("TimeRemainingReset", GPSData.TimeRemainingReset);
            values.put("CertifyFG", 0);
            values.put("SyncFg", 0);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());


            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " _id = ?", new String[]{logId + ""});

            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertifyRevert Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogCertifyRevert close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update dailylog in database
    public static Boolean DailyLogSyncRevert(int driverId, int logId) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("SyncFg", 0);
            values.put("ModifiedBy", driverId);
            values.put("ModifiedDate", Utility.getCurrentDateTime());


            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " _id = ?", new String[]{logId + ""});

            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::DailyLogSyncRevert Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::DailyLogSyncRevert close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: get log data to post
    public static JSONArray getLogDataSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id ,LogDate ,DriverId ,ShippingId ,TrailerId ,StartTime ,StartOdometerReading ,EndOdometerReading ,CertifyFG ,certifyCount " +
                            ",Signature ,CreatedBy ,CreatedDate ,ModifiedBy ,ModifiedDate,DrivingTimeRemaining ,WorkShiftTimeRemaining ,TimeRemaining70 ,TimeRemaining120 ,TimeRemainingUS70 ,TimeRemainingReset   from " + MySQLiteOpenHelper.TABLE_DAILYLOG +
                            " where SyncFg=? order by 1 desc LIMIT 1"
                    , new String[]{"0"});
            if (cursor.moveToNext()) {

                JSONObject obj = new JSONObject();
                int logId = cursor.getInt(cursor.getColumnIndex("_id"));
                Utility.LogId = logId;

                obj.put("DAILYLOGID", logId);
                String logDate = cursor.getString(cursor.getColumnIndex("LogDate"));
                obj.put("LOGDATE", Utility.getDateForServer(logDate));
                obj.put("DRIVERID", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("SHIPPINGID", cursor.getString(cursor.getColumnIndex("ShippingId")));
                obj.put("TRAILERID", cursor.getString(cursor.getColumnIndex("TrailerId")));
                obj.put("STARTTIME", cursor.getString(cursor.getColumnIndex("StartTime")));
                obj.put("STARTODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("StartOdometerReading"))));
                obj.put("ENDODOMETERREADING", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EndOdometerReading"))));
                obj.put("CERTIFYFG", cursor.getInt(cursor.getColumnIndex("CertifyFG")));
                obj.put("CertifyCount", cursor.getInt(cursor.getColumnIndex("certifyCount")));
                String signature = cursor.getString(cursor.getColumnIndex("Signature"));
                obj.put("Signature", signature == null ? "" : signature);
                obj.put("COMPANYID", Utility.companyId);
                obj.put("LOGCREATEDBY", cursor.getInt(cursor.getColumnIndex("CreatedBy")));

                String createdDate = cursor.getString(cursor.getColumnIndex("CreatedDate"));
                createdDate = Utility.getDateTimeForServer(createdDate);
                if (createdDate.isEmpty()) {
                    createdDate = Utility.getDateTimeForServer(logDate + " 00:00:00");
                }
                obj.put("LOGCREATEDDATE", createdDate);
                obj.put("MODIFIEDBY", cursor.getInt(cursor.getColumnIndex("ModifiedBy")));
                String modifiedDate = cursor.getString(cursor.getColumnIndex("ModifiedDate"));
                modifiedDate = (modifiedDate == null || modifiedDate.isEmpty()) ? "1970-01-01 00:00:00" : modifiedDate;
                obj.put("MODIFIEDDATE", Utility.getDateTimeForServer(modifiedDate));
                obj.put("DrivingTimeRemaining", cursor.getInt(cursor.getColumnIndex("DrivingTimeRemaining")));
                obj.put("WorkShiftRemaining", cursor.getInt(cursor.getColumnIndex("WorkShiftTimeRemaining")));
                obj.put("TimeRemaining70", cursor.getInt(cursor.getColumnIndex("TimeRemaining70")));
                obj.put("TimeRemaining120", cursor.getInt(cursor.getColumnIndex("TimeRemaining120")));
                obj.put("TimeRemainingUS70", cursor.getInt(cursor.getColumnIndex("TimeRemainingUS70")));
                obj.put("TimeRemainingReset", cursor.getInt(cursor.getColumnIndex("TimeRemainingReset")));

                obj.put("EventList", EventDB.GetEventSync(logId));
                obj.put("RuleList", getDailyLogRuleSync(logId));
                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.LogId = 0;
            LogFile.write(DailyLogDB.class.getName() + "::getLogDataSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception exe) {
                LogFile.write(DailyLogDB.class.getName() + "::getLogDataSync close DB Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return array;
    }

    // Created By: Deepak Sharma
    // Created Date: 29 January 2015
    // Purpose: update SyncFg=0 for all table whose data is posted from Post/All service
    public static void UpdateSyncStatusAll() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " SyncFg= ? and _id=?", new String[]{"0", Utility.LogId + ""});

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT, values,
                    " SyncFg= ? and DailyLogId=?", new String[]{"0", Utility.LogId + ""});

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_RULE,
                    values, " SyncFg= ? and DailyLogId=? ", new String[]{"0", Utility.LogId + ""});


            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG_CODRIVER,
                    values, " SyncFg= ? ", new String[]{"0"});
            Utility.LogId = 0;
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::UpdateSyncStatusAll Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::UpdateSyncStatusAll close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
    }

    public static void certifyLogBook(int driverId, String logIds) {
        try {

            if (DailyLogDB.DailyLogCertify("", driverId, logIds)) {
                String[] logs = logIds.split(",");
                // need to discuss about this should we enter multiple event related to multiple certification
                for (int i = 0; i < logs.length; i++) {
                    int logId = Integer.parseInt(logs[i]);
                    int n = DailyLogDB.getCertifyCount(logId) + 1;
                    DailyLogDB.CertifyCountUpdate(logId, n);
                    if (n > 9)
                        n = 9;
                    // to be discuss about event
                    //123 LogFile.write(DailyLogDB.class.getName() + "::certifyLogBook: " + "Driver's " + n + "'th certification of a daily record" + " of driverId:" + driverId, LogFile.DIAGNOSTIC_MALFUNCTION, LogFile.DRIVEREVENT_LOG);
                    EventDB.EventCreate(Utility.getCurrentDateTime(), 4, n, "Driver's " + n + "'th certification of a daily record", 1, 1, logId, driverId, "");
                }
            }
        } catch (Exception e) {
            LogFile.write(DailyLogDB.class.getName() + "::certifyLogBook Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2015
    // Purpose: add or update dailylog in database
    public static Boolean TrailerUpdate(int driverId, String trailerNo) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        boolean status = false;
        try {
            String logDate = Utility.getCurrentDate();
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("TrailerId", trailerNo);
            values.put("SyncFg", 0);

            database.update(MySQLiteOpenHelper.TABLE_DAILYLOG, values,
                    " DriverId =? and LogDate=?", new String[]{driverId
                            + "", logDate});
            status = true;


        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(DailyLogDB.class.getName() + "::CertifyCountUpdate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(DailyLogDB.class.getName() + "::CertifyCountUpdate close database Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return status;
    }
}
