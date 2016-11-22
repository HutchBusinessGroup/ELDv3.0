package com.hutchgroup.elog.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.beans.TripInspectionBean;
import com.hutchgroup.elog.common.BitmapUtility;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.CustomDateFormat;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.TripInspectionDB;
import com.hutchgroup.elog.services.AutoStartService;
import com.hutchgroup.elog.tasks.GeocodeTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class NewInspectionFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, DefectSelectionDialog.DefectSelectionDialogInterface {
    String TAG = NewInspectionFragment.class.getName();
    private OnFragmentInteractionListener mListener;

    final int DEFECT_SELECTION_CODE = 1;
    final int TAKE_PHOTO_CODE = 0;
    final int VIEW_PHOTO = 2;

    TextView tvDateTime;
    TextView tvLocation;
    TextView tvOdometer;

    EditText edTruckNum;
    EditText edTrailerNum;
    EditText edComments;

    CheckBox switchDefect;
    CheckBox switchDefectRepaired;
    CheckBox switchSafeToDrive;


    //Button butCertify;
    //Button butClose;
    ImageButton fabCertify;
    HorizontalScrollView horizontalScrollView;
    TableLayout tableDefects;

    TextView tvDefectItemsLabel;
    TextView tvDefectItems;
    Button butAddDefect;
    Button butAddPiture;

    LinearLayout layoutImages;

    double latitude;
    double longitude;

    int defect;
    int defectRepaired;
    int safeToDrive;
    String defectItems;

    String certifyMessage;
    boolean viewMode;

    String location = "";

    int count = 0;
    String imageFile;
    List<String> listImages;
    Bitmap scaleBitmap = null;
    RadioGroup rbgType;

    //Toolbar toolbar;
    DefectSelectionDialog defectSelectionDialog;
    ViewImageDialog viewImageDialog;
    int countCurrentLocationFailed;
    String currentDatetime;
    GeocodeTask.PostTaskListener<Address> geocodeTaskListener = new GeocodeTask.PostTaskListener<Address>() {
        @Override
        public void onPostTask(Address address) {
            if (address == null) {
                countCurrentLocationFailed++;
                if (countCurrentLocationFailed < 5) {
                    callGeocodeTask();
                }
            } else {
                String addressName = "";

                String add = address.getAddressLine(0);
                String city = address.getLocality();
                String state = address.getAdminArea();
                String country = address.getCountryName();
                String postalCode = address.getPostalCode();
                addressName = add + ", " + city + ", " + state + ", " + country + " " + postalCode;

                tvLocation.setText(addressName);
            }
        }
    };


    public NewInspectionFragment() {

    }

    int inspectionType = 0;

    private void initialize(View view) {
        try {
            rbgType = (RadioGroup) view.findViewById(R.id.rbgType);
            rbgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.rbPre:
                            inspectionType = 0;
                            break;

                        case R.id.rbInter:
                            inspectionType = 1;
                            break;

                        case R.id.rbPost:
                            inspectionType = 2;
                            break;
                    }
                }
            });
            tvDateTime = (TextView) view.findViewById(R.id.tvDateTime);
            tvLocation = (TextView) view.findViewById(R.id.tvLocation);
            tvOdometer = (TextView) view.findViewById(R.id.tvOdometer);

            edTruckNum = (EditText) view.findViewById(R.id.edTruckValue);
            edTrailerNum = (EditText) view.findViewById(R.id.edTrailerValue);
            edComments = (EditText) view.findViewById(R.id.edComments);

            fabCertify = (ImageButton) view.findViewById(R.id.fabInspectionCertify);
            fabCertify.setOnClickListener(this);

            butAddDefect = (Button) view.findViewById(R.id.butAddDefect);
            butAddDefect.setOnClickListener(this);
            butAddPiture = (Button) view.findViewById(R.id.butAddPicture);
            butAddPiture.setOnClickListener(this);

            switchDefect = (CheckBox) view.findViewById(R.id.switchDefect);
            switchDefect.setOnCheckedChangeListener(this);
            switchDefectRepaired = (CheckBox) view.findViewById(R.id.switchDefectRepaired);
            switchDefectRepaired.setOnCheckedChangeListener(this);
            switchSafeToDrive = (CheckBox) view.findViewById(R.id.switchSafeToDrive);
            switchSafeToDrive.setOnCheckedChangeListener(this);

            tvDefectItemsLabel = (TextView) view.findViewById(R.id.tvDefectedItemsLabel);
            tvDefectItems = (TextView) view.findViewById(R.id.tvDefectedItems);

            layoutImages = (LinearLayout) view.findViewById(R.id.linear);
            defect = 0;
            defectRepaired = 0;
            safeToDrive = 0;

            horizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.horizontal_scroll);
            horizontalScrollView.setVisibility(View.GONE);

            tableDefects = (TableLayout) view.findViewById(R.id.tableDefects);
            tableDefects.setVisibility(View.GONE);

            if (viewMode) {
                edTruckNum.setEnabled(false);
                edTrailerNum.setEnabled(false);
                for (View v : rbgType.getTouchables()) {
                    v.setEnabled(false);
                }

                edComments.setEnabled(false);

                switchDefect.setEnabled(false);
                switchDefectRepaired.setEnabled(false);
                switchSafeToDrive.setEnabled(false);

                butAddDefect.setVisibility(View.GONE);
                butAddPiture.setVisibility(View.GONE);

                //butCertify.setVisibility(View.GONE);
                //butClose.setVisibility(View.VISIBLE);
                fabCertify.setVisibility(View.GONE);

                //actionBar.setTitle("Inspection");
            }

            listImages = new ArrayList<String>();

            countCurrentLocationFailed = 0;
            //toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            //.setSupportActionBar(toolbar);
        } catch (Exception e) {
            LogFile.write(NewInspectionFragment.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public static NewInspectionFragment newInstance() {
        return new NewInspectionFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_new_inspection, container, false);

        defectItems = "";

        viewMode = false;
        if (getArguments() != null) {
            if (getArguments().getBoolean("view_mode", false)) {
                viewMode = true;
            }
        }

        initialize(view);
        String format = CustomDateFormat.dt5; //12hr
        if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
            format = CustomDateFormat.dt6;
        }

        if (!viewMode) {
            currentDatetime = Utility.getCurrentDateTime();


            String currentDate = Utility.convertDate(Utility.newDate(), format);
            tvDateTime.setText(currentDate);
            latitude = Utility.currentLocation.getLatitude();
            longitude = Utility.currentLocation.getLongitude();
            //tvLocation.setText(Utility.currentLocation.getLocationDescription());
            tvOdometer.setText(CanMessages.OdometerReading);
            String plateNo = Utility.PlateNo == null || Utility.PlateNo.isEmpty() ? "" : " (" + Utility.PlateNo + ")";
            edTruckNum.setText(Utility.UnitNo + plateNo);
            edTrailerNum.setText(Utility.TrailerNumber);

            callGeocodeTask();
        } else {
            TripInspectionBean bean = (TripInspectionBean) getArguments().getSerializable("trip_inspection");
            inspectionType = bean.getType();
            currentDatetime = bean.getInspectionDateTime();
            String tripDate = Utility.convertDate(bean.getInspectionDateTime(), format);
            tvDateTime.setText(tripDate);
            tvLocation.setText(bean.getLocation());
            tvOdometer.setText(bean.getOdometerReading());
            edTruckNum.setText(bean.getTruckNumber());
            edTrailerNum.setText(bean.getTrailerNumber());
            edComments.setText(bean.getComments());

            if (bean.getDefect() == 1) {
                switchDefect.setChecked(true);
                tvDefectItems.setVisibility(View.VISIBLE);
                tvDefectItemsLabel.setVisibility(View.VISIBLE);
                switchDefectRepaired.setVisibility(View.VISIBLE);
            } else {
                switchDefect.setChecked(false);
            }

            if (bean.getDefectRepaired() == 1) {
                switchDefectRepaired.setChecked(true);
            } else {
                switchDefectRepaired.setChecked(false);
                if (bean.getDefect() == 1) {
                    switchSafeToDrive.setVisibility(View.VISIBLE);
                }
            }

            if (bean.getSafeToDrive() == 1) {
                switchSafeToDrive.setChecked(true);
            } else {
                switchSafeToDrive.setChecked(false);
            }

            defectItems = bean.getDefectItems();
            if (defectItems != null && defectItems.length() > 0) {
                String[] index = defectItems.split(",");
                String text = "";
                for (int i = 0; i < index.length; i++) {
                    int idx = Integer.valueOf(index[i]);
                    text += getResources().getStringArray(R.array.defect_items)[idx];
                    if (i < index.length - 1) {
                        text += ", ";
                    }
                }
                tableDefects.setVisibility(View.VISIBLE);
                tvDefectItemsLabel.setVisibility(View.VISIBLE);
                tvDefectItems.setVisibility(View.VISIBLE);
                tvDefectItems.setText(text);
            }

            if (!bean.getPictures().equals("")) {
                horizontalScrollView.setVisibility(View.VISIBLE);
                String[] images = bean.getPictures().split(",");
                if (images.length > 0) {
                    for (int i = 0; i < images.length; i++) {
                        listImages.add(images[i]);

                        scaleBitmap = BitmapUtility.decodeSampledBitmapFromFile(images[i], 128, 72);

                        final ImageView imageView = new ImageView(getActivity());
                        imageView.setId(listImages.size() - 1);
                        imageView.setPadding(2, 2, 2, 2);
                        imageView.setImageBitmap(scaleBitmap);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (viewImageDialog == null) {
                                    viewImageDialog = new ViewImageDialog();
                                }
                                if (viewImageDialog.isAdded()) {
                                    viewImageDialog.dismiss();
                                }
                                viewImageDialog.setImagePath(listImages.get(imageView.getId()));
                                viewImageDialog.show(getFragmentManager(), "viewImage_dialog");
                            }
                        });

                        layoutImages.addView(imageView);
                    }
                    System.gc();
                }
            }
        }

        ((RadioButton) rbgType.getChildAt(inspectionType)).setChecked(true);

        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
      /*  try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup viewGroup = (ViewGroup) getView();
            View view = inflater.inflate(R.layout.activity_new_inspection, viewGroup, false);
            viewGroup.removeAllViews();
            viewGroup.addView(view);
            initialize(view);
        } catch (Exception exe) {
        }*/
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (viewMode)
            return;
        switch (buttonView.getId()) {
            case R.id.switchDefect:
                if (isChecked) {
                    defect = 1;

                    switchDefectRepaired.setVisibility(View.VISIBLE);
                    switchSafeToDrive.setVisibility(View.VISIBLE);

                    butAddDefect.setVisibility(View.VISIBLE);
                    butAddPiture.setVisibility(View.VISIBLE);

                    //butCertify.setEnabled(false);
                    fabCertify.setEnabled(false);
                    certifyMessage = "";
                } else {
                    defect = 0;
                    switchDefectRepaired.setVisibility(View.GONE);

                    switchSafeToDrive.setVisibility(View.GONE);

                    //lvDefectItems.setVisibility(View.INVISIBLE);
                    butAddDefect.setVisibility(View.GONE);
                    butAddPiture.setVisibility(View.GONE);
                    tvDefectItemsLabel.setVisibility(View.GONE);
                    tvDefectItems.setVisibility(View.GONE);

                    //butCertify.setEnabled(true);
                    fabCertify.setEnabled(true);
                }
                break;
            case R.id.switchDefectRepaired:
                if (isChecked) {
                    defectRepaired = 1;
                    certifyMessage = getResources().getString(R.string.repaired_msg);

                    switchSafeToDrive.setVisibility(View.GONE);
                } else {
                    defectRepaired = 0;
                    certifyMessage = "";

                    switchSafeToDrive.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.switchSafeToDrive:
                if (isChecked) {
                    safeToDrive = 1;
                    certifyMessage = getResources().getString(R.string.safetodrive_msg);
                } else {
                    safeToDrive = 0;
                    certifyMessage = getResources().getString(R.string.no_safetodrive_msg);
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.butAddDefect:
                    if (defectSelectionDialog == null) {
                        defectSelectionDialog = new DefectSelectionDialog();
                    }
                    if (defectSelectionDialog.isAdded()) {
                        defectSelectionDialog.dismiss();
                    }
                    defectSelectionDialog.mListener = this;
                    defectSelectionDialog.setItems(defectItems);
                    defectSelectionDialog.show(getFragmentManager(), "defectSelection_dialog");
                    break;

                case R.id.butAddPicture:
                    final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/hutch/";
                    File newdir = new File(dir);
                    if (!newdir.exists()) {
                        newdir.mkdirs();
                    }
                    final String path = dir + Utility.getCurrentDate() + "/";
                    File currentDir = new File(path);
                    currentDir.mkdirs();

                    // Here, the counter will be incremented each time, and the
                    // picture taken by camera will be stored as 1.jpg,2.jpg
                    // and likewise.
                    count++;
                    imageFile = path + Utility.getStringTime(Utility.getCurrentDateTime()) + ".jpg";
                    File newfile = new File(imageFile);
                    try {
                        newfile.createNewFile();

                    } catch (IOException e) {
                    }

                    //call to stop AutoStartService because it will launch another activity,
                    //that means our application will go to background

                    AutoStartService.pauseTask = true;
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        Uri photoURI = Uri.fromFile(newfile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
                    }
                    break;


                //case R.id.butCertify:
                case R.id.fabInspectionCertify:
                    showCertifyMessage();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(NewEventFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            if (requestCode == DEFECT_SELECTION_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    defectItems = intent.getStringExtra("selected_items");
                    if (defectItems.length() > 0) {
                        String[] index = defectItems.split(",");
                        String text = "";
                        for (int i = 0; i < index.length; i++) {
                            int idx = Integer.valueOf(index[i]);
                            text += getResources().getStringArray(R.array.defect_items)[idx];
                            if (i < index.length - 1) {
                                text += ", ";
                            }
                        }
                        tableDefects.setVisibility(View.VISIBLE);
                        tvDefectItemsLabel.setVisibility(View.VISIBLE);
                        tvDefectItems.setVisibility(View.VISIBLE);
                        tvDefectItems.setText(text);

                        //butCertify.setEnabled(true);
                        fabCertify.setEnabled(true);
                    }
                }
            } else if (requestCode == TAKE_PHOTO_CODE && resultCode == Activity.RESULT_OK) {

                BitmapUtility.compressAndSaveBitmap(imageFile);
                listImages.add(imageFile);

                scaleBitmap = BitmapUtility.decodeSampledBitmapFromFile(imageFile, 128, 72);


                final ImageView imageView = new ImageView(getActivity());
                imageView.setId(listImages.size() - 1);
                imageView.setPadding(2, 2, 2, 2);
                imageView.setImageBitmap(scaleBitmap);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewImageDialog == null) {
                            viewImageDialog = new ViewImageDialog();
                        }
                        if (viewImageDialog.isAdded()) {
                            viewImageDialog.dismiss();
                        }
                        viewImageDialog.setImagePath(listImages.get(imageView.getId()));
                        viewImageDialog.show(getFragmentManager(), "viewImage_dialog");
                    }
                });
                //add delete button for current captured image
                Button butDelete = new Button(getActivity());
                butDelete.setText("Delete");
                butDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String image = listImages.get(imageView.getId());
                        listImages.remove(image);
                        layoutImages.removeViewAt(imageView.getId());
                    }
                });
                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(imageView);
                layout.addView(butDelete);
                layoutImages.addView(layout);
                //layoutImages.addView(imageView);
                horizontalScrollView.setVisibility(View.VISIBLE);
            } else if (requestCode == VIEW_PHOTO && resultCode == Activity.RESULT_OK) {

            }
        } catch (Exception e) {
            LogFile.write(NewInspectionFragment.class.getName() + "::onActivityResult Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
        AutoStartService.pauseTask = false;
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
        if (switchDefect == null)
            return;

        switchDefect.setOnCheckedChangeListener(null);

        switchDefectRepaired.setOnCheckedChangeListener(null);

        switchSafeToDrive.setOnCheckedChangeListener(null);

        //butBack.setOnClickListener(null);
        if (defectSelectionDialog != null) {
            defectSelectionDialog.mListener = null;
        }
        fabCertify.setOnClickListener(null);


        butAddDefect.setOnClickListener(null);
        butAddPiture.setOnClickListener(null);
    }

    public void showCertifyMessage() {
        try {
            certifyMessage = "";
            if (defect == 0) {
                certifyMessage = getResources().getString(R.string.defect_msg);
            } else {
                if (defectRepaired == 1) {
                    certifyMessage = getResources().getString(R.string.repaired_msg);
                } else {
                    if (safeToDrive == 1) {
                        certifyMessage = getResources().getString(R.string.safetodrive_msg);
                    } else {
                        certifyMessage = getResources().getString(R.string.no_safetodrive_msg);
                    }
                }
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(certifyMessage)
                    .setTitle("E-Log")
                    .setIcon(R.drawable.ic_launcher)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            //save to DB
                            int driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
                            String driverName = Utility.user1.isOnScreenFg() ? (Utility.user1.getFirstName() + " " + Utility.user1.getLastName()) : (Utility.user2.getFirstName() + " " + Utility.user2.getLastName());

                            String pictures = "";
                            for (int i = 0; i < listImages.size(); i++) {
                                pictures += listImages.get(i);
                                if (i < listImages.size() - 1) {
                                    pictures += ",";
                                }
                            }

                            TripInspectionDB.CreateTripInspection(currentDatetime, driverId, driverName, inspectionType, defect, defectRepaired, safeToDrive,
                                    defectItems, Double.toString(latitude), Double.toString(longitude), tvLocation.getText().toString(),
                                    tvOdometer.getText().toString(), edTruckNum.getText().toString(), edTrailerNum.getText().toString(), edComments.getText().toString(), pictures);
                            dialog.cancel();
                            if (mListener != null) {
                                mListener.finishInspection();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            LogFile.write(NewInspectionFragment.class.getName() + "::showCertifyMessage Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void setSelectedItems(String selectedIndices) {
        defectItems = selectedIndices;
        if (defectItems.length() > 0) {
            String[] index = defectItems.split(",");
            String text = "";
            for (int i = 0; i < index.length; i++) {
                int idx = Integer.valueOf(index[i]);
                text += getResources().getStringArray(R.array.defect_items)[idx];
                if (i < index.length - 1) {
                    text += ", ";
                }
            }
            tableDefects.setVisibility(View.VISIBLE);
            tvDefectItemsLabel.setVisibility(View.VISIBLE);
            tvDefectItems.setVisibility(View.VISIBLE);
            tvDefectItems.setText(text);

            //butCertify.setEnabled(true);
            fabCertify.setEnabled(true);
        }
    }

    private void callGeocodeTask() {
        //Log.i(TAG, "callGeocodeTask " + countCurrentLocationFailed);
        new GeocodeTask(geocodeTaskListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void finishInspection();

        void stopService();
    }
}
