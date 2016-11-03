package com.hutchgroup.elog.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.hutchgroup.elog.MainActivity;
import com.hutchgroup.elog.SplashActivity;
import com.hutchgroup.elog.common.Utility;

import java.util.Timer;
import java.util.TimerTask;

public class AutoStartService extends Service {
    private static final String TAG = AutoStartService.class.getName();
    private static final int INTERVAL = 2 * 60 * 1000; //90000; // poll every 2 minutes
    private static final String APP_PACKAGE_NAME = "com.hutchgroup.e_log";

    public static boolean stopTask, pauseTask;
    private PowerManager.WakeLock mWakeLock;
    boolean pmode_scrn_on = true;

    @Override
    public void onCreate() {
        super.onCreate();
        stopTask = false;
        pauseTask = false;
        // Screen will never switch off this way
        mWakeLock = null;
        if (pmode_scrn_on) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "a_tag");
            mWakeLock.acquire();
        }

        // Start your (polling) task
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // If you wish to stop the task/polling
                if (stopTask) {
                    this.cancel();
                }

                if (!stopTask && !pauseTask) {
                    Log.i("AutoStart", "check app is foreground or not");
                    // The first in the list of RunningTasks is always the foreground task.
                    ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                    ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
                    String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();

                    // Check foreground app: If it is not in the foreground... bring it!
                    if (!foregroundTaskPackageName.equals(APP_PACKAGE_NAME)) {
                        Log.i("AutoStartService", "AutoStart: " + Utility.user1.getAccountId() + "" + Utility.ApplicationVersion);
                        //Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(APP_PACKAGE_NAME);
                        Intent LaunchIntent = new Intent(getApplicationContext(), MainActivity.class);
                        LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Bundle b = new Bundle();
                        b.putBoolean("firstrun", false);
                        b.putBoolean("CoDriverFg", false);
                        b.putBoolean("loginCall", true);
                        b.putBoolean("AutoStartFg", true);
                        LaunchIntent.putExtras(b);
                        startActivity(LaunchIntent);
                    }
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, INTERVAL);
    }

    @Override
    public void onDestroy() {
        stopTask = true;
        if (mWakeLock != null)
            mWakeLock.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 0;
    }
}