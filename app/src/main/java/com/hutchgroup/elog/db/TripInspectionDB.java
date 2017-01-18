package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Message;
import android.util.Log;

import com.hutchgroup.elog.beans.MessageBean;
import com.hutchgroup.elog.beans.TripInspectionBean;
import com.hutchgroup.elog.beans.UserBean;
import com.hutchgroup.elog.common.BitmapUtility;
import com.hutchgroup.elog.common.ChatClient;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TripInspectionDB {

    public static TripInspectionBean CreateTripInspection(String dateTime, int driverId, String driverName, int type, int defect, int defectRepaired, int safeToDrive, String defectItems, String latitude, String longitude,
                                                          String location, String odometer, String truckNumber, String trailerNumber, String comments, String pictures) {
        TripInspectionBean bean = new TripInspectionBean();
        bean.setInspectionDateTime(dateTime);
        bean.setDriverId(driverId);
        bean.setDriverName(driverName);
        bean.setType(type);
        bean.setDefect(defect);
        bean.setDefectRepaired(defectRepaired);
        bean.setSafeToDrive(safeToDrive);
        bean.setDefectItems(defectItems);
        bean.setLatitude(latitude);
        bean.setLongitude(longitude);
        bean.setLocation(location);
        bean.setOdometerReading(odometer);
        bean.setTruckNumber(truckNumber);
        bean.setTrailerNumber(trailerNumber);
        bean.setComments(comments);
        bean.setPictures(pictures);
        bean.setSyncFg(0);

        Save(bean);
        return bean;

    }

    public static void Save(TripInspectionBean bean) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;

        int tripInspectionId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("DateTime", bean.getInspectionDateTime());
            values.put("DriverId", bean.getDriverId());
            values.put("DriverName", bean.getDriverName());
            values.put("Type", bean.getType());
            values.put("Defect", bean.getDefect());
            values.put("DefectRepaired", bean.getDefectRepaired());
            values.put("SafeToDrive", bean.getSafeToDrive());
            values.put("DefectItems", bean.getDefectItems());
            values.put("Latitude", bean.getLatitude());
            values.put("Longitude", bean.getLongitude());
            values.put("LocationDescription", bean.getLocation());
            values.put("Odometer", bean.getOdometerReading());
            values.put("TruckNumber", bean.getTruckNumber());
            values.put("TrailerNumber", bean.getTrailerNumber());
            values.put("Comments", bean.getComments());
            values.put("Pictures", bean.getPictures());
            values.put("SyncFg", bean.getSyncFg());

            tripInspectionId = (int) database.insertOrThrow(MySQLiteOpenHelper.TABLE_TRIP_INSPECTION,
                    "_id", values);
            Log.e(TripInspectionDB.class.getName(), "Saved " + tripInspectionId);
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            Log.e(TripInspectionDB.class.getName(), "::Save Error:" + e.getMessage());
            LogFile.write(TripInspectionDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
                LogFile.write(TripInspectionDB.class.getName() + "::Save close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
    }

    public static ArrayList<TripInspectionBean> getInspections(String date) {
        ArrayList<TripInspectionBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id, DateTime, DriverId, DriverName, Type, Defect, DefectRepaired, SafeToDrive, DefectItems, Latitude, Longitude, " +
                    "LocationDescription, Odometer, TruckNumber, TrailerNumber, Comments, Pictures,SyncFg from " +
                    MySQLiteOpenHelper.TABLE_TRIP_INSPECTION + " where DateTime>=? order by DateTime desc", new String[]{date});
            while (cursor.moveToNext()) {
                TripInspectionBean bean = new TripInspectionBean();
                bean.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                bean.setInspectionDateTime(cursor.getString(cursor.getColumnIndex("DateTime")));
                bean.setDriverId(cursor.getInt(cursor.getColumnIndex("DriverId")));
                bean.setDriverName(cursor.getString(cursor.getColumnIndex("DriverName")));
                bean.setType(cursor.getInt(cursor.getColumnIndex("Type")));
                bean.setDefect(cursor.getInt(cursor.getColumnIndex("Defect")));
                bean.setDefectRepaired(cursor.getInt(cursor.getColumnIndex("DefectRepaired")));
                bean.setSafeToDrive(cursor.getInt(cursor.getColumnIndex("SafeToDrive")));
                bean.setDefectItems(cursor.getString(cursor.getColumnIndex("DefectItems")));
                bean.setLatitude(cursor.getString(cursor.getColumnIndex("Latitude")));
                bean.setLongitude(cursor.getString(cursor.getColumnIndex("Longitude")));
                bean.setLocation(cursor.getString(cursor.getColumnIndex("LocationDescription")));
                bean.setOdometerReading(cursor.getString(cursor.getColumnIndex("Odometer")));
                bean.setTruckNumber(cursor.getString(cursor.getColumnIndex("TruckNumber")));
                bean.setTrailerNumber(cursor.getString(cursor.getColumnIndex("TrailerNumber")));
                bean.setComments(cursor.getString(cursor.getColumnIndex("Comments")));
                bean.setPictures(cursor.getString(cursor.getColumnIndex("Pictures")));
                bean.setSyncFg(cursor.getInt(cursor.getColumnIndex("SyncFg")));

                list.add(bean);
            }

        } catch (Exception e) {
            LogFile.write(TripInspectionDB.class.getName() + "::getInspections Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(TripInspectionDB.class.getName() + "::getInspections close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return list;
    }

    public static long removeDVIR(String id) {

        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        long res = -1;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            String[] ids = id.split(",");
            for (String _id : ids) {
                res = database.delete(MySQLiteOpenHelper.TABLE_TRIP_INSPECTION,
                        "_id=?", new String[]{_id});
            }

        } catch (Exception e) {
            System.out.println("removeDVIR");
            e.printStackTrace();
            LogFile.write(TrackingDB.class.getName() + "::removeDVIR Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception exe) {

            }
        }
        return res;
    }
    public static ArrayList<TripInspectionBean> getInspectionsToRemove(String date) {
        ArrayList<TripInspectionBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id,Pictures from " +
                    MySQLiteOpenHelper.TABLE_TRIP_INSPECTION + " where DateTime<? and SyncFg=1 order by DateTime desc", new String[]{date});
            while (cursor.moveToNext()) {
                TripInspectionBean bean = new TripInspectionBean();
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String pictures = cursor.getString(cursor.getColumnIndex("Pictures"));
                bean.setId(id);
                bean.setPictures(pictures);
                list.add(bean);
            }

        } catch (Exception e) {
            LogFile.write(TripInspectionDB.class.getName() + "::getInspections Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(TripInspectionDB.class.getName() + "::getInspections close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return list;
    }


    public static boolean getInspections(String date, int driverId) {
        boolean status = false;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();
            cursor = database.rawQuery("select _id from " +
                    MySQLiteOpenHelper.TABLE_TRIP_INSPECTION + " where DateTime>=? and driverId=? order by DateTime desc Limit 1", new String[]{date, driverId + ""});
            if (cursor.moveToNext()) {
                status = true;
            }

        } catch (Exception e) {
            LogFile.write(TripInspectionDB.class.getName() + "::getInspections Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();
            } catch (Exception e) {
                LogFile.write(TripInspectionDB.class.getName() + "::getInspections close DB Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 27 July 2016
    // Purpose: get DVIR for web sync
    public static JSONArray getDVIRSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();
        int id = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select _id, DateTime , DriverId , DriverName , Type , Defect , DefectRepaired , SafeToDrive , DefectItems , Latitude " +
                            ", Longitude , LocationDescription , Odometer , TruckNumber , TrailerNumber , Comments , Pictures ,SyncFg INTEGER from " + MySQLiteOpenHelper.TABLE_TRIP_INSPECTION +
                            " where SyncFg=0"
                    , null);
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                int inspectionId = cursor.getInt(0);
                obj.put("InspectionId", inspectionId);
                obj.put("InspectionDateTime", Utility.getDateTimeForServer(cursor.getString(cursor.getColumnIndex("DateTime"))));

                obj.put("UserId", cursor.getInt(cursor.getColumnIndex("DriverId")));
                obj.put("InspectionType", cursor.getInt(cursor.getColumnIndex("Type")));
                obj.put("DefectFg", cursor.getInt(cursor.getColumnIndex("Defect")));
                obj.put("RepairedFg", cursor.getInt(cursor.getColumnIndex("DefectRepaired")));
                obj.put("SafeToDriveFg", cursor.getInt(cursor.getColumnIndex("SafeToDrive")));
                obj.put("DefectItems", cursor.getString(cursor.getColumnIndex("DefectItems")));
                obj.put("Latitude", cursor.getString(cursor.getColumnIndex("Latitude")));
                obj.put("Longitude", cursor.getString(cursor.getColumnIndex("Longitude")));
                obj.put("LocationDescription", cursor.getString(cursor.getColumnIndex("LocationDescription")));
                double odometer = 0d;
                try {
                    odometer = Double.parseDouble(cursor.getString(cursor.getColumnIndex("Odometer")));
                    JSONArray imgArray = new JSONArray();
                    String[] images = cursor.getString(cursor.getColumnIndex("Pictures")).split(",");
                    for (String img : images) {
                        String imageContent = BitmapUtility.convertToBase64String(img);
                        JSONObject objImage = new JSONObject();
                        objImage.put("InspectionId", inspectionId);
                        objImage.put("ImageContent", imageContent);
                        imgArray.put(objImage);
                        // break;
                    }
                    obj.put("tripImages", imgArray);

                } catch (Exception exe) {
                }

                obj.put("OdometerReading", odometer);
                obj.put("TruckNumber", cursor.getString(cursor.getColumnIndex("TruckNumber")));
                obj.put("TrailerNumber", cursor.getString(cursor.getColumnIndex("TrailerNumber")));
                obj.put("Comments", cursor.getString(cursor.getColumnIndex("Comments")));
                obj.put("CompanyId", Utility.companyId);
                obj.put("CreatedBy", Utility.activeUserId);
                obj.put("CreatedDate", Utility.getDateTimeForServer(cursor.getString(cursor.getColumnIndex("DateTime"))));
                obj.put("StatusId", 1);
                // obj.put("tripImages", cursor.getString(cursor.getColumnIndex("")));

                array.put(obj);

            }
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(TripInspectionDB.class.getName() + "::getDVIRSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
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
    // Created Date: 27 July 2016
    // Purpose: update DVIR for web sync
    public static JSONArray DVIRSyncUpdate() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        JSONArray array = new JSONArray();
        int inspectionId = 0;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("SyncFg", 1);
            database.update(MySQLiteOpenHelper.TABLE_TRIP_INSPECTION, values,
                    " SyncFg=?", new String[]{"0"});
            /*cursor = database.rawQuery("select _id from " + MySQLiteOpenHelper.TABLE_TRIP_INSPECTION +
                            " where SyncFg=0 order by _id Limit 1"
                    , null);
            if (cursor.moveToNext()) {
                inspectionId = cursor.getInt(0);
                ContentValues values = new ContentValues();
                values.put("SyncFg", 1);
                database.update(MySQLiteOpenHelper.TABLE_TRIP_INSPECTION, values,
                        " _id=?", new String[]{inspectionId + ""});
            }*/
        } catch (Exception exe) {
            Utility.printError(exe.getMessage());
            LogFile.write(TripInspectionDB.class.getName() + "::getDVIRSync Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                database.close();
                helper.close();
            } catch (Exception e) {
                Utility.printError(e.getMessage());
            }
        }
        return array;
    }

}
