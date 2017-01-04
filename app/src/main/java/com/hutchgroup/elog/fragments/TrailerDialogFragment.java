package com.hutchgroup.elog.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.TrailerRecycleAdapter;
import com.hutchgroup.elog.beans.VehicleBean;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.VehicleDB;

import android.support.v4.app.DialogFragment;
import android.widget.ImageButton;

import java.util.ArrayList;

public class TrailerDialogFragment extends DialogFragment implements TrailerRecycleAdapter.IViewHolder {

    public OnFragmentInteractionListener mListener;
    TrailerRecycleAdapter rAdapter;
    ArrayList<VehicleBean> list;
    RecyclerView rvTrailer;
    EditText etSearch;
    String hooked = "";
    ImageButton imgCancel;

    public TrailerDialogFragment() {
        // Required empty public constructor
    }

    public static TrailerDialogFragment newInstance() {
        TrailerDialogFragment fragment = new TrailerDialogFragment();
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
        View view = inflater.inflate(R.layout.fragment_trailer_dialog, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        this.setCancelable(false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        for (String id : Utility.hookedTrailers) {
            hooked += "'" + id + "', ";
        }

        if (!hooked.isEmpty()) {
            hooked = hooked.replaceAll(", $", "");
        }
        TrailerRecycleAdapter.mListner = this;
        rvTrailer = (RecyclerView) view.findViewById(R.id.rvTrailer);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        rvTrailer.setLayoutManager(mLayoutManager);
        rvTrailer.setItemAnimator(new DefaultItemAnimator());
        imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyboard(getActivity(), v);
                dismiss();
            }
        });
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
        BindTrailers("", hooked);
    }

    private void BindTrailers(String search, String except) {
        list = VehicleDB.TrailerGet(search, except);
        rAdapter = new TrailerRecycleAdapter(list);
        rvTrailer.setAdapter(rAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        TrailerRecycleAdapter.mListner = null;

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
                        int trailerId = bean.getVehicleId();
                        if (mListener != null)
                            mListener.hooked(trailerId);
                        dismiss();
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

        void hooked(int trailerId);
    }


    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width =WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }
}
