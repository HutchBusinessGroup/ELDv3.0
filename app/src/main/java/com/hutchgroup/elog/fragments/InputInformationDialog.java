package com.hutchgroup.elog.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;

public class InputInformationDialog extends DialogFragment implements View.OnClickListener {
    String TAG = InputInformationDialog.class.getName();

    public InputInformationDialogInterface mListener;

    EditText edShippingNumber;
    //EditText edCommodity;
    EditText edTrailerNumber;

    ImageButton imgCancel;
    Button butSave;

    int driverId;

    boolean callWhenAssume = false;
    String dialogTitle = "E-Log";
    String selectedDate = "";

    public InputInformationDialog(){

    }

    public void setTitle(String value) {
        dialogTitle = value;
    }

    public void setCallWhenAssume(boolean value) {
        callWhenAssume = value;
    }

    public void setSelectedDate(String date) {
        selectedDate = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_input_infos, container);

        try {
            initialize(view);
            getDialog().setTitle(dialogTitle);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            this.setCancelable(false);

        } catch (Exception e) {
            LogFile.write(InputInformationDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width =WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    private void initialize(View view) {
        try {

            imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(this);
            butSave = (Button) view.findViewById(R.id.butSave);
            butSave.setOnClickListener(this);


            edShippingNumber = (EditText) view.findViewById(R.id.edShippingNumber);
            edTrailerNumber = (EditText) view.findViewById(R.id.edTrailerNumber);
            driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();

            if (Utility._appSetting.getCopyTrailer() == 1) {
                SharedPreferences sp = getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE);
                edShippingNumber.setText(Utility.ShippingNumber);
                edTrailerNumber.setText(Utility.TrailerNumber);
            } else {
                if (Utility.user1.getAccountId() > 0 && Utility.user2.getAccountId() > 0) {
                    //has two users
                    if (Utility.user1.isOnScreenFg() && Utility.user1.isActive()) {
                        SharedPreferences sp = getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE);
                        edShippingNumber.setText(Utility.ShippingNumber);
                        edTrailerNumber.setText(Utility.TrailerNumber);
                    }
                    if (Utility.user2.isOnScreenFg() && Utility.user2.isActive()) {
                        SharedPreferences sp = getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE);
                        edShippingNumber.setText(Utility.ShippingNumber);
                        edTrailerNumber.setText(Utility.TrailerNumber);
                    }
                }
            }


            Utility.ShippingNumber = edShippingNumber.getText().toString();
            Utility.TrailerNumber = edTrailerNumber.getText().toString();
        } catch (Exception e) {
            LogFile.write(InputInformationDialog.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.imgCancel:
                    if (mListener != null) {
                        mListener.onInputFinished();
                    }
                    Utility.hideKeyboard(getActivity(), view);
                    dismiss();
                    break;
                case R.id.butSave:
                    Utility.hideKeyboard(getActivity(), view);
                    saveInformation();
                    if (mListener != null) {
                        mListener.onInputSaved(edShippingNumber.getText().toString(), edTrailerNumber.getText().toString());
                    }

                    dismiss();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(InputInformationDialog.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void saveInformation() {
        if (!callWhenAssume) {
            SharedPreferences.Editor e = (getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE))
                    .edit();
            e.putString("shipping_number", edShippingNumber.getText().toString());
            e.putString("trailer_number", edTrailerNumber.getText().toString());
            //e.putInt("driverid", Utility.onScreenUserId);
            e.commit();

            Utility.ShippingNumber = edShippingNumber.getText().toString();
            Utility.TrailerNumber = edTrailerNumber.getText().toString();

            if (!selectedDate.equals("")) {
                DailyLogDB.DailyLogCreateByDate(driverId, selectedDate, edShippingNumber.getText().toString(), edTrailerNumber.getText().toString(), "" );
            } else {
                DailyLogDB.DailyLogCreate(driverId, edShippingNumber.getText().toString(), edTrailerNumber.getText().toString(), "");
            }
        }
    }

    public interface InputInformationDialogInterface {
        void onInputSaved(String shippId, String trailerId);
        void onInputFinished();
    }
}
