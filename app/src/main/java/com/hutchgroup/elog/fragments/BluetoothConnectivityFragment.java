package com.hutchgroup.elog.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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


public class BluetoothConnectivityFragment extends Fragment implements View.OnClickListener, BluetoothDeviceAdapter.ItemClickListener, BluetoothPairedDeviceAdapter.ButtonClickListener {
    final String TAG = BluetoothConnectivityFragment.class.getName();

    static BluetoothConnectivityFragment mInstance = null;

    private OnFragmentInteractionListener mListener;

    int driverId;
    int logId;

    List<BluetoothDevice> listAvailableDevices;
    BluetoothDeviceAdapter availableDevicesAdapter;
    List<BluetoothDevice> listPairedDevices;
    BluetoothPairedDeviceAdapter pairedDevicesAdapter;

    ListView lvPairedDevices;
    ListView lvAvailableDevices;
    Button butNext;
    Button butSearch;
    RelativeLayout rlLoadingProgress;

    LinearLayout layoutBluetooth;
    RelativeLayout layoutBluetoothConnectionInfos;
    TextView tvBluetoothConnectionInfos;
    LinearLayout connectionPanel;

    int numberConnectionFailed;

    BluetoothDevice selectedDevice;

    private BluetoothAdapter bTAdapter;
    private CanMessages objCan = null;

