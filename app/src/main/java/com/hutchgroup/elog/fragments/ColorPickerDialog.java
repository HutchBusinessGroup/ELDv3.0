package com.hutchgroup.elog.fragments;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.OutputFile;
import com.hutchgroup.elog.common.StorageDirectory;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class ColorPickerDialog extends DialogFragment implements View.OnClickListener{

    public ColorPickerInterface mListener;

    Button butColor1;
    Button butColor2;
    Button butColor3;
    Button butColor4;
    Button butColor5;
    Button butColor6;
    Button butColor7;
    Button butColor8;
    Button butColor9;
    Button butColor10;
    Button butColor11;
    Button butColor12;
    Button butColor13;
    Button butColor14;
    Button butColor15;
    Button butColor16;
    Button butColor17;
    Button butColor18;
    Button butColor19;
    Button butColor20;
    Button butColor21;
    Button butColor22;
    Button butColor23;
    Button butColor24;
    Button butSelectedColor;

    ImageButton imgCancel;
    Button butSave;

    int colorType; //0: canada, 1: us
    int initializeColor;
    int selectedColor;
    int selectedIndex;
    private final int[] initializeColors;

    public ColorPickerDialog() {
        initializeColors = new int[] {
                0xFFFF8080, 0xFFFFFF80, 0xFF80FF80, 0xFF00FF80, 0xFF80FFFF, 0xFF0080FF, 0xFFFF80C0, 0xFFFF80FF,
                0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FF40, 0xFF00FFFF, 0xFF8080FF, 0xFF8080C0, 0xFFFF00FF,
                0xFF804040, 0xFFFF8040, 0xFF008000, 0xFF008040, 0xFF0000FF, 0xFF0080C0, 0xFF800040, 0xFFFF0080
        };
    }

    public void setColorType(int type) {
        colorType = type;
    }

    public void setInitialColor(int initialColor){
        this.initializeColor = initialColor;
        this.selectedColor = initialColor;
        selectedIndex = 0;
        for (int i = 0; i < initializeColors.length; i++) {
            if (selectedColor == initializeColors[i]) {
                selectedIndex = i;
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_color_picker, container);
        try {


            initialize(view);
            //getDialog().setTitle("E-Log");
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            this.setCancelable(false);


        } catch (Exception e) {
            LogFile.write(PopupDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {

        } catch (Exception e) {
            LogFile.write(PopupDialog.class.getName() + "::onActivityCreated Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onResume() {
//        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
//        params.width =WindowManager.LayoutParams.MATCH_PARENT;
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    private void initialize(View view) {
        try {
            butColor1 = (Button) view.findViewById(R.id.butColor1);
            butColor1.setOnClickListener(this);
            butColor2 = (Button) view.findViewById(R.id.butColor2);
            butColor2.setOnClickListener(this);
            butColor3 = (Button) view.findViewById(R.id.butColor3);
            butColor3.setOnClickListener(this);
            butColor4 = (Button) view.findViewById(R.id.butColor4);
            butColor4.setOnClickListener(this);
            butColor5 = (Button) view.findViewById(R.id.butColor5);
            butColor5.setOnClickListener(this);
            butColor6 = (Button) view.findViewById(R.id.butColor6);
            butColor6.setOnClickListener(this);
            butColor7 = (Button) view.findViewById(R.id.butColor7);
            butColor7.setOnClickListener(this);
            butColor8 = (Button) view.findViewById(R.id.butColor8);
            butColor8.setOnClickListener(this);
            butColor9 = (Button) view.findViewById(R.id.butColor9);
            butColor9.setOnClickListener(this);
            butColor10 = (Button) view.findViewById(R.id.butColor10);
            butColor10.setOnClickListener(this);
            butColor11 = (Button) view.findViewById(R.id.butColor11);
            butColor11.setOnClickListener(this);
            butColor12 = (Button) view.findViewById(R.id.butColor12);
            butColor12.setOnClickListener(this);
            butColor13 = (Button) view.findViewById(R.id.butColor13);
            butColor13.setOnClickListener(this);
            butColor14 = (Button) view.findViewById(R.id.butColor14);
            butColor14.setOnClickListener(this);
            butColor15 = (Button) view.findViewById(R.id.butColor15);
            butColor15.setOnClickListener(this);
            butColor16 = (Button) view.findViewById(R.id.butColor16);
            butColor16.setOnClickListener(this);
            butColor17 = (Button) view.findViewById(R.id.butColor17);
            butColor17.setOnClickListener(this);
            butColor18 = (Button) view.findViewById(R.id.butColor18);
            butColor18.setOnClickListener(this);
            butColor19 = (Button) view.findViewById(R.id.butColor19);
            butColor19.setOnClickListener(this);
            butColor20 = (Button) view.findViewById(R.id.butColor20);
            butColor20.setOnClickListener(this);
            butColor21 = (Button) view.findViewById(R.id.butColor21);
            butColor21.setOnClickListener(this);
            butColor22 = (Button) view.findViewById(R.id.butColor22);
            butColor22.setOnClickListener(this);
            butColor23 = (Button) view.findViewById(R.id.butColor23);
            butColor23.setOnClickListener(this);
            butColor24 = (Button) view.findViewById(R.id.butColor24);
            butColor24.setOnClickListener(this);
            imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(this);
            butSave = (Button) view.findViewById(R.id.butSave);
            butSave.setOnClickListener(this);
            butSelectedColor = (Button) view.findViewById(R.id.butSelectedColor);

            butSelectedColor.setBackgroundColor(selectedColor);
            butColor1.setBackgroundColor(initializeColors[0]);
            butColor2.setBackgroundColor(initializeColors[1]);
            butColor3.setBackgroundColor(initializeColors[2]);
            butColor4.setBackgroundColor(initializeColors[3]);
            butColor5.setBackgroundColor(initializeColors[4]);
            butColor6.setBackgroundColor(initializeColors[5]);
            butColor7.setBackgroundColor(initializeColors[6]);
            butColor8.setBackgroundColor(initializeColors[7]);
            butColor9.setBackgroundColor(initializeColors[8]);
            butColor10.setBackgroundColor(initializeColors[9]);
            butColor11.setBackgroundColor(initializeColors[10]);
            butColor12.setBackgroundColor(initializeColors[11]);
            butColor13.setBackgroundColor(initializeColors[12]);
            butColor14.setBackgroundColor(initializeColors[13]);
            butColor15.setBackgroundColor(initializeColors[14]);
            butColor16.setBackgroundColor(initializeColors[15]);
            butColor17.setBackgroundColor(initializeColors[16]);
            butColor18.setBackgroundColor(initializeColors[17]);
            butColor19.setBackgroundColor(initializeColors[18]);
            butColor20.setBackgroundColor(initializeColors[19]);
            butColor21.setBackgroundColor(initializeColors[20]);
            butColor22.setBackgroundColor(initializeColors[21]);
            butColor23.setBackgroundColor(initializeColors[22]);
            butColor24.setBackgroundColor(initializeColors[23]);
        } catch (Exception e) {
            LogFile.write(ColorPickerDialog.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.butColor1:
                    selectedIndex = 0;
                    setSelectedColor();
                    break;
                case R.id.butColor2:
                    selectedIndex = 1;
                    setSelectedColor();
                    break;
                case R.id.butColor3:
                    selectedIndex = 2;
                    setSelectedColor();
                    break;
                case R.id.butColor4:
                    selectedIndex = 3;
                    setSelectedColor();
                    break;
                case R.id.butColor5:
                    selectedIndex = 4;
                    setSelectedColor();
                    break;
                case R.id.butColor6:
                    selectedIndex = 5;
                    setSelectedColor();
                    break;
                case R.id.butColor7:
                    selectedIndex = 6;
                    setSelectedColor();
                    break;
                case R.id.butColor8:
                    selectedIndex = 7;
                    setSelectedColor();
                    break;
                case R.id.butColor9:
                    selectedIndex = 8;
                    setSelectedColor();
                    break;
                case R.id.butColor10:
                    selectedIndex = 9;
                    setSelectedColor();
                    break;
                case R.id.butColor11:
                    selectedIndex = 10;
                    setSelectedColor();
                    break;
                case R.id.butColor12:
                    selectedIndex = 11;
                    setSelectedColor();
                    break;
                case R.id.butColor13:
                    selectedIndex = 12;
                    setSelectedColor();
                    break;
                case R.id.butColor14:
                    selectedIndex = 13;
                    setSelectedColor();
                    break;
                case R.id.butColor15:
                    selectedIndex = 14;
                    setSelectedColor();
                    break;
                case R.id.butColor16:
                    selectedIndex = 15;
                    setSelectedColor();
                    break;
                case R.id.butColor17:
                    selectedIndex = 16;
                    setSelectedColor();
                    break;
                case R.id.butColor18:
                    selectedIndex = 17;
                    setSelectedColor();
                    break;
                case R.id.butColor19:
                    selectedIndex = 18;
                    setSelectedColor();
                    break;
                case R.id.butColor20:
                    selectedIndex = 19;
                    setSelectedColor();
                    break;
                case R.id.butColor21:
                    selectedIndex = 20;
                    setSelectedColor();
                    break;
                case R.id.butColor22:
                    selectedIndex = 21;
                    setSelectedColor();
                    break;
                case R.id.butColor23:
                    selectedIndex = 22;
                    setSelectedColor();
                    break;
                case R.id.butColor24:
                    selectedIndex = 23;
                    setSelectedColor();
                    break;

                case R.id.imgCancel:
                    mListener = null;
                    dismiss();
                    break;
                case R.id.butSave:
                    if (mListener != null) {
                        mListener.colorChanged(selectedColor, colorType);
                    }
                    mListener = null;
                    dismiss();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(RuleChangeDialog.class.getName() + "::onClick Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void setSelectedColor() {
        selectedColor = initializeColors[selectedIndex];
        butSelectedColor.setBackgroundColor(selectedColor);
    }


    public interface ColorPickerInterface {
        void colorChanged(int color, int colorType);
    }
}
