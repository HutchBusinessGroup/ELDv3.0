package com.hutchgroup.elog.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hutchgroup.elog.beans.DutyStatusBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by Deepak.Sharma on 7/20/2015.
 */
public class HourOfServiceDB {

    static String fullFormat = "yyyy-MM-dd HH:mm:ss";

    public static DutyStatusBean DutyStatusObjectGet(int status, Date startTime, Date endTime, int totalMinutes, int personalUseFg) {
        DutyStatusBean bean = new DutyStatusBean();
        bean.setStatus(status);
        bean.setStartTime(Utility.format(startTime, fullFormat));
        bean.setEndTime(Utility.format(endTime, fullFormat));
        bean.setTotalMinutes(totalMinutes);
        bean.setPersonalUse(personalUseFg);
        return bean;
    }

    public static ArrayList<DutyStatusBean> DutyStatusGet15Days(Date logDate, String driverId, boolean cdFg) {
        ArrayList<DutyStatusBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            logDate = Utility.dateOnlyGet(logDate);
            Date startDate = Utility.addDays(logDate, -15);
            Date endDate = Utility.addDays(logDate, 2);

            if (cdFg) {
                startDate = Utility.dateOnlyGet(logDate);
                endDate = Utility.addSeconds(Utility.addDays(startDate, 1), -1);
            }
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String start = Utility.format(startDate, fullFormat);
            String end = Utility.format(endDate, fullFormat);

            String sql = "select *,1 as recordType from(select EventDateTime dutyStatusTime ,case when EventType=3 and EventCode=2 then 4 else EventCode end dutyStatus," +
                    "case when EventType=3 and EventCode!=0 then 1 else 0 end personalUseFg from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " where EventRecordStatus=1 and EventType in (1,3) and EventCode>0 and  DriverId in (" + driverId + ") and EventDateTime < '" + start + "' order by EventDateTime desc LIMIT 1)a union ";
            sql += "select EventDateTime dutyStatusTime, case when EventType=3 and EventCode=2 then 4 else EventCode end dutyStatus,case when EventType=3 and EventCode!=0 then 1 else 0 end personalUseFg" +
                    ",2 recordType from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT + " where EventRecordStatus=1 and EventType in (1,3) and EventCode>0  and EventDateTime between '" + start +
                    "' and '" + end + "' and DriverId in (" + driverId + ") union ";
            sql += "select *,3 from(select EventDateTime dutyStatusTime,case when EventType=3 and EventCode=2 then 4 else EventCode end  dutyStatus," +
                    "case when EventType=3 and EventCode!=0 then 1 else 0 end personalUseFg from " + MySQLiteOpenHelper.TABLE_DAILYLOG_EVENT +
                    " where EventRecordStatus=1 and EventType in (1,3) and EventCode>0  and DriverId in (" + driverId + ") and EventDateTime > '" + end + "' LIMIT 1)B order by EventDateTime ";

            cursor = database.rawQuery(sql, null);

