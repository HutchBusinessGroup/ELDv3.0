package com.hutchgroup.elog.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.DTCAdapter;
import com.hutchgroup.elog.beans.DTCBean;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DTCDB;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DTCFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class DTCFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public DTCFragment() {
        // Required empty public constructor
    }

    ListView lvActiveDTCCode, lvInActiveDTCCode;

    public static DTCFragment newInstance() {
        DTCFragment fragment = new DTCFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private ArrayList<DTCBean> testList() {

        ArrayList<DTCBean> list = new ArrayList<>();

        DTCBean dtcBean = new DTCBean();
        dtcBean.setSpn(2322);
        dtcBean.setSpnDescription("Engine Fuel Filter Differential Pressure");
        dtcBean.setFmi(322);
        dtcBean.setFmiDescription("Below Normal Operational Range - Moderate");
        dtcBean.setDateTime(Utility.getCurrentDateTime());
        dtcBean.setProtocol("J1939");
        dtcBean.setOccurence(1);
        dtcBean.setStatus(1);

        list.add(dtcBean);

        dtcBean = new DTCBean();
        dtcBean.setSpn(2323);
        dtcBean.setSpnDescription("Transmission Synchronizer Clutch Value");
        dtcBean.setFmi(322);
        dtcBean.setFmiDescription("Special Instructions");
        dtcBean.setDateTime(Utility.getCurrentDateTime());
        dtcBean.setProtocol("J1939");
        dtcBean.setOccurence(4);
        dtcBean.setStatus(1);

        list.add(dtcBean);


        dtcBean = new DTCBean();
        dtcBean.setSpn(2323);
        dtcBean.setSpnDescription("Transmission Synchronizer Clutch Value");
        dtcBean.setFmi(323);
        dtcBean.setFmiDescription("Special Instructions");
        dtcBean.setDateTime(Utility.getCurrentDateTime());
        dtcBean.setProtocol("J1939");
        dtcBean.setOccurence(4);
        dtcBean.setStatus(1);

        list.add(dtcBean);

        dtcBean = new DTCBean();
        dtcBean.setSpn(2324);
        dtcBean.setSpnDescription("Engine Blower Bypass Valve Position");
        dtcBean.setFmi(322);
        dtcBean.setFmiDescription("Current Below Normal");
        dtcBean.setDateTime(Utility.getCurrentDateTime());
        dtcBean.setProtocol("J1939");
        dtcBean.setOccurence(1);
        dtcBean.setStatus(1);

        list.add(dtcBean);

        dtcBean = new DTCBean();
        dtcBean.setSpn(2325);
        dtcBean.setSpnDescription("Engine Oil Filter Differential Pressure");
        dtcBean.setFmi(322);
        dtcBean.setFmiDescription("Root Cause Unknown");
        dtcBean.setDateTime(Utility.getCurrentDateTime());
        dtcBean.setProtocol("J1939");
        dtcBean.setOccurence(2);
        dtcBean.setStatus(0);

        list.add(dtcBean);

        dtcBean = new DTCBean();
        dtcBean.setSpn(2326);
        dtcBean.setSpnDescription("Engine Governor Droop");
        dtcBean.setFmi(322);
        dtcBean.setFmiDescription("Reserved");
        dtcBean.setDateTime(Utility.getCurrentDateTime());
        dtcBean.setProtocol("J1939");
        dtcBean.setOccurence(2);
        dtcBean.setStatus(1);

        list.add(dtcBean);

        dtcBean = new DTCBean();
        dtcBean.setSpn(2327);
        dtcBean.setSpnDescription("Engine Injector Metering Rail 2 Pressure");
        dtcBean.setFmi(322);
        dtcBean.setFmiDescription("Reserved");
        dtcBean.setDateTime(Utility.getCurrentDateTime());
        dtcBean.setProtocol("J1939");
        dtcBean.setOccurence(2);
        dtcBean.setStatus(0);

        list.add(dtcBean);
        return list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dtc, container, false);
        initialize(view);
        initializeTab(view);
        return view;
    }

    int activeCount = 0, inactiveCount = 0;

    private void initialize(View view) {
        ArrayList<DTCBean> list = DTCDB.getDTCCode();
       /* list = testList();
        DTCDB.Save(list);
*/
        ArrayList<DTCBean> activeList = new ArrayList<>();
        ArrayList<DTCBean> inactiveList = new ArrayList<>();
        for (DTCBean bean : list) {
            if (bean.getStatus() == 1) {
                activeList.add(bean);
            } else {
                inactiveList.add(bean);
            }
        }
        activeCount = activeList.size();
        inactiveCount = inactiveList.size();

        lvActiveDTCCode = (ListView) view.findViewById(R.id.lvActiveDTCCode);
        lvActiveDTCCode.setAdapter(new DTCAdapter(getContext(), R.layout.fragment_dtc, activeList));

        lvInActiveDTCCode = (ListView) view.findViewById(R.id.lvInActiveDTCCode);
        lvInActiveDTCCode.setAdapter(new DTCAdapter(getContext(), R.layout.fragment_dtc, inactiveList));
    }

    private void initializeTab(View view) {
        TabHost host = (TabHost) view.findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        View tabview = createTabView(host.getContext(), "Active");
        TabHost.TabSpec spec = host.newTabSpec("Active").setIndicator(tabview);
        spec.setContent(R.id.tabActive);
        host.addTab(spec);

        //Tab 2
        tabview = createTabView(host.getContext(), "In-active");
        spec = host.newTabSpec("In-active").setIndicator(tabview);
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

    }
}
