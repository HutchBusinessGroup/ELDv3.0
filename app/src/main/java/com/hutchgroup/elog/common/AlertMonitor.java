package com.hutchgroup.elog.common;

import android.util.Log;

import com.hutchgroup.elog.beans.AlertBean;
import com.hutchgroup.elog.db.AlertDB;

import java.util.Date;

/**
 * Created by Deepak on 12/1/2016.
 */

public class AlertMonitor {
    public static boolean SpeedVLFg;
    public static long SpeedVLDate;

    public static double PostedSpeed = 60d;

    Thread thAlertMonitor = null;

    private void SpeedViolationGet() {
        if (!SpeedVLFg) {
            double speed = Double.parseDouble(CanMessages.Speed);
            if (speed > PostedSpeed) {
                SpeedVLFg = true;
                SpeedVLDate = System.currentTimeMillis();
                AlertBean bean = new AlertBean();
                bean.setAlertCode("SpeedVL");
                bean.setAlertName("Speed Violation");
                bean.setAlertDateTime(Utility.getCurrentDateTime());
                AlertDB.Save(bean);

            }
        } else {

            double speed = Double.parseDouble(CanMessages.Speed);
            if (speed <= PostedSpeed) {
                int duration = (int) ((System.currentTimeMillis() - SpeedVLDate) / (1000 * 60));
                int score = 0;
                if (duration > 10) {
                    score += (duration / 10) * 4 + 2;
                } else if (duration > 2) {
                    score += 2;
                }
                AlertDB.Update("SpeedVL", duration, score);
            }
        }
    }

    // Deepak Sharma
    // 3 Aug 2016
    // send request to bluetooth device every 5 seconds
    public void startAlertMonitor() {

        if (thAlertMonitor != null) {
            thAlertMonitor.interrupt();
            thAlertMonitor = null;
        }
        thAlertMonitor = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException exe) {
                    }

                    // monitor speed violation
                    SpeedViolationGet();
                }
            }
        });
        thAlertMonitor.setName("TransmitRequest");
        thAlertMonitor.start();
    }


    public void stopAlertMonitor() {
        if (thAlertMonitor != null) {
            thAlertMonitor.interrupt();
            thAlertMonitor = null;
        }
    }
}
