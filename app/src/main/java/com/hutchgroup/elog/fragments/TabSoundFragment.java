package com.hutchgroup.elog.fragments;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
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

public class TabSoundFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    final String TAG = TabSoundFragment.class.getName();


    int driverId = 0;
    Switch switchReadViolation;
    Switch switchReadingMessage;
    Switch switchReadDutyStatusChanged;
    TextView tvVolumeLevel;

    AudioManager audio;

    SeekBar seekVolume;

    public TabSoundFragment() {

    }


    private void initialize(View view) {
        try {
            switchReadViolation = (Switch) view.findViewById(R.id.switchReadViolation);
            switchReadViolation.setChecked(Utility._appSetting.getViolationReading() == 1 ? true : false);
            switchReadViolation.setOnCheckedChangeListener(this);

            switchReadingMessage = (Switch) view.findViewById(R.id.switchReadingMessage);
            switchReadingMessage.setChecked(Utility._appSetting.getMessageReading() == 1 ? true : false);
            switchReadingMessage.setOnCheckedChangeListener(this);

            switchReadDutyStatusChanged = (Switch) view.findViewById(R.id.switchReadDutyStatusChanged);
            switchReadDutyStatusChanged.setChecked(Utility._appSetting.getDutyStatusReading() == 1 ? true : false);
            switchReadDutyStatusChanged.setOnCheckedChangeListener(this);

            tvVolumeLevel = (TextView) view.findViewById(R.id.tvVolumeLevel);

            audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            seekVolume = (SeekBar) view.findViewById(R.id.seekVolume);
            //set maximum for progress bar
            seekVolume.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            //set current value for progress
            seekVolume.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));

            seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progreessed = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                    if (progress > progreessed) {
//                        //UP
//                        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//                                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
//                    } else {
//                        //down
//                        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//                                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
//                    }

                    progreessed = progress;
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, progreessed, AudioManager.FLAG_PLAY_SOUND);
                    Log.i(TAG, "progress=" + progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    if (seekBar.getProgress() != progreessed) {
                        Log.i(TAG, "Not update");
                        progreessed = seekBar.getProgress();
                    }
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, progreessed, 0);
                }
            });
        } catch (Exception e) {
            LogFile.write(TabSoundFragment.class.getName() + "::initialize error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }

    }

    public static TabSoundFragment newInstance() {
        TabSoundFragment fragment = new TabSoundFragment();
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
        Log.i("Settings", "onCreateView Sound");
        View view = inflater.inflate(R.layout.tab_fragment_sound, container, false);
        initialize(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            switch (buttonView.getId()) {
                case R.id.switchReadingMessage:
                    Utility._appSetting.setMessageReading(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
                case R.id.switchReadViolation:
                    Utility._appSetting.setViolationReading(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;

                case R.id.switchReadDutyStatusChanged:
                    Utility._appSetting.setDutyStatusReading(isChecked ? 1 : 0);
                    SettingsDB.CreateSettings();
                    break;
            }

        } catch (Exception e) {
            LogFile.write(TabSoundFragment.class.getName() + "::onCheckedChanged Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }
}