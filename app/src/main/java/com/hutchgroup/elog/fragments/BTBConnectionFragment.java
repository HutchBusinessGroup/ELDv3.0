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
import android.graphics.drawable.AnimationDrawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.hutchgroup.elog.util.AnimationUtil;

import java.lang.reflect.Method;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class BTBConnectionFragment extends Fragment implements View.OnClickListener, BluetoothDeviceAdapter.ItemClickListener, BluetoothPairedDeviceAdapter.ButtonClickListener {
    final String TAG = BTBConnectionFragment.class.getName();
    private static final int REQUEST_ENABLE_BT = 1;
    private OnFragmentInteractionListener mListener;
    int driverId;
    List<BluetoothDevice> listAvailableDevices;
    BluetoothDeviceAdapter availableDevicesAdapter;
    List<BluetoothDevice> listPairedDevices;
    BluetoothPairedDeviceAdapter pairedDevicesAdapter;

    ListView lvPairedDevices;
    ListView lvAvailableDevices;
    Button butSearch;

    LinearLayout layoutBluetooth;
    TextView tvBluetoothConnectionInfos;
    LinearLayout connectionPanel;

    TextView tvBluetoothEnable;
    TextView tvBTBHeartbeat;
    TextView tvEstablishedConnection;
    TextView tvNumberOfTry;
    ImageView ivBluetoothEnable;
    ImageView ivBTBHeartbeat;
    ImageView ivEstablishedConnection;


    TextView tvSearchingBluetooth;
    ImageView ivSearchingBluetooth;

    BluetoothDevice selectedDevice;
    AlertDialog alertDialog;
    AlertDialog errorDialog;
    boolean dismissDialog;

    private BluetoothAdapter bTAdapter;
    private CanMessages objCan = null;

    //broad cast receiver
    //receiver to get nearby bluetooth devices and to update paired/unpaired device
    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                Log.i(TAG, "Receive Device");
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    Log.i(TAG, "Starting discovery");

                    Log.i(TAG, "1---- Starting discovery");
                    //rlLoadingProgress.setVisibility(View.VISIBLE);
                    tvSearchingBluetooth.setVisibility(View.VISIBLE);
                    ivSearchingBluetooth.setVisibility(View.VISIBLE);

                    tvSearchingBluetooth.setTextColor(getResources().getColor(R.color.yellow2));
                    AnimationUtil.startSetupProcessingAnimation(getResources(), ivSearchingBluetooth);

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.i(TAG, "Finished discovery");
                    //rlLoadingProgress.setVisibility(View.GONE);
                    ivSearchingBluetooth.setBackgroundResource(R.drawable.ic_setup_passed);
                    tvSearchingBluetooth.setTextColor(getResources().getColor(R.color.green2));
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
                        Log.i(TAG, "notifyDataSetChanged");
                        if (device.getName().contains(CanMessages.BT_NAME) || device.getName().startsWith(CanMessages.BT_NAME_1)) {
                            listAvailableDevices.add(device);
                            //availableDevicesAdapter.changeItems(listAvailableDevices);
                            availableDevicesAdapter.notifyDataSetChanged();
                        }
                    }

                } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    Log.i(TAG, "Bond state changed");
                    availableDevicesAdapter.updateParingText();
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                        Log.i(TAG, "Paired");
                        getPairedDevices();
                        //connectDevice(listPairedDevices);

                        layoutBluetooth.setVisibility(View.GONE);
                        tvEstablishedConnection.setVisibility(View.VISIBLE);
                        ivEstablishedConnection.setVisibility(View.VISIBLE);

                        tvEstablishedConnection.setTextColor(getResources().getColor(R.color.yellow2));
                        AnimationUtil.startSetupProcessingAnimation(getResources(), ivEstablishedConnection);
                        if (connectTry < 5)
                            connectDevice(listPairedDevices);

                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                        Log.i(TAG, "Unpaired");
                        getPairedDevices();
                    }

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.i(TAG, "Entered the Finished");
                    //startDiscovery();
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    //startDiscovery();
                }
            } catch (Exception e) {
                LogFile.write(BTBConnectionFragment.class.getName() + "::bReciever Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            }
        }
    };

    public BTBConnectionFragment() {
        // Required empty public constructor
        //driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        //  driverId = 0;
        //logId = DailyLogDB.DailyLogCreate(driverId, "", "", "");
    }

    private void initialize(View view) {
        try {
            layoutStartVehicle = (LinearLayout) view.findViewById(R.id.layoutStartVehicle);

            tvBluetoothEnable = (TextView) view.findViewById(R.id.tvCheckingBluetoothEnable);
            ivBluetoothEnable = (ImageView) view.findViewById(R.id.icBluetoothEnable);

            tvBTBHeartbeat = (TextView) view.findViewById(R.id.tvCheckingBTBHeartbeat);
            tvBTBHeartbeat.setVisibility(View.GONE);
            ivBTBHeartbeat = (ImageView) view.findViewById(R.id.icBTBHearBeat);
            ivBTBHeartbeat.setVisibility(View.GONE);
            tvEstablishedConnection = (TextView) view.findViewById(R.id.tvEstablishConnection);
            tvEstablishedConnection.setVisibility(View.GONE);
            ivEstablishedConnection = (ImageView) view.findViewById(R.id.icEstablishConnection);
            ivEstablishedConnection.setVisibility(View.GONE);
            tvNumberOfTry = (TextView) view.findViewById(R.id.tvNumberOfTry);
            tvNumberOfTry.setVisibility(View.GONE);
            tvSearchingBluetooth = (TextView) view.findViewById(R.id.tvBluetoothSearching);
            tvSearchingBluetooth.setVisibility(View.GONE);
            ivSearchingBluetooth = (ImageView) view.findViewById(R.id.icBluetoothSearching);
            ivSearchingBluetooth.setVisibility(View.GONE);

            //tvBluetoothMessage = (TextView) view.findViewById(R.id.tvBTBInformation) ;

            lvPairedDevices = (ListView) view.findViewById(R.id.lvPairedDevices);
            lvAvailableDevices = (ListView) view.findViewById(R.id.lvAvailableDevices);
            //butNext = (Button) view.findViewById(R.id.btnNext);
//            butNext.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (mListener != null) {
//                        mListener.onButtonClicked();
//                    }
//                }
//            });
            //butNext.setEnabled(false);
            butSearch = (Button) view.findViewById(R.id.btnSearch);
            butSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "2---- Starting discovery");
                    bTAdapter.cancelDiscovery();

                    tvSearchingBluetooth.setTextColor(getResources().getColor(R.color.yellow2));
                    AnimationUtil.startSetupProcessingAnimation(getResources(), ivSearchingBluetooth);

                    startDiscovery();
                }
            });

            //rlLoadingProgress = (RelativeLayout) view.findViewById(R.id.loadingPanel);

            layoutBluetooth = (LinearLayout) view.findViewById(R.id.layoutBluetooth);
            layoutBluetooth.setVisibility(View.GONE);
            //layoutBluetoothConnectionInfos = (RelativeLayout) view.findViewById(R.id.layoutBluetoothConnecting);
            tvBluetoothConnectionInfos = (TextView) view.findViewById(R.id.tvBluetoothConnectionInfos);
            connectionPanel = (LinearLayout) view.findViewById(R.id.connectionPanel);

            listAvailableDevices = new ArrayList<BluetoothDevice>();
            availableDevicesAdapter = new BluetoothDeviceAdapter(getContext(), this, listAvailableDevices);

            lvAvailableDevices.setAdapter(availableDevicesAdapter);

            listPairedDevices = new ArrayList<BluetoothDevice>();

            pairedDevicesAdapter = new BluetoothPairedDeviceAdapter(getContext(), this, listPairedDevices);

            lvPairedDevices.setAdapter(pairedDevicesAdapter);

            //layoutBluetooth.setVisibility(View.VISIBLE);
            //layoutBluetoothConnectionInfos.setVisibility(View.GONE);
        } catch (Exception e) {
            LogFile.write(BTBConnectionFragment.class.getName() + "::initialize Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    public static BTBConnectionFragment newInstance() {
        BTBConnectionFragment fragment = new BTBConnectionFragment();
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
        View view = inflater.inflate(R.layout.activity_bluetooth_connectivity_check, container, false);
        try {
            initialize(view);

            //after initialize the layout, check if bluetooth is enable or not
            checkBluetoothEnable();

        } catch (Exception e) {
            LogFile.write(BTBConnectionFragment.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
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
            getActivity().unregisterReceiver(bReciever);
            handlerBT.removeCallbacksAndMessages(null);
            checkStatusHandler.removeCallbacksAndMessages(null);
            nextHandler.removeCallbacksAndMessages(null);
            checkHeartBeatHandler.removeCallbacks(null);
            mListener = null;
            objCan = null;

        } catch (Exception e) {
            LogFile.write(BTBConnectionFragment.class.getName() + "::onDetach Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onItemClicked(BluetoothDevice device) {
        //check if the MAC Address of device is correct when pairing device
        if (!device.getAddress().equals(Utility.MACAddress)) {
            showErrorMessage("Error: BTB Mismatch!");
            return;
        }
        //if it is correct, pair to that device
        pairDevice(device);
    }

    @Override
    public void onButtonClicked(BluetoothDevice device) {
        unpairDevice(device);
    }

    //call to pair with the BluetoothDevice
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
            LogFile.write(BTBConnectionFragment.class.getName() + "::pairDevice Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onActivityResult");
        if (requestCode == REQUEST_ENABLE_BT) {
            checkBluetoothEnable();
        }

    }

    //check if bluetooth is enable or not
    private void checkBluetoothEnable() {
        try {
            Log.i(TAG, "checkBluetoothEnable");
            tvBluetoothEnable.setTextColor(getResources().getColor(R.color.yellow2));
            AnimationUtil.startSetupProcessingAnimation(getResources(), ivBluetoothEnable);

            bTAdapter = BluetoothAdapter.getDefaultAdapter();

            if (bTAdapter == null) {
                Log.i(TAG, "Device doest not support Bluetooth.");
                LogFile.write(BluetoothConnectivityFragment.class.getName() + "::checkBluetoothEnable Error:Device doest not support Bluetooth.", LogFile.SETUP, LogFile.ERROR_LOG);
                //showMessage();
            } else {
                if (!bTAdapter.isEnabled()) {
                    tvBluetoothEnable.setTextColor(getResources().getColor(R.color.red1));
                    ivBluetoothEnable.setBackgroundResource(R.drawable.ic_setup_failed);

                    if (mListener != null) {
                        mListener.onUpdateBluetoothOnOff(false);
                    }
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, REQUEST_ENABLE_BT);
                } else {
                    //Bluetooth is on
                    tvBluetoothEnable.setTextColor(getResources().getColor(R.color.green2));
                    ivBluetoothEnable.setBackgroundResource(R.drawable.ic_setup_passed);

                    Log.i(TAG, "Searching bluetooth...");
                    tvSearchingBluetooth.setVisibility(View.VISIBLE);
                    ivSearchingBluetooth.setVisibility(View.VISIBLE);
                    layoutBluetooth.setVisibility(View.VISIBLE);
                    if (mListener != null) {
                        mListener.onUpdateBluetoothOnOff(true);
                    }

                    //update text and icon for searching bluetooth devices
                    tvSearchingBluetooth.setTextColor(getResources().getColor(R.color.yellow2));
                    AnimationUtil.startSetupProcessingAnimation(getResources(), ivSearchingBluetooth);

                    //register receiver to get devices or status change of device
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    getActivity().registerReceiver(bReciever, filter);

                    //call to connect with device to get data
                    objCan = new CanMessages();
                    initializeBluetooth();
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "checkBluetoothEnable error: " + e.getMessage());
        }
    }

    //use for unpair device
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
            LogFile.write(BTBConnectionFragment.class.getName() + "::unpairDevice Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }

    }

    //connect with bluetooth device
    private void initializeBluetooth() {
        bTAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.i(TAG, "initializeBluetooth");
        if (bTAdapter == null) {
            Log.i(TAG, "Device doest not support Bluetooth.");
            LogFile.write(BluetoothConnectivityFragment.class.getName() + "::initializeBluetooth Error:Device doest not support Bluetooth.", LogFile.SETUP, LogFile.ERROR_LOG);
        } else {
            //bluetooth is on
            //get paired devices if already paired, get the address and name of first device start with RNBT
            getPairedDevices();
            for (BluetoothDevice device : listPairedDevices) {
                if (device.getName().startsWith(CanMessages.BT_NAME) || device.getName().startsWith(CanMessages.BT_NAME_1)) {
                    CanMessages.deviceAddress = device.getAddress();
                    CanMessages.deviceName = device.getName();
                    break;
                }
            }

            Log.i(TAG, "address=" + CanMessages.deviceAddress);
            if (CanMessages.deviceAddress == null) {
                //if we dont have any paired device start with RNBT, we will find nearby devices
                startDiscovery();
            } else {
                //if found paired device start with RNBT, try to connect with that device
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            if (listPairedDevices.size() > 0) {
//                                if (mListener != null) {
//                                    mListener.onUpdateBluetoothSearching(true);
//                                }


                                connectDevice(listPairedDevices);
                            }
                            Log.i(TAG, "Start scanning nearly devices");
                            //startDiscovery();

                        } catch (Exception e) {

                            e.printStackTrace();
                            LogFile.write(BTBConnectionFragment.class.getName() + "::initializeBluetooth Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
                        }
                    }
                }).start();
            }
        }
    }

    //get paired devices from bluetooth adapter
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
//                if (device.getName().startsWith(BT_NAME)) {
//                    Log.i(TAG, "Paried Device: " + device.getAddress());
//                    bd = device;
//                }
                listPairedDevices.add(device);
            }
            pairedDevicesAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            LogFile.write(BTBConnectionFragment.class.getName() + "::getPairedDevices Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    //unpair all of paired devices
    private void unpairAll() {
        for (BluetoothDevice device : listPairedDevices) {
            unpairDevice(device);
        }
    }

    //finding nearby devices
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
            LogFile.write(BTBConnectionFragment.class.getName() + "::startDiscovery Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    //connect to bluetooth device
    private void connectDevice(List<BluetoothDevice> devices) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (BluetoothDevice device : listPairedDevices) {
                        if (device.getName().startsWith(CanMessages.BT_NAME) || device.getName().startsWith(CanMessages.BT_NAME_1)) {
                            CanMessages.deviceAddress = device.getAddress();
                            CanMessages.deviceName = device.getName();
                            break;
                        }
                    }


                    if (CanMessages.deviceAddress != null) {
                        dismissDialog = false;
                        connectTry++;
                        //                if (mListener != null) {
                        //                    mListener.onUpdateBluetoothSearching(true);
                        //                }

                        layoutBluetooth.setVisibility(View.GONE);
                        tvEstablishedConnection.setVisibility(View.VISIBLE);
                        ivEstablishedConnection.setVisibility(View.VISIBLE);

                        tvEstablishedConnection.setTextColor(getResources().getColor(R.color.yellow2));
                        AnimationUtil.startSetupProcessingAnimation(getResources(), ivEstablishedConnection);

                        //update number of try to connect with device
                        tvNumberOfTry.setVisibility(View.VISIBLE);
                        tvNumberOfTry.setTextColor(getResources().getColor(R.color.yellow2));
                        tvNumberOfTry.setText(connectTry + "/5");

                        connectDevice(true);
                        handlerBT.postDelayed(runnableStatus, 30000);
                        checkStatusHandler.postDelayed(checkStatus, 5000);
                    } else {
                        if (mListener != null) {
                            unpairAll();
                            tvEstablishedConnection.setTextColor(getResources().getColor(R.color.red1));
                            ivEstablishedConnection.setBackgroundResource(R.drawable.ic_setup_failed);
                            if (mListener != null) {
                                mListener.onUpdateBluetoothConnection(false);
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            LogFile.write(BTBConnectionFragment.class.getName() + "::connectDevice Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }

    }

    //connect to device to get data
    private void connectDevice(boolean secure) {

        //showConnectingMessage();
        bTAdapter.cancelDiscovery();
        layoutBluetooth.setVisibility(View.GONE);

        tvEstablishedConnection.setTextColor(getResources().getColor(R.color.yellow2));
        AnimationUtil.startSetupProcessingAnimation(getResources(), ivEstablishedConnection);
        // Get the device MAC address
        String address = CanMessages.deviceAddress;
        // Get the BluetoothDevice object
        BluetoothDevice device = bTAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        objCan.connect(device, secure);
        objCan.startTransmitRequestHB();

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onNextToCanbusRead();

        void onUpdateBluetoothOnOff(boolean isPassed);

        //void onUpdateBluetoothSearching(boolean isPassed);
        void onUpdateBluetoothConnection(boolean isPassed);

        void onUpdateBluetoothHeartBeat(boolean isPassed);
    }

    //display message for user
    private void showErrorMessage(String msg) {
        if ((errorDialog != null && errorDialog.isShowing())) {
            return;
        }
        if (getActivity() != null) {
            errorDialog = new AlertDialog.Builder(getActivity()).create();
            errorDialog.setCancelable(false);
            errorDialog.setCanceledOnTouchOutside(false);
            errorDialog.setTitle("E-Log");
            errorDialog.setIcon(R.drawable.ic_launcher);
            errorDialog.setMessage(msg);
            errorDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            availableDevicesAdapter.updateParingText();
                            errorDialog.cancel();
                        }
                    });
            errorDialog.show();
        }
    }


    private Handler handlerBT = new Handler();
    int connectTry = 0;
    //try to connect at least 5 times if the connection state is still LISTEN
    private Runnable runnableStatus = new Runnable() {
        @Override
        public void run() {
            try {
                if (objCan.getState() == CanMessages.STATE_LISTEN) {
                    if (connectTry < 5) {
                        connectTry++;
                        tvEstablishedConnection.setVisibility(View.VISIBLE);
                        ivEstablishedConnection.setVisibility(View.VISIBLE);
                        tvNumberOfTry.setVisibility(View.VISIBLE);
                        tvNumberOfTry.setText(connectTry + "/5");
                        connectDevice(true);
                    } else {
                        //after 5 times, cannot connect with device, that means connection failed
                        if (mListener != null) {
                            unpairAll();
                            //mListener.showConnectionError();
                            tvEstablishedConnection.setTextColor(getResources().getColor(R.color.red1));
                            ivEstablishedConnection.setBackgroundResource(R.drawable.ic_setup_failed);
                            if (mListener != null) {
                                mListener.onUpdateBluetoothConnection(false);
                            }
                        }
                    }
                } else {
                    connectTry = 0;
                }

                handlerBT.postDelayed(this, 30000);
            } catch (Exception e) {
                LogFile.write(BTBConnectionFragment.class.getName() + "::runnableStatus Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            }
        }
    };

    private Handler checkStatusHandler = new Handler();
    //check status if connected, call to check heartbeat data
    private Runnable checkStatus = new Runnable() {
        @Override
        public void run() {

            if (objCan.getState() == CanMessages.STATE_CONNECTED) {
                //connection successfull
                layoutBluetooth.setVisibility(View.GONE);
                tvEstablishedConnection.setVisibility(View.VISIBLE);
                ivEstablishedConnection.setVisibility(View.VISIBLE);
                if (getActivity() != null) {
                    tvEstablishedConnection.setTextColor(getResources().getColor(R.color.green2));
                    tvNumberOfTry.setTextColor(getResources().getColor(R.color.green2));
                }
                ivEstablishedConnection.setBackgroundResource(R.drawable.ic_setup_passed);

                if (mListener != null) {
                    mListener.onUpdateBluetoothConnection(true);
                }

                tvBTBHeartbeat.setVisibility(View.VISIBLE);
                ivBTBHeartbeat.setVisibility(View.VISIBLE);

                tvBTBHeartbeat.setTextColor(getResources().getColor(R.color.yellow2));
                AnimationUtil.startSetupProcessingAnimation(getResources(), ivBTBHeartbeat);

                checkHeartBeatHandler.postDelayed(checkHeartBeat, 1000);
                connectTry = 0;

//                if (mListener != null) {
//                    mListener.showConnectionSuccessfull();
//                }

            } else
                checkStatusHandler.postDelayed(this, 5000);
        }
    };
    private LinearLayout layoutStartVehicle;
    private Handler checkHeartBeatHandler = new Handler();
    //check heart beat from bluetooth device
    private Runnable checkHeartBeat = new Runnable() {
        @Override
        public void run() {

            if (objCan.HeartBeat) {
                if (getActivity() != null) {
                    tvBTBHeartbeat.setTextColor(getResources().getColor(R.color.green2));
                    ivBTBHeartbeat.setBackgroundResource(R.drawable.ic_setup_passed);
                    if (mListener != null) {
                        mListener.onUpdateBluetoothHeartBeat(true);
                    }
                    //show message to user
                    //  showMessage("Please start the vehicle!");
                    layoutStartVehicle.setVisibility(View.VISIBLE);

                    //tvBluetoothMessage.setVisibility(View.VISIBLE);
                    objCan.HeartBeat = false;
                    checkHeartBeatHandler.removeCallbacksAndMessages(null);
                }

                nextHandler.postDelayed(nextRunnable, 5000);
            } else {

                checkHeartBeatHandler.postDelayed(this, 1000);
            }
        }
    };

    private Handler nextHandler = new Handler();
    private Runnable nextRunnable = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                float rpm = Float.valueOf(CanMessages.RPM);
                if (rpm > 0f) {

                    if (getActivity() != null && mListener != null) {
                        mListener.onNextToCanbusRead();

                    }
                } else
                    nextHandler.postDelayed(nextRunnable, 2000);
            }


        }
    };
}
