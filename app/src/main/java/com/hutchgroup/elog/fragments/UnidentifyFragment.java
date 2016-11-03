package com.hutchgroup.elog.fragments;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.UnidentifiedAdapter;
import com.hutchgroup.elog.beans.DiagnosticIndicatorBean;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.common.DiagnosticMalfunction;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;

import java.util.ArrayList;


public class UnidentifyFragment extends Fragment implements View.OnClickListener, InputInformationDialog.InputInformationDialogInterface, UnidentifiedAdapter.UnidentifiedInterface {
    public int selectedItemIndex = -1;
    ListView lvUnidentified;
    ArrayList<EventBean> eventList;
    UnidentifiedAdapter adapter;
    int driverId = 0;

    //Button btnAssume;
    //Button btnSkip;
    ImageButton fabAssume;
    private OnFragmentInteractionListener mListener;
    boolean isAssume;
    //FloatingActionButton fabMenu;

    InputInformationDialog infosDialog;

    public UnidentifyFragment() {
        // Required empty public constructor

    }


    private void initialize(View view) {
        driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        lvUnidentified = (ListView) view.findViewById(R.id.lvUnidentified);
        //btnAssume = (Button) view.findViewById(R.id.btnAssume);
        //btnSkip = (Button) view.findViewById(R.id.btnSkip);
        fabAssume = (ImageButton) view.findViewById(R.id.fabAssume);
        fabAssume.setOnClickListener(this);

//        fabMenu = (FloatingActionButton) getActivity().findViewById(R.id.fab);
//        if (fabMenu != null) {
//            fabMenu.setVisibility(View.GONE);
//        }

//        btnAssume.setOnClickListener(this);
//        btnSkip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                if (fabMenu != null) {
////                    fabMenu.setVisibility(View.VISIBLE);
////                }
//                if (mListener != null) {
//                    mListener.onSkipAssumeRecord();
//                }
//            }
//        });
        EventBind();
    }

    private void EventBind() {
        selectedItemIndex = -1;
        eventList = EventDB.EventUnAssignedGet();
        adapter = new UnidentifiedAdapter(R.layout.unidentified_row_layout, eventList);
        adapter.mListener = this;
        lvUnidentified.setAdapter(adapter);

    }

    public static UnidentifyFragment newInstance() {
        //UnidentifyFragment fragment = new UnidentifyFragment();
        return new UnidentifyFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        isAssume = false;
        View view = inflater.inflate(R.layout.fragment_unidentify, container, false);
        initialize(view);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAssume:
            case R.id.fabAssume:
                if (isAssume) {
                    boolean status = false;
                    for (EventBean bean : eventList) {
                        if (bean.getChecked()) {
                            status = true;
                            break;
                        }
                    }
                    if (status) {
                        if (infosDialog == null) {
                            infosDialog = new InputInformationDialog();
                        }
                        if (!infosDialog.isVisible()) {
                            infosDialog.setTitle("Assuming Event(s)");
                            infosDialog.mListener = this;
                            infosDialog.setCallWhenAssume(true);
                            infosDialog.show(getFragmentManager(), "infos_dialog");
                        }
                    } else {
                        Utility.showAlertMsg("Please select record!");
                    }
                } else {
                    if (mListener != null) {
                        mListener.onSkipAssumeRecord();
                    }
                }

                break;
        }
    }

    @Override
    public void selectItem() {
        boolean status = false;
        for (EventBean bean : eventList) {
            if (bean.getChecked()) {
                status = true;
                break;
            }
        }
        if (status) {
            fabAssume.setImageResource(R.drawable.ic_fab_check_double);
            isAssume = true;
        } else {
            fabAssume.setImageResource(R.drawable.ic_fab_skip);
            isAssume = false;
        }
    }

    @Override
    public void onInputFinished() {
        Log.i("Input", "fragment onInputFinished");
        infosDialog.mListener = null;
    }

    @Override
    public void onInputSaved(String shipId, String trailerId) {
        boolean status = false;
        for (EventBean bean : eventList) {
            if (bean.getChecked()) {
                int eventId = bean.get_id();
                String eventDate = Utility.dateOnlyStringGet(bean.getEventDateTime());
                int dailyLogId = DailyLogDB.getDailyLog(driverId, eventDate);
                // EventDB.EventUpdate(eventId, bean.getEventRecordOrigin(), 2, driverId, dailyLogId, shipId, trailerId);
                EventDB.EventUpdate(eventId, 2, driverId);
                DailyLogDB.DailyLogSyncRevert(driverId, bean.getDailyLogId());

                EventDB.EventCopy(eventId, 4, 1, driverId, dailyLogId);
                DailyLogDB.DailyLogCertifyRevert(driverId, dailyLogId);
                status = true;
            }
        }

        if (status) {
            // total minutes of unidentified driving for previous 7 days and current 24 hours
            String date = Utility.getDateTime(Utility.getCurrentDate(), -7);
            int seconds = EventDB.getUnidentifiedTime(date);

            if (seconds <= 15 * 60) {
                //clear Data diagnostic event for unidentified driving time if indicator is on for this event
                if (DiagnosticIndicatorBean.UnidentifiedDrivingDiagnosticFg) {
                    DiagnosticIndicatorBean.UnidentifiedDrivingDiagnosticFg = false;

                    // clear data diagnostic event for unidentified driving time
                    DiagnosticMalfunction.saveDiagnosticIndicatorByCode("5", 4, "UnidentifiedDrivingDiagnosticFg");
                }
            }

            date = Utility.getDateTime(Utility.getCurrentDate(), 0);
            Utility.UnidentifiedDrivingTime = EventDB.getUnidentifiedTime(date);

            EventBind();
            if (mListener != null) {
                mListener.onAssumeRecord();
            }
        } else
            Utility.showAlertMsg("Please select record!");

        infosDialog.mListener = null;
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

        if (infosDialog != null) {
            infosDialog.mListener = null;
        }
        infosDialog = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void onAssumeRecord();

        void onSkipAssumeRecord();
    }


}
