package com.hutchgroup.elog.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.AxleAdapter;
import com.hutchgroup.elog.adapters.AxleRecycleAdapter;
import com.hutchgroup.elog.beans.AxleBean;
import com.hutchgroup.elog.beans.TPMSBean;
import com.hutchgroup.elog.beans.TrailerBean;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Tpms;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.TrailerDB;
import com.hutchgroup.elog.db.VehicleDB;

import java.util.ArrayList;


public class TpmsFragment extends Fragment implements View.OnClickListener, Tpms.ITPMS, AxleRecycleAdapter.IHookTrailer, TrailerDialogFragment.OnFragmentInteractionListener {
    String TAG = TpmsFragment.class.getName();
    RecyclerView rvTPMS;
    AxleRecycleAdapter rAdapter;
    ArrayList<AxleBean> list;
    private OnFragmentInteractionListener mListener;

    public TpmsFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static TpmsFragment newInstance() {
        TpmsFragment fragment = new TpmsFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tpms_test, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        AxleRecycleAdapter.mListner = this;
        rvTPMS = (RecyclerView) view.findViewById(R.id.rvTPMS);
        Configuration config = getResources().getConfiguration();
        RecyclerView.LayoutManager mLayoutManager;
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        } else {
            mLayoutManager = new LinearLayoutManager(getContext());
        }

        rvTPMS.setLayoutManager(mLayoutManager);
        rvTPMS.setItemAnimator(new DefaultItemAnimator());
        TPMSDataGet();
    }

    private void TPMSDataGet() {

        ArrayList<String> trailerList = TrailerDB.getHookedTrailer(); // including power unit

        list = VehicleDB.AxleInfoGet(trailerList);
        testData();
        int hooked = trailerList.size() - 1;
        int position = 1;
        for (int i = hooked; i < 3; i++) {
            AxleBean bean = new AxleBean();
            bean.setEmptyFg(true);
            bean.setAxlePosition(position);
            position++;
            list.add(bean);
        }

        rAdapter = new AxleRecycleAdapter(list);
        rvTPMS.setAdapter(rAdapter);
    }

    private void testData() {
        list = new ArrayList<>();
        list.add(createItem(1, 1, false, true, new double[]{90, 90, 90, 90}, new double[]{45, 45, 45, 45}, new double[]{80, 120}, new double[]{40, 60}));

        list.add(createItem(2, 1, true, true, new double[]{90, 90, 90, 90}, new double[]{50, 50, 50, 50}, new double[]{80, 120}, new double[]{40, 60}));
        list.add(createItem(3, 2, true, true, new double[]{90, 90, 90, 90}, new double[]{55, 55, 55, 55}, new double[]{80, 120}, new double[]{40, 60}));

/*
        list.add(createItem(1, 1, true, true, new double[]{95, 95, 95, 95}, new double[]{46, 46, 46, 46}, new double[]{80, 120}, new double[]{40, 60}));
        list.add(createItem(2, 2, true, true, new double[]{100, 100, 100, 100}, new double[]{47, 47, 47, 47}, new double[]{80, 120}, new double[]{40, 60}));
        list.add(createItem(3, 3, true, true, new double[]{100, 70, 100, 100}, new double[]{47, 47, 47, 30}, new double[]{80, 120}, new double[]{40, 60}));*/

    }

    private AxleBean createItem(int axleNo, int axlePosition, boolean doubleTire, boolean frontFg, double[] temp, double[] pressure, double[] tempRange, double[] pressRange) {
        AxleBean bean = new AxleBean();
        bean.setAxleNo(axleNo);
        bean.setAxlePosition(axlePosition);
        bean.setDoubleTireFg(doubleTire);
        bean.setFrontTireFg(frontFg);

        bean.setLowPressure(pressRange[0]);
        bean.setHighPressure(pressRange[1]);

        bean.setLowTemperature(tempRange[0]);
        bean.setHighTemperature(tempRange[1]);

        bean.setPressure1(pressure[0]);
        bean.setPressure2(pressure[1]);


        bean.setTemperature1(temp[0]);
        bean.setTemperature2(temp[1]);
        if (doubleTire) {
            bean.setTemperature3(temp[2]);
            bean.setTemperature4(temp[3]);

            bean.setPressure3(pressure[2]);
            bean.setPressure4(pressure[3]);
        }

        return bean;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        mListener = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup viewGroup = (ViewGroup) getView();
            View view = inflater.inflate(R.layout.fragment_tpms_test, viewGroup, false);
            viewGroup.removeAllViews();
            viewGroup.addView(view);
            initialize(view);

        } catch (Exception exe) {
        }

    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void update(TPMSBean data) {
        for (int j = 0; j < list.size(); j++) {
            AxleBean bean = list.get(j);
            String[] sensorIds = bean.getSensorIdsAll();
            for (int i = 0; i < sensorIds.length; i++) {
                String sensorId = sensorIds[i];
                if (sensorId.equals(data.getSensorId())) {
                    switch (i) {
                        case 0:
                            bean.setPressure1(data.getPressure());
                            bean.setTemperature1(data.getTemperature());
                            break;
                        case 1:
                            bean.setPressure2(data.getPressure());
                            bean.setTemperature2(data.getTemperature());
                            break;
                        case 2:
                            bean.setPressure3(data.getPressure());
                            bean.setTemperature3(data.getTemperature());
                            break;
                        case 3:
                            bean.setPressure4(data.getPressure());
                            bean.setTemperature4(data.getTemperature());
                            break;
                    }
                    rAdapter.notifyItemChanged(j);
                    return;
                }
            }

        }
    }

    TrailerDialogFragment dialog;

    @Override
    public void hook() {
        if (dialog == null) {
            dialog = new TrailerDialogFragment();
        }
        dialog.show(getFragmentManager(), "trailer_dialog");
    }

    @Override
    public void hooked(int trailerId) {
        TrailerBean bean = new TrailerBean();
        bean.setTrailerId(trailerId);
        bean.setHookDate(Utility.getCurrentDateTime());
        bean.setDriverId(Utility.activeUserId);
        bean.setHookedFg(1);
        bean.setLatitude1(Utility.currentLocation.getLatitude() + "");
        bean.setLongitude2(Utility.currentLocation.getLongitude() + "");
        bean.setStartOdometer(CanMessages.OdometerReading);
        TrailerDB.hook(bean);
        TPMSDataGet();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name

    }


}
