package com.hutchgroup.elog.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;

import java.util.Date;


public class InspectLogFragment extends Fragment {

    private ViewPager pager;
    private android.support.v4.view.PagerAdapter adapter;
    private OnFragmentInteractionListener mListener;
    private int totalPage = 15;
    private Date startDate;

    public InspectLogFragment() {
        // Required empty public constructor
    }


    public static InspectLogFragment newInstance() {
        return new InspectLogFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inspect_log, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {

        int currentRule = DailyLogDB.getCurrentRule(Utility.onScreenUserId);
        if (Utility.InspectorModeFg && currentRule > 2) {
            totalPage = 8;
        }
        startDate = Utility.dateOnlyGet(new Date());
        startDate = Utility.addDays(startDate, -(totalPage - 1));

        pager = (ViewPager) view.findViewById(R.id.pager);
        adapter = new SlideAdapter(getFragmentManager());
        pager.setAdapter(adapter);
        pager.setCurrentItem(totalPage - 1);

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

    private class SlideAdapter extends FragmentStatePagerAdapter {
        public SlideAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Date date = Utility.addDays(startDate, position);
            return DetailFragment.newInstance(date);
        }

        @Override
        public int getCount() {
            return totalPage;
        }
    }
}
