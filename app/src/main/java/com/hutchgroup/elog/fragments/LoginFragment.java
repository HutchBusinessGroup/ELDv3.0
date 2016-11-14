package com.hutchgroup.elog.fragments;

import android.app.AlertDialog;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hutchgroup.elog.FirstTimeUser;
import com.hutchgroup.elog.MainActivity;
import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.DiagnosticMalfunctionAdapter;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.beans.MessageBean;
import com.hutchgroup.elog.beans.SettingsBean;
import com.hutchgroup.elog.bll.HourOfService;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.ChatClient;
import com.hutchgroup.elog.common.ConstantFlag;
import com.hutchgroup.elog.common.DiagnosticMalfunction;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.UserPreferences;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;
import com.hutchgroup.elog.db.LoginDB;
import com.hutchgroup.elog.db.MessageDB;
import com.hutchgroup.elog.db.SettingsDB;
import com.hutchgroup.elog.tasks.SyncData;
import com.hutchgroup.elog.tasks.SyncUserData;

import java.util.Date;
import java.util.List;
import java.util.Locale;


public class LoginFragment extends Fragment implements View.OnClickListener {
    String TAG = LoginFragment.class.getName();

    private OnFragmentInteractionListener mListener;

    int SPEAK_OUT_PERIOD = 30;

    AutoCompleteTextView etUserName;
    EditText etPassword;
    Button btnLogin;
    ImageButton butDiagnostic, butMalfunction, fabSync, btnBack;

    Dialog diagnosticDlg;
    Dialog malfunctionDlg;

    TextView tvVersion;

    TextToSpeech textToSpeech;
    boolean coDriver = false, InspectorModeFg;
    boolean firstLogin = true;

    RelativeLayout rlLoadingPanel;

    AlertDialog alertDialog;
    Thread thBTB = null;

    boolean vehicleStarted = false, vehicleStopped = false;

    // Deepak Sharma
    // 3 Aug 2016
    // send request to bluetooth device every 5 seconds
    private void startBTBThread() {

        if (thBTB != null) {
            thBTB.interrupt();
            thBTB = null;
        }
        thBTB = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException exe) {
                    }
                    if (MainActivity.undockingMode) {
                        continue;
                    }