            if (cursor.moveToNext()) {
                int firstRecord = cursor.getInt(cursor.getColumnIndex("recordType"));
                Date startTime = Utility.parse(cursor.getString(cursor.getColumnIndex("dutyStatusTime")));
                // remove seconds
                // startTime = Utility.addSeconds(startTime, -startTime.getSeconds());
                int personalUseFg = cursor.getInt(cursor.getColumnIndex("personalUseFg"));

                if (firstRecord > 1) {
                    int initialMinutes = (int) Math.round((startTime.getTime() - Utility.dateOnlyGet(startTime).getTime()) / (1000 * 60.0));
                    initialMinutes = initialMinutes >= 600 ? initialMinutes : 600;
                    list.add(DutyStatusObjectGet(1, Utility.addMinutes(startTime, -initialMinutes), startTime, initialMinutes, 0));
                }

                int status = cursor.getInt(cursor.getColumnIndex("dutyStatus"));
                DutyStatusBean bean = DutyStatusObjectGet(status, startTime, endDate, 0, personalUseFg);
                int i = 0;
                while (cursor.moveToNext()) {

                    Date endTime = Utility.parse(cursor.getString(cursor.getColumnIndex("dutyStatusTime")));
                    // remove seconds
                    // endTime = Utility.addSeconds(endTime, -endTime.getSeconds());
                    bean.setEndTime(Utility.format(endTime, fullFormat));

                    if (bean.getStatus() != cursor.getInt(cursor.getColumnIndex("dutyStatus"))) {
                        startTime = Utility.parse(bean.getStartTime());
                        // remove seconds
                        // startTime = Utility.addSeconds(startTime, -startTime.getSeconds());

                        int totalMinute = (int) Math.round((endTime.getTime() - startTime.getTime()) / (1000 * 60.0));
                        bean.setTotalMinutes(totalMinute);
                        list.add(bean);
                        status = cursor.getInt(cursor.getColumnIndex("dutyStatus"));
                        personalUseFg = cursor.getInt(cursor.getColumnIndex("personalUseFg"));
                        bean = DutyStatusObjectGet(status, endTime, endDate, 0, personalUseFg);
                    }
                }

                startTime = Utility.parse(bean.getStartTime());
                // remove seconds
                //  startTime = Utility.addSeconds(startTime, -startTime.getSeconds());
                int totalMinute = (int) Math.round((endDate.getTime() - startTime.getTime()) / (1000 * 60.0));
                bean.setEndTime(Utility.format(endDate, fullFormat));
                bean.setTotalMinutes(totalMinute);
                list.add(bean);
            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusGet15Days Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }

