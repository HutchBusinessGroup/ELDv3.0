package com.hutchgroup.elog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import com.hutchgroup.elog.beans.DiagnosticIndicatorBean;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.DiagnosticMalfunction;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.common.ZoneList;
import com.hutchgroup.elog.db.CarrierInfoDB;
import com.hutchgroup.elog.db.EventDB;
import com.hutchgroup.elog.db.LoginDB;
import com.hutchgroup.elog.tasks.SyncData;

public class SplashActivity extends Activity implements Runnable {

    com.hutchgroup.elog.tasks.SyncData.PostTaskListener<Boolean> syncDataPostTaskListener = new com.hutchgroup.elog.tasks.SyncData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            if (result) {

                Editor e = (getSharedPreferences("HutchGroup", MODE_PRIVATE))
                        .edit();
                e.putBoolean("syncStatus", true);
                e.commit();
                NavigateToLogin();
            } else {

                LogFile.write(SplashActivity.class.getName() + "::SyncData Error:" + Utility.errorMessage, LogFile.DATABASE, LogFile.ERROR_LOG);
            }
        }
    };
    boolean firstRun = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void initialize() {
        CanMessages.deviceAddress = null;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            firstRun = bundle.getBoolean("firstrun", false);

        // will exit application when running first time user must start application mannualy after this exit
        if (firstRun) {
            Utility.showAlertMsg("ELD Device will be restarted in 10 seconds.", SplashActivity.this);
            //System.exit(0);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10000);
                        rebootDevice();
                    } catch (InterruptedException exe) {

                    }
                }
            }).start();
            return;
        }

        Utility.IMEIGet(SplashActivity.this);
        Utility.VersionGet(SplashActivity.this);
        Utility.TimeZoneOffsetUTC = ZoneList.getTimeZoneOffset();
        if (!Utility.isLargeScreen(getApplicationContext())) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        SharedPreferences sp = getSharedPreferences("HutchGroup", MODE_PRIVATE);

        boolean syncStatus = sp.getBoolean("syncStatus", false);
        DiagnosticMalfunction.getDiagnosticIndicator();

        if (syncStatus) {
            CarrierInfoDB.getCompanyInfo();
            NavigateToLogin();
        } else {
            boolean isEmulator = false;
            if (Utility.isInternetOn() || isEmulator) {

                new SyncData(syncDataPostTaskListener).execute("0");
            } else {
                Utility.showAlertMsg("Device is not connected to internet. Please try again later!", SplashActivity.this);

            }
        }

        //Utility._appSetting = SettingsDB.getSettings();
    }

    // Created By: Deepak Sharma
    // Created Date: 15 April 2015
    // Purpose: navigate to login screen
    private void NavigateToLogin() {
        if (Utility.user1.getAccountId() == 0) {

            Utility.unIdentifiedDriverId = LoginDB.getUnidentifiedDriverId();
            if (Utility.unIdentifiedDriverId == 0) {
                Editor e = (getSharedPreferences("HutchGroup", MODE_PRIVATE))
                        .edit();
                e.putBoolean("syncStatus", false);
                e.commit();
                Utility.showAlertMsg("An account with Unidentified Driver Account Type is missing!", SplashActivity.this);
                return;
            } else {
                // check in unidentified data diagnostic indicator is off
                if (!DiagnosticIndicatorBean.UnidentifiedDrivingDiagnosticFg) {
                    if (Utility.UnidentifiedDrivingTime == 0) {
                        // total minutes of unidentified driving for current 24 hours
                        String date = Utility.getDateTime(Utility.getCurrentDate(), 0);
                        Utility.UnidentifiedDrivingTime = EventDB.getUnidentifiedTime(date);
                    }
                }
            }
        }

        Thread t = new Thread(this);
        t.setName("Splash-login");
        t.start();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000);
            RedirectToLogin(false);

        } catch (Exception e) {
            Utility.printError(e.getMessage());
        }

    }

    private void RedirectToLogin(boolean coDriver) {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        Bundle b = new Bundle();
        b.putBoolean("firstrun", firstRun);
        b.putBoolean("CoDriverFg", coDriver);
        b.putBoolean("loginCall", true);
        i.putExtras(b);
        startActivity(i);
        SplashActivity.this.finish();
        // overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    private void rebootDevice() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
            proc.waitFor();
        } catch (final Exception exe) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Utility.showAlertMsg(exe.getMessage(), SplashActivity.this);
                }
            });
        }
    }

}
