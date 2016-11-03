package com.hutchgroup.elog.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;


public class ShutDownDeviceDialog extends DialogFragment {

    public OnFragmentInteractionListener mListener;

    private CountDownTimer countDownTimer;
    private TextView tvCountdownView;
    private TextView tvMessage;

    private boolean bShowing;

    public ShutDownDeviceDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shut_down_device_dialog, container, false);
        try {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            this.setCancelable(false);
            tvCountdownView = (TextView) view.findViewById(R.id.countdownTimer);
            tvMessage = (TextView) view.findViewById(R.id.dialogMessage);
            bShowing = true;
        } catch (Exception e) {
            LogFile.write(PopupDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            countDownTimer = new CountDownTimer(3 * 60 * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    int numLeft = ((int) Math.round(l / 1000.0) - 1);
                    String strNum = Utility.getTimeInMinuteFromSeconds(numLeft);
                    tvCountdownView.setText(strNum);
                    if (Float.valueOf(CanMessages.RPM) > 0f) {

                        dismiss();
                    }
                }

                @Override
                public void onFinish() {
                    bShowing = false;
                    shutdownDevice();
                    dismiss();
                }
            };
            countDownTimer.start();
        } catch (Exception e) {
            LogFile.write(PopupDialog.class.getName() + "::onActivityCreated Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
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
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        mListener = null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        tvCountdownView = null;

        mListener = null;

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

    }


    private void shutdownDevice() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot -p"});
            proc.waitFor();
        } catch (Exception exe) {
            Utility.showAlertMsg(exe.getMessage());
        }
    }
}
