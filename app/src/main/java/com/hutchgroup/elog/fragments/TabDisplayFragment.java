package com.hutchgroup.elog.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.hutchgroup.elog.MainActivity;
import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.SettingsDB;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TabDisplayFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, ColorPickerDialog.ColorPickerInterface {
    final String TAG = TabDisplayFragment.class.getName();

    int driverId = 0;

    TextView tvGraphLine;
    LinearLayout layoutCanadaColor;
    LinearLayout layoutUsColor;
    Switch switchViolationOnGrid;
    TextView tvOrientation;
    TextView tvVisionMode;
    Switch switchShowViolation;

    SeekBar seekBrightness;
    RadioGroup rgSelectOrientation;

    //we dont support to change font size
    Spinner spinFontSize;
    Spinner spinGraphLine;
    Spinner spinVisionMode;

    RadioButton rbPortrait;
    RadioButton rbLandscape;
    RadioButton rbAuto;

    Button butCanadaColor;
    Button butUSColor;

    LinearLayout layoutOrientation;
    LinearLayout layoutOrientationSeparator;

    int canadaColor;
    int usColor;

    int currentFontSize;

    ColorPickerDialog colorPickerDialog;
    ColorPickerDialog usColorPickerDialog;

    public TabDisplayFragment() {
        // Required empty public constructor
    }

    private void initialize(View view) {
        try {
            layoutOrientation = (LinearLayout) view.findViewById(R.id.layoutOrientation);
            layoutOrientationSeparator = (LinearLayout) view.findViewById(R.id.layoutOrientationSeparator);

            tvGraphLine = (TextView) view.findViewById(R.id.tvGraphLine);
            layoutCanadaColor = (LinearLayout) view.findViewById(R.id.layoutCanadaColor);
            layoutCanadaColor.setOnClickListener(this);
            layoutUsColor = (LinearLayout) view.findViewById(R.id.layoutUSColor);
            layoutUsColor.setOnClickListener(this);
            switchViolationOnGrid = (Switch) view.findViewById(R.id.switchViolationOnGrid);
            switchViolationOnGrid.setChecked(Utility._appSetting.getViolationOnGrid() == 1 ? true : false);
            switchViolationOnGrid.setOnCheckedChangeListener(this);

            tvOrientation = (TextView) view.findViewById(R.id.tvOrientation);
            tvOrientation.setOnClickListener(this);
            if (!Utility.isLargeScreen(getContext())) {
                layoutOrientation.setVisibility(View.GONE);
                layoutOrientationSeparator.setVisibility(View.GONE);
            }
            rgSelectOrientation = (RadioGroup) view.findViewById(R.id.rgSelectOrientation);
            rbPortrait = (RadioButton) view.findViewById(R.id.rbPortrait);
            rbPortrait.setOnCheckedChangeListener(this);
            rbLandscape = (RadioButton) view.findViewById(R.id.rbLanscape);
            rbLandscape.setOnCheckedChangeListener(this);
            rbAuto = (RadioButton) view.findViewById(R.id.rbAuto);
            rbAuto.setOnCheckedChangeListener(this);
            if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.AUTO.ordinal()) {
                rbAuto.setChecked(true);
            } else if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.PORTRAIT.ordinal()) {
                rbPortrait.setChecked(true);
            } else if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.LANSCAPE.ordinal()) {
                rbLandscape.setChecked(true);
            }

            tvVisionMode = (TextView) view.findViewById(R.id.tvVisionMode);

            currentFontSize = Utility._appSetting.getFontSize();

            spinFontSize = (Spinner) view.findViewById(R.id.spinnerFontSize);
            spinFontSize.setSelection(currentFontSize);
            spinFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.i(TAG, "Item font size selected");

                    if (position != currentFontSize) {
                        Utility._appSetting.setFontSize(position);
                        SettingsDB.CreateSettings();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinGraphLine = (Spinner) view.findViewById(R.id.spinnerGraphLine);
            spinGraphLine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Utility._appSetting.setGraphLine(position);
                    SettingsDB.CreateSettings();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinGraphLine.setSelection(Utility._appSetting.getGraphLine());

            spinVisionMode = (Spinner) view.findViewById(R.id.spinnerVisionMode);
            spinVisionMode.setSelection(Utility._appSetting.getVisionMode());
            spinVisionMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Utility._appSetting.setVisionMode(position);
                    SettingsDB.CreateSettings();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            switchShowViolation = (Switch) view.findViewById(R.id.switchShowViolation);
            switchShowViolation.setOnCheckedChangeListener(this);
            switchShowViolation.setChecked(Utility._appSetting.getShowViolation() == 1 ? true : false);

            seekBrightness = (SeekBar) view.findViewById(R.id.seekBrightness);
            seekBrightness.setMax(255);
            int brightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            seekBrightness.setProgress(brightness);
            seekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    android.provider.Settings.System.putInt(getContext().getContentResolver(),
                            android.provider.Settings.System.SCREEN_BRIGHTNESS, progress);

                    WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                    lp.screenBrightness = (float) progress / 255; //...and put it here
                    getActivity().getWindow().setAttributes(lp);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            butCanadaColor = (Button) view.findViewById(R.id.butCanadaColor);
            butCanadaColor.setOnClickListener(this);
            butUSColor = (Button) view.findViewById(R.id.butUSColor);
            butUSColor.setOnClickListener(this);
            canadaColor = Utility._appSetting.getColorLineCanada(); //use same value in ColorPickerDialog

            usColor = Utility._appSetting.getColorLineUS(); //use same value in ColorPickerDialog

            butCanadaColor.setBackgroundColor(canadaColor);
            butUSColor.setBackgroundColor(usColor);
        } catch (Exception e) {
            LogFile.write(TabDisplayFragment.class.getName() + "::initialize error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }

    }

    public static TabDisplayFragment newInstance() {
        TabDisplayFragment fragment = new TabDisplayFragment();
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
        Log.i("Settings", "onCreateView Display");
        View view = inflater.inflate(R.layout.tab_fragment_display, container, false);
        initialize(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.tvOrientation:
                    rgSelectOrientation.setVisibility(View.VISIBLE);
                    break;
                case R.id.layoutCanadaColor:
                case R.id.butCanadaColor:
                    if (colorPickerDialog == null) {
                        colorPickerDialog =new ColorPickerDialog();
                        colorPickerDialog.mListener = this;
                    }
                    colorPickerDialog.setInitialColor(canadaColor);
                    colorPickerDialog.setColorType(0);
                    colorPickerDialog.show(getFragmentManager(), "canada_color_dlg");
                    break;
                case R.id.layoutUSColor:
                case R.id.butUSColor:
                    if (usColorPickerDialog == null) {
                        usColorPickerDialog = new ColorPickerDialog();
                        usColorPickerDialog.mListener = this;
                    }
                    usColorPickerDialog.setInitialColor(usColor);
                    usColorPickerDialog.setColorType(1);
                    usColorPickerDialog.show(getFragmentManager(), "us_color_dlg");
                    break;
            }
        } catch (Exception e) {
            LogFile.write(TabDisplayFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            switch (buttonView.getId()) {
                case R.id.rbPortrait:
                    //not support to change orientation for phone
                    if (Utility.isLargeScreen(getContext())) {
                        if (isChecked) {
                            Utility._appSetting.setOrientation(AppSettings.AppOrientation.PORTRAIT.ordinal());
                            SettingsDB.CreateSettings();
                        }
                    }
                    break;
                case R.id.rbLanscape:
                    //not support to change orientation for phone
                    if (Utility.isLargeScreen(getContext())) {
                        if (isChecked) {
                            Utility._appSetting.setOrientation(AppSettings.AppOrientation.LANSCAPE.ordinal());
                            SettingsDB.CreateSettings();
                        }
                    }
                    break;
                case R.id.rbAuto:
                    //not support to change orientation for phone
                    if (Utility.isLargeScreen(getContext())) {
                        if (isChecked) {
                            Utility._appSetting.setOrientation(AppSettings.AppOrientation.AUTO.ordinal());
                            SettingsDB.CreateSettings();
                        }
                    }
                    break;
                case R.id.switchShowViolation:
                    Utility._appSetting.setShowViolation(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
                case R.id.switchViolationOnGrid:
                    Utility._appSetting.setViolationOnGrid(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
            }

            Log.i(TAG, "violation reading = " + AppSettings.getViolationReading());
            Log.i(TAG, "copy trailer = " + AppSettings.getCopyTrailer());
            //change screen orientation by selection from user
            //need permission WRITE_SETTINGS for this feature
            if (Utility.isLargeScreen(getContext())) {
                if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.PORTRAIT.ordinal()) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.LANSCAPE.ordinal()) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.AUTO.ordinal()) {
                    Settings.System.putInt(getActivity().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            } else {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            Log.i(TAG, "orientation = " + Utility._appSetting.getOrientation());
        } catch (Exception e) {
            LogFile.write(TabDisplayFragment.class.getName() + "::onCheckedChanged Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void colorChanged(int color, int colorType) {
        try {
            if (colorType == 0) {
                canadaColor = color;
                butCanadaColor.setBackgroundColor(color);
                Utility._appSetting.setColorLineCanada(canadaColor);
            } else {
                usColor = color;
                butUSColor.setBackgroundColor(color);
                Utility._appSetting.setColorLineUS(usColor);
            }
            SettingsDB.CreateSettings();
        } catch (Exception e) {
            LogFile.write(TabDisplayFragment.class.getName() + "::colorChanged Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

}