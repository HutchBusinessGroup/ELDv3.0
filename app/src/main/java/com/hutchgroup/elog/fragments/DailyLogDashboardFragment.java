package com.hutchgroup.elog.fragments;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.common.LogFile;

/**
 * A simple {@link Fragment} subclass.
 */
public class DailyLogDashboardFragment extends Fragment implements View.OnClickListener {
    String TAG = DailyLogDashboardFragment.class.getName();

    private OnFragmentInteractionListener mListener;

    ImageButton butELog;
    ImageButton butInspectELog;
    ImageButton butCreateEvent;
    ImageButton butEditRequest;
    ImageButton butUnidentifiedEvent;
    ImageButton butCertifyLogBook;
    ImageButton butViolationHistory,btnDriverProfile, btnInspectorMode;

    View view = null;

    public DailyLogDashboardFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static DailyLogDashboardFragment newInstance() {
        DailyLogDashboardFragment fragment = new DailyLogDashboardFragment();
        return fragment;
    }

    private void initializeControls(View view) {
        butELog = (ImageButton) view.findViewById(R.id.btnELog);
        butELog.setOnClickListener(this);
        butInspectELog = (ImageButton) view.findViewById(R.id.btnInspectELog);
        butInspectELog.setOnClickListener(this);
        butCreateEvent = (ImageButton) view.findViewById(R.id.btnCreateEvent);
        butCreateEvent.setOnClickListener(this);
        butEditRequest = (ImageButton) view.findViewById(R.id.btnEditRequest);
        butEditRequest.setOnClickListener(this);
        butUnidentifiedEvent = (ImageButton) view.findViewById(R.id.btnUnidentified);
        butUnidentifiedEvent.setOnClickListener(this);
        butCertifyLogBook = (ImageButton) view.findViewById(R.id.btnCertify);
        butCertifyLogBook.setOnClickListener(this);
        butViolationHistory = (ImageButton) view.findViewById(R.id.btnViolationHistory);
        butViolationHistory.setOnClickListener(this);

        btnDriverProfile = (ImageButton) view.findViewById(R.id.btnDriverProfile);
        btnDriverProfile.setOnClickListener(this);


        btnInspectorMode = (ImageButton) view.findViewById(R.id.btnInspectorMode);
        btnInspectorMode.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //view = inflater.inflate(R.layout.fragment_daily_log_dashboard, container, false);

//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Log.i(TAG, "landscape");
//            view = inflater.inflate(R.layout.fragment_daily_log_dashboard, container, false);
//        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            Log.i(TAG, "portrait");
//            view = inflater.inflate(R.layout.fragment_daily_log_dashboard_portrait, container, false);
//        }

        //initializeControls(view);

        //using FrameLayout to inflate layout
        //this way will help fragment change layout when orientation is changed
        FrameLayout frameLayout = new FrameLayout(getActivity());
        populateViewForOrientation(inflater, frameLayout);
        return frameLayout;

        //return view;
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
//            Log.i(TAG, "this fragment onConfigurationChanged");
//            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            ViewGroup viewGroup = (ViewGroup) getView();
//            //viewGroup.removeAllViews();
//            viewGroup.removeView(view);
//
//            //view = inflater.inflate(R.layout.fragment_daily_log_dashboard, null);
//
//            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                Log.i(TAG, "landscape");
//                view = inflater.inflate(R.layout.fragment_daily_log_dashboard, viewGroup, false);
//            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//                Log.i(TAG, "portrait");
//                view = inflater.inflate(R.layout.fragment_daily_log_dashboard_portrait, viewGroup, false);
//            }
//
//            initializeControls(view);

            //viewGroup.addView(view);
//            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            View view = inflater.inflate(R.layout.fragment_daily_log_dashboard, null);
//
//            ViewGroup viewGroup = (ViewGroup) getView();
//
//            viewGroup.removeAllViews();
//            viewGroup.addView(view);

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            populateViewForOrientation(inflater, (ViewGroup) getView());


        } catch (Exception e) {
            LogFile.write(DailyLogDashboardFragment.class.getName() + "::onConfigurationChanged Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        View subview = inflater.inflate(R.layout.fragment_daily_log_dashboard, viewGroup);

        initializeControls(subview);

        // Find your buttons in subview, set up onclicks, set up callbacks to your parent fragment or activity here.
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btnELog:
                    if (mListener != null) {
                        mListener.callELog();
                    }
                    break;
                case R.id.btnInspectELog:
                    if (mListener != null) {
                        mListener.callInspectELog();
                    }
                    break;
                case R.id.btnCreateEvent:
                    if (mListener != null) {
                        mListener.callNewEvent();
                    }
                    break;
                case R.id.btnEditRequest:
                    if (mListener != null) {
                        mListener.callEditRequest();
                    }
                    break;
                case R.id.btnUnidentified:
                    if (mListener != null) {
                        mListener.callUnidentifiedEvent();
                    }
                    break;
                case R.id.btnCertify:
                    if (mListener != null) {
                        mListener.callCertifyLogBook();
                    }
                    break;
                case R.id.btnViolationHistory:
                    if (mListener != null) {
                        mListener.callViolationHistory();
                    }
                    break;
                case R.id.btnDriverProfile:
                    if (mListener != null) {
                        mListener.callDriverProfile();
                    }
                    break;
                case R.id.btnInspectorMode: {
                    if (mListener != null) {
                        mListener.setInspectorMode(true);
                    }
                }
            }
        } catch (Exception e) {
            LogFile.write(DailyLogDashboardFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
        void callELog();
        void callInspectELog();
        void callNewEvent();
        void callEditRequest();
        void callUnidentifiedEvent();
        void callCertifyLogBook();
        void callViolationHistory();
        void callDriverProfile();
        void setInspectorMode(boolean status);
    }


}
