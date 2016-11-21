package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.CarrierInfoBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.common.ZoneList;

import java.util.ArrayList;
import java.util.TimeZone;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class CarrierInfoDB {

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: check duplicate account
    public static void getCompanyInfo() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select VehicleId,CompanyId,CarrierName,ELDManufacturer,USDOT,UnitNo,VIN, MACAddress,PlateNo,TimeZoneId from "
                            + MySQLiteOpenHelper.TABLE_CARRIER + " Where SerialNo=? LIMIT 1"
                    , new String[]{Utility.IMEI}
            );
            if (cursor.moveToFirst()) {
                Utility.vehicleId = cursor.getInt(0);
                Utility.companyId = cursor.getInt(1);

                Utility.CarrierName = cursor.getString(2);
                Utility.ELDManufacturer = cursor.getString(3);
                Utility.USDOT = cursor.getString(4);
                Utility.UnitNo = cursor.getString(5);
                Utility.VIN = cursor.getString(6);
                Utility.MACAddress = cursor.getString(7);
                Utility.PlateNo = cursor.getString(8);
                Utility.TimeZoneId = cursor.getString(9);
                Utility.TimeZoneOffset = ZoneList.getOffset(Utility.TimeZoneId);
                Utility.TimeZoneOffsetUTC = ZoneList.getTimeZoneOffset(Utility.TimeZoneId);
                Utility.sdf.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + "::getCompanyInfo Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                // TODO: handle exception
            }
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: check duplicate account
    private static int checkDuplicate(int vehicleId) {
        int recordId = 0;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select VehicleId from "
                    + MySQLiteOpenHelper.TABLE_CARRIER
                    + " where VehicleId=?", new String[]{vehicleId + ""});
            if (cursor.moveToFirst()) {
                recordId = cursor.getInt(0);

            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + "::checkDuplicate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
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
    public static boolean Save(ArrayList<CarrierInfoBean> lst) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (int i = 0; i < lst.size(); i++) {
                CarrierInfoBean bean = lst.get(i);
                values.put("CompanyId", bean.getCompanyId());
                values.put("CarrierName", bean.getCarrierName());
                values.put("ELDManufacturer", bean.getELDManufacturer());
                values.put("USDOT", bean.getUSDOT());
                values.put("VehicleId", bean.getVehicleId());
                values.put("UnitNo", bean.getUnitNo());
                values.put("VIN", bean.getVIN());
                values.put("PlateNo", bean.getPlateNo());
                values.put("StatusId", bean.getStatusId());
                values.put("SerialNo", bean.getSerailNo());
                values.put("MACAddress", bean.getMACAddress());
                values.put("TimeZoneId", bean.getTimeZoneId());
                int vehicleId = checkDuplicate(bean.getVehicleId());
                if (vehicleId == 0) {
                    database.insert(MySQLiteOpenHelper.TABLE_CARRIER,
                            "modifiedDate", values);

                } else {
                    database.update(MySQLiteOpenHelper.TABLE_CARRIER, values,
                            " VehicleId= ?",
                            new String[]{bean.getVehicleId() + ""});
                }

                if (Utility.IMEI.equals(bean.getSerailNo())) {

                    Utility.vehicleId = bean.getVehicleId();
                    Utility.companyId = bean.getCompanyId();
                    Utility.CarrierName = bean.getCarrierName();
                    Utility.ELDManufacturer = bean.getELDManufacturer();
                    Utility.USDOT = bean.getUSDOT();
                    Utility.UnitNo = bean.getUnitNo();
                    Utility.PlateNo = bean.getPlateNo();
                    Utility.VIN = bean.getVIN();
                    Utility.MACAddress = bean.getMACAddress();
                    Utility.TimeZoneId = bean.getTimeZoneId();
                    Utility.TimeZoneOffset = ZoneList.getOffset(Utility.TimeZoneId);
                    Utility.TimeZoneOffsetUTC = ZoneList.getTimeZoneOffset(Utility.TimeZoneId);
                    Utility.sdf.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
                }
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + "::checkDuplicate Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }
        return status;

    }

    public static boolean SaveUnitNo() {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("CompanyId", Utility.companyId);
            values.put("CarrierName", Utility.CarrierName);
            values.put("ELDManufacturer", Utility.ELDManufacturer);
            values.put("USDOT", Utility.USDOT);
            values.put("UnitNo", Utility.UnitNo);
            values.put("PlateNo", Utility.PlateNo);
            values.put("VIN", Utility.VIN);
            values.put("SerialNo", Utility.IMEI);
            values.put("MACAddress", Utility.MACAddress);

            database.update(MySQLiteOpenHelper.TABLE_CARRIER, values,
                    " VehicleId= ?",
                    new String[]{Utility.vehicleId + ""});
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + "::SaveUnitNo Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }
        return status;

    }

    public static void UpdateTimeZone() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("TimeZoneId", TimeZone.getDefault().getID());
            database.update(MySQLiteOpenHelper.TABLE_CARRIER, values,
                    " VehicleId > 0",
                    null);
        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(CarrierInfoDB.class.getName() + "::UpdateTimeZone Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }
    }


}
