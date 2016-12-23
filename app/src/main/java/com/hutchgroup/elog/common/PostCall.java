package com.hutchgroup.elog.common;

import com.hutchgroup.elog.db.AlertDB;
import com.hutchgroup.elog.db.DTCDB;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.TpmsDB;
import com.hutchgroup.elog.db.TripInspectionDB;
import com.hutchgroup.elog.db.UserDB;

import org.json.JSONObject;

/**
 * Created by Deepak.Sharma on 1/27/2016.
 */
public class PostCall {

    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: get log data to post
    public static boolean PostAll() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            JSONObject obj = new JSONObject();
            // get daily log data with events and rules w.r.t daily log
            obj.put("logData", DailyLogDB.getLogDataSync());
            // get codriver data
            obj.put("coDriverList", DailyLogDB.getCoDriverSync());
            //124 LogFile.write(PostCall.class.getName() + "::PostAll:\n" + obj.toString(), LogFile.AUTOMATICALLY_TASK, LogFile.AUTOSYNC_LOG);
            // post record to server
            String result = ws.doPost(
                    WebUrl.POST_ALL,
                    obj.toString());
            if (result != null) {
                // update syncFG column for the posted record
                DailyLogDB.UpdateSyncStatusAll();

            }
        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST EVENT SYSNC
    public static boolean POSTEVENTSYNC(int eventRecordStatus, int eventRecordOrigin) {
        boolean status = true;
        WebService ws = new WebService();

        try {
            JSONObject obj = new JSONObject();
            obj.put("DriverId", Utility.onScreenUserId);
            obj.put("EVENTRECORDORIGIN", eventRecordOrigin);
            obj.put("EVENTRECORDSTATUS", eventRecordStatus);

            ws.doPost(
                    WebUrl.POST_EVENT_SYNC,
                    obj.toString());

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST EVENT SYSNC
    public static boolean POSTMessageSYNC() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            JSONObject obj = new JSONObject();
            obj.put("DriverId", Utility.onScreenUserId);

            ws.doPost(
                    WebUrl.POST_MESSAGE_SYNC,
                    obj.toString());

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST EVENT SYSNC
    public static boolean PostAccount() {
        boolean status = true;
        WebService ws = new WebService();

        try {

            String data = UserDB.accountSyncGet();

            if (data.equals(""))
                return status;
            String result = ws.doPost(
                    WebUrl.POST_ACCOUNT,
                    data);
            if (result != null) {
                UserDB.accountSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST EVENT SYSNC
    public static boolean PostDVIR() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = TripInspectionDB.getDVIRSync().toString();

            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_DVIR,
                    data);
            if (result != null) {
                TripInspectionDB.DVIRSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }

    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST DTC SYNC
    public static boolean PostDTC() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = DTCDB.getDTCCodeSync().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_DTC,
                    data);
            if (result != null) {
                DTCDB.DTCSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST DTC SYNC
    public static boolean PostAlert() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = AlertDB.getAlertSync().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_ALERT,
                    data);
            if (result != null) {
                AlertDB.AlertSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }


    // Created By: Deepak Sharma
    // Created Date: 29 January 2016
    // Purpose: POST DTC SYNC
    public static boolean PostTPMS() {
        boolean status = true;
        WebService ws = new WebService();

        try {
            String data = TpmsDB.getTPMSDataSync().toString();
            if (data.equals("[]")) {
                return status;
            }

            String result = ws.doPost(
                    WebUrl.POST_TPMS,
                    data);
            if (result != null) {
                TpmsDB.TpmsSyncUpdate();
            }

        } catch (Exception e) {
            status = false;
            Utility.printError(e.getMessage());
        }
        return status;
    }
}
