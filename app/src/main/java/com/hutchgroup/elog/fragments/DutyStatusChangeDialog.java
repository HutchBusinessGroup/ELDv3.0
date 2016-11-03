package com.hutchgroup.elog.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.hutchgroup.elog.MainActivity;
import com.hutchgroup.elog.R;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;

public class DutyStatusChangeDialog extends DialogFragment implements View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener {
    String TAG = DutyStatusChangeDialog.class.getName();

    public DutyStatusChangeDialogInterface mListener;
    private final int OFF_DUTY = 1;
    private final int SLEEPER = 2;
    private final int DRIVING = 3;
    private final int ON_DUTY = 4;
    private final int PERSONAL_USE = 5;
    private final int YARD_MOVE = 6;

    Button btnAnnotation1, btnAnnotation2, btnAnnotation3, btnAnnotation4, btnAnnotation5, btnAnnotation6, btnAnnotation7;
    ColorStateList dutyStatusColorStateList;

    Button butOffDuty;
    Button butSleeper;
    Button butDriving;
    Button butDisableDriving;
    Button butOnDuty;
    Button butPersonalUse;
    Button butYardMove;
    Button butBack;
    Button butSave;
    EditText edAnnotation;
    EditText edLocation;
    ImageButton imgCancel;
    LinearLayout layoutOffDuty;
    LinearLayout layoutSleeper;
    LinearLayout layoutDriving;
    LinearLayout layoutOnDuty;
    LinearLayout layoutPersonalUse;
    LinearLayout layoutYardMove;

    int currentStatus = 1;
    int previousStatus = 1;
    int driverId;
    int logId;
    public String eventDescription = "";

    String textToSpeech = "";

    public int eventType;
    public int eventCode;
    public int eventRecordOrigin;
    public int eventRecordStatus;
    String shortStatus;

    int fromCreateEvent = 0;
    boolean saveForActiveDriver = false;
    String driverName = "";

    AlertDialog ad;
    Runnable clearText = new Runnable() {
        @Override
        public void run() {
            edAnnotation.setText("");
        }
    };

    public DutyStatusChangeDialog() {

    }

    public void clear() {
        edAnnotation.post(clearText);
        edLocation.setText("");
    }

    public void setCurrentStatus(int status) {
        currentStatus = status;
        previousStatus = currentStatus;
    }

    public void setFromNewEvent(int isNewEvent) {
        fromCreateEvent = isNewEvent;
    }

    public void setActiveDriver(boolean value) {
        saveForActiveDriver = value;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.activity_duty_status_change, container);

