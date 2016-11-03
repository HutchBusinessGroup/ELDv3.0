package com.hutchgroup.elog.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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

public class InputTruckIDDialog extends DialogFragment implements View.OnClickListener {
    String TAG = InputTruckIDDialog.class.getName();

    public InputTruckIDDialogInterface mListener;

    EditText edTruckID;

    ImageButton imgCancel;
    Button butSave;
    String truckID;
    public InputTruckIDDialog(){

    }

    public void setTrucID(String id) {
        truckID = id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_input_truckid, container);

        try {
            initialize(view);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            this.setCancelable(false);

        } catch (Exception e) {
            LogFile.write(InputTruckIDDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
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

            edTruckID = (EditText) view.findViewById(R.id.edTruckID);
            edTruckID.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (butSave != null)
                        butSave.setEnabled(true);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            edTruckID.setText(truckID);

        } catch (Exception e) {
            LogFile.write(InputTruckIDDialog.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onClick(View view) {
        try {
            Utility.hideKeyboard(getActivity(), view);
            switch (view.getId()) {
                case R.id.imgCancel:

                    if (mListener != null) {
                        mListener.onTruckIDFinished();
                    }
                    dismiss();
                    break;
                case R.id.butSave:
                    Log.i("Input", "Save");
                    if (mListener != null) {
                        mListener.onTruckIDSaved(edTruckID.getText().toString());
                    }

                    dismiss();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(InputTruckIDDialog.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public interface InputTruckIDDialogInterface {
        void onTruckIDSaved(String truckID);
        void onTruckIDFinished();
    }
}
