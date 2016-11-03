package com.hutchgroup.elog.beans;

/**
 * Created by Dev-1 on 6/17/2016.
 */
public class TimeZoneBean {
    private String timeZoneName;
    private String timeZoneValue;

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    private String timeZoneId;
    float timeZoneOffset;

    public String getTimeZoneName() {
        return timeZoneName;
    }

    public void setTimeZoneName(String timeZoneName) {
        this.timeZoneName = timeZoneName;
    }

    public String getTimeZoneValue() {
        return timeZoneValue;
    }

    public void setTimeZoneValue(String timeZoneValue) {
        this.timeZoneValue = timeZoneValue;
    }

    public float getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(float timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }
}
