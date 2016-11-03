package com.hutchgroup.elog.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.drawable.AnimationDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.hutchgroup.elog.ELogApplication;
import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.BluetoothDeviceAdapter;
import com.hutchgroup.elog.adapters.BluetoothPairedDeviceAdapter;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.Constants;
import com.hutchgroup.elog.common.DiagnosticMalfunction;
import com.hutchgroup.elog.common.GPSTracker;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Mail;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;
import com.hutchgroup.elog.tasks.MailSync;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class SummaryFragment extends Fragment implements View.OnClickListener {
    final String TAG = SummaryFragment.class.getName();

    OnFragmentInteractionListener mListener;

    TextView tvFindingSatellite;
    TextView tvUTCTime;
    TextView tvLatitudeLongitude;
    TextView tvCurrentLocation;
    ImageView ivFindingSatellite;
    ImageView ivUTCTime;
    ImageView ivLatitudeLongitude;
    ImageView ivCurrentLocation;
    ImageView icCurrentLocation;

    TextView tvBluetoothEnable;
    TextView tvBTBHeartbeat;
    TextView tvEstablishedConnection;
    ImageView ivBluetoothEnable;
    ImageView ivBTBHeartbeat;
    ImageView ivEstablishedConnection;
    //TextView tvSearchingBluetooth;
    //ImageView ivSearchingBluetooth;

    //TextView tvCellularType;
    TextView tvCellularConnection;
    TextView tvWifiConnection;
    TextView tvHutchConnection;
    TextView tvDownloadConfiguration;

    ImageView ivCellularConnection;
    ImageView ivWifiConnection;
    ImageView ivHutchConnection;
    ImageView ivDownloadConfiguration;

    Button butProceed;

    boolean cellularPassed;
    boolean wifiPassed;
    boolean hutchPassed;
    boolean downloadPassed;
    boolean bluetoothOnOff;
    //boolean bluetoothSearch;
    boolean bluetoothConnected;
    boolean btbHearBeat;
    boolean gpsSatellite;
    boolean utcTime;
    boolean latitudeStatus;
    boolean locationGot;
    int numberLocationFailed;
    String letter;

    AlertDialog alertDialog;
    AlertDialog warningDialog;

    AsyncTask mailAsyncTask;
    MailSync.PostTaskListener<Boolean> mailPostTaskListener = new MailSync.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            if (result) {
                Log.i("Summary", "Mail successfully");
                callNextScreen();
            } else {
                Log.i("Summary", "Mail failed");
                callError();
            }
        }
    };

    public SummaryFragment() {
        numberLocationFailed = 0;
        letter = "";
    }

    private void initialize(View view) {
        try {
            tvCellularConnection = (TextView) view.findViewById(R.id.tvCheckingCellularConnection);
            //tvCellularType = (TextView) view.findViewById(R.id.tvCellularType);
            tvWifiConnection = (TextView) view.findViewById(R.id.tvCheckingWiFiConnection);
            tvWifiConnection.setVisibility(View.GONE);
            tvHutchConnection = (TextView) view.findViewById(R.id.tvHutchConnection);
            tvHutchConnection.setVisibility(View.GONE);
            tvDownloadConfiguration = (TextView) view.findViewById(R.id.tvDownloadConfiguration);
            tvDownloadConfiguration.setVisibility(View.GONE);

            ivCellularConnection = (ImageView) view.findViewById(R.id.icCellularChecking);
            ivWifiConnection = (ImageView) view.findViewById(R.id.icWifiChecking);
            ivWifiConnection.setVisibility(View.GONE);
            ivHutchConnection = (ImageView) view.findViewById(R.id.icHutchConnectionChecking);
            ivHutchConnection.setVisibility(View.GONE);
            ivDownloadConfiguration = (ImageView) view.findViewById(R.id.icDownloadConfiguration);
            ivDownloadConfiguration.setVisibility(View.GONE);

            tvBluetoothEnable = (TextView) view.findViewById(R.id.tvCheckingBluetoothEnable);
            ivBluetoothEnable = (ImageView) view.findViewById(R.id.icBluetoothEnable);
            tvBTBHeartbeat = (TextView) view.findViewById(R.id.tvCheckingBTBHeartbeat);
            ivBTBHeartbeat = (ImageView) view.findViewById(R.id.icBTBHearBeat);
            tvEstablishedConnection = (TextView) view.findViewById(R.id.tvEstablishConnection);
            ivEstablishedConnection = (ImageView) view.findViewById(R.id.icEstablishConnection);
            //tvSearchingBluetooth = (TextView) view.findViewById(R.id.tvBluetoothSearching);
            //ivSearchingBluetooth = (ImageView) view.findViewById(R.id.icBluetoothSearching);

            tvFindingSatellite = (TextView) view.findViewById(R.id.tvFindingSatellite);
            ivFindingSatellite = (ImageView) view.findViewById(R.id.icFindingSatellite);
            tvUTCTime = (TextView) view.findViewById(R.id.tvCheckingUTCTime);
            ivUTCTime = (ImageView) view.findViewById(R.id.icCheckingUTCTime);
            tvLatitudeLongitude = (TextView) view.findViewById(R.id.tvCheckingLatitudeLongitude);
            ivLatitudeLongitude = (ImageView) view.findViewById(R.id.icCheckingLatitudeLongitude);
            tvCurrentLocation = (TextView) view.findViewById(R.id.tvCheckingLocation);
            ivCurrentLocation = (ImageView) view.findViewById(R.id.icCheckingLocation);
            icCurrentLocation = (ImageView) view.findViewById(R.id.icCurrentLocation);

            butProceed = (Button) view.findViewById(R.id.butProceed);
            butProceed.setOnClickListener(this);

            updateCellular(cellularPassed);
            updateWifi(wifiPassed);
            updateHutchSystems(hutchPassed);
            updateDownloadStatus(downloadPassed);
            updateBluetooth(bluetoothOnOff);
            updateBluetoothConnect(bluetoothConnected);
            updateHeartBeat(btbHearBeat);
            updateFindingStatellite(gpsSatellite);
            updateUTCTime(utcTime);
            updateLatitudeLongitude(latitudeStatus);
            updateCurrentLocation(locationGot);
        } catch (Exception e) {
            LogFile.write(SummaryFragment.class.getName() + "::initialize Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    public static SummaryFragment newInstance() {
        SummaryFragment fragment = new SummaryFragment();
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
        View view = inflater.inflate(R.layout.activity_setup_complete, container, false);

        initialize(view);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.butProceed:
                sendMail();
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
        if (mailAsyncTask != null) {
            mailAsyncTask.cancel(true);
        }
    }

    public void updateCellularConnection(boolean isPassed) {
        cellularPassed = isPassed;
    }

    public void updateCellular(boolean isPassed) {
        letter += "Cellular connection check -- ";
        if (isPassed) {
            letter += "Passed";
        } else {
            letter += "Failed";
        }

        tvCellularConnection.setVisibility(View.VISIBLE);
        ivCellularConnection.setVisibility(View.VISIBLE);

        if (isPassed) {
            ivCellularConnection.setBackgroundResource(R.drawable.ic_done_all_white_24dp);
        } else {
            ivCellularConnection.setBackgroundResource(R.drawable.ic_completed_failed);
        }
    }

    public void updateWifiConnection(boolean isPassed) {
        wifiPassed = isPassed;
    }

    public void updateWifi(boolean isPassed) {
        letter += "\n";
        letter += "Wi-Fi connection check -- ";
        if (isPassed) {
            letter += "Passed";
        } else {
            letter += "Failed";
        }


        tvWifiConnection.setVisibility(View.VISIBLE);
        ivWifiConnection.setVisibility(View.VISIBLE);

        if (isPassed) {
            ivWifiConnection.setBackgroundResource(R.drawable.ic_done_all_white_24dp);
        } else {
            ivWifiConnection.setBackgroundResource(R.drawable.ic_completed_failed);
        }
    }

    public void updateHutchConnection(boolean isPassed) {
        hutchPassed = isPassed;
    }

    public void updateHutchSystems(boolean isPassed) {
        letter += "\n";
        letter += "Hutch Systems connection check -- ";
        if (isPassed) {
            letter += "Passed";
        } else {
            letter += "Failed";
        }

        tvHutchConnection.setVisibility(View.VISIBLE);
        ivHutchConnection.setVisibility(View.VISIBLE);

        if (isPassed) {
            ivHutchConnection.setBackgroundResource(R.drawable.ic_done_all_white_24dp);
        } else {
            ivHutchConnection.setBackgroundResource(R.drawable.ic_completed_failed);
        }
    }

    public void updateDownloadConfiguration(boolean isPassed) {
        downloadPassed = isPassed;
    }

    public void updateDownloadStatus(boolean isPassed) {
        letter += "\n";
        letter += "Download Configurations -- ";
        if (isPassed) {
            letter += "Passed";
        } else {
            letter += "Failed";
        }

        tvDownloadConfiguration.setVisibility(View.VISIBLE);
        ivDownloadConfiguration.setVisibility(View.VISIBLE);

        if (isPassed) {
            ivDownloadConfiguration.setBackgroundResource(R.drawable.ic_done_all_white_24dp);
        } else {
            ivDownloadConfiguration.setBackgroundResource(R.drawable.ic_completed_failed);
        }
    }

    public void updateBluetoothOnOff(boolean isPassed) {
        bluetoothOnOff = isPassed;
    }

    public void updateBluetooth(boolean isPassed) {
        letter += "\n";
        letter += "Bluetooth check -- ";
        if (isPassed) {
            letter += "Passed";
        } else {
            letter += "Failed";
        }

        tvBluetoothEnable.setVisibility(View.VISIBLE);
        ivBluetoothEnable.setVisibility(View.VISIBLE);

        if (isPassed) {
            ivBluetoothEnable.setBackgroundResource(R.drawable.ic_done_all_white_24dp);
        } else {
            ivBluetoothEnable.setBackgroundResource(R.drawable.ic_completed_failed);
        }
    }

//    public void updateBluetoothSearching(boolean isPassed) {
//        bluetoothSearch = isPassed;
//    }
//
//    public void updateBluetoothSearch(boolean isPassed) {
//        tvSearchingBluetooth.setVisibility(View.VISIBLE);
//        ivSearchingBluetooth.setVisibility(View.VISIBLE);
//
//        if (isPassed) {
//            ivSearchingBluetooth.setBackgroundResource(R.drawable.ic_setup_passed);
//        } else {
//            ivSearchingBluetooth.setBackgroundResource(R.drawable.ic_setup_failed);
//        }
//    }

    public void updateBluetoothConnection(boolean isPassed) {
        bluetoothConnected = isPassed;
    }

    public void updateBluetoothConnect(boolean isPassed) {
        letter += "\n";
        letter += "BTB connection check -- ";
        if (isPassed) {
            letter += "Passed";
        } else {
            letter += "Failed";
        }

        tvEstablishedConnection.setVisibility(View.VISIBLE);
        ivEstablishedConnection.setVisibility(View.VISIBLE);

        if (isPassed) {
            ivEstablishedConnection.setBackgroundResource(R.drawable.ic_done_all_white_24dp);
        } else {
            ivEstablishedConnection.setBackgroundResource(R.drawable.ic_completed_failed);
        }
    }

    public void updateBluetoothHeartBeat(boolean isPassed) {
        btbHearBeat = isPassed;
    }

    public void updateHeartBeat(boolean isPassed) {
        letter += "\n";
        letter += "BTB Heartbeat check -- ";
        if (isPassed) {
            letter += "Passed";
        } else {
            letter += "Failed";
        }

        tvBTBHeartbeat.setVisibility(View.VISIBLE);
        ivBTBHeartbeat.setVisibility(View.VISIBLE);

        if (isPassed) {
            ivBTBHeartbeat.setBackgroundResource(R.drawable.ic_done_all_white_24dp);
        } else {
            ivBTBHeartbeat.setBackgroundResource(R.drawable.ic_completed_failed);
        }
    }

    public void updateFindingStatelliteStatus(boolean isPassed) {
        gpsSatellite = isPassed;
    }

    public void updateFindingStatellite(boolean isPassed) {
        letter += "\n";
        letter += "Find GPS Satellites -- ";
        if (isPassed) {
            letter += "Passed";
        } else {
            letter += "Failed";
        }

        tvFindingSatellite.setVisibility(View.VISIBLE);
        ivFindingSatellite.setVisibility(View.VISIBLE);

        if (isPassed) {
            ivFindingSatellite.setBackgroundResource(R.drawable.ic_done_all_white_24dp);
        } else {
            ivFindingSatellite.setBackgroundResource(R.drawable.ic_completed_failed);
        }
    }

    public void updateUTCTimeStatus(boolean isPassed) {
        utcTime = isPassed;
    }

    public void updateUTCTime(boolean isPassed) {
        letter += "\n";
        letter += "UTC Time check -- ";
        if (isPassed) {
            letter += "Passed";
        } else {
            letter += "Failed";
        }

        tvUTCTime.setVisibility(View.VISIBLE);
        ivUTCTime.setVisibility(View.VISIBLE);

        if (isPassed) {
            ivUTCTime.setBackgroundResource(R.drawable.ic_done_all_white_24dp);
        } else {
            ivUTCTime.setBackgroundResource(R.drawable.ic_completed_failed);
        }
    }

    public void updateLatitudeLongitudeStatus(boolean isPassed) {
        latitudeStatus = isPassed;
    }

    public void updateLatitudeLongitude(boolean isPassed) {
        letter += "\n";
        letter += "Latitude and Longitude check -- ";
        if (isPassed) {
            letter += "Passed";
        } else {
            letter += "Failed";
        }

        tvLatitudeLongitude.setVisibility(View.VISIBLE);
        ivLatitudeLongitude.setVisibility(View.VISIBLE);

        if (isPassed) {
            ivLatitudeLongitude.setBackgroundResource(R.drawable.ic_done_all_white_24dp);
        } else {
            ivLatitudeLongitude.setBackgroundResource(R.drawable.ic_completed_failed);
        }
    }

    public void updateCurrentLocationStatus(boolean isPassed, int numberFailed) {
        locationGot = isPassed;
        numberLocationFailed = numberFailed;
    }

    public void updateCurrentLocation(boolean isPassed) {
        if (numberLocationFailed < 5) {
            letter += "\n";
            letter += "Current Location check -- ";
            if (isPassed) {
                letter += "Passed";
            } else {
                letter += "Failed";
            }

            tvCurrentLocation.setVisibility(View.VISIBLE);
            ivCurrentLocation.setVisibility(View.VISIBLE);

            if (isPassed) {
                ivCurrentLocation.setBackgroundResource(R.drawable.ic_done_all_white_24dp);
            } else {
                ivCurrentLocation.setBackgroundResource(R.drawable.ic_completed_failed);
            }
        } else {
            tvCurrentLocation.setVisibility(View.GONE);
            ivCurrentLocation.setVisibility(View.GONE);
            icCurrentLocation.setVisibility(View.GONE);
        }
    }

    public void sendMail() {
        if (getActivity() != null) {
            if ((warningDialog != null && warningDialog.isShowing())) {
                return;
            }
            warningDialog = new AlertDialog.Builder(getActivity()).create();
            warningDialog.setCancelable(false);
            warningDialog.setCanceledOnTouchOutside(false);
            warningDialog.setTitle("E-Log");
            warningDialog.setIcon(R.drawable.ic_launcher);
            warningDialog.setMessage("Connecting to Hutch Systems...");
            warningDialog.show();
        }

        SharedPreferences prefs = getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE);
        String installerID = prefs.getString("installer_id", "");
        String content = "Installer ID: " + installerID + "\n";
        content += letter;
        //  callNextScreen();
       mailAsyncTask = new MailSync(mailPostTaskListener, false).execute("Installation completed", content);
    }

    public void callNextScreen() {
        warningDialog.dismiss();
        if (mListener != null) {
            mListener.proceedToELD();
        }
    }

    public void callError() {
        if (getActivity() != null) {
            warningDialog.dismiss();
            if ((alertDialog != null && alertDialog.isShowing())) {
                return;
            }
            alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setTitle("E-Log");
            alertDialog.setIcon(R.drawable.ic_launcher);
            alertDialog.setMessage("Cannot connect to Hutch Systems! Please try again!");
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Retry",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendMail();
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void proceedToELD();
    }

}
