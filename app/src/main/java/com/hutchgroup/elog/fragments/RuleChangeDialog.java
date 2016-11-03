package com.hutchgroup.elog.fragments;


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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.common.LogFile;

public class RuleChangeDialog extends DialogFragment implements View.OnClickListener{

    public RuleChangeDialogInterface mListener;
    private final int CANADA_RULE_1 = 1;
    private final int CANADA_RULE_2 = 2;
    private final int US_RULE_1 = 3;
    private final int US_RULE_2 = 4;

    ColorStateList ruleColorStateList;

    LinearLayout layoutCanadaRule1;
    LinearLayout layoutCanadaRule2;
    LinearLayout layoutUSRule1;
    LinearLayout layoutUSRule2;

    TextView tvCanadaRule1;
    TextView tvCanadaRule2;
    TextView tvUSRule1;
    TextView tvUSRule2;

    Button butSave;

    ImageButton imgCancel;
    int currentRule = 1;


    public RuleChangeDialog(){

    }

    public void setCurrentRule(int value) {
        currentRule = value;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_rule_change, container);
        try {
            ruleColorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_selected}, //selected
                            new int[]{android.R.attr.state_enabled}, //un-selected
                    },
                    new int[]{
                            ContextCompat.getColor(getContext(), R.color.white), //1
                            ContextCompat.getColor(getContext(), R.color.colorPrimary) //2
                    }
            );

            //Intent intent = getIntent();
            //currentRule = intent.getIntExtra("current_rule", 1);

            initialize(view);
            //getDialog().setTitle("Hutch ELD supported rules");
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            this.setCancelable(false);


        } catch (Exception e) {
            LogFile.write(RuleChangeDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {

        } catch (Exception e) {
            LogFile.write(RuleChangeDialog.class.getName() + "::onActivityCreated Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
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
            layoutCanadaRule1 = (LinearLayout) view.findViewById(R.id.layoutCanadaRule1);
            layoutCanadaRule1.setOnClickListener(this);
            layoutCanadaRule2 = (LinearLayout) view.findViewById(R.id.layoutCanadaRule2);
            layoutCanadaRule2.setOnClickListener(this);
            layoutUSRule1 = (LinearLayout) view.findViewById(R.id.layoutUSRule1);
            layoutUSRule1.setOnClickListener(this);
            //layoutUSRule2 = (LinearLayout) findViewById(R.id.layoutUSRule2);
            //layoutUSRule2.setOnClickListener(this);

            tvCanadaRule1 = (TextView) view.findViewById(R.id.tvCanadaRule1);
            tvCanadaRule2 = (TextView) view.findViewById(R.id.tvCanadaRule2);
            tvUSRule1 = (TextView) view.findViewById(R.id.tvUSRule1);
            //tvUSRule2 = (TextView) findViewById(R.id.tvUSRule2);

            imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
            imgCancel.setOnClickListener(this);
            butSave = (Button) view.findViewById(R.id.butRuleSave);
            butSave.setOnClickListener(this);

            butSave.setEnabled(false);

            clearRules();
            switch (currentRule) {
                case 1:
                    layoutCanadaRule1.setSelected(true);
                    tvCanadaRule1.setSelected(true);
                    break;
                case 2:
                    layoutCanadaRule2.setSelected(true);
                    tvCanadaRule2.setSelected(true);
                    break;
                case 3:
                    layoutUSRule1.setSelected(true);
                    tvUSRule1.setSelected(true);
                    break;
                case 4:
                    //layoutUSRule2.setSelected(true);
                    //tvUSRule2.setSelected(true);
                    break;
            }
        } catch (Exception e) {
            LogFile.write(RuleChangeDialog.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.layoutCanadaRule1:
                    if (!layoutCanadaRule1.isSelected()) {
                        clearRules();
                        layoutCanadaRule1.setSelected(true);
                        tvCanadaRule1.setSelected(true);
                        butSave.setEnabled(true);
                        currentRule = CANADA_RULE_1;
                    }
                    break;
                case R.id.layoutCanadaRule2:
                    if (!layoutCanadaRule2.isSelected()) {
                        clearRules();
                        layoutCanadaRule2.setSelected(true);
                        tvCanadaRule2.setSelected(true);
                        butSave.setEnabled(true);
                        currentRule = CANADA_RULE_2;
                    }
                    break;
                case R.id.layoutUSRule1:
                    if (!layoutUSRule1.isSelected()) {
                        clearRules();
                        layoutUSRule1.setSelected(true);
                        tvUSRule1.setSelected(true);
                        butSave.setEnabled(true);
                        currentRule = US_RULE_1;
                    }
                    break;
                case R.id.imgCancel:

                    dismiss();
                    break;
                case R.id.butRuleSave:
                    if (mListener != null) {
                        mListener.onSavedRule(currentRule);
                    }

                    dismiss();
                    break;
            }
        } catch (Exception e) {
            LogFile.write(RuleChangeDialog.class.getName() + "::onClick Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    private void clearRules() {
        try {
            layoutCanadaRule1.setSelected(false);
            layoutCanadaRule2.setSelected(false);
            layoutUSRule1.setSelected(false);
            //layoutUSRule2.setSelected(false);

            tvCanadaRule1.setSelected(false);
            tvCanadaRule2.setSelected(false);
            tvUSRule1.setSelected(false);
            //tvUSRule2.setSelected(false);

            tvCanadaRule1.setTextColor(ruleColorStateList);
            tvCanadaRule2.setTextColor(ruleColorStateList);
            tvUSRule1.setTextColor(ruleColorStateList);
            //tvUSRule2.setTextColor(ruleColorStateList);
        } catch (Exception e) {
            LogFile.write(RuleChangeDialog.class.getName() + "::clearRules Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public interface RuleChangeDialogInterface {
        void onSavedRule(int rule);
    }
}
