package com.hutchgroup.elog.common;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.hutchgroup.elog.MainActivity;
import com.hutchgroup.elog.beans.DiagnosticIndicatorBean;
import com.hutchgroup.elog.beans.GPSData;
import com.hutchgroup.elog.beans.GpsSignalBean;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.TrackingDB;
import com.hutchgroup.elog.fragments.ELogFragment;
import com.hutchgroup.elog.tracklocations.ClientSocket;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class GPSTracker extends Service implements LocationListener {

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;// 1609 * 4; // 4 Miles

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000;//1000 * 60; //* 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public static String gpsTime;
    Context context;
    private final float MAX_DIFFERENT_TIME = 10f; //10 mins
    private final float MAX_DIFFERENT_GPS_TIME = 60f; //60 mins

    public GPSTracker() {

    }

    static public Map<String, String> mStateMap = null;

    private void populateStates() {
        if (mStateMap == null) {
            mStateMap = new HashMap<String, String>();
            mStateMap.put("Alabama", "AL");
            mStateMap.put("Alaska", "AK");
            mStateMap.put("Alberta", "AB");
            mStateMap.put("American Samoa", "AS");
            mStateMap.put("Arizona", "AZ");
            mStateMap.put("Arkansas", "AR");
            mStateMap.put("Armed Forces (AE)", "AE");
            mStateMap.put("Armed Forces Americas", "AA");
            mStateMap.put("Armed Forces Pacific", "AP");
            mStateMap.put("British Columbia", "BC");
            mStateMap.put("California", "CA");
            mStateMap.put("Colorado", "CO");
            mStateMap.put("Connecticut", "CT");
            mStateMap.put("Delaware", "DE");
            mStateMap.put("District Of Columbia", "DC");
            mStateMap.put("Florida", "FL");
            mStateMap.put("Georgia", "GA");
            mStateMap.put("Guam", "GU");
            mStateMap.put("Hawaii", "HI");
            mStateMap.put("Idaho", "ID");
            mStateMap.put("Illinois", "IL");
            mStateMap.put("Indiana", "IN");
            mStateMap.put("Iowa", "IA");
            mStateMap.put("Kansas", "KS");
            mStateMap.put("Kentucky", "KY");
            mStateMap.put("Louisiana", "LA");
            mStateMap.put("Maine", "ME");
            mStateMap.put("Manitoba", "MB");
            mStateMap.put("Maryland", "MD");
            mStateMap.put("Massachusetts", "MA");
            mStateMap.put("Michigan", "MI");
            mStateMap.put("Minnesota", "MN");
            mStateMap.put("Mississippi", "MS");
            mStateMap.put("Missouri", "MO");
            mStateMap.put("Montana", "MT");
            mStateMap.put("Nebraska", "NE");
            mStateMap.put("Nevada", "NV");
            mStateMap.put("New Brunswick", "NB");
            mStateMap.put("New Hampshire", "NH");
            mStateMap.put("New Jersey", "NJ");
            mStateMap.put("New Mexico", "NM");
            mStateMap.put("New York", "NY");
            mStateMap.put("Newfoundland", "NF");
            mStateMap.put("North Carolina", "NC");
            mStateMap.put("North Dakota", "ND");
            mStateMap.put("Northwest Territories", "NT");
            mStateMap.put("Nova Scotia", "NS");
            mStateMap.put("Nunavut", "NU");
            mStateMap.put("Ohio", "OH");
            mStateMap.put("Oklahoma", "OK");
            mStateMap.put("Ontario", "ON");
            mStateMap.put("Oregon", "OR");
            mStateMap.put("Pennsylvania", "PA");
            mStateMap.put("Prince Edward Island", "PE");
            mStateMap.put("Puerto Rico", "PR");
            mStateMap.put("Quebec", "PQ");
            mStateMap.put("Rhode Island", "RI");
            mStateMap.put("Saskatchewan", "SK");
            mStateMap.put("South Carolina", "SC");
            mStateMap.put("South Dakota", "SD");
            mStateMap.put("Tennessee", "TN");
            mStateMap.put("Texas", "TX");
            mStateMap.put("Utah", "UT");
            mStateMap.put("Vermont", "VT");
            mStateMap.put("Virgin Islands", "VI");
            mStateMap.put("Virginia", "VA");
            mStateMap.put("Washington", "WA");
            mStateMap.put("West Virginia", "WV");
            mStateMap.put("Wisconsin", "WI");
            mStateMap.put("Wyoming", "WY");
            mStateMap.put("Yukon Territory", "YT");
        }
    }

    public void startProgress() {

        // do something long
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {
                        Thread.sleep(60000);

                        if (Utility.isInternetOn()) {
                            ArrayList<GpsSignalBean> list = TrackingDB.getGpsSignalList();
                            StringBuilder sbIds = new StringBuilder();
                            StringBuilder sbSignals = new StringBuilder();

                            for (int i = 0; i < list.size(); i++) {
                                GpsSignalBean s = list.get(i);
                                sbIds.append(s.get_id()).append(",");
                                sbSignals.append(s.get_gpsSignal());
                            }

                            if (list.size() > 0) {
                                list.clear();

                                sbIds.setLength(sbIds.length() - 1);
                                String ids = sbIds.toString();
                                String signals = sbSignals.toString();

                                sbIds.setLength(0);
                                sbSignals.setLength(0);

                                ClientSocket obj = new ClientSocket(
                                        Utility.ServerIp, Utility.Port, context);
                                obj.execute(signals, ids);
                            }
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thGps = new Thread(runnable);
        thGps.setName("GpsTracker-Gps");
        thGps.start();
    }

    public void startUsingGPS(Context context) {
        try {
            this.context = context;
            populateStates();
            locationManager = (LocationManager) context
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, GPSTracker.this);

                try {
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location == null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    if (location != null) {
                        Utility.currentLocation.setLatitude(location.getLatitude());
                        Utility.currentLocation.setLongitude(location.getLongitude());
                        gpsTime = getUTCDateFromMilisecond(location.getTime());
                        Utility.currentLocation.setValidLocationDate(gpsTime);

                    } else {
                        gpsTime = Utility.getCurrentUTCDateTime();
                        Utility.currentLocation.setValidLocationDate(gpsTime);

                    }
                } catch (Exception e) {
                }

            } /*else {
                Utility.showAlertMsg("Please Enable GPS.");
            }*/
            startProgress();

            thGpsOut = new Thread(checkGPS);

            thGpsOut.setName("GPS-Invalid-check");
            thGpsOut.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    long lastLocationTime = 0;

    @Override
    public void onLocationChanged(Location location) {
        if (Utility.vehicleId == 0)
            return;
        // check time compliance
        gpsTime = getUTCDateFromMilisecond(location.getTime());
        float difference = (location.getTime() - lastLocationTime) / 60000f;
        Utility.currentLocation.setValidLocationDate(gpsTime);
        Utility.currentLocation.setLatitude(location.getLatitude());
        Utility.currentLocation.setLongitude(location.getLongitude());
        Utility.currentLocation.setAccuracy(location.getAccuracy());
        Utility.currentLocation.setBearing(location.getBearing());
        if (difference >= 1) {
            boolean invalidFg = location.getAccuracy() > 500;// we assume invalid location if accuracy is more than 500 meters

            lastLocationTime = location.getTime();
            checkPositioningCompliance(invalidFg);

            checkTimingCompliance();
            ConvertGPSData(location);

            Log.i("GpsLocation: ", "location saved");
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 14 July 2016
    // Purpose: check for positioning compliance
    private void checkPositioningCompliance(boolean invalidFg) {
        if (invalidFg) {
            if (Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate().equals("0")) {
                Utility.currentLocation.setOdometerReadingSinceLastValidCoordinate(CanMessages.OdometerReading);
            }

            double distance = (double) (Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.currentLocation.getOdometerReadingSinceLastValidCoordinate()).intValue()) * 0.621371d; // distance travelled in miles since last valid location
            if (distance > 5) {
                Utility.currentLocation.setLatitude(-1);
                Utility.currentLocation.setLongitude(-1); //0 means M, -1 means X,-2 means E
                Utility.currentLocation.setLocationDescription("");

                String currentDateUTC = Utility.getCurrentUTCDateTime();
                String validLocationDate = Utility.currentLocation.getValidLocationDate();

                float diffTime = Utility.getDiffTime(validLocationDate, currentDateUTC) * 60f; // in minutes
                Log.i("GPS", "TimeDiff: " + diffTime);
                if (diffTime > MAX_DIFFERENT_GPS_TIME) {
                    Utility.currentLocation.setLatitude(-2);
                    Utility.currentLocation.setLongitude(-2);
                    Utility.currentLocation.setLocationDescription("");

                    if (!DiagnosticIndicatorBean.PositioningMalfunctionFg) {
                        DiagnosticIndicatorBean.PositioningMalfunctionFg = true;
                        DiagnosticMalfunction.saveDiagnosticIndicatorByCode("L", 1, "PositioningMalfunctionFg");
                    }
                }

            }
        } else {

            Utility.currentLocation.setOdometerReadingSinceLastValidCoordinate(CanMessages.OdometerReading);
            Utility.currentLocation.setEngineHoursSinceLastValidCoordinate(CanMessages.EngineHours);
            Utility.currentLocation.setValidLocationDate(gpsTime);

            if (DiagnosticIndicatorBean.PositioningMalfunctionFg) {
                DiagnosticIndicatorBean.PositioningMalfunctionFg = false;
                DiagnosticMalfunction.saveDiagnosticIndicatorByCode("L", 2, "PositioningMalfunctionFg");
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    Thread thGpsOut;

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void ConvertGPSData(Location location) {
        try {
            //1-Driving, 2-Idling,3-PowerOff
            setCurrentStatus();
            setStatusCode();
            double latitude = (location.getLatitude() < 0 ? -location.getLatitude() : location.getLatitude());
            double longitude = (location.getLongitude() < 0 ? -location.getLongitude() : location.getLongitude());
            String signal = Utility.IMEI + "," +// IMEI
                    Utility._productCode + ":" + Utility.ApplicationVersion + "," + // Product Code
                    "1G," + // Message Type
                    MilisecondsToDate(location.getTime()) + "," +  // Signal Date
                    "A," + // GPS Status
                    latitude + "," +
                    (location.getLatitude() > 0 ? "N" : "S") + "," +
                    longitude + "," +
                    (location.getLongitude() > 0 ? "E" : "W") + "," +
                    CanMessages.Speed + "," + //can Speed
                    location.getBearing() + "," + // Heading
                    CanMessages.OdometerReading + "," + // Odometer Reading
                    location.getAccuracy() + "," + //accuracy
                    location.getSpeed() + "," + // gps speed
                    GPSData.PostRoadSpeed + "," + // Post Road speed
                    CanMessages.EngineHours + "," +
                    CanMessages.TotalFuelConsumed + "," +
                    CanMessages.TotalIdleHours + "," +
                    CanMessages.TotalIdleFuelConsumed + "," +
                    CanMessages.TotalAverage + "," +
                    MilisecondsToDate(GPSData.LastStatusTime) + "," +
                    GPSData.ETATimeRemaining + "," +
                    GPSData.CurrentStatus + "," +
                    GPSData.CurrentStatusRemaining + "," +
                    Utility.activeUserId + "," +
                    Utility.vehicleId + "," +
                    GPSData.StatusCode + ";"; // Status Code Its Static value

            TrackingDB.addGpsSignal(signal);


        } catch (Exception exe) {

        }
    }

    // Deepak Sharma
    // 21 July 2016
    // set status code for gps data
    private static void setStatusCode() {
        String code1 = Utility.convertBinaryToHex((GPSData.ACPowerFg == 0 ? "1" : "0") + "" + GPSData.TripInspectionCompletedFg + "" + (GPSData.NoHOSViolationFgFg == 1 ? "0" : "1") + "" + GPSData.CellOnlineFg);
        String code2 = Utility.convertBinaryToHex(GPSData.RoamingFg + "" + GPSData.DTCOnFg + "" + GPSData.WifiOnFg + "" + GPSData.TPMSWarningOnFg);
        String code3 = Utility.convertBinaryToHex((Utility.dataDiagnosticIndicatorFg ? "1" : "0") + (Utility.malFunctionIndicatorFg ? "1" : "0") + (Float.valueOf(CanMessages.RPM) == 0f ? "0" : "1") + "0");
        String code4 = "0";
        GPSData.StatusCode = code1 + code2 + code3 + code4;
    }

    private void setCurrentStatus() {
        //1-Driving, 2-Idling,3-Shutdown
        if (Utility.motionFg) {
            GPSData.CurrentStatus = 1;
        } else if (Float.valueOf(CanMessages.RPM) > 0f) {
            long difference = (System.currentTimeMillis() - GPSData.LastStatusTime) / (1000 * 60);
            if (difference > 10)
                GPSData.CurrentStatus = 2;
            else
                GPSData.CurrentStatus = 1;

        } else {
            GPSData.CurrentStatus = 3;
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 13 July 2016
    // Purpose: check for timing compliance
    private void checkTimingCompliance() {
        try {
            if (gpsTime != null) {
                String currentDateUTC = Utility.getCurrentUTCDateTime();

                float diffTime = Utility.getDiffTime(currentDateUTC, gpsTime) * 60f;

                if (Math.abs(diffTime) > MAX_DIFFERENT_TIME) {
                    if (!DiagnosticIndicatorBean.TimingMalfunctionFg) {
                        DiagnosticIndicatorBean.TimingMalfunctionFg = true;
                        // save malfunction for timing compliance
                        DiagnosticMalfunction.saveDiagnosticIndicatorByCode("T", 1, "TimingMalfunctionFg");
                    }
                } else {
                    if (DiagnosticIndicatorBean.TimingMalfunctionFg) {
                        // clear malfunction for timing compliance
                        DiagnosticIndicatorBean.TimingMalfunctionFg = false;
                        DiagnosticMalfunction.saveDiagnosticIndicatorByCode("T", 2, "TimingMalfunctionFg");
                    }
                }

            }
        } catch (Exception e) {
        }
    }

    private String getUTCDateFromMilisecond(long ms) {

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date(ms));
        return utcTime;
    }

    private static String MilisecondsToDate(long ms) {

        final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date(ms));
        return utcTime;
    }

    private static String getUtcTime() {

        final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date());
        return utcTime;
    }

    @Deprecated
    private void geoLocationGet() {

        Thread thGeo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    double latitude = Utility.currentLocation.getLatitude();
                    double longitude = Utility.currentLocation.getLongitude();
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                    List<Address> addresses = geocoder.getFromLocation(latitude,
                            longitude, 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        String countryCode = address.getCountryCode();
                        String addr = address.getAddressLine(0);
                        String city = address.getLocality();
                        String state = mStateMap.get(address.getAdminArea());
                        if (state == null) {
                            state = address.getAdminArea();
                        }

                        Utility.currentLocation.setLocationDescription(addr + ", " + city + ", " + state);

                        // automatic change rule
                        if (Utility._appSetting.getAutomaticRuleChange() == 1) {
                            if (countryCode == "CA" && ELogFragment.currentRule == 3) {
                                DailyLogDB.DailyLogRuleSave(Utility.user1.getAccountId(), 1, Utility.getCurrentDateTime(), Utility.getCurrentDateTime());
                                if (Utility.user2.getAccountId() > 0) {

                                    DailyLogDB.DailyLogRuleSave(Utility.user2.getAccountId(), 1, Utility.getCurrentDateTime(), Utility.getCurrentDateTime());
                                }
                                ELogFragment.currentRule = 1;
                            } else if (countryCode == "US" && ELogFragment.currentRule != 3) {
                                DailyLogDB.DailyLogRuleSave(Utility.user1.getAccountId(), 3, Utility.getCurrentDateTime(), Utility.getCurrentDateTime());
                                if (Utility.user2.getAccountId() > 0) {
                                    DailyLogDB.DailyLogRuleSave(Utility.user2.getAccountId(), 3, Utility.getCurrentDateTime(), Utility.getCurrentDateTime());
                                }

                                ELogFragment.currentRule = 1;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thGeo.setName("GPSTracker-Geo");
        thGeo.start();
    }


    Runnable checkGPS = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(60000);
                  /*  if (ConstantFlag.Flag_Development) {
                        CanMessages.OdometerReading = (Double.valueOf(CanMessages.OdometerReading).intValue() + 10) + "";
                        CanMessages.EngineHours = (System.currentTimeMillis() / 60000000) + 1 + "";
                    }*/

                    float odometer = Float.valueOf(CanMessages.OdometerReading);
                    float engineHours = Float.valueOf(CanMessages.EngineHours);

                    if (odometer > 0 && engineHours > 0) {
                        // save vehicle odometer reading and engine hours
                        Utility.saveVehicleCanInfo();
                    }
                    checkPositioningCompliance(true);
                } catch (Exception exe) {

                }
            }
        }
    };

    public static String getShutDownEvent() {
        String signal = "";
        try {
            GPSData.CurrentStatus = 4;
            GPSData.LastStatusTime = System.currentTimeMillis();
            setStatusCode();
            double latitude = (Utility.currentLocation.getLatitude() < 0 ? -Utility.currentLocation.getLatitude() : Utility.currentLocation.getLatitude());
            double longitude = (Utility.currentLocation.getLongitude() < 0 ? -Utility.currentLocation.getLongitude() : Utility.currentLocation.getLongitude());

            signal = Utility.IMEI + "," +// IMEI
                    Utility._productCode + ":" + Utility.ApplicationVersion + "," + // Product Code
                    "1G," + // Message Type
                    getUtcTime() + "," +  // Signal Date
                    "A," + // GPS Status
                    latitude + "," +
                    (Utility.currentLocation.getLatitude() > 0 ? "N" : "S") + "," +
                    longitude + "," +
                    (Utility.currentLocation.getLongitude() > 0 ? "E" : "W") + "," +
                    CanMessages.Speed + "," + //can Speed
                    Utility.currentLocation.getBearing() + "," + // Heading
                    CanMessages.OdometerReading + "," + // Odometer Reading
                    Utility.currentLocation.getAccuracy() + "," + //accuracy
                    CanMessages.Speed + "," + // gps speed
                    GPSData.PostRoadSpeed + "," + // Post Road speed
                    CanMessages.EngineHours + "," +
                    CanMessages.TotalFuelConsumed + "," +
                    CanMessages.TotalIdleHours + "," +
                    CanMessages.TotalIdleFuelConsumed + "," +
                    CanMessages.TotalAverage + "," +
                    MilisecondsToDate(GPSData.LastStatusTime) + "," +
                    GPSData.ETATimeRemaining + "," +
                    GPSData.CurrentStatus + "," +
                    GPSData.CurrentStatusRemaining + "," +
                    Utility.activeUserId + "," +
                    Utility.vehicleId + "," +
                    GPSData.StatusCode + ";"; // Status Code Its Static value


        } catch (Exception exe) {

        }
        return signal;
    }

}