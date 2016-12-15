package com.hutchgroup.elog.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.ScoreCardAdapter;
import com.hutchgroup.elog.beans.AlertBean;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.AlertDB;

import java.util.ArrayList;

public class ScoreCardFragment extends Fragment implements AlertDB.IScoreCard {

    Switch swMonthly;
    ListView lvScoreCard;
    boolean isMonthly = false;

    public ScoreCardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_score_card, container, false);
        initialize(view);
        return view;
    }

    private void ScoreCardGet(boolean isCurrentDate) {
        String date = Utility.getCurrentDate();
        if (!isCurrentDate) {
            date = Utility.getPreviousDate(-30);
        }
        ArrayList<AlertBean> list = AlertDB.getScoreCard(Utility.onScreenUserId, date);
        ScoreCardAdapter adapter = new ScoreCardAdapter(R.layout.fragment_score_card, list);
        lvScoreCard.setAdapter(adapter);
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
        ScoreCardGet(true);
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
