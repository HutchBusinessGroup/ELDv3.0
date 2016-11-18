package com.hutchgroup.elog.common;

import android.util.Log;

import com.hutchgroup.elog.beans.CarrierInfoBean;
import com.hutchgroup.elog.beans.DailyLogBean;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.beans.MessageBean;
import com.hutchgroup.elog.beans.PlaceBean;
import com.hutchgroup.elog.beans.UserBean;
import com.hutchgroup.elog.beans.VersionInformationBean;
import com.hutchgroup.elog.db.CarrierInfoDB;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.MessageDB;
import com.hutchgroup.elog.db.PlaceInfoDB;
import com.hutchgroup.elog.db.UserDB;
import com.hutchgroup.elog.db.VersionInformationDB;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Deepak.Sharma on 1/14/2016.
 */
public class GetCall {

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: sync user accounts with web
    public static boolean AccountSync(int userId) {
        boolean status = true;
        WebService ws = new WebService();
        String result = "";
        try {

            result = ws
                    .doGet(WebUrl.GET_ACCOUNT
                            + Utility.IMEI + "&userId=" + userId);
            if (result == null || result.isEmpty())
                return status;

            JSONArray obja = new JSONArray(result);
            //Log.i("DB", obja.toString());
            ArrayList<UserBean> al = new ArrayList<UserBean>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                UserBean bean = new UserBean();
                bean.setAccountId(json.getInt("AccountId"));
                bean.setFirstName(json.getString("FirstName"));
                bean.setLastName(json.getString("LastName"));
                bean.setEmailId(json.getString("EmailId"));
                bean.setMobileNo(json.getString("MobileNo"));
                bean.setAccountType(json.getInt("AccountType"));
                bean.setDrivingLicense(json.getString("LicenseNo"));
                bean.setDlIssueState(json.getString("DLIssuerState"));
                bean.setUserName(json.getString("UserName"));
                bean.setPassword(json.getString("Password"));
                bean.setDotPassword(json.getString("DotPassword"));
                bean.setSalt(json.getString("Salt"));
                bean.setExemptELDUseFg(json.getBoolean("ExemptELDUseFg") ? 1 : 0);
                bean.setExemptionRemark(json.getString("ExemptionRemarks"));
                bean.setSpecialCategory(json.getString("SpecialCategory"));
                bean.setCurrentRule(json.getInt("CurrentRule"));
                bean.setTimeZoneOffsetUTC(json.getString("TimeZoneOffsetUTC"));
                bean.setLicenseAcceptFg(json.getInt("LicenseAcceptFg"));
                bean.setLicenseExpiryDate(json.getString("LicenseExpiryDate"));
                bean.setStatusId(json.getInt("StatusId"));
                al.add(bean);
            }

            if (al.size() > 0) {
                UserDB.Save(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
           // LogFile.write(GetCall.class.getName() + "::AccountSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: sync user accounts with web
    public static boolean CarrierInfoSync() {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_CARRIER_INFO
                            + Utility.IMEI);
            if (result == null || result.isEmpty())
                return status;

            JSONArray obja = new JSONArray(result);
            ArrayList<CarrierInfoBean> al = new ArrayList<CarrierInfoBean>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                CarrierInfoBean bean = new CarrierInfoBean();
                bean.setCompanyId(json.getInt("CompanyId"));
                bean.setCarrierName(json.getString("CarrierName"));
                bean.setELDManufacturer(json.getString("ELDManufacturer"));
                bean.setUSDOT(json.getString("USDOT"));
                bean.setVehicleId(json.getInt("VehicleId"));
                bean.setUnitNo(json.getString("UnitNo"));
                bean.setPlateNo(json.getString("PlateNo"));
                bean.setVIN(json.getString("VinNo"));
                bean.setStatusId(json.getInt("StatusId"));
                bean.setSerailNo(json.getString("SerialNo"));
                bean.setMACAddress(json.getString("MACAddress"));
                bean.setTimeZoneId(json.getString("TimeZoneId"));
                al.add(bean);
            }

            if (al.size() > 0) {
                CarrierInfoDB.Save(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
          //  LogFile.write(GetCall.class.getName() + "::CarrierInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 14 January 2016
    // Purpose: sync user accounts with web
    public static boolean PlaceInfoSync() {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_PLACE);

            JSONArray obja = new JSONArray(result);
            ArrayList<PlaceBean> al = new ArrayList<PlaceBean>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                PlaceBean bean = new PlaceBean();
                bean.setFEATURE_ID(json.getInt("FEATURE_ID"));
                bean.setFEATURE_NAME(json.getString("FEATURE_NAME"));
                bean.setFEATURE_CLASS(json.getString("FEATURE_CLASS"));
                bean.setSTATE_ALPHA(json.getString("STATE_ALPHA"));
                bean.setSTATE_NUMERIC(json.getString("STATE_NUMERIC"));
                bean.setCOUNTY_NAME(json.getString("COUNTY_NAME"));
                bean.setCOUNTY_NUMERIC(json.getString("COUNTY_NUMERIC"));
                bean.setPRIMARY_LAT_DMS(json.getString("PRIMARY_LAT_DMS"));
                bean.setPRIM_LONG_DMS(json.getString("PRIM_LONG_DMS"));
                bean.setPRIM_LAT_DEC(json.getString("PRIM_LAT_DEC"));
                bean.setPRIM_LONG_DEC(json.getString("PRIM_LONG_DEC"));
                bean.setSOURCE_LAT_DMS(json.getString("SOURCE_LAT_DMS"));
                bean.setSOURCE_LONG_DMS(json.getString("SOURCE_LONG_DMS"));
                bean.setSOURCE_LAT_DEC(json.getString("SOURCE_LAT_DEC"));
                bean.setSOURCE_LONG_DEC(json.getString("SOURCE_LONG_DEC"));
                bean.setELEV_IN_M(json.getString("ELEV_IN_M"));
                bean.setELEV_IN_FT(json.getString("ELEV_IN_FT"));
                bean.setMAP_NAME(json.getString("MAP_NAME"));
                bean.setDATE_CREATED(Utility.ConvertFromJsonDateTime(json.getString("DATE_CREATED")));
                bean.setDATE_EDITED(Utility.ConvertFromJsonDateTime(json.getString("DATE_EDITED")));
                al.add(bean);
            }

            if (al.size() > 0) {
                PlaceInfoDB.Save(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 27 January 2016
    // Purpose: sync daily log data with web incase driver using multiple vehicle
    public static boolean LogInfoSync(String fromDate, String toDate) {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_LOG_DATA
                            + Utility.onScreenUserId + "&fromDate=" + fromDate + "&toDate=" + toDate);

            if (result == null || result.isEmpty())
                return status;
            JSONArray obja = new JSONArray(result);
            ArrayList<DailyLogBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                DailyLogBean bean = new DailyLogBean();
                bean.setOnlineDailyLogId(json.getInt("DAILYLOGID"));
                bean.setLogDate(Utility.ConvertFromJsonDate(json.getString("LOGDATE")));
                bean.setDriverId(json.getInt("DRIVERID"));
                bean.setShippingId(json.getString("SHIPPINGID"));
                bean.setTrailerId(json.getString("TRAILERID"));
                bean.setStartTime(json.getString("STARTTIME"));
                bean.setStartOdometerReading(json.getString("STARTODOMETERREADING"));
                bean.setEndOdometerReading(json.getString("ENDODOMETERREADING"));
                bean.setCertifyFG(json.getBoolean("CERTIFYFG") ? 1 : 0);
                //bean.setcertifyCount(json.getString("CERTIFYCOUNT"));
                //bean.setSignature(json.getString("SIGNATURE"));
                bean.setCreatedBy(json.getInt("LOGCREATEDBY"));
                bean.setCreatedDate(Utility.ConvertFromJsonDateTime(json.getString("LOGCREATEDDATE")));
                // bean.setModifiedBy(json.getInt("MODIFIEDBY"));
                //bean.setModifiedDate(json.getString("MODIFIEDDATE"));
                bean.setStatusId(1);
                bean.setSyncFg(1);

                Log.i("GetCall", "dailylogid=" + bean.getOnlineDailyLogId() + " / create date=" + bean.getCreatedDate());

                JSONArray jaEvent = json.getJSONArray("EventList");
                ArrayList<EventBean> arrEvent = new ArrayList<>();
                for (int j = 0; j < jaEvent.length(); j++) {
                    JSONObject joEvent = jaEvent.getJSONObject(j);
                    EventBean eventBean = new EventBean();
                    eventBean.setDriverId(joEvent.getInt("DRIVERID"));
                    eventBean.setOnlineEventId(joEvent.getInt("EVENTID"));
                    eventBean.setEventSequenceId(joEvent.getInt("EVENTSEQUENCEID"));
                    eventBean.setEventType(joEvent.getInt("EVENTTYPE"));
                    eventBean.setEventCode(joEvent.getInt("EVENTCODE"));
                    eventBean.setEventCodeDescription(joEvent.getString("EVENTCODEDESCRIPTION"));
                    eventBean.setOdometerReading(joEvent.getString("ODOMETERREADING"));
                    eventBean.setEngineHour(joEvent.getString("ENGINEHOUR"));
                    eventBean.setEventRecordOrigin(joEvent.getInt("EVENTRECORDORIGIN"));
                    eventBean.setEventRecordStatus(joEvent.getInt("EVENTRECORDSTATUS"));
                    eventBean.setEventDateTime(Utility.ConvertFromJsonDateTime(joEvent.getString("EVENTDATETIME")));
                    eventBean.setLatitude(joEvent.getString("LATITUDE"));
                    eventBean.setLongitude(joEvent.getString("LONGITUDE"));
                    eventBean.setLocationDescription(joEvent.getString("LOCATIONDESCRIPTION"));
                    eventBean.setDailyLogId(joEvent.getInt("DAILYLOGID"));
                    eventBean.setCreatedBy(joEvent.getInt("EVENTCREATEDBY"));
                    eventBean.setCreatedDate(Utility.ConvertFromJsonDateTime(joEvent.getString("EVENTCREATEDDATE")));
                    //eventBean.setModifiedBy(json.getInt("MODIFIEDBY"));
                    //eventBean.setModifiedDate(json.getString("MODIFIEDDATE"));
                    eventBean.setVehicleId(joEvent.getInt("VEHICLEID"));
                    eventBean.setStatusId(1);
                    eventBean.setSyncFg(1);

                    eventBean.setCheckSumWeb(joEvent.getString("CheckSum"));
                    eventBean.setDistanceSinceLastValidCoordinate(joEvent.getString("DistanceSinceLastValidCoordinate"));
                    eventBean.setMalfunctionIndicatorFg(joEvent.getBoolean("MalfunctionIndicatorFg") ? 1 : 0);
                    eventBean.setDataDiagnosticIndicatorFg(joEvent.getBoolean("DataDiagnosticIndicatorFg") ? 1 : 0);
                    eventBean.setDiagnosticCode(joEvent.getString("DiagnosticCode"));
                    eventBean.setAccumulatedVehicleMiles(joEvent.getString("AccumulatedVehicleMiles"));
                    eventBean.setElaspsedEngineHour(joEvent.getString("ElaspsedEngineHour"));
                    eventBean.setMotorCarrier(joEvent.getString("MotorCarrier"));
                    eventBean.setShippingDocumentNo(joEvent.getString("ShippingDocumentNo"));
                    eventBean.setTrailerNo(joEvent.getString("TrailerNo"));
                    eventBean.setTimeZoneOffsetUTC(joEvent.getString("TimeZoneOffsetUTC"));
                    eventBean.setCoDriverId(joEvent.getInt("CoDriverId"));

                    arrEvent.add(eventBean);
                }
                bean.setEventList(arrEvent);
                al.add(bean);
            }

            if (al.size() > 0) {
                DailyLogDB.DailyLogSave(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            Log.i("GetCall", "Err:" + e.getMessage());
         //   LogFile.write(GetCall.class.getName() + "::LogInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 27 January 2016
    // Purpose: sync daily log data with web if editted in web
    public static boolean EditRequestSync() {
        boolean status = true;
        WebService ws = new WebService();
        String url = WebUrl.GET_ASSIGNED_EVENT + Utility.onScreenUserId + "&EventRecordOrigin=3&EventRecordStatus=3";
        try {
            String result = ws
                    .doGet(url);
            if (result == null || result.isEmpty())
                return status;
            JSONArray obja = new JSONArray(result);
            ArrayList<DailyLogBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                DailyLogBean bean = new DailyLogBean();
                bean.setOnlineDailyLogId(json.getInt("DAILYLOGID"));
                bean.setLogDate(Utility.ConvertFromJsonDate(json.getString("LOGDATE")));
                bean.setDriverId(json.getInt("DRIVERID"));
                bean.setShippingId(json.getString("SHIPPINGID"));
                bean.setTrailerId(json.getString("TRAILERID"));
                bean.setStartTime(json.getString("STARTTIME"));
                bean.setStartOdometerReading(json.getString("STARTODOMETERREADING"));
                bean.setEndOdometerReading(json.getString("ENDODOMETERREADING"));
                bean.setCertifyFG(json.getBoolean("CERTIFYFG") ? 1 : 0);
                //bean.setcertifyCount(json.getString("CERTIFYCOUNT"));
                //bean.setSignature(json.getString("SIGNATURE"));
                bean.setCreatedBy(json.getInt("LOGCREATEDBY"));
                bean.setCreatedDate(Utility.ConvertFromJsonDateTime(json.getString("LOGCREATEDDATE")));
                //bean.setModifiedBy(json.getInt("MODIFIEDBY"));
                //bean.setModifiedDate(json.getString("MODIFIEDDATE"));
                bean.setStatusId(1);
                bean.setSyncFg(1);

                JSONArray jaEvent = json.getJSONArray("EventList");
                ArrayList<EventBean> arrEvent = new ArrayList<>();
                for (int j = 0; j < jaEvent.length(); j++) {
                    JSONObject joEvent = jaEvent.getJSONObject(j);
                    EventBean eventBean = new EventBean();
                    eventBean.setDriverId(bean.getDriverId());
                    eventBean.setOnlineEventId(joEvent.getInt("EVENTID"));
                    eventBean.setEventSequenceId(joEvent.getInt("EVENTSEQUENCEID"));
                    eventBean.setEventType(joEvent.getInt("EVENTTYPE"));
                    eventBean.setEventCode(joEvent.getInt("EVENTCODE"));
                    eventBean.setEventCodeDescription(joEvent.getString("EVENTCODEDESCRIPTION"));
                    eventBean.setOdometerReading(joEvent.getString("ODOMETERREADING"));
                    eventBean.setEngineHour(joEvent.getString("ENGINEHOUR"));
                    eventBean.setEventRecordOrigin(joEvent.getInt("EVENTRECORDORIGIN"));
                    eventBean.setEventRecordStatus(joEvent.getInt("EVENTRECORDSTATUS"));
                    eventBean.setEventDateTime(Utility.ConvertFromJsonDateTime(joEvent.getString("EVENTDATETIME")));
                    eventBean.setLatitude(joEvent.getString("LATITUDE"));
                    eventBean.setLongitude(joEvent.getString("LONGITUDE"));
                    eventBean.setLocationDescription(joEvent.getString("LOCATIONDESCRIPTION"));
                    eventBean.setDailyLogId(joEvent.getInt("DAILYLOGID"));
                    eventBean.setCreatedBy(joEvent.getInt("EVENTCREATEDBY"));
                    eventBean.setCreatedDate(Utility.ConvertFromJsonDateTime(joEvent.getString("EVENTCREATEDDATE")));
                    eventBean.setVehicleId(joEvent.getInt("VEHICLEID"));
                    eventBean.setCoDriverId(joEvent.getInt("CoDriverId"));
                    //eventBean.setModifiedBy(json.getInt("MODIFIEDBY"));
                    //eventBean.setModifiedDate(json.getString("MODIFIEDDATE"));
                    eventBean.setStatusId(1);
                    eventBean.setSyncFg(1);

                    eventBean.setCheckSumWeb(joEvent.getString("CheckSum"));
                    eventBean.setDistanceSinceLastValidCoordinate(joEvent.getString("DistanceSinceLastValidCoordinate"));
                    eventBean.setMalfunctionIndicatorFg(joEvent.getBoolean("MalfunctionIndicatorFg") ? 1 : 0);
                    eventBean.setDataDiagnosticIndicatorFg(joEvent.getBoolean("DataDiagnosticIndicatorFg") ? 1 : 0);
                    eventBean.setDiagnosticCode(joEvent.getString("DiagnosticCode"));
                    eventBean.setAccumulatedVehicleMiles(joEvent.getString("AccumulatedVehicleMiles"));
                    eventBean.setElaspsedEngineHour(joEvent.getString("ElaspsedEngineHour"));
                    eventBean.setMotorCarrier(joEvent.getString("MotorCarrier"));
                    eventBean.setShippingDocumentNo(joEvent.getString("ShippingDocumentNo"));
                    eventBean.setTrailerNo(joEvent.getString("TrailerNo"));
                    eventBean.setTimeZoneOffsetUTC(joEvent.getString("TimeZoneOffsetUTC"));

                    arrEvent.add(eventBean);
                }
                bean.setEventList(arrEvent);
                al.add(bean);
            }

            if (al.size() > 0) {
                DailyLogDB.DailyLogSave(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
           // LogFile.write(GetCall.class.getName() + "::EditRequestSync Error " + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 27 January 2016
    // Purpose: sync daily log data with web if editted in web
    public static boolean MessageSync() {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_MESSAGE
                            + Utility.onScreenUserId);
            if (result == null || result.isEmpty())
                return status;
            JSONArray obja = new JSONArray(result);
            ArrayList<MessageBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                MessageBean bean = new MessageBean();
                bean.setMessage(json.getString("Message"));
                bean.setCreatedById(json.getInt("CreatedById"));
                bean.setMessageToId(json.getInt("MessageToId"));
                bean.setMessageDate(json.getString("MessageDate"));
                bean.setDeliveredFg(0);
                bean.setReadFg(json.getInt("ReadFg"));
                bean.setSendFg(0);
                bean.setSyncFg(1);
                al.add(bean);
            }

            if (al.size() > 0) {
                MessageDB.Save(al);
            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
           // LogFile.write(GetCall.class.getName() + "::MessageSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }


    //call web service to check if update is existed in server
    public static boolean checkUpdate(String currentVersion) {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws.doGet(WebUrl.GET_UPDATE + currentVersion);

            if (result == null || result.isEmpty())
                return false;
            JSONObject json = new JSONObject(result);

            VersionInformationBean bean = new VersionInformationBean();
            bean.setAutoDownloadFg(json.getBoolean("AutoDownloadFg"));
            bean.setAutoUpdateFg(json.getBoolean("AutoUpdateFg"));
            bean.setCurrentVersion(json.getString("CurrentVersion"));
            bean.setDownloadDate(json.getString("DownloadDate"));
            bean.setDownloadFg(json.getBoolean("DownloadFg"));
            bean.setLiveFg(json.getBoolean("LiveFg"));
            bean.setPreviousVersion(json.getString("PreviousVersion"));
            bean.setSerialNo(json.getString("SerialNo"));
            bean.setUpdateArchiveName(json.getString("UpdateArchiveName"));
            bean.setUpdateDate(json.getString("UpdateDate"));
            bean.setUpdateUrl(json.getString("UpdateUrl"));
            bean.setUpdatedFg(json.getBoolean("UpdatedFg"));
            bean.setVersionDate(json.getString("VersionDate"));

            VersionInformationDB.Save(bean);
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            //LogFile.write(GetCall.class.getName() + "::AccountSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 05 August 2016
    // Purpose: sync daily log data with web in case driver using multiple vehicle
    public static boolean LogEventSync(String lastEventDate) {
        boolean status = true;
        WebService ws = new WebService();
        try {
            String result = ws
                    .doGet(WebUrl.GET_LOG_EVENT_DATA
                            + Utility.onScreenUserId + "&lastEventDate=" + lastEventDate.replaceAll ( " ", "%20" ));

            if (result == null || result.isEmpty())
                return false;
            JSONArray obja = new JSONArray(result);
            ArrayList<DailyLogBean> al = new ArrayList<>();
            for (int i = 0; i < obja.length(); i++) {
                JSONObject json = obja.getJSONObject(i);
                DailyLogBean bean = new DailyLogBean();
                bean.setOnlineDailyLogId(json.getInt("DAILYLOGID"));
                bean.setLogDate(Utility.ConvertFromJsonDate(json.getString("LOGDATE")));
                bean.setDriverId(json.getInt("DRIVERID"));
                bean.setShippingId(json.getString("SHIPPINGID"));
                bean.setTrailerId(json.getString("TRAILERID"));
                bean.setStartTime(json.getString("STARTTIME"));
                bean.setStartOdometerReading(json.getString("STARTODOMETERREADING"));
                bean.setEndOdometerReading(json.getString("ENDODOMETERREADING"));
                bean.setCertifyFG(json.getBoolean("CERTIFYFG") ? 1 : 0);
                //bean.setcertifyCount(json.getString("CERTIFYCOUNT"));
                //bean.setSignature(json.getString("SIGNATURE"));
                bean.setCreatedBy(json.getInt("LOGCREATEDBY"));
                bean.setCreatedDate(Utility.ConvertFromJsonDateTime(json.getString("LOGCREATEDDATE")));
                // bean.setModifiedBy(json.getInt("MODIFIEDBY"));
                //bean.setModifiedDate(json.getString("MODIFIEDDATE"));
                bean.setStatusId(1);
                bean.setSyncFg(1);

                Log.i("GetCall", "dailylogid=" + bean.getOnlineDailyLogId() + " / create date=" + bean.getCreatedDate());

                JSONArray jaEvent = json.getJSONArray("EventList");
                ArrayList<EventBean> arrEvent = new ArrayList<>();
                for (int j = 0; j < jaEvent.length(); j++) {
                    JSONObject joEvent = jaEvent.getJSONObject(j);
                    EventBean eventBean = new EventBean();
                    eventBean.setDriverId(joEvent.getInt("DRIVERID"));
                    eventBean.setOnlineEventId(joEvent.getInt("EVENTID"));
                    eventBean.setEventSequenceId(joEvent.getInt("EVENTSEQUENCEID"));
                    eventBean.setEventType(joEvent.getInt("EVENTTYPE"));
                    eventBean.setEventCode(joEvent.getInt("EVENTCODE"));
                    eventBean.setEventCodeDescription(joEvent.getString("EVENTCODEDESCRIPTION"));
                    eventBean.setOdometerReading(joEvent.getString("ODOMETERREADING"));
                    eventBean.setEngineHour(joEvent.getString("ENGINEHOUR"));
                    eventBean.setEventRecordOrigin(joEvent.getInt("EVENTRECORDORIGIN"));
                    eventBean.setEventRecordStatus(joEvent.getInt("EVENTRECORDSTATUS"));
                    eventBean.setEventDateTime(Utility.ConvertFromJsonDateTime(joEvent.getString("EVENTDATETIME")));
                    eventBean.setLatitude(joEvent.getString("LATITUDE"));
                    eventBean.setLongitude(joEvent.getString("LONGITUDE"));
                    eventBean.setLocationDescription(joEvent.getString("LOCATIONDESCRIPTION"));
                    eventBean.setDailyLogId(joEvent.getInt("DAILYLOGID"));
                    eventBean.setCreatedBy(joEvent.getInt("EVENTCREATEDBY"));
                    eventBean.setCreatedDate(Utility.ConvertFromJsonDateTime(joEvent.getString("EVENTCREATEDDATE")));
                    //eventBean.setModifiedBy(json.getInt("MODIFIEDBY"));
                    //eventBean.setModifiedDate(json.getString("MODIFIEDDATE"));
                    eventBean.setVehicleId(joEvent.getInt("VEHICLEID"));
                    eventBean.setStatusId(1);
                    eventBean.setSyncFg(1);

                    eventBean.setCheckSumWeb(joEvent.getString("CheckSum"));
                    eventBean.setDistanceSinceLastValidCoordinate(joEvent.getString("DistanceSinceLastValidCoordinate"));
                    eventBean.setMalfunctionIndicatorFg(joEvent.getBoolean("MalfunctionIndicatorFg") ? 1 : 0);
                    eventBean.setDataDiagnosticIndicatorFg(joEvent.getBoolean("DataDiagnosticIndicatorFg") ? 1 : 0);
                    eventBean.setDiagnosticCode(joEvent.getString("DiagnosticCode"));
                    eventBean.setAccumulatedVehicleMiles(joEvent.getString("AccumulatedVehicleMiles"));
                    eventBean.setElaspsedEngineHour(joEvent.getString("ElaspsedEngineHour"));
                    eventBean.setMotorCarrier(joEvent.getString("MotorCarrier"));
                    eventBean.setShippingDocumentNo(joEvent.getString("ShippingDocumentNo"));
                    eventBean.setTrailerNo(joEvent.getString("TrailerNo"));
                    eventBean.setTimeZoneOffsetUTC(joEvent.getString("TimeZoneOffsetUTC"));
                    eventBean.setCoDriverId(joEvent.getInt("CoDriverId"));

                    arrEvent.add(eventBean);
                }
                bean.setEventList(arrEvent);
                al.add(bean);
            }

            if (al.size() > 0) {
                DailyLogDB.DailyLogSave(al);
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
            Log.i("GetCall", "Err:" + e.getMessage());
          //  LogFile.write(GetCall.class.getName() + "::LogInfoSync Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return status;
    }


}