package com.hutchgroup.elog.fragments;


import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
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

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.ScoreCardAdapter;
import com.hutchgroup.elog.adapters.ScoreCardThresholdAdapter;
import com.hutchgroup.elog.beans.AlertBean;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.GForceMonitor;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.AlertDB;
import com.hutchgroup.elog.db.HourOfServiceDB;

import org.xmlpull.v1.XmlPullParserException;

import java.util.ArrayList;

import static com.hutchgroup.elog.common.AlertMonitor.PostedSpeed;
import static com.hutchgroup.elog.common.AlertMonitor.PostedSpeedThreshold;

public class ScoreCardFragment extends Fragment implements AlertDB.IScoreCard {

    CheckBox swMonthly;
    ListView lvScoreCard, lvValues;
    boolean isCurrentDate = true;
    public static boolean IsTesting = true;

    HorizontalBarChart mChart;
    Thread thScores;

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
        lvValues = (ListView) view.findViewById(R.id.lvValues);
        tvDriving = (TextView) view.findViewById(R.id.tvDriving);
        tvDeduction = (TextView) view.findViewById(R.id.tvDeduction);
        tvTotalScores = (TextView) view.findViewById(R.id.tvTotalScores);
        mChart = (HorizontalBarChart) view.findViewById(R.id.chartBar);
        mChart.setDrawBarShadow(false);

        mChart.setDrawValueAboveBar(true);

        mChart.getDescription().setEnabled(false);
        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        // draw shadows for each bar that show the maximum value
        // mChart.setDrawBarShadow(true);

        mChart.setDrawGridBackground(false);
        XAxis xl = mChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        // xl.setTypeface(mTfLight);
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(false);
        xl.setGranularity(10f);

        YAxis yl = mChart.getAxisLeft();
        // yl.setTypeface(mTfLight);
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
        yl.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        yl.setInverted(true);

        YAxis yr = mChart.getAxisRight();
        //  yr.setTypeface(mTfLight);
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(false);
        yr.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        yr.setInverted(true);

        ScoreCardGet(isCurrentDate);
        StartThread();
        setGraphData();
        mChart.setFitBars(true);
        mChart.animateY(2500);
        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setXEntrySpace(4f);
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

        //Tab 3
        View tabview = createTabView(host.getContext(), "Graph");
        TabHost.TabSpec spec = host.newTabSpec("Graph").setIndicator(tabview);
        spec.setContent(R.id.tabGraph);
        host.addTab(spec);

        tabview = createTabView(host.getContext(), "Current");
        spec = host.newTabSpec("Current").setIndicator(tabview);
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

    private static final String XMLTAG_ALERT = "alert";
    private ArrayList<AlertBean> graphData = new ArrayList<>();

    private void setGraphData() {

        float barWidth = 9f;
        float spaceForBar = 10f;
  /*      ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
int count = 12;
        int range = 50;
        for (int i = 0; i < count; i++) {
            float val = (float) (Math.random() * range);
            yVals1.add(new BarEntry(i * spaceForBar, val));
        }

        BarDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "DataSet 1");

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            //  data.setValueTypeface(mTfLight);
            data.setBarWidth(barWidth);
            mChart.setData(data);
        }*/

        if (graphData.size() == 0)
            graphData = getGraphData();

        ArrayList<BarEntry> yVal = new ArrayList<>();
        ArrayList<String> xAxis = new ArrayList<>();
        xAxis.add("Driving");
        yVal.add(new BarEntry(0, drivingScore));
        for (int i = 1; i <= graphData.size(); i++) {
            AlertBean data = graphData.get(i - 1);
            yVal.add(new BarEntry(i * spaceForBar, data.getScores() * 1.0f));
            xAxis.add(data.getAlertCode());
        }

        // BarData data = new BarData(xAxis, dataSets);
        // mChart.setData(data);
        BarDataSet set1;
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);

            set1.setValues(yVal);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVal, "Alert Data");
            set1.setColors(ColorTemplate.MATERIAL_COLORS);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData( dataSets);
            data.setValueTextSize(10f);
            //data.setValueTypeface(mTfLight);
            data.setBarWidth(barWidth);
            mChart.setData(data);
        }
    }

    // Created By: Deepak Sharma
    // Created Date: 11 Aug 2016
    // Purpose: get graph data
    public ArrayList<AlertBean> getGraphData() {
        ArrayList<AlertBean> myData = new ArrayList<>();

        try {
            XmlResourceParser xrp = Utility.context.getResources().getXml(R.xml.alert);
            while (xrp.next() != XmlResourceParser.START_TAG)
                continue;
            xrp.next();
            while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                while (xrp.getEventType() != XmlResourceParser.START_TAG) {
                    if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
                        return myData;
                    }
                    xrp.next();
                }
                if (xrp.getName().equals(XMLTAG_ALERT)) {
                    String code = xrp.getAttributeValue(0);
                    String displayName = xrp.nextText();
                    addItem(myData, code, displayName);
                }
                while (xrp.getEventType() != XmlResourceParser.END_TAG) {
                    xrp.next();
                }
                xrp.next();
            }
            xrp.close();
        } catch (XmlPullParserException xppe) {
        } catch (java.io.IOException ioe) {

        }

        return myData;
    }

    private void addItem(ArrayList<AlertBean> myData, String code, String name) {
        AlertBean bean = new AlertBean();
        bean.setAlertCode(code);
        bean.setAlertName(name);
        bean.setScores(0);
        for (AlertBean item : list) {
            if (item.getAlertCode().equals(code)) {
                bean.setScores(item.getScores());
            }
        }
        myData.add(bean);
    }
}
