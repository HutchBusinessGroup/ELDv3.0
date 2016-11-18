package com.hutchgroup.elog.common;

import android.content.res.XmlResourceParser;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.TimeZoneBean;

import org.xmlpull.v1.XmlPullParserException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Dev-1 on 8/11/2016.
 */
public class ZoneList {
    private static final String XMLTAG_TIMEZONE = "timezone";

    private static final int HOURS_1 = 60 * 60000;

    // Created By: Deepak Sharma
    // Created Date: 11 Aug 2016
    // Purpose: get time zone list
    public static ArrayList<TimeZoneBean> getZones() {
        ArrayList<TimeZoneBean> myData = new ArrayList<>();
        long date = Calendar.getInstance().getTimeInMillis();
        try {
            XmlResourceParser xrp = Utility.context.getResources().getXml(R.xml.timezones);
            while (xrp.next() != XmlResourceParser.START_TAG)
                continue;
            xrp.next();
            while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                    if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                        return myData;
                    }
                    xrp.next();
                }
                if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
                    String id = xrp.getAttributeValue(0);
                    String displayName = xrp.nextText();
                    addItem(myData, id, displayName, date);
                }
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    xrp.next();
                }
                xrp.next();
            }
            xrp.close();
        } catch (XmlPullParserException xppe) {
        } catch (java.io.IOException ioe) {

        }

        return myData;
    }

    // Created By: Deepak Sharma
    // Created Date: 11 Aug 2016
    // Purpose: add time zone to  list
    private static void addItem(ArrayList<TimeZoneBean> myData, String id, String displayName,
                                long date) {
        TimeZone tz = TimeZone.getTimeZone(id);
        int offset = tz.getOffset(date);
        int p = Math.abs(offset);
        StringBuilder name = new StringBuilder();
        name.append("UTC");

        if (offset < 0) {
            name.append('-');
        } else {
            name.append('+');
        }

        name.append(p / (HOURS_1));
        name.append(':');

        int min = p / 60000;
        min %= 60;

        if (min < 10) {
            name.append('0');
        }
        name.append(min);
        TimeZoneBean bean = new TimeZoneBean();
        bean.setTimeZoneId(id);
        bean.setTimeZoneName(displayName);
        bean.setTimeZoneValue(name.toString());
        bean.setTimeZoneOffset(offset / 3600000f);
        myData.add(bean);
    }

    // Created By: Deepak Sharma
    // Created Date: 11 Aug 2016
    // Purpose: get time zone offset of device
    public static String getTimeZoneOffset() {
        StringBuilder name = new StringBuilder();
        name.append("UTC ");
        long date = Calendar.getInstance().getTimeInMillis();
        TimeZone timeZone = TimeZone.getDefault();
        int offset = timeZone.getOffset(date);
        int p = Math.abs(offset);
        if (offset < 0) {
            name.append('-');
        } else {
            name.append('+');
        }
        int hours = p / (HOURS_1);
        if (hours < 10) {
            name.append('0');
        }

        name.append(hours);
        name.append(':');

        int min = p / 60000;
        min %= 60;

        if (min < 10) {
            name.append('0');
        }
        name.append(min);

        return name.toString();
    }

    // Created By: Deepak Sharma
    // Created Date: 11 Aug 2016
    // Purpose: get time zone offset by timeZoneId
    public static String getTimeZoneOffset(String timeZoneId) {
        StringBuilder name = new StringBuilder();
        name.append("UTC ");
        long date = Calendar.getInstance().getTimeInMillis();
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
        int offset = timeZone.getOffset(date);
        int p = Math.abs(offset);
        if (offset < 0) {
            name.append('-');
        } else {
            name.append('+');
        }
        int hours = p / (HOURS_1);
        if (hours < 10) {
            name.append('0');
        }

        name.append(hours);
        name.append(':');

        int min = p / 60000;
        min %= 60;

        if (min < 10) {
            name.append('0');
        }
        name.append(min);

        return name.toString();
    }

    // Created By: Deepak Sharma
    // Created Date: 11 Aug 2016
    // Purpose: get time zone offset of device
    public static int getOffset(String timeZoneId) {

        long date = Calendar.getInstance().getTimeInMillis();
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
        int offset = timeZone.getOffset(date);
        return offset;
    }
}
