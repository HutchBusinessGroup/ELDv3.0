package com.hutchgroup.elog.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.BluetoothDeviceAdapter;
import com.hutchgroup.elog.adapters.BluetoothPairedDeviceAdapter;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.Constants;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;

import java.lang.reflect.Method;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class CanBusDataFragment extends Fragment implements View.OnClickListener {
    final String TAG = CanBusDataFragment.class.getName();

    private OnFragmentInteractionListener mListener;

    TextView edVINNumber;
    TextView edOdometer;
    TextView edRPM;
    TextView edEngineHours;

    ImageButton butNext;


    private int selectedProtocol;
    long numberGetVIN;
    long numberGetEngineHours;

    int countAfterVINRequested;
    int countAfterEngineHoursRequested;

    Handler handler = new Handler();
    Runnable updateInformation = new Runnable() {
        @Override
        public void run() {
            try {
                Log.i(TAG, "get VIN, Engine Hours,...");
                if (CanMessages.VIN.equals("")) {
                    numberGetVIN++;
                    if (countAfterVINRequested != 0) {
                        countAfterVINRequested++;
                    }
                } else {
                    numberGetVIN = 0;
                }
                if (CanMessages.EngineHours.equals("0")) {
                    numberGetEngineHours++;
                    if (countAfterEngineHoursRequested != 0) {
                        countAfterEngineHoursRequested++;
                    }
                } else {
                    numberGetEngineHours = 0;
                }
                edVINNumber.setText(CanMessages.VIN);
                edOdometer.setText(CanMessages.OdometerReading);
                //edSpeed.setText(CanMessages.Speed);
                edRPM.setText(CanMessages.RPM);
                edEngineHours.setText(CanMessages.EngineHours);
//                if (!CanMessages.VIN.equals("") && !CanMessages.EngineHours.equals("0")) {
//                    tvInformation.setVisibility(View.VISIBLE);
//                    butNext.setEnabled(true);
//                }

                if (!CanMessages.OdometerReading.equals("0") && !CanMessages.EngineHours.equals("0") && !CanMessages.RPM.equals("0")) {
                    //tvInformation.setVisibility(View.VISIBLE);
                    butNext.setEnabled(true);
                }

                if (countAfterVINRequested > 10) {
                    if (mListener != null) {
                        //handler.removeCallbacks(this);
                        //mListener.onCanBusError();
                    }
                }
                if (numberGetVIN > 5) {
                    //butCheckVIN.setVisibility(View.VISIBLE);
                }
                if (countAfterEngineHoursRequested > 10) {
                    if (mListener != null) {
                        //handler.removeCallbacks(this);
                        //mListener.onCanBusError();
                    }
                }
                if (numberGetEngineHours > 5) {
                    //butCheckEngineHours.setVisibility(View.VISIBLE);
                }
                handler.postDelayed(this, 1000);
            } catch (Exception e) {
                LogFile.write(CanBusDataFragment.class.getName() + "::updateInformation Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            }
        }
    };

    public CanBusDataFragment() {

    }

    private void initialize(View view) {
        try {
            edVINNumber = (TextView) view.findViewById(R.id.edSetupVinNumber);
            edOdometer = (TextView) view.findViewById(R.id.edSetupOdometer);
            //edSpeed = (EditText) view.findViewById(R.id.edSetupSpeed);
            edRPM = (TextView) view.findViewById(R.id.edSetupRPM);
            edEngineHours = (TextView) view.findViewById(R.id.edSetupEngineHours);

            butNext = (ImageButton) view.findViewById(R.id.btnNext);
            butNext.setOnClickListener(this);
            butNext.setEnabled(false);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(updateInformation, 500);
                }
            });
        } catch (Exception e) {
            LogFile.write(CanBusDataFragment.class.getName() + "::updateInformation Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    public static CanBusDataFragment newInstance() {
        CanBusDataFragment fragment = new CanBusDataFragment();
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
        View view = inflater.inflate(R.layout.activity_bluetooth_connectivity_check2, container, false);
        try {
            initialize(view);

            countAfterVINRequested = 0;
            countAfterEngineHoursRequested = 0;

        } catch (Exception e) {
            LogFile.write(CanBusDataFragment.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }

        return view;
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btnNext:
                    if (mListener != null) {
                        mListener.onNextToGPSCheck();
                    }
                    break;
            }
        } catch (Exception e) {
            LogFile.write(CanBusDataFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
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
        try {
            mListener = null;
            handler.removeCallbacks(updateInformation);
        } catch (Exception e) {
            LogFile.write(CanBusDataFragment.class.getName() + "::onDetach Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    private void showCanBusData() {
        edVINNumber.setText(CanMessages.VIN);
        edOdometer.setText(CanMessages.OdometerReading);
        //edSpeed.setText(CanMessages.Speed);
        edRPM.setText(CanMessages.RPM);
        edEngineHours.setText(CanMessages.EngineHours);

        butNext.setEnabled(true);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
        //void onNextToWebService();
        //void onCanBusError();
        void onNextToGPSCheck();
    }

    private final Handler bluetoothHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case CanMessages.STATE_CONNECTED:
                            // setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            // mConversationArrayAdapter.clear();
                            break;
                        case CanMessages.STATE_CONNECTING:
                            // setStatus(R.string.title_connecting);
                            break;
                        case CanMessages.STATE_LISTEN:
                        case CanMessages.STATE_NONE:
                            // setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;

                case Constants.MESSAGE_TOAST:

                    //Toast.makeText(getContext(), msg.getData().getString(Constants.TOAST),
                    //        Toast.LENGTH_LONG).show();

                    break;
            }
        }
    };
}
