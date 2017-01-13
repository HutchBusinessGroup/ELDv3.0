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

    CheckBox swMonthly;
    ListView lvScoreCard, lvValues;
    boolean isCurrentDate = true;
    public static boolean IsTesting = false;

    Thread thScores;
    TextView tvDrivingTime;

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
                ScoreCardGet(isCurrentDate);
            }
        });
        lvScoreCard = (ListView) view.findViewById(R.id.lvScoreCard);
        lvValues = (ListView) view.findViewById(R.id.lvValues);
        tvDriving = (TextView) view.findViewById(R.id.tvDriving);
        tvDeduction = (TextView) view.findViewById(R.id.tvDeduction);
        tvTotalScores = (TextView) view.findViewById(R.id.tvTotalScores);

        ScoreCardGet(isCurrentDate);
        StartThread();


    }

    private void fillValues()
    {
        String date = Utility.getCurrentDate();
        if (!isCurrentDate) {
            date = Utility.getPreviousDate(-30);
        }
        int drivingMinute = HourOfServiceDB.DrivingTimeGet(date, Utility.onScreenUserId);
        drivingScore = (drivingMinute / 10) * 5;
        int deductedScore = 0;
        list = AlertDB.getScoreCard(Utility.onScreenUserId, date);
        for (AlertBean item : list) {
            deductedScore += item.getScores();
        }
        int totalScores = drivingScore - deductedScore;
    }

    private void StopThread() {
        if (thScores != null) {
            thScores.interrupt();
            thScores = null;
        }
    }

    int i = 0;

    private void StartThread() {
        if (thScores != null) {
            thScores.interrupt();
            thScores = null;
        }
        thScores = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (Thread.interrupted())
                        break;
                    try {
                        Thread.sleep(1000);
                        setAlert();
                    } catch (Exception exe) {
                        break;
                    }
                }
            }
        });
        thScores.setName("ScoresThread");
        thScores.start();
    }

    TextView tvDriving, tvDeduction, tvTotalScores;

    private void setAlert() {
        ArrayList<AlertBean> arrayList = new ArrayList<>();
        arrayList.add(AddAlert("Low Washer Fluid", "80", CanMessages.WasherFluidLevel));
        arrayList.add(AddAlert("Failure to warm up the engine", "80", CanMessages.CoolantTemperature));
        arrayList.add(AddAlert("Low Engine Oil", "80", CanMessages.EngineOilLevel));
        arrayList.add(AddAlert("Low Coolant Level", "80", CanMessages.EngineCoolantLevel));
        arrayList.add(AddAlert("High RPM", "2000", CanMessages.RPM));
        arrayList.add(AddAlert("Speed Violation", (PostedSpeed + PostedSpeedThreshold) + "", CanMessages.Speed));
        arrayList.add(AddAlert("Hard Acceleration", ".25", GForceMonitor._acc + ""));
        arrayList.add(AddAlert("Hard Breaking", ".40", GForceMonitor._break + ""));
        arrayList.add(AddAlert("Sharp Turn Left", ".25", GForceMonitor._left + ""));
        arrayList.add(AddAlert("Sharp Turn Right", ".25", GForceMonitor._right + ""));

        final ScoreCardThresholdAdapter adapter = new ScoreCardThresholdAdapter(R.layout.fragment_score_card, arrayList);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (i == 300 && Utility.motionFg) {
                    ScoreCardGet(isCurrentDate);
                    i = 0;
                }
                i++;
                lvValues.setAdapter(adapter);
            }
        });
    }


    private AlertBean AddAlert(String Name, String threshold, String currentValue) {
        AlertBean bean = new AlertBean();
        bean.setAlertName(Name);
        bean.setThreshold(threshold);
        bean.setCurrentValue(currentValue);
        return bean;
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
        View view = inflater.inflate(R.layout.fragment_score_card, container, false);
        AlertDB.mListener = this;
        initialize(view);
        initializeTab(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ScoreCardGet(isCurrentDate);
    }

    private ArrayList<AlertBean> list = new ArrayList<>();
    int drivingScore = 0;

    private void ScoreCardGet(boolean isCurrentDate) {
        String date = Utility.getCurrentDate();
        if (!isCurrentDate) {
            date = Utility.getPreviousDate(-30);
        }
        int drivingMinute = HourOfServiceDB.DrivingTimeGet(date, Utility.onScreenUserId);
        drivingScore = (drivingMinute / 10) * 5;
        int deductedScore = 0;
        list = AlertDB.getScoreCard(Utility.onScreenUserId, date);
        for (AlertBean item : list) {
            deductedScore += item.getScores();
        }
        ScoreCardAdapter adapter = new ScoreCardAdapter(R.layout.fragment_score_card, list);
        lvScoreCard.setAdapter(adapter);
        int totalScores = drivingScore - deductedScore;
        tvDriving.setText("Driving: " + drivingScore);
        tvDeduction.setText("Deduction: " + deductedScore);
        tvTotalScores.setText("Net Scores: " + totalScores);
    }

    private void initializeTab(View view) {
        TabHost host = (TabHost) view.findViewById(R.id.tabHost);
        host.setup();
        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
              /*  if (tabId == "Current") {
                    StartThread();
                } else {
                }*/
            }
        });
        //Tab 1
        View tabview = createTabView(host.getContext(), "Current");
        TabHost.TabSpec spec = host.newTabSpec("Current").setIndicator(tabview);
        spec.setContent(R.id.tabActive);
        host.addTab(spec);

        //Tab 2
        tabview = createTabView(host.getContext(), "Alerts");
        spec = host.newTabSpec("Alerts").setIndicator(tabview);
        spec.setContent(R.id.tabInActive);
        host.addTab(spec);


    }


    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabdesign, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    @Override
    public void onUpdate() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ScoreCardGet(isCurrentDate);
            }
        });
    }
}
