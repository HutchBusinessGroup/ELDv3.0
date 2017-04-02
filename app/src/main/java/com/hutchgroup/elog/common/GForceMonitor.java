package com.hutchgroup.elog.common;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.renderscript.Double2;
import android.util.Log;

import com.hutchgroup.elog.beans.GPSData;

/**
 * Created by Deepak on 12/6/2016.
 */

public class GForceMonitor implements SensorEventListener {


    public GForceMonitor() {
        this.inR = new float[16];
        this.f49I = new float[16];
        this.gravity = new float[3];
        this.geomag = new float[3];
        this.orientVals = new float[3];
        this.pitch = 0.0d;
    }


    private IGForceMonitor mListener;

    public void setGForceChangeListener(IGForceMonitor listener) {
        this.mListener = listener;
    }

    float[] f49I;
    private float calX;
    private float calY;
    private float calZ;
    float[] geomag;
    float[] gravity;
    float[] inR;
    private float mLowPassX;
    private float mLowPassY;
    private float mLowPassZ;
    float[] orientVals;
    double pitch;
    public boolean highPassFilter = true;
    double lrForce = 0f;
    public static float _acc, _break, _left, _right;

    @Override
    public void onSensorChanged(SensorEvent se) {
        switch (se.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER /*1*/:
                this.gravity = se.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD /*2*/:
                this.geomag = se.values.clone();
                break;
        }

        // if on battery power no sensor
        if (GPSData.ACPowerFg == 0) {
            return;
        }

        if (se.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

        if (!(this.gravity == null || this.geomag == null)) {
            if (SensorManager.getRotationMatrix(this.inR, this.f49I, this.gravity, this.geomag)) {
                SensorManager.getOrientation(this.inR, this.orientVals);
                this.pitch = Math.toDegrees((double) this.orientVals[1]);
            }
        }

        this.mLowPassX = se.values[0];
        this.mLowPassY = se.values[1];
        this.mLowPassZ = se.values[2];

        if (highPassFilter) {
            this.calX = this.mLowPassX;
            this.calY = this.mLowPassY;
            this.calZ = this.mLowPassZ;
            highPassFilter = false;
        }

        lrForce = ((double) ((int) (((this.mLowPassX - this.calX) / 9.80665f) * 1000.0f))) / 1000.0d;
        float lrForcePrint = ((float) ((int) (Math.abs(lrForce) * 100.0d))) / 100.0f;
        boolean isLeftTurn = (lrForce > 0.0d);

        SharpTurnMonitor(lrForcePrint, isLeftTurn);

        if (this.pitch < -45.0d || this.pitch > 45.0d) {
            double adForceZ = ((double) ((int) (((this.mLowPassZ - this.calZ) / 9.80665f) * 1000.0f))) / 1000.0d;
            double tempPitch = Math.abs(this.pitch);
            double adForce = 0.0d;
            if (tempPitch > 45.0d && tempPitch <= 90.0d) {
                tempPitch -= 45.0d;
                adForce = (1.0d / Math.cos(0.017453292519943295d * tempPitch)) * adForceZ;

            } else if (tempPitch > 90.0d && tempPitch <= 135.0d) {
                tempPitch -= 90.0d;
                adForce = (1.0d / Math.cos(0.017453292519943295d * tempPitch)) * adForceZ;
            }
            float adForceZABS = ((float) ((int) (Math.abs(adForce) * 100.0d))) / 100.0f;
            boolean isAcc = adForce < 0.0d; // check if accelerating or brake

            abForceMonitor(adForceZABS, isAcc);

        } else {
            double adForceY = ((double) ((int) (((this.mLowPassY - this.calY) / 9.80665f) * 1000.0f))) / 1000.0d;

            double adForce = (1.0d / Math.cos(this.pitch * 0.017453292519943295d)) * adForceY;
            float adForceYABS = ((float) ((int) (Math.abs(adForce) * 100.0d))) / 100.0f;
            boolean isAcc = adForce > 0.0d;
            abForceMonitor(adForceYABS, isAcc);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private int lrDirectionChangeCount = 0;
    private long lrFirstDirectionChangeTime = 0;
    float SHARP_TURN_THRESHOLD = .25f;
    float SHARP_TURN_MAX_DURATION_THRESHOLD = 1000f;
    private static final int SHARP_TURN_MIN_DIRECTION_CHANGE = 2;

    private void SharpTurnMonitor(float left_right_Force, boolean isLeft) {
        if (isLeft) {
            _left = left_right_Force;
        } else {
            _right = left_right_Force;
        }

        if (left_right_Force > SHARP_TURN_THRESHOLD) {
            // get time
            long now = System.currentTimeMillis();
            // store first movement time
            if (lrFirstDirectionChangeTime == 0) {
                lrFirstDirectionChangeTime = now;
            }

            // check total duration
            long totalDuration = now - lrFirstDirectionChangeTime;
            if (totalDuration > SHARP_TURN_MAX_DURATION_THRESHOLD) {
                lrDirectionChangeCount++;
                if (lrDirectionChangeCount == SHARP_TURN_MIN_DIRECTION_CHANGE) {
                    if (isLeft) {
                        mListener.onLeftSharpTurn(left_right_Force);
                    } else {
                        mListener.onRightSharpTurn(left_right_Force);
                    }
                }
            }

        } else {
            lrFirstDirectionChangeTime = 0;
            lrDirectionChangeCount = 0;
        }
    }

    private int abDirectionChangeCount = 0;
    private long abFirstDirectionChangeTime = 0;

    float HARD_ACCLERATION_THRESHOLD = .25f;
    float HARD_BREAK_THRESHOLD = .40f;

    private static final int MAX_TOTAL_ACC_DURATION_OF_EVENT = 1000;
    private static final int MAX_TOTAL_DURATION_OF_EVENT = 1000;

    private static final int MIN_DIRECTION_CHANGE = 3;


    double speed = 0;

    private void abForceMonitor(float abForce, boolean isAcc) {
        if (isAcc) {
            _acc = abForce;
        } else {
            _break = abForce;
        }

        float threshold = isAcc ? HARD_ACCLERATION_THRESHOLD : HARD_BREAK_THRESHOLD;
        if (abForce > threshold) {
            // get time
            long now = System.currentTimeMillis();
            // store first movement time
            if (abFirstDirectionChangeTime == 0) {
                double speed = Double.valueOf(CanMessages.Speed);
                if (speed < 30)
                    return;
                abFirstDirectionChangeTime = now;
            }

            // check total duration
            long totalDuration = now - abFirstDirectionChangeTime;
            long durationThreshold = isAcc ? MAX_TOTAL_ACC_DURATION_OF_EVENT : MAX_TOTAL_DURATION_OF_EVENT;
            if (totalDuration > durationThreshold) {
                abDirectionChangeCount++;
                if (abDirectionChangeCount == MIN_DIRECTION_CHANGE) {

                    if (isAcc) {
                        mListener.onHardAcceleration(abForce);
                    } else {
                        mListener.onHardBrake(abForce);
                    }
                }
            }
        } else {
            abFirstDirectionChangeTime = 0;
            abDirectionChangeCount = 0;
        }
    }

    public static void resetValues() {
        _acc = _break = _left = _right = 0f;
    }

    public interface IGForceMonitor {

        void onLeftSharpTurn(float force);

        void onRightSharpTurn(float force);

        void onHardAcceleration(float force);

        void onHardBrake(float force);
    }
}
