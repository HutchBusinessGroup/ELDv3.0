package com.hutchgroup.elog.fragments;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hutchgroup.elog.MainActivity;
import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.beans.VersionInformationBean;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.GetCall;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.SettingsDB;

import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class SettingsFragment extends Fragment implements View.OnClickListener {
    final String TAG = SettingsFragment.class.getName();

    int driverId = 0;
    int batteryLevel;

    ScrollView layoutTimeZone;
    LinearLayout layoutSettings;
    ScrollView layoutSettingsScroll;
    ScrollView layoutSystemInfor;
    TextView tvAndroidVersion;
    TextView tvAppVersion;
    TextView tvIMEI;
    TextView tvBatteryLevel;
    TextView tvBluetoothName;
    Button butBack;

    TabLayout tabLayout;

    ViewPager viewPager;
    PagerAdapter pagerAdapter;

    Handler handler = new Handler();
    //update battery level in information of the system
    Runnable updateBattery = new Runnable() {
        @Override
        public void run() {
            if (tvBatteryLevel != null) {
                tvBatteryLevel.setText(Utility.BatteryLevel + "%");
            }

            handler.postDelayed(this, 30000);
        }
    };

    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
    }


    private void initialize(View view) {
        try {
            Log.i(TAG, "initialize the view");
            //using TabLayout to show 3 tabs: System, Display, and Sound
            tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
            tabLayout.addTab(tabLayout.newTab().setText("System"));
            tabLayout.addTab(tabLayout.newTab().setText("Display"));
            tabLayout.addTab(tabLayout.newTab().setText("Sound"));

            //using ViewPager to support sliding for changing tab
            viewPager = (ViewPager) view.findViewById(R.id.pager);
            //using custom adapter to change size of displayed tab
            pagerAdapter = new PagerAdapter(getChildFragmentManager(), this);
            viewPager.setAdapter(pagerAdapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            //use another layout for selecting timezone, should change it to Spinner to match with the FirstUser screen
            layoutSettings = (LinearLayout) view.findViewById(R.id.layoutSettings);
            layoutSettingsScroll = (ScrollView) view.findViewById(R.id.layoutSettingsScroll);
            layoutSettingsScroll.setVisibility(View.VISIBLE);

            layoutSystemInfor = (ScrollView) view.findViewById(R.id.layoutSystemInfor);
            tvAndroidVersion = (TextView) view.findViewById(R.id.tvAndroidVersion);
            String androidOS = Build.VERSION.RELEASE;
            tvAndroidVersion.setText(androidOS);
            tvAppVersion = (TextView) view.findViewById(R.id.tvAppVersion);
            try {
                PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                String version = pInfo.versionName;
                tvAppVersion.setText(version);
            } catch (Exception e) {

            }

            tvIMEI = (TextView) view.findViewById(R.id.tvIMEI);
            tvIMEI.setText(Utility.IMEI);
            tvBatteryLevel = (TextView) view.findViewById(R.id.tvBatteryLevel);
            tvBatteryLevel.setText(batteryLevel + "%");
            tvBluetoothName = (TextView) view.findViewById(R.id.tvBluetoothName);
            tvBluetoothName.setText(CanMessages.deviceName);
            butBack = (Button) view.findViewById(R.id.butBack);
            butBack.setOnClickListener(this);

            driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        } catch (Exception e) {
            LogFile.write(SettingsFragment.class.getName() + "::initialize error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }

    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        initialize(view);

        handler.postDelayed(updateBattery, 50);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach");
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
        Log.i(TAG, "Detach");
        mListener = null;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.butBack:
                    layoutSystemInfor.setVisibility(View.GONE);
                    layoutSettingsScroll.setVisibility(View.VISIBLE);
                    butBack.setVisibility(View.GONE);
                    break;
            }
        } catch (Exception e) {
            LogFile.write(SettingsFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
    }


    public void showSystemInformation() {
        layoutSystemInfor.setVisibility(View.VISIBLE);
        layoutSettingsScroll.setVisibility(View.GONE);
        butBack.setVisibility(View.VISIBLE);
    }
}
