package com.hutchgroup.elog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hutchgroup.elog.adapters.DiagnosticMalfunctionAdapter;
import com.hutchgroup.elog.adapters.DrawerItemAdapter;
import com.hutchgroup.elog.adapters.EventAdapter;
import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.beans.DailyLogBean;
import com.hutchgroup.elog.beans.DiagnosticIndicatorBean;
import com.hutchgroup.elog.beans.DrawerItemBean;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.beans.GPSData;
import com.hutchgroup.elog.beans.MessageBean;
import com.hutchgroup.elog.beans.TripInspectionBean;
import com.hutchgroup.elog.beans.UserBean;
import com.hutchgroup.elog.beans.VersionInformationBean;
import com.hutchgroup.elog.bll.HourOfService;
import com.hutchgroup.elog.common.AlarmSetter;
import com.hutchgroup.elog.common.AlertMonitor;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.ChatClient;
import com.hutchgroup.elog.common.ConstantFlag;
import com.hutchgroup.elog.common.CustomDateFormat;
import com.hutchgroup.elog.common.DiagnosticMalfunction;
import com.hutchgroup.elog.common.GForceMonitor;
import com.hutchgroup.elog.common.GetCall;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.SPNMap;
import com.hutchgroup.elog.common.Tpms;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.common.ZoneList;
import com.hutchgroup.elog.db.AlertDB;
import com.hutchgroup.elog.db.CarrierInfoDB;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;
import com.hutchgroup.elog.db.LoginDB;
import com.hutchgroup.elog.db.MessageDB;
import com.hutchgroup.elog.db.SettingsDB;
import com.hutchgroup.elog.db.TripInspectionDB;
import com.hutchgroup.elog.db.UserDB;
import com.hutchgroup.elog.db.VehicleDB;
import com.hutchgroup.elog.db.VehicleInfoDB;
import com.hutchgroup.elog.db.VersionInformationDB;
import com.hutchgroup.elog.fragments.BluetoothConnectivityFragment;
import com.hutchgroup.elog.fragments.DTCFragment;
import com.hutchgroup.elog.fragments.DailyLogDashboardFragment;
import com.hutchgroup.elog.fragments.DetailFragment;
import com.hutchgroup.elog.fragments.DockingFragment;
import com.hutchgroup.elog.fragments.DriverProfileFragment;
import com.hutchgroup.elog.fragments.DvirFragment;
import com.hutchgroup.elog.fragments.ELogFragment;
import com.hutchgroup.elog.fragments.ExtraFragment;
import com.hutchgroup.elog.fragments.InspectLogFragment;
import com.hutchgroup.elog.fragments.LoginFragment;
import com.hutchgroup.elog.fragments.MessageFragment;
import com.hutchgroup.elog.fragments.ModifiedFragment;
import com.hutchgroup.elog.fragments.NewEventFragment;
import com.hutchgroup.elog.fragments.NewInspectionFragment;
import com.hutchgroup.elog.fragments.OutputFileSendDialog;
import com.hutchgroup.elog.fragments.PopupDialog;
import com.hutchgroup.elog.fragments.ScoreCardFragment;
import com.hutchgroup.elog.fragments.SettingsFragment;
import com.hutchgroup.elog.fragments.ShutDownDeviceDialog;
import com.hutchgroup.elog.fragments.TabSystemFragment;
import com.hutchgroup.elog.fragments.TpmsFragment;
import com.hutchgroup.elog.fragments.TrailerDialogFragment;
import com.hutchgroup.elog.fragments.TrailerManagementFragment;
import com.hutchgroup.elog.fragments.UnCertifiedFragment;
import com.hutchgroup.elog.fragments.UnidentifyFragment;
import com.hutchgroup.elog.fragments.UserListFragment;
import com.hutchgroup.elog.fragments.VehicleInfoFragment;
import com.hutchgroup.elog.fragments.ViolationFragment;
import com.hutchgroup.elog.services.AutoStartService;
import com.hutchgroup.elog.tasks.AppUpdateData;
import com.hutchgroup.elog.tasks.AutoSyncData;
import com.hutchgroup.elog.tasks.DownloadAPK;
import com.hutchgroup.elog.tasks.MessageSyncData;
import com.hutchgroup.elog.util.LetterSpacingTextView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends ELogMainActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentManager.OnBackStackChangedListener, ELogFragment.OnFragmentInteractionListener, DockingFragment.OnFragmentInteractionListener,
        UnCertifiedFragment.OnFragmentInteractionListener, UnidentifyFragment.OnFragmentInteractionListener,
        ModifiedFragment.OnFragmentInteractionListener, ViolationFragment.OnFragmentInteractionListener, EventAdapter.ItemClickListener, DiagnosticMalfunction.DiagnosticMalfunctionNotification,
        NewEventFragment.OnFragmentInteractionListener, DetailFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener,
        UserListFragment.OnFragmentInteractionListener, BluetoothConnectivityFragment.OnFragmentInteractionListener, OutputFileSendDialog.OutputFileDialogInterface,
        DvirFragment.OnFragmentInteractionListener, DailyLogDashboardFragment.OnFragmentInteractionListener, TpmsFragment.OnFragmentInteractionListener, TabSystemFragment.OnFragmentInteractionListener,
        LoginFragment.OnFragmentInteractionListener, MessageFragment.OnFragmentInteractionListener, NewInspectionFragment.OnFragmentInteractionListener, InspectLogFragment.OnFragmentInteractionListener, ChatClient.ChatMessageReceiveIndication, PopupDialog.DialogActionInterface, HourOfService.IViolation, ShutDownDeviceDialog.OnFragmentInteractionListener, CanMessages.ICanMessage, ExtraFragment.OnFragmentInteractionListener, DTCFragment.OnFragmentInteractionListener
        , GForceMonitor.IGForceMonitor, VehicleInfoFragment.OnFragmentInteractionListener {

    private PopupDialog ponDutyChangeDialog;
    private boolean onDutyChangeDialogResponse, autoDismissOnDutyChangeDialog, isDialogShown;

    AlertDialog dialog;
    AlertDialog.Builder onDutyChangeBuilder, builder;

    private final int CHECK_UPDATE_TIME = 10 * 60 * 1000; //10 mins
    private final int DATASTORAGE_CHECKING_TIME = 1800000; //30 mins

    private final int DailyLog_Screen = 0;
    private final int Inspect_DailyLog_Screen = 1;
    private final int Uncertified_LogBook_Screen = 2;
    private final int Unidentified_Data_Screen = 3;
    private final int Edit_Request_Screen = 4;
    private final int Violation_History_Screen = 5;
    private final int Unidentified_Event_Screen = 6;
    private final int Input_Information_Screen = 7;
    private final int New_Event_Screen = 8;
    private final int Message = 9;
    private final int DVIR = 10;
    private final int TPMS = 11;
    public final int Login_Screen = 12;
    public final int Driver_Profile_Screen = 13;
    private final int Extra = 14;
    private final int DTC = 15;
    private final int ScoreCard = 16;
    private final int TrailerManagement = 17;
    private final int VehicleInfo = 18;
    public static Date ViolationDT;

    BluetoothAdapter adapter = null;
    ImageView ivActiveUser;

    static TextView tvUserName;
    static LinearLayout tvFreeze;
    RelativeLayout tvLoginFreeze;
    LinearLayout tvGauge;

    RelativeLayout rlLoadingPanel;
    String driverName;
    String title;
    public static int currentDutyStatus = 1;
    public static int activeCurrentDutyStatus = 1;
    int totalDistance;
    int startOdometer;
    int startHourEngine;

    public static boolean undockingMode;

    String TAG = MainActivity.class.getName();

    private boolean bEditEvent;
    private boolean bWebEvent;
    private EventBean selectedEvent;
    private boolean isOnDailyLog;
    private boolean bInspectDailylog;
    private boolean bLogin;

    Toolbar toolbar;

    private int previousScreen;
    public int currentScreen;

    boolean bEventPowerOn;
    boolean bEventPowerOff;

    boolean bBluetoothConnectionSuccess;
    boolean bBluetoothConnectionError;
    boolean bBluetoothConnecting;
    ProgressDialog progressBarDialog;
    OutputFileSendDialog outputFileDialog;
    AlertDialog messageDialog;
    AlertDialog successDialog;

    ELogFragment elogFragment;
    /* DetailFragment inspectFragment;*/
    LoginFragment loginFragment;

    boolean specialCategoryChanged = false;

    AlertDialog specialCategoryDialog;
    boolean bSaveDriving;
    FrameLayout frameSpeedometer, frameBoost, frameRPM;

    final android.os.Handler handler = new android.os.Handler();

    Runnable autoSync = new Runnable() {
        @Override
        public void run() {

            if (Utility.isInternetOn()) {
                new AutoSyncData(autoSyncDataPostTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new MessageSyncData(messageSyncDataPostTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            // and here comes the "trick"
            handler.postDelayed(this, Utility._appSetting.getSyncTime() * 60 * 1000);
        }
    };

    final android.os.Handler updateHandler = new android.os.Handler();

    Runnable autoCheckUpdate = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "auto check update");
            Log.i("AutoUpdate", "Start");
            SharedPreferences prefs = getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);

            if (Utility.isInternetOn()) {
                //Log.d(TAG, "auto check update has internet");
                if (!prefs.getBoolean("check_update", false)) {
                    try {
                        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        new AppUpdateData(autoDownloadUpdatePostTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pInfo.versionName);
                    } catch (Exception e) {
                        Log.d(TAG, "Cannot find package info: " + e.getMessage());
                    }
                }
            }

            // and here comes the "trick"
            updateHandler.postDelayed(this, CHECK_UPDATE_TIME);
        }
    };

    final Handler checkDataStorageHandler = new Handler();
    Runnable checkDataStorage = new Runnable() {
        @Override
        public void run() {
            try {

                UserBean currentUser = Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;
                if (currentUser.getExemptELDUseFg() == 0) {
                    float memory = Utility.gigabytesAvailable(Environment.getExternalStorageDirectory());
                    if (memory < .1f) {
                        //memory less than 100MB
                        //data recording malfunction
                        if (!DiagnosticIndicatorBean.DataRecordingMalfunctionFg) {
                            DiagnosticIndicatorBean.DataRecordingMalfunctionFg = true;
                            // save malfunction for storage compliance
                            DiagnosticMalfunction.saveDiagnosticIndicatorByCode("R", 1, "DataRecordingMalfunctionFg");
                        }
                    } else {
                        if (DiagnosticIndicatorBean.DataRecordingMalfunctionFg) {
                            // clear malfunction for storage compliance
                            DiagnosticIndicatorBean.DataRecordingMalfunctionFg = false;
                            DiagnosticMalfunction.saveDiagnosticIndicatorByCode("R", 2, "DataRecordingMalfunctionFg");
                        }

                    }
                }
            } catch (Exception ex) {

            }
            checkDataStorageHandler.postDelayed(this, DATASTORAGE_CHECKING_TIME);
        }
    };

    Handler checkDataTransferMalfunctionHandler = new Handler();
    Runnable checkDataTransferMalfunction = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "checkDataTransferMalfunction");
            UserBean currentUser = Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;
            if (currentUser.getExemptELDUseFg() == 0) {
                //count number of checking after diagnostic happens, only check 3 times, period is 24 hours
                Utility.DataTransferMalfunctionCheckingNumber++;

                SharedPreferences sp = getApplicationContext().getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
                String transferSuccessDate = sp.getString("data_transfer", "");
                if (transferSuccessDate != "") {
                    if (Utility.getDiffDay(transferSuccessDate, Utility.getCurrentDate()) < 3) {
                        //has data transfer success in 3 next days -> clear diagnostic

                    }
                } else {
                    Utility.DataTransferDiagnosticCount++;
                }

                if (Utility.DataTransferMalfunctionCheckingNumber > 3) {
                    if (Utility.DataTransferDiagnosticCount >= 3) {

                        Utility.DataTransferDiagnosticCount = 0;
                    }

                    Utility.DataTransferMalfunctionCheckingNumber = 0;
                    checkDataTransferMalfunctionHandler.removeCallbacks(this);
                }
            }
            checkDataTransferMalfunctionHandler.postDelayed(this, 24 * 60 * 60 * 1000);
        }
    };

    Handler checkDataTransferDiagnosticHandler = new Handler();
    Runnable checkDataTransferDiagnostic = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "checkDataTransferDiagnostic");
            boolean bDiagnotic;
            UserBean currentUser = Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;
            if (currentUser.getExemptELDUseFg() == 0) {
                SharedPreferences sp = getApplicationContext().getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
                String transferSuccessDate = sp.getString("data_transfer", "");
                if (transferSuccessDate != "") {
                    if (Utility.getDiffDay(transferSuccessDate, Utility.getCurrentDate()) > 7) {
                        //diagnostic
                        bDiagnotic = true;
                    } else {
                        bDiagnotic = false;
                    }
                } else {
                    //diagnostic
                    bDiagnotic = true;
                }

                if (bDiagnotic) {
                    //data transfer diagnostic
                    String diagnosticDate = sp.getString("data_diagnostic_date", "");
                    //have not save data transfer diagnostic or it is already saved more than 7 days
                    if (diagnosticDate.equals("") || (Utility.getDiffDay(diagnosticDate, Utility.getCurrentDate()) > 7)) {

                        sp.edit().putString("data_diagnostic_date", Utility.getCurrentDate()).commit();
                    }

                    checkDataTransferMalfunctionHandler.postDelayed(checkDataTransferMalfunction, 50);
                } else {
                    //clear diagnostic
                    //clear cache about diagnostic date
                    sp.edit().putString("data_diagnostic_date", "").commit();
                }
            }
            checkDataTransferDiagnosticHandler.postDelayed(this, 7 * 24 * 60 * 60 * 1000);
        }
    };

    private BroadcastReceiver gpsChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Receive GPS change");
            LocationManager locationManager = (LocationManager) Utility.context
                    .getSystemService(getBaseContext().LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                turnOnGPSIcon();
            } else {
                turnOffGPSIcon();

            }
        }
    };

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo localNetworkInfo = ((ConnectivityManager) getBaseContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();

            if (localNetworkInfo != null) {
                GPSData.RoamingFg = localNetworkInfo.isRoaming() ? 1 : 0;
                if (localNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    onUpdateWifiIcon(localNetworkInfo.isConnected());

                } else if (localNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

                    onUpdateNetworkIcon(localNetworkInfo.isConnected());
                } else {
                    GPSData.WifiOnFg = 0;
                    GPSData.CellOnlineFg = 0;
                }
            } else {
                GPSData.WifiOnFg = 0;
                GPSData.CellOnlineFg = 0;
                onUpdateWifiIcon(false);
            }
        }
    };

    private void onAcPower() {
        Utility.showMsg("On AC Power");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    if (gForceMonitor != null) {
                        gForceMonitor.highPassFilter = true;
                        resumeGforce();
                    }
                } catch (Exception exe) {
                }
            }
        }).start();
    }

    private void onBatterPower() {

        pauseGforce();
        Utility.showMsg("On Batter Power");
    }

    Thread thBatteryMonitor;
    private final static int levelThreshold = 60;
    private final static int shutDownThreshold = 30;
    LinearLayout layoutAlertMessage;
    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            Utility.BatteryLevel = level;
            //Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean isPlugged = (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB);
            if (GPSData.ACPowerFg == 0) {
                if (isPlugged) {
                    onAcPower();
                }
            } else {
                if (!isPlugged) {
                    onBatterPower();
                }
            }
            GPSData.ACPowerFg = (isPlugged ? 1 : 0);
            onUpdateBatteryIcon(level, isPlugged);
            if (level <= levelThreshold && !isPlugged) {
                if (thBatteryMonitor == null) {
                    thBatteryMonitor = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            layoutAlertMessage.setVisibility(View.VISIBLE);
                                        }
                                    });

                                    int waitTime = 5 * 60 * 1000; // 5 minutes wait time
                                    Thread.sleep(waitTime);

                                } catch (InterruptedException exe) {
                                    break;
                                }
                            }
                        }
                    });
                    thBatteryMonitor.setName("th-BatteryMonitor");
                    thBatteryMonitor.start();
                }
            } else {
                if (thBatteryMonitor != null) {
                    layoutAlertMessage.setVisibility(View.GONE);
                    Log.i("BatterMonitor-Removed: ", Utility.BatteryLevel + " %");
                    thBatteryMonitor.interrupt();
                    thBatteryMonitor = null;
                }
            }
        }
    };

    AutoSyncData.PostTaskListener<Boolean> autoSyncDataPostTaskListener = new AutoSyncData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            showLoaderAnimation(false);
            if (!result) {
                Log.e(GetCall.class.getName(), "AutoSyncData Error:" + Utility.errorMessage);
                //LogFile.write(GetCall.class.getName() + "::AutoSyncData Error:" + Utility.errorMessage, LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
            }

            onUpdateWebServiceIcon(true);

            specialCategoryChanged = Utility.specialCategoryChanged();
        }
    };


    MessageSyncData.PostTaskListener<Boolean> messageSyncDataPostTaskListener = new MessageSyncData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            Log.d(TAG, "message sync result = " + result);
            if (result) {
            } else {
                Log.e(GetCall.class.getName(), "MessageSyncData Error:" + Utility.errorMessage);
                //LogFile.write(GetCall.class.getName() + "::MessageSyncData Error:" + Utility.errorMessage, LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
            }
        }
    };

    AppUpdateData.PostTaskListener<Boolean> autoDownloadUpdatePostTaskListener = new AppUpdateData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            Log.d(TAG, "appUpdate check done");
            if (result) {
                Log.d(TAG, "new version to download");
                VersionInformationBean bean = VersionInformationDB.getVersionInformation();

                SharedPreferences prefs = getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
                prefs.edit().putBoolean("check_update", true).commit();

                if (bean.getAutoDownloadFg()) {
                    manuallyUpdate = false;
                    new DownloadAPK(downloadListener, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    checkingUpdate = true;
                } else {
                    //Utility.showAlertMsg("New update is available.");
                    checkingUpdate = false;
                }
            } else {
                Log.d(TAG, "Do not have new version");
                checkingUpdate = false;
            }
        }
    };

    boolean manuallyUpdate = false;
    boolean checkingUpdate = false;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;
    public String newVersion = "";

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading ELD " + newVersion + "...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    AppUpdateData.PostTaskListener<Boolean> appUpdatePostTaskListener = new AppUpdateData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            Log.d(TAG, "appUpdate check done");
            showLoaderAnimation(false);
            if (result) {
                try {
                    Log.d(TAG, "new version to download");
                    VersionInformationBean bean = VersionInformationDB.getVersionInformation();

                    SharedPreferences prefs = getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
                    prefs.edit().putBoolean("check_update", true).commit();
                    newVersion = bean.getCurrentVersion();
                    if (bean.getAutoDownloadFg()) {
                        showDialog(DIALOG_DOWNLOAD_PROGRESS);
                        new DownloadAPK(downloadListener, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        checkingUpdate = false;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Downloading Error: " + e.getMessage());
                    checkingUpdate = false;
                }

            } else {
                Log.d(TAG, "Do not have new version");
                Utility.showAlertMsg("The latest updates have already been installed.");
                checkingUpdate = false;
            }
        }
    };

    DownloadAPK.DownloadAPKListener<Boolean> downloadListener = new DownloadAPK.DownloadAPKListener<Boolean>() {
        @Override
        public void updateProgress(int progress) {
            if (manuallyUpdate) {
                if (mProgressDialog != null) {
                    mProgressDialog.show();
                    mProgressDialog.setProgress(progress);
                }
            }
        }

        @Override
        public void closeProgress(String fileName) {
            SharedPreferences prefs = getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
            prefs.edit().putString("upgrade_file", fileName).commit();

            Log.d(TAG, "upgrade: " + fileName);

            if (manuallyUpdate) {
                dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            }
            checkingUpdate = false;
            manuallyUpdate = false;
        }

        @Override
        public void progressError() {
            if (manuallyUpdate) {
                dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            }
            checkingUpdate = false;
            manuallyUpdate = false;
        }

        @Override
        public void autoLogout() {
            LogoutAllOnInstall();
        }
    };


    // update the main content by replacing fragments
    FragmentManager fragmentManager = getSupportFragmentManager();

    LinearLayout flagBar, flagBarFreeze, flagBarFreeze1;

    boolean bHaveUnAssignedEvent;
    boolean bHaveLogbookToCertify;
    boolean firstLogin;

    FrameLayout frameDiagnostic;
    FrameLayout frameMalfunction;

    TextView tvSpeed, tvRPM, tvPosition, tvCoolant, tvVoltage;
    LetterSpacingTextView tvOdometer, tvEngineHours, tvDrivingRemainingFreeze;
    Thread threadUpdateCANInfos;

    ImageView imgreezeSpeed, imgFreezeRPM, imgFreezeVoltage, imgFreezeCoolantTemp, imgFreezeThrPos;
    ImageView icGPS, icViolation, icNetwork, icWifi, icWebService, icCanbus, icBattery, icInspection, icMessage, icTPMS, icCertifyLog;

    ImageView icFreezeGPS;
    ImageView icFreezeViolation;
    ImageView icFreezeNetwork;
    ImageView icFreezeWifi;
    ImageView icFreezeWebService;
    ImageView icFreezeCanbus;
    ImageView icFreezeBattery;
    ImageView icFreezeInspection;
    ImageView icFreezeMessage;
    ImageView icFreezeTPMS;
    ImageView icFreezeActiveUser;

    TextView tvLoginName, tvFreezeLoginName;
    CheckBox chkRules;

    int canbusState;
    Bitmap batteryBmp;
    Canvas canvas;

    ListView lvDrawer;
    ArrayList<DrawerItemBean> lstDrawerItems;
    public static TextToSpeech textToSpeech;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawer;

    boolean stopService = false;

    private void getDrawerItem() {
        lstDrawerItems = new ArrayList<>();
        DrawerItemBean bean;

        if (Utility.InspectorModeFg) {
            bean = new DrawerItemBean();
            bean.setId(R.id.inspect_dailylog);
            bean.setItem("Inspect Log");
            bean.setIcon(R.drawable.ic_drawer_daliy_log_inspect_drawer);
        } else {
            bean = new DrawerItemBean();
            bean.setId(R.id.daily_log);
            bean.setItem("ELD");
            bean.setIcon(R.drawable.ic_drawer_eld);
        }

        lstDrawerItems.add(bean);
        bean = new DrawerItemBean();
        bean.setId(R.id.dvir);
        bean.setItem("DVIR");
        bean.setIcon(R.drawable.ic_drawer_dvir);
        lstDrawerItems.add(bean);
        if (!Utility.InspectorModeFg) {
            bean = new DrawerItemBean();
            bean.setId(R.id.message);
            bean.setItem("Message");
            bean.setIcon(R.drawable.ic_drawer_messages);
            lstDrawerItems.add(bean);

            bean = new DrawerItemBean();
            bean.setId(R.id.tpms);
            bean.setItem("TPMS");
            bean.setIcon(R.drawable.ic_drawer_tpsm);
            lstDrawerItems.add(bean);


            bean = new DrawerItemBean();
            bean.setId(R.id.extra);
            bean.setItem("More");
            bean.setIcon(R.drawable.ic_drawer_more);
            lstDrawerItems.add(bean);

            bean = new DrawerItemBean();
            bean.setId(R.id.settings);
            bean.setItem("Settings");
            bean.setIcon(R.drawable.ic_drawer_settings);
            lstDrawerItems.add(bean);

        }


        bean = new DrawerItemBean();
        bean.setId(R.id.logout);
        bean.setItem("Logout");
        bean.setIcon(R.drawable.ic_drawer_logout);
        lstDrawerItems.add(bean);
    }

    private void bindDrawerItem() {
        getDrawerItem();
        DrawerItemAdapter drawerItemAdapter = new DrawerItemAdapter(R.layout.activity_main, lstDrawerItems);
        lvDrawer.setAdapter(drawerItemAdapter);
    }

    public boolean onNavigationItemSelected(int id) {

        elogFragment = null;
        //inspectFragment = null;
        loginFragment = null;
        // Handle navigation view item clicks here.
        if (id == R.id.daily_log) {
            if (undockingMode) {
                replaceFragment(new DockingFragment());
            } else {
                replaceFragment(DailyLogDashboardFragment.newInstance());
            }
            bInspectDailylog = false;
            bEditEvent = false;
            isOnDailyLog = false;
            getSupportActionBar().setTitle("ELD");
        } else if (id == R.id.inspect_dailylog) {
            callInspectELog();
        } else if (id == R.id.dvir) {
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(DvirFragment.newInstance());
            getSupportActionBar().setTitle("DVIR");
            previousScreen = currentScreen;
            currentScreen = DVIR;
            title = getApplicationContext().getResources().getString(R.string.menu_dvir);
        } else if (id == R.id.tpms) {
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(TpmsFragment.newInstance());
            getSupportActionBar().setTitle("TPMS");
            previousScreen = currentScreen;
            currentScreen = TPMS;
            title = getApplicationContext().getResources().getString(R.string.menu_tpms);
        } else if (id == R.id.settings) {
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(SettingsFragment.newInstance());
            getSupportActionBar().setTitle("Settings");
            previousScreen = currentScreen;
            currentScreen = TPMS;
        } else if (id == R.id.extra) {
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(ExtraFragment.newInstance());
            getSupportActionBar().setTitle("More");
            previousScreen = currentScreen;
            currentScreen = Extra;
        } else if (id == R.id.message) {
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(UserListFragment.newInstance());
            getSupportActionBar().setTitle("Message");
            previousScreen = currentScreen;
            currentScreen = Message;
            title = getApplicationContext().getResources().getString(R.string.menu_Message);
        } else if (id == R.id.logout) {
            if (!Utility.InspectorModeFg) {
                int currentStatus = EventDB.getCurrentDutyStatus(Utility.onScreenUserId);
                if (currentStatus != 1) {
                    Utility.showAlertMsg("you are not allowed to logout until you are in OFF DUTY");
                    return true;
                }
            }

            if (specialCategoryChanged) {
                showSpecialCategory(true);
            } else {
                showLogoutDialog();
            }
        }

        invalidateOptionsMenu();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
          /*  if (!ConstantFlag.Flag_Development) {
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

                if (wifiManager.isWifiEnabled())
                    wifiManager.setWifiEnabled(false);
            }*/

            // set alarm
            if (Utility.as == null) {
                Utility.as = new AlarmSetter();
                Utility.as.SetAlarm(this);
            }
            setOrientation();

            new SPNMap(this);
            stopService = false;
            SharedPreferences prefs = getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
            String upgradeFile = prefs.getString("upgrade_file", "");
            Log.d(TAG, "upgrade: " + upgradeFile);
            if (!upgradeFile.equals("")) {
                File file = new File(upgradeFile);
                if (file.exists()) {
                    file.delete();
                }
            }


            CanMessages.OdometerReading = prefs.getString("odometer", "0");
            CanMessages.EngineHours = prefs.getString("engine_hours", "0");

            //service to check and download if the new version is existed on server
            updateHandler.postDelayed(autoCheckUpdate, 1 * 60 * 1000);


            Log.d(TAG, "onCreate Activity");
            bBluetoothConnectionSuccess = false;
            bBluetoothConnectionError = false;
            bBluetoothConnecting = false;
            DiagnosticMalfunction.mListener = this;
            HourOfService.mListener = this;
            CanMessages.mCanListner = this;
            undockingMode = prefs.getBoolean("undocking", false);

            //start a service to check if the application is in background every 3sec
            //if yes the service will call the application again.
            if (ConstantFlag.AUTOSTART_MODE) {
                if (!undockingMode) {
                    startService(new Intent(this, AutoStartService.class));
                }
            }

            //checkDataTransferDiagnosticHandler.postDelayed(checkDataTransferDiagnostic, 50);

            checkDataStorageHandler.postDelayed(checkDataStorage, 50);

            setContentView(R.layout.activity_main);

            Utility.context = this;


            icGPS = (ImageView) findViewById(R.id.icGPS);
            icGPS.setBackgroundResource(R.drawable.ic_flagbar_gps_on);
            //icGPS.setVisibility(View.GONE);
            icViolation = (ImageView) findViewById(R.id.icViolation);
            icViolation.setVisibility(View.GONE);
            icNetwork = (ImageView) findViewById(R.id.icNetwork);
            icNetwork.setVisibility(View.GONE);
            icWifi = (ImageView) findViewById(R.id.icWifi);
            icWifi.setVisibility(View.GONE);
            icWebService = (ImageView) findViewById(R.id.icWebService);
            icCanbus = (ImageView) findViewById(R.id.icCanbus);
            icCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_disconnect);
            icBattery = (ImageView) findViewById(R.id.icBattery);
            icInspection = (ImageView) findViewById(R.id.icInspection);
            icMessage = (ImageView) findViewById(R.id.icMessage);
            icMessage.setBackgroundResource(R.drawable.ic_flagbar_message);
            icMessage.setVisibility(View.GONE);
            ChatClient.icListner = this;
            icTPMS = (ImageView) findViewById(R.id.icTPMS);

            icCertifyLog = (ImageView) findViewById(R.id.icCertify_Log);
            icCertifyLog.setVisibility(View.GONE);
            ivActiveUser = (ImageView) findViewById(R.id.ivDriver);

            icFreezeGPS = (ImageView) findViewById(R.id.icFreezeGPS);
            icFreezeGPS.setBackgroundResource(R.drawable.ic_flagbar_gps_on);
            icFreezeViolation = (ImageView) findViewById(R.id.icFreezeViolation);
            icFreezeViolation.setVisibility(View.GONE);
            icFreezeNetwork = (ImageView) findViewById(R.id.icFreezeNetwork);
            icFreezeNetwork.setVisibility(View.GONE);
            icFreezeWifi = (ImageView) findViewById(R.id.icFreezeWifi);
            icFreezeWifi.setVisibility(View.GONE);
            icFreezeWebService = (ImageView) findViewById(R.id.icFreezeWebService);
            icFreezeCanbus = (ImageView) findViewById(R.id.icFreezeCanbus);
            icFreezeCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_disconnect);
            icFreezeBattery = (ImageView) findViewById(R.id.icFreezeBattery);
            icFreezeInspection = (ImageView) findViewById(R.id.icFreezeInspection);
            icFreezeMessage = (ImageView) findViewById(R.id.icFreezeMessage);
            icFreezeMessage.setBackgroundResource(R.drawable.ic_flagbar_message);
            icFreezeMessage.setVisibility(View.GONE);
            icFreezeTPMS = (ImageView) findViewById(R.id.icFreezeTPMS);
            icFreezeActiveUser = (ImageView) findViewById(R.id.icFreezeDriver);
            flagBar = (LinearLayout) findViewById(R.id.flagBar);
            flagBarFreeze = (LinearLayout) findViewById(R.id.flagBarFreeze);
            flagBarFreeze1 = (LinearLayout) findViewById(R.id.flagBarFreeze1);

            frameSpeedometer = (FrameLayout) findViewById(R.id.frameSpeedometer);
            frameSpeedometer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.NightModeFg = !Utility.NightModeFg;
                    setUIMode();
                }
            });

            frameRPM = (FrameLayout) findViewById(R.id.frameRPM);
            frameRPM.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                        brightness -= 51;
                        if (brightness < 1) {
                            brightness = 1;
                        }

                        setBrightness(brightness);
                    } catch (Exception exe) {

                    }

                }
            });

            frameBoost = (FrameLayout) findViewById(R.id.frameBoost);
            frameBoost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                        brightness += 51;
                        if (brightness > 255) {
                            brightness = 255;
                        }

                        setBrightness(brightness);
                    } catch (Exception exe) {

                    }
                }
            });

            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_flagbar_battery_full);
            batteryBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(batteryBmp);
            bmp.recycle();

            this.registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            this.registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            this.registerReceiver(gpsChangeReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
            this.registerReceiver(dateChangedReceiver, new IntentFilter("MID_NIGHT"));

            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.findViewById(R.id.title);

            toolbar.setOnTouchListener(new View.OnTouchListener() {
                Rect hitrect = new Rect();

                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEvent.ACTION_DOWN == event.getAction()) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                        boolean hit = false;
                        for (int i = toolbar.getChildCount() - 1; i >= 0; i--) {
                            View view = toolbar.getChildAt(i);
                            if (view instanceof TextView) {
                                view.getHitRect(hitrect);
                                if (hitrect.contains((int) event.getX(), (int) event.getY())) {
                                    hit = true;
                                    break;
                                }
                            }
                        }

                      /*  if (hit) {
                            //Hit action
                            if (bInspectDailylog) {
                                if (inspectFragment != null)
                                    inspectFragment.ChooseDate();
                            }
                        }*/
                    }
                    return false;
                }
            });

            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

                @Override
                public void onDrawerStateChanged(int newState) {
                    super.onDrawerStateChanged(newState);

                    //Log.d(TAG, "State Changed");
                    if (bLogin) {
                        drawer.closeDrawers();
                    }
                }
            };
            toggle.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            originalToolbarListener = toggle.getToolbarNavigationClickListener();
            fragmentManager.addOnBackStackChangedListener(this);
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            //  navigationView.setNavigationItemSelectedListener(this);
            lvDrawer = (ListView) findViewById(R.id.lvDrawer);
            lvDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    onNavigationItemSelected(lstDrawerItems.get(position).getId());

                }
            });
            bindDrawerItem();
            rlLoadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
            tvUserName = (TextView) navigationView.findViewById(R.id.tvUserName);
            tvFreeze = (LinearLayout) findViewById(R.id.tvFreeze);
            tvLoginFreeze = (RelativeLayout) findViewById(R.id.tvLoginFreeze);
            tvGauge = (LinearLayout) findViewById(R.id.layoutGauge);

            imgreezeSpeed = (ImageView) findViewById(R.id.imgreezeSpeed);
            imgFreezeRPM = (ImageView) findViewById(R.id.imgFreezeRPM);
            imgFreezeVoltage = (ImageView) findViewById(R.id.imgFreezeVoltage);
            imgFreezeCoolantTemp = (ImageView) findViewById(R.id.imgFreezeCoolantTemp);
            imgFreezeThrPos = (ImageView) findViewById(R.id.imgFreezeThrPos);


            tvOdometer = (LetterSpacingTextView) findViewById(R.id.tvOdometer);
            tvSpeed = (TextView) findViewById(R.id.tvSpeed);
            tvRPM = (TextView) findViewById(R.id.tvRPM);
            tvPosition = (TextView) findViewById(R.id.tvPosition);
            tvCoolant = (TextView) findViewById(R.id.tvCoolant);
            tvVoltage = (TextView) findViewById(R.id.tvVoltage);
            tvEngineHours = (LetterSpacingTextView) findViewById(R.id.tvEngineHours);
            tvDrivingRemainingFreeze = (LetterSpacingTextView) findViewById(R.id.tvDrivingRemainingFreeze);

            layoutAlertMessage = (LinearLayout) findViewById(R.id.layoutAlertMessage);

            layoutAlertMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    layoutAlertMessage.setVisibility(View.GONE);
                }
            });

            if (Utility.isLargeScreen(getApplicationContext())) {
                tvOdometer.setLetterSpacing(21);
                tvEngineHours.setLetterSpacing(18);
                tvDrivingRemainingFreeze.setLetterSpacing(22);
            } else {
                tvOdometer.setLetterSpacing(23);
                tvEngineHours.setLetterSpacing(20);
                tvDrivingRemainingFreeze.setLetterSpacing(24);
            }
            tvDrivingRemainingFreeze.setText("00:00:00");
            //control to load fragment
            boolean autoStartFg = false;
            boolean isLoginCall = false;
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                isLoginCall = bundle.getBoolean("loginCall", false);
                autoStartFg = bundle.getBoolean("AutoStartFg", false);
                firstLogin = bundle.getBoolean("firstLogin", true);
            }

            // intialize what we do on splash screen
            if (autoStartFg) {
                Utility.IMEIGet(MainActivity.this);
                Utility.VersionGet(MainActivity.this);
                Utility.ShippingNumber = prefs.getString("shipping_number", "");
                Utility.TrailerNumber = prefs.getString("trailer_number", "");
                DiagnosticMalfunction.getDiagnosticIndicator();
                CarrierInfoDB.getCompanyInfo();
                Utility.unIdentifiedDriverId = LoginDB.getUnidentifiedDriverId();
                //Utility.TimeZoneOffsetUTC = ZoneList.getTimeZoneOffset();
            }

            if (isLoginCall) {
                setDrawerState(false);
                toolbar.setVisibility(View.GONE);
                //flagBar.setVisibility(View.GONE);

                bLogin = true;
                previousScreen = -1;
                currentScreen = Login_Screen;
                if (loginFragment == null) {
                    loginFragment = new LoginFragment();
                }
                boolean codriver = bundle.getBoolean("CoDriverFg");
                Bundle bun = new Bundle();
                bun.putBoolean("CoDriverFg", codriver);
                loginFragment.setArguments(bun);
                replaceFragment(loginFragment);
            } else {
                //already login
                setDriverName();
                bLogin = false;
                int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
                Utility._appSetting = SettingsDB.getSettings(driverId);

                setDrawerState(true);

                bHaveUnAssignedEvent = false;
                ArrayList<EventBean> unAssignedEventList = EventDB.EventUnAssignedGet();
                if (unAssignedEventList.size() > 0) {
                    bHaveUnAssignedEvent = true;
                }

                bHaveLogbookToCertify = false;

                ArrayList<DailyLogBean> logList = DailyLogDB.getUncertifiedDailyLog(driverId);
                if (logList.size() > 0) {
                    bHaveLogbookToCertify = true;
                }

                isOnDailyLog = false;

                if (undockingMode) {
                    replaceFragment(new DockingFragment());
                    getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_eld));
                } else {
                    if (!bHaveUnAssignedEvent && !bHaveLogbookToCertify) {
                        //relogin
                        Log.d(TAG, "relogin");
                        if (elogFragment == null) {
                            elogFragment = new ELogFragment();
                        }
                        elogFragment.setFirstLogin(firstLogin);
                        replaceFragment(elogFragment);

                        isOnDailyLog = true;
                        previousScreen = -1;
                        currentScreen = DailyLog_Screen;
                        firstLogin = false;
                    } else {
                        if (bHaveUnAssignedEvent) {
                            replaceFragment(new UnidentifyFragment());
                            getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_unidentified_event));
                            isOnDailyLog = false;
                            bInspectDailylog = false;
                            previousScreen = -1;
                            currentScreen = Unidentified_Event_Screen;
                        } else {
                            replaceFragment(new UnCertifiedFragment());
                            getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_certify_log_book));
                            isOnDailyLog = false;
                            bInspectDailylog = false;
                            previousScreen = -1;
                            currentScreen = Uncertified_LogBook_Screen;

                        }
                    }
                }
            }


            bEventPowerOn = false;
            bEventPowerOff = false;
            bSaveDriving = false;

            title = "";

            hideFreezeLayout();

            frameDiagnostic = (FrameLayout) findViewById(R.id.frameDiagnostic);
            frameMalfunction = (FrameLayout) findViewById(R.id.frameMalfunction);

            tvLoginName = (TextView) findViewById(R.id.tvLoginName);
            tvLoginName.setText(driverName);
            tvFreezeLoginName = (TextView) findViewById(R.id.tvFreezeLoginName);
            tvFreezeLoginName.setText(driverName);
            chkRules = (CheckBox) findViewById(R.id.chkRules);
            chkRules.setClickable(false);

            bEditEvent = false;
            bWebEvent = false;

            //for synchonize data with server after period defined in SCHEDULER_TIME
            handler.postDelayed(autoSync, 50);

            textToSpeech = new TextToSpeech(Utility.context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {

                        textToSpeech.setLanguage(Locale.US);
                    }
                }
            });

            //handlerOD.postDelayed(runnableStatus, 5000);
            threadUpdateCANInfos = new Thread(runnableCAN);
            threadUpdateCANInfos.start();


            if (!prefs.getBoolean("undocking", false)) {
                //Log.d(TAG, "already docked!");

                if (ConstantFlag.PRODUCTION_BUILD) {
                    if (CanMessages.deviceAddress == null) {
                        //Log.d(TAG, "initializeBluetooth");
                        adapter = BluetoothAdapter.getDefaultAdapter();
                        initializeBluetooth();
                    }

                    initializeTpms();
               /*     else {
                        boolean firstRun;
                        if (getIntent() != null && getIntent().getExtras() != null) {
                            firstRun = getIntent().getExtras().getBoolean("firstrun", false);
                            if (firstRun) {
                                adapter = BluetoothAdapter.getDefaultAdapter();
                            }
                        }
                    }*/
                }
            }


            ponDutyChangeDialog = new PopupDialog();

            onDutyChangeDialogResponse = false;


            isDialogShown = false;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("E-Log");
            builder.setMessage("Do you wish to continue Personal Use?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener);

            dialog = builder.create();

            onDutyChangeBuilder = new AlertDialog.Builder(this);
            onDutyChangeBuilder.setMessage("Do you wish to continue driving? Otherwise please change status").setPositiveButton("OK", onDutyChangeDialogClickListener);

            initializeBTBAlertDialog();
            initializeGforce();
            alertMonitor = new AlertMonitor();
            alertMonitor.startAlertMonitor();

            // add sensorids to tpmsdata static variable int Tpms.java class. we only get data from sensorids of hooked trailer plus power unit
            VehicleDB.SensorInfoGet();
        } catch (Exception e) {
            LogFile.write(MainActivity.class.getName() + "::onCreate error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void setOrientation()
    {

        if (!Utility.isLargeScreen(getApplicationContext())) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.PORTRAIT.ordinal()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.LANSCAPE.ordinal()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.AUTO.ordinal()) {
                Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        }
    }

    AlertMonitor alertMonitor;
    LinearLayout layoutAlertBTB;
    TextView tvAlertHeader, tvAlertMessage;
    View vAlertBorder;
    ImageView imgAlertIcon;

    // purpose initialize dialog for BTB alert
    private void initializeBTBAlertDialog() {
        layoutAlertBTB = (LinearLayout) findViewById(R.id.layoutAlertBTB);
        imgAlertIcon = (ImageView) findViewById(R.id.imgIcon);
        vAlertBorder = findViewById(R.id.vBorder);
        tvAlertHeader = (TextView) findViewById(R.id.tvHeader);
        tvAlertMessage = (TextView) findViewById(R.id.tvMessage);
        if (CanMessages.mState != CanMessages.STATE_CONNECTED && !ConstantFlag.Flag_Development && !undockingMode) {
            onAlertWarning();
        }
    }

    @Override
    public void onAlertError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                imgAlertIcon.setImageResource(R.drawable.ic_error_outline_red_36dp);
                vAlertBorder.setBackgroundColor(getResources().getColor(R.color.red1));
                tvAlertHeader.setTextColor(getResources().getColor(R.color.red1));

                tvAlertHeader.setText("BTB Connection Failed");
                tvAlertMessage.setText("There is problem while connecting to BTB. Please restart Device to continue..");


                // set layout visible
                layoutAlertBTB.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onAlertWarning() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                imgAlertIcon.setImageResource(R.drawable.ic_warning_36dp);
                vAlertBorder.setBackgroundColor(getResources().getColor(R.color.yellow2));
                tvAlertHeader.setTextColor(getResources().getColor(R.color.yellow3));

                tvAlertHeader.setText("BTB Connection in progress");
                tvAlertMessage.setText("Please wait while ELD is connecting to BTB...");


                layoutAlertBTB.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public void onAlertVehicleStart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                imgAlertIcon.setImageResource(R.drawable.ic_warning_36dp);
                vAlertBorder.setBackgroundColor(getResources().getColor(R.color.yellow2));
                tvAlertHeader.setTextColor(getResources().getColor(R.color.yellow3));

                tvAlertHeader.setText("Turn On Ignition");
                tvAlertMessage.setText("Please turn on your ignition...");


                layoutAlertBTB.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onAlertClear() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutAlertBTB.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        isAppActive = true;

        //resumeGforce();

    }

    static boolean isAppActive = false;

    @Override
    public void onPause() {

        // pauseGforce();
        isAppActive = false;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
      /*  if (Utility.InspectorModeFg) {
            return;
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (undockingMode) {
                return;
            }

            //super.onBackPressed();
            if (previousScreen != -1) {
                if (currentScreen == Login_Screen) {
                    toolbar.setVisibility(View.VISIBLE);
                    flagBar.setVisibility(View.VISIBLE);
                    toggle.setDrawerIndicatorEnabled(true);
                }
                setDrawerState(true);
                currentScreen = previousScreen;
                if (previousScreen == Unidentified_Event_Screen) {
                    ArrayList<EventBean> unAssignedEventList = EventDB.EventUnAssignedGet();
                    if (unAssignedEventList.size() > 0) {
                        isOnDailyLog = false;
                        bInspectDailylog = false;
                        replaceFragment(UnidentifyFragment.newInstance());
                        title = getApplicationContext().getResources().getString(R.string.title_unidentified_event);

                        previousScreen = -1;
                    }
                    return;
                } else if (previousScreen == Message) {
                    fragmentManager.popBackStack();
                    previousScreen = -1;

                } else if (previousScreen == DVIR) {
                    fragmentManager.popBackStack();
                    previousScreen = -1;
                    getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_dvir));
                } else {
                    bLogin = false;
                    isOnDailyLog = true;
                    bInspectDailylog = false;
                    if (elogFragment == null) {
                        elogFragment = new ELogFragment();
                    }
                    replaceFragment(elogFragment);
                    title = getSupportActionBar().getTitle().toString();
                    previousScreen = -1;
                }
                getSupportActionBar().setTitle(title);
                invalidateOptionsMenu();

                drawer.closeDrawer(GravityCompat.START);
            } else {
                return;
            }

        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //MenuItem item = menu.findItem(R.id.action_set_active);
        //MenuItem editItem = menu.findItem(R.id.action_edit_event);
        //MenuItem backItem = menu.findItem(R.id.action_back_edit_event);
        MenuItem confirmItem = menu.findItem(R.id.action_confirm_web_event);
        MenuItem rejectItem = menu.findItem(R.id.action_reject_web_event);
      /*  MenuItem backOneDayItem = menu.findItem(R.id.action_back_one_day);
        MenuItem forwardOneDayItem = menu.findItem(R.id.action_forward_one_day);*/
        MenuItem malfunctionItem = menu.findItem(R.id.action_malfunction);
        MenuItem diagnosticItem = menu.findItem(R.id.action_diagnostic);
        MenuItem inspectorModeItem = menu.findItem(R.id.action_inspector_mode);
        MenuItem sendItem = menu.findItem(R.id.action_send);
        inspectorModeItem.setCheckable(false);
        inspectorModeItem.setVisible(Utility.InspectorModeFg);

        if (Utility.motionFg || ((Utility.user1.isActive() && Utility.user1.isOnScreenFg()) || (Utility.user2.isActive() && Utility.user2.isOnScreenFg()))) {
            //item.setIcon(R.drawable.ic_driver_active);
            //item.setEnabled(false);
            if (ivActiveUser != null) {
                ivActiveUser.setBackgroundResource(R.drawable.ic_flagbar_driver_active);
            }
            if (icFreezeActiveUser != null) {
                icFreezeActiveUser.setBackgroundResource(R.drawable.ic_flagbar_driver_active);
            }
            //item.setVisible(false);
        }

        if (Utility.dataDiagnosticIndicatorFg) {
            diagnosticItem.setVisible(true);
        } else {
            diagnosticItem.setVisible(false);
        }

        if (Utility.malFunctionIndicatorFg) {
            malfunctionItem.setVisible(true);
        } else {
            malfunctionItem.setVisible(false);
        }

        if (Utility.InspectorModeFg) {
            malfunctionItem.setVisible(false);
            diagnosticItem.setVisible(false);
            sendItem.setVisible(false);
        } else {
            sendItem.setVisible(true);
        }

        if (bEditEvent) {
            //editItem.setVisible(true);
            //editItem.setIcon(R.drawable.edit_button);
            //editItem.setEnabled(true);
            //backItem.setIcon(R.drawable.edit_button);
            //backItem.setVisible(true);
        /*    backOneDayItem.setVisible(false);
            forwardOneDayItem.setVisible(false);*/

            if (bWebEvent) {
                confirmItem.setVisible(true);
                rejectItem.setVisible(true);
                //editItem.setVisible(false);
                //backItem.setVisible(false);
            } else {
                confirmItem.setVisible(false);
                rejectItem.setVisible(false);
            }
            //item.setVisible(false);
        } else {
            confirmItem.setVisible(false);
            rejectItem.setVisible(false);
            //editItem.setVisible(false);
            //backItem.setVisible(false);

           /* if (bInspectDailylog) {
                backOneDayItem.setVisible(true);
                forwardOneDayItem.setVisible(true);
                //item.setVisible(false);
            } else {
                backOneDayItem.setVisible(false);
                forwardOneDayItem.setVisible(false);
                //item.setVisible(true);
            }*/

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.home) {
            //Log.d(TAG, "Works here 1!");
        } else if (id == R.id.homeAsUp) {
            //Log.d(TAG, "Works here 2!");
        } else if (id == R.id.action_diagnostic) {
            final Dialog dlg = new Dialog(MainActivity.this);
            LayoutInflater li = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = li.inflate(R.layout.listview_dialog, null, false);
            dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlg.setContentView(view);

            TextView tvTitle = (TextView) dlg.findViewById(R.id.tvTitle);
            tvTitle.setText("Data Diagnostic");

            ImageButton imgCancel = (ImageButton) dlg.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlg.dismiss();
                }
            });

            ListView listView = (ListView) dlg.findViewById(R.id.lvDiagnosticMalfunctionEvent);
            DiagnosticMalfunctionAdapter eventAdapter = new DiagnosticMalfunctionAdapter(MainActivity.this, DiagnosticMalfunction.getActiveDiagnosticMalfunctionList(3)); // code 3 means diagnostic
            listView.setAdapter(eventAdapter);
            dlg.show();
        } else if (id == R.id.action_malfunction) {
            final Dialog dlg = new Dialog(MainActivity.this);
            LayoutInflater li = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = li.inflate(R.layout.listview_dialog, null, false);
            dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dlg.setContentView(view);

            TextView tvTitle = (TextView) dlg.findViewById(R.id.tvTitle);
            tvTitle.setText("Malfunction");

            ImageButton imgCancel = (ImageButton) dlg.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlg.dismiss();
                }
            });
            ListView listView = (ListView) dlg.findViewById(R.id.lvDiagnosticMalfunctionEvent);
            DiagnosticMalfunctionAdapter eventAdapter = new DiagnosticMalfunctionAdapter(MainActivity.this, DiagnosticMalfunction.getActiveDiagnosticMalfunctionList(1));// code 1 means malfunction
            listView.setAdapter(eventAdapter);
            dlg.show();
        } else if (id == R.id.action_reject_web_event) {
            if (undockingMode) {
                return super.onOptionsItemSelected(item);
            }
            bEditEvent = false;
            bWebEvent = false;
            invalidateOptionsMenu();
            int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            int eventId = selectedEvent.get_id();

            EventDB.EventUpdate(eventId, 4, driverId, selectedEvent.getDailyLogId());
        } else if (id == R.id.action_confirm_web_event) {
            if (undockingMode) {
                return super.onOptionsItemSelected(item);
            }
            bEditEvent = false;
            bWebEvent = false;
            invalidateOptionsMenu();

            int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();

            int eventId = EventDB.getEventId(driverId, selectedEvent.getCreatedDate(), 1);
            EventDB.EventUpdate(eventId, 2, driverId, selectedEvent.getDailyLogId());

            eventId = selectedEvent.get_id();
            //  EventDB.EventCopy(eventId, bean.getEventRecordOrigin(), 1, driverId, bean.getDailyLogId());
            EventDB.EventUpdate(eventId, 1, driverId, selectedEvent.getDailyLogId());
            DailyLogDB.DailyLogCertifyRevert(driverId, selectedEvent.getDailyLogId());
        } else if (id == R.id.action_send) {
            if (undockingMode) {
                return super.onOptionsItemSelected(item);
            }

            outputFileDialog = new OutputFileSendDialog();
            outputFileDialog.mListener = this;
            outputFileDialog.show(getSupportFragmentManager(), "outputfile_dialog");
        } /*else if (id == R.id.action_motion) {
            if (CanMessages.Speed == "0") {
                Log.d(TAG, "Moving");
                Utility.motionFg = true;
                CanMessages.Speed = "80";
                CanMessages.RPM = "1000";
            } else {
                Log.d(TAG, "STOP");
                Utility.motionFg = false;
                CanMessages.Speed = "0";
            }
        } else if (id == R.id.action_PowerOnOff) {
            if (CanMessages.RPM == "0") {
                Log.d(TAG, "Power On");
                CanMessages.Speed = "0";
                Utility.motionFg = false;
                CanMessages.RPM = "1000";
            } else {
                Log.d(TAG, "Power Off");
                CanMessages.Speed = "0";
                CanMessages.RPM = "0";
                Utility.motionFg = false;
            }
        }*/
        /* else if (id == R.id.action_change_rule) {
            if (isOnDailyLog) {
                ELogFragment.newInstance().launchRuleChange();
            }
        } else if (id == R.id.action_certify) {
            if (isOnDailyLog) {
                ELogFragment.newInstance().launchCertifyLog();
            }
        }*/


        return super.onOptionsItemSelected(item);
    }

    private void callActive() {
        final AlertDialog ad = new AlertDialog.Builder(this)
                .create();
        String message = "Are you sure you want to be in Driving Mode?";
        String title = "Driving Mode Confirmation";
        if (Utility.motionFg) {
            message = "You cannot be an Active Driver when vehicle is in motion!";
            title = "Active Driver";
        }
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle(title);
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage(message);
        if (Utility.motionFg) {
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, "OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            ad.cancel();
                        }
                    });
        } else {
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {


                            setActive();
                        }
                    });
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            ad.cancel();
                        }
                    });
        }
        ad.show();
    }

    // set driver active
    private void setActive() {
        if (Utility.user2.getAccountId() > 0 && !Utility.motionFg) {
            // switch driver status
            Utility.user1.setActive(!Utility.user1.isActive());
            Utility.user2.setActive(!Utility.user2.isActive());
            Utility.activeUserId = Utility.user1.isActive() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            getSharedPreferences("HutchGroup", MODE_PRIVATE).edit().putInt("activeuserid", Utility.activeUserId).commit();
            updateActiveIcon();

            // if matchine is on check fuel economy
            if (Float.valueOf(CanMessages.RPM) > 0f) {
                AlertMonitor.FuelEconomyViolationGet();

                // switch driver case to get fuel economy per driver
                Utility.OdometerReadingStart = CanMessages.OdometerReading;
                Utility.savePreferences("OdometerReadingStart", Utility.OdometerReadingStart);


                // switch driver case to get fuel economy per driver
                Utility.FuelUsedStart = CanMessages.TotalFuelConsumed;
                Utility.savePreferences("FuelUsedStart", Utility.FuelUsedStart);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.daily_log) {
            if (undockingMode) {
                replaceFragment(new DockingFragment());
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
            isOnDailyLog = true;
            bInspectDailylog = false;
            if (elogFragment == null) {
                elogFragment = new ELogFragment();
            }
            replaceFragment(elogFragment);
            title = getSupportActionBar().getTitle().toString();
            previousScreen = currentScreen;
            currentScreen = DailyLog_Screen;
            // Handle the camera action
        } else if (id == R.id.inspect_dailylog) {
            if (undockingMode) {
                return true;
            }
            bInspectDailylog = true;
            bEditEvent = false;
            isOnDailyLog = false;
           /* if (inspectFragment == null) {
                inspectFragment = new DetailFragment();
            }*/

            replaceFragment(InspectLogFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = Inspect_DailyLog_Screen;
            title = getApplicationContext().getResources().getString(R.string.title_inspect_elog);
            title += Utility.getStringCurrentDate();
        } else if (id == R.id.create_event) {
            if (undockingMode) {
                return true;
            }
            Bundle bundle = new Bundle();
            bundle.putInt("current_status", currentDutyStatus);

            NewEventFragment fragment = NewEventFragment.newInstance();
            fragment.setArguments(bundle);
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(fragment);
            previousScreen = currentScreen;
            currentScreen = New_Event_Screen;
            title = getApplicationContext().getResources().getString(R.string.menu_create_event);
        } else if (id == R.id.violation_history) {
            if (undockingMode) {
                return true;
            }
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(ViolationFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = Violation_History_Screen;
            title = getApplicationContext().getResources().getString(R.string.menu_violation_history);
        } else if (id == R.id.uncertified_logbook) {
            if (undockingMode) {
                return true;
            }
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(UnCertifiedFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = Uncertified_LogBook_Screen;
            title = getApplicationContext().getResources().getString(R.string.title_certify_log_book);
        } else if (id == R.id.Unidentified) {
            if (undockingMode) {
                return true;
            }
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(UnidentifyFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = Unidentified_Data_Screen;
            title = getApplicationContext().getResources().getString(R.string.menu_unidentified_event);
        } else if (id == R.id.edit_request) {
            if (undockingMode) {
                return true;
            }
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(ModifiedFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = Edit_Request_Screen;
            title = getApplicationContext().getResources().getString(R.string.menu_edit_request);
        } else if (id == R.id.send_report) {
            LogFile.sendLogFile(LogFile.AFTER_MID_NIGHT);

        } else if (id == R.id.dvir) {
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(DvirFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = DVIR;
            title = getApplicationContext().getResources().getString(R.string.menu_dvir);
        } else if (id == R.id.message) {
            isOnDailyLog = false;
            bInspectDailylog = false;
            replaceFragment(UserListFragment.newInstance());
            previousScreen = currentScreen;
            currentScreen = Message;
            title = getApplicationContext().getResources().getString(R.string.menu_Message);
        } else if (id == R.id.logout) {
            if (!Utility.InspectorModeFg) {
                int currentStatus = EventDB.getCurrentDutyStatus(Utility.onScreenUserId);
                if (currentStatus != 1) {
                    Utility.showAlertMsg("you are not allowed to logout until you are in OFF DUTY");
                    return true;
                }
            }

            boolean unCertifyFg = DailyLogDB.getUncertifiedLogFg(Utility.onScreenUserId);
            String message = "";

            if (unCertifyFg && !Utility.InspectorModeFg) {
                message = "You have Uncertified Logs!!!";
            }
            final AlertDialog ad = new AlertDialog.Builder(this)
                    .create();
            ad.setCancelable(true);
            ad.setCanceledOnTouchOutside(false);
            ad.setTitle("Logout Confirmation");
            ad.setIcon(R.drawable.ic_launcher);
            ad.setMessage("Are you sure you want to Logout? " + message);
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            isOnDailyLog = false;
                            if (Utility.InspectorModeFg) {
                                RedirectToLogin(Utility.user2.isOnScreenFg());
                                setInspectorMode(false);
                            } else
                                Logout();
                        }
                    });
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            ad.cancel();
                        }
                    });
            ad.show();
        }

        invalidateOptionsMenu();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateTitle = true;
        tvFreeze.setBackgroundResource(0);
        if (tvFreeze != null) {
            if (Utility.activeUserId == 0)
                tvFreeze.setBackgroundResource(R.drawable.login_freeze);
            else {
                setUIMode();
            }
        }

    }

    boolean updateTitle = true;

    private void replaceFragment(Fragment fragment) {

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            updateTitle = false;
            if (currentScreen == Inspect_DailyLog_Screen || currentScreen == DailyLog_Screen) {
                updateTitle = true;
            }

            try {
                Utility.hideKeyboard(MainActivity.this, MainActivity.this.getCurrentFocus());
            } catch (Exception exe) {
            }
        }

        FragmentManager manager = fragmentManager;
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.commit();
    }

    @Override
    public void setDutyStatus(int status) {
        currentDutyStatus = status;
        if (Utility.onScreenUserId == Utility.activeUserId) {
            activeCurrentDutyStatus = status;
            if (status != 3) {
                bSaveDriving = false;
            }
        }
    }

    @Override
    public void setActiveDutyStatus(int status) {
        activeCurrentDutyStatus = status;
        if (status != 3) {
            bSaveDriving = false;
        }
        //Log.d(TAG, "setActiveDutyStatus activeCurrentDutyStatus=" + activeCurrentDutyStatus);
    }

    @Override
    public void updateTitle(String date) {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getApplicationContext().getResources().getString(R.string.title_inspect_elog));
    }

    @Override
    public void setTotalDistance(int total) {
        totalDistance = total;
    }

    @Override
    public void setStartOdoMeter(int odo) {
        startOdometer = odo;
    }

    @Override
    public void setStartEngineHour(int value) {
        startHourEngine = value;
    }

    @Override
    public void onEditEventSaved() {
        if (bInspectDailylog) {

            loadInspectDailylog();
        } else {
            loadDailyLog();
        }
    }

    @Override
    public void onEditEventFinished() {
        if (bInspectDailylog) {
            loadInspectDailylog();
        } else {
            loadDailyLog();
        }
    }

    @Override
    public void onNewEventSaved() {
        //complete Create Event -> load ElogFragment
        loadDailyLog();
    }

    @Override
    public void onNewEventFinished() {
        //complete Create Event -> load ElogFragment
        loadDailyLog();
    }

    @Override
    public void onAssumeRecord() {
        if (firstLogin) {
            //Log.d(TAG, "first login");
            if (bHaveLogbookToCertify) {
                replaceFragment(new UnCertifiedFragment());
                previousScreen = Unidentified_Event_Screen;
                currentScreen = Uncertified_LogBook_Screen;
                getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_certify_log_book));
            } else {
                //Log.d(TAG, "relogin");
                if (elogFragment == null) {
                    elogFragment = new ELogFragment();
                }
                elogFragment.setFirstLogin(firstLogin);
                replaceFragment(elogFragment);

                previousScreen = Unidentified_Event_Screen;
                currentScreen = DailyLog_Screen;
                isOnDailyLog = true;
                firstLogin = false;
            }
        } else {
            //relogin
            //Log.d(TAG, "relogin");
            if (elogFragment == null) {
                elogFragment = new ELogFragment();
            }
            replaceFragment(elogFragment);

            previousScreen = Unidentified_Event_Screen;
            currentScreen = DailyLog_Screen;
            isOnDailyLog = true;
        }

    }

    @Override
    public void onSkipAssumeRecord() {

        if (firstLogin) {
            //Log.d(TAG, "first login");
            if (bHaveLogbookToCertify) {
                replaceFragment(new UnCertifiedFragment());
                previousScreen = Unidentified_Event_Screen;
                currentScreen = Uncertified_LogBook_Screen;
                getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_certify_log_book));
            } else {
                //Log.d(TAG, "relogin");
                if (elogFragment == null) {
                    elogFragment = new ELogFragment();
                }
                elogFragment.setFirstLogin(firstLogin);
                replaceFragment(elogFragment);
                previousScreen = Unidentified_Event_Screen;
                currentScreen = DailyLog_Screen;
                isOnDailyLog = true;
                firstLogin = false;
            }
        } else {
            //relogin
            //Log.d(TAG, "relogin");
            if (elogFragment == null) {
                elogFragment = new ELogFragment();
            }
            replaceFragment(elogFragment);
            previousScreen = Unidentified_Event_Screen;
            currentScreen = DailyLog_Screen;
            isOnDailyLog = true;
        }


    }

    @Override
    public void onLogbookCertified() {
        //relogin
        //Log.d(TAG, "relogin");
        if (elogFragment == null) {
            elogFragment = new ELogFragment();
        }
        elogFragment.setFirstLogin(firstLogin);
        replaceFragment(elogFragment);
        previousScreen = Uncertified_LogBook_Screen;
        currentScreen = DailyLog_Screen;

        isOnDailyLog = true;
        firstLogin = false;
    }

    @Override
    public void onSkipCertify() {
        //relogin
        //Log.d(TAG, "relogin");
        if (elogFragment == null) {
            elogFragment = new ELogFragment();
        }
        elogFragment.setFirstLogin(firstLogin);
        replaceFragment(elogFragment);
        previousScreen = Uncertified_LogBook_Screen;
        currentScreen = DailyLog_Screen;

        isOnDailyLog = true;
        firstLogin = false;
    }

    private void loadDailyLog() {
        //fragmentManager.beginTransaction()
        //        .replace(R.id.container, ELogFragment.newInstance())
        //        .commit();

        isOnDailyLog = true;
        bInspectDailylog = false;
        if (elogFragment == null) {
            elogFragment = new ELogFragment();
        }
        firstLogin = false;
        elogFragment.setFirstLogin(firstLogin);
        replaceFragment(elogFragment);
        previousScreen = currentScreen;
        currentScreen = DailyLog_Screen;
        title = getApplicationContext().getResources().getString(R.string.title_daily_log);
        title += " - " + Utility.convertDate(Utility.newDate(), CustomDateFormat.d10);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    private void loadInspectDailylog() {
        bInspectDailylog = true;
        bEditEvent = false;
        isOnDailyLog = false;
        /*if (inspectFragment == null) {
            inspectFragment = new DetailFragment();
        }*/
        InspectLogFragment inspectFragment = InspectLogFragment.newInstance();
        // set date if previous date event edited from inspect dailylog
        if (selectedEvent != null) {
            invalidateOptionsMenu();
            int position = Utility.getDiffDay(selectedEvent.getEventDateTime(), Utility.getCurrentDateTime()); //Utility.dateOnlyGet(selectedEvent.getEventDateTime());
            inspectFragment = InspectLogFragment.newInstance(position);
        }
        replaceFragment(inspectFragment);
        previousScreen = currentScreen;
        currentScreen = Inspect_DailyLog_Screen;
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_inspect_elog));
    }

    // Created By: Deepak Sharma
    // Created Date: 11 August 2016
    // Purpose: logout all user before install
    private void LogoutAllOnInstall() {
        int driverId = Utility.user1.getAccountId();
        int coDriverId = Utility.user2.getAccountId();

        // logout coDriver if any
        if (coDriverId != 0) {
            Logout(coDriverId);
            Utility.user2 = new UserBean();
        }

        // logout driver if any
        if (driverId != 0) {
            Logout(driverId);
            Utility.user1 = new UserBean();
        }

        Utility.activeUserId = Utility.onScreenUserId = 0;
        // disconnect chat server when both of the users are offline
        ChatClient.disconnect();
        finish();
    }

    // Created By: Deepak Sharma
    // Created Date: 11 August 2016
    // Purpose: logout user set common cached user data manually after executing this method
    private void Logout(int userId) {

        // make  driver offline on chat server
        MessageBean bean = MessageDB.CreateMessage(Utility.IMEI, userId, userId, "Disconnect");
        MessageDB.Send(bean);

        int logId = DailyLogDB.getDailyLog(userId, Utility.getCurrentDate());

        // get co driver record primary key
        int cId = DailyLogDB.getCoDriver(userId);
        if (cId > 0) {
            DailyLogDB.AddDriver(0, 0, cId);
        }

        EventDB.EventCreate(Utility.getCurrentDateTime(), 5, 2, "Authenticated Driver's ELD logout activity", 1, 1, logId, userId, "");
    }

    private void Logout() {

        // make  driver offline on chat server
        MessageBean bean = MessageDB.CreateMessage(Utility.IMEI, Utility.onScreenUserId, Utility.onScreenUserId, "Disconnect");
        MessageDB.Send(bean);

        int driverId = Utility.user1.getAccountId();


        // get co driver record primary key
        int cId = DailyLogDB.getCoDriver(driverId);
        if (cId > 0) {
            DailyLogDB.AddDriver(0, 0, cId);
        }

        // create event on logout
        driverId = Utility.onScreenUserId;

        int logId = DailyLogDB.getDailyLog(driverId, Utility.getCurrentDate());
        EventDB.EventCreate(Utility.getCurrentDateTime(), 5, 2, "Authenticated Driver's ELD logout activity", 1, 1, logId, driverId, "");

        if (Utility.user1.isOnScreenFg()) {
            if (Utility.user2.getAccountId() > 0) {
                Utility.user1 = Utility.user2;
                Utility.user1.setActive(true);
                Utility.user1.setOnScreenFg(true);
                Utility.activeUserId = Utility.onScreenUserId = Utility.user1.getAccountId();
                Utility.user2 = new UserBean();
                RedirectToMain();
                Utility.saveLoginInfo(Utility.user1.getAccountId(), 0, Utility.activeUserId, Utility.onScreenUserId);
            } else {
                Utility.user1 = new UserBean();
                Utility.activeUserId = Utility.onScreenUserId = 0;
                RedirectToLogin(false);
                // disconnect chat server when both of the users are offline
                ChatClient.disconnect();
                Utility.saveLoginInfo(0, 0, Utility.activeUserId, Utility.onScreenUserId);
            }
        } else {
            Utility.user2 = new UserBean();
            Utility.user1.setActive(true);
            Utility.user1.setOnScreenFg(true);
            Utility.activeUserId = Utility.onScreenUserId = Utility.user1.getAccountId();
            RedirectToMain();
            Utility.saveLoginInfo(Utility.user1.getAccountId(), 0, Utility.activeUserId, Utility.onScreenUserId);
        }

        if (Utility.isInternetOn()) {
            showLoaderAnimation(true);
            new AutoSyncData(autoSyncDataPostTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    private void RedirectToMain() {
        loginSuccessfully(false);
    }

    private void RedirectToLogin(boolean coDriver) {
        elogFragment = null;
        setDrawerState(false);
        toolbar.setVisibility(View.GONE);
        //   flagBar.setVisibility(View.GONE);

        if (coDriver) {
            previousScreen = DailyLog_Screen;
        } else {
            previousScreen = -1;
        }
        currentScreen = Login_Screen;

        isOnDailyLog = false;
        bInspectDailylog = false;


        loginFragment = new LoginFragment();

        Bundle bun = new Bundle();
        bun.putBoolean("CoDriverFg", coDriver);
        bun.putBoolean("InspectorModeFg", Utility.InspectorModeFg);
        loginFragment.setArguments(bun);
        replaceFragment(loginFragment);
    }

    // Switch driver
    private void SwitchDriver() {
        RedirectToLogin(Utility.user1.isOnScreenFg());
    }

    public void onFragmentInteraction(Uri id) {
        //you can leave it empty
    }

    private static int currentRule = 1;

    //Implement method of ELogMainActivity
    @Override
    public void freezeLayout() {
        if (ScoreCardFragment.IsTesting)
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvFreeze.setVisibility(View.GONE);
                tvLoginFreeze.setVisibility(View.GONE);
                setDrawerState(true);
                if (Utility.motionFg) {

                    toolbar.setVisibility(View.GONE);
                    flagBar.setVisibility(View.GONE);
                    if (Utility.user1.getAccountId() == 0 && Utility.user2.getAccountId() == 0) {
                        //login screen
                        tvLoginFreeze.setVisibility(View.VISIBLE);
                        tvGauge.setVisibility(View.INVISIBLE);
                        tvFreeze.setVisibility(View.VISIBLE);
                        tvFreeze.setBackgroundResource(R.drawable.login_freeze);


                        flagBar.setVisibility(View.VISIBLE);
                        setDrawerState(false);
                    } else if (Utility.user1.isOnScreenFg() && Utility.user1.isActive()) {
                        // freeze activity
                        //Log.d(TAG, "freeze activity");
                        tvLoginFreeze.setVisibility(View.GONE);
                        tvGauge.setVisibility(View.VISIBLE);
                        tvFreeze.setVisibility(View.VISIBLE);
                        tvFreeze.setBackgroundResource(R.drawable.freeze_background);

                        setDrawerState(false);
                    } else if (Utility.user2.isOnScreenFg() && Utility.user2.isActive()) {
                        // freeze activity
                        tvLoginFreeze.setVisibility(View.GONE);
                        tvGauge.setVisibility(View.VISIBLE);
                        tvFreeze.setVisibility(View.VISIBLE);
                        tvFreeze.setBackgroundResource(R.drawable.freeze_background);

                        setDrawerState(false);
                    } else {
                        tvLoginFreeze.setVisibility(View.GONE);
                        tvGauge.setVisibility(View.VISIBLE);
                        tvFreeze.setVisibility(View.VISIBLE);
                        tvFreeze.setBackgroundResource(R.drawable.freeze_background);

                        setDrawerState(false);
                    }
                }
            }
        });
    }

    @Override
    public void hideFreezeLayout() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvFreeze.setBackgroundResource(R.drawable.freeze_background);
                tvDrivingRemainingFreeze.setBackgroundResource(R.drawable.remaining_driving_hours_bg);
                tvFreeze.setVisibility(View.GONE);
                tvLoginFreeze.setVisibility(View.GONE);
                tvGauge.setVisibility(View.INVISIBLE);

                if (currentScreen != Login_Screen) {
                    toolbar.setVisibility(View.VISIBLE);
                    flagBar.setVisibility(View.VISIBLE);
                } else {
                    flagBar.setVisibility(View.VISIBLE);
                }
                setDrawerState(true);
            }
        });
    }

    @Override
    public void machineOn() {
        //save event
        if (!bEventPowerOn) {


            int evenCode = 1;
            String description = "Engine power-up with conventional location precision";
            if (activeCurrentDutyStatus == 5 || activeCurrentDutyStatus == 6) {
                evenCode = 2;
                description = "Engine power-up with reduced location precision";
            }

            int driverId = 0;
            if (Utility.user1.isOnScreenFg() && Utility.user1.isActive()) {
                driverId = Utility.user1.getAccountId();
            } else if (Utility.user2.isOnScreenFg() && Utility.user2.isActive()) {
                driverId = Utility.user2.getAccountId();
            } else {
                driverId = Utility.activeUserId;
            }

            int logId = DailyLogDB.DailyLogCreate(driverId, Utility.ShippingNumber, Utility.TrailerNumber, "");

            //123 LogFile.write(MainActivity.class.getName() + "::machineOn: " + description + " of driverId:" + driverId, LogFile.USER_INTERACTION, LogFile.DRIVEREVENT_LOG);
            EventDB.EventCreate(Utility.getCurrentDateTime(), 6, evenCode, description, 1, 1, logId, driverId, "");
            bEventPowerOn = true;
            bEventPowerOff = false;
            //save vehicle Data
            VehicleInfoDB.Save(CanMessages._vehicleInfo);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (elogFragment != null)
                        elogFragment.updateOdometer();
                }
            });
        }

        if (Utility.statusFlag == 1) {
            if (Utility.powerOnOff == 0)
                Utility.powerOnOff = 1;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkStatus(true);
            }
        });
    }

    @Override
    public void machineOff() {
        //save event
        int driverId = 0;
        if (Utility.user1.isOnScreenFg() && Utility.user1.isActive()) {
            driverId = Utility.user1.getAccountId();
        } else if (Utility.user2.isOnScreenFg() && Utility.user2.isActive()) {
            driverId = Utility.user2.getAccountId();
        } else {
            driverId = Utility.activeUserId;
        }
        int logId = DailyLogDB.DailyLogCreate(driverId, Utility.ShippingNumber, Utility.TrailerNumber, "");
        if (!bEventPowerOff) {
            Utility.NightModeFg = false;


            int evenCode = 3;
            String description = "Engine shut down with conventional location precision";
            if (activeCurrentDutyStatus == 5 || activeCurrentDutyStatus == 6) {
                evenCode = 4;
                description = "Engine shut down with reduced location precision";
            }

            //123 LogFile.write(MainActivity.class.getName() + "::machineOff: " + description + " of driverId:" + driverId, LogFile.USER_INTERACTION, LogFile.DRIVEREVENT_LOG);
            EventDB.EventCreate(Utility.getCurrentDateTime(), 6, evenCode, description, 1, 1, logId, driverId, "");

            //save vehicle Data
            VehicleInfoDB.Save(CanMessages._vehicleInfo);

            bEventPowerOff = true;
            bEventPowerOn = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setUIMode();
                    if (elogFragment != null)
                        elogFragment.updateOdometer();
                }
            });

        }

        if (Utility.statusFlag == 1) {
            Utility.powerOnOff = 0;
        }

        if (activeCurrentDutyStatus == 6) {
            Utility.statusFlag = 0;
            //123 LogFile.write(MainActivity.class.getName() + "::machineOff: " + "Clear PC, YM and WT" + " of driverId:" + driverId, LogFile.USER_INTERACTION, LogFile.DRIVEREVENT_LOG);
            EventDB.EventCreate(Utility.getCurrentDateTime(), 3, 0, "Driver Indication for PC, YM and WT cleared", 1, 1, logId, driverId, "");
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkStatus(false);
            }
        });
    }

    @Override
    public void saveIntermediateLog() {
        int evenCode = 1;
        String description = "Intermediate log with conventional location precision";
        if (activeCurrentDutyStatus == 5 || activeCurrentDutyStatus == 6) {
            evenCode = 2;
            description = "Intermediate log with reduced location precision";
        }
        int driverId = 0;
        if (Utility.user1.isOnScreenFg() && Utility.user1.isActive()) {
            driverId = Utility.user1.getAccountId();
        } else if (Utility.user2.isOnScreenFg() && Utility.user2.isActive()) {
            driverId = Utility.user2.getAccountId();
        } else {
            driverId = Utility.activeUserId;
        }

        int logId = DailyLogDB.DailyLogCreate(driverId, Utility.ShippingNumber, Utility.TrailerNumber, "");

        EventDB.EventCreate(Utility.getCurrentDateTime(), 2, evenCode, description, 1, 1, logId, driverId, "");
    }

    @Override
    public void autoChangeStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Utility.user1.isOnScreenFg() && Utility.user1.isActive()) {
                    autoChangeDutyStatus();
                } else if (Utility.user2.isOnScreenFg() && Utility.user2.isActive()) {
                    autoChangeDutyStatus();
                } else {
                    //auto save for active driver
                    if (!bSaveDriving) {
                        activeCurrentDutyStatus = 3;
                        int logId = DailyLogDB.DailyLogCreate(Utility.activeUserId, Utility.ShippingNumber, Utility.TrailerNumber, "");
                        EventDB.EventCreate(Utility.getCurrentDateTime(), 1, 3, "Driver's Duty Status changed to DRIVING", 1, 1, logId, Utility.activeUserId, "");
                        bSaveDriving = true;
                    }
                }
            }
        });
    }

    @Override
    public void resetFlag() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resetOnDutyChangeDialogResponse();
            }
        });
    }

    @Override
    public void setTitle(final String title) {
        if (!updateTitle) {
            updateTitle = true;
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    getSupportActionBar().setTitle(title);
                } catch (Exception exe) {
                }
            }
        });
    }

    //End Implement method of ELogMainActivity

    Runnable runnableCAN = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (Utility.motionFg) {
                        updateCanInformation();
                    }
                    onUpdateCanbusIcon(CanMessages.mState);
                } catch (Exception e) {
                    Log.d(TAG, "ERROR update CAN: " + e.getMessage());
                    break;
                }
            }
        }
    };

    private void updateCanInformation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float voltage = Float.parseFloat(CanMessages.Voltage);
                float speed = Float.parseFloat(CanMessages.Speed);
                Double odometerReading = Double.parseDouble(CanMessages.OdometerReading);
                float coolantTemp = Float.parseFloat(CanMessages.CoolantTemperature);
                String speedUnit = " km/h";
                String distanceUnit = " Kms";
                String tempUnit = " ° F";
                if (Utility._appSetting.getUnit() == 2) {
                    odometerReading = odometerReading * .62137d;
                    speed = speed * .62137f;
                    speedUnit = " MPH";
                    distanceUnit = " Miles";
                } else {
                    coolantTemp = ((coolantTemp - 32) * 5) / 9;
                    tempUnit = "° C";
                }
                // set rotations
                setSpeedRoatation(speed);
                setRPMRoatation();
                setThrottleRoatation(Float.parseFloat(CanMessages.Boost));
                setCoolantRoatation(coolantTemp);
                if (voltage < 48f)
                    setVoltageRoatation(voltage);

                if (frameDiagnostic != null) {
                    if (Utility.dataDiagnosticIndicatorFg) {
                        frameDiagnostic.setVisibility(View.VISIBLE);
                    } else {
                        frameDiagnostic.setVisibility(View.GONE);
                    }
                }

                if (frameMalfunction != null) {
                    if (Utility.malFunctionIndicatorFg) {
                        frameMalfunction.setVisibility(View.VISIBLE);
                    } else {
                        frameMalfunction.setVisibility(View.GONE);
                    }
                }

                // set label
                if (tvSpeed != null)
                    tvSpeed.setText(Math.round(speed) + speedUnit);

                DecimalFormat df = new DecimalFormat("#######.#");
                //String odoText = df.format(odo);
                String odoText = String.format("%.1f", odometerReading);
                //Log.d(TAG, "odo=" + odoText);
                odoText = odoText.replace(".", "");

                String zero = "";
                if (odoText.length() < 8) {
                    for (int i = odoText.length(); i < 8; i++) {
                        zero += "0";
                    }
                }
                odoText = zero + odoText;

                if (tvOdometer != null)
                    tvOdometer.setText(odoText);

                int rpm = Math.round(Float.parseFloat(CanMessages.RPM));
                if (tvRPM != null)
                    tvRPM.setText(rpm + "");

                float engineHrs = Float.parseFloat(CanMessages.EngineHours);
                //String engineText = df.format(engineHrs);
                String engineText = String.format("%.1f", engineHrs);
                //Log.d(TAG, "engineHrs=" + engineText);
                engineText = engineText.replace(".", "");
                zero = "";
                if (engineText.length() < 8) {
                    for (int i = engineText.length(); i < 8; i++) {
                        zero += "0";
                    }
                }
                engineText = zero + engineText;

                if (tvEngineHours != null)
                    tvEngineHours.setText(engineText);

                int boost = Math.round(Float.parseFloat(CanMessages.Boost));
                if (tvPosition != null)
                    tvPosition.setText(boost + " psi");

                int coolant = Math.round(coolantTemp);
                if (tvCoolant != null)
                    tvCoolant.setText(coolant + tempUnit);

                if (voltage < 48f) {
                    if (tvVoltage != null)
                        tvVoltage.setText(Float.parseFloat(CanMessages.Voltage) + " V");
                }

                if (tvDrivingRemainingFreeze != null && activeCurrentDutyStatus == 3 && ViolationDT != null) {
                    int secondsLeft = (int) (ViolationDT.getTime() - (Utility.newDate()).getTime()) / 1000;
                    if (secondsLeft < 0) {
                        secondsLeft = 0;
                    }

                    if (secondsLeft == 0) {
                        if (GPSData.NoHOSViolationFgFg == 1) {
                            onUpdateViolation(true);
                            GPSData.NoHOSViolationFgFg = 0;
                        }
                        tvDrivingRemainingFreeze.setBackgroundResource(R.drawable.remaining_driving_hours_bg_red);
                    } else if (secondsLeft <= 3600) {
                        tvDrivingRemainingFreeze.setBackgroundResource(R.drawable.remaining_driving_hours_bg_yellow);

                    }

                    String timeRemaining = Utility.getTimeFromSeconds(secondsLeft);
                    tvDrivingRemainingFreeze.setText(timeRemaining);
                    if (secondsLeft > 0) {
                        if (GPSData.NoHOSViolationFgFg == 0) {
                            onUpdateViolation(false);
                            GPSData.NoHOSViolationFgFg = 1;
                        }
                    }
                }
            }
        });
    }

    //imgreezeSpeed,imgFreezeRPM,imgFreezeVoltage,imgFreezeCoolantTemp,imgFreezeThrPos;
    private void setSpeedRoatation(float speed) {

        //if speed is 10 then angle is 15
        float angle = 15 * speed / 10;

        // float from = imgreezeSpeed.getRotation();
        imgreezeSpeed.setRotation(angle);
        //   android.animation.ObjectAnimator.ofFloat(imgreezeSpeed, "rotation", from, angle).start();

    }

    private void setRPMRoatation() {

        float RPM = Float.parseFloat(CanMessages.RPM);

        //if RPM is 1000 then angle is 60
        float angle = 60 * RPM / 1000;

        // float from = imgFreezeRPM.getRotation();
        imgFreezeRPM.setRotation(angle);
        // android.animation.ObjectAnimator.ofFloat(imgFreezeRPM, "rotation", from, angle).start();

    }


    private void setThrottleRoatation(float position) {

        //if position is 100 then angle is 180
        float angle = 180 * position / 100;

        // float from = imgFreezeThrPos.getRotation();
        imgFreezeThrPos.setRotation(angle);
        // android.animation.ObjectAnimator.ofFloat(imgFreezeThrPos, "rotation", from, angle).start();

    }


    private void setCoolantRoatation(float coolant) {

        //if voltage 200 then angle is 90
        float angle = (coolant - 100) * 90 / 100.0f;

        //  float from = imgFreezeCoolantTemp.getRotation();
        imgFreezeCoolantTemp.setRotation(angle);

        //android.animation.ObjectAnimator.ofFloat(imgFreezeCoolantTemp, "rotation", from, angle).start();

    }

    private void setVoltageRoatation(float voltage) {
        if (voltage > 48) {
            return;
        }

        // if voltage is 9 then angle is 0 and if 18 then 180
        float angle = (voltage - 8) * 18;

        // float from = imgFreezeVoltage.getRotation();
        imgFreezeVoltage.setRotation(angle);
        //android.animation.ObjectAnimator.ofFloat(imgFreezeVoltage, "rotation", from, angle).start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(autoSync);
        //handlerOD.removeCallbacks(runnableStatus);
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }

        this.unregisterReceiver(batteryInfoReceiver);
        this.unregisterReceiver(networkChangeReceiver);
        this.unregisterReceiver(gpsChangeReceiver);
        this.unregisterReceiver(dateChangedReceiver);

        if (threadUpdateCANInfos != null)
            threadUpdateCANInfos.interrupt();
        ELogApplication.objGps.stopUsingGPS();

        specialCategoryDialog = null;
        messageDialog = null;
        successDialog = null;
        dialogHandler.removeCallbacksAndMessages(null);

        // Utility.user1 = new UserBean();
        // Utility.user2 = new UserBean();
    }

    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_ENABLE_BT) {
                initializeBluetooth();
            }
        } catch (Exception e) {
            LogFile.write(LoginFragment.class.getName() + "::onActivityResult Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void editEvent(EventBean bean) {
        if (bean.getEventType() == 1 || bean.getEventType() == 3) {
            selectedEvent = bean;
            bEditEvent = false;
            if (bean.getEventRecordStatus() == 1) {
                bEditEvent = true;
            }
            bWebEvent = false;
            if (bean.getEventRecordOrigin() == 3 && bean.getEventRecordStatus() == 3) {
                bEditEvent = true;
                bWebEvent = true;
            }
            /*if (!Utility.isLargeScreen(getApplicationContext())) {

            } else {
                invalidateOptionsMenu();
            }*/
        }
    }

    @Override
    public void onEventEdited() {

        if (undockingMode || selectedEvent == null) {
            return;
        }
        invalidateOptionsMenu();
        Bundle bundle = new Bundle();

        int status = 1;
        int type = selectedEvent.getEventType();
        int code = selectedEvent.getEventCode();
        if (type == 1) {
            if (code == 1) {
                status = 1;
            } else if (code == 2) {
                status = 2;
            } else if (code == 3) {
                status = 3;
            } else if (code == 4) {
                status = 4;
            }
        } else if (type == 3) {
            if (code == 1) {
                status = 5;
            } else if (code == 2) {
                status = 6;
            }
        }

        bundle.putInt("current_status", status);
        bundle.putBoolean("edit_event", true);
        bundle.putSerializable("selected_event", selectedEvent);
        NewEventFragment fragment = new NewEventFragment();

        fragment.setArguments(bundle);
        replaceFragmentWithBackStack(fragment);
        prevTitle = getSupportActionBar().getTitle().toString();
        title = getApplicationContext().getResources().getString(R.string.title_edit_event);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle(title);
        bWebEvent = false;

        previousScreen = currentScreen;
        currentScreen = New_Event_Screen;
    }

    String prevTitle = "";

    @Override
    public void onDetailClosed() {
        loadDailyLog();
    }

    @Override
    public void onDetailSaved() {
        loadDailyLog();
    }

    @Override
    public void onDocking() {

        //Demo build
        SharedPreferences prefs = getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
        prefs.edit().putBoolean("undocking", false).commit();
        undockingMode = false;
        showLoaderAnimation(true);
        Thread thOnDocking = new Thread(new Runnable() {
            @Override
            public void run() {
                if (ConstantFlag.Flag_Development == false) {
                    if (CanMessages.deviceAddress == null) {
                        //Log.d(TAG, "initializeBluetooth");
                        adapter = BluetoothAdapter.getDefaultAdapter();
                        initializeBluetooth();
                    } else
                        connectDevice(true);
                    while (CanMessages.mState != CanMessages.STATE_CONNECTED) {
                        try {

                            Thread.sleep(1000);
                        } catch (InterruptedException exe) {

                        }

                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // start can heart beat
                        objCan.StartCanHB();
                        objTpms.StartTpmsHB();
                        loadDailyLog();
                        if (ConstantFlag.AUTOSTART_MODE) {
                            startService(new Intent(MainActivity.this, AutoStartService.class));
                        }
                        showLoaderAnimation(false);
                    }
                });
            }
        });
        thOnDocking.setName("th-OnDocking");
        thOnDocking.start();

    }

    @Override
    public void showBluetoothConnectionMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!bBluetoothConnecting) {
                    progressBarDialog = new ProgressDialog(MainActivity.this);
                    progressBarDialog.setIcon(R.drawable.ic_launcher);
                    progressBarDialog.setCancelable(true);
                    progressBarDialog.setCanceledOnTouchOutside(false);
                    progressBarDialog.setTitle("Connecting...");
                    progressBarDialog.setMessage(getResources().getString(R.string.bluetooth_connection_infos));
                    progressBarDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                    progressBarDialog.show();
                    bBluetoothConnecting = true;
                }
            }
        });

    }

    @Override
    public void showConnectionSuccessfull() {
        if (!bBluetoothConnectionSuccess) {
            progressBarDialog.dismiss();

            final AlertDialog ad = new AlertDialog.Builder(this).create();
            ad.setCancelable(true);
            ad.setCanceledOnTouchOutside(false);
            ad.setIcon(R.drawable.ic_lanucher);
            ad.setTitle("E-Log");
            ad.setMessage(getResources().getString(R.string.bluetooth_connection_ok));
            ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ad.cancel();

                            //if in undocking mode, call login screen
                            SharedPreferences prefs = getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
                            if (prefs.getBoolean("undocking", false)) {
                                //turn off undocking mode
                                prefs.edit().putBoolean("undocking", false).commit();
                                undockingMode = false;

                                if (elogFragment == null) {
                                    elogFragment = new ELogFragment();
                                }
                                replaceFragment(elogFragment);
                                if (ConstantFlag.AUTOSTART_MODE) {
                                    startService(new Intent(MainActivity.this, AutoStartService.class));
                                }
                            }
                        }
                    });
            ad.show();
            bBluetoothConnectionSuccess = true;
        }
    }

    @Override
    public void showConnectionError() {
        if (!bBluetoothConnectionError) {
            progressBarDialog.dismiss();

            final AlertDialog ad = new AlertDialog.Builder(this).create();
            ad.setCancelable(true);
            ad.setCanceledOnTouchOutside(false);
            ad.setIcon(R.drawable.ic_lanucher);
            ad.setTitle("E-Log");
            ad.setMessage(getResources().getString(R.string.setup_error_msg));
            ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ad.cancel();
                            finish();
                        }
                    });
            ad.show();
            bBluetoothConnectionError = true;
        }
    }

    @Override
    public void onButtonClicked() {

    }

    private void showLoaderAnimation(boolean isShown) {
        if (isShown) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showLoaderAnimation(false);
                        }
                    });
                }
            }, 30000);
            rlLoadingPanel.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            rlLoadingPanel.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    public void updateActiveIcon() {
        if (ivActiveUser != null) {
            ivActiveUser.setBackgroundResource(R.drawable.ic_flagbar_driver_active);
        }
        if (icFreezeActiveUser != null) {
            icFreezeActiveUser.setBackgroundResource(R.drawable.ic_flagbar_driver_active);
        }
    }

    @Override
    public void onMessageReceived() {
        if (icMessage != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    icMessage.setVisibility(View.VISIBLE);
                    icFreezeMessage.setVisibility(View.VISIBLE);
                }
            });

        }
    }


    @Override
    public void onMessageRead() {
        if (icMessage != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    icMessage.setVisibility(View.GONE);
                    icFreezeMessage.setVisibility(View.GONE);
                }
            });
        }
    }

    public void turnOffGPSIcon() {
        if (icGPS != null) {
            icGPS.setVisibility(View.VISIBLE);
            icGPS.setBackgroundResource(R.drawable.ic_flagbar_gps_off);
        }

        if (icFreezeGPS != null) {
            icFreezeGPS.setVisibility(View.VISIBLE);
            icFreezeGPS.setBackgroundResource(R.drawable.ic_flagbar_gps_off);
        }
    }


    public void turnOnGPSIcon() {
        if (icGPS != null) {
            icGPS.setVisibility(View.VISIBLE);
            icGPS.setBackgroundResource(R.drawable.ic_flagbar_gps_on);
        }

        if (icFreezeGPS != null) {
            icFreezeGPS.setVisibility(View.VISIBLE);
            icFreezeGPS.setBackgroundResource(R.drawable.ic_flagbar_gps_on);
        }
    }

    @Override
    public void onUpdateInspectionIcon() {
        GPSData.TripInspectionCompletedFg = 1;
        if (icInspection != null) {
            icInspection.setVisibility(View.VISIBLE);
            icInspection.setBackgroundResource(R.drawable.ic_flagbar_dvir_done);
            //change the icon to GREEN
            //Log.d(TAG, "Change Inspection GREEN");
        }
        if (icFreezeInspection != null) {
            icFreezeInspection.setVisibility(View.VISIBLE);
            icFreezeInspection.setBackgroundResource(R.drawable.ic_flagbar_dvir_done);
        }
    }


    @Override
    public void resetInspectionIcon() {
        if (icInspection != null) {
            icInspection.setVisibility(View.VISIBLE);
            //change the icon to RED
            icInspection.setBackgroundResource(R.drawable.ic_flagbar_dvir_pending);
            //Log.d(TAG, "Change Inspection RED");
        }
        if (icFreezeInspection != null) {
            icFreezeInspection.setVisibility(View.VISIBLE);
            icFreezeInspection.setBackgroundResource(R.drawable.ic_flagbar_dvir_pending);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            Bundle bundle = intent.getExtras();
            int userId = bundle.getInt("UserId", 0);
            String userName = bundle.getString("UserName", "");
            if (userId != 0) {
                onNavigationItemSelected(R.id.message);
                navigateToMessage(userId, userName);
            }

        } catch (Exception exe) {
        }
    }

    public void onUpdateWebServiceIcon(boolean result) {
        if (icWebService != null) {
            if (result) {
                icWebService.setVisibility(View.VISIBLE);
                //set icon to Server icon
                icWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_on);
                //Log.d(TAG, "Show WebService Icon");
            } else {
                icWebService.setVisibility(View.VISIBLE);
                icWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);
            }
        }

        if (icFreezeWebService != null) {
            if (result) {
                icFreezeWebService.setVisibility(View.VISIBLE);
                icFreezeWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_on);
            } else {
                icFreezeWebService.setVisibility(View.VISIBLE);
                icFreezeWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);
            }
        }
    }

    @Override
    public void onServerStatusChanged(final boolean status) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onUpdateWebServiceIcon(status);
            }
        });
    }

    public void onUpdateCanbusIcon(int state) {
        canbusState = state;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (icCanbus != null) {
                    icCanbus.setVisibility(View.VISIBLE);

                    switch (canbusState) {
                        case CanMessages.STATE_NONE:
                        case CanMessages.STATE_LISTEN:
                            //no connection with canbus
                            //set icon to RED
                            //Log.d(TAG, "Show Canbus RED");
                            icCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_disconnect);
                            break;

                        case CanMessages.STATE_CONNECTING:
                            //set to blinking icon
                            //Log.d(TAG, "Show Canbus blinking");
                            icCanbus.setBackgroundResource(R.drawable.trans);
                            icCanbus.setBackgroundResource(R.drawable.flagbar_canbus_blinking);

                            AnimationDrawable frameAnimation = (AnimationDrawable) icCanbus.getBackground();
                            frameAnimation.start();
                            break;
                        case CanMessages.STATE_CONNECTED:
                            //set icon to GREEN
                            //Log.d(TAG, "Show Canbus GREEN");
                            icCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_connect);
                            break;
                    }
                }

                if (icFreezeCanbus != null) {
                    icFreezeCanbus.setVisibility(View.VISIBLE);

                    switch (canbusState) {
                        case CanMessages.STATE_NONE:
                        case CanMessages.STATE_LISTEN:
                            icFreezeCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_disconnect);
                            break;

                        case CanMessages.STATE_CONNECTING:
                            icFreezeCanbus.setBackgroundResource(R.drawable.trans);
                            icFreezeCanbus.setBackgroundResource(R.drawable.flagbar_canbus_blinking);

                            AnimationDrawable frameAnimation = (AnimationDrawable) icFreezeCanbus.getBackground();
                            frameAnimation.start();
                            break;
                        case CanMessages.STATE_CONNECTED:
                            icFreezeCanbus.setBackgroundResource(R.drawable.ic_flagbar_canbus_connect);
                            break;
                    }
                }
            }
        });
    }

    public void onUpdateNetworkIcon(boolean isConnected) {
        icWifi.setVisibility(View.GONE);
        icNetwork.setVisibility(View.VISIBLE);
        icFreezeWifi.setVisibility(View.GONE);
        icFreezeNetwork.setVisibility(View.VISIBLE);
        if (isConnected) {
            GPSData.CellOnlineFg = 1;
            //set Network icon to solid GREEN bar
            //Log.d(TAG, "Show Network GREEN");
            icNetwork.setBackgroundResource(R.drawable.ic_flagbar_network_on);
            icFreezeNetwork.setBackgroundResource(R.drawable.ic_flagbar_network_on);
        } else {
            GPSData.CellOnlineFg = 0;
            //set Network icon to RED
            //Log.d(TAG, "Show Network RED");
            icNetwork.setBackgroundResource(R.drawable.ic_flagbar_network_off);
            icWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);
            icFreezeNetwork.setBackgroundResource(R.drawable.ic_flagbar_network_off);
            icFreezeWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);
        }
    }

    public void onUpdateWifiIcon(boolean isConnected) {
        icWifi.setVisibility(View.VISIBLE);
        icNetwork.setVisibility(View.GONE);
        icFreezeWifi.setVisibility(View.VISIBLE);
        icFreezeNetwork.setVisibility(View.GONE);
        if (isConnected) {
            GPSData.WifiOnFg = 1;
            //set Wifi icon to solid GREEN
            //Log.d(TAG, "Show wifi GREEN");
            icWifi.setBackgroundResource(R.drawable.ic_flagbar_wifi_on);
            icFreezeWifi.setBackgroundResource(R.drawable.ic_flagbar_wifi_on);
        } else {

            GPSData.WifiOnFg = 0;
            //set Wifi icon to GREY
            //Log.d(TAG, "Show wifi GREY");
            icWifi.setBackgroundResource(R.drawable.ic_flagbar_wifi_off);
            icWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);
            icFreezeWifi.setBackgroundResource(R.drawable.ic_flagbar_wifi_off);
            icFreezeWebService.setBackgroundResource(R.drawable.ic_flagbar_web_service_off);
        }
    }

    public void onUpdateBatteryIcon(int level, boolean isPlugged) {
        //level is percent of battery
        if (icBattery != null) {
            icBattery.setVisibility(View.VISIBLE);
            canvas = new Canvas((batteryBmp));
            drawingBattery(level, isPlugged, batteryBmp, icBattery);
        }

        if (icFreezeBattery != null) {
            icFreezeBattery.setVisibility(View.VISIBLE);
            drawingBattery(level, isPlugged, batteryBmp, icFreezeBattery);
        }
    }

    private void drawingBattery(int level, boolean isPlugged, Bitmap bmp, ImageView view) {
        Canvas c = new Canvas(bmp);
        if (level < 20) {
            view.setImageResource(R.drawable.ic_flagbar_battery_empty);
        } else {
            float topPercent = 8;

            float top = bmp.getHeight() * 0.08f;
            float x = (1f * bmp.getWidth()) / 2f - (bmp.getWidth() * 0.2f);
            float xEnd = (1f * bmp.getWidth()) / 2f + (bmp.getWidth() * 0.2f);
            float xTop = x + (bmp.getWidth() * 0.1f);
            float xTopEnd = xEnd - (bmp.getWidth() * 0.1f);
            float yTop = (topPercent / 100f) * bmp.getHeight() + top;

            Paint pUsed = new Paint();
            pUsed.setColor(Color.LTGRAY);
            pUsed.setStrokeWidth(1);

            Paint pLeft = new Paint();
            pLeft.setColor(Color.argb(255, 78, 163, 42));
            pLeft.setStrokeWidth(1);

            float height = bmp.getHeight() - (2 * top);
            int levelUsed = 100 - level;
            float y = ((1f * levelUsed) / 100f) * height + top;

            if (levelUsed < topPercent) {
                c.drawRect(xTop, top, xTopEnd, y, pUsed);
                c.drawRect(xTop, y, xTopEnd, yTop, pLeft);
                c.drawRect(x, yTop, xEnd, height, pLeft);
            } else {
                c.drawRect(xTop, top, xTopEnd, yTop, pUsed);
                float yUsed = yTop + ((1f * (levelUsed - topPercent) / 100f) * height);
                c.drawRect(x, yTop, xEnd, yUsed, pUsed);
                c.drawRect(x, yUsed, xEnd, height, pLeft);
            }

            if (isPlugged) {
                Paint p = new Paint();
                p.setColor(Color.WHITE);
                p.setStyle(Paint.Style.FILL);
                p.setStrokeWidth(1);

                float chargedX1 = x + (bmp.getWidth() * 0.1f);
                float chargedX2 = chargedX1 + (bmp.getWidth() * 0.2f);
                float chargedX3 = chargedX2 - (bmp.getWidth() * 0.1f);
                float chargedX4 = chargedX3 + (bmp.getWidth() * 0.15f);
                float chargedX5 = chargedX1 + (bmp.getWidth() * 0.05f);
                float chargedX6 = chargedX5 + (bmp.getWidth() * 0.05f);
                float chargedX7 = chargedX6 - (bmp.getWidth() * 0.1f);

                float chargedY1 = yTop + bmp.getHeight() * 0.1f;
                float chargedY2 = chargedY1 + bmp.getHeight() * 0.18f;
                float chargedY3 = chargedY2 + bmp.getHeight() * 0.08f;
                float chargedY4 = height - bmp.getHeight() * 0.05f;

                Path path = new Path();
                path.reset();
                path.moveTo(chargedX1, chargedY1);
                path.lineTo(chargedX2, chargedY1);
                path.lineTo(chargedX3, chargedY2);
                path.lineTo(chargedX4, chargedY2);
                path.lineTo(chargedX5, chargedY4);
                path.lineTo(chargedX6, chargedY3);
                path.lineTo(chargedX7, chargedY3);
                path.lineTo(chargedX1, chargedY1);

                c.drawPath(path, p);
            }

            view.setImageBitmap(bmp);
        }
    }

    @Override
    public void callELog() {
        if (undockingMode) {
            replaceFragment(new DockingFragment());
            getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_eld));
            //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            //drawer.closeDrawer(GravityCompat.START);
            return;
        }
        if (stopService) {
            if (ConstantFlag.AUTOSTART_MODE) {
                startService(new Intent(this, AutoStartService.class));
            }
        }
        isOnDailyLog = true;
        bInspectDailylog = false;
        if (elogFragment == null) {
            elogFragment = new ELogFragment();
        }
        replaceFragment(elogFragment);
        previousScreen = currentScreen;
        currentScreen = DailyLog_Screen;

        invalidateOptionsMenu();
    }

    @Override
    public void callInspectELog() {
        if (undockingMode) {
            return;
        }
        bInspectDailylog = true;
        bEditEvent = false;
        isOnDailyLog = false;

       /* if (inspectFragment == null) {
            inspectFragment = new DetailFragment();
        }*/
        replaceFragment(InspectLogFragment.newInstance());

        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_inspect_elog));
        previousScreen = currentScreen;
        currentScreen = Inspect_DailyLog_Screen;
        invalidateOptionsMenu();
    }

    @Override
    public void callNewEvent() {
        if (undockingMode) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("current_status", currentDutyStatus);

        NewEventFragment fragment = NewEventFragment.newInstance();
        fragment.setArguments(bundle);
        isOnDailyLog = false;
        bInspectDailylog = false;
        replaceFragment(fragment);
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_create_event));
        previousScreen = currentScreen;
        currentScreen = New_Event_Screen;
        invalidateOptionsMenu();
    }

    @Override
    public void callEditRequest() {
        if (undockingMode) {
            return;
        }
        isOnDailyLog = false;
        bInspectDailylog = false;
        replaceFragment(ModifiedFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_edit_request));
        previousScreen = currentScreen;
        currentScreen = Edit_Request_Screen;
        title = getApplicationContext().getResources().getString(R.string.menu_edit_request);
        invalidateOptionsMenu();
    }

    @Override
    public void callUnidentifiedEvent() {
        if (undockingMode) {
            return;
        }
        isOnDailyLog = false;
        bInspectDailylog = false;
        replaceFragment(UnidentifyFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_unidentified_event));
        previousScreen = currentScreen;
        currentScreen = Unidentified_Data_Screen;
        title = getApplicationContext().getResources().getString(R.string.menu_unidentified_event);
        invalidateOptionsMenu();
    }

    @Override
    public void callCertifyLogBook() {
        if (undockingMode) {
            return;
        }
        isOnDailyLog = false;
        bInspectDailylog = false;
        replaceFragment(UnCertifiedFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_certify_log_book));
        previousScreen = currentScreen;
        currentScreen = Uncertified_LogBook_Screen;
        title = getApplicationContext().getResources().getString(R.string.title_certify_log_book);
        invalidateOptionsMenu();
    }

    @Override
    public void callViolationHistory() {
        if (undockingMode) {
            return;
        }
        isOnDailyLog = false;
        bInspectDailylog = false;
        replaceFragment(ViolationFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.menu_violation_history));
        previousScreen = currentScreen;
        currentScreen = Violation_History_Screen;
        title = getApplicationContext().getResources().getString(R.string.menu_violation_history);
        invalidateOptionsMenu();
    }

    @Override
    public void callDriverProfile() {
        if (undockingMode) {
            return;
        }
        isOnDailyLog = false;
        bInspectDailylog = false;
        replaceFragment(DriverProfileFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.menu_driver_profile));
        previousScreen = currentScreen;
        currentScreen = Driver_Profile_Screen;
        title = getApplicationContext().getResources().getString(R.string.menu_driver_profile);
        invalidateOptionsMenu();
    }

    final Handler dialogHandler = new Handler();
    final Runnable dialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (successDialog.isShowing()) {
                successDialog.dismiss();
            }
        }
    };

    @Override
    public void outputFileSuccess() {
        if (outputFileDialog != null) {
            outputFileDialog.dismiss();
        }
        if (successDialog == null) {
            successDialog = new AlertDialog.Builder(this).create();
        }

        successDialog.setCancelable(true);
        successDialog.setCanceledOnTouchOutside(false);
        successDialog.setTitle("Send Data");
        successDialog.setIcon(R.drawable.ic_launcher);
        successDialog.setMessage("Data transfer successfully!");
        successDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        successDialog.cancel();
                    }
                });
        successDialog.show();
        successDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialogHandler.removeCallbacks(dialogRunnable);
            }
        });

        dialogHandler.postDelayed(dialogRunnable, 2000);
    }

    @Override
    public void outputFileFailed() {
        showMessageDialog("Data transfer failed! Please retry!");
    }

    private void showMessageDialog(String message) {
        if (messageDialog == null) {
            messageDialog = new AlertDialog.Builder(this).create();
        }

        messageDialog.setCancelable(true);
        messageDialog.setCanceledOnTouchOutside(false);
        messageDialog.setTitle("E-Log");
        messageDialog.setIcon(R.drawable.ic_launcher);
        messageDialog.setMessage(message);
        messageDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Retry",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        messageDialog.cancel();
                    }
                });
        messageDialog.show();
    }

    // update diagnostic/malfunction indicator
    @Override
    public void onDiagnoticMalfunctionUpdated(boolean malfunctionFg) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
                if (loginFragment != null) {
                    loginFragment.updateDiagnosticMalfunction();
                }
            }
        });

    }

    @Override
    public void backFromLogin() {
        setDrawerState(true);
        bLogin = false;
        toolbar.setVisibility(View.VISIBLE);
        flagBar.setVisibility(View.VISIBLE);
        toggle.setDrawerIndicatorEnabled(true);

        isOnDailyLog = true;
        bInspectDailylog = false;
        if (elogFragment == null) {
            elogFragment = new ELogFragment();
        }
        replaceFragment(elogFragment);
        //title = getApplicationContext().getResources().getString(R.string.menu_daily_log);
        previousScreen = -1;

        invalidateOptionsMenu();

        drawer.closeDrawer(GravityCompat.START);
        setDriverName();
    }

    @Override
    public void updateWebserviceIcon(boolean active) {
        onUpdateWebServiceIcon(active);
    }

    @Override
    public void updateSpecialCategoryChanged(boolean value) {
        specialCategoryChanged = value;
    }

    @Override
    public void activeUser() {
        callActive();
    }

    @Override
    public void changeUser() {
        SwitchDriver();
    }

    @Override
    public void undocking() {
        SharedPreferences prefs = getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
        prefs.edit().putBoolean("undocking", true).commit();
        undockingMode = true;

        if (ConstantFlag.AUTOSTART_MODE) {
            AutoStartService.stopTask = true;
            stopService(new Intent(MainActivity.this, AutoStartService.class));
        }
        //stop bluetooth
        objCan.stop();
        // stop can heart beat
        objCan.StopCanHB();
        objTpms.StopTpmsHB();
        //disable all
        bEditEvent = false;
        bEditEvent = false;
        bInspectDailylog = false;
        invalidateOptionsMenu();

        //show DockingFragment
        replaceFragment(new DockingFragment());
    }

    @Override
    public void setCertify(final int certifyFg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                icCertifyLog.setVisibility(View.VISIBLE);
                if (certifyFg == 1) {
                    boolean uncertifyFg = DailyLogDB.unCertifiedFg(Utility.onScreenUserId);
                    if (uncertifyFg)
                        icCertifyLog.setBackground(getResources().getDrawable(R.drawable.ic_flagbar_uncertify));
                    else
                        icCertifyLog.setBackground(getResources().getDrawable(R.drawable.ic_flagbar_certify));

                } else if (certifyFg == 0) {

                    icCertifyLog.setBackground(getResources().getDrawable(R.drawable.ic_flagbar_uncertify));
                }
            }
        });
    }

    @Override
    public void changeRule(int rule) {

        currentRule =rule;
        chkRules.setChecked(currentRule < 3);

    }

    @Override
    public void autologinSuccessfully() {

        //update inspection icon
        boolean inspections = TripInspectionDB.getInspections(Utility.getCurrentDate(), Utility.onScreenUserId);
        if (inspections) {
            GPSData.TripInspectionCompletedFg = 1;
            onUpdateInspectionIcon();
        } else {
            GPSData.TripInspectionCompletedFg = 0;
            resetInspectionIcon();

        }
        updateFlagbar(true);
        int unreadCount = MessageDB.getUnreadCount();
        if (unreadCount > 0) {
            onMessageReceived();
        }
        setCertify(1);


        if (firstLogin) {

            currentRule = DailyLogDB.getCurrentRule(Utility.activeUserId);
            chkRules.setChecked(currentRule < 3);
            setOrientation();

            showSpecialCategory(false);
            // sync message
            new MessageSyncData(messageSyncDataPostTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        //Log.d(TAG, "loginSuccessfully");
        this.firstLogin = false;
        setDrawerState(true);
        //already login
        setDriverName();
        bLogin = false;
        int userIcon = Utility.onScreenUserId == Utility.activeUserId ? R.drawable.ic_flagbar_driver_active : R.drawable.ic_flagbar_driver_inactive;
        if (ivActiveUser != null) {
            ivActiveUser.setBackgroundResource(userIcon);
        }
        if (icFreezeActiveUser != null) {
            icFreezeActiveUser.setBackgroundResource(userIcon);
        }

        bHaveUnAssignedEvent = false;
        bHaveLogbookToCertify = false;

        isOnDailyLog = false;

        if (undockingMode) {
            replaceFragment(new DockingFragment());
            getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_eld));
        } else {
            if (elogFragment == null) {
                elogFragment = new ELogFragment();
            }
            elogFragment.setFirstLogin(firstLogin);
            replaceFragment(elogFragment);
            isOnDailyLog = true;
            previousScreen = -1;
            currentScreen = DailyLog_Screen;

        }

        toolbar.setVisibility(View.VISIBLE);
        flagBar.setVisibility(View.VISIBLE);

    }

    @Override
    public void loginSuccessfully(boolean firstLogin) {

        //update inspection icon
        boolean inspections = TripInspectionDB.getInspections(Utility.getCurrentDate(), Utility.onScreenUserId);
        if (inspections) {
            GPSData.TripInspectionCompletedFg = 1;
            onUpdateInspectionIcon();
        } else {
            GPSData.TripInspectionCompletedFg = 0;
            resetInspectionIcon();

        }
        updateFlagbar(true);
        int unreadCount = MessageDB.getUnreadCount();
        if (unreadCount > 0) {
            onMessageReceived();
        }
        setCertify(1);


        if (firstLogin) {

            currentRule = DailyLogDB.getCurrentRule(Utility.activeUserId);
            chkRules.setChecked(currentRule < 3);
            setOrientation();
            showSpecialCategory(false);
            // sync message
            new MessageSyncData(messageSyncDataPostTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        //Log.d(TAG, "loginSuccessfully");
        this.firstLogin = firstLogin;
        setDrawerState(true);
        //already login
        setDriverName();
        bLogin = false;
        int userIcon = Utility.onScreenUserId == Utility.activeUserId ? R.drawable.ic_flagbar_driver_active : R.drawable.ic_flagbar_driver_inactive;
        if (ivActiveUser != null) {
            ivActiveUser.setBackgroundResource(userIcon);
        }
        if (icFreezeActiveUser != null) {
            icFreezeActiveUser.setBackgroundResource(userIcon);
        }

        bHaveUnAssignedEvent = false;
        bHaveLogbookToCertify = false;
        if (firstLogin) {
            ArrayList<EventBean> unAssignedEventList = EventDB.EventUnAssignedGet();
            if (unAssignedEventList.size() > 0) {
                bHaveUnAssignedEvent = true;
            }

            int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            ArrayList<DailyLogBean> logList = DailyLogDB.getUncertifiedDailyLog(driverId);
            if (logList.size() > 0) {
                bHaveLogbookToCertify = true;
            }
        }

        isOnDailyLog = false;

        if (undockingMode) {
            replaceFragment(new DockingFragment());
            getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_eld));
        } else {
            if (!bHaveUnAssignedEvent && !bHaveLogbookToCertify) {
                //relogin
                if (elogFragment == null) {
                    elogFragment = new ELogFragment();
                }
                elogFragment.setFirstLogin(firstLogin);
                replaceFragment(elogFragment);
                isOnDailyLog = true;
                previousScreen = -1;
                currentScreen = DailyLog_Screen;
            } else {
                if (bHaveUnAssignedEvent) {
                    replaceFragment(new UnidentifyFragment());
                    getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_unidentified_event));
                    isOnDailyLog = false;
                    bInspectDailylog = false;
                    previousScreen = -1;
                    currentScreen = Unidentified_Event_Screen;
                } else {
                    replaceFragment(new UnCertifiedFragment());
                    getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_certify_log_book));
                    isOnDailyLog = false;
                    bInspectDailylog = false;
                    previousScreen = -1;
                    currentScreen = Uncertified_LogBook_Screen;
                }
            }
        }

        toolbar.setVisibility(View.VISIBLE);
        flagBar.setVisibility(View.VISIBLE);


    }

    public void setDrawerState(boolean isEnabled) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (isEnabled) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            toggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_UNLOCKED);
            toggle.setDrawerIndicatorEnabled(true);
            toggle.syncState();
            if (getActionBar() != null)
                getActionBar().setHomeButtonEnabled(true);

        } else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.setDrawerIndicatorEnabled(false);
            toggle.syncState();
            if (getActionBar() != null)
                getActionBar().setHomeButtonEnabled(false);
        }
    }

    private void setDriverName() {
        try {
            if (!Utility.isLargeScreen(getApplicationContext())) {
                if (Utility.user1.isOnScreenFg()) {
                    Utility.onScreenUserId = Utility.user1.getAccountId();
                    driverName = Utility.user1.getFirstName() + (Utility.user1.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvUserName.setText(Utility.user1.getUserName());
                } else {
                    Utility.onScreenUserId = Utility.user2.getAccountId();
                    driverName = Utility.user2.getFirstName() + (Utility.user2.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvUserName.setText(Utility.user2.getUserName());
                }
            } else {
                if (Utility.user1.isOnScreenFg()) {
                    Utility.onScreenUserId = Utility.user1.getAccountId();
                    driverName = Utility.user1.getFirstName() + " " + Utility.user1.getLastName() + (Utility.user1.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvUserName.setText(Utility.user1.getUserName());
                } else {
                    Utility.onScreenUserId = Utility.user2.getAccountId();
                    driverName = Utility.user2.getFirstName() + " " + Utility.user2.getLastName() + (Utility.user2.getExemptELDUseFg() == 1 ? " (Exempt Use)" : "");
                    tvUserName.setText(Utility.user2.getUserName());
                }
            }
            tvLoginName.setText(driverName);
            tvFreezeLoginName.setText(driverName);
        } catch (Exception exe) {
        }

    }

    // Created By: Deepak Sharma
    // Created Date: 26 June 2016
    // Purpose: initialize bluetooth
    private void initializeBluetooth() {
        if (adapter == null) {
            Utility.showMsg("Device doest not support Bluetooth.");
            return;
        }

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    if (!adapter.isEnabled()) {
                        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(turnOn, REQUEST_ENABLE_BT);
                        return;
                    }
                    adapter = BluetoothAdapter.getDefaultAdapter();
                    Set<BluetoothDevice> devices = adapter.getBondedDevices();
                    for (BluetoothDevice device : devices) {

                        if (device.getName().startsWith(CanMessages.BT_NAME) || device.getName().startsWith(CanMessages.BT_NAME_1)) {
                            CanMessages.deviceAddress = device.getAddress();
                            CanMessages.deviceName = device.getName();
                            break;
                        }
                    }

                    Log.d(TAG, "address=" + CanMessages.deviceAddress);
                    if (CanMessages.deviceAddress == null) {

                        Thread.sleep(60000);
                        initializeBluetooth();

                    } else {

                        // start Engine synchronization timer
                        if (DiagnosticIndicatorBean.EngineSynchronizationDiagnosticFg) {
                            SharedPreferences sp = getSharedPreferences("HutchGroup", MODE_PRIVATE);
                            CanMessages.diagnosticEngineSynchronizationTime = sp.getLong("diagnostic_time", 0);
                        }

                        connectDevice(true);
                        objCan.StartCanHB();

                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });
        thread.setName("ElogApplication-BT1");
        thread.start();

    }

    // Created By: Deepak Sharma
    // Created Date: 26 June 2016
    // Purpose: initialize bluetooth tpms
    private void initializeTpms() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    if (!adapter.isEnabled()) {
                        Thread.sleep(60000);
                        initializeTpms();
                        return;
                    }
                    String deviceAddress = "";
                    adapter = BluetoothAdapter.getDefaultAdapter();
                    Set<BluetoothDevice> devices = adapter.getBondedDevices();
                    for (BluetoothDevice device : devices) {

                        if (device.getName().startsWith(Tpms.TPMS_NAME)) {
                            deviceAddress = device.getAddress();
                            break;
                        }
                    }

                    Log.d(TAG, "address=" + deviceAddress);
                    if (deviceAddress == null) {

                       /* Thread.sleep(60000);
                        initializeTpms();*/

                    } else {
                        BluetoothDevice device = adapter.getRemoteDevice(deviceAddress);
                        Tpms.deviceAddress = deviceAddress;
                        objTpms.connect(device, true);
                        objTpms.StartTpmsHB();
                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });
        thread.setName("Thread-TPMSInitialize");
        thread.start();

    }

    CanMessages objCan = new CanMessages();
    Tpms objTpms = new Tpms();

    private void connectDevice(boolean secure) {
        // Get the device MAC address
        String address = CanMessages.deviceAddress;
        // Get the BluetoothDevice object
        BluetoothDevice device = adapter.getRemoteDevice(address);


        // Attempt to connect to the device
        objCan.connect(device, secure);
    }

    private void replaceFragmentWithBackStack(Fragment fragment) {
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = fragmentManager;
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.container, fragment);
        //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(backStateName);
        ft.commit();
    }

    String messageTitle = "";

    @Override
    public void navigateToMessage(int userId, String userName) {
        //create canbus fragment to show data
        previousScreen = Message;
        //call replace to show the fragment
        replaceFragmentWithBackStack(MessageFragment.newInstance(userId, userName));

        getSupportActionBar().setTitle(userName);
        messageTitle = userName;
    }

    View.OnClickListener originalToolbarListener = null;

    @Override
    public void onBackStackChanged() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            toggle.setDrawerIndicatorEnabled(false);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String currentTitle = getSupportActionBar().getTitle().toString();
                    if (currentTitle.equals("New Inspection") || currentTitle.equals("Inspection")) {
                        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_dvir));
                    } else if (currentTitle.equals("Edit Event")) {
                        getSupportActionBar().setTitle(prevTitle);
                    } else if (currentTitle.equals(messageTitle)) {
                        getSupportActionBar().setTitle(R.string.title_Message);
                        try {
                            Utility.hideKeyboard(MainActivity.this, MainActivity.this.getCurrentFocus());
                        } catch (Exception exe) {
                        }
                        messageTitle = "";
                    }
                   /* else
                    {
                        getSupportActionBar().setTitle(title);
                    }*/
                    fragmentManager.popBackStack();
                }
            });
        } else {
            toggle.setDrawerIndicatorEnabled(true);
            toggle.setToolbarNavigationClickListener(originalToolbarListener);
        }
        toggle.syncState();
    }

    @Override
    public void newInspection() {
        previousScreen = DVIR;

        replaceFragmentWithBackStack(NewInspectionFragment.newInstance());
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_new_inspection));
    }

    @Override
    public void viewInspection(boolean viewMode, TripInspectionBean bean) {
        previousScreen = DVIR;
        NewInspectionFragment fragment = NewInspectionFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putBoolean("view_mode", viewMode);
        bundle.putSerializable("trip_inspection", bean);
        fragment.setArguments(bundle);
        replaceFragmentWithBackStack(fragment);
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_inspection));
    }

    @Override
    public void finishInspection() {
        fragmentManager.popBackStack();
        previousScreen = -1;
        getSupportActionBar().setTitle(getApplicationContext().getResources().getString(R.string.title_dvir));
    }

    @Override
    public void stopService() {
        stopService = true;
        AutoStartService.stopTask = true;
        stopService(new Intent(MainActivity.this, AutoStartService.class));
    }

    @Override
    public void callCheckUpdate() {
        try {
            if (!checkingUpdate) {
                checkingUpdate = true;
                manuallyUpdate = true;
                showLoaderAnimation(true);
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                new AppUpdateData(appUpdatePostTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pInfo.versionName);
            } else {
                Utility.showMsg(
                        "An update is already in progress please wait...");
            }
        } catch (Exception e) {
            Log.d(TAG, "Cannot find package info: " + e.getMessage());
        }
    }

    private void showSpecialCategory(final boolean whenLogout) {
        String specialCategory = "";
        if (Utility.user1.isOnScreenFg()) {
            specialCategory = Utility.user1.getSpecialCategory();
        } else if (Utility.user2.isOnScreenFg()) {
            specialCategory = Utility.user2.getSpecialCategory();
        }

        String message = "";
        if (specialCategory.equals("1")) {
            message = "Your configured category is Authorized Personal Use of CMV";
        } else if (specialCategory.equals("2")) {
            message = "Your configured category is Yard Moves";
        } else if (specialCategory.equals("0") || specialCategory.equals("")) {
            message = "You have no category configured";
        } else if (specialCategory.equals("3")) {
            message = "Your configured categories are Authorized Personal Use of CMV and Yard Moves";
        }

        final Snackbar snackbar = Snackbar.make(drawer, message, Snackbar.LENGTH_INDEFINITE);

        if (whenLogout) {
            showLogoutDialog();
        }

        snackbar.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    snackbar.dismiss();
                } catch (Exception exe) {
                }
            }
        }).start();
    }

    private void showLogoutDialog() {
        boolean unCertifyFg = DailyLogDB.getUncertifiedLogFg(Utility.onScreenUserId);
        String message = "";
        if (unCertifyFg && !Utility.InspectorModeFg) {
            message = "You have Uncertified Logs!!!";
        }
        final AlertDialog ad = new AlertDialog.Builder(this)
                .create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle("Logout Confirmation");
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage("Are you sure you want to Logout? " + message);
        ad.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        isOnDailyLog = false;
                        if (Utility.InspectorModeFg) {
                            RedirectToLogin(Utility.user2.isOnScreenFg());
                            setInspectorMode(false);
                        } else {
                            Logout();
                        }
                    }
                });
        ad.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        ad.cancel();
                    }
                });
        ad.show();
    }


    //this receiver to use when mid night comes to refresh the Duty Status chart
    private final BroadcastReceiver dateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                if (Utility.motionFg && activeCurrentDutyStatus == 3 && Utility.activeUserId > 0) {
                    String previousDate = Utility.getPreviousDateOnly(-1);

                    // get previous dailylog id
                    int dailyLogId = DailyLogDB.getDailyLog(Utility.activeUserId, previousDate);

                    if (dailyLogId > 0) {
                        // enter intermediate event for midnight
                        EventDB.EventCreate(previousDate + " 23:59:59", 2, 1, "Intermediate log with conventional location precision", 1, 1, dailyLogId, Utility.activeUserId, "");
                        DailyLogDB.DailyLogSyncRevert(Utility.activeUserId, dailyLogId);
                    }
                    Thread.sleep(2000);
                    dailyLogId = DailyLogDB.DailyLogCreate(Utility.activeUserId, Utility.ShippingNumber, Utility.TrailerNumber, "");
                    EventDB.EventCreate(Utility.getCurrentDateTime(), 2, 1, "Intermediate log with conventional location precision", 1, 1, dailyLogId, Utility.activeUserId, "");

                }


                LogFile.sendLogFile(LogFile.MID_NIGHT);

                SharedPreferences prefs = getSharedPreferences("HutchGroup", MODE_PRIVATE);
                prefs.edit().putBoolean("check_update", false).commit();

                GPSData.TripInspectionCompletedFg = 0;
                Utility.UnidentifiedDrivingTime = 0;
                Utility.bEventUnidentifiedDriverDiagnostic = false;
                resetInspectionIcon();

            } catch (Exception e) {
                LogFile.write(ELogFragment.class.getName() + "::dateChangedReceiver Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            }
        }
    };

    // Created By: Deepak Sharma
    // Created Date: 31 Aug 2016
    // Purpose: set brightness of tablet
    private void setBrightness(int brightness) {
        try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            android.provider.Settings.System.putInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = (float) brightness / 255;
            getWindow().setAttributes(lp);
        } catch (Exception exe) {

        }
    }

    // Created By: Deepak Sharma
    // Created Date: 31 Aug 2016
    // Purpose: check mode night/day and set it
    private void setUIMode() {
        if (Utility.NightModeFg) {

            tvFreeze.setBackgroundResource(R.drawable.freeze_background_night);
            flagBarFreeze.setBackgroundResource(R.drawable.flagbar_freeze_bg_night);
            if (flagBarFreeze1 != null) {
                flagBarFreeze1.setBackgroundResource(R.drawable.flagbar_freeze_bg_night);
            }
            setBrightness(1);


        } else {
            tvFreeze.setBackgroundResource(R.drawable.freeze_background);
            flagBarFreeze.setBackgroundResource(R.drawable.flagbar_freeze_bg);
            if (flagBarFreeze1 != null) {
                flagBarFreeze1.setBackgroundResource(R.drawable.flagbar_freeze_bg);
            }

            setBrightness(255);
        }
    }


    DialogInterface.OnClickListener onDutyChangeDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    //continue to drive
                    onDutyChangeDialogResponse = false;
                    break;


            }
        }
    };

    public void resetOnDutyChangeDialogResponse() {
        onDutyChangeDialogResponse = false;
    }


    @Override
    public void dialogDismiss() {
        try {
            ponDutyChangeDialog.close();

            Log.d(TAG, "Current Status change to On Duty");
            onDutyChangeDialogResponse = true;
            activeCurrentDutyStatus = 4;
            //save event that it automatically change
            int driverId = Utility.activeUserId;

            int logId = DailyLogDB.DailyLogCreate(driverId, Utility.ShippingNumber, Utility.TrailerNumber, "");
            //123 LogFile.write(ELogFragment.class.getName() + "::dialogDismiss: " + "Change DutyStatus to ON DUTY" + " of driverId:" + driverId, LogFile.USER_INTERACTIO
            // N, LogFile.DRIVEREVENT_LOG);
            EventDB.EventCreate(Utility.getCurrentDateTime(), 1, 4, getResources().getString(R.string.duty_status_changed_to_on_duty), 1, 1, logId, driverId, "");

            if (Utility.onScreenUserId == Utility.activeUserId) {
                setActiveDutyStatus(activeCurrentDutyStatus);
                if (elogFragment != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            elogFragment.refresh();
                        }
                    });
                }
            } else {
                Log.i(TAG, "Active drive is not onScreen");
            }

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::dialogDismiss Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }


    @Override
    public void changeStatusPressed() {
        try {
            Log.d(TAG, "change status");
            onDutyChangeDialogResponse = false;
            ponDutyChangeDialog.close();
            if (elogFragment != null)
                elogFragment.launchDutyStatusChange(true);
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        callELog();
                        if (elogFragment != null)
                            elogFragment.launchDutyStatusChange(true);
                    }
                });
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::changeStatusPressed Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void keepDrivingPressed() {
        try {
            Log.d(TAG, "keep driving");
            onDutyChangeDialogResponse = false;
            ponDutyChangeDialog.close();
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::keepDrivingPressed Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            isDialogShown = false;
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    Utility.statusFlag = 1;
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    Utility.statusFlag = 0;
                    int logId = DailyLogDB.DailyLogCreate(Utility.activeUserId, Utility.ShippingNumber, Utility.TrailerNumber, "");

                    EventDB.EventCreate(Utility.getCurrentDateTime(), 3, 0, "Driver Indication for PC, YM and WT cleared", 1, 1, logId, Utility.onScreenUserId, "");
                    break;
            }
        }
    };


    public void checkStatus(boolean isOn) {
        try {
            if (isOn) {
                if (!isDialogShown) {
                    Log.d(TAG, "first time dialog");
                    if (activeCurrentDutyStatus == 5 && Utility.statusFlag == 1) { //PU
                        if (Utility.powerOnOff == 1) {
                            if (dialog != null && !dialog.isShowing()) {
                                Log.d(TAG, "showing dialog");
                                isDialogShown = true;
                                dialog.show();
                            }
                        }
                    }

                    if (activeCurrentDutyStatus == 6) {
                        activeCurrentDutyStatus = 4;
                        setDutyStatus(activeCurrentDutyStatus);
                        if (elogFragment != null) {
                            elogFragment.refresh();

                        }
                    }
                }
            } else {
                isDialogShown = false;
                if (activeCurrentDutyStatus == 6) {
                    //yard move

                }
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::checkStatus Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }


    //call it when vehicle is moving
    public void autoChangeDutyStatus() {
        try {
            if (activeCurrentDutyStatus != 3) {
                if (activeCurrentDutyStatus == 5 && Utility.statusFlag == 1) { //PU
                    // instead of using isDialogShown shouldn't we use dialgo.isShowing()?? This will also cover the use case if driver closes the dialog using back button of device.
                    if (isDialogShown) {
                        int logId = DailyLogDB.DailyLogCreate(Utility.onScreenUserId, Utility.ShippingNumber, Utility.TrailerNumber, "");

                        EventDB.EventCreate(Utility.getCurrentDateTime(), 3, 0, "Driver Indication for PC, YM and WT cleared", 1, 1, logId, Utility.activeUserId, "");
                        Utility.statusFlag = 0;
                        dialog.dismiss();
                        isDialogShown = false;

                    } else
                        return;

                }
                if (activeCurrentDutyStatus == 6 && Utility.statusFlag == 2) { //YM
                    return;
                }

                if (activeCurrentDutyStatus == 5 || activeCurrentDutyStatus == 6) {
                    int logId = DailyLogDB.DailyLogCreate(Utility.onScreenUserId, Utility.ShippingNumber, Utility.TrailerNumber, "");
                    EventDB.EventCreate(Utility.getCurrentDateTime(), 3, 0, "Driver Indication for PC, YM and WT cleared", 1, 1, logId, Utility.activeUserId, "");
                }

                activeCurrentDutyStatus = 3;
                Utility.DrivingTime = 0;
                resetFlag();
                int logId = DailyLogDB.DailyLogCreate(Utility.onScreenUserId, Utility.ShippingNumber, Utility.TrailerNumber, "");
                EventDB.EventCreate(Utility.getCurrentDateTime(), 1, 3, "Driver's Duty Status changed to DRIVING", 1, 1, logId, Utility.activeUserId, "");

                if (Utility.onScreenUserId == Utility.activeUserId)
                    if (elogFragment != null) {
                        elogFragment.refresh();

                    } else {
                        try {
                            HourOfService.InvokeRule(Utility.newDate(), Utility.activeUserId);
                        } catch (Exception exe) {
                        }
                    }
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::autoChangeDutyStatus Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }


    //call when vehicle is stop
    @Override
    public void promptToChangeStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    String title = "E-Log";
                    int status = activeCurrentDutyStatus;
                    title += " - " + UserDB.getUserName(Utility.activeUserId);

                    if (status == 3) {
                        if (isAppActive) {
                            if (ponDutyChangeDialog != null) {
                                if (!ponDutyChangeDialog.isShowing() && !onDutyChangeDialogResponse) {

                                    autoDismissOnDutyChangeDialog = false;
                                    onDutyChangeDialogResponse = true;
                                    ponDutyChangeDialog.setTitle(title);
                                    ponDutyChangeDialog.mListener = MainActivity.this;

                                    ponDutyChangeDialog.show(getSupportFragmentManager(), "popup_dialog");
                                }
                            }
                        } else {
                            int logId = DailyLogDB.DailyLogCreate(Utility.onScreenUserId, Utility.ShippingNumber, Utility.TrailerNumber, "");
                            // change status to on duty
                            EventDB.EventCreate(Utility.getCurrentDateTime(), 1, 4, "Driver's Duty Status changed to ON DUTY", 1, 1, logId, Utility.activeUserId, "");

                            setActiveDutyStatus(4);
                            resetFlag();

                            if (elogFragment != null) {
                                elogFragment.refresh();
                            }

                            if (Utility._appSetting.getDutyStatusReading() == 1) {
                                String textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_driving);
                                if (MainActivity.textToSpeech != null)
                                    MainActivity.textToSpeech.speak(textToSpeech, TextToSpeech.QUEUE_ADD, null);
                            }
                        }
                    }
                } catch (Exception e) {
                    onDutyChangeDialogResponse = false;
                    LogFile.write(ELogFragment.class.getName() + "::promptToChangeStatus Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                }
            }
        });
    }

    public void updateFlagbar(final boolean status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (status) {

                    ivActiveUser.setVisibility(View.VISIBLE);
                    icCertifyLog.setVisibility(View.VISIBLE);
                    //  icViolation.setVisibility(View.VISIBLE);
                    icInspection.setVisibility(View.VISIBLE);
                    icCertifyLog.setVisibility(View.VISIBLE);
                } else {
                    tvLoginName.setText("");
                    tvFreezeLoginName.setText("");
                    ivActiveUser.setVisibility(View.GONE);
                    icCertifyLog.setVisibility(View.GONE);
                    icViolation.setVisibility(View.GONE);
                    icMessage.setVisibility(View.GONE);
                    icInspection.setVisibility(View.GONE);
                    icCertifyLog.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void setInspectorMode(boolean status) {
        Utility.InspectorModeFg = status;

        if (status) {
            callInspectELog();
            flagBar.setVisibility(View.GONE);
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.inspect_log_action_bar));
        } else {
            bInspectDailylog = false;
            getSupportActionBar().setTitle("ELD");
            flagBar.setVisibility(View.VISIBLE);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff007dc1));
            invalidateOptionsMenu();
            if (Utility.user2.isOnScreenFg()) {

                Utility.user1.setOnScreenFg(true);
                Utility.user2.setOnScreenFg(false);
            } else {
                Utility.user1.setOnScreenFg(false);
            }

        }
        bindDrawerItem();
    }

    @Override
    public void onUpdateViolation(boolean status) {
        int visibility = status ? View.VISIBLE : View.GONE;
        try {
            icViolation.setVisibility(visibility);
            icFreezeViolation.setVisibility(visibility);
        } catch (Exception exe) {
        }
    }

    @Override
    public void onLoadDTC() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(DTCFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = DTC;
        title = getApplicationContext().getResources().getString(R.string.title_dtc);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    @Override
    public void onLoadScoreCard() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(ScoreCardFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = ScoreCard;
        title = getApplicationContext().getResources().getString(R.string.title_score_card);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    @Override
    public void onLoadTrailerManagement() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(TrailerManagementFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = TrailerManagement;
        title = getApplicationContext().getResources().getString(R.string.title_trailer_management);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    @Override
    public void onLoadVehicleInfo() {
        isOnDailyLog = false;
        bInspectDailylog = false;

        replaceFragment(VehicleInfoFragment.newInstance());
        previousScreen = currentScreen;
        currentScreen = VehicleInfo;
        title = getApplicationContext().getResources().getString(R.string.title_vehicle_info);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
    }

    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mMagneticField;
    private GForceMonitor gForceMonitor;

    private void initializeGforce() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gForceMonitor = new GForceMonitor();
        gForceMonitor.setGForceChangeListener(this);
    }

    private void resumeGforce() {
        if (GPSData.ACPowerFg == 0)
            return;
        mSensorManager.registerListener(gForceMonitor, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(gForceMonitor, mMagneticField, SensorManager.SENSOR_DELAY_UI);
    }

    private void pauseGforce() {
        GForceMonitor.resetValues();
        mSensorManager.unregisterListener(gForceMonitor, mAccelerometer);
        mSensorManager.unregisterListener(gForceMonitor, mMagneticField);
    }

    @Override
    public void onLeftSharpTurn(float force) {

        int driverId = Utility.activeUserId;

        if (driverId == 0) {
            driverId = Utility.unIdentifiedDriverId;
        }

        AlertDB.Save("SharpTurnLeftVL", "Sharp Turn Left", Utility.getCurrentDateTime(), 5, 0, driverId);
    }

    @Override
    public void onRightSharpTurn(float force) {


        int driverId = Utility.activeUserId;

        if (driverId == 0) {
            driverId = Utility.unIdentifiedDriverId;
        }

        AlertDB.Save("SharpTurnRightVL", "Sharp Turn Right", Utility.getCurrentDateTime(), 5, 0, driverId);
    }

    @Override
    public void onHardAcceleration(float force) {

        int driverId = Utility.activeUserId;

        if (driverId == 0) {
            driverId = Utility.unIdentifiedDriverId;
        }

        AlertDB.Save("HardAccelerationVL", "Hard Acceleration", Utility.getCurrentDateTime(), 5, 0, driverId);
    }

    @Override
    public void onHardBrake(float force) {

        int driverId = Utility.activeUserId;

        if (driverId == 0) {
            driverId = Utility.unIdentifiedDriverId;
        }


        AlertDB.Save("HardBreakingVL", "Hard Breaking", Utility.getCurrentDateTime(), 5, 0, driverId);

    }
}
