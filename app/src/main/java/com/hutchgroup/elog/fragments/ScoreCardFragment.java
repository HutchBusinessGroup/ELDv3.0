package com.hutchgroup.elog.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.ScoreCardAdapter;
import com.hutchgroup.elog.adapters.ScoreCardThresholdAdapter;
import com.hutchgroup.elog.beans.AlertBean;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.GForceMonitor;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.AlertDB;
import com.hutchgroup.elog.db.HourOfServiceDB;


import java.util.ArrayList;

import static com.hutchgroup.elog.common.AlertMonitor.PostedSpeed;
import static com.hutchgroup.elog.common.AlertMonitor.PostedSpeedThreshold;

public class ScoreCardFragment extends Fragment implements AlertDB.IScoreCard {
    int drivingScore = 0;
    CheckBox swMonthly;
    boolean isCurrentDate = true;
    public static boolean IsTesting = false;

    TextView tvDrivingTime, tvDrivingScore;
    //Driving points gained through time:  648 Mins | Last Day Points 2450
    TextView tvLowWasherFluid, tvLowWasherFluidPer, tvLowWasherFluidCount;
    TextView tvWarmUpEngine, tvWarmUpEnginePer, tvWarmUpEngineCount;
    TextView tvLowEngineOil, tvLowEngineOilPer, tvLowEngineOilCount;
    TextView tvLowCoolant, tvLowCoolantPer, tvLowCoolantCount;
    TextView tvHighRPM, tvHighRPMPer, tvHighRPMCount;
    TextView tvSpeedViolation, tvSpeedViolationPer, tvSpeedViolationCount;
    TextView tvHardAcceleration, tvHardAccelerationPer, tvHardAccelerationCount;
    TextView tvHardBreaking, tvHardBreakingPer, tvHardBreakingCount;
    TextView tvIdling, tvIdlingPer, tvIdlingCount;
    TextView tvTPMS, tvTPMSPer, tvTPMSCount;
    TextView tvCriticalWarning, tvCriticalWarningPer, tvCriticalWarningCount;
    TextView tvChecnkEngineOn, tvChecnkEngineOnPer, tvChecnkEngineOnCount;
    TextView tvSharpTurnLeft, tvSharpTurnLeftPer, tvSharpTurnLeftCount;
    TextView tvSharpTurnRight, tvSharpTurnRightPer, tvSharpTurnRightCount;
    TextView tvHOS, tvHOSPer, tvHOSCount;
    TextView tvTripInspectionFailure, tvTripInspectionFailurePer, tvTripInspectionFailureCount;

