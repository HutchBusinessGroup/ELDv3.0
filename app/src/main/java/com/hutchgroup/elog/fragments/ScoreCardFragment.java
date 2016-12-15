package com.hutchgroup.elog.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.ScoreCardAdapter;
import com.hutchgroup.elog.beans.AlertBean;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.AlertDB;
import com.hutchgroup.elog.db.HourOfServiceDB;

import java.util.ArrayList;

public class ScoreCardFragment extends Fragment implements AlertDB.IScoreCard {

    CheckBox swMonthly;
    ListView lvScoreCard;
    boolean isCurrentDate = true;

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
        AlertDB.mListener = this;
        initialize(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ScoreCardGet(isCurrentDate);
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
        tvDriving.setText("Driving: " + drivingScore);
        tvDeduction.setText("Deduction: " + deductedScore);
        tvTotalScores.setText("Net Scores: " + totalScores);
    }

    private void initialize(View view) {
        swMonthly = (CheckBox) view.findViewById(R.id.swMonthly);
        swMonthly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isCurrentDate = !b;
                ScoreCardGet(isCurrentDate);
            }
        });
        lvScoreCard = (ListView) view.findViewById(R.id.lvScoreCard);
        tvDriving = (TextView) view.findViewById(R.id.tvDriving);
        tvDeduction = (TextView) view.findViewById(R.id.tvDeduction);
        tvTotalScores = (TextView) view.findViewById(R.id.tvTotalScores);
        ScoreCardGet(isCurrentDate);
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