        Collections.sort(list, DutyStatusBean.dateDesc);
        return list;
    }

    public static ArrayList<DutyStatusBean> DutyStatusDrivingGet(Date date, ArrayList<DutyStatusBean> list) {

        ArrayList<DutyStatusBean> data = new ArrayList<DutyStatusBean>();

        try {
            Date nextDay = Utility.addDays(date, 1);

            for (int i = 0; i < list.size(); i++) {
                Date startDate = Utility.parse(list.get(i).getStartTime());
                Date endDate = Utility.parse(list.get(i).getEndTime());

                startDate = startDate.before(date) ? date : startDate;
                endDate = endDate.after(nextDay) ? nextDay : endDate;
                if ((startDate.after(date) || endDate.after(date))
                        && list.get(i).getStatus() >= 3 && startDate.before(nextDay)) {
                    data.add(list.get(i));
                }
            }

            Collections.sort(data, DutyStatusBean.dateAsc);

        } catch (Exception exe) {
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusGet15Days Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
        return data;
    }

    public static ArrayList<DutyStatusBean> DutyStatusOffDutyGet(Date date, ArrayList<DutyStatusBean> list) {

        ArrayList<DutyStatusBean> data = new ArrayList<>();

        try {
            Date nextDay = Utility.dateOnlyGet(Utility.addDays(Utility.dateOnlyGet(date), 1));
            Date currentDate = Utility.dateOnlyGet(date);
            for (int i = 0; i < list.size(); i++) {
                Date startDate = Utility.parse(list.get(i).getStartTime());
                Date endDate = Utility.parse(list.get(i).getEndTime());

                startDate = startDate.before(currentDate) ? currentDate : startDate;
                endDate = endDate.after(nextDay) ? nextDay : endDate;
                if (list.get(i).getStatus() <= 2 && startDate.before(date)) {
                    data.add(list.get(i));
                }

                /*if ((startDate.after(currentDate) || endDate.after(currentDate))
                        && list.get(i).getStatus() <= 2 && startDate.before(nextDay)) {
                    data.add(list.get(i));
                }*/
            }
            Collections.sort(data, DutyStatusBean.dateDesc);

        } catch (Exception exe) {
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusOffDutyGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
        return data;
    }

    public static ArrayList<DutyStatusBean> DutyStatusGet(Date date, ArrayList<DutyStatusBean> list) {
        date = Utility.dateOnlyGet(Utility.format(date, fullFormat));
        ArrayList<DutyStatusBean> data = new ArrayList<>();

        try {
            Date nextDay = Utility.addDays(date, 1);

            for (int i = 0; i < list.size(); i++) {
                Date startDate = Utility.parse(list.get(i).getStartTime());
                Date endDate = Utility.parse(list.get(i).getEndTime());

                startDate = startDate.before(date) ? date : startDate;
                endDate = endDate.after(nextDay) ? nextDay : endDate;
                if ((startDate.after(date) || endDate.after(date)) && startDate.before(nextDay)) {
                    data.add(list.get(i));
                }
            }

        } catch (Exception exe) {
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
        return data;
    }

    public static ArrayList<DutyStatusBean> DutyStatusGet(Date date, ArrayList<DutyStatusBean> list, int status) {
        date = Utility.dateOnlyGet(Utility.format(date, fullFormat));
        ArrayList<DutyStatusBean> data = new ArrayList<DutyStatusBean>();

        try {
            Date nextDay = Utility.addDays(date, 1);

            for (int i = 0; i < list.size(); i++) {
                Date startDate = Utility.parse(list.get(i).getStartTime());
                Date endDate = Utility.parse(list.get(i).getEndTime());

                startDate = startDate.before(date) ? date : startDate;
                endDate = endDate.after(nextDay) ? nextDay : endDate;
                if ((startDate.after(date) || endDate.after(date)) && startDate.before(nextDay) && list.get(i).getStatus() == status) {
                    data.add(list.get(i));
                }
            }

        } catch (Exception exe) {
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusGet Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
        return data;
    }

    public static ArrayList<DutyStatusBean> DutyStatusOffDutyGet25(Date date, ArrayList<DutyStatusBean> list) {
        date = Utility.dateOnlyGet(Utility.format(date, fullFormat));
        ArrayList<DutyStatusBean> data = new ArrayList<DutyStatusBean>();

        try {
            Date endDate = Utility.addDays(date, -14);

            for (int i = 0; i < list.size(); i++) {
                Date startDate = Utility.parse(list.get(i).getStartTime());
                Date endTime = Utility.parse(list.get(i).getEndTime());

                startDate = startDate.before(date) ? date : startDate;
                endTime = endTime.after(endDate) ? endDate : endTime;
                if ((startDate.before(date) && ((startDate.after(endDate) || startDate.equals(endDate)) ||
                        (startDate.before(endDate) && endTime.after(endDate)))) && list.get(i).getStatus() <= 2) {
                    data.add(list.get(i));
                }
            }
            Collections.sort(data, DutyStatusBean.dateDesc);

        } catch (Exception exe) {
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusOffDutyGet25 Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
        return data;
    }

    public static ArrayList<DutyStatusBean> DutyStatusDrivingGet25(Date date, ArrayList<DutyStatusBean> list) {
        date = Utility.dateOnlyGet(Utility.format(date, fullFormat));
        ArrayList<DutyStatusBean> data = new ArrayList<DutyStatusBean>();

        try {
            Date nextDay = Utility.addDays(date, 1);

            for (int i = 0; i < list.size(); i++) {
                Date startDate = Utility.parse(list.get(i).getStartTime());
                Date endTime = Utility.parse(list.get(i).getEndTime());

                startDate = startDate.before(date) ? date : startDate;
                endTime = endTime.after(nextDay) ? nextDay : endTime;
                if ((startDate.after(date) || endTime.after(date)) && startDate.before(nextDay) && list.get(i).getStatus() == 3) {
                    data.add(list.get(i));
                }
            }
            Collections.sort(data, DutyStatusBean.dateDesc);

        } catch (Exception exe) {
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusDrivingGet25 Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
        return data;
    }

    public static ArrayList<DutyStatusBean> DutyStatusOffDutyGet26(Date date, ArrayList<DutyStatusBean> list) {
        date = Utility.dateOnlyGet(Utility.format(date, fullFormat));
        ArrayList<DutyStatusBean> data = new ArrayList<DutyStatusBean>();

        try {
            Date endDate = Utility.addDays(date, -6);
            Date nextDay = Utility.addDays(date, 1);

            for (int i = 0; i < list.size(); i++) {
                Date startDate = Utility.parse(list.get(i).getStartTime());
                Date endTime = Utility.parse(list.get(i).getEndTime());

                if ((startDate.before(nextDay) && endTime.before(nextDay) && startDate.after(endDate) && list.get(i).getStatus() <= 2)) {
                    data.add(list.get(i));
                }

                if (startDate.before(endDate)) {
                    data.add(list.get(i));
                    break;
                }
            }
            Collections.sort(data, DutyStatusBean.dateDesc);

        } catch (Exception exe) {
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusOffDutyGet26 Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
        return data;
    }

    public static int DutyStatusOnDutyMinuteGet26(Date startDate, Date date, ArrayList<DutyStatusBean> list) {
        int onDutyMinute = 0;

        try {

            for (int i = 0; i < list.size(); i++) {
                Date startTime = Utility.parse(list.get(i).getStartTime());
                Date endTime = Utility.parse(list.get(i).getEndTime());

                if ((startTime.after(startDate) || startTime.equals(startDate) || endTime.after(startDate)) &&
                        startTime.before(date) && list.get(i).getStatus() >= 3) {

                    startTime = startTime.before(startDate) ? startDate : startTime;
                    endTime = endTime.after(Utility.newDate()) ? Utility.newDate() : endTime;
                    int totalMinute = (int) (endTime.getTime() - startTime.getTime()) / (1000 * 60);
                    onDutyMinute += totalMinute;
                }
            }


        } catch (Exception exe) {
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusOnDutyMinuteGet26 Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
        return onDutyMinute;
    }

    public static ArrayList<DutyStatusBean> DutyStatusDrivingOnDutyGet26(Date date, ArrayList<DutyStatusBean> list) {
        date = Utility.dateOnlyGet(Utility.format(date, fullFormat));
        ArrayList<DutyStatusBean> data = new ArrayList<DutyStatusBean>();

        try {
            Date nextDay = Utility.addDays(date, 1);

            for (int i = 0; i < list.size(); i++) {
                Date startDate = Utility.parse(list.get(i).getStartTime());
                Date endTime = Utility.parse(list.get(i).getEndTime());

                startDate = startDate.before(date) ? date : startDate;
                endTime = endTime.after(nextDay) ? nextDay : endTime;
                if ((startDate.after(date) || endTime.after(date)) && startDate.before(nextDay) && list.get(i).getStatus() >= 3) {
                    data.add(list.get(i));
                }
            }
            Collections.sort(data, DutyStatusBean.dateDesc);

        } catch (Exception exe) {
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusDrivingOnDutyGet26 Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
        return data;
    }

    public static ArrayList<DutyStatusBean> DutyStatusOffDutyGet395_B(Date date, ArrayList<DutyStatusBean> list) {
        date = Utility.dateOnlyGet(Utility.format(date, fullFormat));
        ArrayList<DutyStatusBean> data = new ArrayList<DutyStatusBean>();

        try {
            Date endDate = Utility.addDays(date, -7);
            Date nextDay = Utility.addDays(date, 1);

            for (int i = 0; i < list.size(); i++) {
                Date startDate = Utility.parse(list.get(i).getStartTime());
                Date endTime = Utility.parse(list.get(i).getEndTime());

                if ((startDate.before(nextDay) && endTime.before(nextDay) && startDate.after(endDate) && list.get(i).getStatus() <= 2)) {
                    data.add(list.get(i));
                }

                if (startDate.before(endDate)) {
                    data.add(list.get(i));
                    break;
                }
            }
            Collections.sort(data, DutyStatusBean.dateDesc);

        } catch (Exception exe) {
            LogFile.write(HourOfServiceDB.class.getName() + "::DutyStatusOffDutyGet395_B Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
        return data;
    }
}
