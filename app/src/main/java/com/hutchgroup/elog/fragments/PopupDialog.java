package com.hutchgroup.elog.fragments;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

public class PopupDialog extends DialogFragment {

    public DialogActionInterface mListener;

    private CountDownTimer countDownTimer;

    private TextView tvCountdownView;
    private Button butChangeStatus;
    private Button butKeepDriving;
    private TextView tvMessage;

    private boolean bShowing;
    public boolean autoDissmisDialog;

    private String dialogTitle;

    public PopupDialog() {

        bShowing = false;
        autoDissmisDialog = false;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setTitle(String title) {
        dialogTitle = title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popup_dialog, container);
        try {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            this.setCancelable(false);
            tvCountdownView = (TextView) view.findViewById(R.id.countdownTimer);
            tvMessage = (TextView) view.findViewById(R.id.dialogMessage);
            butChangeStatus = (Button) view.findViewById(R.id.butChangeStatus);
            butChangeStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    butChangeStatus.setOnClickListener(null);
                    butKeepDriving.setOnClickListener(null);
                    if (mListener != null) {
                        mListener.changeStatusPressed();
                    }
                }
            });
            butKeepDriving = (Button) view.findViewById(R.id.butKeepDriving);
            butKeepDriving.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    butChangeStatus.setOnClickListener(null);
                    butKeepDriving.setOnClickListener(null);
                    if (mListener != null) {
                        mListener.keepDrivingPressed();
                    }
                }
            });
            bShowing = true;
            autoDissmisDialog = false;
        } catch (Exception e) {
            LogFile.write(PopupDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            countDownTimer = new CountDownTimer(60000, 1000) {
                @Override
                public void onTick(long l) {
                    int numLeft = ((int) Math.round(l / 1000.0) - 1);
                    String strNum = "";
                    if (numLeft < 10) {
                        strNum = "0" + numLeft;
                    } else {
                        strNum += numLeft;
                    }
                    tvCountdownView.setText("0:" + strNum);
                    if (Utility.motionFg) {
                        butChangeStatus.setOnClickListener(null);
                        butKeepDriving.setOnClickListener(null);
                        if (mListener != null) {
                            mListener.keepDrivingPressed();
                        }
                    }
                }

                @Override
                public void onFinish() {
                    autoDissmisDialog = true;
                    bShowing = false;
                    //dismiss();
                    if (mListener != null) {
                        mListener.dialogDismiss();
                    }
                }
            };
            countDownTimer.start();
        } catch (Exception e) {
            LogFile.write(PopupDialog.class.getName() + "::onActivityCreated Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
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

    public boolean isShowing() {
        return bShowing;
    }

    public void close() {
        try {
            countDownTimer.cancel();
            //countDownTimer.onFinish();
            bShowing = false;
            dismissAllowingStateLoss();
        } catch (Exception e) {
            LogFile.write(PopupDialog.class.getName() + "::close Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public interface DialogActionInterface {
        void dialogDismiss();

        void changeStatusPressed();

        void keepDrivingPressed();
    }
}
