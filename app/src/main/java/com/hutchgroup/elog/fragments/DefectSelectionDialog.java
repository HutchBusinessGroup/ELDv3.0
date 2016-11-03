package com.hutchgroup.elog.fragments;


import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

public class DefectSelectionDialog extends DialogFragment implements View.OnClickListener{

    public DefectSelectionDialogInterface mListener;

    CheckBox chBox1;
    CheckBox chBox2;
    CheckBox chBox3;
    CheckBox chBox4;
    CheckBox chBox5;
    CheckBox chBox6;
    CheckBox chBox7;
    CheckBox chBox8;
    CheckBox chBox9;
    CheckBox chBox10;
    CheckBox chBox11;
    CheckBox chBox12;
    CheckBox chBox13;
    CheckBox chBox14;
    CheckBox chBox15;
    CheckBox chBox16;
    CheckBox chBox17;
    CheckBox chBox18;
    CheckBox chBox19;
    CheckBox chBox20;
    CheckBox chBox21;
    CheckBox chBox22;
    CheckBox chBox23;
    CheckBox chBox24;

    LinearLayout layout1;
    LinearLayout layout2;
    LinearLayout layout3;
    LinearLayout layout4;
    LinearLayout layout5;
    LinearLayout layout6;
    LinearLayout layout7;
    LinearLayout layout8;
    LinearLayout layout9;
    LinearLayout layout10;
    LinearLayout layout11;
    LinearLayout layout12;
    LinearLayout layout13;
    LinearLayout layout14;
    LinearLayout layout15;
    LinearLayout layout16;
    LinearLayout layout17;
    LinearLayout layout18;
    LinearLayout layout19;
    LinearLayout layout20;
    LinearLayout layout21;
    LinearLayout layout22;
    LinearLayout layout23;
    LinearLayout layout24;

    Button butOK;
    ImageButton imgCancel;

    String items = "";

    public DefectSelectionDialog(){

    }

