package com.hutchgroup.elog.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.InspectionAdapter;
import com.hutchgroup.elog.beans.GPSData;
import com.hutchgroup.elog.beans.TripInspectionBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.TripInspectionDB;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DvirFragment extends Fragment implements View.OnClickListener, InspectionAdapter.ItemClickListener {
    final String TAG = DvirFragment.class.getName();

    ListView lvCurrentInspections;
    ImageButton butNewInspection;

    //List<TripInspectionBean> listInspections;
    InspectionAdapter adapter;

    int driverId = 0;

    private OnFragmentInteractionListener mListener;

    public DvirFragment() {
        // Required empty public constructor
        //mInstance = this;
    }


    private void initialize(View view) {
        //driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();

        lvCurrentInspections = (ListView) view.findViewById(R.id.lvCurrentInspections);

        adapter = new InspectionAdapter(getContext(), this, getListInspections());
        lvCurrentInspections.setAdapter(adapter);

        butNewInspection = (ImageButton) view.findViewById(R.id.btnNewInspection);
        butNewInspection.setOnClickListener(this);

        if (Utility.InspectorModeFg) {
            butNewInspection.setVisibility(View.GONE);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clearDvir();
                } catch (Exception e) {

                }
            }
        }).start();
    }

    public static DvirFragment newInstance() {
//        if (mInstance == null) {
//            mInstance = new DvirFragment();
//        }
        DvirFragment fragment = new DvirFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dvir, container, false);
        initialize(view);
        return view;
    }

    private void clearDvir() {
        String previousDate = Utility.getPreviousDateOnly(-1); // remove dvir older than 2 days
        ArrayList<TripInspectionBean> list = TripInspectionDB.getInspectionsToRemove(previousDate);
        StringBuilder ids = new StringBuilder();
        for (TripInspectionBean trip : list) {
            int id = trip.getId();
            String picture = trip.getPictures();
            try {
                if (!picture.equals("")) {
                    String[] pictures = picture.split(",");
                    for (int i = 0; i < pictures.length; i++) {
                        String path = pictures[i];
                        File file = new File(path);
                        if (file.exists()) {
                            file.delete();
                        }

                        File directory = file.getParentFile();
                        if (directory.isDirectory()) {
                            File[] contents = directory.listFiles();
                            if (contents.length == 0) {
                                directory.delete();
                            }
                        }
                    }
                }
            } catch (Exception exe) {

            }
            ids.append(id + ",");
        }

        if (list.size() > 0) {
            ids.setLength(ids.length() - 1);
            TripInspectionDB.removeDVIR(ids.toString());
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            //mListener.onFragmentInteraction(uri);
        }
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btnNewInspection:
                    Log.d(TAG, "launch");
                    if (mListener != null) {
                        mListener.newInspection();
                    }
                    break;
            }
        } catch (Exception e) {
            LogFile.write(DvirFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void viewInspection(TripInspectionBean bean) {
        Log.d(TAG, "View the inspection id:" + bean.getId() + " type " + bean.getType());
        if (mListener != null) {
            mListener.viewInspection(true, bean);
        }
    }

    private List<TripInspectionBean> getListInspections() {
        String previousDate = Utility.getPreviousDateOnly(-1);
        List<TripInspectionBean> listInspections = TripInspectionDB.getInspections(previousDate);

        boolean inspections = TripInspectionDB.getInspections(Utility.getCurrentDate(), Utility.onScreenUserId);
        if (inspections) {
            if (mListener != null)
                mListener.onUpdateInspectionIcon();
        }
        return listInspections;
    }


    //these callbacks are used to call Activity update the fragment with new data or change to another fragment
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
        void onUpdateInspectionIcon();

        void newInspection();

        void viewInspection(boolean viewMode, TripInspectionBean bean);
    }


}
