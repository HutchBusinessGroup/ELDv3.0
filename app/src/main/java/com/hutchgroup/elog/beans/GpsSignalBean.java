package com.hutchgroup.elog.beans;

/**
 * Created by Vaneet.Sethi on 4/6/2016.
 */
public class GpsSignalBean {
    private int _id;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_gpsSignal() {
        return _gpsSignal;
    }

    public void set_gpsSignal(String _gpsSignal) {
        this._gpsSignal = _gpsSignal;
    }

    private String _gpsSignal;
}
