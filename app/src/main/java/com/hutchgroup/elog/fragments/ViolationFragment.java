package com.hutchgroup.elog.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.beans.ViolationBean;
import com.hutchgroup.elog.bll.HourOfService;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ViolationFragment extends Fragment {
    static ViolationFragment mInstance = null;

    public int selectedItemIndex = -1;
    ListView lvData;
    ArrayList<ViolationBean> violationList;
    ViolationAdapter adapter;
    int driverId = 0;

    public ViolationFragment() {
        // Required empty public constructor
    }

    private void initialize(View view) {
        driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        lvData = (ListView) view.findViewById(R.id.lvData);
        ViolationBind();
    }

    private void ViolationBind() {
        selectedItemIndex = -1;
        HourOfService.ViolationCalculation(Utility.newDate(), driverId);
        //tempViolation();
        violationList = HourOfService.violations;


        adapter = new ViolationAdapter(R.layout.violation_row_layout, violationList);
        lvData.setAdapter(adapter);
    }
/*
    private void tempViolation() {
        try {
            HourOfService.ViolationAdd("12(A)", Utility.parse("2016-01-11 09:20:00"), 180, true);
            HourOfService.ViolationAdd("13(B)", Utility.parse("2016-01-12 13:37:00"), 30, true);
            HourOfService.ViolationAdd("14(C)", Utility.parse("2016-01-12 18:43:00"), 75, true);
            HourOfService.ViolationAdd("16(A)", Utility.parse("2016-01-15 23:01:00"), 69, true);
            HourOfService.ViolationAdd("26(B)", Utility.parse("2016-01-19 07:29:00"), 256, true);
            HourOfService.ViolationAdd("25(A)", Utility.parse("2016-01-21 05:15:00"), 300, true);
        } catch (Exception exe) {
        }
    }*/

    public static ViolationFragment newInstance() {
        return new ViolationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_violation, container, false);
        initialize(view);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Created by Deepak.Sharma on 1/15/2016.
     */
    public class ViolationAdapter extends ArrayAdapter<ViolationBean> {

        ArrayList<ViolationBean> data;

        public ViolationAdapter(int resource,
                                ArrayList<ViolationBean> data) {
            super(Utility.context, resource, data);
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ViewHolderItem viewHolder;
            if (convertView == null || convertView.getTag() == null) {

                LayoutInflater inflater = (LayoutInflater) Utility.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(
                        R.layout.violation_row_layout, parent,
                        false);
                viewHolder = new ViewHolderItem();
                viewHolder.tvSerialNo = (TextView) convertView.findViewById(R.id.tvSerialNo);
                viewHolder.tvViolationDateTime = (TextView) convertView.findViewById(R.id.tvViolationDateTime);
                viewHolder.tvViolationDescription = (TextView) convertView.findViewById(R.id.tvViolationDescription);
                viewHolder.tvViolationCode = (TextView) convertView.findViewById(R.id.tvViolationCode);
            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            ViolationBean bean = data.get(position);
            viewHolder.tvSerialNo.setText((position + 1) + "");
            viewHolder.tvViolationDateTime.setText(Utility.ConverDateFormat(bean.getStartTime()));
            viewHolder.tvViolationCode.setText(bean.getRule());
            viewHolder.tvViolationDescription.setText("Total Hour(s): " + Utility.getTimeFromMinute(bean.getTotalMinutes()));


            final String vTitle = bean.getTitle();
            final String vExplanation = bean.getExplanation();

            try {
                String format = "hh:mm a"; //12hr
                if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                    format = "HH:mm";
                }

                String datetime = new SimpleDateFormat(format).format(bean.getStartTime()) + "\n" + new SimpleDateFormat("MMM dd,yyyy").format(bean.getStartTime());
                viewHolder.tvViolationDateTime.setText(datetime);
            } catch (Exception exe) {

            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
                        alertDialog.setCancelable(true);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setTitle(vTitle);
                        alertDialog.setIcon(Utility.DIALOGBOX_ICON);
                        alertDialog.setMessage(vExplanation);
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
            return convertView;
        }


    }

    class ViewHolderItem {
        TextView tvViolationDateTime, tvViolationDescription, tvViolationCode, tvSerialNo;
    }
}
