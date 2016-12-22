package com.hutchgroup.elog;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.beans.DiagnosticIndicatorBean;
import com.hutchgroup.elog.beans.GPSData;
import com.hutchgroup.elog.beans.SettingsBean;
import com.hutchgroup.elog.common.AlarmSetter;
import com.hutchgroup.elog.common.AlertMonitor;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.ChatClient;
import com.hutchgroup.elog.common.ConstantFlag;
import com.hutchgroup.elog.common.DiagnosticMalfunction;
import com.hutchgroup.elog.common.GPSTracker;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.UserPreferences;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.common.ZoneList;
import com.hutchgroup.elog.db.AlertDB;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;
import com.hutchgroup.elog.db.SettingsDB;

import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;


public class ELogApplication extends Application {
    String TAG = ELogApplication.class.getName();
    final int STOP_TIME = 3;
    final int STOPPING_TIME = 300;//5 mins
    final int ONE_HOUR_DRIVING = 3600; //1 hour
    final int HALF_HOUR_DRIVING = 1800;

    private ELogMainActivity activity = null;
    boolean bEventSavedWhenMoving;
    boolean bEventSavedWhenStopping;
    boolean bEventPowerOff;
    boolean bEventPowerOn;
    public static GPSTracker objGps = new GPSTracker();

    boolean bPowerOnOff;
    int currentStatus;

    boolean bFreezeLayoutShowing;

    private static Application mApp;

    private static SharedPreferences mPrefs;

    public static Application get() {
        return mApp;
    }

