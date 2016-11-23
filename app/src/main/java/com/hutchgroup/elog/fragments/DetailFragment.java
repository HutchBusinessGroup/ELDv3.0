package com.hutchgroup.elog.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.adapters.EventAdapter;
import com.hutchgroup.elog.beans.DailyLogBean;
import com.hutchgroup.elog.beans.DutyStatusBean;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.beans.RuleBean;
import com.hutchgroup.elog.beans.UserBean;
import com.hutchgroup.elog.beans.ViolationBean;
import com.hutchgroup.elog.bll.HourOfService;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.CarrierInfoDB;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;
import com.hutchgroup.elog.db.HourOfServiceDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class DetailFragment extends Fragment implements View.OnClickListener, InputInformationDialog.InputInformationDialogInterface, InputTruckIDDialog.InputTruckIDDialogInterface {
    String TAG = DetailFragment.class.getName();
    static String fullFormat = "yyyy-MM-dd HH:mm:ss";
    int driverId = 1;
    TextView tvRecordDate, tvUSDot, tvDriverLicense, tvDriverLicenseState, tvTimeZoneText, tvDriverName;
    TextView tvCoDriverName, tvELDManufacturer, tvDataDiagnostic, tvCarrier, tvDriverID, tvCoDriverID, tvUnidentifiedDriverRecords;
    TextView tvELDMalfunction, tvCurrentLocation, tvStartEndOdometer, tvMilesToday, tvTruckTractorVIN, tvExemptDriverStatus, tvStartEndEngineHours;
    EditText edTrailerID, edShippingID, edCommodity, tvTruckTractorID;
    ImageButton fabUncertify;
    TextView tvOffDutyTime, tvSleeperTime, tvDrivingTime, tvOnDutyTime;

    private ImageView imgDutyStatus;
    private ImageView imgViewLandscape;
    private ImageView imgViewPortrait;

    Bitmap bmp;
    Canvas canvas;
    public Date currentDate;
    Date selectedDate;
    public Date statusDT;

    ListView lvEvents;

    EventAdapter eventAdapter;
    List<EventBean> listEvents;
    int dailyLogId, certifyFG;

    int finalHeight = 0;
    int finalWidth = 0;
    int imageWidth = 0;
    int imageHeight = 0;
    int currentRule = 1;
    View inforHeader;

    InputTruckIDDialog truckIdDialog;
    InputInformationDialog infosDialog;

    public int currentStatus = 1;

    @SuppressLint("ValidFragment")
    public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            //int hour = c.get(Calendar.HOUR_OF_DAY);
            //int minute = c.get(Calendar.MINUTE);
            c.setTime(selectedDate);

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dilog = new DatePickerDialog(getActivity(), this, year, month, day);
            DatePicker picker = dilog.getDatePicker();
            c.setTime(Utility.newDate());
            c.add(Calendar.DATE, 1);
            picker.setMaxDate(c.getTime().getTime());
            c.setTime(Utility.dateOnlyGet(Utility.newDate()));
            c.add(Calendar.DATE, -14);
            picker.setMinDate(c.getTime().getTime());
            return dilog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day); //reset seconds to zero

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            try {
                calendar.setTime(formatter.parse(formatter.format(calendar.getTime())));
                selectedDate = calendar.getTime();
                refresh();
                Log.d(TAG, "Date:" + Utility.GetString(selectedDate));
            } catch (Exception ex) {
                Log.d(TAG, "Error: " + ex.getMessage());
                LogFile.write(DetailFragment.class.getName() + "::onDateSet Error: " + ex.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            }

        }
    }

    private OnFragmentInteractionListener mListener;

    public DetailFragment() {
        // Required empty public constructor
    }

    private void initialize(View view) {
        try {
            //driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            tvOffDutyTime = (TextView) view.findViewById(R.id.tvOffDutyTime);
            tvSleeperTime = (TextView) view.findViewById(R.id.tvSleeperTime);
            tvDrivingTime = (TextView) view.findViewById(R.id.tvDrivingTime);
            tvOnDutyTime = (TextView) view.findViewById(R.id.tvOnDutyTime);

            tvRecordDate = (TextView) view.findViewById(R.id.tvRecordDate);
            tvUSDot = (TextView) view.findViewById(R.id.tvUsDot);
            tvDriverLicense = (TextView) view.findViewById(R.id.tvDriverLicense);
            tvDriverLicenseState = (TextView) view.findViewById(R.id.tvDriverLicenseState);
            tvTimeZoneText = (TextView) view.findViewById(R.id.tvTimeZoneText);
            tvDriverName = (TextView) view.findViewById(R.id.tvDriverName);
            tvCoDriverName = (TextView) view.findViewById(R.id.tvCoDriverName);
            tvELDManufacturer = (TextView) view.findViewById(R.id.tvELDManufacturer);
            tvCarrier = (TextView) view.findViewById(R.id.tvCarrier);
            tvDriverID = (TextView) view.findViewById(R.id.tvDriverID);
            tvCoDriverID = (TextView) view.findViewById(R.id.tvCoDriverID);
            tvTruckTractorID = (EditText) view.findViewById(R.id.tvTruckTractorID);


            tvUnidentifiedDriverRecords = (TextView) view.findViewById(R.id.tvUnidentifiedDriverRecords);
            boolean unidentifiedFg = EventDB.UnIdentifiedEventFg();
            if (unidentifiedFg)
                tvUnidentifiedDriverRecords.setText("Yes");

            tvELDMalfunction = (TextView) view.findViewById(R.id.tvELDMalfunction);
            if (Utility.malFunctionIndicatorFg) {
                tvELDMalfunction.setText("Yes");
            }
            tvCurrentLocation = (TextView) view.findViewById(R.id.tvCurrentLocation);
            tvStartEndOdometer = (TextView) view.findViewById(R.id.tvStartEndOdometer);
            tvMilesToday = (TextView) view.findViewById(R.id.tvMilesToday);
            tvTruckTractorVIN = (TextView) view.findViewById(R.id.tvTruckTractorVIN);
            tvExemptDriverStatus = (TextView) view.findViewById(R.id.tvExemptDriverStatus);
            tvStartEndEngineHours = (TextView) view.findViewById(R.id.tvStartEndEngineHours);
            tvDataDiagnostic = (TextView) view.findViewById(R.id.tvDataDiagnostic);
            if (Utility.dataDiagnosticIndicatorFg) {
                tvDataDiagnostic.setText("Yes");
            }

            tvTimeZoneText.setText(Utility.TimeZoneOffsetUTC);

            edShippingID = (EditText) view.findViewById(R.id.edShippingID);

            edTrailerID = (EditText) view.findViewById(R.id.edTrailerID);

            fabUncertify = (ImageButton) view.findViewById(R.id.fab_certify);
            if (!Utility.InspectorModeFg) {
                fabUncertify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dailyLogId == 0) {
                            Utility.showAlertMsg("Logbook does not exists!");
                            return;
                        }
                        certifyLogBook();
                    }
                });

                tvTruckTractorID.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "enter truck id");
                        launchEnterTruckID();
                    }
                });

                edShippingID.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchInputDialog();
                    }
                });

                edTrailerID.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchInputDialog();
                    }
                });
            }
            if (certifyFG == 1) {
                fabUncertify.setClickable(false);
                fabUncertify.setImageResource(R.drawable.ic_verified_user_black_36dp);
            } else {
                fabUncertify.setClickable(!Utility.InspectorModeFg);
                fabUncertify.setImageResource(R.drawable.ic_fab_uncertified_red);
            }

            //set values into controls
            UserBean user = null;//Utility.user1.isOnScreenFg() ? Utility.user1 : Utility.user2;
            UserBean coUser = null;
            if (Utility.user1.isOnScreenFg()) {
                user = Utility.user1;
            } else {
                user = Utility.user2;
            }

            tvRecordDate.setText(Utility.GetStringDate(selectedDate));
            tvDriverName.setText(user.getFirstName() + " " + user.getLastName());
            tvDriverID.setText(Integer.toString(user.getAccountId()));
            tvDriverLicense.setText(user.getDrivingLicense());
            tvDriverLicenseState.setText(user.getDlIssueState());
            tvCarrier.setText(Utility.CarrierName);
            tvELDManufacturer.setText(Utility.ELDManufacturer);
            tvUSDot.setText(Utility.USDOT);
            String plateNo = Utility.PlateNo == null || Utility.PlateNo.isEmpty() ? "" : " (" + Utility.PlateNo + ")";
            tvTruckTractorID.setText(Utility.UnitNo + plateNo);
            tvTruckTractorVIN.setText(Utility.VIN);
            tvExemptDriverStatus.setText(user.getExemptELDUseFg() == 1 ? "Yes" : "No");


            currentRule = DailyLogDB.getCurrentRule(Utility.onScreenUserId);
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::initialize Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void initializeBitmap() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "initializeBitmap");
                    //bmp = Bitmap.createBitmap(imgDutyStatus.getWidth(),
                    //        imgDutyStatus.getHeight(), Bitmap.Config.ARGB_8888);
                    if (bmp != null) {
                        bmp.recycle();
                    }
                    Log.e(TAG, "sceen size=" + finalWidth + "x" + finalHeight);
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
                    Log.d(TAG, "bitmap size=" + bmp.getWidth() + "x" + bmp.getHeight());


                    canvas = new Canvas(bmp);
                    //imgDutyStatus.draw(canvas);

                    initializeStatus();
                } catch (Exception exe) {

                    //Utility.printError(exe.getMessage());
                    Log.d(TAG, "Error: " + exe.getMessage());
                    LogFile.write(DetailFragment.class.getName() + "::initializeBitmap Error: " + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
                }
            }
        }, 50);
    }

    public static DetailFragment newInstance() {
        return new DetailFragment();

    }

    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabdesign, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    private void initializeTab(View view) {
        TabHost host = (TabHost) view.findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        View tabview = createTabView(host.getContext(), "Driver");
        TabHost.TabSpec spec = host.newTabSpec("Driver").setIndicator(tabview);
        spec.setContent(R.id.tabDriver);
        host.addTab(spec);

        //Tab 2
        tabview = createTabView(host.getContext(), "Vehicle");
        spec = host.newTabSpec("Vehicle").setIndicator(tabview);
        spec.setContent(R.id.tabVehicle);
        host.addTab(spec);

        //Tab 3
        tabview = createTabView(host.getContext(), "Company");
        spec = host.newTabSpec("Company").setIndicator(tabview);
        spec.setContent(R.id.tabCompany);
        host.addTab(spec);
    }

    public static final String ARG_Date = "date";
    private int mPageNumber, totalPage = 15;

    public static DetailFragment newInstance(Date date) {
        DetailFragment fragment = new DetailFragment();
      /*  Bundle args = new Bundle();
        args.putSerializable(ARG_Date, date);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //currentDate = (Date) getArguments().getSerializable(ARG_Date);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Configuration config = getResources().getConfiguration();
        View view = inflater.inflate(R.layout.fragment_inspectdailylog, container, false);
        try {
            // currentDate, Utility.dutyStatusList was null so intializing
            if (currentDate == null) {
                currentDate = Utility.dateOnlyGet(Utility.newDate());
            }
            selectedDate = currentDate;

            driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
            DailyLogBean dailyLog = DailyLogDB.getDailyLogInfo(driverId, Utility.GetString(selectedDate));
            dailyLogId = dailyLog.get_id();
            certifyFG = dailyLog.getCertifyFG();

            lvEvents = (ListView) view.findViewById(R.id.lvEvent);


            Utility.dutyStatusList = HourOfServiceDB.DutyStatusGet15Days(currentDate, driverId + "", false);

            Log.d(TAG, "onCreateView");
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                inforHeader = inflater.inflate(R.layout.fragment_detail, null, false);
                //lvEvents.addHeaderView(header);
            } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                inforHeader = inflater.inflate(R.layout.fragment_detail_portrait, null, false);
                //lvEvents.addHeaderView(header);
            }
            lvEvents.addHeaderView(inforHeader);

            initialize(view);

            initializeTab(view);

            if (mListener != null) {
                String title = getContext().getResources().getString(R.string.title_inspect_elog);
                mListener.setTitle(title);
            }

            listEvents = getListEvents(Utility.getCurrentDate());
            eventAdapter = new EventAdapter(this.getActivity(), listEvents);
            eventAdapter.setFromInspect(true);
            lvEvents.setAdapter(eventAdapter);
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
                    //Log.e(TAG, "Height = " + finalHeight + " - Width = " + finalWidth);
                    return false;
                }
            });

            initializeBitmap();
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "onResume inspect");
//            if (lvEvents != null) {
//                //eventAdapter.changeItems(getListEvents());
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //eventAdapter = new EventAdapter(Utility.context, getListEvents());
//                        if (selectedDate != null) {
//                            listEvents = getListEvents(Utility.GetString(selectedDate));
//                            eventAdapter.changeItems(listEvents);
//                        }
//                    }
//                });
//            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::onResume Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
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
        try {
            mListener = null;
            finalHeight = 0;
            finalWidth = 0;
            canvas = null;
            listEvents.clear();
            eventAdapter.clear();
            lvEvents.setAdapter(null);
            HourOfService.listDutyStatus.clear();
            ;
            Utility.dutyStatusList.clear();
            ;
            HourOfService.violations.clear();
            ;
            listEvents = null;
            infosDialog = null;
            truckIdDialog = null;
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::onDetach Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.butBack:
                    //setResult(Activity.RESULT_CANCELED);
                    mListener.onDetailClosed();
                    //finish();
                    break;
                case R.id.butSave:
                    SharedPreferences.Editor e = (getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE))
                            .edit();
                    e.putString("shipping_number", edShippingID.getText().toString());
                    e.putString("trailer_number", edTrailerID.getText().toString());
                    e.putInt("driverid", Utility.onScreenUserId);
                    e.commit();
                    DailyLogDB.DailyLogCreate(driverId, edShippingID.getText().toString(), edTrailerID.getText().toString(), "");
                    mListener.onDetailSaved();
                    //finish();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            if (requestCode == 1) {
                if (resultCode == Activity.RESULT_OK) {
                    edShippingID.setText(Utility.ShippingNumber);
                    edTrailerID.setText(Utility.TrailerNumber);
                }
            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::onActivityResult Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            currentDate = selectedDate;
            Log.d(TAG, "fragment onConfigurationChanged");
            //lvEvents.removeHeaderView(inforHeader);
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.fragment_inspectdailylog, null);

            ViewGroup viewGroup = (ViewGroup) getView();

            viewGroup.removeAllViews();
            viewGroup.addView(view);

            lvEvents = (ListView) view.findViewById(R.id.lvEvent);


            Log.d(TAG, "onCreateView");
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                inforHeader = inflater.inflate(R.layout.fragment_detail, null, false);
                //lvEvents.addHeaderView(header);
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                inforHeader = inflater.inflate(R.layout.fragment_detail_portrait, null, false);
                //lvEvents.addHeaderView(header);
            }
            lvEvents.addHeaderView(inforHeader);

            initialize(view);
            initializeTab(view);
            //eventAdapter = new EventAdapter(this.getActivity(), getListEvents(Utility.GetString(selectedDate)));
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
                                    initializeBitmap();
                                }
                            });

                            break;
                        }
                    }
                }
            });
            thBitMap.setName("ElogDetail-Bitmap");
            thBitMap.start();

            refresh();
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::onConfigurationChanged Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void launchEnterTruckID() {
        try {
            if (truckIdDialog == null) {
                truckIdDialog = new InputTruckIDDialog();
            }
            truckIdDialog.setTrucID(tvTruckTractorID.getText().toString());
            truckIdDialog.mListener = this;

            truckIdDialog.show(getFragmentManager(), "truckid_dialog");
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::launchEnterTruckID Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void launchInputDialog() {
        try {
            if (infosDialog == null) {
                infosDialog = new InputInformationDialog();
            }
            if (!infosDialog.isVisible()) {
                infosDialog.mListener = this;
                infosDialog.setSelectedDate(Utility.GetString(selectedDate));

                infosDialog.show(getFragmentManager(), "infos_dialog");
            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::launchInputDialog Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onInputFinished() {
        Log.d("Input", "fragment onInputFinished");
        infosDialog.mListener = null;
    }

    @Override
    public void onInputSaved(String shippId, String trailerId) {
        Log.d("Input", "fragment onInputSaved");
        edShippingID.setText(shippId);
        edTrailerID.setText(trailerId);

        //DailyLogDB.DailyLogCreate(driverId, shippId, trailerId, "");
        infosDialog.mListener = null;
    }

    @Override
    public void onTruckIDSaved(String truckID) {
        Utility.UnitNo = truckID;
        tvTruckTractorID.setText(truckID);

        CarrierInfoDB.SaveUnitNo();
        truckIdDialog.mListener = null;
    }

    @Override
    public void onTruckIDFinished() {
        truckIdDialog.mListener = null;
    }

    public void ChooseDate() {
        try {
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::ChooseDate Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public void BackOneDay() {
        try {
            Log.d(TAG, "Back One Day");
            if (selectedDate != null) {
                Log.d(TAG, "Num of days=" + Utility.getDiffDay(Utility.GetString(selectedDate), Utility.getCurrentDate()));
                int diff = Utility.getDiffDay(Utility.GetString(selectedDate), Utility.getCurrentDate());

                if (Utility.InspectorModeFg && currentRule > 2 && diff >= 8) {
                    return;
                }

                if (diff >= 15) {
                    return;
                }
                selectedDate = Utility.getDate(selectedDate, -1);
                currentDate = selectedDate;
                Log.d(TAG, "previous date: " + selectedDate);
                //update Activity title
                refresh();
            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::BackOneDay Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public void ForwardOneDay() {
        try {
            Log.d(TAG, "Forward One Day");
            if (selectedDate != null) {
                if (Utility.getDiffDay(Utility.GetString(selectedDate), Utility.getCurrentDate()) <= 0) {
                    return;
                }
                selectedDate = Utility.getDate(selectedDate, 1);
                currentDate = selectedDate;
                Log.d(TAG, "next date: " + selectedDate);

                refresh();
            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::ForwardOneDay Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public void refresh() {
        try {
            if (mListener != null) {
                mListener.updateTitle(Utility.GetStringDate(selectedDate));
            }
            DailyLogBean dailyLog = DailyLogDB.getDailyLogInfo(driverId, Utility.GetString(selectedDate));
            dailyLogId = dailyLog.get_id();

            tvRecordDate.setText(Utility.GetStringDate(selectedDate));

            DutyStatusGetByDate(selectedDate);

            certifyFG = dailyLog.getCertifyFG();
            if (certifyFG == 1) {
                fabUncertify.setClickable(false);
                fabUncertify.setImageResource(R.drawable.ic_verified_user_black_36dp);
            } else {
                fabUncertify.setClickable(!Utility.InspectorModeFg);
                fabUncertify.setImageResource(R.drawable.ic_fab_uncertified_red);
            }

            if (lvEvents != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        eventAdapter.changeItems(getListEvents(Utility.GetString(selectedDate)));
                    }
                });
            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::refresh Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void initializeStatus() {
        try {
            statusDT = selectedDate;
            // Utility.dutyStatusList = HourOfServiceDB.DutyStatusGet15Days(selectedDate, driverId + "", false);
            Log.d(TAG, "Utility dutyStatusList size=" + Utility.dutyStatusList.size());
            if (Utility.dutyStatusList.size() > 0) {
                try {
                    currentStatus = Utility.dutyStatusList.get(0).getStatus();
                    statusDT = Utility.parse(Utility.dutyStatusList.get(0).getStartTime());
                } catch (Exception exe) {
                }
            } else {
                currentStatus = 1;
            }

            DutyStatusGet();
            if (currentStatus == 3) {
                InvokeRule();
            }
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::initializeStatus Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private List<EventBean> getListEvents(String date) {

        List<EventBean> dutyStatusList = EventDB.EventGetByLogId(driverId, dailyLogId);
        //Log.d(TAG, "duty list size=" + dutyStatusList.size());
        this.DailyLogInfoGet(driverId, date);
        //return dutyStatusList;
        return AddViolationToEvents(dutyStatusList);
    }

    // Deepak Sharma
    // 3 June 2016
    // add violation to events
    private List<EventBean> AddViolationToEvents(List<EventBean> eventList) {
        if (Utility._appSetting.getShowViolation() == 0)
            return eventList;
        HourOfService.ViolationCalculation(selectedDate, driverId);
        ArrayList<ViolationBean> vList = HourOfService.violations;
        Date currTime = Utility.newDate();
        for (int i = 0; i < vList.size(); i++) {

            Date startTime = vList.get(i).getStartTime();
            int totalMinutes = vList.get(i).getTotalMinutes();
            //  Date endTime = Utility.addMinutes(startTime, totalMinutes);
            Date vDate = Utility.dateOnlyGet(startTime);
            boolean isCurrent = vDate.equals(selectedDate);
            if (!isCurrent)
                continue;

           /* if (startTime.after(currTime) || endTime.before(selectedDate))
                continue;*/

            EventBean event = new EventBean();
            event.setEventType(-1); //-1 for violation
            event.setEventDateTime(Utility.format(startTime, fullFormat));
            event.setViolation(vList.get(i).getRule());

            event.setViolationTitle(vList.get(i).getTitle());
            event.setViolationExplanation(vList.get(i).getExplanation());

            event.setViolationMintes(Utility.getTimeFromMinute(totalMinutes));
            event.setEventRecordStatus(1);
            eventList.add(event);
        }
        Collections.sort(eventList, EventBean.dateDesc);
        return eventList;
    }

    private void InvokeRule() {
        try {
            HourOfService.InvokeRule(Utility.newDate(), driverId);
            //AutoHoursCalculate();
        } catch (Exception exe) {
            //Utility.printError(exe.getMessage());
            LogFile.write(DetailFragment.class.getName() + "::InvokeRule Error: " + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void DutyStatusGet() {
        try {
            ArrayList<DutyStatusBean> dutyStatus = this.DutyStatusGet(currentDate, Utility.dutyStatusList);
            Log.d(TAG, "dutyStatus size=" + dutyStatus.size());
            drawLine(dutyStatus, currentDate);
            StatusHourGet(dutyStatus);
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::DutyStatusGet Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void DutyStatusGetByDate(Date date) {
        try {
            ArrayList<DutyStatusBean> dutyStatus = this.DutyStatusGet(date, Utility.dutyStatusList);
            drawLine(dutyStatus, date);
            StatusHourGet(dutyStatus);
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::DutyStatusGetByDate Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void drawViolationArea() {
        HourOfService.ViolationCalculation(Utility.newDate(), driverId);
        ArrayList<ViolationBean> vList = HourOfService.violations;
        int startMinutes = 0;
        Date endTime = null, startTime = null;
        for (int i = 0; i < vList.size(); i++) {

            startTime = vList.get(i).getStartTime();
            startMinutes = (int) (startTime.getTime() - selectedDate.getTime()) / (1000 * 60);
            if (endTime != null && startTime.before(endTime)) {
                continue;
            }
            endTime = Utility.addMinutes(startTime, vList.get(i).getTotalMinutes());
            if (Utility._appSetting.getGraphLine() == 1 && endTime.after(Utility.newDate())) {
                if (startTime.after(Utility.newDate()))
                    break;
                endTime = Utility.newDate();
            }

            int endMinutes = (int) (endTime.getTime() - selectedDate.getTime()) / (1000 * 60);
            drawRect(getX(startMinutes), getRectY(1), getX(endMinutes), getRectY(4));

        }

    }

    //Created By: Deepak Sharma
    //Created Date: 5/27/2016
    //Purpose: get rule when event changed
    private RuleBean eventRuleGet(Date eventTime, ArrayList<RuleBean> ruleList) {
        RuleBean obj = new RuleBean();
        obj.setRuleId(1);
        obj.setRuleStartTime(selectedDate);
        obj.setRuleEndTime(Utility.addDays(selectedDate, 1));
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

    private void drawLine(ArrayList<DutyStatusBean> dutyStatus, Date logDate) {
        //clear bitmap bmp 333
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
        try {
            if (Utility._appSetting.getViolationOnGrid() == 1)
                drawViolationArea();

            String logDT = Utility.format(logDate, fullFormat);
            ArrayList<RuleBean> ruleList = DailyLogDB.getRuleByDate(logDT, driverId, dailyLogId);
            Collections.sort(ruleList, RuleBean.dateDesc);
            int ruleId = 1, startMinutes, endMinutes = 0;

            for (int i = 0; i < dutyStatus.size(); i++) {
                DutyStatusBean item = dutyStatus.get(i);
                int status = item.getStatus();
                Date startTime = Utility.parse(item.getStartTime()), endTime = Utility.parse(item.getEndTime());
                // graph line upto current time
                if (Utility._appSetting.getGraphLine() == 1 && i == dutyStatus.size() - 1) {
                    endTime = Utility.newDate();
                }

                RuleBean rule = eventRuleGet(startTime, ruleList);
                ruleId = rule.getRuleId();
                if (rule.getRuleEndTime().before(endTime)) {

                    startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                    endMinutes = (int) (rule.getRuleEndTime().getTime() - logDate.getTime()) / (1000 * 60);
                    if (i == 0 && startMinutes > 0) {
                        startMinutes = 0;
                    }
                    //Log.d(TAG, "1- " + i + "," + status + " :start time=" + startMinutes + " - end time=" + endMinutes);
                    drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId);
                    startTime = rule.getRuleEndTime();
                    ArrayList<RuleBean> ruleEventList = eventRuleListGet(startTime, endTime, ruleList);
                    for (RuleBean ruleBean : ruleEventList) {
                        if (ruleBean.getRuleStartTime().equals(rule.getRuleStartTime()))
                            continue;
                        startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                        endMinutes = (int) ((ruleBean.getRuleEndTime().before(endTime) ? ruleBean.getRuleEndTime() : endTime).getTime() - logDate.getTime()) / (1000 * 60);
                        ruleId = ruleBean.getRuleId();
                        //Log.d(TAG, "1- " + i + "," + status + " X=" + getX(endMinutes) + " - Y1=" + getY(status) + "/Y2=" + getY(status));
                        drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId);
                        startTime = ruleBean.getRuleEndTime();
                    }
                } else {

                    startMinutes = (int) (startTime.getTime() - logDate.getTime()) / (1000 * 60);
                    endMinutes = (int) (endTime.getTime() - logDate.getTime()) / (1000 * 60);
                    if (i == 0 && startMinutes > 0) {
                        startMinutes = 0;
                    }

                   /* if (status == 1) {
                        drawRect(getX(startMinutes), getRectY(1), getX(endMinutes), getRectY(4), ruleId);
                    }*/
                    Log.d(TAG, i + "," + status + " :start time=" + startMinutes + " - end time=" + endMinutes);
                    drawLine(getX(startMinutes), getY(status), getX(endMinutes), getY(status), ruleId);

                }
                if (i < dutyStatus.size() - 1) {
                    item = dutyStatus.get(i + 1);
                    Log.d(TAG, i + "," + status + " X=" + getX(endMinutes) + " - Y1=" + getY(status) + "/Y2=" + getY(item.getStatus()));
                    drawLine(getX(endMinutes), getY(status), getX(endMinutes), getY(item.getStatus()), ruleId);
                }
            }

            if (dutyStatus.size() == 0) {
                if (ruleList.size() > 0) {
                    ruleId = ruleList.get(0).getRuleId();

                }
                endMinutes = 1439;
                drawLine(getX(0), getY(1), getX(endMinutes), getY(1), ruleId);
            }

            if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
                imgViewLandscape.setImageDrawable(null);
                imgViewLandscape.setImageBitmap(bmp);
            } else if (Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation) {
                imgViewPortrait.setImageDrawable(null);
                imgViewPortrait.setImageBitmap(bmp);
            }
        } catch (Exception exe) {
            Log.d(TAG, "drawLine got exception");
            LogFile.write(ELogFragment.class.getName() + "::drawLine Error:" + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void drawLine(float x, float y, float xend, float yend, int ruleId) {
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
            canvas.drawLine(x, y, xend, yend, p);
        } catch (Exception e) {
            LogFile.write(ELogFragment.class.getName() + "::drawLine Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
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

    private int getX(int minutes) {
        float boxWidth = (float) imageWidth;//(float) imgDutyStatus.getWidth();
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

    private void StatusHourGet(ArrayList<DutyStatusBean> dutyStatus) {
        try {
            int offDuty = 0, sleeper = 0, driving = 0, onDuty = 0;
            boolean firstStatus = true;
            for (DutyStatusBean bean : dutyStatus) {
                if (Utility._appSetting.getGraphLine() == 1) {
                    Date startTime = Utility.parse(bean.getStartTime());
                    Date endTime = Utility.parse(bean.getEndTime());
                    if (firstStatus) {
                        firstStatus = false;
                        if (startTime.after(currentDate)) {
                            startTime = currentDate;

                            int totalHours = (int) Math.round((endTime.getTime() - startTime.getTime()) / (1000 * 60.0));
                            bean.setTotalMinutes(totalHours);
                        }
                    }

                    if (endTime.after(Utility.newDate())) {

                        int totalHours = (int) Math.round(((Utility.newDate()).getTime() - startTime.getTime()) / (1000 * 60.0));
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
            if (dutyStatus.size() == 0) {
                offDuty = 1440;
            }
            tvOffDutyTime.setText(Utility.getTimeFromMinute(offDuty));
            tvSleeperTime.setText(Utility.getTimeFromMinute(sleeper));
            tvDrivingTime.setText(Utility.getTimeFromMinute(driving));
            tvOnDutyTime.setText(Utility.getTimeFromMinute(onDuty));
        } catch (Exception e) {
            LogFile.write(DetailFragment.class.getName() + "::StatusHourGet Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public static ArrayList<DutyStatusBean> DutyStatusGet(Date date, ArrayList<DutyStatusBean> list) {

        ArrayList<DutyStatusBean> data = new ArrayList<>();
        try {
            Date nextDay = Utility.addDays(date, 1);

            for (int i = 0; i < list.size(); i++) {
                Date startDate = Utility.parse(list.get(i).getStartTime());
                Date endDate = Utility.parse(list.get(i).getEndTime());
                //Log.d(TAG, "Duty Status= " + list.get(i).getStatus() + " - Start Time=" + list.get(i).getStartTime() + " / End Time=" + list.get(i).getEndTime());
                int status = list.get(i).getStatus();
                int personalUseFg = list.get(i).getPersonalUse();
                startDate = startDate.before(date) ? date : startDate;
                endDate = endDate.after(nextDay) ? nextDay : endDate;
                if ((startDate.after(date) || endDate.after(date)) && startDate.before(nextDay)) {
                    int totalMinutes = (int) Math.round((endDate.getTime() - startDate.getTime()) / (1000 * 60.0));

                    DutyStatusBean bean = new DutyStatusBean();
                    bean.setStartTime(Utility.format(startDate, fullFormat));
                    bean.setEndTime(Utility.format(endDate, fullFormat));
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void onDetailClosed();

        void onDetailSaved();

        void updateTitle(String selectedDate);

        void setCertify(int certifyFg);

        void setTitle(String title);
    }

    //Created By: Deepak Sharma
    //Created Date: 12 March 2016
    //Purpose: Total distance of driver of day
    private void DailyLogInfoGet(int driverId, String date) {
        try {
            Log.d(TAG, "DailyLogInfoGet " + date);
            List<EventBean> dutyStatusList = EventDB.TotalDistanceGetByLogId(dailyLogId);
            int statusStartOdometerReading = 0, startOR = 0, endOR = 0, startEngineHours = 0, endEngineHours = 0;
            int totalDistance = 0;
            for (int i = 0; i < dutyStatusList.size(); i++) {
                EventBean bean = dutyStatusList.get(i);

                if (bean.getEventCode() == 3 || bean.getEventType() == 2) {
                    if (statusStartOdometerReading == 0)
                        statusStartOdometerReading = Double.valueOf(bean.getOdometerReading()).intValue();
                    else {
                        totalDistance += (Double.valueOf(bean.getOdometerReading()).intValue() - statusStartOdometerReading);
                        statusStartOdometerReading = Double.valueOf(bean.getOdometerReading()).intValue();
                    }

                } else {
                    if (statusStartOdometerReading > 0)
                        totalDistance += (Double.valueOf(bean.getOdometerReading()).intValue() - statusStartOdometerReading);
                    statusStartOdometerReading = 0;
                }

                if (i == 0) {
                    startOR = Double.valueOf(bean.getOdometerReading()).intValue();
                    startEngineHours = Double.valueOf(bean.getEngineHour()).intValue();


                } else if (i == dutyStatusList.size() - 1) {
                    endOR = Double.valueOf(bean.getOdometerReading()).intValue();
                    endEngineHours = Double.valueOf(bean.getEngineHour()).intValue();
                }
            }

            if (selectedDate.equals(currentDate)) {
                endOR = Double.valueOf(CanMessages.OdometerReading).intValue();
                endEngineHours = Double.valueOf(CanMessages.EngineHours).intValue();
                if (statusStartOdometerReading > 0) {
                    totalDistance += endOR - statusStartOdometerReading;
                }
            }

            tvStartEndOdometer.setText(startOR + " - " + endOR);
            tvStartEndEngineHours.setText(startEngineHours + " - " + endEngineHours);
            tvMilesToday.setText(totalDistance + "");
            Log.d(TAG, "totalDistance " + totalDistance);
            String CoDrivers = DailyLogDB.getCoDriver(driverId, date);

            if (CoDrivers.contains("#")) {
                String arrCD[] = CoDrivers.split("#");
                tvCoDriverID.setText(arrCD[0]);
                tvCoDriverName.setText(arrCD[1]);
            }

            DailyLogBean dailyBean = DailyLogDB.getDailyLogInfo(driverId, date);
            edShippingID.setText(dailyBean.getShippingId());
            edTrailerID.setText(dailyBean.getTrailerId());
        } catch (Exception exe) {
            Log.d(DetailFragment.class.getName(), "::DailyLogInfoGet Error: " + exe.getMessage());
            LogFile.write(DetailFragment.class.getName() + "::DailyLogInfoGet Error: " + exe.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public void certifyLogBook() {
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
                            DailyLogDB.certifyLogBook(driverId, dailyLogId + "");
                            listEvents = getListEvents(Utility.getCurrentDate());
                            eventAdapter.changeItems(listEvents);
                            fabUncertify.setClickable(false);
                            fabUncertify.setImageResource(R.drawable.ic_verified_user_black_36dp);

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
            LogFile.write(DailyLogDB.class.getName() + "::certifyLogBook Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
    }
}