    //broad cast receiver
    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                Log.i(TAG, "Receive Device");
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    Log.i(TAG, "Starting discovery");
                    rlLoadingProgress.setVisibility(View.VISIBLE);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.i(TAG, "Finished discovery");
                    rlLoadingProgress.setVisibility(View.GONE);
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Create a new device item
                    Log.i(TAG, "Found Device: " + device.getName());
                    boolean isAlreadyPaired = false;
                    for (BluetoothDevice d : listPairedDevices) {
                        if (device.getAddress().equals(d.getAddress()) && device.getName().equals(d.getName())) {
                            isAlreadyPaired = true;
                            break;
                        }
                    }
                    if (!isAlreadyPaired) {
                        listAvailableDevices.add(device);
                        //availableDevicesAdapter.changeItems(listAvailableDevices);
                        availableDevicesAdapter.notifyDataSetChanged();
                    }

                } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    Log.i(TAG, "Bond state changed");
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                        Log.i(TAG, "Paired");
                        getPairedDevices();
                        connectDevice(listPairedDevices);
                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                        Log.i(TAG, "Unpaired");
                        getPairedDevices();
                    }

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.i(TAG, "Entered the Finished");
                    startDiscovery();
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    startDiscovery();
                }
            } catch (Exception e) {
                LogFile.write(BluetoothConnectivityFragment.class.getName() + "::bReciever Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            }
        }
    };

    public BluetoothConnectivityFragment() {
        // Required empty public constructor
        //driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        //  driverId = 0;
        //logId = DailyLogDB.DailyLogCreate(driverId, "", "", "");
    }

    private void initialize(View view) {
        try {
            lvPairedDevices = (ListView) view.findViewById(R.id.lvPairedDevices);
            lvAvailableDevices = (ListView) view.findViewById(R.id.lvAvailableDevices);
            butNext = (Button) view.findViewById(R.id.btnNext);
            butNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onButtonClicked();
                    }
                }
            });
            butNext.setEnabled(false);
            butSearch = (Button) view.findViewById(R.id.btnSearch);
            butSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startDiscovery();
                }
            });

            rlLoadingProgress = (RelativeLayout) view.findViewById(R.id.loadingPanel);

            layoutBluetooth = (LinearLayout) view.findViewById(R.id.layoutBluetooth);
            layoutBluetoothConnectionInfos = (RelativeLayout) view.findViewById(R.id.layoutBluetoothConnecting);
            tvBluetoothConnectionInfos = (TextView) view.findViewById(R.id.tvBluetoothConnectionInfos);
            connectionPanel = (LinearLayout) view.findViewById(R.id.connectionPanel);

            layoutBluetooth.setVisibility(View.VISIBLE);
            layoutBluetoothConnectionInfos.setVisibility(View.GONE);
        } catch (Exception e) {
            LogFile.write(BluetoothConnectivityFragment.class.getName() + "::initialize Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    public static BluetoothConnectivityFragment newInstance() {
        //NewEventFragment fragment = new NewEventFragment();
        if (mInstance == null) {
            mInstance = new BluetoothConnectivityFragment();
        }
        return mInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setup_bluetooth_connectivity, container, false);
        try {
            initialize(view);

            listAvailableDevices = new ArrayList<BluetoothDevice>();
            availableDevicesAdapter = new BluetoothDeviceAdapter(getContext(), this, listAvailableDevices);

            lvAvailableDevices.setAdapter(availableDevicesAdapter);

            listPairedDevices = new ArrayList<BluetoothDevice>();
            pairedDevicesAdapter = new BluetoothPairedDeviceAdapter(getContext(), this, listPairedDevices);

            lvPairedDevices.setAdapter(pairedDevicesAdapter);

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            getActivity().registerReceiver(bReciever, filter);

            objCan = new CanMessages();
            initializeBluetooth();
        } catch (Exception e) {
            LogFile.write(BluetoothConnectivityFragment.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnNext:
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
        try {
            mListener = null;
            getActivity().unregisterReceiver(bReciever);
            handlerBT.removeCallbacksAndMessages(null);
            checkStatusHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            LogFile.write(BluetoothConnectivityFragment.class.getName() + "::onDetach Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onItemClicked(BluetoothDevice device) {
        pairDevice(device);
    }

    @Override
    public void onButtonClicked(BluetoothDevice device) {
        unpairDevice(device);
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Log.i(TAG, "Start Pairing...");
            bTAdapter.cancelDiscovery();
            Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
            Method createBondMethod = btClass.getMethod("createBond");
            createBondMethod.invoke(device);

            selectedDevice = device;
            Log.i(TAG, "Pairing finished.");
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
            LogFile.write(BluetoothConnectivityFragment.class.getName() + "::pairDevice Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Log.i(TAG, "Start Unpairing...");

            bTAdapter.cancelDiscovery();
            Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
            Method removeBondMethod = btClass.getMethod("removeBond");
            removeBondMethod.invoke(device);

            Log.i(TAG, "Unpairing finished.");
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
            LogFile.write(BluetoothConnectivityFragment.class.getName() + "::unpairDevice Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    private void initializeBluetooth() {
        bTAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bTAdapter == null) {
            Log.i(TAG, "Device doest not support Bluetooth.");
            LogFile.write(BluetoothConnectivityFragment.class.getName() + "::unpairDevice Error:Device doest not support Bluetooth.", LogFile.SETUP, LogFile.ERROR_LOG);
            //showMessage();
        } else {
            if (!bTAdapter.isEnabled()) {
                Log.i(TAG, "Enable Bluetooth.");
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, 0);
            } else {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            getPairedDevices();
                            if (listPairedDevices.size() > 0) {
                                connectDevice(listPairedDevices);
                            }
                            Log.i(TAG, "Start scanning nearly devices");
                            startDiscovery();

                        } catch (Exception e) {

                            e.printStackTrace();
                            LogFile.write(BluetoothConnectivityFragment.class.getName() + "::initializeBluetooth Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
                        }
                    }
                }).start();

            }
        }
    }

    private void getPairedDevices() {
        try {
            Set<BluetoothDevice> devices = bTAdapter.getBondedDevices();

            Log.i(TAG, "Number of paired devices: " + devices.size());
            System.out.println("Print: " + devices.size());
            BluetoothDevice bd = null;
            //add to list of paired devices
            listPairedDevices.clear();
            pairedDevicesAdapter.notifyDataSetChanged();
            for (BluetoothDevice device : devices) {
                //if (device.getName().startsWith(BT_NAME)) {
                //    Log.i(TAG, "Paried Device: " + device.getAddress());
                //    bd = device;
                //}
                listPairedDevices.add(device);
            }
            pairedDevicesAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            LogFile.write(BluetoothConnectivityFragment.class.getName() + "::getPairedDevices Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    private void unpairAll() {
        for (BluetoothDevice device: listPairedDevices) {
            unpairDevice(device);
        }
    }

    private void startDiscovery() {
        try {
            listAvailableDevices.clear();
            availableDevicesAdapter.notifyDataSetChanged();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "start discovery");
                    bTAdapter.startDiscovery();
                }
            }).start();
        } catch (Exception e) {
            LogFile.write(BluetoothConnectivityFragment.class.getName() + "::startDiscovery Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    private void connectDevice(List<BluetoothDevice> devices) {
        try {
            for (BluetoothDevice device : devices) {
                if (device.getName().startsWith(CanMessages.BT_NAME) || device.getName().startsWith(CanMessages.BT_NAME_1)) {
                    CanMessages.deviceAddress = device.getAddress();
                    CanMessages.deviceName = device.getName();
                    break;
                }
            }
            if (CanMessages.deviceAddress != null) {
                connectDevice(true);
                handlerBT.postDelayed(runnableStatus, 60000);
                checkStatusHandler.postDelayed(checkStatus, 5000);
            } else {
                if (mListener != null) {
                    unpairAll();
                    mListener.showConnectionError();
                }
            }
        } catch (Exception e) {
            LogFile.write(BluetoothConnectivityFragment.class.getName() + "::connectDevice Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    private void connectDevice(boolean secure) {

        showConnectingMessage();

        // Get the device MAC address
        String address = CanMessages.deviceAddress;
        // Get the BluetoothDevice object
        BluetoothDevice device = bTAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        objCan.connect(device, secure);
    }

    private void showMessage(final String message) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showConnectingMessage() {
        if (mListener != null) {
            mListener.showBluetoothConnectionMessage();
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onButtonClicked();
        void showBluetoothConnectionMessage();
        void showConnectionSuccessfull();
        void showConnectionError();
    }

    private Handler handlerBT = new Handler();
    int connectTry = 0;
    private Runnable runnableStatus = new Runnable() {
        @Override
        public void run() {
            try {
                if (objCan.getState() == CanMessages.STATE_LISTEN) {
                    if (connectTry < 4) {
                        connectTry++;
                        connectDevice(true);
                    } else {
                        if (mListener != null) {
                            unpairAll();
                            mListener.showConnectionError();
                        }
                    }
                } else {
                    connectTry = 0;
                }

                handlerBT.postDelayed(this, 60000);
            } catch (Exception e) {
                LogFile.write(BluetoothConnectivityFragment.class.getName() + "::runnableStatus Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            }
        }
    };

    private Handler checkStatusHandler = new Handler();
    private Runnable checkStatus = new Runnable() {
        @Override
        public void run() {

            if (objCan.getState() == CanMessages.STATE_CONNECTED) {
                connectTry = 0;

                if (mListener != null) {
                    mListener.showConnectionSuccessfull();
                }

            }

            checkStatusHandler.postDelayed(this, 10000);
        }
    };
}
