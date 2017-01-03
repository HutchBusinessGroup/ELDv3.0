package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.AxleBean;
import com.hutchgroup.elog.beans.VehicleBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Tpms;
import com.hutchgroup.elog.common.Utility;

import java.util.ArrayList;

/**
 * Created by SAMSUNG on 28-12-2016.
 */

public class VehicleDB {

    // Created By: Deepak Sharma
    // Created Date: 28 December 2016
    // Purpose: add or update vehicles in database
    public static boolean Save(ArrayList<VehicleBean> lst) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (int i = 0; i < lst.size(); i++) {
                VehicleBean bean = lst.get(i);
                values.put("VehicleId", bean.getVehicleId());
                values.put("UnitNo", bean.getUnitNo());
                values.put("PlateNo", bean.getPlateNo());
                values.put("TotalAxle", bean.getTotalAxle());
                Cursor cursor = database.rawQuery("select VehicleId from "
                        + MySQLiteOpenHelper.TABLE_TRAILER
                        + " where VehicleId=?", new String[]{bean.getVehicleId() + ""});
                int vehicleId = 0;
                if (cursor.moveToNext()) {
                    vehicleId = cursor.getInt(0);
                }
                cursor.close();

                if (vehicleId == 0) {
                    database.insert(MySQLiteOpenHelper.TABLE_TRAILER,
                            null, values);

                } else {
                    database.update(MySQLiteOpenHelper.TABLE_TRAILER, values,
                            " VehicleId= ?",
                            new String[]{bean.getVehicleId() + ""});
                }
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(VehicleDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 28 December 2016
    // Purpose: add or update Axle info in database
    public static boolean SaveAxleInfo(ArrayList<AxleBean> lst) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            for (int i = 0; i < lst.size(); i++) {
                AxleBean bean = lst.get(i);
                values.put("axleId", bean.getAxleId());
                values.put("VehicleId", bean.getVehicleId());
                values.put("axleNo", bean.getAxleNo());
                values.put("axlePosition", bean.getAxlePosition());
                values.put("doubleTireFg", (bean.isDoubleTireFg() ? 1 : 0));
                values.put("frontTireFg", (bean.isFrontTireFg() ? 1 : 0));
                values.put("PowerUnitFg", (bean.isPowerUnitFg() ? 1 : 0));
                values.put("sensorIds", bean.getSensorIds());
                values.put("pressures", bean.getPressures());
                values.put("temperatures", bean.getTemperatures());

                Cursor cursor = database.rawQuery("select axleId from "
                        + MySQLiteOpenHelper.TABLE_AXLE_INFO
                        + " where axleId=?", new String[]{bean.getAxleId() + ""});
                int axleId = 0;
                if (cursor.moveToNext()) {
                    axleId = cursor.getInt(0);
                }

                cursor.close();

                if (axleId == 0) {
                    database.insert(MySQLiteOpenHelper.TABLE_AXLE_INFO,
                            null, values);

                } else {
                    database.update(MySQLiteOpenHelper.TABLE_AXLE_INFO, values,
                            " axleId= ?",
                            new String[]{bean.getAxleId() + ""});
                }
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            LogFile.write(VehicleDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }
        return status;
    }

    public static ArrayList<AxleBean> AxleInfoGet(ArrayList<String> vehicleIds) {
        ArrayList<AxleBean> list = new ArrayList<>();
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            for (String id : vehicleIds) {
                cursor = database.rawQuery("select VehicleId ,axleNo ,axlePosition ,doubleTireFg ,frontTireFg ,PowerUnitFg ,sensorIds ,pressures ,temperatures from "
                                + MySQLiteOpenHelper.TABLE_AXLE_INFO + " Where VehicleId =? order by frontTireFg desc,axlePosition"
                        , new String[]{id});
                while (cursor.moveToNext()) {
                    AxleBean bean = new AxleBean();
                    bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
                    bean.setAxleNo(cursor.getInt(cursor.getColumnIndex("axleNo")));
                    bean.setAxlePosition(cursor.getInt(cursor.getColumnIndex("axlePosition")));
                    bean.setDoubleTireFg(cursor.getInt(cursor.getColumnIndex("doubleTireFg")) == 1);
                    bean.setFrontTireFg(cursor.getInt(cursor.getColumnIndex("frontTireFg")) == 1);
                    bean.setPowerUnitFg(cursor.getInt(cursor.getColumnIndex("PowerUnitFg")) == 1);
                    bean.setSensorIds(cursor.getString(cursor.getColumnIndex("sensorIds")));
                    bean.setTemperatures(cursor.getString(cursor.getColumnIndex("pressures")));
                    bean.setPressures(cursor.getString(cursor.getColumnIndex("temperatures")));

                    String[] pressures = bean.getPressures().split(",");
                    if (pressures.length == 3) {
                        bean.setPressure(Double.parseDouble(pressures[0]));
                        bean.setLowPressure(Double.parseDouble(pressures[1]));
                        bean.setHighPressure(Double.parseDouble(pressures[2]));
                    }

                    String[] temperatures = bean.getTemperatures().split(",");
                    if (pressures.length == 3) {
                        bean.setTemperature(Double.parseDouble(temperatures[0]));
                        bean.setLowTemperature(Double.parseDouble(temperatures[1]));
                        bean.setHighTemperature(Double.parseDouble(temperatures[2]));
                    }

                    String[] sensorIds = bean.getSensorIds().split(",");
                    bean.setSensorIdsAll(sensorIds);
                    Tpms.getTpmsData(sensorIds, bean);
                    list.add(bean);
                }
            }

        } catch (Exception e) {
            Utility.printError(e.getMessage());
            LogFile.write(VehicleDB.class.getName() + "::AxleInfoGet Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
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

    public static ArrayList<VehicleBean> TrailerGet(String search, String except) {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        ArrayList<VehicleBean> list = new ArrayList<>();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getReadableDatabase();

            cursor = database.rawQuery("select VehicleId, UnitNo, PlateNo from "
                    + MySQLiteOpenHelper.TABLE_TRAILER
                    + " where UnitNo like ? " + " and VehicleId not in (" + except + ") order by UnitNo", new String[]{search + "%"});

            while (cursor.moveToNext()) {
                VehicleBean bean = new VehicleBean();
                bean.setVehicleId(cursor.getInt(cursor.getColumnIndex("VehicleId")));
                bean.setUnitNo(cursor.getString(cursor.getColumnIndex("UnitNo")));
                bean.setPlateNo(cursor.getString(cursor.getColumnIndex("PlateNo")));
                list.add(bean);
            }
        } catch (Exception e) {

            Utility.printError(e.getMessage());
            LogFile.write(VehicleDB.class.getName() + "::TrailerGet Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
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

}
