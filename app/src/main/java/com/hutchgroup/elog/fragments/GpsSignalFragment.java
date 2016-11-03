package com.hutchgroup.elog.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.GpsSatelliteAdapter;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.tasks.GeocodeTask;
import com.hutchgroup.elog.util.AnimationUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class GpsSignalFragment extends Fragment implements View.OnClickListener, LocationListener, GpsStatus.NmeaListener, GpsStatus.Listener {
    final String TAG = GpsSignalFragment.class.getName();

    private final int SECONDS_TO_MILLISECONDS = 1000;

    private OnFragmentInteractionListener mListener;

    private LocationManager mService;

    private LocationProvider mProvider;

    private long minTime; // Min Time between location updates, in milliseconds

    private float minDistance; // Min Distance between location updates, in meters

    private GpsStatus mStatus;

    public boolean mStarted;

    TextView tvFindingSatellite;
    TextView tvUTCTime;
    TextView tvFindingSatelliteValue;
    TextView tvUTCTimeValue;
    TextView tvLatitude;
    TextView tvLongitude;
    TextView tvLatitudeValue;
    TextView tvLongitudeValue;
    TextView tvCurrentLocation;
    TextView tvCurrentLocationValue;
    ImageView ivFindingSatellite;
    ImageView ivUTCTime;
    ImageView ivLatitude;
    ImageView ivLongitude;
    ImageView ivCurrentLocation;

    private GpsSatelliteAdapter mAdapter;
    List<GpsSatellite> listSatellites;

    double latitude;
    double longitude;

    int countCurrentLocationFailed;
    boolean isGeoCalled;

    ImageButton butGPSNext;

    GeocodeTask.PostTaskListener<Address> geocodeTaskListener = new GeocodeTask.PostTaskListener<Address>() {
        @Override
        public void onPostTask(Address address) {
            if (address == null) {
                countCurrentLocationFailed++;
                if (countCurrentLocationFailed < 5) {
                    callGeocodeTask();
                }
            } else {
                String addressName = "";

                String add = address.getAddressLine(0);
                String city = address.getLocality();
                String state = address.getAdminArea();
                String country = address.getCountryName();
                String postalCode = address.getPostalCode();
                addressName = add + ", " + city + ", " + state + ", " + country + " " + postalCode;

                tvFindingSatellite.setTextColor(getResources().getColor(R.color.green2));
                tvFindingSatelliteValue.setTextColor(getResources().getColor(R.color.green2));
                ivFindingSatellite.setBackgroundResource(R.drawable.ic_setup_passed);
                tvCurrentLocation.setVisibility(View.VISIBLE);
                tvCurrentLocationValue.setVisibility(View.VISIBLE);
                ivCurrentLocation.setVisibility(View.VISIBLE);
                ivCurrentLocation.setBackgroundResource(R.drawable.ic_setup_passed);
                tvCurrentLocationValue.setText(addressName);

                if (mListener != null) {
                    mListener.onUpdateCurrentLocationStatus(true, countCurrentLocationFailed);
                }

                butGPSNext.setVisibility(View.VISIBLE);
                butGPSNext.setEnabled(true);
            }
        }
    };

    public GpsSignalFragment() {

    }

    private void initialize(View view) {
        try {
            tvFindingSatellite = (TextView) view.findViewById(R.id.tvFindingSatellite);
            tvFindingSatelliteValue = (TextView) view.findViewById(R.id.tvFindingSatelliteValue);
            tvFindingSatelliteValue.setVisibility(View.GONE);
            ivFindingSatellite = (ImageView) view.findViewById(R.id.icFindingSatellite);

            tvUTCTime = (TextView) view.findViewById(R.id.tvCheckingUTCTime);
            tvUTCTime.setVisibility(View.GONE);
            tvUTCTimeValue = (TextView) view.findViewById(R.id.tvCheckingUTCTimeValue);
            tvUTCTimeValue.setVisibility(View.GONE);
            ivUTCTime = (ImageView) view.findViewById(R.id.icCheckingUTCTime);
            ivUTCTime.setVisibility(View.GONE);
            tvLatitude = (TextView) view.findViewById(R.id.tvCheckingLatitude);
            tvLatitude.setVisibility(View.GONE);
            tvLatitudeValue = (TextView) view.findViewById(R.id.tvCheckingLatitudeValue);
            tvLatitudeValue.setVisibility(View.GONE);
            ivLatitude = (ImageView) view.findViewById(R.id.icCheckingLatitude);
            ivLatitude.setVisibility(View.GONE);
            tvLongitude = (TextView) view.findViewById(R.id.tvCheckingLongitude);
            tvLongitude.setVisibility(View.GONE);
            tvLongitudeValue = (TextView) view.findViewById(R.id.tvCheckingLongitudeValue);
            tvLongitudeValue.setVisibility(View.GONE);
            ivLongitude = (ImageView) view.findViewById(R.id.icCheckingLongitude);
            ivLongitude.setVisibility(View.GONE);
            tvCurrentLocation = (TextView) view.findViewById(R.id.tvCheckingLocation);
            tvCurrentLocation.setVisibility(View.GONE);
            tvCurrentLocationValue = (TextView) view.findViewById(R.id.tvCheckingLocationValue);
            tvCurrentLocationValue.setVisibility(View.GONE);
            ivCurrentLocation = (ImageView) view.findViewById(R.id.icCheckingLocation);
            ivCurrentLocation.setVisibility(View.GONE);

            tvFindingSatellite.setTextColor(getResources().getColor(R.color.yellow2));
            tvFindingSatelliteValue.setTextColor(getResources().getColor(R.color.yellow2));
            AnimationUtil.startSetupProcessingAnimation(getResources(), ivFindingSatellite);

            butGPSNext = (ImageButton) view.findViewById(R.id.butGPSNext);
            butGPSNext.setOnClickListener(this);
            butGPSNext.setVisibility(View.GONE);

            countCurrentLocationFailed = 0;
            isGeoCalled = false;
        } catch (Exception e) {
            LogFile.write(GpsSignalFragment.class.getName() + "::initialize Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    public static GpsSignalFragment newInstance() {
        GpsSignalFragment fragment = new GpsSignalFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_gps_check, container, false);

        mService = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mProvider = mService.getProvider(LocationManager.GPS_PROVIDER);
        if (mProvider == null) {
            Log.e(TAG, "Unable to get GPS_PROVIDER");
            Toast.makeText(getContext(), getString(R.string.gps_not_supported), Toast.LENGTH_SHORT).show();

            //return;
        }

        mService.addGpsStatusListener(this);
        mService.addNmeaListener(this);

        ListView listView = (ListView) view.findViewById(R.id.sv_grid);

//        View inforHeader = inflater.inflate(R.layout.activity_gps_check, null, false);
//        listView.addHeaderView(inforHeader);

        initialize(view);
        //List<GpsSatellite> list = new ArrayList<GpsSatellite>();
        listSatellites = new ArrayList<GpsSatellite>();

        mAdapter = new GpsSatelliteAdapter(getContext(), getListSatellites());
        listView.setAdapter(mAdapter);
        listView.setFocusable(false);
        listView.setFocusableInTouchMode(false);

        Utility.context = getContext();
        Utility.checkAndGrantPermissions();

        minTime = (long) (1000);
        minDistance = 0;

        gpsStart();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mService.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            promptEnableGps();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.butGPSNext:
                if (mListener != null) {
                    mListener.onNextToSummary();
                }
                break;
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mService.removeGpsStatusListener(this);
        mService.removeUpdates(this);
    }

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

        }

    }

    private void turnGPSOn() {
        try {
            String provider = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

            if (!provider.contains("gps")) { //if gps is disabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                getActivity().sendBroadcast(poke);
            }
        } catch (Exception e) {
            LogFile.write(GpsSignalFragment.class.getName() + "::turnGPSOn Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        Log.i("GPS", "onNmeaReceived " + nmea);
        if (nmea.replace("\r\n", "").contains("GPRMC")) {
            String[] list = nmea.split(",");
            if (list.length > 0) {
                if (list[2].equals("A")) {
                    try {
                        String time = list[1];
                        //Log.i("GPS", "time=" + time)
                        String date = list[9];
                        //Log.i("GPS", "date=" + date);

                        String dateTime = date + " " + time.substring(0, time.lastIndexOf("."));
                        //Log.i("GPS", "dateTime=" + dateTime);
                        SimpleDateFormat dtFormat = new java.text.SimpleDateFormat("ddMMyy HHmmss");
                        SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        //Log.i("GPS", "gps time=" + sdf.format(dtFormat.parse(dateTime)));
                        String gpsTime = sdf.format(dtFormat.parse(dateTime));
                        //convert from GPS time to local time
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date parsed = sdf.parse(gpsTime);

                        Calendar c = Calendar.getInstance();
                        TimeZone tz = c.getTimeZone();
                        SimpleDateFormat destFormat = new SimpleDateFormat("HH:mm:ss");
                        destFormat.setTimeZone(tz);
                        String localTime = destFormat.format(parsed);

                        tvUTCTime.setVisibility(View.VISIBLE);
                        tvUTCTimeValue.setVisibility(View.VISIBLE);
                        ivUTCTime.setVisibility(View.VISIBLE);
                        ivUTCTime.setBackgroundResource(R.drawable.ic_setup_passed);

                        tvUTCTimeValue.setText(localTime);
                        if (mListener != null) {
                            mListener.onUpdateUTCTimeStatus(true);
                        }
                    } catch (Exception e) {

                    }
                }
            }
        } else if (nmea.replace("\r\n", "").contains("GPGSA")) {
            String[] list = nmea.split(",");
            if (list.length > 0) {
                tvFindingSatelliteValue.setVisibility(View.VISIBLE);
                if (list[2].equals("1")) {
                    tvFindingSatelliteValue.setText("No fix");
                } else {
                    tvFindingSatelliteValue.setText(list[2] + "D fix");
                    if (mListener != null) {
                        mListener.onUpdateFindingStatelliteStatus(true);
                    }
                }

            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        tvLatitude.setVisibility(View.VISIBLE);
        ivLatitude.setVisibility(View.VISIBLE);
        tvLatitudeValue.setVisibility(View.VISIBLE);
        tvLongitude.setVisibility(View.VISIBLE);
        tvLongitudeValue.setVisibility(View.VISIBLE);
        ivLongitude.setVisibility(View.VISIBLE);

        ivLatitude.setBackgroundResource(R.drawable.ic_setup_passed);
        ivLongitude.setBackgroundResource(R.drawable.ic_setup_passed);

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        tvLatitudeValue.setText(doubleToString(location.getLatitude(), 7));
        tvLongitudeValue.setText(doubleToString(location.getLongitude(), 7));
        if (mListener != null) {
            mListener.onUpdateLatitudeLongitudeStatus(true);
        }
        //get current location
        try {
            //Log.i(TAG, "Get location failed " + countCurrentLocationFailed);
            if (countCurrentLocationFailed < 5) {
                if (tvCurrentLocationValue.getText().toString().equals("")) {
                    if (!isGeoCalled) {
                        //Log.i(TAG, "callGeocodeTask in locationChanged");
                        callGeocodeTask();
                        isGeoCalled = true;
                    }
                }
            } else {
                //Log.i(TAG, "Current location failed " + countCurrentLocationFailed);
                tvFindingSatellite.setTextColor(getResources().getColor(R.color.green2));
                tvFindingSatelliteValue.setTextColor(getResources().getColor(R.color.green2));
                ivFindingSatellite.setBackgroundResource(R.drawable.ic_setup_passed);

                if (mListener != null) {
                    mListener.onUpdateCurrentLocationStatus(false, countCurrentLocationFailed);
                }

                butGPSNext.setVisibility(View.VISIBLE);
                butGPSNext.setEnabled(true);
            }

        } catch (Exception e) {
            Log.i(TAG, "Error: " + e.getMessage());
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {
        mStatus = mService.getGpsStatus(mStatus);

        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                break;

            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                updateStatus(mStatus);
                break;
        }
    }

    private List<GpsSatellite> getListSatellites() {
        List<GpsSatellite> list = new ArrayList<GpsSatellite>();

        for (int i = 0; i < listSatellites.size(); i++) {
            list.add(listSatellites.get(i));
        }

        return list;
    }

    private void updateStatus(GpsStatus status) {
        mAdapter.notifyDataSetChanged();

        Iterator<GpsSatellite> satellites = status.getSatellites().iterator();
        listSatellites.clear();
        while (satellites.hasNext()) {
            GpsSatellite satellite = satellites.next();
            listSatellites.add(satellite);
        }

        mAdapter.changeItems(getListSatellites());

        mAdapter.notifyDataSetChanged();
    }

    private String doubleToString(double value, int decimals) {
        String result = Double.toString(value);
        // truncate to specified number of decimal places
        int dot = result.indexOf('.');
        if (dot > 0) {
            int end = dot + decimals + 1;
            if (end < result.length()) {
                result = result.substring(0, end);
            }
        }
        return result;
    }

    private void callGeocodeTask() {
        Log.i(TAG, "callGeocodeTask " + countCurrentLocationFailed);
        new GeocodeTask(geocodeTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onNextToSummary();

        void onUpdateFindingStatelliteStatus(boolean isPassed);
        void onUpdateUTCTimeStatus(boolean isPassed);
        void onUpdateLatitudeLongitudeStatus(boolean isPassed);
        void onUpdateCurrentLocationStatus(boolean isPassed, int numberFailed);
    }

}
