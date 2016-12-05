package com.hutchgroup.elog.common;

import android.util.Log;

import com.hutchgroup.elog.beans.AlertBean;
import com.hutchgroup.elog.beans.GPSData;
import com.hutchgroup.elog.db.AlertDB;

import java.util.Date;

/**
 * Created by Deepak on 12/1/2016.
 */

public class AlertMonitor {
    public static boolean SpeedVLFg;
    public static long SpeedVLDate;
    public static double MaxSpeed = 0d;
    public static double PostedSpeed = 60d;
    public static double PostedSpeedThreshold = 10d;

    public static boolean HOSVLFg;
    public static long HOSVLDate;

    public static boolean NoTripInspectionVL;

    Thread thAlertMonitor = null;
    public static boolean HighRPMVL;
    public static long HighRPMVLDate;
    public static double MaxRPM = 0;

    public static boolean IdlingVLFg;
    public static long IdlingVLDate;

    private void IdlingViolationGet() {
        if (!IdlingVLFg) {
            if (GPSData.CurrentStatus == 2) {
                IdlingVLFg = true;
                IdlingVLDate = System.currentTimeMillis();
                int driverId = Utility.activeUserId;

                if (driverId == 0) {
                    driverId = Utility.unIdentifiedDriverId;
                }

                AlertDB.Save("IdlingVL", "Idling", Utility.getCurrentDateTime(), 0, 0, driverId);
            }
        } else {
            if (GPSData.CurrentStatus != 2) {
                IdlingVLFg = false;
                int duration = (int) ((System.currentTimeMillis() - IdlingVLDate) / (1000 * 60)) + 10;
                int score = 5;
                AlertDB.Update("IdlingVL", duration, score);
            }
        }
    }

    private void NoTripInspectionGet() {
        if (!NoTripInspectionVL) {
            if (GPSData.TripInspectionCompletedFg == 0) {
                if (Utility.motionFg) {
                    NoTripInspectionVL = true;
                    AlertDB.Save("HOSVL", "Hours Of Service", Utility.getCurrentDateTime(), 50, 0, Utility.onScreenUserId);
                }
            }

        }
    }

    private void HighRPMGet() {
        double RPM = Double.parseDouble(CanMessages.RPM);
        if (!HighRPMVL) {
            if (RPM > 1600d) {
                HighRPMVL = true;
                HighRPMVLDate = System.currentTimeMillis();

                int driverId = Utility.activeUserId;
                if (driverId == 0) {
                    driverId = Utility.unIdentifiedDriverId;
                }

                AlertDB.Save("HighRPMVL", "High RPM", Utility.getCurrentDateTime(), 0, 0, driverId);
            }
        } else {
            if (RPM < 1600d) {
                HighRPMVL = false;
                int duration = (int) ((System.currentTimeMillis() - HighRPMVLDate) / (1000 * 60));
                int score = 0;
                if (duration > 60) {
                    score += Math.ceil((duration - 60) / 60) * 6 + 8;
                } else if (duration >= 20 && duration <= 60) {
                    score += 8;
                } else if (duration > 5 && duration < 20) {
                    score += 2;
                }

                if (MaxRPM > 2000) {
                    score += 20;
                } else if (MaxRPM >= 1800 && MaxRPM <= 2000) {
                    score += 8;
                } else {
                    score += 2;
                }
                AlertDB.Update("HighRPMVL", duration, score);

            } else {
                if (RPM > MaxRPM) {
                    MaxRPM = RPM;
                }
            }
        }
    }

    private void HOSViolationGet() {
        if (!HOSVLFg) {
            if (GPSData.NoHOSViolationFgFg == 0) {
                HOSVLFg = true;
                HOSVLDate = System.currentTimeMillis();

                int driverId = Utility.activeUserId;
                if (driverId == 0) {
                    driverId = Utility.unIdentifiedDriverId;
                }

                AlertDB.Save("HOSVL", "Hours Of Service", Utility.getCurrentDateTime(), 0, 0, driverId);
            }
        } else {
            if (GPSData.NoHOSViolationFgFg == 1) {
                HOSVLFg = false;
                int duration = (int) ((System.currentTimeMillis() - HOSVLDate) / (1000 * 60));
                int score = 5;
                if (duration > 30) {
                    score += Math.ceil((duration - 30) / 10) * 30 + 58;
                } else if (duration >= 10 && duration <= 30) {
                    score += 15;
                } else if (duration > 5 && duration < 10) {
                    score += 8;
                } else {
                    score += 5;
                }
                AlertDB.Update("HOSVL", duration, score);
            }
        }
    }

    private void SpeedViolationGet() {
        double speed = Double.parseDouble(CanMessages.Speed);
        if (!SpeedVLFg) {
            if (speed > (PostedSpeed + PostedSpeedThreshold)) {
                SpeedVLFg = true;
                SpeedVLDate = System.currentTimeMillis();

                int driverId = Utility.activeUserId;
                if (driverId == 0) {
                    driverId = Utility.unIdentifiedDriverId;
                }


                AlertDB.Save("SpeedVL", "Speed Violation", Utility.getCurrentDateTime(), 0, 0, driverId);

            }
        } else {
            if (speed <= (PostedSpeed + PostedSpeedThreshold)) {
                SpeedVLFg = false;
                int duration = (int) ((System.currentTimeMillis() - SpeedVLDate) / (1000 * 60));
                int score = 0;
                if (duration > 10) {
                    score += ((duration / 10)) * 4 + 2;
                } else if (duration > 2) {
                    score += 2;
                }
                if (MaxSpeed > PostedSpeed + 40) {
                    score += 32;
                } else if (MaxSpeed > PostedSpeed + 30 && MaxSpeed <= PostedSpeed + 40) {
                    score += 14;
                } else if (MaxSpeed > PostedSpeed + 20 && MaxSpeed <= PostedSpeed + 30) {
                    score += 5;
                } else if (MaxSpeed >= PostedSpeed + 10 && MaxSpeed <= PostedSpeed + 20) {
                    score += 1;
                }
                AlertDB.Update("SpeedVL", duration, score);
            } else {
                if (speed > MaxSpeed) {
                    MaxSpeed = speed;
                }
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
