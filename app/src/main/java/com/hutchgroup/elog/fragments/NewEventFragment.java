package com.hutchgroup.elog.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.hutchgroup.elog.MainActivity;
import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.DiagnosticIndicatorBean;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.DiagnosticMalfunction;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;
import com.hutchgroup.elog.db.LoginDB;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class NewEventFragment extends Fragment implements View.OnClickListener, DutyStatusChangeDialog.DutyStatusChangeDialogInterface {

    TextView tvEventType;
    static TextView tvEventTime;
    TextView tvOdometer;
    TextView tvEngineHours;
    TextView tvOrigin;
    TextView tvLocationLabel;
    EditText edLocationDescription;
    EditText edComments, etPassword;
    Switch swTransfer;
    LinearLayout layoutTransfer;
    ScrollView sv_grid;
    boolean bDialogShowing;
    //Button butBack;
    //static Button butSave;
    ImageButton fabSave;

    int currentStatus = 1, eventId = 0;
    public static boolean CurrentEventFg = false;
    EventBean eventData;

    int driverId;
    int logId;
    String eventDescription = "";

    int eventType;
    int eventCode;
    int eventRecordOrigin;
    int eventRecordStatus;

    boolean isEditEvent;
    String selectedEventDateTime;

    DutyStatusChangeDialog dutyDialog;

    private OnFragmentInteractionListener mListener;

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        int second = 0;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            //final Calendar c = Calendar.getInstance();
            //int hour = c.get(Calendar.HOUR_OF_DAY);
            //int minute = c.get(Calendar.MINUTE);
            String[] times = tvEventTime.getText().toString().split(":");
            int hour = Integer.valueOf(times[0]);
            int minute = Integer.valueOf(times[1]);
            second = Integer.valueOf(times[2]);

            // Create a new instance of TimePickerDialog and return it
            TimePickerDialog dilog = new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
            return dilog;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar calendar = Calendar.getInstance();
            if (hourOfDay > calendar.get(Calendar.HOUR_OF_DAY)) {
                Utility.showAlertMsg("Please do not choose the future time.");
                return;
            } else if (hourOfDay == calendar.get(Calendar.HOUR_OF_DAY) && minute > calendar.get(Calendar.MINUTE)) {
                Utility.showAlertMsg("Please do not choose the future time.");
                return;
            }
            String s;
            Format formatter;

            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second); //reset seconds to zero

            formatter = new SimpleDateFormat("HH:mm:ss");
            s = formatter.format(calendar.getTime());
            tvEventTime.setText(s);
            //butSave.setEnabled(true);
        }
    }

    public NewEventFragment() {
    }

    private void initialize(View view) {
        try {
            driverId = Utility.onScreenUserId;
            isEditEvent = false;
            etPassword = (EditText) view.findViewById(R.id.etPassword);
            etPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //butSave.setEnabled(true);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            swTransfer = (Switch) view.findViewById(R.id.swTransfer);
            swTransfer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        etPassword.setVisibility(View.VISIBLE);
                        sv_grid.setVisibility(View.GONE);

                    } else {
                        etPassword.setVisibility(View.GONE);
                        sv_grid.setVisibility(View.VISIBLE);
                    }
                }
            });
            layoutTransfer = (LinearLayout) view.findViewById(R.id.layoutTransfer);
            sv_grid = (ScrollView) view.findViewById(R.id.sv_grid);

            tvEventType = (TextView) view.findViewById(R.id.tvEventType);

            tvEventTime = (TextView) view.findViewById(R.id.tvTimeValue);
            tvOdometer = (TextView) view.findViewById(R.id.tvOdometerValue);
            tvEngineHours = (TextView) view.findViewById(R.id.tvEngineHoursValue);
            tvOrigin = (TextView) view.findViewById(R.id.tvOriginValue);

            edLocationDescription = (EditText) view.findViewById(R.id.edLocationDescription);
            tvLocationLabel = (TextView) view.findViewById(R.id.tvDescriptionLabel);
            //always show these for create new event
            //create new event is always manually
            edLocationDescription.setVisibility(View.VISIBLE);
            tvLocationLabel.setVisibility(View.VISIBLE);

            edComments = (EditText) view.findViewById(R.id.edComments);

            tvEventTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimePicker();
                }
            });

            //butBack = (Button) view.findViewById(R.id.butEventBack);
            //butBack.setOnClickListener(this);
            //butSave = (Button) view.findViewById(R.id.butEventSave);
            //butSave.setOnClickListener(this);
            fabSave = (ImageButton) view.findViewById(R.id.fabSave);
            fabSave.setOnClickListener(this);

            tvEventTime.setText(Utility.getCurrentTime());
            tvOdometer.setText(CanMessages.OdometerReading);
            tvEngineHours.setText(CanMessages.EngineHours);

            updateEventType();

            if (eventData != null) {
                logId = eventData.getDailyLogId();
                eventId = eventData.get_id();
                //tvOrigin;
                //tvEngineHours.setText(eventData.getEngineHour());
                tvEventTime.setText(Utility.getTime(eventData.getEventDateTime()));
                //tvOdometer.setText(eventData.getOdometerReading());
                selectedEventDateTime = eventData.getEventDateTime();
                isEditEvent = true;
                String statusText = "";
                switch (eventData.getEventType()) {
                    case 1:
                        if (eventData.getEventCode() == 1) {
                            statusText = getResources().getString(R.string.off_duty_long);
                            currentStatus = 1;
                        } else if (eventData.getEventCode() == 2) {
                            statusText = getResources().getString(R.string.sleeper_long);
                            currentStatus = 2;
                        } else if (eventData.getEventCode() == 3) {
                            statusText = getResources().getString(R.string.driving_long);
                            currentStatus = 3;
                        } else {
                            statusText = getResources().getString(R.string.on_duty_long);
                            currentStatus = 4;
                        }
                        break;
                    case 3:
                        if (eventData.getEventCode() == 1) {
                            statusText = getResources().getString(R.string.personal_use_long);
                            currentStatus = 5;
                        } else if (eventData.getEventCode() == 2) {
                            statusText = getResources().getString(R.string.yard_move_long);
                            currentStatus = 6;
                        }
                        break;
                }
                if (currentStatus == 3 && Utility.user2.getAccountId() > 0 && eventData.getCoDriverId() > 0) {
                    layoutTransfer.setVisibility(View.VISIBLE);
                } else {
                    layoutTransfer.setVisibility(View.GONE);

                }
                tvEventType.setText(statusText);
                if (eventData.getEventRecordOrigin() == 1)
                    tvOrigin.setText(getResources().getString(R.string.event_origin_automatically));
                else
                    tvOrigin.setText(getResources().getString(R.string.event_origin_edited));

                //it is edited mode:
                //if we dont have latitude, user will enter manually
                if (Utility.currentLocation.getLatitude() < 0) {
                    edLocationDescription.setVisibility(View.VISIBLE);
                    tvLocationLabel.setVisibility(View.VISIBLE);
                } else {
                    edLocationDescription.setVisibility(View.GONE);
                    tvLocationLabel.setVisibility(View.GONE);
                }

                String annotation = "";
                if (eventData.getAnnotation() != null) {
                    annotation = eventData.getAnnotation();
                }
                edComments.setText(annotation);
                edLocationDescription.setText(eventData.getLocationDescription());

                tvOdometer.setText(eventData.getOdometerReading());
                tvEngineHours.setText(eventData.getEngineHour());

            } else {
                logId = DailyLogDB.getDailyLog(driverId, Utility.getCurrentDate());
            }
            if (currentStatus != 3) {
                tvEventType.setOnClickListener(this);
            }
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public static NewEventFragment newInstance() {
        return new NewEventFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_new_event, container, false);

        //Intent intent = getActivity().getIntent();
        currentStatus = getArguments().getInt("current_status", 1);
        eventData = null;
        if (getArguments().getBoolean("edit_event", false)) {
            eventData = (EventBean) getArguments().getSerializable("selected_event");
        }
        eventRecordOrigin = 2; //change by driver
        eventRecordStatus = 1; //active
        initialize(view);
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
      /*  try {

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.activity_new_event, null);

            ViewGroup viewGroup = (ViewGroup) getView();

            viewGroup.removeAllViews();
            viewGroup.addView(view);

            initialize(view);
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::onConfigurationChanged Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }*/
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.tvEventType:
                    //call activity Duty Status Change
                    launchDutyStatusChange();
                    break;
                case R.id.butEventBack:
                    Utility.hideKeyboard(getActivity(), view);
                    if (isEditEvent) {
                        mListener.onEditEventFinished();
                    } else {
                        mListener.onNewEventFinished();
                    }
                    //finish();
                    break;

                case R.id.butEventSave:
                case R.id.fabSave:
                    Utility.hideKeyboard(getActivity(), view);
                    if (swTransfer.isChecked()) {
                        String password = etPassword.getText().toString();
                        if (password.isEmpty()) {
                            Utility.showAlertMsg("Please enter password to continue!");
                            break;
                        } else {
                            if (!LoginDB.authCoDriver(eventData.getCoDriverId(), password)) {
                                Utility.showMsg("Please enter valid Password!");
                            } else {
                                String logDate = Utility.dateOnlyStringGet(eventData.getEventDateTime());

                                EventDB.EventUpdate(eventId, 2, driverId);
                                DailyLogDB.DailyLogSyncRevert(driverId, logId);

                                int dailyLogId = DailyLogDB.getDailyLog(eventData.getCoDriverId(), logDate);
                                EventDB.EventCopy(eventId, 4, 1, eventData.getCoDriverId(), dailyLogId);
                                DailyLogDB.DailyLogCertifyRevert(driverId, dailyLogId);

                                // EventDB.EventTransfer(eventData.get_id(), eventData.getDriverId(), eventData.getCoDriverId(), logDate);
                                mListener.onEditEventFinished();
                            }
                        }
                        break;
                    }

                    if (edComments.length() < 4) {
                        Utility.showAlertMsg("Please enter an explanation (minimum 4 characters long)!");
                        break;
                    }

                    if (Utility.currentLocation.getLatitude() < 0 && edLocationDescription.getText().toString().isEmpty()) {
                        Utility.showAlertMsg("Please enter your location!");
                        break;
                    }

                    if (isEditEvent) {
                        Date currentSelectedTime = Utility.parse(Utility.getDate(selectedEventDateTime) + " " + tvEventTime.getText());
                        Date previousTime = Utility.parse(selectedEventDateTime);
                        if (eventData.getEventRecordOrigin() != 4) {
                            EventBean previousEvent = EventDB.previousDutyStatusGet(driverId, selectedEventDateTime);
                            if (previousEvent.getEventCode() == 3 && currentSelectedTime.before(previousTime)) {
                                Utility.showAlertMsg("Shortening of drive time is not allowed!");
                                break;
                            }

                            if (currentSelectedTime.after(previousTime) && currentStatus == 3) {
                                Utility.showAlertMsg("Driving time shortened is not allowed!");
                                break;
                            }
                        }

                    }
                    saveEvent();

                    //finish();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void showTimePicker() {
        try {
            DialogFragment newFragment = new TimePickerFragment();
            newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::showTimePicker Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void launchDutyStatusChange() {
        try {
            /*if (getFragmentManager().getBackStackEntryCount() > 0) {
                for (int i = 0; i < getFragmentManager().getBackStackEntryCount(); i++)
                    getFragmentManager().popBackStackImmediate();
            }*/
            if (dutyDialog == null) {
                dutyDialog = new DutyStatusChangeDialog();
            } else {
                dutyDialog.clear();
            }

            if (dutyDialog.isAdded()) {
                dutyDialog.dismiss();
            }
            dutyDialog.mListener = this;
            bDialogShowing = true;
            dutyDialog.setFromNewEvent(1);
            dutyDialog.setCurrentStatus(currentStatus);
            dutyDialog.show(getFragmentManager(), "dutystatus_dialog");
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::launchDutyStatusChange Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void updateEventType() {
        try {
            String eventText = "";
            switch (currentStatus) {
                case 1:
                    eventType = 1;
                    eventCode = 1;
                    eventDescription = getResources().getString(R.string.duty_status_changed_to_off_duty);
                    eventText = "OFF DUTY";
                    break;
                case 2:
                    eventType = 1;
                    eventCode = 2;
                    eventDescription = getResources().getString(R.string.duty_status_changed_to_sleeper_berth);
                    eventText = "SLEEPER BERTH";
                    break;
                case 3:
                    eventType = 1;
                    eventCode = 3;
                    eventDescription = getResources().getString(R.string.duty_status_changed_to_driving);
                    eventText = "DRIVING";
                    break;
                case 4:
                    eventType = 1;
                    eventCode = 4;
                    eventDescription = getResources().getString(R.string.duty_status_changed_to_on_duty);
                    eventText = "ON DUTY";
                    break;
                case 5:
                    eventType = 3;
                    eventCode = 1;
                    eventDescription = getResources().getString(R.string.duty_status_changed_to_personal_use);
                    eventText = "PERSONAL USE";
                    break;
                case 6:
                    eventType = 3;
                    eventCode = 2;
                    eventDescription = getResources().getString(R.string.duty_status_changed_to_yard_move);
                    eventText = "YARD MOVE";
                    break;
            }


            tvEventType.setText(eventText);
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::updateEventType Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            if (requestCode == 1) {
                if (resultCode == Activity.RESULT_OK) {
                    //butSave.setEnabled(true);
                    setupKeyboard();
                    currentStatus = intent.getIntExtra("selected_duty_status", 1);

                    updateEventType();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.i("NewEventFragment", "Duty Status is not changed");
                }
            }
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::onActivityResult Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
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
        tvEventTime = null;
        //butSave = null;
        mListener = null;
        try {
            Utility.hideKeyboard(getActivity(), getActivity().getCurrentFocus());
        } catch (Exception exe) {

        }
    }

    @Override
    public void onSavedDutyStatus(int status, boolean saveForActiveDriver, String annotation, String location) {
        try {
            dutyDialog = null;
            //butSave.setEnabled(true);
            setupKeyboard();
            currentStatus = status;

            updateEventType();
            if (!annotation.isEmpty())
                edComments.setText(annotation);

            if (!location.isEmpty())
                edLocationDescription.setText(location);
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::onSavedDutyStatus Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onDissmisDialog() {
        bDialogShowing = false;
    }

    public void setupKeyboard() {
//        if (edLocationDescription == null || edComments == null) {
//            return;
//        }
        edLocationDescription.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on Enter key press
                    edLocationDescription.clearFocus();
                    //etPassword.requestFocus();
                    (new Handler()).postDelayed(new Runnable() {
                        public void run() {
                            edComments.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                            edComments.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                        }
                    }, 100);

                    return true;
                }
                return false;
            }
        });

        edComments.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            saveEvent();
                        }
                    }, 100);
                }
                return false;
            }
        });
    }

    private void saveEvent() {
        if (isEditEvent) {
            String location = edLocationDescription.getText().toString();
            EventBean newEvent = eventData;
            // set new editted informations
            newEvent.setEventType(eventType);
            newEvent.setEventCode(eventCode);
            newEvent.setEventCodeDescription(eventDescription);
            newEvent.setEventRecordOrigin(eventRecordOrigin);
            newEvent.setEventRecordStatus(eventRecordStatus);
            newEvent.setAnnotation(edComments.getText() + "");
            newEvent.setEventSequenceId(EventDB.getEventSequenceId());
            newEvent.setSyncFg(0);
            newEvent.setCreatedBy(driverId);
            newEvent.setCreatedDate(Utility.getCurrentDateTime());
            newEvent.setEventDateTime(Utility.getDate(selectedEventDateTime) + " " + tvEventTime.getText());
            if (!location.isEmpty()) {
                newEvent.setLatitude("0");
                newEvent.setLongitude("0");
                newEvent.setLocationDescription(location);
            }
            EventDB.EventSave(newEvent);

            // EventDB.EventCreateWithLocation(Utility.getDate(selectedEventDateTime) + " " + tvEventTime.getText(), eventType, eventCode, eventDescription, eventRecordOrigin, eventRecordStatus, logId, driverId, edLocationDescription.getText() + "", edComments.getText() + "");
            //update to change status of the old to inactive
            eventRecordStatus = 2;
            if (eventId != 0) {
                EventDB.EventUpdate(eventId, eventRecordStatus, driverId);
            } else
                EventDB.EventUpdate(selectedEventDateTime, eventRecordOrigin, eventRecordStatus, driverId, logId);


            // clear missing data diagnostic event
            if (DiagnosticIndicatorBean.MissingElementDiagnosticFg && eventType == 1 && eventCode >= 3) {
                if (!EventDB.getMissingDataFg(driverId)) {
                    DiagnosticIndicatorBean.MissingElementDiagnosticFg = false;
                    // save malfunction for Missing element Diagnostic event
                    DiagnosticMalfunction.saveDiagnosticIndicatorByCode("3", 4, "MissingElementDiagnosticFg");
                }
            }

            DailyLogDB.DailyLogCertifyRevert(driverId, logId);
            mListener.onEditEventSaved();

            if (CurrentEventFg) {
                MainActivity.currentDutyStatus = currentStatus;
                if (Utility.activeUserId == Utility.onScreenUserId)
                    MainActivity.activeCurrentDutyStatus = currentStatus;
            }
        } else {
            //123 LogFile.write(NewEventFragment.class.getName() + "::Save clicked: " + "Create new event" + " of driverId:" + driverId, LogFile.USER_INTERACTION, LogFile.DRIVEREVENT_LOG);
            EventDB.EventCreateManually(Utility.getCurrentDate() + " " + tvEventTime.getText(), eventType, eventCode, eventDescription, eventRecordOrigin, eventRecordStatus, logId, driverId, edLocationDescription.getText() + "", edComments.getText() + "");

            DailyLogDB.DailyLogCertifyRevert(driverId, logId);
            mListener.onNewEventSaved();
        }

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
        void onNewEventSaved();

        void onNewEventFinished();

        void onEditEventSaved();

        void onEditEventFinished();

    }
}
