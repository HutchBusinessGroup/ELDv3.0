package com.hutchgroup.elog.common;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.Settings;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.hutchgroup.elog.ELogApplication;
import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.beans.DTCBean;
import com.hutchgroup.elog.beans.DutyStatusBean;
import com.hutchgroup.elog.beans.LocationBean;
import com.hutchgroup.elog.beans.SettingsBean;
import com.hutchgroup.elog.beans.TimeZoneBean;
import com.hutchgroup.elog.beans.UserBean;
import com.hutchgroup.elog.db.UserDB;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.security.MessageDigest;
import java.util.TimeZone;

public class Utility implements ActivityCompat.OnRequestPermissionsResultCallback {
    public static String LastEventDate;
    public static String _productCode = "12";
    public static SettingsBean _appSetting = new SettingsBean();
    public static AlarmSetter as;
    public static String ServerIp = "207.194.137.58";
    public static int Port = 31004;
    public static int NO_OPTIONS = 0;
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public final static String DIALOGBOX_TITLE = "E-Log";
    public static final int DIALOGBOX_ICON = R.drawable.ic_launcher; //android.R.drawable.sym_def_app_icon;
    public static String IMEI = "";
    public static String errorMessage = "";
    public static Context context;

    public static UserBean user1 = new UserBean();
    public static UserBean user2 = new UserBean();
    public static int onScreenUserId = 0, vehicleId = 0, companyId = 0, unIdentifiedDriverId = 0, multiDayBasisUsed = 8, activeUserId = 0;
    public static ArrayList<String> onlineUserList = new ArrayList<>();
    public static boolean malFunctionIndicatorFg, dataDiagnosticIndicatorFg;
    public static String CarrierName = "", ELDManufacturer = "", USDOT = "", UnitNo = "", VIN = "", TimeZoneOffsetUTC = "08", ShippingNumber = "", TrailerNumber = "", MACAddress = "", PlateNo = "", TimeZoneId;
    public static int TimeZoneOffset = 0;

    public static String OdometerReadingSincePowerOn = "0", EngineHourSincePowerOn = "0", DiagnosticCode = "";

    public static int UnidentifiedDrivingTime = 0;
    public static int DrivingTime = 0;
    public static boolean bEventUnidentifiedDriverDiagnostic = false;
    public static int DataTransferDiagnosticCount = 0;
    public static int DataTransferMalfunctionCheckingNumber = 0;

    public static LocationBean currentLocation = new LocationBean();
    public static boolean motionFg = false;

    public static int statusFlag = 0; //0 : none, 1: PU, 2: YM
    public static int powerOnOff = -1; //0: off, 1: on

    public static int BatteryLevel = 100;

    private static AlertDialog alertDialog = null;

    public static ArrayList<DutyStatusBean> dutyStatusList;
    private static final int READ_PHONE_STATE = 1, ACCESS_FINE_LOCATION = 2, INTERNET = 3, PERMISSION_ALL = 999;
    public static final String RegistrationId = "0004";
    public static final String EldIdentifier = "held16";
    public static String ApplicationVersion = "-";
    public static boolean NightModeFg = false;
    public static boolean InspectorModeFg = false;
    public static int LogId = 0;
    public static ArrayList<DTCBean> dtcList = new ArrayList<>();

    public static boolean hasPermissions(String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }

