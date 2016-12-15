package com.hutchgroup.elog.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.ScoreCardAdapter;
import com.hutchgroup.elog.beans.AlertBean;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.AlertDB;
import com.hutchgroup.elog.db.HourOfServiceDB;

import java.util.ArrayList;

public class ScoreCardFragment extends Fragment implements AlertDB.IScoreCard {

    Switch swMonthly;
    ListView lvScoreCard;
    boolean isMonthly = false;

    TextView tvDriving, tvDeduction, tvTotalScores;

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
        initialize(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ScoreCardGet(isMonthly);
    }

    private void ScoreCardGet(boolean isCurrentDate) {
        String date = Utility.getCurrentDate();
        if (!isCurrentDate) {
            date = Utility.getPreviousDate(-30);
        }
        int drivingMinute = HourOfServiceDB.DrivingTimeGet(date, Utility.onScreenUserId);
        int drivingScore = (drivingMinute / 10) * 5;
        int deductedScore = 0;
        ArrayList<AlertBean> list = AlertDB.getScoreCard(Utility.onScreenUserId, date);
        for (AlertBean item : list) {
            deductedScore += item.getScores();
        }
        ScoreCardAdapter adapter = new ScoreCardAdapter(R.layout.fragment_score_card, list);
        lvScoreCard.setAdapter(adapter);
        int totalScores = drivingScore - deductedScore;
        tvDriving.setText(drivingScore + "");
        tvDeduction.setText(deductedScore + "");
        tvTotalScores.setText(totalScores + "");
    }

    private void initialize(View view) {
        swMonthly = (Switch) view.findViewById(R.id.swMonthly);
        swMonthly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isMonthly = b;
                ScoreCardGet(isMonthly);
            }
        });
        lvScoreCard = (ListView) view.findViewById(R.id.lvScoreCard);
        tvDriving = (TextView) view.findViewById(R.id.tvDriving);
        tvDeduction = (TextView) view.findViewById(R.id.tvDeduction);
        tvTotalScores = (TextView) view.findViewById(R.id.tvTotalScores);
        ScoreCardGet(isMonthly);
    }

    @Override
    public void onUpdate() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ScoreCardGet(isMonthly);
            }
        });
    }
}