        try {
            dutyStatusColorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_selected}, //selected
                            new int[]{android.R.attr.state_enabled}, //un-selected
                    },
                    new int[]{
                            ContextCompat.getColor(getActivity().getBaseContext(), R.color.white), //1
                            ContextCompat.getColor(getActivity().getBaseContext(), R.color.sixsix) //2
                    }
            );

            //Intent intent = getIntent();

            driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            driverName = Utility.user1.isOnScreenFg() ? Utility.user1.getFirstName() : Utility.user2.getFirstName();

            if (saveForActiveDriver) {
                driverName = (Utility.user1.getAccountId() == Utility.activeUserId) ? Utility.user1.getFirstName() : Utility.user2.getFirstName();
            }

            initialize(view);

            eventRecordOrigin = 2; //change by driver
            eventRecordStatus = 1; //active

            //getDialog().setTitle("E-Log");
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            this.setCancelable(false);


        } catch (Exception e) {
            LogFile.write(DutyStatusChangeDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.activity_duty_status_change, null);

            ViewGroup viewGroup = (ViewGroup) getView();

            viewGroup.removeAllViews();
            viewGroup.addView(view);

            initialize(view);

        } catch (Exception e) {
            LogFile.write(DutyStatusChangeDialog.class.getName() + "::onConfigurationChanged Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {

        } catch (Exception e) {
            LogFile.write(DutyStatusChangeDialog.class.getName() + "::onActivityCreated Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    private void initialize(View view) {
        try {
            butOffDuty = (Button) view.findViewById(R.id.butOffDuty);
            butOffDuty.setOnClickListener(this);
            butSleeper = (Button) view.findViewById(R.id.butSleeper);
            butSleeper.setOnClickListener(this);
            butDriving = (Button) view.findViewById(R.id.butDriving);
            butDriving.setOnClickListener(this);
            butDisableDriving = (Button) view.findViewById(R.id.butDisableDriving);
            butOnDuty = (Button) view.findViewById(R.id.butOnDuty);
            butOnDuty.setOnClickListener(this);
            butPersonalUse = (Button) view.findViewById(R.id.butPersonalUse);
            butPersonalUse.setOnClickListener(this);
            butYardMove = (Button) view.findViewById(R.id.butYardMove);
            butYardMove.setOnClickListener(this);

            imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(this);
            butSave = (Button) view.findViewById(R.id.butDutyStatusSave);
            butSave.setOnClickListener(this);
            butSave.setEnabled(fromCreateEvent == 1);
            butSave.setOnClickListener(this);

            btnAnnotation1 = (Button) view.findViewById(R.id.btnAnnotation1);
            btnAnnotation1.setOnClickListener(this);

            btnAnnotation2 = (Button) view.findViewById(R.id.btnAnnotation2);
            btnAnnotation2.setOnClickListener(this);

            btnAnnotation3 = (Button) view.findViewById(R.id.btnAnnotation3);
            btnAnnotation3.setOnClickListener(this);

            btnAnnotation4 = (Button) view.findViewById(R.id.btnAnnotation4);
            btnAnnotation4.setOnClickListener(this);

            btnAnnotation5 = (Button) view.findViewById(R.id.btnAnnotation5);
            btnAnnotation5.setOnClickListener(this);

            btnAnnotation6 = (Button) view.findViewById(R.id.btnAnnotation6);
            btnAnnotation6.setOnClickListener(this);

            btnAnnotation7 = (Button) view.findViewById(R.id.btnAnnotation7);
            btnAnnotation7.setOnClickListener(this);

            edAnnotation = (EditText) view.findViewById(R.id.edDutyStatusAnnotation);
            edAnnotation.setOnFocusChangeListener(this);
            edAnnotation.setOnKeyListener(this);
            edAnnotation.setText("");

            edLocation = (EditText) view.findViewById(R.id.edLocation);
            edLocation.setText("");
            if (Utility.currentLocation.getLatitude() < 0) {
                edLocation.setVisibility(View.VISIBLE);
            } else {
                edLocation.setVisibility(View.GONE);
            }
            layoutOffDuty = (LinearLayout) view.findViewById(R.id.lOffDuty);
            layoutSleeper = (LinearLayout) view.findViewById(R.id.lSleeper);
            layoutDriving = (LinearLayout) view.findViewById(R.id.lDriving);
            layoutOnDuty = (LinearLayout) view.findViewById(R.id.lOnDuty);
            layoutPersonalUse = (LinearLayout) view.findViewById(R.id.lPersonalUse);
            layoutYardMove = (LinearLayout) view.findViewById(R.id.lYardMove);

            //butSave.setEnabled(false);
            if (Utility.user1.isOnScreenFg()) {
                if (Utility.user1.getSpecialCategory().equals("1")) {
                    layoutYardMove.setVisibility(View.GONE);
                } else if (Utility.user1.getSpecialCategory().equals("2")) {
                    layoutPersonalUse.setVisibility(View.GONE);
                } else if (Utility.user1.getSpecialCategory().equals("0")) {
                    layoutYardMove.setVisibility(View.GONE);
                    layoutPersonalUse.setVisibility(View.GONE);
                }
            } else if (Utility.user2.isOnScreenFg()) {
                if (Utility.user2.getSpecialCategory().equals("1")) {
                    layoutYardMove.setVisibility(View.GONE);
                } else if (Utility.user2.getSpecialCategory().equals("2")) {
                    layoutPersonalUse.setVisibility(View.GONE);
                } else if (Utility.user2.getSpecialCategory().equals("0")) {
                    layoutYardMove.setVisibility(View.GONE);
                    layoutPersonalUse.setVisibility(View.GONE);
                }
            }

            clearDutyStatus();
            switch (currentStatus) {
                case 1:
                    butOffDuty.setSelected(true);
                    break;
                case 2:
                    butSleeper.setSelected(true);
                    break;
                case 3:
                    butDriving.setSelected(true);
                    break;
                case 4:
                    butOnDuty.setSelected(true);
                    break;
                case 5:
                    butPersonalUse.setSelected(true);
                    break;
                case 6:
                    butYardMove.setSelected(true);
                    break;
            }
            logId = DailyLogDB.getDailyLog(driverId, Utility.getCurrentDate());
            if (logId == 0)
                logId = DailyLogDB.DailyLogCreate(driverId, Utility.ShippingNumber, Utility.TrailerNumber, "");
        } catch (Exception e) {
            LogFile.write(DutyStatusChangeDialog.class.getName() + "::initializeControls Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            Utility.printError(e.getMessage());
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            Utility.hideKeyboard(getActivity(), v);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                (keyCode == KeyEvent.KEYCODE_ENTER)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentStatus != previousStatus) {
                        String annotation = edAnnotation.getText() + "";
                        String location = edLocation.getText().toString();
                        if (eventType == 3 && annotation.length() < 4) {
                            Utility.showMsg("Please enter Annotation of atleast 4 words!");
                            return;
                        }
                        saveDutyStatus();

                        edAnnotation.setOnKeyListener(null);
                        edAnnotation.setOnFocusChangeListener(null);
                        edAnnotation.setOnClickListener(null);
                        if (mListener != null) {
                            mListener.onSavedDutyStatus(currentStatus, saveForActiveDriver, annotation, location);
                        }
                    }
                    mListener = null;
                    dismiss();
                }
            }, 100);
        }
        return false;
    }

    public static void onClickAnnotation(View v) {
        Utility.showAlertMsg("Show");
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.butOffDuty:
                    if (!butOffDuty.isSelected()) {
                        clearDutyStatus();
                        butOffDuty.setSelected(true);

                        setupKeyboard();
                        currentStatus = OFF_DUTY;
                        eventDescription = getResources().getString(R.string.duty_status_changed_to_off_duty);
                        textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_off_duty);
                        eventType = 1;
                        eventCode = 1;
                        shortStatus = getResources().getString(R.string.off_duty);
                    }
                    break;
                case R.id.butSleeper:
                    if (!butSleeper.isSelected()) {
                        clearDutyStatus();
                        butSleeper.setSelected(true);

                        setupKeyboard();
                        currentStatus = SLEEPER;
                        eventDescription = getResources().getString(R.string.duty_status_changed_to_sleeper_berth);
                        textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_sleeper_berth);
                        eventType = 1;
                        eventCode = 2;
                        shortStatus = getResources().getString(R.string.sleeper);
                    }
                    break;
                case R.id.butDriving:
                    if (!butDriving.isSelected()) {
                        if ((Utility.user1.isOnScreenFg() && !Utility.user1.isActive()) || (Utility.user2.isOnScreenFg() && !Utility.user2.isActive())) {
                            showWarningMessage("You must be Active User to change Duty Status to DRIVING!");
                            break;
                        }

                        clearDutyStatus();
                        butDriving.setSelected(true);

                        setupKeyboard();
                        currentStatus = DRIVING;
                        eventDescription = getResources().getString(R.string.duty_status_changed_to_driving);
                        textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_driving);
                        eventType = 1;
                        eventCode = 3;
                        shortStatus = getResources().getString(R.string.driving);
                    }
                    break;
                case R.id.butOnDuty:
                    if (!butOnDuty.isSelected()) {
                        clearDutyStatus();
                        butOnDuty.setSelected(true);

                        setupKeyboard();
                        currentStatus = ON_DUTY;
                        eventDescription = getResources().getString(R.string.duty_status_changed_to_on_duty);
                        textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_on_duty);
                        eventType = 1;
                        eventCode = 4;
                        shortStatus = getResources().getString(R.string.on_duty);
                    }
                    break;
                case R.id.butPersonalUse:
                    if (!butPersonalUse.isSelected()) {
                        if ((Utility.user1.isOnScreenFg() && !Utility.user1.isActive()) || (Utility.user2.isOnScreenFg() && !Utility.user2.isActive())) {
                            showWarningMessage("You must be Active User to indicate \"Authorize Personal Use of CMV\"!");
                            break;
                        }

                        clearDutyStatus();
                        butPersonalUse.setSelected(true);

                        setupKeyboard();
                        currentStatus = PERSONAL_USE;
                        eventDescription = getResources().getString(R.string.duty_status_changed_to_personal_use);
                        textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_personal_use);
                        eventType = 3;
                        eventCode = 1;
                        shortStatus = getResources().getString(R.string.personal_use);
                    }
                    break;
                case R.id.butYardMove:
                    if (!butYardMove.isSelected()) {
                        if ((Utility.user1.isOnScreenFg() && !Utility.user1.isActive()) || (Utility.user2.isOnScreenFg() && !Utility.user2.isActive())) {
                            showWarningMessage("You must be Active User to indicate \"Yard Moves\"!");
                            break;
                        }

                        clearDutyStatus();
                        butYardMove.setSelected(true);

                        setupKeyboard();
                        currentStatus = YARD_MOVE;
                        eventDescription = getResources().getString(R.string.duty_status_changed_to_yard_move);
                        textToSpeech = driverName + " " + getResources().getString(R.string.texttospeech_duty_status_changed_to_yard_move);
                        eventType = 3;
                        eventCode = 2;
                        shortStatus = getResources().getString(R.string.yard_move);
                    }
                    break;
                case R.id.imgCancel:
                    if (mListener != null) {
                        mListener.onDissmisDialog();
                    }
                    Utility.hideKeyboard(getActivity(), view);
                    mListener = null;
                    edAnnotation.setOnKeyListener(null);
                    edAnnotation.setOnFocusChangeListener(null);
                    edAnnotation.setOnClickListener(null);

                    dismiss();
                    break;
                case R.id.butDutyStatusSave:

                    Utility.hideKeyboard(getActivity(), view);
                    if (currentStatus != previousStatus) {
                        String annotation = edAnnotation.getText() + "";
                        String location = edLocation.getText().toString();
                        if (eventType == 3 && annotation.length() < 4) {
                            Utility.showMsg("Please enter comment minimum 4 characters long!");
                            return;
                        }

                        if (Utility.currentLocation.getLatitude() < 0 && edLocation.getText().toString().isEmpty()) {
                            Utility.showAlertMsg("Please enter location before saving event!");
                            return;
                        }
                        saveDutyStatus();

                        edAnnotation.setOnKeyListener(null);
                        edAnnotation.setOnFocusChangeListener(null);
                        edAnnotation.setOnClickListener(null);


                        if (mListener != null) {
                            mListener.onSavedDutyStatus(currentStatus, saveForActiveDriver, annotation, location);
                        }

                        dismiss();
                    }

                    mListener = null;
                    break;
                case R.id.btnAnnotation1:
                case R.id.btnAnnotation2:
                case R.id.btnAnnotation3:
                case R.id.btnAnnotation4:
                case R.id.btnAnnotation5:
                case R.id.btnAnnotation6:
                case R.id.btnAnnotation7:
                    try {
                        Button annotation = (Button) view;
                        edAnnotation.setText(annotation.getText());
                    } catch (Exception exe) {
                    }

                    break;
            }
            if (fromCreateEvent == 0) {
                butSave.setEnabled(currentStatus != previousStatus);
            }


        } catch (Exception e) {
            LogFile.write(DutyStatusChangeDialog.class.getName() + "::onClick Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void clearDutyStatus() {
        try {
            butOffDuty.setSelected(false);
            butSleeper.setSelected(false);
            butDriving.setSelected(false);
            butOnDuty.setSelected(false);
            butPersonalUse.setSelected(false);
            butYardMove.setSelected(false);

            butOffDuty.setTextColor(dutyStatusColorStateList);
            butSleeper.setTextColor(dutyStatusColorStateList);
            butDriving.setTextColor(dutyStatusColorStateList);
            butOnDuty.setTextColor(dutyStatusColorStateList);
            butPersonalUse.setTextColor(dutyStatusColorStateList);
            butYardMove.setTextColor(dutyStatusColorStateList);
        } catch (Exception e) {
            LogFile.write(DutyStatusChangeDialog.class.getName() + "::clearDutyStatus Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void saveDutyStatus() {
        String dateTime = Utility.getCurrentDateTime();
        String annotation = edAnnotation.getText() + "";


        if (fromCreateEvent == 0) {
            int dId = driverId;
            int log = logId;
            if (saveForActiveDriver) {
                dId = Utility.activeUserId;
                log = DailyLogDB.DailyLogCreate(dId, Utility.ShippingNumber, Utility.TrailerNumber, "");
            }

            //123 LogFile.write(DutyStatusChangeDialog.class.getName() + "::Save clicked " + "to Save Duty Status:" + currentStatus + " of driverId:" + dId, LogFile.USER_INTERACTION, LogFile.DRIVEREVENT_LOG);
            if (currentStatus == 3)
                Utility.DrivingTime = 0;
            if (Utility.currentLocation.getLatitude() < 0) {
                String location = edLocation.getText() + "";
                EventDB.EventCreateWithLocation(dateTime, eventType, eventCode, eventDescription, eventRecordOrigin, eventRecordStatus, log, dId, location, annotation);
            } else
                EventDB.EventCreate(dateTime, eventType, eventCode, eventDescription, eventRecordOrigin, eventRecordStatus, log, dId, annotation);

            if (previousStatus == 5 || previousStatus == 6) {
                EventDB.EventCreate(dateTime, 3, 0, "Driver Indication for PC, YM and WT cleared", 1, 1, log, dId, annotation);
            }

            if (Utility._appSetting.getDutyStatusReading() == 1) {
                if (MainActivity.textToSpeech != null)
                    MainActivity.textToSpeech.speak(textToSpeech, TextToSpeech.QUEUE_ADD, null);
            }
        }
    }

    private void showWarningMessage(String message) {
        if (ad == null) {
            ad = new AlertDialog.Builder(getActivity()).create();
        }
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle("E-Log");
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage(message);
        ad.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ad.dismiss();
                    }
                });
        ad.show();
    }

    private void setupKeyboard() {

       /* edAnnotation.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (currentStatus != previousStatus) {
                                String annotation = edAnnotation.getText() + "";
                                if (eventType == 3 && annotation.length() < 4) {
                                    Utility.showMsg("Please enter Annotation of atleast 4 words!");
                                    return;
                                }
                                saveDutyStatus();


                                if (mListener != null) {
                                    mListener.onSavedDutyStatus(currentStatus);
                                }
                            }
                            mListener = null;
                            dismiss();
                        }
                    }, 100);
                }
                return false;
            }
        });*/
    }

    public interface DutyStatusChangeDialogInterface {
        void onSavedDutyStatus(int status, boolean saveForActiveDriver, String annotation, String location);

        void onDissmisDialog();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ad = null;
        dismiss();
    }
}
