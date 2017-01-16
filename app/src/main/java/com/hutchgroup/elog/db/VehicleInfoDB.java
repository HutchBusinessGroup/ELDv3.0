package com.hutchgroup.elog.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hutchgroup.elog.beans.VehicleInfoBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by SAMSUNG on 16-01-2017.
 */

public class VehicleInfoDB {

    // Created By: Deepak Sharma
    // Created Date: 16 January 2017
    // Purpose: Save vehicle info
    public static boolean Save(VehicleInfoBean bean) {
        boolean status = true;
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("CreatedDate", bean.getCreatedDate());
            values.put("OdometerReading", bean.getOdometerReading());
            values.put("Speed", bean.getSpeed());
            values.put("RPM", bean.getRPM());
            values.put("Average", bean.getAverage());
            values.put("EngineHour", bean.getEngineHour());
            values.put("FuelUsed", bean.getFuelUsed());
            values.put("IdleFuelUsed", bean.getIdleFuelUsed());
            values.put("IdleHours", bean.getIdleHours());
            values.put("Boost", bean.getBoost());
            values.put("CoolantTemperature", bean.getCoolantTemperature());
            values.put("CoolantLevel", bean.getCoolantLevel());
            values.put("BatteryVoltage", bean.getBatteryVoltage());
            values.put("WasherFluidLevel", bean.getWasherFluidLevel());
            values.put("EngineLoad", bean.getEngineLoad());
            values.put("EngineOilLevel", bean.getEngineOilLevel());
            values.put("CruiseSpeed", bean.getCruiseSpeed());
            values.put("MaxRoadSpeed", bean.getMaxRoadSpeed());
            values.put("AirSuspension", bean.getAirSuspension());
            values.put("TransmissionOilLevel", bean.getTransmissionOilLevel());
            values.put("DEFTankLevel", bean.getDEFTankLevel());
            values.put("DEFTankLevelLow", bean.getDEFTankLevelLow());
            values.put("EngineSerialNo", bean.getEngineSerialNo());
            values.put("EngineRatePower", bean.getEngineRatePower());

            values.put("CruiseSetFg", bean.getCruiseSetFg());
            values.put("PowerUnitABSFg", bean.getPowerUnitABSFg());
            values.put("TrailerABSFg", bean.getTrailerABSFg());
            values.put("DerateFg", bean.getDerateFg());
            values.put("BrakeApplication", bean.getBrakeApplication());
            values.put("RegenerationRequiredFg", bean.getRegenerationRequiredFg());
            values.put("WaterInFuelFg", bean.getWaterInFuelFg());
            values.put("PTOEngagementFg", bean.getPTOEngagementFg());
            values.put("CuriseTime", bean.getCuriseTime());
            values.put("SeatBeltFg", bean.getSeatBeltFg());
            values.put("TransmissionGear", bean.getTransmissionGear());
            values.put("ActiveDTCFg", bean.getActiveDTCFg());
            values.put("InActiveDTCFg", bean.getInActiveDTCFg());
            values.put("PTOHours", bean.getPTOHours());
            values.put("TPMSWarningFg", bean.getTPMSWarningFg());
            values.put("SyncFg", bean.getSyncFg());

            database.insert(MySQLiteOpenHelper.TABLE_VEHICLE_INFO,
                    "_id", values);

        } catch (Exception e) {
            status = false;
            LogFile.write(VehicleInfoDB.class.getName() + "::Save Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            database.close();
            helper.close();
        }
        return status;

    }

    // Created By: Deepak Sharma
    // Created Date: 16 January 2017
    // Purpose: get Vehicle Info for web sync
    public static JSONArray getVehicleInfoSync() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;

        JSONArray array = new JSONArray();
        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();

            cursor = database.rawQuery("select _Id , OdometerReading   ,Speed   ,RPM   ,Average   ,EngineHour   ,FuelUsed   ,IdleFuelUsed   ,IdleHours   ,Boost   ,CoolantTemperature   ,CoolantLevel   ,BatteryVoltage   ,WasherFluidLevel   ,EngineLoad   ,EngineOilLevel   ,CruiseSetFg  ,CruiseSpeed   ,PowerUnitABSFg  ,TrailerABSFg  ,DerateFg  ,BrakeApplication  ,RegenerationRequiredFg  ,WaterInFuelFg  ,MaxRoadSpeed   ,PTOEngagementFg  ,CuriseTime  ,SeatBeltFg  ,AirSuspension   ,TransmissionOilLevel   ,TransmissionGear  ,DEFTankLevel   ,DEFTankLevelLow  ,ActiveDTCFg ,InActiveDTCFg ,PTOHours ,TPMSWarningFg ,EngineSerialNo  ,EngineRatePower  ,CreatedDate from "
                            + MySQLiteOpenHelper.TABLE_VEHICLE_INFO + " Where SyncFg=0"
                    , null);

            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                obj.put("CreatedDate", cursor.getString(cursor.getColumnIndex("CreatedDate")));
                obj.put("EngineSerialNo", cursor.getString(cursor.getColumnIndex("EngineSerialNo")));