    public static SharedPreferences getPrefs() {
        return mPrefs;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Utility.context = this;

            initializeUserPreferences();
            currentStatus = 1;

            bEventSavedWhenMoving = false;
            bEventSavedWhenStopping = false;
            bEventPowerOn = false;
            bEventPowerOff = false;
            bPowerOnOff = false; //off

            bFreezeLayoutShowing = false;

            SharedPreferences prefs = this.getSharedPreferences("HutchGroup", MODE_PRIVATE);
            CanMessages.OdometerReading = prefs.getString("odometer", "0");
            CanMessages.EngineHours = prefs.getString("engine_hours", "0");

            Utility.TimeZoneId = prefs.getString("timezoneid", TimeZone.getDefault().getID());
            Utility.TimeZoneOffset = ZoneList.getOffset(Utility.TimeZoneId);
            Utility.TimeZoneOffsetUTC = ZoneList.getTimeZoneOffset(Utility.TimeZoneId);
            Utility.sdf.setTimeZone(TimeZone.getTimeZone(Utility.TimeZoneId));
            // start gps thread
            objGps.startUsingGPS(getApplicationContext());


            boolean isFirstRun = prefs.getBoolean("firstrun", true);

            if (!isFirstRun) {
                //run thread to auto check vehicle
                Thread thVehicleMotion = new Thread(checkVehicleMotion);
                thVehicleMotion.setName("ElogApplication-checkVehicleMotion");
                thVehicleMotion.start();

                Thread thcheckIntermediate = new Thread(checkIntermediate);
                thcheckIntermediate.setName("ElogApplication-checkIntermediate");
                thcheckIntermediate.start();
            }

            mApp = this;
            mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        } catch (Exception e) {
            LogFile.write(ELogApplication.class.getName() + "::onCreate Error: " + e.getMessage(), LogFile.APPLICATION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onTerminate() {
        //objGps.stopUsingGPS();
        Utility.showMsg("Application terminated");

        super.onTerminate();
        mApp = null;
    }

    public synchronized void setActivity(ELogMainActivity activity) {
        this.activity = activity;
    }

    private synchronized void updateWhenMoving() {
        try {
            if (activity != null) {
                if (!bFreezeLayoutShowing) {
                    if (Utility.user1.getAccountId() == 0 && Utility.user2.getAccountId() == 0) {
                        activity.freezeLayout();
                        bFreezeLayoutShowing = true;
                    } else if (Utility.user1.isActive()) {
                        if (Utility.user1.isOnScreenFg()) {
                            //call
                            if (((MainActivity) activity).currentScreen != ((MainActivity) activity).Login_Screen) {
                                activity.freezeLayout();
                                bFreezeLayoutShowing = true;
                            }
                        } else {
                            if (Utility.user2.isOnScreenFg()) {
                                if (((MainActivity) activity).currentScreen == ((MainActivity) activity).Login_Screen) {
                                    activity.freezeLayout();
                                    bFreezeLayoutShowing = true;
                                }
                            }
                        }
                    } else if (Utility.user2.isActive()) {
                        if (Utility.user2.isOnScreenFg()) {
                            //call
                            if (((MainActivity) activity).currentScreen != ((MainActivity) activity).Login_Screen) {
                                activity.freezeLayout();
                                bFreezeLayoutShowing = true;
                            }
                        } else {
                            if (Utility.user1.isOnScreenFg()) {
                                if (((MainActivity) activity).currentScreen == ((MainActivity) activity).Login_Screen) {
                                    activity.freezeLayout();
                                    bFreezeLayoutShowing = true;
                                }
                            }
                        }
                    }
                }
            }

            if (Utility.user1.getAccountId() == 0 && Utility.user2.getAccountId() == 0) {
                //unidentified driver
                if (!bEventSavedWhenMoving) {
                    //122 LogFile.write(ELogApplication.class.getName() + "::checkVehicleMotion " + "Save Unidentified Event with Duty Status is DRIVING", LogFile.AUTOMATICALLY_TASK, LogFile.NOLOGIN_LOG);
                    // create/update dailyLog record
                    Utility.DrivingTime = 0;
                    currentStatus = 3;
                    int logId = DailyLogDB.DailyLogCreate(Utility.unIdentifiedDriverId, "", "", "");
                    //Log.i(TAG, "Save Unidentified Event with Duty Status is DRIVING");
                    EventDB.EventCreate(Utility.getCurrentDateTime(), 1, 3, "Driver's Duty Status changed to DRIVING", 1, 1, logId, Utility.unIdentifiedDriverId, "");
                    bEventSavedWhenMoving = true;
                }
            } else {
                if (activity != null) {
                    activity.autoChangeStatus();
                    activity.resetFlag();
                    currentStatus = ((MainActivity) activity).activeCurrentDutyStatus;
                }
            }

        } catch (Exception e) {
            LogFile.write(ELogApplication.class.getName() + "::updateWhenMoving Error: " + e.getMessage(), LogFile.APPLICATION, LogFile.ERROR_LOG);
        }
    }

    private synchronized void updateWhenStopping() {
        try {
            if (activity != null) {
                activity.promptToChangeStatus();
                currentStatus = ((MainActivity) activity).activeCurrentDutyStatus;
            }
        } catch (Exception e) {
            LogFile.write(ELogApplication.class.getName() + "::updateWhenStopping Error: " + e.getMessage(), LogFile.APPLICATION, LogFile.ERROR_LOG);
        }
    }

    private void saveUnidentifiedRecord() {
        try {
            if (!bEventSavedWhenStopping) {
                //122 LogFile.write(ELogApplication.class.getName() + "::checkVehicleMotion " + "Save Unidentified Event with Duty Status is ON DUTY", LogFile.AUTOMATICALLY_TASK, LogFile.NOLOGIN_LOG);
                // create/update dailyLog record
                currentStatus = 4;
                int logId = DailyLogDB.DailyLogCreate(Utility.unIdentifiedDriverId, Utility.ShippingNumber, Utility.TrailerNumber, "");
                Log.i(TAG, "Save Unidentified Event with Duty Status is ON DUTY");
                EventDB.EventCreate(Utility.getCurrentDateTime(), 1, 4, "Driver's Duty Status changed to ON DUTY", 1, 1, logId, Utility.unIdentifiedDriverId, "");
                bEventSavedWhenStopping = true;
            }
        } catch (Exception e) {
            LogFile.write(ELogApplication.class.getName() + "::saveUnidentifiedRecord Error: " + e.getMessage(), LogFile.APPLICATION, LogFile.ERROR_LOG);
        }
    }

    private synchronized void updateWhenMachineOn() {
        try {
            if (!bEventPowerOn) {
                // alerts when engine starts
                AlertMonitor.EngineStartAlerts();
                Log.i("Application", "Power On");
                bEventPowerOn = true;
                bEventPowerOff = false;

                GPSData.LastStatusTime = System.currentTimeMillis();
                Utility.OdometerReadingSincePowerOn = CanMessages.OdometerReading;

                // switch driver case to get fuel economy per driver
                Utility.OdometerReadingStart = CanMessages.OdometerReading;
                Utility.savePreferences("OdometerReadingStart", Utility.OdometerReadingStart);

                Utility.EngineHourSincePowerOn = CanMessages.EngineHours;
                Utility.FuelUsedSincePowerOn = CanMessages.TotalFuelConsumed;

                // switch driver case to get fuel economy per driver
                Utility.FuelUsedStart = CanMessages.TotalFuelConsumed;
                Utility.savePreferences("FuelUsedStart", Utility.FuelUsedStart);

                if (activity != null) {
                    if (Utility.onScreenUserId == 0) {
                        int logId = DailyLogDB.DailyLogCreate(Utility.unIdentifiedDriverId, "", "", "");
                        EventDB.EventCreate(Utility.getCurrentDateTime(), 6, 1, "Engine power-up with conventional location precision", 1, 1, logId, Utility.unIdentifiedDriverId, "");
                    } else {
                        activity.machineOn();
                    }
                    activity.shutDownThreadStop();
                }
            }
        } catch (Exception e) {
            LogFile.write(ELogApplication.class.getName() + "::updateWhenMachineOn Error: " + e.getMessage(), LogFile.APPLICATION, LogFile.ERROR_LOG);
        }
    }

    public synchronized void updateWhenMachineOff() {
        try {
            if (!bEventPowerOff) {
                Log.i("Application", "Power Off");
                bEventPowerOff = true;
                bEventPowerOn = false;
                AlertMonitor.FuelEconomyViolationGet();
                GPSData.LastStatusTime = System.currentTimeMillis();
                if (activity != null) {
                    if (Utility.onScreenUserId == 0) {
                        int logId = DailyLogDB.DailyLogCreate(Utility.unIdentifiedDriverId, "", "", "");
                        EventDB.EventCreate(Utility.getCurrentDateTime(), 6, 3, "Engine shut down with conventional location precision", 1, 1, logId, Utility.unIdentifiedDriverId, "");
                    } else {
                        activity.machineOff();
                    }
                    activity.shutDownThreadStart();
                }
            }
        } catch (Exception e) {
            LogFile.write(ELogApplication.class.getName() + "::updateWhenMachineOff Error: " + e.getMessage(), LogFile.APPLICATION, LogFile.ERROR_LOG);
        }
    }

    private void initializeUserPreferences() {
        try {
            UserPreferences.setCurrentRule(1);
            UserPreferences.setTimeZone("UTC-08:00");
            UserPreferences.setStartTime("12:00 AM");
        } catch (Exception e) {
            LogFile.write(ELogApplication.class.getName() + "::initializeUserPreferences Error: " + e.getMessage(), LogFile.APPLICATION, LogFile.ERROR_LOG);
        }
    }

    Runnable checkIntermediate = new Runnable() {
        long dTime = System.currentTimeMillis();

        @Override
        public void run() {
            while (true) {
                try {
                    if (Utility.user1.getAccountId() == 0 && Utility.user2.getAccountId() == 0) {

                    } else {
                        if (activity != null) {
                            currentStatus = ((MainActivity) activity).activeCurrentDutyStatus;
                        }
                    }

                    if (currentStatus == 3 || currentStatus == 5 || currentStatus == 6) {
                        Utility.DrivingTime += (int) ((System.currentTimeMillis() - dTime) / 1000);
                    }

                    if (Utility.DrivingTime >= ONE_HOUR_DRIVING) {

                        if (Utility.user1.getAccountId() == 0 && Utility.user2.getAccountId() == 0) {
                            //unidentified
                            int logId = DailyLogDB.DailyLogCreate(Utility.unIdentifiedDriverId, "", "", "");
                            EventDB.EventCreate(Utility.getCurrentDateTime(), 2, 1, "Intermediate log with conventional location precision", 1, 1, logId, Utility.unIdentifiedDriverId, "");
                        } else {
                            if (activity != null) {
                                activity.saveIntermediateLog();
                            }
                        }

                        Utility.DrivingTime = 0;
                    }
                    dTime = System.currentTimeMillis();

                    Thread.sleep(1000);
                } catch (Exception e) {
                    LogFile.write(ELogApplication.class.getName() + "::checkIntermediate Error: " + e.getMessage(), LogFile.APPLICATION, LogFile.ERROR_LOG);
                    break;
                }
            }
        }
    };

    Runnable checkVehicleMotion = new Runnable() {
        long time = System.currentTimeMillis();
        int count = 0;
        long stopedTime = System.currentTimeMillis();
        int countStop = 0;
        int countStopNonActivity = 0;
        long stopedTimeNonActivity = System.currentTimeMillis();
        long dTime = System.currentTimeMillis();
        long drivingStartTime = 0;

        @Override
        public void run() {
            //Log.i(TAG, "Run auto thead");
            while (true) {
                try {
                    //checking vehicle is moving or not
                    if (Float.valueOf(CanMessages.Speed) > 8f) {
                        if (drivingStartTime == 0) {
                            GPSData.LastStatusTime = System.currentTimeMillis();
                            drivingStartTime = System.currentTimeMillis();
                        }

                        if (!Utility.motionFg) {
                            GPSData.LastStatusTime = System.currentTimeMillis();
                            AlertMonitor.NoTripInspectionGet();
                        }
                        //Log.i(TAG, "Speed over 5");
                        if (Utility.user1.getAccountId() == 0 && Utility.user2.getAccountId() == 0) {
                            int time = Utility.UnidentifiedDrivingTime + (int) ((System.currentTimeMillis() - drivingStartTime) / 1000);

                            if (time > HALF_HOUR_DRIVING) {

                                //Data diagnostic event for unidentified driving time
                                if (!DiagnosticIndicatorBean.UnidentifiedDrivingDiagnosticFg) {
                                    DiagnosticIndicatorBean.UnidentifiedDrivingDiagnosticFg = true;
                                    // save data diagnostic event for unidentified driver
                                    DiagnosticMalfunction.saveDiagnosticIndicatorByCode("5", 3, "UnidentifiedDrivingDiagnosticFg");
                                }
                            }
                        }

                        Utility.motionFg = true;
                        countStop = 0;
                        countStopNonActivity = 0;
                        bEventSavedWhenStopping = false;
                        //call to change duty status to Driving
                        updateWhenMoving();
                    } else {
                        if (Float.valueOf(CanMessages.Speed) == 0f) {
                            if (drivingStartTime > 0) {

                                Utility.UnidentifiedDrivingTime = Utility.UnidentifiedDrivingTime + (int) ((System.currentTimeMillis() - drivingStartTime) / 1000);
                                drivingStartTime = 0;
                            }

                            //speed = 0 in 3 second
                            long stTime = (System.currentTimeMillis() - time) / 1000;
                            if (stTime > STOP_TIME) {
                                if (Utility.motionFg) {
                                    GPSData.LastStatusTime = System.currentTimeMillis();
                                }
                                dTime = System.currentTimeMillis();
                                Utility.motionFg = false;
                                if (activity != null) {
                                    if (bFreezeLayoutShowing) {
                                        activity.hideFreezeLayout();
                                        bFreezeLayoutShowing = false;
                                    }
                                }
                                if (Utility.user1.getAccountId() == 0 && Utility.user2.getAccountId() == 0) {
                                    //unidentified
                                    if (countStopNonActivity == 0) {
                                        stopedTimeNonActivity = System.currentTimeMillis();
                                        countStopNonActivity++;
                                    }

                                    bEventSavedWhenMoving = false;
                                    saveUnidentifiedRecord();
                                    countStopNonActivity = 0;
                                    //}
                                } else {
                                    if (activity != null) {
                                        int status = MainActivity.activeCurrentDutyStatus;

                                        if (status == 3) {
                                            if (countStop == 0) {
                                                stopedTime = System.currentTimeMillis();
                                                countStop++;
                                            }
                                            boolean setOnDuty = (System.currentTimeMillis() - stopedTime) / 1000 > STOPPING_TIME;
                                            if (setOnDuty) {
                                                bEventSavedWhenMoving = false;
                                                updateWhenStopping();
                                                countStop = 0;
                                            }
                                        } else {
                                            countStop = 0;
                                        }
                                    }

                                }
                            }
                            if (count == 0) {
                                time = System.currentTimeMillis();
                                count++;
                            }
                        } else {
                            time = System.currentTimeMillis();
                            count = 0;
                            countStop = 0;
                        }

                        //checking vehicle engine is on or off
                        if (Float.valueOf(CanMessages.RPM) == 0f) {
                            //off
                            //Log.i(TAG, "Off");
                            updateWhenMachineOff();
                        } else if (Float.valueOf(CanMessages.RPM) > 0f) {
                            //on
                            //Log.i(TAG, "On");
                            updateWhenMachineOn();
                        }
                    }
                    //Log.i(TAG, "checking " + CanMessages.RPM);

                    Thread.sleep(1000);
                } catch (Exception e) {
                    LogFile.write(ELogApplication.class.getName() + "::checkVehicleMotion Error: " + e.getMessage(), LogFile.APPLICATION, LogFile.ERROR_LOG);
                    break;
                }
            }
        }
    };


}