        return true;
    }


    public static void checkAndGrantPermissions() {

        String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.RECEIVE_BOOT_COMPLETED, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS};
        if (!hasPermissions(PERMISSIONS)) {
            ActivityCompat.requestPermissions((Activity) context, PERMISSIONS, PERMISSION_ALL);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }

    public static void IMEIGet(Activity activity) {

        if (checkGrantPermission(Manifest.permission.READ_PHONE_STATE, READ_PHONE_STATE)) {
            IMEI = ((TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        }
        //IMEI = "355458096931911";
        //IMEI = "351962070020736";
        //IMEI = "356252071014150";
    }

    public static void VersionGet(Activity activity) {
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            Utility.ApplicationVersion = pInfo.versionName;
        } catch (Exception e) {
            Utility.ApplicationVersion = "";
        }

    }

    public static boolean checkGrantPermission(String permission, int responseCode) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, responseCode);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case READ_PHONE_STATE:
                    IMEI = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                    break;
                case ACCESS_FINE_LOCATION:
                    ELogApplication.objGps.startUsingGPS(Utility.context);
                    break;
                case PERMISSION_ALL:
                    break;

            }
        }
    }

    public static void showMsg(String msg) {

        Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        t.show();
    }

    public static void showAlertMsg(String msg) {
        if (alertDialog != null && alertDialog.isShowing()) {
            return;
        }
        try {
            alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setTitle(DIALOGBOX_TITLE);
            alertDialog.setIcon(DIALOGBOX_ICON);
            alertDialog.setMessage(msg);
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.cancel();
                        }
                    });
            alertDialog.show();
            //return alertDialog;
        } catch (Exception ex) {
            LogFile.write("Show Alert Msg: " + ex.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }

    }


    public static void showAlertMsg(String msg, Context ctx) {
        if (alertDialog != null && alertDialog.isShowing()) {
            return;
        }
        try {
            alertDialog = new AlertDialog.Builder(ctx).create();
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setTitle(DIALOGBOX_TITLE);
            alertDialog.setIcon(DIALOGBOX_ICON);
            alertDialog.setMessage(msg);
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.cancel();
                        }
                    });
            alertDialog.show();
            //return alertDialog;
        } catch (Exception ex) {
            LogFile.write("Show Alert Msg: " + ex.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }

    }

    public static boolean isInternetOn() {
        try {

            NetworkInfo localNetworkInfo = ((ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();

            if (localNetworkInfo != null) {
                //if (localNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

                return localNetworkInfo.isConnected();
                //}
            }
        } catch (Exception exe) {
            errorMessage = exe.getMessage();
        }

        //  return true;
        return false;
    }

    // Created By: Deepak Sharma
    // Created Date: 21 Nov 2016 4:31 PM
    // get current system date time and format it using User TimeZoneId
    public static String getCurrentDateTime() {
        Date d = new Date();
        return sdf.format(d);
    }

    public static Date newDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
        return calendar.getTime();
    }

    static Date parse(String dateTime) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
        } catch (ParseException pe) {
            return new Date();
        }
    }

    static Date parse(String dateTime, String format) {
        try {
            return new SimpleDateFormat(format).parse(dateTime);
        } catch (ParseException pe) {
            return new Date();
        }
    }

    static String format(Date date) {
        return sdf.format(date);
    }

    static String format(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    static String format(String dateTime, String format) {

        Date date = parse(dateTime);
        return format(date, format);
    }

    public static String getTime(String dateTime) {
        return format(dateTime, "HH:mm:ss");
    }

    public static String getStringTime(String dateTime) {
        return format(dateTime, "HHmmss");
    }

    public static String getTimeHHMM(String dateTime) {
        return format(dateTime, "hh:mm a");
    }

    public static float getDiffMins(String dateTime1, String dateTime2) {
        float mins = 0f;
        try {
            Date d1 = parse(dateTime1);
            Date d2 = parse(dateTime2);

            long millis = d2.getTime() - d1.getTime();
            mins = (1.0f * millis) / (1000f * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //int mins = millis % (1000*60*60);
        return mins;
    }

    public static float getDiffTime(String dateTime1, String dateTime2) {
        float hours = 0f;
        try {
            Date d1 = parse(dateTime1);
            Date d2 = parse(dateTime2);

            long millis = d2.getTime() - d1.getTime();
            hours = (1.0f * millis) / (1000f * 60 * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //int mins = millis % (1000*60*60);
        return hours;
    }

    public static int getDiffDay(String dateTime1, String dateTime2) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        //float hours = 0f;
        float days = 0;
        try {
            Date d1 = sf.parse(dateTime1);
            Date d2 = sf.parse(dateTime2);

            long millis = d2.getTime() - d1.getTime();
            days = (1.0f * millis) / (1000f * 60 * 60 * 24);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //int mins = millis % (1000*60*60);
        return (int) days;
    }

    public static String getTimeByFormat(String dateTime) {
        String str = "";
        try {
            String format = "hh:mm:ss a"; //12hr
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                format = "HH:mm:ss";
            }
            str = format(dateTime, format);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getDateTimeForServer(String date) {
        String str = "";
        try {
            Date d = parse(date);

            int offset = Utility.TimeZoneOffset;

            String gmtTZ = String.format("%s%02d%02d", offset < 0 ? "-" : "+",
                    Math.abs(offset) / 3600000, Math.abs(offset) / 60000 % 60);
            str = "/Date(" + d.getTime() + "" + gmtTZ + ")/";
            System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getDateForServer(String date) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String str = "";
        try {

            Date d = parse(date);
            int offset = Utility.TimeZoneOffset;

            String gmtTZ = String.format("%s%02d%02d", offset < 0 ? "-" : "+",
                    Math.abs(offset) / 3600000, Math.abs(offset) / 60000 % 60);
            str = "/Date(" + d.getTime() + "" + gmtTZ + ")/";
            System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String getCurrentUTCDateTime() {

        SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdfUTC.format(new Date());

        return utcTime;
    }


    public static String convertUTCToLocalDateTime(String date) {

        try {
            SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdfLocal.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date utcDate = sdfLocal.parse(date);

            final String utcTime = sdf.format(utcDate);
            return utcTime;
        } catch (Exception e) {
        }
        return date;
    }

    public static String getStringCurrentDate() {
        String dateTime = getCurrentDateTime();
        return format(dateTime, "MMM dd,yyyy");

    }

    public static String getCurrentDate() {
        String dateTime = getCurrentDateTime();
        return format(dateTime, "yyyy-MM-dd");
    }

    public static String getCurrentTime() {
        String dateTime = getCurrentDateTime();
        return format(dateTime, "HH:mm:ss");
    }


    public static String getDate(String dateTime) {
        return format(dateTime, "yyyy-MM-dd");
    }

    public static Date getDate(Date date, int numGapDays) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(sdf.format(date)));
            c.add(Calendar.DATE, numGapDays);
            c.setTime(sdf.parse(sdf.format(c.getTime())));
            return c.getTime();
        } catch (Exception e) {

        }
        return null;
    }


    public static String getDateFromString(String date, int numGapDays) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(date));
            c.add(Calendar.DATE, numGapDays);
            c.setTime(sdf.parse(sdf.format(c.getTime())));
            return sdf.format(c.getTime());
        } catch (Exception e) {

        }
        return null;
    }

    // Created By: Deepak Sharma
    // Created Date: 18 July 2016
    // get date time after adding gap of given days
    public static String getDateTime(String date, int numGapDays) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdfDate.parse(date));
            c.add(Calendar.DATE, numGapDays);
            return sdf.format(c.getTime());
        } catch (Exception e) {

        }
        return null;
    }

    public static String addDate(String date, int timeFrequency) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(date));
            c.add(Calendar.DATE, timeFrequency);
            String duedate = sdf.format(c.getTime());
            return duedate;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public static Date addDays(Date date, int timeFrequency) {

        try {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DATE, timeFrequency);
            return c.getTime();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public static String getPreviousDate(int timeFrequency) {

        try {
            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
            c.add(Calendar.DATE, timeFrequency);
            return sdf.format(c.getTime());
        } catch (Exception e) {
            return getCurrentDateTime();
        }
    }

    public static String getPreviousDateOnly(int timeFrequency) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
            c.add(Calendar.DATE, timeFrequency);
            return format(c.getTime(), "yyyy-MM-dd");
        } catch (Exception e) {
            return getCurrentDateTime();
        }
    }

    public static Date dateOnlyGet(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(sdf.format(date)));
            return c.getTime();
        } catch (Exception e) {
        }
        return null;
    }

    public static Date dateOnlyGet(String date) {
        return parse(date, "yyyy-MM-dd");
    }

    public static String dateOnlyStringGet(String date) {
        return format(date, "yyyy-MM-dd");
    }

    public static String parseDate(String date) {
        return format(date, "yyyy-MM-dd");
    }

    public static String timeOnlyGet(String date) {
        try {
            return date.substring(11, 16);
        } catch (Exception e) {
            String message = e.getMessage();
        }
        return date;
    }

    public static Date addMinutes(Date date, int timeFrequency) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.MINUTE, timeFrequency);
            return c.getTime();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public static Date addSeconds(Date date, int timeFrequency) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.SECOND, timeFrequency);
            return c.getTime();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public static String ConverDateFormat(Date date) {
        return format(date, "MMM dd,yyyy hh:mm a");
    }

    public static String ConverDateFormat(String date) {
        return format(date, "MMM dd,yyyy hh:mm a");
    }

    public static String getTimeFromMinute(int totalMinutes) {
        String hours = Integer.toString(totalMinutes / 60);
        hours = hours.length() == 1 ? "0" + hours : hours;
        String minutes = Integer.toString(totalMinutes % 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;
        return hours + ":" + minutes;
    }

    public static String getTimeFromSeconds(int totalSeconds) {
        String hours = Integer.toString(totalSeconds / 3600);
        hours = hours.length() == 1 ? "0" + hours : hours;
        String minutes = Integer.toString((totalSeconds % 3600) / 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;

        String seconds = Integer.toString(totalSeconds % 60);
        seconds = seconds.length() == 1 ? "0" + seconds : seconds;
        return hours + ":" + minutes + ":" + seconds;
    }


    public static String getTimeInMinuteFromSeconds(int totalSeconds) {

        String minutes = Integer.toString(totalSeconds / 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;

        String seconds = Integer.toString(totalSeconds % 60);
        seconds = seconds.length() == 1 ? "0" + seconds : seconds;
        return minutes + ":" + seconds;
    }

    public static boolean isAlpha(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!((c >= 'a' && c <= 'z') || c == ' ')) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmail(String str) {
        if (str.indexOf("@") > 0 && str.indexOf(".") > 0) {
            return true;
        }
        return false;
    }

    public static boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!(c >= '0' && c <= '9')) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasSpace(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {
                return true;
            }
        }
        return false;
    }

    public static void printError(String message) {
        if (message == null) {
            message = "An unexpected error occurred. Please contact admin!";
        }
        errorMessage = message;
        System.out.println("errorLog: " + message);
    }

    // Created By: Deepak Sharma
    // Created Date: 22 April 2015
    // Purpose: show error message
    public static AlertDialog showErrorMessage(Context ctx) {

        final AlertDialog ad = new AlertDialog.Builder(ctx).create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(DIALOGBOX_TITLE);
        ad.setIcon(DIALOGBOX_ICON);
        ad.setMessage(errorMessage);
        ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ad.cancel();
                    }
                });
        ad.show();
        return ad;

    }

    public static String ConvertFromJsonDateTime(String date) {

        String splitChar = date.contains("-") ? "-" : "+";
        Calendar calendar = Calendar.getInstance();
        String datereip = date.replace("/Date(", "").replace(")/", "").split(splitChar)[0];
        Long timeInMillis = Long.valueOf(datereip);
        calendar.setTimeInMillis(timeInMillis);

        SimpleDateFormat sdfPacific = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfPacific.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        String pstTime = sdfPacific.format(calendar.getTime());
        return pstTime;
    }

    public static String ConvertFromJsonDate(String date) {

        String splitChar = date.contains("-") ? "-" : "+";
        Calendar calendar = Calendar.getInstance();
        String datereip = date.replace("/Date(", "").replace(")/", "").split(splitChar)[0];
        Long timeInMillis = Long.valueOf(datereip);
        calendar.setTimeInMillis(timeInMillis);

        SimpleDateFormat sdfPacific = new SimpleDateFormat("yyyy-MM-dd");
        sdfPacific.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        String pstTime = sdfPacific.format(calendar.getTime());
        return pstTime;
    }

    private static String convertToHex(byte[] data) throws java.io.IOException {


        StringBuffer sb = new StringBuffer();
        String hex = null;

        hex = Base64.encodeToString(data, 0, data.length, NO_OPTIONS);

        sb.append(hex);

        return sb.toString();
    }


    // Created By: Deepak Sharma
    // Created Date: 21 July 2016
    // Purpose: convert binary to hex
    public static String convertBinaryToHex(String val) {
        int decimal = Integer.parseInt(val, 2);
        return Integer.toString(decimal, 16).toUpperCase();
    }

    public static String computeSHAHash(String password, String salt) {
        password = password.concat(salt);
        byte[] key = password.getBytes();
        MessageDigest mdSha1 = null;
        try {
            mdSha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e1) {

        }
        String result = "";
        byte[] hash = mdSha1.digest(key);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) hex = "0" + hex;
            hex = hex.substring(hex.length() - 2);
            result += hex;
        }

        return result.toUpperCase();
    }

    public static LocationBean getLocation() {
        LocationBean bean = new LocationBean();

        return bean;
    }

    public static String GetStringDate(Date date) {
        return format(date, "MMM dd,yyyy");
    }

    // Created By: Deepak Sharma
    // Created Date: 12 September 2016
    // Purpose: convert to specified date format
    public static String convertDate(Date date, String f) {
        return format(date, f);
    }


    // Created By: Deepak Sharma
    // Created Date: 12 September 2016
    // Purpose: convert to specified date format
    public static String convertDate(String date, String f) {
        return format(date, f);
    }

    public static String GetString(Date date) {

        return format(date, "yyyy-MM-dd");
    }

    public static float gigabytesAvailable(File f) {
        StatFs stat = new StatFs(f.getPath());
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        ;
        return bytesAvailable / (1024.f * 1024.f * 1024.f);
    }

    // save the diagnostic time of last diagnostic event occurrence
    public static void saveDiagnosticTime(boolean diagnosticFg) {
        SharedPreferences.Editor e = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE)).edit();
        e.putBoolean("diagnostic_engine_error", diagnosticFg);
        e.putLong("diagnostic_time", CanMessages.diagnosticEngineSynchronizationTime);
        e.commit();
    }

    // save odometer reading and engine hours
    public static void saveVehicleCanInfo() {
        SharedPreferences.Editor e = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE)).edit();
        e.putString("odometer", CanMessages.OdometerReading);
        e.putString("engine_hours", CanMessages.EngineHours);
        e.commit();
    }


    // save odometer reading and engine hours
    public static void saveLoginInfo(int driverId, int coDriverId, int activeUserId, int onScreenUserId) {
        SharedPreferences.Editor e = (context.getSharedPreferences("HutchGroup", context.MODE_PRIVATE)).edit();
        e.putInt("driverid", driverId);
        e.putInt("codriverid", coDriverId);
        e.putInt("activeuserid", activeUserId);
        e.putInt("onscreenuserid", onScreenUserId);
        e.commit();
    }


    public static void hideKeyboard(Activity activity, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isLargeScreen(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    // Created By: Deepak Sharma
    // Created Date: 17 June 2016
    // Purpose: populate Time Zone
    public static ArrayList<TimeZoneBean> populateTimeZone() {
        return ZoneList.getZones();
    }

    @Deprecated
    // Created By: Deepak Sharma
    // Created Date: 17 June 2016
    // Purpose: populate Time Zone
    public static ArrayList<TimeZoneBean> populateTimeZone1() {

        ArrayList<TimeZoneBean> list = new ArrayList<>();
        TimeZoneBean bean = new TimeZoneBean();
        bean.setTimeZoneName("Hawaii Time");
        bean.setTimeZoneValue("UTC-10:00");
        bean.setTimeZoneOffset(-10f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("Alaska Time");
        bean.setTimeZoneValue("UTC-09:00");
        bean.setTimeZoneOffset(-9f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("Pacific Time");
        bean.setTimeZoneValue("UTC-08:00");
        bean.setTimeZoneOffset(-8f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("Mountain Time");
        bean.setTimeZoneValue("UTC-07:00");
        bean.setTimeZoneOffset(-7f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("Central Time");
        bean.setTimeZoneValue("UTC-06:00");
        bean.setTimeZoneOffset(-6f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("Eastern Time");
        bean.setTimeZoneValue("UTC-05:00");
        bean.setTimeZoneOffset(-5f);
        list.add(bean);


        bean = new TimeZoneBean();
        bean.setTimeZoneName("Alantic Time");
        bean.setTimeZoneValue("UTC-04:00");
        bean.setTimeZoneOffset(-4f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("West Greenland Time");
        bean.setTimeZoneValue("UTC-03:30");
        bean.setTimeZoneOffset(-3.5f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("Saint Pierre and Miquelon Time");
        bean.setTimeZoneValue("UTC-03:00");
        bean.setTimeZoneOffset(-3f);
        list.add(bean);

        bean = new TimeZoneBean();
        bean.setTimeZoneName("East Greenland Time");
        bean.setTimeZoneValue("UTC-01:00");
        bean.setTimeZoneOffset(-1f);
        list.add(bean);

        return list;
    }

    // Created By: Deepak Sharma
    // Created Date: 30 June 2016
    // Purpose: calcualte authenticationvalue
    public static String getAuthenticationValue() {
        String authValue = "";
        String vin = Utility.VIN;
        int length = vin.length();

        // calculate checksum
        int checksum = 0;
        // sum of all numeric character of vin number
        for (int i = 0; i < length; i++) {
            Character character = vin.charAt(i);
            if (Character.isDigit(character)) {
                checksum += Character.getNumericValue(character);
            }
        }

        // five consective circular shift left
        checksum = (checksum << 5);

        // xor with number 150 as per document
        checksum = checksum ^ 168;

        // get lower 8-bit byte
        checksum = checksum & 0xFF;

        // Concatenate Vin no and checksum
        authValue = vin + checksum;

        return authValue;
    }

    public static boolean specialCategoryChanged() {
        //check the specialCategory
        boolean result = false;
        int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        String specialCategory = UserDB.getSpecialCategory(driverId);
        Log.i("Special", "specialCategory=" + specialCategory);
        UserBean currentUser = Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;
        Log.i("Special", "currentUser specialCategory=" + currentUser.getSpecialCategory());
        if (specialCategory != "" && currentUser != null && !specialCategory.equals(currentUser.getSpecialCategory())) {
            result = true;
        }
        return result;
    }


}