                    if (CanMessages.mState == CanMessages.STATE_CONNECTED) {
                        if (Float.valueOf(CanMessages.RPM) > 0f) {
                            if (!vehicleStarted) {
                                vehicleStarted = true;
                                vehicleStopped = false;
                                if (mListener != null) {
                                    mListener.onAlertClear();
                                }
                            }
                        } else {
                            if (!vehicleStopped) {
                                vehicleStopped = true;
                                vehicleStarted = false;
                                if (mListener != null) {
                                    mListener.onAlertVehicleStart();
                                }
                            }
                        }
                    }
                }
            }
        });
        thBTB.setName("thBTB");
        thBTB.start();
    }

    private void stopBTBThread() {
        if (thBTB != null) {
            thBTB.interrupt();
            thBTB = null;
        }
    }


    Handler checkLoginHandler = new Handler();
    Runnable checkLogin = new Runnable() {
        @Override
        public void run() {
            if (Utility.activeUserId == 0) {
                if (Utility.motionFg) {
                    textToSpeech.speak("Unidentified driver you must stop and log in to the E L D", TextToSpeech.QUEUE_ADD, null);
                }
                checkLoginHandler.postDelayed(this, SPEAK_OUT_PERIOD * 1000);
            }
        }
    };

    public LoginFragment() {

    }


    private void initialize(View view) {
        try {

            tvVersion = (TextView) view.findViewById(R.id.tvVersion);
            rlLoadingPanel = (RelativeLayout) getActivity().findViewById(R.id.loadingPanel);

            tvVersion.setText("Version " + Utility.ApplicationVersion);
            fabSync = (ImageButton) view.findViewById(R.id.fab);

            fabSync.setOnClickListener(this);

            etUserName = (AutoCompleteTextView) view.findViewById(R.id.etUserName);
            etPassword = (EditText) view.findViewById(R.id.etPassword);
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etPassword.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getActionMasked();

                    switch (action) {

                        case MotionEvent.ACTION_DOWN:
                            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            break;

                        case MotionEvent.ACTION_UP:
                            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            break;
                    }

                    return v.onTouchEvent(event);
                }
            });


            etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        login();
                    }
                    return false;
                }
            });

            butDiagnostic = (ImageButton) view.findViewById(R.id.btnDiagnostic);
            //if (!Utility.dataDiagnosticIndicatorFg) {
            butDiagnostic.setVisibility(View.GONE);
            //}
            butDiagnostic.setOnClickListener(this);

            butMalfunction = (ImageButton) view.findViewById(R.id.btnMalfunction);
            if (!Utility.malFunctionIndicatorFg) {
                butMalfunction.setVisibility(View.GONE);
            }
            butMalfunction.setOnClickListener(this);


            btnLogin = (Button) view.findViewById(R.id.btnLogin);
            btnLogin.setOnClickListener(this);

            btnBack = (ImageButton) view.findViewById(R.id.btnBack);
            btnBack.setOnClickListener(this);
            btnBack.setVisibility(View.GONE);

            if (coDriver) {
                btnBack.setVisibility(View.VISIBLE);
                Utility.user2.setFirstLoginFg(Utility.user2.getAccountId() == 0);
                if (!Utility.user2.isFirstLoginFg()) {
                    etUserName.setText(Utility.user2.getUserName());
                    etUserName.setEnabled(false);
                    firstLogin = false;
                }

            } else {
                Utility.user1.setFirstLoginFg(Utility.user1.getAccountId() == 0);
                if (!Utility.user1.isFirstLoginFg()) {
                    etUserName.setText(Utility.user1.getUserName());
                    etUserName.setEnabled(false);
                    firstLogin = false;
                    btnBack.setVisibility(View.VISIBLE);
                }

            }

            if (InspectorModeFg) {
                btnBack.setVisibility(View.GONE);
            } else {
                if (firstLogin) {
                    startBTBThread();
                }
            }

            if (mListener != null) {
                mListener.updateFlagbar(false);
            }
        } catch (Exception e) {
            LogFile.write(LoginFragment.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_login, container, false);
        coDriver = getArguments().getBoolean("CoDriverFg");
        InspectorModeFg = getArguments().getBoolean("InspectorModeFg");
        initialize(mainView);

        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        if (Utility.user1.getAccountId() == 0 && Utility.user2.getAccountId() == 0) {
            checkLoginHandler.postDelayed(checkLogin, 0);
        }

        if (ConstantFlag.Flag_Development) {
            String username = "deepak";

            if (Utility.IMEI.equals("355458048616198")) {
                username = "gary";
                if (coDriver) {
                    username = "minh";
                }
                if (etUserName.getText().toString().isEmpty())
                    etUserName.setText(username);
                if (etPassword.getText().toString().isEmpty())
                    etPassword.setText("#72Hutch5");
            } else {
                if (coDriver) {
                    username = "rakesh";
                }
                if (etUserName.getText().toString().isEmpty())
                    etUserName.setText(username);
                if (etPassword.getText().toString().isEmpty())
                    etPassword.setText("#85Shar8");

            }
        }
        return mainView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View view) {
        try {
            Utility.hideKeyboard(getActivity(), view);
            switch (view.getId()) {
                case R.id.btnLogin:
                    login();
                    break;
                case R.id.btnBack:
                    mListener.backFromLogin();
                    break;
                case R.id.fab:

                    if (Utility.isInternetOn()) {
                        showLoaderAnimation(true);
                        new SyncData(syncDataPostTaskListener).execute();
                    } else {
                        Utility.showAlertMsg("Please check your internet connection!");
                    }
                    break;
                case R.id.btnDiagnostic:
                    if (diagnosticDlg == null) {
                        diagnosticDlg = new Dialog(getActivity());
                        diagnosticDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    }

                    if (diagnosticDlg != null && !diagnosticDlg.isShowing()) {
                        LayoutInflater li = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View v = li.inflate(R.layout.listview_dialog, null, false);

                        diagnosticDlg.setContentView(v);

                        TextView tvTitle = (TextView) diagnosticDlg.findViewById(R.id.tvTitle);
                        tvTitle.setText("Data Diagnostic");
                        ImageButton imgCancel = (ImageButton) diagnosticDlg.findViewById(R.id.imgCancel);
                        imgCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                diagnosticDlg.dismiss();
                            }
                        });

                        ListView listView = (ListView) diagnosticDlg.findViewById(R.id.lvDiagnosticMalfunctionEvent);
                        DiagnosticMalfunctionAdapter eventAdapter = new DiagnosticMalfunctionAdapter(getActivity(), getDiagnosticMalfunctionEvents(3));
                        listView.setAdapter(eventAdapter);
                        diagnosticDlg.show();
                    }
                    break;
                case R.id.btnMalfunction:
                    if (malfunctionDlg == null) {
                        malfunctionDlg = new Dialog(getActivity());
                        malfunctionDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    }
                    if (malfunctionDlg != null && !malfunctionDlg.isShowing()) {
                        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View malView = layoutInflater.inflate(R.layout.listview_dialog, null, false);

                        malfunctionDlg.setContentView(malView);

                        TextView tvTitle = (TextView) malfunctionDlg.findViewById(R.id.tvTitle);
                        tvTitle.setText("Malfunction");

                        ImageButton imgCancel = (ImageButton) malfunctionDlg.findViewById(R.id.imgCancel);
                        imgCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                malfunctionDlg.dismiss();
                            }
                        });

                        ListView listViewEvent = (ListView) malfunctionDlg.findViewById(R.id.lvDiagnosticMalfunctionEvent);
                        DiagnosticMalfunctionAdapter malEventAdapter = new DiagnosticMalfunctionAdapter(getActivity(), getDiagnosticMalfunctionEvents(1));
                        listViewEvent.setAdapter(malEventAdapter);
                        malfunctionDlg.show();
                    }
                    break;
            }
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
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
        mListener = null;
        checkLoginHandler.removeCallbacksAndMessages(null);
        if (textToSpeech != null) {
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        diagnosticDlg = null;
        malfunctionDlg = null;
        stopBTBThread();
    }

    public void updateDiagnosticMalfunction() {
        if (butMalfunction != null) {
            if (Utility.malFunctionIndicatorFg) {
                butMalfunction.setVisibility(View.VISIBLE);
            } else {
                butMalfunction.setVisibility(View.GONE);
            }
        }
    }

    private List<EventBean> getDiagnosticMalfunctionEvents(int code) {
        List<EventBean> eventList = EventDB.DiagnosticMalFunctionEventGetByCode(code);

        return eventList;
    }

    private void showLoaderAnimation(boolean isShown) {
        if (isShown) {
            rlLoadingPanel.setVisibility(View.VISIBLE);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            rlLoadingPanel.setVisibility(View.GONE);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void login() {
        String userName = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (userName.isEmpty()) {
            Utility.showAlertMsg("Please enter User Name");
        } else if (password.isEmpty()) {
            Utility.showAlertMsg("Please enter Password");
        } else {
            // check user credential
            boolean status = LoginDB.LoginUser(userName, password, coDriver);
            if (status) {
                SharedPreferences prefs = getActivity().getSharedPreferences("HutchGroup", getActivity().MODE_PRIVATE);
                boolean logSent = prefs.getBoolean("logfile_sent", false);
                if (!logSent) {
                    LogFile.sendLogFile(LogFile.AFTER_MID_NIGHT);
                }
                boolean userConfigured = prefs.getBoolean("user_configured", false);

                // get driverId for record
                int driverId = coDriver ? Utility.user2.getAccountId() : Utility.user1.getAccountId();
                // set on screen userid
                Utility.onScreenUserId = driverId;
                Utility.activeUserId = Utility.user2.isActive() ? Utility.user2.getAccountId() : Utility.user1.getAccountId();
                if (userConfigured) {
                    DailyLogDB.DailyLogUserPreferenceRuleSave(driverId, UserPreferences.getCurrentRule(), Utility.getCurrentDateTime(), Utility.getCurrentDateTime());
                    prefs.edit().putBoolean("user_configured", false).commit();
                }

                // we'll have to fetch setting on each time switching user as driver and codriver may have different settings
                getSettings(driverId);
                if (Utility.user1.isOnScreenFg()) {
                    Utility.user1.setOnScreenFg(false);
                    Utility.user2.setOnScreenFg(true);
                } else {
                    Utility.user1.setOnScreenFg(true);
                    Utility.user2.setOnScreenFg(false);
                }

                try {
                    HourOfService.InvokeRule(new Date(), Utility.onScreenUserId);
                    SharedPreferences sp = getActivity().getSharedPreferences("HutchGroup", getActivity().MODE_PRIVATE);
                    int shippingDriverId = sp.getInt("driverid", -1);
                    if (shippingDriverId == Utility.user1.getAccountId() || shippingDriverId == Utility.user2.getAccountId()) {
                        Utility.ShippingNumber = sp.getString("shipping_number", "");
                        Utility.TrailerNumber = sp.getString("trailer_number", "");
                    } else {
                        Utility.ShippingNumber = "";
                        Utility.TrailerNumber = "";
                    }
                } catch (Exception exe) {
                }
                if (firstLogin) {
                    Utility.LastEventDate = EventDB.getLastEventDate(Utility.onScreenUserId);

                    // get engine hour and odometer reading since engine power on.
                    EventDB.getEngineHourOdometerSincePowerOn(driverId);
                    int logId = DailyLogDB.DailyLogCreate(driverId, Utility.ShippingNumber, Utility.TrailerNumber, "");

                    // create event on login
                    EventDB.EventCreate(Utility.getCurrentDateTime(), 5, 1, "Authenticated Driver's ELD login activity", 1, 1, logId, driverId, "");


                    if (coDriver) {
                        // add codriver record
                        DailyLogDB.AddDriver(Utility.user1.getAccountId(), driverId, 0);

                        MessageBean bean = MessageDB.CreateMessage(Utility.IMEI, Utility.user2.getAccountId(), Utility.user2.getAccountId(), "Connect");
                        MessageDB.Send(bean);
                    } else {
                        //connect to server and make driver online
                        if (ChatClient.in == null) {
                            // connect to chat server
                            ChatClient.connect();
                            ChatClient.checkConnection();

                        } else {
                            // make driver online on chat server if server is already connected
                            MessageBean bean = MessageDB.CreateMessage(Utility.IMEI, Utility.user1.getAccountId(), Utility.user1.getAccountId(), "Connect");
                            MessageDB.Send(bean);
                        }

                    }

                }


                int acceptLicense = coDriver ? Utility.user2.getLicenseAcceptFg() : Utility.user1.getLicenseAcceptFg();

                if (acceptLicense == 0) {
                    Intent i = new Intent(getContext(), FirstTimeUser.class);
                    startActivityForResult(i, 1);
                } else {
                    mListener.loginSuccessfully(firstLogin);

                }

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("E-Log");
                builder.setIcon(R.drawable.ic_launcher);
                builder.setMessage(Utility.errorMessage);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.cancel();
                    }
                });
                alertDialog = builder.show();

                TextView messageView = (TextView) alertDialog.findViewById(android.R.id.message);
                messageView.setGravity(Gravity.CENTER);
                Utility.errorMessage = "";
            }
        }
    }

    private void getSettings(int driverId) {

        SettingsBean bean = SettingsDB.getSettings(driverId);
        Utility._appSetting = bean;
    }


    SyncData.PostTaskListener<Boolean> syncDataPostTaskListener = new SyncData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            showLoaderAnimation(false);
        }
    };


    public interface OnFragmentInteractionListener {
        void loginSuccessfully(boolean firstLogin);

        void backFromLogin();

        void updateFlagbar(boolean status);

        void onAlertVehicleStart();

        void onAlertClear();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            mListener.loginSuccessfully(firstLogin);
        } else {
            if (Utility.user1.isOnScreenFg()) {
                Utility.user1.setOnScreenFg(false);
                Utility.user2.setOnScreenFg(true);
            } else {
                Utility.user1.setOnScreenFg(true);
                Utility.user2.setOnScreenFg(false);
            }
            Utility.showAlertMsg("You have to accept terms & conditions to proceed!!!");
        }
    }

    private void rebootDevice() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
            proc.waitFor();
        } catch (Exception exe) {
            Utility.showAlertMsg(exe.getMessage());
        }
    }

}