    public void setItems(String detectItems) {
        items = detectItems;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_defect_selection, container);
        try {

            initialize(view);
            //getDialog().setTitle("Select Defect(s)");
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            this.setCancelable(false);


        } catch (Exception e) {
            LogFile.write(DefectSelectionDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {

        } catch (Exception e) {
            LogFile.write(DefectSelectionDialog.class.getName() + "::onActivityCreated Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width =WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    private void initialize(View view) {
        try {
            chBox1 = (CheckBox) view.findViewById(R.id.chBox1);
            chBox2 = (CheckBox) view.findViewById(R.id.chBox2);
            chBox3 = (CheckBox) view.findViewById(R.id.chBox3);
            chBox4 = (CheckBox) view.findViewById(R.id.chBox4);
            chBox5 = (CheckBox) view.findViewById(R.id.chBox5);
            chBox6 = (CheckBox) view.findViewById(R.id.chBox6);
            chBox7 = (CheckBox) view.findViewById(R.id.chBox7);
            chBox8 = (CheckBox) view.findViewById(R.id.chBox8);
            chBox9 = (CheckBox) view.findViewById(R.id.chBox9);
            chBox10 = (CheckBox) view.findViewById(R.id.chBox10);
            chBox11 = (CheckBox) view.findViewById(R.id.chBox11);
            chBox12 = (CheckBox) view.findViewById(R.id.chBox12);
            chBox13 = (CheckBox) view.findViewById(R.id.chBox13);
            chBox14 = (CheckBox) view.findViewById(R.id.chBox14);
            chBox15 = (CheckBox) view.findViewById(R.id.chBox15);
            chBox16 = (CheckBox) view.findViewById(R.id.chBox16);
            chBox17 = (CheckBox) view.findViewById(R.id.chBox17);
            chBox18 = (CheckBox) view.findViewById(R.id.chBox18);
            chBox19 = (CheckBox) view.findViewById(R.id.chBox19);
            chBox20 = (CheckBox) view.findViewById(R.id.chBox20);
            chBox21 = (CheckBox) view.findViewById(R.id.chBox21);
            chBox22 = (CheckBox) view.findViewById(R.id.chBox22);
            chBox23 = (CheckBox) view.findViewById(R.id.chBox23);
            chBox24 = (CheckBox) view.findViewById(R.id.chBox24);

            layout1 = (LinearLayout) view.findViewById(R.id.layout1);
            layout2 = (LinearLayout) view.findViewById(R.id.layout2);
            layout3 = (LinearLayout) view.findViewById(R.id.layout3);
            layout4 = (LinearLayout) view.findViewById(R.id.layout4);
            layout5 = (LinearLayout) view.findViewById(R.id.layout5);
            layout6 = (LinearLayout) view.findViewById(R.id.layout6);
            layout7 = (LinearLayout) view.findViewById(R.id.layout7);
            layout8 = (LinearLayout) view.findViewById(R.id.layout8);
            layout9 = (LinearLayout) view.findViewById(R.id.layout9);
            layout10 = (LinearLayout) view.findViewById(R.id.layout10);
            layout11 = (LinearLayout) view.findViewById(R.id.layout11);
            layout12 = (LinearLayout) view.findViewById(R.id.layout12);
            layout13 = (LinearLayout) view.findViewById(R.id.layout13);
            layout14 = (LinearLayout) view.findViewById(R.id.layout14);
            layout15 = (LinearLayout) view.findViewById(R.id.layout15);
            layout16 = (LinearLayout) view.findViewById(R.id.layout16);
            layout17 = (LinearLayout) view.findViewById(R.id.layout17);
            layout18 = (LinearLayout) view.findViewById(R.id.layout18);
            layout19 = (LinearLayout) view.findViewById(R.id.layout19);
            layout20 = (LinearLayout) view.findViewById(R.id.layout20);
            layout21 = (LinearLayout) view.findViewById(R.id.layout21);
            layout22 = (LinearLayout) view.findViewById(R.id.layout22);
            layout23 = (LinearLayout) view.findViewById(R.id.layout23);
            layout24 = (LinearLayout) view.findViewById(R.id.layout24);

            layout1.setOnClickListener(this);
            layout2.setOnClickListener(this);
            layout3.setOnClickListener(this);
            layout4.setOnClickListener(this);
            layout5.setOnClickListener(this);
            layout6.setOnClickListener(this);
            layout7.setOnClickListener(this);
            layout8.setOnClickListener(this);
            layout9.setOnClickListener(this);
            layout10.setOnClickListener(this);
            layout11.setOnClickListener(this);
            layout12.setOnClickListener(this);
            layout13.setOnClickListener(this);
            layout14.setOnClickListener(this);
            layout15.setOnClickListener(this);
            layout16.setOnClickListener(this);
            layout17.setOnClickListener(this);
            layout18.setOnClickListener(this);
            layout19.setOnClickListener(this);
            layout20.setOnClickListener(this);
            layout21.setOnClickListener(this);
            layout22.setOnClickListener(this);
            layout23.setOnClickListener(this);
            layout24.setOnClickListener(this);


            butOK = (Button) view.findViewById(R.id.butOK);
            butOK.setOnClickListener(this);
            imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(this);

            if (!items.equals("")) {
                String[] indices = items.split(",");
                if (indices.length > 0) {
                    for (int i = 0; i < indices.length; i++) {
                        updateSelectedItem(Integer.valueOf(indices[i]));
                    }
                }
            }
        } catch (Exception e) {
            LogFile.write(RuleChangeDialog.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onClick(View view) {
        try {
            CheckBox chBox = null;

            switch (view.getId()) {
                case R.id.imgCancel:
                    mListener = null;
                    Utility.hideKeyboard(getActivity(), view);
                    dismiss();
                    break;
                case R.id.butOK:
                    String selectedIndices = "";
                    if (chBox1.isChecked()) {
                        selectedIndices += "0";
                    }
                    if (chBox2.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "1";
                    }
                    if (chBox3.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "2";
                    }
                    if (chBox4.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "3";
                    }
                    if (chBox5.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "4";
                    }
                    if (chBox6.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "5";
                    }
                    if (chBox7.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "6";
                    }
                    if (chBox8.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "7";
                    }
                    if (chBox9.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "8";
                    }
                    if (chBox10.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "9";
                    }
                    if (chBox11.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "10";
                    }
                    if (chBox12.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "11";
                    }
                    if (chBox13.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "12";
                    }
                    if (chBox14.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "13";
                    }
                    if (chBox15.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "14";
                    }
                    if (chBox16.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "15";
                    }
                    if (chBox17.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "16";
                    }
                    if (chBox18.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "17";
                    }
                    if (chBox19.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "18";
                    }
                    if (chBox20.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "19";
                    }
                    if (chBox21.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "20";
                    }
                    if (chBox22.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "21";
                    }
                    if (chBox23.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "22";
                    }

                    if (chBox24.isChecked()) {
                        if (selectedIndices.length() > 0)
                            selectedIndices += ",";
                        selectedIndices += "23";
                    }
                    Intent intent = new Intent();
                    intent.putExtra("selected_items", selectedIndices);

                    if (mListener != null) {
                        mListener.setSelectedItems(selectedIndices);
                    }
                    mListener = null;
                    dismiss();
                    break;
                case R.id.layout1:
                    chBox = chBox1;
                    break;
                case R.id.layout2:
                    chBox = chBox2;
                    break;
                case R.id.layout3:
                    chBox = chBox3;
                    break;
                case R.id.layout4:
                    chBox = chBox4;
                    break;
                case R.id.layout5:
                    chBox = chBox5;
                    break;
                case R.id.layout6:
                    chBox = chBox6;
                    break;
                case R.id.layout7:
                    chBox = chBox7;
                    break;
                case R.id.layout8:
                    chBox = chBox8;
                    break;
                case R.id.layout9:
                    chBox = chBox9;
                    break;
                case R.id.layout10:
                    chBox = chBox10;
                    break;
                case R.id.layout11:
                    chBox = chBox11;
                    break;
                case R.id.layout12:
                    chBox = chBox12;
                    break;
                case R.id.layout13:
                    chBox = chBox13;
                    break;
                case R.id.layout14:
                    chBox = chBox14;
                    break;
                case R.id.layout15:
                    chBox = chBox15;
                    break;
                case R.id.layout16:
                    chBox = chBox16;
                    break;
                case R.id.layout17:
                    chBox = chBox17;
                    break;
                case R.id.layout18:
                    chBox = chBox18;
                    break;
                case R.id.layout19:
                    chBox = chBox19;
                    break;
                case R.id.layout20:
                    chBox = chBox20;
                    break;
                case R.id.layout21:
                    chBox = chBox21;
                    break;
                case R.id.layout22:
                    chBox = chBox22;
                    break;
                case R.id.layout23:
                    chBox = chBox23;
                    break;
                case R.id.layout24:
                    chBox = chBox24;
                    break;
            }

            if (chBox != null) {
                chBox.setChecked(!chBox.isChecked());
            }
        } catch (Exception e) {
            LogFile.write(DefectSelectionDialog.class.getName() + "::onClick Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void updateSelectedItem(int idx) {
        switch (idx) {
            case 0:
                chBox1.setChecked(true);
                break;
            case 1:
                chBox2.setChecked(true);
                break;
            case 2:
                chBox3.setChecked(true);
                break;
            case 3:
                chBox4.setChecked(true);
                break;
            case 4:
                chBox5.setChecked(true);
                break;
            case 5:
                chBox6.setChecked(true);
                break;
            case 6:
                chBox7.setChecked(true);
                break;
            case 7:
                chBox8.setChecked(true);
                break;
            case 8:
                chBox9.setChecked(true);
                break;
            case 9:
                chBox10.setChecked(true);
                break;
            case 10:
                chBox11.setChecked(true);
                break;
            case 11:
                chBox12.setChecked(true);
                break;
            case 12:
                chBox13.setChecked(true);
                break;
            case 13:
                chBox14.setChecked(true);
                break;
            case 14:
                chBox15.setChecked(true);
                break;
            case 15:
                chBox16.setChecked(true);
                break;
            case 16:
                chBox17.setChecked(true);
                break;
            case 17:
                chBox18.setChecked(true);
                break;
            case 18:
                chBox19.setChecked(true);
                break;
            case 20:
                chBox21.setChecked(true);
                break;
            case 21:
                chBox22.setChecked(true);
                break;
            case 22:
                chBox23.setChecked(true);
                break;
            case 23:
                chBox24.setChecked(true);
                break;
        }
    }

    public interface DefectSelectionDialogInterface {
        void setSelectedItems(String selectedIndices);
    }
}
