package com.hutchgroup.elog.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.common.GetCall;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.CarrierInfoDB;
import com.hutchgroup.elog.tasks.DownloadConfiguration;
import com.hutchgroup.elog.tasks.SyncData;
import com.hutchgroup.elog.util.AnimationUtil;


public class WirelessConnectivityFragment extends Fragment implements View.OnClickListener {
    final String TAG = WirelessConnectivityFragment.class.getName();

     private OnFragmentInteractionListener mListener;

    TextView tvCellularType;
    TextView tvCellularConnection;
    TextView tvWifiConnection;
    TextView tvHutchConnection;
    TextView tvDownloadConfiguration;

    ImageView ivCellularConnection;
    ImageView ivWifiConnection;
    ImageView ivHutchConnection;
    ImageView ivDownloadConfiguration;
    //RelativeLayout layoutLoadingPanel;

    ImageButton butTryAgain;

    AsyncTask webAsyncTask;
    AsyncTask downloadConfigurationAsyncTask;

    int step;

    boolean isCellularConnected;
    boolean isHutchConnected;
    boolean isDownloadSuccessful;

    boolean cellularConnected;

    Handler handler = new Handler();
    Runnable checkCellularConnection = new Runnable() {
        @Override
        public void run() {
            try {
                boolean cellularConnected = isCellularConnection();
                if (cellularConnected) {
                    //Log.i(TAG, "Cellular passed");
                    ivCellularConnection.setBackgroundResource(R.drawable.ic_setup_passed);
                    tvCellularType.setVisibility(View.VISIBLE);
                    tvCellularType.setText(getNetworkClass(getContext()));
                    tvCellularConnection.setTextColor(getResources().getColor(R.color.green2));
                    isCellularConnected = true;
                } else {
                    //Log.i(TAG, "Cellular failed");
                    ivCellularConnection.setBackgroundResource(R.drawable.ic_setup_failed);
                    tvCellularConnection.setTextColor(getResources().getColor(R.color.red1));
                    isCellularConnected = false;

                    ivCellularConnection.setBackgroundResource(R.drawable.ic_setup_passed);
                    tvCellularType.setVisibility(View.VISIBLE);
                    tvCellularType.setText("3G");
                    tvCellularConnection.setTextColor(getResources().getColor(R.color.green2));
                    isCellularConnected = true;
                }
                if (mListener != null) {
                    mListener.onUpdateCellularStatus(isCellularConnected);
                }

                WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);

                checkingWifiConnection();
            } catch (Exception e) {
                LogFile.write(WirelessConnectivityFragment.class.getName() + "::checkCellularConnection Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            }
        }
    };

    Runnable checkWifiConnection = new Runnable() {
        @Override
        public void run() {
            try {
                boolean wifiConnected = isWifiConnection();
                if (wifiConnected) {
                    Log.i(TAG, "WiFi passed");
                    ivWifiConnection.setBackgroundResource(R.drawable.ic_setup_passed);
                    tvWifiConnection.setTextColor(getResources().getColor(R.color.green2));
                } else {
                    Log.i(TAG, "Wifi failed");
                    ivWifiConnection.setBackgroundResource(R.drawable.ic_setup_failed);
                    tvWifiConnection.setTextColor(getResources().getColor(R.color.red1));
                }
                if (mListener != null) {
                    mListener.onUpdateWifiStatus(wifiConnected);
                }

                checkHutchConnection();
            } catch (Exception e) {
                LogFile.write(WirelessConnectivityFragment.class.getName() + "::checkWifiConnection Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            }
        }
    };

    Runnable checkWebService = new Runnable() {
        @Override
        public void run() {
            try {
                webAsyncTask = new SyncData(syncDataPostTaskListener).execute("0");

            } catch (Exception e) {
                LogFile.write(WirelessConnectivityFragment.class.getName() + "::checkWebService Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            }
        }
    };

    Runnable downloadConfiguraion = new Runnable() {
        @Override
        public void run() {
            try {
                downloadConfigurationAsyncTask = new DownloadConfiguration(configurationPostTaskListener).execute("0");
            } catch (Exception e) {
                LogFile.write(WirelessConnectivityFragment.class.getName() + "::downloadConfiguraion Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            }
        }
    };

    com.hutchgroup.elog.tasks.SyncData.PostTaskListener<Boolean> syncDataPostTaskListener = new com.hutchgroup.elog.tasks.SyncData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            try {
                if (result) {
                    CarrierInfoDB.getCompanyInfo();
                    //layoutLoadingPanel.setVisibility(View.GONE);

                    Log.i(TAG, "Hutch Systems passed");
                    ivHutchConnection.setBackgroundResource(R.drawable.ic_setup_passed);
                    tvHutchConnection.setTextColor(getResources().getColor(R.color.green2));
                } else {
                    Log.i(TAG, "Hutch Systems failed");
                    ivHutchConnection.setBackgroundResource(R.drawable.ic_setup_failed);
                    tvHutchConnection.setTextColor(getResources().getColor(R.color.red1));
                }
                isHutchConnected = result;
                if (mListener != null) {
                    mListener.onUpdateHutchConnectionStatus(result);
                }
                downloadData();
            } catch (Exception e) {
                LogFile.write(WirelessConnectivityFragment.class.getName() + "::SyncData Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            }
        }
    };

    com.hutchgroup.elog.tasks.DownloadConfiguration.PostTaskListener<Boolean> configurationPostTaskListener = new com.hutchgroup.elog.tasks.DownloadConfiguration.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            try {
                if (result) {
                    SharedPreferences.Editor e = (getActivity().getSharedPreferences("HutchGroup", getActivity().MODE_PRIVATE))
                            .edit();
                    e.putBoolean("syncStatus", true);
                    e.commit();
                    // CarrierInfoDB.getCompanyInfo();
                    //layoutLoadingPanel.setVisibility(View.GONE);

                    Log.i(TAG, "Download configuraion passed");
                    ivDownloadConfiguration.setBackgroundResource(R.drawable.ic_setup_passed);
                    tvDownloadConfiguration.setTextColor(getResources().getColor(R.color.green2));
                } else {
                    Log.i(TAG, "Download configuraion failed");
                    ivDownloadConfiguration.setBackgroundResource(R.drawable.ic_setup_failed);
                    tvDownloadConfiguration.setTextColor(getResources().getColor(R.color.red1));
                }

                isDownloadSuccessful = result;

                if (mListener != null) {
                    mListener.onUpdateDownloadDataStatus(result);
                }
                //pass cellular
                //isCellularConnected = true;
                if (isCellularConnected && isHutchConnected && isDownloadSuccessful) {
                    if (mListener != null) {
                        mListener.onNextFromWirelessConnectivity();
                    }
                } else {
                    butTryAgain.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                LogFile.write(WirelessConnectivityFragment.class.getName() + "::SyncData Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
            }
        }
    };

    public WirelessConnectivityFragment() {

    }

    private void initialize(View view) {
        try {
            tvCellularConnection = (TextView) view.findViewById(R.id.tvCheckingCellularConnection);
            tvCellularType = (TextView) view.findViewById(R.id.tvCellularType);
            tvCellularType.setVisibility(View.GONE);
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


            //layoutLoadingPanel = (RelativeLayout) view.findViewById(R.id.loadingPanel);

            butTryAgain = (ImageButton) view.findViewById(R.id.btnWirelessConnectionTryAgain);
            butTryAgain.setVisibility(View.GONE);
            butTryAgain.setOnClickListener(this);

        } catch (Exception e) {
            LogFile.write(WirelessConnectivityFragment.class.getName() + "::initialize Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    public static WirelessConnectivityFragment newInstance() {

        WirelessConnectivityFragment fragment = new WirelessConnectivityFragment();
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
        View view = inflater.inflate(R.layout.activity_wireless_connectivity_check, container, false);
        isCellularConnected = false;
        isHutchConnected = false;
        isDownloadSuccessful = false;

        initialize(view);

        checkingCellularConnection();

        return view;
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {

                case R.id.btnWirelessConnectionTryAgain:
                    butTryAgain.setVisibility(View.GONE);
                    tvDownloadConfiguration.setVisibility(View.GONE);
                    ivDownloadConfiguration.setVisibility(View.GONE);
                    tvHutchConnection.setVisibility(View.GONE);
                    ivHutchConnection.setVisibility(View.GONE);
                    tvWifiConnection.setVisibility(View.GONE);
                    ivWifiConnection.setVisibility(View.GONE);
                    tvCellularType.setVisibility(View.GONE);

                    checkingCellularConnection();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(WirelessConnectivityFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
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
            webAsyncTask.cancel(true);
            downloadConfigurationAsyncTask.cancel(true);
            handler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            LogFile.write(WirelessConnectivityFragment.class.getName() + "::onDetach Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    private void checkingCellularConnection() {
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);

        tvCellularConnection.setTextColor(getResources().getColor(R.color.yellow2));
        AnimationUtil.startSetupProcessingAnimation(getResources(), ivCellularConnection);

        handler.postDelayed(checkCellularConnection, 30000);
    }

    private boolean isCellularConnection() {
        boolean result = false;
        try {
            //cellularConnected = false;
            //cellularHandler.postDelayed(cellularRunnable, 15000);
            NetworkInfo localNetworkInfo = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();

            if (localNetworkInfo != null) {
                if (localNetworkInfo.isConnected() && localNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    result = true;
                }
            }

            //result = cellularConnected;
        } catch (Exception exe) {
            Log.i(TAG, "Error: " + exe.getMessage());
        }

        return result;
    }

    public String getNetworkClass(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            default:
                return "Unknown";
        }
    }

    private void checkingWifiConnection() {
        tvWifiConnection.setVisibility(View.VISIBLE);
        ivWifiConnection.setVisibility(View.VISIBLE);
        tvWifiConnection.setTextColor(getResources().getColor(R.color.yellow2));
        AnimationUtil.startSetupProcessingAnimation(getResources(), ivWifiConnection);

        handler.removeCallbacks(checkCellularConnection);
        handler.postDelayed(checkWifiConnection, 15000);
    }

    private boolean isWifiConnection() {
        boolean result = false;
        try {

            NetworkInfo localNetworkInfo = ((ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();

            if (localNetworkInfo != null) {
                if (localNetworkInfo.isConnected() && localNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    result = true;
                }
            }
        } catch (Exception exe) {
            Log.i(TAG, "Error: " + exe.getMessage());
        }

        return result;
    }

    private void checkHutchConnection() {
        try {
            tvHutchConnection.setVisibility(View.VISIBLE);
            ivHutchConnection.setVisibility(View.VISIBLE);
            tvHutchConnection.setTextColor(getResources().getColor(R.color.yellow2));
            AnimationUtil.startSetupProcessingAnimation(getResources(), ivHutchConnection);

            handler.removeCallbacks(checkWifiConnection);
            handler.postDelayed(checkWebService, 5000);
        } catch (Exception e) {
            LogFile.write(WirelessConnectivityFragment.class.getName() + "::checkHutchConnection Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    private void downloadData() {
        try {
            tvDownloadConfiguration.setVisibility(View.VISIBLE);
            ivDownloadConfiguration.setVisibility(View.VISIBLE);
            tvDownloadConfiguration.setTextColor(getResources().getColor(R.color.yellow2));
            AnimationUtil.startSetupProcessingAnimation(getResources(), ivDownloadConfiguration);

            handler.removeCallbacks(checkWebService);
            handler.postDelayed(downloadConfiguraion, 5000);
        } catch (Exception e) {
            LogFile.write(WirelessConnectivityFragment.class.getName() + "::downloadData Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onNextFromWirelessConnectivity();

        void onUpdateCellularStatus(boolean isPassed);

        void onUpdateWifiStatus(boolean isPassed);

        void onUpdateHutchConnectionStatus(boolean isPassed);

        void onUpdateDownloadDataStatus(boolean isPassed);
    }
}