    private void initialize(View view) {
        tvDrivingTime = (TextView) view.findViewById(R.id.tvDrivingTime);
        tvDrivingScore = (TextView) view.findViewById(R.id.tvDrivingScore);

        tvLowWasherFluid = (TextView) view.findViewById(R.id.tvLowWasherFluid);
        tvLowWasherFluidPer = (TextView) view.findViewById(R.id.tvLowWasherFluidPer);
        tvLowWasherFluidCount = (TextView) view.findViewById(R.id.tvLowWasherFluidCount);

        tvWarmUpEngine = (TextView) view.findViewById(R.id.tvWarmUpEngine);
        tvWarmUpEnginePer = (TextView) view.findViewById(R.id.tvWarmUpEnginePer);
        tvWarmUpEngineCount = (TextView) view.findViewById(R.id.tvWarmUpEngineCount);

        tvLowEngineOil = (TextView) view.findViewById(R.id.tvLowEngineOil);
        tvLowEngineOilPer = (TextView) view.findViewById(R.id.tvLowEngineOilPer);
        tvLowEngineOilCount = (TextView) view.findViewById(R.id.tvLowEngineOilCount);

        tvLowCoolant = (TextView) view.findViewById(R.id.tvLowCoolant);
        tvLowCoolantPer = (TextView) view.findViewById(R.id.tvLowCoolantPer);
        tvLowCoolantCount = (TextView) view.findViewById(R.id.tvLowCoolantCount);

        tvHighRPM = (TextView) view.findViewById(R.id.tvHighRPM);
        tvHighRPMPer = (TextView) view.findViewById(R.id.tvHighRPMPer);
        tvHighRPMCount = (TextView) view.findViewById(R.id.tvHighRPMCount);

        tvSpeedViolation = (TextView) view.findViewById(R.id.tvSpeedViolation);
        tvSpeedViolationPer = (TextView) view.findViewById(R.id.tvSpeedViolationPer);
        tvSpeedViolationCount = (TextView) view.findViewById(R.id.tvSpeedViolationCount);

        tvHardAcceleration = (TextView) view.findViewById(R.id.tvHardAcceleration);
        tvHardAccelerationPer = (TextView) view.findViewById(R.id.tvHardAccelerationPer);
        tvHardAccelerationCount = (TextView) view.findViewById(R.id.tvHardAccelerationCount);

        tvHardBreaking = (TextView) view.findViewById(R.id.tvHardBreaking);
        tvHardBreakingPer = (TextView) view.findViewById(R.id.tvHardBreakingPer);
        tvHardBreakingCount = (TextView) view.findViewById(R.id.tvHardBreakingCount);

        tvIdling = (TextView) view.findViewById(R.id.tvIdling);
        tvIdlingPer = (TextView) view.findViewById(R.id.tvIdlingPer);
        tvIdlingCount = (TextView) view.findViewById(R.id.tvIdlingCount);

        tvTPMS = (TextView) view.findViewById(R.id.tvTPMS);
        tvTPMSPer = (TextView) view.findViewById(R.id.tvTPMSPer);
        tvTPMSCount = (TextView) view.findViewById(R.id.tvTPMSCount);

        tvCriticalWarning = (TextView) view.findViewById(R.id.tvCriticalWarning);
        tvCriticalWarningPer = (TextView) view.findViewById(R.id.tvCriticalWarningPer);
        tvCriticalWarningCount = (TextView) view.findViewById(R.id.tvCriticalWarningCount);

        tvChecnkEngineOn = (TextView) view.findViewById(R.id.tvChecnkEngineOn);
        tvChecnkEngineOnPer = (TextView) view.findViewById(R.id.tvChecnkEngineOnPer);
        tvChecnkEngineOnCount = (TextView) view.findViewById(R.id.tvChecnkEngineOnCount);

        tvSharpTurnLeft = (TextView) view.findViewById(R.id.tvSharpTurnLeft);
        tvSharpTurnLeftPer = (TextView) view.findViewById(R.id.tvSharpTurnLeftPer);
        tvSharpTurnLeftCount = (TextView) view.findViewById(R.id.tvSharpTurnLeftCount);

        tvSharpTurnRight = (TextView) view.findViewById(R.id.tvSharpTurnRight);
        tvSharpTurnRightPer = (TextView) view.findViewById(R.id.tvSharpTurnRightPer);
        tvSharpTurnRightCount = (TextView) view.findViewById(R.id.tvSharpTurnRightCount);

        tvHOS = (TextView) view.findViewById(R.id.tvHOS);
        tvHOSPer = (TextView) view.findViewById(R.id.tvHOSPer);
        tvHOSCount = (TextView) view.findViewById(R.id.tvHOSCount);

        tvTripInspectionFailure = (TextView) view.findViewById(R.id.tvTripInspectionFailure);
        tvTripInspectionFailurePer = (TextView) view.findViewById(R.id.tvTripInspectionFailurePer);
        tvTripInspectionFailureCount = (TextView) view.findViewById(R.id.tvTripInspectionFailureCount);

        swMonthly = (CheckBox) view.findViewById(R.id.swMonthly);
        swMonthly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isCurrentDate = !b;
                ScoreCardGet();
            }
        });

    }

    private void UpdateScoreValue(String code, int score, int count) {
        switch (code) {
            case "LowCoolantTemperatureVL":
                fillTextView(score, count, tvWarmUpEngine, tvWarmUpEnginePer, tvWarmUpEngineCount);
                break;
            case "LowEngineOilVL":
                fillTextView(score, count, tvLowEngineOil, tvLowEngineOilPer, tvLowEngineOilCount);
                break;
            case "LowCoolantLevelVL":
                fillTextView(score, count, tvLowCoolant, tvLowCoolantPer, tvLowCoolantCount);
                break;
            case "HighRPMVL":
                fillTextView(score, count, tvHighRPM, tvHighRPMPer, tvHighRPMCount);
                break;
            case "SpeedVL":
                fillTextView(score, count, tvSpeedViolation, tvSpeedViolationPer, tvSpeedViolationCount);
                break;
            case "HardAccelerationVL":
                fillTextView(score, count, tvHardAcceleration, tvHardAccelerationPer, tvHardAccelerationCount);
                break;
            case "HardBreakingVL":
                fillTextView(score, count, tvHardBreaking, tvHardBreakingPer, tvHardBreakingCount);
                break;
            case "IdlingVL":
                fillTextView(score, count, tvIdling, tvIdlingPer, tvIdlingCount);
                break;
            case "TPMSWarningVL":
                fillTextView(score, count, tvTPMS, tvTPMSPer, tvTPMSCount);
                break;
            case "CriticalWarningVL":
                fillTextView(score, count, tvCriticalWarning, tvCriticalWarningPer, tvCriticalWarningCount);
                break;
            case "CheckEngineVL":
                fillTextView(score, count, tvChecnkEngineOn, tvChecnkEngineOnPer, tvChecnkEngineOnCount);
                break;
            case "SharpTurnLeftVL":
                fillTextView(score, count, tvSharpTurnLeft, tvSharpTurnLeftPer, tvSharpTurnLeftCount);
                break;
            case "SharpTurnRightVL":
                fillTextView(score, count, tvSharpTurnRight, tvSharpTurnRightPer, tvSharpTurnRightCount);
                break;
            case "NoTripInspectionVL":
                fillTextView(score, count, tvTripInspectionFailure, tvTripInspectionFailurePer, tvTripInspectionFailureCount);
                break;
        }
    }

    private void ScoreCardGet() {
        String date = Utility.getCurrentDate();
        if (!isCurrentDate) {
            date = Utility.getPreviousDate(-30);
        }
        int drivingMinute = HourOfServiceDB.DrivingTimeGet(date, Utility.onScreenUserId);
        tvDrivingTime.setText("Driving points gained through time: " + drivingMinute + " Mins");
        drivingScore = (drivingMinute / 10) * 5;
        tvDrivingScore.setText(drivingScore+"");
        ArrayList<AlertBean> list = AlertDB.getScoreCard(Utility.onScreenUserId, date);
        for (AlertBean item : list) {
            int score = item.getScores();
            int count = item.getCount();
            UpdateScoreValue(item.getAlertCode(), score, count);

        }
    }

    private void fillTextView(int score, int count, TextView tvVal, TextView tvPer, TextView tvCount) {
        tvVal.setText(score + "");
        if (drivingScore == 0) {
            drivingScore = 1;
        }
        int percentage = (score / drivingScore) * 100;
        tvPer.setText(percentage + "%");
        tvCount.setText(count + "");

    }

    public ScoreCardFragment() {
        // Required empty public constructor
    }

    public static ScoreCardFragment newInstance() {
        ScoreCardFragment fragment = new ScoreCardFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_driver_score_card_layout, container, false);
        AlertDB.mListener = this;
        initialize(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ScoreCardGet();
    }


    @Override
    public void onUpdate(final String code) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ScoreCardGet();
            }
        });
    }
}
