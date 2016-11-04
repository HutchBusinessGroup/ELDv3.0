package com.hutchgroup.elog.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.hutchgroup.elog.R;


public class ExtraFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    ImageButton btnDTC;

    public ExtraFragment() {
        // Required empty public constructor
    }

    public static ExtraFragment newInstance() {
        ExtraFragment fragment = new ExtraFragment();
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
        View view = inflater.inflate(R.layout.fragment_extra, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        btnDTC = (ImageButton) view.findViewById(R.id.btnDTC);
        btnDTC.setOnClickListener(this);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDTC:
                mListener.onLoadDTC();
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void onLoadDTC();
    }
}
