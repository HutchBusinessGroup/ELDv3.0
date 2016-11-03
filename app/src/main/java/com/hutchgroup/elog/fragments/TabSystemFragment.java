package com.hutchgroup.elog.fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.beans.TimeZoneBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.SettingsDB;
import com.hutchgroup.elog.db.UserDB;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class TabSystemFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, RuleChangeDialog.RuleChangeDialogInterface {
    final String TAG = TabSystemFragment.class.getName();
    int driverId = 0;
    int currentRuleIdx;
    ArrayList<String> listRules;
    TextView tvDefaultRule;
    TextView tvRule;
    TextView tvTimeFromat;
    TextView tvSystemInformation;
    TextView tvLegal;
    TextView tvSyncTime;
    Switch switchCopyTrailer;
    Switch switchAutomaticRuleChange;

    RadioGroup rgSelectTimeFormat;

    Spinner spinSyncTime, spinnerTimeZone;

    RadioButton rbTime12;
    RadioButton rbTime24;
    Button butCheckUpdate;

    LinearLayout layoutOrientation;
    LinearLayout layoutOrientationSeparator;

    int currentSyncTimeSelection;

    SettingsFragment settingsFragment;
    LegalDialog legalDialog;

    private OnFragmentInteractionListener mListener;


    public TabSystemFragment() {
    }

    public void setSettingsFragment(SettingsFragment frag) {
        settingsFragment = frag;
    }


    private void initialize(View view) {
        try {
            layoutOrientation = (LinearLayout) view.findViewById(R.id.layoutOrientation);
            layoutOrientationSeparator = (LinearLayout) view.findViewById(R.id.layoutOrientationSeparator);

            tvDefaultRule = (TextView) view.findViewById(R.id.tvDefaultRule);
            tvDefaultRule.setOnClickListener(this);
            tvRule = (TextView) view.findViewById(R.id.tvRule);
            tvRule.setOnClickListener(this);

            tvTimeFromat = (TextView) view.findViewById(R.id.tvTimeFormat);
            rgSelectTimeFormat = (RadioGroup) view.findViewById(R.id.rgSelectTimeFormat);
            rbTime12 = (RadioButton) view.findViewById(R.id.rbTime12);
            rbTime12.setOnCheckedChangeListener(this);
            rbTime24 = (RadioButton) view.findViewById(R.id.rbTime24);
            rbTime24.setOnCheckedChangeListener(this);
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR12.ordinal()) {
                rbTime12.setChecked(true);
            } else {
                rbTime24.setChecked(true);
            }

            //tvCheckUpdate = (TextView) view.findViewById(R.id.tvCheckUpdate);
            //tvCheckUpdate.setOnClickListener(this);
            butCheckUpdate = (Button) view.findViewById(R.id.butCheckUpdate);
            butCheckUpdate.setOnClickListener(this);

            tvSystemInformation = (TextView) view.findViewById(R.id.tvSystemInformation);
            tvSystemInformation.setOnClickListener(this);
            tvLegal = (TextView) view.findViewById(R.id.tvLegal);
            tvLegal.setOnClickListener(this);
            tvSyncTime = (TextView) view.findViewById(R.id.tvSyncTime);
            spinSyncTime = (Spinner) view.findViewById(R.id.spinnerSyncTime);
            spinnerTimeZone = (Spinner) view.findViewById(R.id.spinnerTimeZone);

            spinnerTimeZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (systemTimeZone == position)
                        return;

                    TimeZoneBean selectedZone = timeZoneList.get(position);
                    AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                    am.setTimeZone(selectedZone.getTimeZoneId());
                    Utility.TimeZoneOffsetUTC = selectedZone.getTimeZoneValue();
                    UserDB.Update("TimeZoneOffsetUTC", Utility.TimeZoneOffsetUTC);
                    Utility._appSetting.setTimeZone(selectedZone.getTimeZoneOffset());
                    SettingsDB.CreateSettings();
                    Utility.sdf.setTimeZone(TimeZone.getDefault());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            populateTimeZone();

            if (Utility._appSetting.getSyncTime() == AppSettings.SYNC5) {
                spinSyncTime.setSelection(0);
            } else if (Utility._appSetting.getSyncTime() == AppSettings.SYNC10) {
                spinSyncTime.setSelection(1);
            } else if (Utility._appSetting.getSyncTime() == AppSettings.SYNC20) {
                spinSyncTime.setSelection(2);
            } else if (Utility._appSetting.getSyncTime() == AppSettings.SYNC30) {
                spinSyncTime.setSelection(3);
            } else if (Utility._appSetting.getSyncTime() == AppSettings.SYNC60) {
                spinSyncTime.setSelection(4);
            }
            currentSyncTimeSelection = spinSyncTime.getSelectedItemPosition();

            spinSyncTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.i(TAG, "selected " + position);
                    if (position != currentSyncTimeSelection) {
                        switch (position) {
                            case 0:
                                Utility._appSetting.setSyncTime(AppSettings.SYNC5);
                                SettingsDB.CreateSettings();
                                break;
                            case 1:
                                Utility._appSetting.setSyncTime(AppSettings.SYNC10);
                                SettingsDB.CreateSettings();
                                break;
                            case 2:
                                Utility._appSetting.setSyncTime(AppSettings.SYNC20);
                                SettingsDB.CreateSettings();
                                break;
                            case 3:
                                Utility._appSetting.setSyncTime(AppSettings.SYNC30);
                                SettingsDB.CreateSettings();
                                break;
                            case 4:
                                Utility._appSetting.setSyncTime(AppSettings.SYNC60);
                                SettingsDB.CreateSettings();
                                break;
                        }
                        currentSyncTimeSelection = position;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            switchCopyTrailer = (Switch) view.findViewById(R.id.switchCopyTrailer);
            switchCopyTrailer.setOnCheckedChangeListener(this);
            switchCopyTrailer.setChecked(Utility._appSetting.getCopyTrailer() == 1 ? true : false);

            switchAutomaticRuleChange = (Switch) view.findViewById(R.id.switchAutomaticRuleChange);
            switchAutomaticRuleChange.setOnCheckedChangeListener(this);
            switchAutomaticRuleChange.setChecked(Utility._appSetting.getAutomaticRuleChange() == 1 ? true : false);

            getRules();
            driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();

            currentRuleIdx = Utility._appSetting.getDefaultRule(); //DailyLogDB.getCurrentRule(driverId);
            if (currentRuleIdx == 0) {
                currentRuleIdx = 1;
            }
            tvRule.setText(listRules.get(currentRuleIdx - 1));

        } catch (Exception e) {
            LogFile.write(TabSystemFragment.class.getName() + "::initialize error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }

    }

    ArrayList<TimeZoneBean> timeZoneList;
    int systemTimeZone = 2;

    private void populateTimeZone() {

        timeZoneList = Utility.populateTimeZone();
        ArrayList<Map<String, String>> items = new ArrayList<>();

        for (int i = 0; i < timeZoneList.size(); i++) {
            TimeZoneBean bean = timeZoneList.get(i);
            String id = bean.getTimeZoneId();
            Map<String, String> item = new HashMap<>(2);
            item.put("text", bean.getTimeZoneName());
            item.put("subText", bean.getTimeZoneValue());

            if (id.equals(TimeZone.getDefault().getID())) {
                systemTimeZone = i;
            }

            items.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(getContext(), items,
                R.layout.simple_list_item_group,
                new String[]{"text", "subText"},
                new int[]{R.id.tvItem, R.id.tvSubItem}
        );
        adapter.setDropDownViewResource(R.layout.simple_list_item_group);
        spinnerTimeZone.setAdapter(adapter);
        spinnerTimeZone.setSelection(systemTimeZone);
    }

    public static TabSystemFragment newInstance() {
        TabSystemFragment fragment = new TabSystemFragment();
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
        Log.i("Settings", "onCreateView System");
        View view = inflater.inflate(R.layout.tab_fragment_system, container, false);
        initialize(view);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            //mListener.onFragmentInteraction(uri);
//        }
//    }

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
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.tvRule:
                case R.id.tvDefaultRule:
                    launchRuleChange();
                    break;
                case R.id.butCheckUpdate:
                    //Log.i(TAG, "Check update");
                    //use interface to call async task run in activity
                    //dont call async task in here, it will NOT work correctly
                    try {
                        if (mListener != null) {
                            mListener.callCheckUpdate();
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "Cannot find package info: " + e.getMessage());
                    }
                    break;
                case R.id.tvLegal:
                    //show legal information
                    if (legalDialog == null) {
                        legalDialog = new LegalDialog();
                    }
                    if (legalDialog.isAdded()) {
                        break;
                    }
                    legalDialog.show(getFragmentManager(), "legal_dialog");

                    break;
                case R.id.tvSystemInformation:
                    showSystemInformation();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(TabSystemFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            switch (buttonView.getId()) {
                case R.id.rbTime12:
                    if (isChecked) {
                        Utility._appSetting.setTimeFormat(AppSettings.AppTimeFormat.HR12.ordinal());
                        SettingsDB.CreateSettings();
                    }
                    break;
                case R.id.rbTime24:
                    if (isChecked) {
                        Utility._appSetting.setTimeFormat(AppSettings.AppTimeFormat.HR24.ordinal());
                        SettingsDB.CreateSettings();
                    }
                    break;
                case R.id.switchCopyTrailer:
                    Utility._appSetting.setCopyTrailer(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
                case R.id.switchAutomaticRuleChange:
                    Utility._appSetting.setAutomaticRuleChange(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
            }

        } catch (Exception e) {
            LogFile.write(TabSystemFragment.class.getName() + "::onCheckedChanged Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void showSystemInformation() {
        if (settingsFragment != null) {
            settingsFragment.showSystemInformation();
        }
    }

    //these default rules should be saved in server,
    // it will help us expand the app easier than using default array in code like this
    private void getRules() {
        listRules = new ArrayList<String>();
        listRules.add(getResources().getString(R.string.canada_rule_1));
        listRules.add(getResources().getString(R.string.canada_rule_2));
        listRules.add(getResources().getString(R.string.us_rule_1));
    }

    private void launchRuleChange() {
        RuleChangeDialog dialog = new RuleChangeDialog();
        dialog.mListener = this;
        dialog.setCurrentRule(currentRuleIdx);
        dialog.show(getFragmentManager(), "rulechange_dialog");
    }

    @Override
    public void onSavedRule(int rule) {
        try {
            if (rule != currentRuleIdx) {
                DailyLogDB.DailyLogRuleSave(Utility.onScreenUserId, rule, Utility.getCurrentDateTime(), Utility.getCurrentDateTime());
                currentRuleIdx = rule;
            }
            tvRule.setText(listRules.get(currentRuleIdx - 1));
            Utility._appSetting.setDefaultRule(currentRuleIdx);
            SettingsDB.CreateSettings();
        } catch (Exception e) {
            LogFile.write(TabSystemFragment.class.getName() + "::onSavedRule Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
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
        void callCheckUpdate();
    }

}