                obj.put("OdometerReading", Double.parseDouble(cursor.getString(cursor.getColumnIndex("OdometerReading"))));
                obj.put("Speed", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Speed"))));
                obj.put("RPM", Double.parseDouble(cursor.getString(cursor.getColumnIndex("RPM"))));
                obj.put("Average", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Average"))));
                obj.put("EngineHour", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineHour"))));
                obj.put("FuelUsed", Double.parseDouble(cursor.getString(cursor.getColumnIndex("FuelUsed"))));
                obj.put("IdleFuelUsed", Double.parseDouble(cursor.getString(cursor.getColumnIndex("IdleFuelUsed"))));
                obj.put("IdleHours", Double.parseDouble(cursor.getString(cursor.getColumnIndex("IdleHours"))));
                obj.put("Boost", Double.parseDouble(cursor.getString(cursor.getColumnIndex("Boost"))));
                obj.put("CoolantTemperature", Double.parseDouble(cursor.getString(cursor.getColumnIndex("CoolantTemperature"))));
                obj.put("CoolantLevel", Double.parseDouble(cursor.getString(cursor.getColumnIndex("CoolantLevel"))));
                obj.put("BatteryVoltage", Double.parseDouble(cursor.getString(cursor.getColumnIndex("BatteryVoltage"))));
                obj.put("WasherFluidLevel", Double.parseDouble(cursor.getString(cursor.getColumnIndex("WasherFluidLevel"))));
                obj.put("EngineLoad", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineLoad"))));
                obj.put("EngineOilLevel", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineOilLevel"))));
                obj.put("CruiseSpeed", Double.parseDouble(cursor.getString(cursor.getColumnIndex("CruiseSpeed"))));
                obj.put("MaxRoadSpeed", Double.parseDouble(cursor.getString(cursor.getColumnIndex("MaxRoadSpeed"))));
                obj.put("AirSuspension", Double.parseDouble(cursor.getString(cursor.getColumnIndex("AirSuspension"))));
                obj.put("TransmissionOilLevel", Double.parseDouble(cursor.getString(cursor.getColumnIndex("TransmissionOilLevel"))));
                obj.put("DEFTankLevel", Double.parseDouble(cursor.getString(cursor.getColumnIndex("DEFTankLevel"))));
                obj.put("DEFTankLevelLow", Double.parseDouble(cursor.getString(cursor.getColumnIndex("DEFTankLevelLow"))));
                obj.put("EngineRatePower", Double.parseDouble(cursor.getString(cursor.getColumnIndex("EngineRatePower"))));

                obj.put("CruiseSetFg", cursor.getInt(cursor.getColumnIndex("CruiseSetFg")));
                obj.put("PowerUnitABSFg", cursor.getInt(cursor.getColumnIndex("PowerUnitABSFg")));
                obj.put("TrailerABSFg", cursor.getInt(cursor.getColumnIndex("TrailerABSFg")));
                obj.put("DerateFg", cursor.getInt(cursor.getColumnIndex("DerateFg")));
                obj.put("BrakeApplication", cursor.getInt(cursor.getColumnIndex("BrakeApplication")));
                obj.put("RegenerationRequiredFg", cursor.getInt(cursor.getColumnIndex("RegenerationRequiredFg")));
                obj.put("WaterInFuelFg", cursor.getInt(cursor.getColumnIndex("WaterInFuelFg")));
                obj.put("PTOEngagementFg", cursor.getInt(cursor.getColumnIndex("PTOEngagementFg")));
                obj.put("CuriseTime", cursor.getInt(cursor.getColumnIndex("CuriseTime")));
                obj.put("SeatBeltFg", cursor.getInt(cursor.getColumnIndex("SeatBeltFg")));
                obj.put("TransmissionGear", cursor.getInt(cursor.getColumnIndex("TransmissionGear")));
                obj.put("ActiveDTCFg", cursor.getInt(cursor.getColumnIndex("ActiveDTCFg")));
                obj.put("InActiveDTCFg", cursor.getInt(cursor.getColumnIndex("InActiveDTCFg")));
                obj.put("PTOHours", cursor.getInt(cursor.getColumnIndex("PTOHours")));
                obj.put("TPMSWarningFg", cursor.getInt(cursor.getColumnIndex("TPMSWarningFg")));

                array.put(obj);
            }

        } catch (Exception e) {
            LogFile.write(VehicleInfoDB.class.getName() + "::getVehicleInfoSync Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        } finally {
            try {
                cursor.close();
                database.close();
                helper.close();

            } catch (Exception e2) {
                // TODO: handle exception
            }
        }
        return array;
    }


    // Created By: Deepak Sharma
    // Created Date: 22 December 2016
    // Purpose: update TPMS for web sync
    public static JSONArray VehicleInfoSyncDelete() {
        MySQLiteOpenHelper helper = null;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        JSONArray array = new JSONArray();

        try {
            helper = new MySQLiteOpenHelper(Utility.context);
            database = helper.getWritableDatabase();
            database.delete(MySQLiteOpenHelper.TABLE_TRAILER_STATUS,
                    " SyncFg=?", new String[]{"0"});

        } catch (Exception exe) {
            LogFile.write(VehicleInfoDB.class.getName() + "::VehicleInfoSyncUpdate Error:" + exe.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
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


}
