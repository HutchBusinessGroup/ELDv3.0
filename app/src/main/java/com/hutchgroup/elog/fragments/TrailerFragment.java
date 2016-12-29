package com.hutchgroup.elog.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.TrailerRecycleAdapter;
import com.hutchgroup.elog.beans.VehicleBean;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.VehicleDB;

import java.util.ArrayList;

public class TrailerFragment extends Fragment implements TrailerRecycleAdapter.IViewHolder {

    private OnFragmentInteractionListener mListener;
    TrailerRecycleAdapter rAdapter;
    ArrayList<VehicleBean> list;
    RecyclerView rvTrailer;
    EditText etSearch;
    String hooked = "";

    public TrailerFragment() {
        // Required empty public constructor
    }

    public static TrailerFragment newInstance() {
        TrailerFragment fragment = new TrailerFragment();
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
        View view = inflater.inflate(R.layout.fragment_trailer, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        rvTrailer = (RecyclerView) view.findViewById(R.id.rvTrailer);

        etSearch = (EditText) view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                BindTrailers(s.toString(), hooked);
            }
        });
    }

    private void BindTrailers(String search, String except) {
        list = VehicleDB.TrailerGet(search, except);
        rAdapter = new TrailerRecycleAdapter(list);
        rvTrailer.setAdapter(rAdapter);
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
    public void onItemClick(final View view) {
        final AlertDialog ad = new AlertDialog.Builder(getContext())
                .create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle("Hook Trailer");
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage("Are you sure. You want to hook trailer?");

        ad.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {

                        int position = rvTrailer.getChildLayoutPosition(view);
                        VehicleBean bean = list.get(position);
                    }
                });
        ad.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        ad.cancel();
                    }
                });

        ad.show();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
