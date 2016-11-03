package com.hutchgroup.elog.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hutchgroup.elog.MainActivity;
import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.EventAdapter;
import com.hutchgroup.elog.beans.DailyLogBean;
import com.hutchgroup.elog.beans.DutyStatusBean;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.beans.GPSData;
import com.hutchgroup.elog.beans.RuleBean;
import com.hutchgroup.elog.beans.UserBean;
import com.hutchgroup.elog.beans.ViolationBean;
import com.hutchgroup.elog.bll.HourOfService;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.CustomDateFormat;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;
import com.hutchgroup.elog.db.HourOfServiceDB;
import com.hutchgroup.elog.db.SettingsDB;
import com.hutchgroup.elog.db.UserDB;
import com.hutchgroup.elog.tasks.LogEventSync;
import com.hutchgroup.elog.tasks.PostData;
import com.hutchgroup.elog.tasks.SyncData;

import android.support.v7.app.ActionBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ELogFragment extends Fragment implements View.OnClickListener, RuleChangeDialog.RuleChangeDialogInterface,
        DutyStatusChangeDialog.DutyStatusChangeDialogInterface, InputInformationDialog.InputInformationDialogInterface {
    String TAG = ELogFragment.class.getName();

    public String title = "Daily Log";

    private OnFragmentInteractionListener mListener;

    private ImageView imgDutyStatus, imgViewLandscape, imgViewPortrait;

    //Bitmap bitMapRestore;
    Bitmap bmp;
    Canvas canvas;

    Date currentDate;

    TextView tvOffDutyTime, tvSleeperTime, tvDrivingTime, tvOnDutyTime, tvViolation, tvViolationDate, tvRemaingTime, tvCoDriver;
    TextView tvVehicleMiles, tvTotalDistance, tvCurrentTrip, tvCanadaRule, tvCanadaRuleValue, tvUSRule, tvUSRuleValue;

    Button butDutyStatus;
    Button butRemainingTime;
    ProgressBar pbTimeCountProgress, pbTotalDrivingHours, pbTotalWorkShiftHour, pbTotalCanadaRule, pbTotalUSRule;

    TextView tvTimeZone, tvTotalDrivingHours, tvTotalWorkShiftHours;

    ListView lvEvents;
    EventAdapter eventAdapter;

    int dailyLogId, totalDistance = 0, statusStartOdometerReading;
    public int currentStatus = 1;
    public static Date statusDT, ViolationDT;
    private Handler handlerElog = new Handler();

    public static int currentRule = 1;
    List<String> listRules;
    boolean bDialogShowing;

    DutyStatusChangeDialog dutyDialog;
    RuleChangeDialog ruleChangeDialog;
    public int certifyFg;

    private boolean firstLogin = false;
    InputInformationDialog infosDialog;

    PostData.PostTaskListener<Boolean> postDataPostTaskListener = new PostData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {

            showLoaderAnimation(false);
        }
    };

    SyncData.PostTaskListener<Boolean> syncDataPostTaskListener = new SyncData.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {


            boolean specialCategoryChanged = Utility.specialCategoryChanged();
            if (specialCategoryChanged) {
                //update special category of current user
                int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
                String specialCategory = UserDB.getSpecialCategory(driverId);
                UserBean currentUser = Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;
                if (currentUser != null) {
                    currentUser.setSpecialCategory(specialCategory);
                }
            }

            if (mListener != null) {
                mListener.updateWebserviceIcon(true);
                mListener.updateSpecialCategoryChanged(specialCategoryChanged);
            }
            ELogFragment.this.refresh();
            showLoaderAnimation(false);
        }
    };

    private void updateTitle() {
        try {
            String title = getContext().getResources().getString(R.string.title_daily_log);
            title += " - " + Utility.convertDate(new Date(), CustomDateFormat.d10);
            mListener.setTitle(title);
        } catch (Exception exe) {
        }

    }

    private Runnable runnableStatus = new Runnable() {
        @Override
        public void run() {
            try {
                Date now = Utility.dateOnlyGet(new Date());
                if (!now.equals(currentDate)) {
                    updateTitle();
                    currentDate = Utility.dateOnlyGet(new Date());
                    // create daily log record for the current date
                    dailyLogId = DailyLogDB.DailyLogCreate(Utility.onScreenUserId, Utility.ShippingNumber, Utility.TrailerNumber, "");
                    mListener.resetFlag();
                    ELogFragment.this.refresh();
                }

                if (Utility._appSetting.getGraphLine() == 1) {
                    ELogFragment.this.DutyStatusGet();
                }

                checkToSpeak();
                AutoHoursCalculate();

                handlerElog.postDelayed(this, 60000);
            } catch (Exception e) {
                LogFile.write(ELogFragment.class.getName() + "::runnalbeStatus Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            }
        }
    };

    boolean fabShowing;

    int finalHeight = 0, finalWidth = 0, imageWidth = 0, imageHeight = 0;
    View inforHeader;
    ImageButton fabMenu, fabPost, fabSync, fabChangeRule, fabCertify, fabUncertify, fabActive, fabSwitchUser, fabUndocking;

    FloatingActionButton fabStop;
    LinearLayout layoutUndocking, layout_menu;
    View restView;
    RelativeLayout rlLoadingPanel;


    final Handler odometerHandler = new Handler();
    final Runnable odometerRunnable = new Runnable() {
        @Override
        public void run() {
            updateOdometer();

            odometerHandler.postDelayed(this, 10000);
        }
    };

    public ELogFragment() {
        // Required empty public constructor
        //mInstance = this;
    }

    public static ELogFragment newInstance() {
        ELogFragment fragment = new ELogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        Configuration config = getResources().getConfiguration();
        View view = inflater.inflate(R.layout.fragment_elog, container, false);

        try {
            currentDate = Utility.dateOnlyGet(new Date());
            DailyLogBean dailyLog = DailyLogDB.getDailyLogInfo(Utility.onScreenUserId, Utility.getCurrentDate());
            dailyLogId = dailyLog.get_id();
            certifyFg = dailyLog.getCertifyFG();
            if (dailyLogId == 0) {
                dailyLogId = DailyLogDB.DailyLogCreate(Utility.onScreenUserId, Utility.ShippingNumber, Utility.TrailerNumber, "");
            }

            mListener.setCertify(certifyFg);
            lvEvents = (ListView) view.findViewById(R.id.lvEvent);

            eventAdapter = new EventAdapter(this.getActivity(), getListEvents());

            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                inforHeader = inflater.inflate(R.layout.elog_scrollview, null, false);
            } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                inforHeader = inflater.inflate(R.layout.elog_scrollview_portrait, null, false);
            }

            lvEvents.addHeaderView(inforHeader);
            lvEvents.setAdapter(eventAdapter);

            initializeControls(view);

            imgViewLandscape = null;
            imgViewPortrait = null;
            imgDutyStatus = null;
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                imgViewLandscape = (ImageView) view.findViewById(R.id.imgDutyStatus);
                imgDutyStatus = imgViewLandscape;
            } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                imgViewPortrait = (ImageView) view.findViewById(R.id.imgDutyStatus_portrait);
                imgDutyStatus = imgViewPortrait;
            }

            ViewTreeObserver vto = imgDutyStatus.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    imgDutyStatus.getViewTreeObserver().removeOnPreDrawListener(this);
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    finalHeight = displayMetrics.heightPixels;
                    finalWidth = displayMetrics.widthPixels;

                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    float density = displayMetrics.density;
                    finalWidth = Math.round(displayMetrics.widthPixels / density);
                    finalHeight = Math.round(displayMetrics.heightPixels / density);

                    return false;
                }
            });

            Thread thBitMap = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException exe) {
                            break;
                        }

                        if (finalHeight != 0 && finalWidth != 0) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initializeBitmap(true);
                                }
                            });

                            break;
                        }
                    }
                }
            });
            thBitMap.setName("Elog-Bitmap1");
            thBitMap.start();


            if (firstLogin) {
                firstLogin = false;
                if (infosDialog == null) {
                    infosDialog = new InputInformationDialog();
                }

                if (!infosDialog.isVisible()) {
                    infosDialog.mListener = this;

                    infosDialog.show(getFragmentManager(), "shippingtrailers_dialog");
                }

                if (Utility.isInternetOn()) {

                    new LogEventSync(refreshElogFragment).execute(Utility.LastEventDate);

                }
            }
            updateTitle();

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::onCreateView Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }

        return view;
    }

    @Override
    public void onInputFinished() {
        Log.d("Input", "fragment onInputFinished");
        infosDialog.mListener = null;
    }

    @Override
    public void onInputSaved(String shippId, String trailerId) {
        Log.d("Input", "fragment onInputSaved");
        infosDialog.mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            Log.d(TAG, "fragment onConfigurationChanged");
            //lvEvents.removeHeaderView(inforHeader);
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.fragment_elog, null);

            ViewGroup viewGroup = (ViewGroup) getView();

            viewGroup.removeAllViews();
            viewGroup.addView(view);


            lvEvents = (ListView) view.findViewById(R.id.lvEvent);

            Log.d(TAG, "onCreateView");
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                inforHeader = inflater.inflate(R.layout.elog_scrollview, null, false);
                //lvEvents.addHeaderView(header);
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                inforHeader = inflater.inflate(R.layout.elog_scrollview_portrait, null, false);
                //lvEvents.addHeaderView(header);
            }
            lvEvents.addHeaderView(inforHeader);

            initializeControls(view);
            //eventAdapter = new EventAdapter(this.getActivity(), getListEvents());
            lvEvents.setAdapter(eventAdapter);

            imgViewLandscape = null;
            imgViewPortrait = null;
            imgDutyStatus = null;
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                imgViewLandscape = (ImageView) view.findViewById(R.id.imgDutyStatus);
                imgDutyStatus = imgViewLandscape;
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                imgViewPortrait = (ImageView) view.findViewById(R.id.imgDutyStatus_portrait);
                imgDutyStatus = imgViewPortrait;
            }
            ViewTreeObserver vto = imgDutyStatus.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    imgDutyStatus.getViewTreeObserver().removeOnPreDrawListener(this);
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    finalHeight = displayMetrics.heightPixels;
                    finalWidth = displayMetrics.widthPixels;

                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    float density = displayMetrics.density;
                    finalWidth = Math.round(displayMetrics.widthPixels / density);
                    finalHeight = Math.round(displayMetrics.heightPixels / density);
                    //Log.e(TAG, "Height = " + finalHeight + " - Width = " + finalWidth);
                    return false;
                }
            });

            Thread thBitMap = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (finalHeight != 0 && finalWidth != 0) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initializeBitmap(true);
                                }
                            });

                            break;
                        }
                    }
                }
            });
            thBitMap.setName("Elog-Bitmap2");
            thBitMap.start();

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::onConfigurationChanged Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;

            odometerHandler.postDelayed(odometerRunnable, 500);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {

            mListener = null;
            handlerElog.removeCallbacksAndMessages(null);
            odometerHandler.removeCallbacksAndMessages(null);
            //odometerHandler.removeCallbacks(odometerRunnable);
            finalHeight = 0;
            finalWidth = 0;
            canvas = null;
            HourOfService.listDutyStatus.clear();
            HourOfService.violations.clear();
            Utility.dutyStatusList.clear();
            infosDialog = null;
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::onDetach Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void resetFlag();

        void setDutyStatus(int status);

        void setActiveDutyStatus(int status);

        void setTotalDistance(int total);

        void setStartOdoMeter(int odo);

        void setStartEngineHour(int value);

        void resetInspectionIcon();

        void setCertify(int certifyFg);

        void updateWebserviceIcon(boolean active);

        void updateSpecialCategoryChanged(boolean value);

        void activeUser();

        void changeUser();

        void undocking();

        void setTitle(String title);
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.tvCurrentRuleLabel:
                case R.id.butChangeRule:
                    //launch the ativity Rule Change
                    launchRuleChange();
                    break;
                case R.id.butRemainingTime:
                case R.id.tvRemaingTime:
                case R.id.butDutyStatus:
                    //lauch the activity Duty Status Change
                    launchDutyStatusChange(false);
                    break;
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::onClick Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }

    }

    private void initializeBitmap(final boolean addRunnable) {

        try {
            Utility.dutyStatusList = HourOfServiceDB.DutyStatusGet15Days(currentDate, Utility.onScreenUserId + "", false);

            if (bmp != null) {
                bmp.recycle();

            }
            if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
                imageWidth = 289;//finalWidth - 136;
                imageHeight = 82;
            } else {
                if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
                    imageWidth = finalWidth - 208;
                    imageHeight = 257;
                } else if (Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation) {
                    imageWidth = 480;//finalWidth - 136;
                    imageHeight = 212;
                }
            }

            bmp = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);

            canvas = new Canvas(bmp);

            initialize(addRunnable);
        } catch (Exception exe) {
            LogFile.write(ELogFragment.class.getName() + "::initializeBitmap Error:" + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }

    }

    private void initializeControls(View view) {
        try {
            tvOffDutyTime = (TextView) view.findViewById(R.id.tvOffDutyTime);
            tvSleeperTime = (TextView) view.findViewById(R.id.tvSleeperTime);
            tvDrivingTime = (TextView) view.findViewById(R.id.tvDrivingTime);
            tvOnDutyTime = (TextView) view.findViewById(R.id.tvOnDutyTime);
            tvViolation = (TextView) view.findViewById(R.id.tvViolation);
            tvViolation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (currentStatus != 3 || ViolationDT == null) {
                            return;
                        }

                        final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
                        alertDialog.setCancelable(true);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setTitle(ViolationTitle);
                        alertDialog.setIcon(Utility.DIALOGBOX_ICON);
                        alertDialog.setMessage(ViolationExplanation);
                        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertDialog.cancel();
                                    }
                                });
                        alertDialog.show();
                    } catch (Exception ex) {
                        LogFile.write("onViolationClick Alert Msg: " + ex.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                    }
                }
            });
            tvViolation.setSelected(true);
            tvViolationDate = (TextView) view.findViewById(R.id.tvViolationDate);

            tvRemaingTime = (TextView) view.findViewById(R.id.tvRemaingTime);
            tvRemaingTime.setOnClickListener(this);

            tvTimeZone = (TextView) view.findViewById(R.id.tvTimeZoneValue);
            tvVehicleMiles = (TextView) view.findViewById(R.id.tvVehicleMilesValue);

            tvCurrentTrip = (TextView) view.findViewById(R.id.tvCurrentTrip);
            tvTotalDistance = (TextView) view.findViewById(R.id.tvTotalDistanceValue);


            tvCanadaRule = (TextView) view.findViewById(R.id.tvCanadaRule);
            tvCanadaRuleValue = (TextView) view.findViewById(R.id.tvCanadaRuleValue);
            tvUSRule = (TextView) view.findViewById(R.id.tvUSRule);
            tvUSRuleValue = (TextView) view.findViewById(R.id.tvUSRuleValue);

            tvCoDriver = (TextView) view.findViewById(R.id.tvCoDriverValue);

            butDutyStatus = (Button) view.findViewById(R.id.butDutyStatus);
            butDutyStatus.setOnClickListener(this);

            butRemainingTime = (Button) view.findViewById(R.id.butRemainingTime);
            butRemainingTime.setOnClickListener(this);

            pbTimeCountProgress = (ProgressBar) view.findViewById(R.id.whiteProgressBar);
            pbTotalDrivingHours = (ProgressBar) view.findViewById(R.id.redProgressBar);
            pbTotalWorkShiftHour = (ProgressBar) view.findViewById(R.id.blueProgressBar);
            pbTotalCanadaRule = (ProgressBar) view.findViewById(R.id.greenProgressBar);
            pbTotalUSRule = (ProgressBar) view.findViewById(R.id.yellowProgressBar);

            tvTotalDrivingHours = (TextView) view.findViewById(R.id.tvDrivingHoursValue);
            tvTotalWorkShiftHours = (TextView) view.findViewById(R.id.tvWorkShiftValue);


            tvTimeZone.setText(Utility.TimeZoneOffsetUTC);

            getRules();
            currentRule = DailyLogDB.getCurrentRule(Utility.onScreenUserId);

            pbTotalCanadaRule.setMax(currentRule == 2 ? 120 * 60 : 70 * 60);
            pbTotalUSRule.setMax(currentRule == 4 ? 60 * 60 : 70 * 60);

            //tvCurrentRuleLabel.setText(getRule(currentRule - 1));
            if (currentRule == 1 || currentRule == 2) //Canada rule
            {
                pbTimeCountProgress.setMax(13 * 60);
                pbTotalDrivingHours.setMax(13 * 60);
                pbTotalWorkShiftHour.setMax(16 * 60);
                tvCanadaRule.setText(getRule(currentRule - 1));
            } else {
                pbTimeCountProgress.setMax(11 * 60);
                pbTotalDrivingHours.setMax(11 * 60);
                pbTotalWorkShiftHour.setMax(14 * 60);
                tvUSRule.setText(getRule(currentRule - 1));
                tvCanadaRule.setText(getRule(0)); //default canada 70 hour rule label if current rule is US
            }

            //update co-driver name if existed
            if (Utility.user1.isOnScreenFg()) {
                if (Utility.user2.getAccountId() > 0) {
                    tvCoDriver.setText(Utility.user2.getFirstName() + " " + Utility.user2.getLastName());
                }
            } else {
                tvCoDriver.setText(Utility.user1.getFirstName() + " " + Utility.user1.getLastName());
            }

            rlLoadingPanel = (RelativeLayout) view.findViewById(R.id.loadingPanel);
            fabPost = (ImageButton) getActivity().findViewById(R.id.fab_post);
            fabSync = (ImageButton) getActivity().findViewById(R.id.fab_sync);
            fabChangeRule = (ImageButton) getActivity().findViewById(R.id.fab_change_rule);
            fabCertify = (ImageButton) getActivity().findViewById(R.id.fab_certify);
            fabCertify.setEnabled(false);
            fabUncertify = (ImageButton) getActivity().findViewById(R.id.fab_uncertify);
            fabActive = (ImageButton) getActivity().findViewById(R.id.fab_active);
            fabSwitchUser = (ImageButton) getActivity().findViewById(R.id.fab_switch_user);

            fabStop = (FloatingActionButton) getActivity().findViewById(R.id.fab_stop);
            restView = getActivity().findViewById(R.id.restView);
            layout_menu = (LinearLayout) getActivity().findViewById(R.id.layout_floating_menu);

            layoutUndocking = (LinearLayout) getActivity().findViewById(R.id.layout_fab_undocking);

            fabMenu = (ImageButton) view.findViewById(R.id.fab);
            if (Utility.user1.getAccountType() == 2) {
                fabMenu.setVisibility(View.GONE);
            }

            fabMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Animation show_fab_menu = AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.fade_in);
                    Animation hide_fab_menu = AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.fade_out);

                    if (layout_menu.getVisibility() == View.INVISIBLE) {
                        android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 0, 45).start();
                        layout_menu.startAnimation(show_fab_menu);
                        layout_menu.setVisibility(View.VISIBLE);
                        restView.setVisibility(View.VISIBLE);
                        if (certifyFg == 1) {
                            fabCertify.setVisibility(View.VISIBLE);
                            fabUncertify.setVisibility(View.GONE);
                        } else {
                            fabCertify.setVisibility(View.GONE);
                            fabUncertify.setVisibility(View.VISIBLE);
                        }
                        fabShowing = true;
                    } else {
                        android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                        layout_menu.startAnimation(hide_fab_menu);
                        layout_menu.setVisibility(View.INVISIBLE);
                        restView.setVisibility(View.INVISIBLE);
                        fabShowing = false;
                    }

                }
            });

            restView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation hide_fab_menu = AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.fade_out);

                    if (layout_menu.getVisibility() == View.VISIBLE) {
                        android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                        layout_menu.startAnimation(hide_fab_menu);
                        layout_menu.setVisibility(View.INVISIBLE);
                        restView.setVisibility(View.INVISIBLE);
                        fabShowing = false;
                    }
                }
            });

            fabPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d(TAG, "Post");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    layout_menu.setVisibility(View.INVISIBLE);
                    restView.setVisibility(View.INVISIBLE);
                    callPost();
                }
            });

            fabSync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Sync");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    layout_menu.setVisibility(View.INVISIBLE);
                    restView.setVisibility(View.INVISIBLE);
                    callSync();
                }
            });

            fabChangeRule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Change Rule");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    layout_menu.setVisibility(View.INVISIBLE);
                    restView.setVisibility(View.INVISIBLE);
                    callChangeRule();
                }
            });

            fabUncertify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Certify");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    layout_menu.setVisibility(View.INVISIBLE);
                    restView.setVisibility(View.INVISIBLE);
                    callCertify();
                }
            });

            fabActive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Active");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    layout_menu.setVisibility(View.INVISIBLE);
                    restView.setVisibility(View.INVISIBLE);
                    if (MainActivity.activeCurrentDutyStatus == 3 || MainActivity.activeCurrentDutyStatus == 5 || MainActivity.activeCurrentDutyStatus == 6) {
                        Utility.showAlertMsg("Co driver must change duty status to Off ,Sleeper or On-duty!");
                        return;
                    }
                    callActive();
                }
            });

            fabActive.setEnabled(Utility.onScreenUserId != Utility.activeUserId);

            fabSwitchUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Switch User");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    layout_menu.setVisibility(View.INVISIBLE);
                    restView.setVisibility(View.INVISIBLE);

                    SwitchDriver();
                }
            });

           /* fabStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "STOP");
                    android.animation.ObjectAnimator.ofFloat(fabMenu, "rotation", 45, 0).start();
                    CanMessages.Speed = "0";
                }
            });*/

            fabUndocking = (ImageButton) getActivity().findViewById(R.id.fab_undocking);
            fabUndocking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callUndocking();
                }
            });

            currentDate = Utility.dateOnlyGet(new Date());

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::initializeControls Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void initialize(boolean addRunnable) {
        try {

            statusDT = currentDate;

            Log.d(TAG, "Utility dutyStatusList size=" + Utility.dutyStatusList.size());
            if (Utility.dutyStatusList.size() > 0) {
                try {
                    currentStatus = Utility.dutyStatusList.get(0).getStatus();

                    if (currentStatus == 1 && Utility.statusFlag == 1) {
                        if (Utility.dutyStatusList.get(0).getPersonalUse() == 1) {
                            currentStatus = 5;
                        }
                    }

                    Log.d(TAG, "reset at initialize");
                    mListener.resetFlag();
                    mListener.setDutyStatus(currentStatus);
                    Log.d(TAG, "initialize currentStatus=" + currentStatus);
                    butDutyStatus.setText(getResources().getStringArray(R.array.duty_status)[currentStatus - 1]);
                    statusDT = Utility.sdf.parse(Utility.dutyStatusList.get(0).getStartTime());
                    String startTime = Utility.timeOnlyGet(Utility.dutyStatusList.get(0).getStartTime());
                    //tvStartTime.setText("from: " + startTime);
                } catch (Exception exe) {
                    Log.d(TAG, "initialize exe error=" + exe.getMessage());
                    //throw exe;
                }
            } else {
                Log.d(TAG, "dutyStatusList size is 0");
                currentStatus = 1;
                Log.d(TAG, "reset at initialize");
                mListener.resetFlag();
                mListener.setDutyStatus(currentStatus);
                Log.d(TAG, "initialize currentStatus=" + currentStatus);
                butDutyStatus.setText(getResources().getStringArray(R.array.duty_status)[currentStatus - 1]);
            }

            DutyStatusGet();
            if (currentStatus == 3) {
                InvokeRule();

            }

            AutoHoursCalculate();
            if (addRunnable) {
                try {
                    handlerElog.removeCallbacksAndMessages(null);
                    handlerElog.postDelayed(runnableStatus, 60000);
                } catch (Exception exe) {

                }
            }
        } catch (Exception e) {
            Log.d(TAG, "initialize error=" + e.getMessage());
            LogFile.write(ELogFragment.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void DutyStatusGet() {
        try {
            ArrayList<DutyStatusBean> dutyStatus = this.DutyStatusGet(currentDate, Utility.dutyStatusList);

            drawLine(dutyStatus, currentDate);
            StatusHourGet(dutyStatus);

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::DutyStatusGet Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    //Created By: Deepak Sharma
    //Created Date: 5/27/2016
    //Purpose: get rule when event changed
    private RuleBean eventRuleGet(Date eventTime, ArrayList<RuleBean> ruleList) {
        RuleBean obj = new RuleBean();
        obj.setRuleId(1);
        obj.setRuleStartTime(currentDate);
        obj.setRuleEndTime(Utility.addDays(currentDate, 1));
        for (RuleBean bean : ruleList) {
            obj.setRuleEndTime(bean.getRuleStartTime());
            if (bean.getRuleStartTime().before(eventTime) || bean.getRuleStartTime().equals(eventTime)) {
                return bean;
            }
        }
        return obj;
    }

    //Created By: Deepak Sharma
    //Created Date: 5/27/2016
    //Purpose: get rule when event changed
    private ArrayList<RuleBean> eventRuleListGet(Date eventStartTime, Date eventEndTime, ArrayList<RuleBean> ruleList) {
        ArrayList<RuleBean> obj = new ArrayList<>();

        for (RuleBean bean : ruleList) {
            if ((bean.getRuleStartTime().after(eventStartTime) || bean.getRuleStartTime().equals(eventStartTime)) && bean.getRuleStartTime().before(eventEndTime)) {
                obj.add(bean);
            }
        }
        Collections.sort(obj, RuleBean.dateAsc);
        return obj;
    }

    private void drawViolationArea() {
        HourOfService.ViolationCalculation(new Date(), Utility.onScreenUserId);
        ArrayList<ViolationBean> vList = HourOfService.violations;
        int startMinutes = 0;
        Date endTime = null, startTime = null;
        for (int i = 0; i < vList.size(); i++) {

            startTime = vList.get(i).getStartTime();
            startMinutes = (int) (startTime.getTime() - currentDate.getTime()) / (1000 * 60);
            if (endTime != null && startTime.before(endTime)) {
                continue;
            }
            endTime = Utility.addMinutes(startTime, vList.get(i).getTotalMinutes());
            if (Utility._appSetting.getGraphLine() == 1 && endTime.after(new Date())) {
                if (startTime.after(new Date()))
                    break;
                endTime = new Date();
            }

            int endMinutes = (int) (endTime.getTime() - currentDate.getTime()) / (1000 * 60);
            drawRect(getX(startMinutes), getRectY(1), getX(endMinutes), getRectY(4));

        }

    }

    private void drawLine(ArrayList<DutyStatusBean> dutyStatus, Date logDate) {
        //clear bitmap bmp 333
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
        try {
            if (Utility._appSetting.getViolationOnGrid() == 1)
                drawViolationArea();

            String logDT = Utility.sdf.format(logDate);
            ArrayList<RuleBean> ruleList = DailyLogDB.getRuleByDate(logDT, Utility.onScreenUserId, dailyLogId);
            Collections.sort(ruleList, RuleBean.dateDesc);
            int ruleId = 1, startMinutes, endMinutes = 0;

            for (int i = 0; i < dutyStatus.size(); i++) {
                DutyStatusBean item = dutyStatus.get(i);
                int status = item.getStatus();
                int personalUseFg = item.getPersonalUse();
                Date startTime = Utility.sdf.parse(item.getStartTime()), endTime = Utility.sdf.parse(item.getEndTime());

                // graph line upto current time
                if (Utility._appSetting.getGraphLine() == 1 && i == dutyStatus.size() - 1) {
                    endTime = new Date();
                }

                RuleBean rule = eventRuleGet(startTime, ruleList);
                ruleId = rule.getRuleId();
                if (rule.getRuleEndTime().before(endTime)) {

                    startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                    endMinutes = (int) (rule.getRuleEndTime().getTime() - logDate.getTime()) / (1000 * 60);
                    if (i == 0 && startMinutes > 0) {
                        startMinutes = 0;
                    }
                    drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId, personalUseFg);

                    //Log.d(TAG, getX(startMinutes) +"/"+ getY(status)+"/"+ getX(endMinutes)+"/"+ getY(status) + "--" + ruleId + "|status=" + status);
                    startTime = rule.getRuleEndTime();
                    ArrayList<RuleBean> ruleEventList = eventRuleListGet(startTime, endTime, ruleList);
                    for (RuleBean ruleBean : ruleEventList) {
                        if (ruleBean.getRuleStartTime().equals(rule.getRuleStartTime()))
                            continue;
                        startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                        endMinutes = (int) ((ruleBean.getRuleEndTime().before(endTime) ? ruleBean.getRuleEndTime() : endTime).getTime() - logDate.getTime()) / (1000 * 60);
                        ruleId = ruleBean.getRuleId();
                        drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId, personalUseFg);

                        startTime = ruleBean.getRuleEndTime();
                    }
                } else {

                    startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                    endMinutes = (int) (endTime.getTime() - logDate.getTime()) / (1000 * 60);
                    if (i == 0 && startMinutes > 0) {
                        startMinutes = 0;
                    }

                    drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId, personalUseFg);
                }
                if (i < dutyStatus.size() - 1) {
                    item = dutyStatus.get(i + 1);
                    drawLine(getX(endMinutes), getY(status), getX(endMinutes), getY(item.getStatus()), ruleId, item.getPersonalUse());
                }
            }

            if (dutyStatus.size() == 0) {
                if (ruleList.size() > 0) {
                    ruleId = ruleList.get(0).getRuleId();

                }
                // Date endTime = Utility.sdf.parse( Utility.getCurrentDateTime());
                //Date endTime = Utility.addSeconds(Utility.addDays(logDate, 1), -1);
                endMinutes = 1439;// (int) (endTime.getTime() - logDate.getTime()) / (1000 * 60);
                drawLine(getX(0), getY(1), getX(endMinutes), getY(1), ruleId, 0);
            }

            if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
                if (imgViewLandscape != null) {
                    imgViewLandscape.setImageDrawable(null);
                    imgViewLandscape.setImageBitmap(bmp);
                }
            } else if (Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation) {
                if (imgViewPortrait != null) {
                    imgViewPortrait.setImageDrawable(null);
                    imgViewPortrait.setImageBitmap(bmp);
                }
            }
        } catch (Exception exe) {
            Log.d(TAG, "drawLine got exception");
            LogFile.write(ELogFragment.class.getName() + "::drawLine2 Error:" + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    DashPathEffect effects = new DashPathEffect(new float[]{4, 2, 4, 2}, 0);

    private void drawLine(float x, float y, float xend, float yend, int ruleId, int personalUseFg) {
        try {
            Paint p = new Paint();
            int color;
            if (ruleId <= 2) {
                color = Utility._appSetting.getColorLineCanada();
            } else {
                color = Utility._appSetting.getColorLineUS();
            }
            color = color == 0 ? Color.BLUE : color;
            p.setColor(color);
            int width = 3;
            if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
                width = 1;
            }
            p.setStrokeWidth(width);
          /*  if (personalUseFg == 1) {
                p.setPathEffect(effects);
            }*/
            canvas.drawLine(x, y, xend, yend, p);
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::drawLine1 Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }


    private void drawRect(float x, float y, float xend, float yend) {
        try {
            Paint p = new Paint();

            p.setColor(getResources().getColor(R.color.red15));
            //p.setStrokeWidth(1);
            canvas.drawRect(x, y, xend, yend, p);
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::drawRect Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public void launchRuleChange() {
        try {
            if (ruleChangeDialog == null) {
                ruleChangeDialog = new RuleChangeDialog();
                ruleChangeDialog.mListener = this;
            }
            ruleChangeDialog.setCurrentRule(currentRule);
            ruleChangeDialog.show(getFragmentManager(), "rulechange_dialog");
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::launchRuleChange Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public void launchDutyStatusChange(boolean fromPopupDialog) {
        try {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                for (int i = 0; i < getFragmentManager().getBackStackEntryCount(); i++)
                    getFragmentManager().popBackStackImmediate();
            }
            if (dutyDialog == null) {
                Log.d(TAG, "Create duty dialog");
                dutyDialog = new DutyStatusChangeDialog();
            } else {
                dutyDialog.clear();
            }

            if (dutyDialog.isAdded()) {
                dutyDialog.dismiss();
            }
            dutyDialog.mListener = this;
            if (fromPopupDialog) {
                dutyDialog.setCurrentStatus(3);
                dutyDialog.setActiveDriver(true);
            } else {
                dutyDialog.setCurrentStatus(currentStatus);
                dutyDialog.setActiveDriver(false);
            }
            bDialogShowing = true;

            dutyDialog.show(getFragmentManager(), "dutystatus_dialog");
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::launchDutyStatusChange Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private int getX(int minutes) {
        float boxWidth = (float) imageWidth;//(float) imgDutyStatus.getWidth();
        //return (int) ((float) Math.ceil(minutes / boxWidth) * boxWidth - boxWidth);
        int x = (int) ((float) boxWidth / 1440 * minutes);
        return x;
    }

    private int getY(int status) {
        float boxHeight = (float) (imageHeight - 64.0) / 4;
        if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
            boxHeight = (float) (imageHeight - 22.0) / 4;
        }

        int y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 64.0);

        if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
            y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 22.0);
        } else {
            if ((Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation)) {
                boxHeight = (float) (imageHeight - 52.0) / 4;
                y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 52.0);
            } else {
                y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 64.0);
            }
        }

        return y;
    }

    private int getRectY(int status) {
        float boxHeight = (float) (imageHeight - 64.0) / 4;
        if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
            boxHeight = (float) (imageHeight - 22.0) / 4;
        }

        //int y = (int) ((status - 1) * boxHeight + boxHeight / 2 + 64.0);
        int y = 0;
        if (!Utility.isLargeScreen(getContext().getApplicationContext())) {
            y = (int) (status * boxHeight + 22.0);
            if (status == 1) {
                y = 22;
            }
        } else {
            if ((Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation)) {
                boxHeight = (float) (imageHeight - 52.0) / 4;

                y = (int) (status * boxHeight + 52.0);
                if (status == 1) {
                    y = 52;
                }

            } else {
                y = (int) (status * boxHeight + 64.0);
                if (status == 1) {
                    y = 64;
                }
            }
        }

        return y;
    }

    private void InvokeRule() {
        try {
            HourOfService.InvokeRule(new Date(), Utility.onScreenUserId);
            //AutoHoursCalculate();
        } catch (Exception exe) {
            //Utility.printError(exe.getMessage());
            LogFile.write(ELogFragment.class.getName() + "::InvokeRule Error:" + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void getRules() {
        try {
            listRules = new ArrayList<String>();
            listRules.add(getResources().getString(R.string.canada_rule_1));
            listRules.add(getResources().getString(R.string.canada_rule_2));
            listRules.add(getResources().getString(R.string.us_rule_1));
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::getRules Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private String getRule(int ruleIdx) {
        if (listRules == null)
            return "";
        if (ruleIdx < 0 || ruleIdx >= listRules.size())
            return "";
        return listRules.get(ruleIdx);
    }

    private void StatusHourGet(ArrayList<DutyStatusBean> dutyStatus) {
        try {
            int offDuty = 0, sleeper = 0, driving = 0, onDuty = 0;
            for (DutyStatusBean bean : dutyStatus) {
                if (Utility._appSetting.getGraphLine() == 1) {
                    Date endTime = Utility.sdf.parse(bean.getEndTime());
                    if (endTime.after(new Date())) {
                        Date startTime = Utility.sdf.parse(bean.getStartTime());
                        int totalHours = (int) Math.round(((new Date()).getTime() - startTime.getTime()) / (1000 * 60.0));
                        bean.setTotalMinutes(totalHours);
                    }
                }
                switch (bean.getStatus()) {
                    case 1:
                        offDuty += bean.getTotalMinutes();
                        break;
                    case 2:
                        sleeper += bean.getTotalMinutes();
                        break;
                    case 3:
                        driving += bean.getTotalMinutes();
                        break;
                    case 4:
                        onDuty += bean.getTotalMinutes();
                        break;
                }
            }
            tvOffDutyTime.setText(Utility.getTimeFromMinute(offDuty));
            tvSleeperTime.setText(Utility.getTimeFromMinute(sleeper));
            tvDrivingTime.setText(Utility.getTimeFromMinute(driving));
            tvOnDutyTime.setText(Utility.getTimeFromMinute(onDuty));
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::StatusHourGet Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private List<EventBean> getListEvents() {
        List<EventBean> dutyStatusList = EventDB.DutyStatusChangedEventGetByLogId(Utility.onScreenUserId, dailyLogId);

        TotalDistanceGet();
        return AddViolationToEvents(dutyStatusList);
    }

    // Deepak Sharma
    // 3 June 2016
    // add violation to events
    private List<EventBean> AddViolationToEvents(List<EventBean> eventList) {
        if (Utility._appSetting.getShowViolation() == 0)
            return eventList;
        HourOfService.ViolationCalculation(new Date(), Utility.onScreenUserId);
        ArrayList<ViolationBean> vList = HourOfService.violations;
        Date currTime = new Date();
        for (int i = 0; i < vList.size(); i++) {

            Date startTime = vList.get(i).getStartTime();
            int totalMinutes = vList.get(i).getTotalMinutes();
            // Date endTime = Utility.addMinutes(startTime, totalMinutes);

            Date vDate = Utility.dateOnlyGet(startTime);
            boolean isCurrent = vDate.equals(currentDate);
            if (!isCurrent)
                continue;

           /* if (startTime.after(currTime) || endTime.before(currentDate))
                continue;
*/
            EventBean event = new EventBean();
            event.setEventType(-1); //-1 for violation
            event.setEventDateTime(Utility.sdf.format(startTime));
            event.setViolation(vList.get(i).getRule());
            event.setViolationTitle(vList.get(i).getTitle());
            event.setViolationExplanation(vList.get(i).getExplanation());
            event.setViolationMintes(Utility.getTimeFromMinute(totalMinutes));
            eventList.add(event);
        }
        Collections.sort(eventList, EventBean.dateDesc);
        return eventList;
    }

    //Created By: Deepak Sharma
    //Created Date: 12 March 2016
    //Purpose: Total distance of driver of day
    private void TotalDistanceGet() {
        try {
            List<EventBean> dutyStatusList = EventDB.TotalDistanceGetByLogId(dailyLogId);
            totalDistance = 0;
            statusStartOdometerReading = 0;
            int engineHour = 0;
            for (int i = 0; i < dutyStatusList.size(); i++) {
                EventBean bean = dutyStatusList.get(i);

                if (bean.getEventCode() == 3) {
                    if (statusStartOdometerReading == 0)
                        statusStartOdometerReading = Double.valueOf(bean.getOdometerReading()).intValue();
                } else {
                    if (statusStartOdometerReading > 0)
                        totalDistance += (Double.valueOf(bean.getOdometerReading()).intValue() - statusStartOdometerReading);
                    statusStartOdometerReading = 0;
                }
                engineHour = Double.valueOf(bean.getEngineHour()).intValue();
            }
            //send totalDistance, startOdometer to MainActivity
            mListener.setStartOdoMeter(statusStartOdometerReading);
            mListener.setTotalDistance(totalDistance);
            mListener.setStartEngineHour(engineHour);
            updateOdometer();
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::TotalDistanceGet Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public static ArrayList<DutyStatusBean> DutyStatusGet(Date date, ArrayList<DutyStatusBean> list) {

        ArrayList<DutyStatusBean> data = new ArrayList<>();
        try {
            Date nextDay = Utility.addDays(date, 1);

            for (int i = 0; i < list.size(); i++) {
                Date startDate = Utility.sdf.parse(list.get(i).getStartTime());
                Date endDate = Utility.sdf.parse(list.get(i).getEndTime());
                //Log.d(TAG, "Duty Status= " + list.get(i).getStatus() + " - Start Time=" + list.get(i).getStartTime() + " / End Time=" + list.get(i).getEndTime());
                int status = list.get(i).getStatus();
                int personalUseFg = list.get(i).getPersonalUse();
                startDate = startDate.before(date) ? date : startDate;
                endDate = endDate.after(nextDay) ? nextDay : endDate;
                if ((startDate.after(date) || endDate.after(date)) && startDate.before(nextDay)) {
                    int totalMinutes = (int) Math.round((endDate.getTime() - startDate.getTime()) / (1000 * 60.0));

                    DutyStatusBean bean = new DutyStatusBean();
                    bean.setStartTime(Utility.sdf.format(startDate));
                    bean.setEndTime(Utility.sdf.format(endDate));
                    bean.setStatus(status);
                    bean.setTotalMinutes(totalMinutes);
                    bean.setPersonalUse(personalUseFg);
                    data.add(bean);
                }
            }

        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::DutyStatusGet Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
        Collections.sort(data, DutyStatusBean.dateAsc);
        return data;
    }

    public static String ViolationDescription, ViolationTitle, ViolationExplanation;

    private void checkToSpeak() {
        try {
            if (currentStatus == 3) {
                if (ViolationDT == null) {
                    return;
                }
                int hourLeft = (int) (ViolationDT.getTime() - (new Date()).getTime()) / (1000 * 60);
                if (hourLeft < 0) {
                    hourLeft = 0;
                }

                if (Utility._appSetting.getViolationReading() == 1) {
                    if (hourLeft == 0) {
                        //Speech
                        MainActivity.textToSpeech.speak("Please stop Driving, Your driving hours have expired.", TextToSpeech.QUEUE_ADD, null);
                    } else if (hourLeft <= 60) {
                        if (hourLeft % 5 == 0) {
                            MainActivity.textToSpeech.speak("Caution Your driving hours are about to expired. You have " + hourLeft + " minutes of driving time remaining.", TextToSpeech.QUEUE_ADD, null);

                        }
                    }
                }
            }
        } catch (Exception exe) {
            LogFile.write(ELogFragment.class.getName() + "::checkToSpeak Error:" + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    //Created By: Deepak Sharma
    //Created Date: 7 March 2016
    //Purpose: calculate next violation and remaining hours
    private void AutoHoursCalculate() {
        try {

            //******************************* for testing purpose************************************
          /*  if (Utility.motionFg)
                CanMessages.OdometerReading = (Integer.parseInt(CanMessages.OdometerReading) + 1) + "";
            CanMessages.EngineHours = (Double.valueOf(CanMessages.EngineHours) + .1) + "";
            updateOdometer();*/
            //********************************End*****************************************************

            if (currentStatus == 3) {

                if (ViolationDT == null) {
                    GPSData.NoHOSViolationFgFg = 1;
                    tvViolation.setText("N/A");
                    tvViolationDate.setText("");
                    AutoViolationCalculate();
                    return;
                }
                int hourLeft = (int) (ViolationDT.getTime() - (new Date()).getTime()) / (1000 * 60);
                if (hourLeft < 0) {
                    hourLeft = 0;
                }

//                if (Utility._appSetting.getViolationReading() == 1) {
//                    if (hourLeft == 0) {
//                        //Speech
//                        MainActivity.textToSpeech.speak("Please stop Driving, Your driving hours has been over.", TextToSpeech.QUEUE_ADD, null);
//                    } else if (hourLeft <= 60) {
//                        if (hourLeft % 5 == 0) {
//                            MainActivity.textToSpeech.speak("Your driving hours are about to over. You have left " + hourLeft + " minutes of driving.", TextToSpeech.QUEUE_ADD, null);
//
//                        }
//                    }
//                }

                pbTimeCountProgress.setProgress(pbTimeCountProgress.getMax() - hourLeft);
                tvViolation.setText((hourLeft == 0 ? " (Stop Driving)" : "") + ViolationDescription);
                tvViolationDate.setText(Utility.ConverDateFormat(ViolationDT));
                tvRemaingTime.setText(Utility.getTimeFromMinute(hourLeft));

                tvTotalDrivingHours.setText(Utility.getTimeFromMinute(hourLeft));
                pbTotalDrivingHours.setProgress(pbTotalDrivingHours.getMax() - hourLeft);
                GPSData.DrivingTimeRemaining = hourLeft;
                GPSData.NoHOSViolationFgFg = hourLeft > 0 ? 1 : 0;

            } else {
                GPSData.NoHOSViolationFgFg = 1;
                // String dutyStatus = currentStatus == 1 ? "Off Duty" : (currentStatus == 2 ? "Sleeper" : "On Duty");
                //int hourLeft = (int) ((new Date()).getTime() - statusDT.getTime()) / (1000 * 60);
                tvViolation.setText("N/A");
                tvViolationDate.setText("");
                tvRemaingTime.setText("N/A");
                tvTotalDrivingHours.setText("N/A");

                pbTotalDrivingHours.setProgress(0);
                pbTimeCountProgress.setProgress(0);
            }
        } catch (Exception exe) {
            tvViolation.setText("N/A");
            tvViolationDate.setText("");
            LogFile.write(ELogFragment.class.getName() + "::AutoHoursCalculate Error:" + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }

        AutoViolationCalculate();
    }


    private void AutoViolationCalculate() {
        try {
            HourOfService.listDutyStatus = Utility.dutyStatusList;
            int timeLeft = 0;
            if (currentRule == 1) {
                timeLeft = HourOfService.CanadaHours70(Utility.getCurrentDateTime());
                timeLeft = timeLeft < 0 ? 0 : timeLeft;
                GPSData.TimeRemaining70 = timeLeft;
                pbTotalCanadaRule.setProgress(pbTotalCanadaRule.getMax() - timeLeft);
                tvCanadaRuleValue.setText(Utility.getTimeFromMinute(timeLeft));
            } else if (currentRule == 2) {
                timeLeft = HourOfService.CanadaHours120(Utility.getCurrentDateTime());
                timeLeft = timeLeft < 0 ? 0 : timeLeft;
                GPSData.TimeRemaining120 = timeLeft;
                pbTotalCanadaRule.setProgress(pbTotalCanadaRule.getMax() - timeLeft);
                tvCanadaRuleValue.setText(Utility.getTimeFromMinute(timeLeft));
            } else if (currentRule == 3) {
                timeLeft = HourOfService.US70HoursGet(Utility.getCurrentDateTime());
                timeLeft = timeLeft < 0 ? 0 : timeLeft;
                GPSData.TimeRemainingUS70 = timeLeft;
                pbTotalUSRule.setProgress(pbTotalUSRule.getMax() - timeLeft);
                tvUSRuleValue.setText(Utility.getTimeFromMinute(timeLeft));
            } else {
                timeLeft = HourOfService.US60HoursGet(Utility.getCurrentDateTime());
                timeLeft = timeLeft < 0 ? 0 : timeLeft;
                pbTotalUSRule.setProgress(pbTotalUSRule.getMax() - timeLeft);
                tvUSRuleValue.setText(Utility.getTimeFromMinute(timeLeft));
            }

            if (currentRule == 1 || currentRule == 2) {
                ViolationBean chs = HourOfService.CanadaHoursSummary(Utility.getCurrentDateTime(), false);
                //tvTotalDrivingHours.setText(Utility.getTimeFromMinute(chs.getTimeLeft13()));
                tvTotalWorkShiftHours.setText(Utility.getTimeFromMinute(chs.getTimeLeft16()));
                GPSData.WorkShiftRemaining = chs.getTimeLeft16();
                //  pbTotalDrivingHours.setProgress(pbTotalDrivingHours.getMax() - chs.getTimeLeft13());
                pbTotalWorkShiftHour.setProgress(pbTotalWorkShiftHour.getMax() - chs.getTimeLeft16());

                // predictive us rule calculation when under canada rule
                timeLeft = HourOfService.US70HoursGet(Utility.getCurrentDateTime());
                timeLeft = timeLeft < 0 ? 0 : timeLeft;
                GPSData.TimeRemainingUS70 = timeLeft;
                pbTotalUSRule.setProgress(pbTotalUSRule.getMax() - timeLeft);
                tvUSRuleValue.setText(Utility.getTimeFromMinute(timeLeft));
            } else {
                ViolationBean chs = HourOfService.USHoursSummaryGet(Utility.getCurrentDateTime(), false);
                //tvTotalDrivingHours.setText(Utility.getTimeFromMinute(chs.getTimeLeft11()));
                tvTotalWorkShiftHours.setText(Utility.getTimeFromMinute(chs.getTimeLeft14US()));
                GPSData.WorkShiftRemaining = chs.getTimeLeft14US();
                //  pbTotalDrivingHours.setProgress(pbTotalDrivingHours.getMax() - chs.getTimeLeft11());
                pbTotalWorkShiftHour.setProgress(pbTotalWorkShiftHour.getMax() - chs.getTimeLeft14US());
                // predictive canada rule calculation when under US rule
                timeLeft = HourOfService.CanadaHours70(Utility.getCurrentDateTime());
                timeLeft = timeLeft < 0 ? 0 : timeLeft;
                GPSData.TimeRemaining70 = timeLeft;
                tvCanadaRuleValue.setText(Utility.getTimeFromMinute(timeLeft));
                pbTotalCanadaRule.setProgress(pbTotalCanadaRule.getMax() - timeLeft);
            }

        } catch (Exception exe) {
            LogFile.write(ELogFragment.class.getName() + "::AutoViolationCalculate Error:" + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }


    public void updateOdometer() {
        try {
            if (tvVehicleMiles != null && tvCurrentTrip != null) {

                tvVehicleMiles.setText(Double.valueOf(CanMessages.OdometerReading).intValue() + "/" + String.format("%.1f", Double.valueOf(CanMessages.EngineHours)));
                try {
                    if (!CanMessages.RPM.equals("0")) {
                        String accumulatedVehicleMiles = (Double.valueOf(CanMessages.OdometerReading).intValue() - Double.valueOf(Utility.OdometerReadingSincePowerOn).intValue()) + "";
                        String elapsedEngineHours = String.format("%.1f", Double.valueOf(CanMessages.EngineHours) - Double.valueOf(Utility.EngineHourSincePowerOn));
                        tvCurrentTrip.setText(accumulatedVehicleMiles + " Km in " + elapsedEngineHours + " Hrs");
                    } else {
                        tvCurrentTrip.setText("--");

                    }

                } catch (Exception ex) {

                }
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::updateOdometer Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }


    private void saveDutyStatusFlag() {
        try {
            if (getActivity() != null) {
                SharedPreferences.Editor e = (getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE))
                        .edit();
                e.putInt("duty_status_flag", Utility.statusFlag);
                e.commit();
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::saveDutyStatusFlag Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public void refresh() {
        try {
            if (canvas != null) {

                Utility.dutyStatusList = HourOfServiceDB.DutyStatusGet15Days(currentDate, Utility.onScreenUserId + "", false);
                dailyLogId = DailyLogDB.getDailyLog(Utility.onScreenUserId, Utility.getCurrentDate());
                currentDate = Utility.dateOnlyGet(new Date());
                if (Utility.dutyStatusList.size() > 0) {
                    currentStatus = Utility.dutyStatusList.get(0).getStatus();
                    statusDT = Utility.sdf.parse(Utility.dutyStatusList.get(0).getStartTime());

                    if (currentStatus == 1 && Utility.statusFlag == 1) {
                        if (Utility.dutyStatusList.get(0).getPersonalUse() == 1) {
                            currentStatus = 5;
                        }
                    }
                } else {
                    currentStatus = 1;
                    statusDT = currentDate;
                }
                mListener.setDutyStatus(currentStatus);
                butDutyStatus.setText(getResources().getStringArray(R.array.duty_status)[currentStatus - 1]);

                // draw line on canvas
                Thread thBitMap = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (finalHeight != 0 && finalWidth != 0 && getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        initializeBitmap(false);
                                    }
                                });

                                break;
                            }
                        }
                    }
                });

                thBitMap.setName("Elog-Bitmap3");
                thBitMap.start();
                DutyStatusGet();
                if (currentStatus == 3) {
                    InvokeRule();
                }

                AutoHoursCalculate();
            } else {
                Log.d(TAG, "Canvas is null");
            }
            if (lvEvents != null && getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        eventAdapter.changeItems(getListEvents());
                    }
                });
            } else {
                Log.d(TAG, "lvEvents is null");
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::refresh Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof ListView)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    unselectEvent();
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public void unselectEvent() {
        Log.d(TAG, "unselectEvent");
        eventAdapter.unselectEvent();
    }


    @Override
    public void onSavedRule(int rule) {
        try {
            if (rule != currentRule) {
                DailyLogDB.DailyLogRuleSave(Utility.onScreenUserId, rule, Utility.getCurrentDateTime(), Utility.getCurrentDateTime());
                DailyLogDB.DailyLogSyncRevert(Utility.onScreenUserId, dailyLogId);
                currentRule = rule;
                //tvCurrentRuleLabel.setText(getRule(currentRule - 1));
                pbTotalCanadaRule.setMax(currentRule == 2 ? 120 * 60 : 70 * 60);
                pbTotalUSRule.setMax(currentRule == 4 ? 60 * 60 : 70 * 60);
                // set max value of driving hours according to US/Canada rule
                if (currentRule == 1 || currentRule == 2) {
                    pbTimeCountProgress.setMax(13 * 60);
                    pbTotalDrivingHours.setMax(13 * 60);
                    pbTotalWorkShiftHour.setMax(16 * 60);
                    tvCanadaRule.setText(getRule(currentRule - 1));
                } else {
                    pbTimeCountProgress.setMax(11 * 60);
                    pbTotalDrivingHours.setMax(11 * 60);
                    pbTotalWorkShiftHour.setMax(14 * 60);
                    tvUSRule.setText(getRule(currentRule - 1));
                }
                DutyStatusGet();
                if (currentStatus == 3) {
                    InvokeRule();
                    AutoHoursCalculate();
                }
                Utility._appSetting.setDefaultRule(rule);
                SettingsDB.CreateSettings();
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::onSavedRule Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onSavedDutyStatus(int status, boolean saveForActiveDriver, String annotation, String location) {
        try {
            currentStatus = status;
            bDialogShowing = false;
            if (currentStatus == 5) {
                Utility.statusFlag = 1;
                Utility.powerOnOff = 2;
            } else if (currentStatus == 6) {
                Utility.statusFlag = 2;
            } else {
                Utility.statusFlag = 0;
            }
            mListener.setActiveDutyStatus(currentStatus);
            mListener.resetFlag();

            if (saveForActiveDriver && Utility.onScreenUserId != Utility.activeUserId) {

                return;
            }

            statusStartOdometerReading = Double.valueOf(CanMessages.OdometerReading).intValue();
            statusDT = new Date();
            Utility.dutyStatusList = HourOfServiceDB.DutyStatusGet15Days(currentDate, Utility.onScreenUserId + "", false);

            butDutyStatus.setText(getResources().getStringArray(R.array.duty_status)[currentStatus - 1]);

            DutyStatusGet();

            if (currentStatus == 3) {
                InvokeRule();
            } else {
                totalDistance = totalDistance + (Double.valueOf(CanMessages.OdometerReading).intValue() - statusStartOdometerReading);
            }

            saveDutyStatusFlag();
            AutoHoursCalculate();

            DailyLogDB.DailyLogHoursReCertify(Utility.onScreenUserId, dailyLogId);

            mListener.setCertify(0);
            setCerifyFlag(0);
            mListener.setDutyStatus(currentStatus);

            if (lvEvents != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        eventAdapter.changeItems(getListEvents());
                    }
                });
            }
            // dutyDialog.dismiss();
            bDialogShowing = false;
        } catch (Exception e) {
            Log.d(TAG, "onSavedDutyStatus error " + e.getMessage());
            LogFile.write(ELogFragment.class.getName() + "::onSavedDutyStatus Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onDissmisDialog() {
        // dutyDialog.dismiss();
        bDialogShowing = false;
    }

    public void SwitchDriver() {
        Log.d(TAG, "SwitchDriver");
        if (mListener != null) {
            mListener.changeUser();
        }
    }

    public void callActive() {
        Log.d(TAG, "Call Active");
        if (mListener != null) {
            mListener.activeUser();
        }
    }

    public void callCertify() {
        Log.d(TAG, "Call Certify");
        try {
            final AlertDialog ad = new AlertDialog.Builder(Utility.context)
                    .create();
            ad.setCancelable(true);
            ad.setCanceledOnTouchOutside(false);
            ad.setTitle("Certify Log(s) ?");
            ad.setIcon(R.drawable.ic_launcher);
            ad.setMessage("I hereby certify that my data entries and my record of duty status for this 24-hour period are true and correct.");
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "Certify",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();


                            int dailyLogId = DailyLogDB.getDailyLog(driverId, Utility.getCurrentDate());
                            DailyLogDB.certifyLogBook(driverId, dailyLogId + "");

                            setCerifyFlag(1);
                            if (mListener != null)
                                mListener.setCertify(1);
                        }
                    });
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, "Not Ready",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            ad.cancel();
                        }
                    });
            ad.show();
        } catch (Exception e) {
            LogFile.write(DailyLogDB.class.getName() + "::callCertify Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
    }

    public void callChangeRule() {
        Log.d(TAG, "Call change rule");
        launchRuleChange();
    }

    public void callSync() {
        Log.d(TAG, "Call Sync");
        showLoaderAnimation(true);
        new SyncData(syncDataPostTaskListener).execute();
    }

    public void callUndocking() {
        Log.d(TAG, "Call Undocking");
        fabMenu.setEnabled(false);
        Animation hide_fab_menu = AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.fade_out);

        if (layout_menu.getVisibility() == View.VISIBLE) {
            layout_menu.startAnimation(hide_fab_menu);
            layout_menu.setVisibility(View.INVISIBLE);
            restView.setVisibility(View.INVISIBLE);
        }
        if (mListener != null) {
            mListener.undocking();
        }
    }

    public void callPost() {
        final AlertDialog ad = new AlertDialog.Builder(getActivity())
                .create();
        ad.setCancelable(true);
        ad.setCanceledOnTouchOutside(false);
        ad.setTitle("Send Data Confirmation?");
        ad.setIcon(R.drawable.ic_launcher);
        ad.setMessage("Are you sure you want to Send Data to Server?");
        ad.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        if (Utility.isInternetOn()) {
                            Log.d(TAG, "run post data");
                            showLoaderAnimation(true);
                            new PostData(postDataPostTaskListener).execute();
                        }
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

    private void showLoaderAnimation(boolean isShown) {
        try {
            if (isShown) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "End animation");
                                    showLoaderAnimation(false);
                                }
                            });
                        }
                    }
                }, 30000);
                rlLoadingPanel.setVisibility(View.VISIBLE);
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            } else {
                rlLoadingPanel.setVisibility(View.GONE);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::showLoaderAnimation Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public void setCerifyFlag(int value) {
        certifyFg = value;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (certifyFg == 1) {
                    fabCertify.setVisibility(View.VISIBLE);
                    fabUncertify.setVisibility(View.GONE);
                } else if (certifyFg == 0) {
                    fabCertify.setVisibility(View.GONE);
                    fabUncertify.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void setFirstLogin(boolean value) {
        firstLogin = value;
    }

    public void updateMissingLogData() {
        final String loginDate = DailyLogDB.getLastDailyLogDate(Utility.onScreenUserId); //Utility.dateOnlyStringGet(Utility.LastEventDate);
        int numMissingDay = 0;
        int count = 0;
        while (count < Utility.getDiffDay(loginDate, Utility.getCurrentDate())) {
            String logDate = Utility.getDateFromString(loginDate, count);
            int logId = DailyLogDB.getDailyLog(Utility.onScreenUserId, logDate);
            if (logId == 0) {//missing day
                numMissingDay++;
            }
            count++;
        }

        if (numMissingDay > 0 && numMissingDay < 15) {
            int dayCount = 0;
            int differ = Utility.getDiffDay(loginDate, Utility.getCurrentDate());
            while (dayCount < differ) {
                //need to check what day has no log
                //if LogInfoSync return list of logged day, it will be easier
                String logDate = Utility.getDateFromString(loginDate, dayCount);

                if (DailyLogDB.getDailyLog(Utility.onScreenUserId, logDate) == 0) {
                    int logId = DailyLogDB.DailyLogCreateByDate(Utility.onScreenUserId, logDate, "", "", "");
                    EventDB.EventCreate(logDate + " 00:00:00", 1, 1, "Driver's Duty Status changed to OFF DUTY", 1, 1, logId, Utility.onScreenUserId, "");
                }
                dayCount++;
            }
        }
    }


    LogEventSync.PostTaskListener<Boolean> refreshElogFragment = new LogEventSync.PostTaskListener<Boolean>() {
        @Override
        public void onPostTask(Boolean result) {
            if (result) {
                try {
                    updateMissingLogData();
                    ELogFragment.this.refresh();
                } catch (Exception exe) {
                }
            } else {
            }

        }
    };

}
