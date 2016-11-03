/*
 * Copyright (C) 2008-2013 The Android Open Source Project,
 * Sean J. Barbeau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hutchgroup.elog.fragments;

import com.hutchgroup.elog.ELogApplication;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.fragments.GpsSkyFragment;
import com.hutchgroup.elog.fragments.GpsStatusFragment;
import com.hutchgroup.elog.util.GpsTestUtil;
import com.hutchgroup.elog.util.MathUtils;
import com.hutchgroup.elog.view.ViewPagerMapBevelScroll;
//import com.github.espiandev.showcaseview.ShowcaseView;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.hutchgroup.elog.R;

public class GpsFragment extends Fragment
        implements LocationListener, GpsStatus.Listener, SensorEventListener, View.OnClickListener, GpsStatus.NmeaListener {

    private static final String TAG = GpsFragment.class.getName();

    private GpsFragmentInteractionListener mListener;

    private static final int SECONDS_TO_MILLISECONDS = 1000;

    static boolean mIsLargeScreen = false;

    private static GpsFragment mInstance;

    // Holds sensor data
    private static float[] mRotationMatrix = new float[16];

    private static float[] mRemappedMatrix = new float[16];

    private static float[] mValues = new float[3];

    private static float[] mTruncatedRotationVector = new float[4];

    private static boolean mTruncateVector = false;

    public boolean mStarted;

    boolean mFaceTrueNorth;

    public String mTtff;

    public SectionsPagerAdapter mSectionsPagerAdapter;

    public ViewPagerMapBevelScroll mViewPager;

    TabLayout tabLayout;
    Button butNext;

    private LocationManager mService;

    private LocationProvider mProvider;

    private GpsStatus mStatus;

    private ArrayList<GpsSignalListener> mGpsTestListeners = new ArrayList<GpsSignalListener>();

    private Location mLastLocation;

    private GeomagneticField mGeomagneticField;

    private long minTime; // Min Time between location updates, in milliseconds

    private float minDistance; // Min Distance between location updates, in meters

    private SensorManager mSensorManager;

    public static GpsFragment getInstance() {
        return mInstance;
    }

    public void addListener(GpsSignalListener listener) {
        mGpsTestListeners.add(listener);
    }

    private synchronized void gpsStart() {
        if (!mStarted) {
            mService.requestLocationUpdates(mProvider.getName(), minTime, minDistance, this);
            mStarted = true;

            // Show Toast only if the user has set minTime or minDistance to something other than default values
            if (minTime != (long) (Double.valueOf(getString(R.string.pref_gps_min_time_default_sec))
                    * SECONDS_TO_MILLISECONDS) ||
                    minDistance != Float
                            .valueOf(getString(R.string.pref_gps_min_distance_default_meters))) {
                Toast.makeText(getActivity(), String.format(getString(R.string.gps_set_location_listener),
                        String.valueOf((double) minTime / SECONDS_TO_MILLISECONDS),
                        String.valueOf(minDistance)), Toast.LENGTH_SHORT).show();
            }

            // Show the indeterminate progress bar on the action bar until first GPS status is shown
            //setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);

            // Reset the options menu to trigger updates to action bar menu items
            //invalidateOptionsMenu();
        }
        for (GpsSignalListener listener : mGpsTestListeners) {
            listener.gpsStart();
        }
    }

    private synchronized void gpsStop() {
        if (mStarted) {
            mService.removeUpdates(this);
            mStarted = false;
            // Stop progress bar
            //setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);

            // Reset the options menu to trigger updates to action bar menu items
            //invalidateOptionsMenu();
        }
        for (GpsSignalListener listener : mGpsTestListeners) {
            listener.gpsStop();
        }
    }

    private boolean sendExtraCommand(String command) {
        return mService.sendExtraCommand(LocationManager.GPS_PROVIDER, command, null);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //getActivity().requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        mInstance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set the default values from the XML file if this is the first
        // execution of the app
        PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, false);

        mService = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mProvider = mService.getProvider(LocationManager.GPS_PROVIDER);
        if (mProvider == null) {
            Log.e(TAG, "Unable to get GPS_PROVIDER");
            Toast.makeText(getContext(), getString(R.string.gps_not_supported), Toast.LENGTH_SHORT).show();

            //return;
        }
        mService.addGpsStatusListener(this);
        mService.addNmeaListener(this);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        View view = inflater.inflate(R.layout.activity_gps_main, container, false);

        //  page adapter contains all the fragment registrations
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPagerMapBevelScroll) view.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the listener for when this tab is
            // selected.
            tabLayout.addTab(tabLayout.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i)));
        }

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        butNext = (Button) view.findViewById(R.id.btnNext);
        butNext.setOnClickListener(this);

        SharedPreferences settings = ELogApplication.getPrefs();

        double tempMinTime = Double.valueOf(
                settings.getString(getString(R.string.pref_key_gps_min_time),
                        getString(R.string.pref_gps_min_time_default_sec))
        );
        minTime = (long) (tempMinTime * SECONDS_TO_MILLISECONDS);
        minDistance = Float.valueOf(
                settings.getString(getString(R.string.pref_key_gps_min_distance),
                        getString(R.string.pref_gps_min_distance_default_meters))
        );

        if (settings.getBoolean(getString(R.string.pref_key_auto_start_gps), true)) {
            gpsStart();
        }

        return view;
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        Log.i("GPS", "onNmeaReceived " + nmea);
        for (GpsSignalListener listener : mGpsTestListeners) {
            listener.onNmeaReceived(timestamp, nmea);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (GpsTestUtil.isRotationVectorSensorSupported(getContext())) {
            // Use the modern rotation vector sensors
            Sensor vectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorManager.registerListener(this, vectorSensor, 16000); // ~60hz
        } else {
            // Use the legacy orientation sensors
            Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            if (sensor != null) {
                mSensorManager.registerListener(this, sensor,
                        SensorManager.SENSOR_DELAY_GAME);
            }
        }

        if (!mService.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            promptEnableGps();
        }

        /**
         * Check preferences to see how they should be initialized
         */
        SharedPreferences settings = ELogApplication.getPrefs();

        checkTrueNorth(settings);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GpsFragmentInteractionListener) {
            mListener = (GpsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnNext:
                if (mListener != null) {
                    mListener.onNextToUserPreference();
                }
                break;
        }
    }

    /**
     * Ask the user if they want to enable GPS
     */
    private void promptEnableGps() {
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.enable_gps_message))
                .setPositiveButton(getString(R.string.enable_gps_positive_button),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        }
                )
                .setNegativeButton(getString(R.string.enable_gps_negative_button),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                )
                .show();
    }

    private void checkTrueNorth(SharedPreferences settings) {
        mFaceTrueNorth = settings.getBoolean(getString(R.string.pref_key_true_north), true);
    }

    @Override
    public void onDestroy() {
        mService.removeGpsStatusListener(this);
        mService.removeUpdates(this);
        super.onDestroy();
    }

    public void onLocationChanged(Location location) {
        //Log.i("GPSFragment", "onLocationChanged " + Utility.getCurrentDateTime());
        mLastLocation = location;

        updateGeomagneticField();

        for (GpsSignalListener listener : mGpsTestListeners) {
            if (butNext != null) {
                if (mListener != null) {
                    mListener.onNextToUserPreference();
                }
                butNext.setEnabled(true);
            }
            listener.onLocationChanged(location);
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        for (GpsSignalListener listener : mGpsTestListeners) {
            listener.onStatusChanged(provider, status, extras);
        }
    }

    public void onProviderEnabled(String provider) {
        for (GpsSignalListener listener : mGpsTestListeners) {
            listener.onProviderEnabled(provider);
        }
    }

    public void onProviderDisabled(String provider) {
        for (GpsSignalListener listener : mGpsTestListeners) {
            listener.onProviderDisabled(provider);
        }
    }

    public void onGpsStatusChanged(int event) {
        mStatus = mService.getGpsStatus(mStatus);

        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                int ttff = mStatus.getTimeToFirstFix();
                if (ttff == 0) {
                    mTtff = "";
                } else {
                    ttff = (ttff + 500) / 1000;
                    mTtff = Integer.toString(ttff) + " sec";
                }
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                // Stop progress bar after the first status information is obtained
                //setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
                break;
        }

        for (GpsSignalListener listener : mGpsTestListeners) {
            listener.onGpsStatusChanged(event, mStatus);
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void onSensorChanged(SensorEvent event) {

        double orientation = Double.NaN;
        double tilt = Double.NaN;

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                // Modern rotation vector sensors
                if (!mTruncateVector) {
                    try {
                        SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
                    } catch (IllegalArgumentException e) {
                        // On some Samsung devices, an exception is thrown if this vector > 4 (see #39)
                        // Truncate the array, since we can deal with only the first four values
                        Log.e(TAG, "Samsung device error? Will truncate vectors - " + e);
                        mTruncateVector = true;
                        // Do the truncation here the first time the exception occurs
                        getRotationMatrixFromTruncatedVector(event.values);
                    }
                } else {
                    // Truncate the array to avoid the exception on some devices (see #39)
                    getRotationMatrixFromTruncatedVector(event.values);
                }

                if (getActivity() == null)
                    return;

                int rot = getActivity().getWindowManager().getDefaultDisplay().getRotation();
                switch (rot) {
                    case Surface.ROTATION_0:
                        // No orientation change, use default coordinate system
                        SensorManager.getOrientation(mRotationMatrix, mValues);
                        // Log.d(TAG, "Rotation-0");
                        break;
                    case Surface.ROTATION_90:
                        // Log.d(TAG, "Rotation-90");
                        SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_Y,
                                SensorManager.AXIS_MINUS_X, mRemappedMatrix);
                        SensorManager.getOrientation(mRemappedMatrix, mValues);
                        break;
                    case Surface.ROTATION_180:
                        // Log.d(TAG, "Rotation-180");
                        SensorManager
                                .remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_MINUS_X,
                                        SensorManager.AXIS_MINUS_Y, mRemappedMatrix);
                        SensorManager.getOrientation(mRemappedMatrix, mValues);
                        break;
                    case Surface.ROTATION_270:
                        // Log.d(TAG, "Rotation-270");
                        SensorManager
                                .remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_MINUS_Y,
                                        SensorManager.AXIS_X, mRemappedMatrix);
                        SensorManager.getOrientation(mRemappedMatrix, mValues);
                        break;
                    default:
                        // This shouldn't happen - assume default orientation
                        SensorManager.getOrientation(mRotationMatrix, mValues);
                        // Log.d(TAG, "Rotation-Unknown");
                        break;
                }
                orientation = Math.toDegrees(mValues[0]);  // azimuth
                tilt = Math.toDegrees(mValues[1]);
                break;
            case Sensor.TYPE_ORIENTATION:
                // Legacy orientation sensors
                orientation = event.values[0];
                break;
            default:
                // A sensor we're not using, so return
                return;
        }

        // Correct for true north, if preference is set
        if (mFaceTrueNorth && mGeomagneticField != null) {
            orientation += mGeomagneticField.getDeclination();
            // Make sure value is between 0-360
            orientation = MathUtils.mod((float) orientation, 360.0f);
        }

        for (GpsSignalListener listener : mGpsTestListeners) {
            listener.onOrientationChanged(orientation, tilt);
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void getRotationMatrixFromTruncatedVector(float[] vector) {
        System.arraycopy(vector, 0, mTruncatedRotationVector, 0, 4);
        SensorManager.getRotationMatrixFromVector(mRotationMatrix, mTruncatedRotationVector);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void updateGeomagneticField() {
        mGeomagneticField = new GeomagneticField((float) mLastLocation.getLatitude(),
                (float) mLastLocation.getLongitude(), (float) mLastLocation.getAltitude(),
                mLastLocation.getTime());
    }

    private void sendLocation() {
        if (mLastLocation != null) {
            Intent intent = new Intent(Intent.ACTION_SENDTO,
                    Uri.fromParts("mailto", "", null));
            String location = "http://maps.google.com/maps?geocode=&q=" +
                    Double.toString(mLastLocation.getLatitude()) + "," +
                    Double.toString(mLastLocation.getLongitude());
            intent.putExtra(Intent.EXTRA_TEXT, location);
            startActivity(intent);
        }
    }

    public interface GpsSignalListener extends LocationListener {

        public void gpsStart();

        public void gpsStop();

        public void onGpsStatusChanged(int event, GpsStatus status);

        public void onOrientationChanged(double orientation, double tilt);

        public void onNmeaReceived(long timestamp, String nmea);
    }

    public interface GpsFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
        void onNextToUserPreference();
    }

    /**
     * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to
     * one of the primary sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public static final int NUMBER_OF_TABS = 2; // Used to set up TabListener

        // Constants for the different fragments that will be displayed in tabs, in numeric order
        public static final int GPS_STATUS_FRAGMENT = 0;

        public static final int GPS_SKY_FRAGMENT = 1;

        // Maintain handle to Fragments to avoid recreating them if one already
        // exists
        Fragment gpsStatus, gpsSky;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case GPS_STATUS_FRAGMENT:
                    if (gpsStatus == null) {
                        gpsStatus = new GpsStatusFragment();
                    }
                    return gpsStatus;
                case GPS_SKY_FRAGMENT:
                    if (gpsSky == null) {
                        gpsSky = new GpsSkyFragment();
                    }
                    return gpsSky;
            }
            return null; // This should never happen
        }

        @Override
        public int getCount() {
            return NUMBER_OF_TABS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case GPS_STATUS_FRAGMENT:
                    return getString(R.string.gps_status_tab);
                case GPS_SKY_FRAGMENT:
                    return getString(R.string.gps_sky_tab);
            }
            return null; // This should never happen
        }
    }
}
