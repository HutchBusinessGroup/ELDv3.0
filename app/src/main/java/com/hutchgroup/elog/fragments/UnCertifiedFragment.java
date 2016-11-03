package com.hutchgroup.elog.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hutchgroup.elog.CertifyLogActivity;
import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.DailyLogBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;

import java.util.ArrayList;


public class UnCertifiedFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    public int selectedItemIndex = -1;
    ListView lvData;
    ArrayList<DailyLogBean> logList;
    UnCertifyLogAdapter adapter;
    int driverId = 0;
    //Button btnCertify;
    //Button btnSkip;

    ImageButton fabCertify;
    boolean isCertify;

    public UnCertifiedFragment() {
        // Required empty public constructor
    }

    String logIds = "";

    private void initialize(View view) {
        logIds = "";
        driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        lvData = (ListView) view.findViewById(R.id.lvData);
        fabCertify = (ImageButton) view.findViewById(R.id.fabCertify);
        fabCertify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCertify) {
                    for (DailyLogBean bean : logList) {
                        if (bean.getCertifyFG() == 1) {
                            logIds += bean.get_id() + ",";
                        }
                    }
                    if (logIds.isEmpty()) {
                        Utility.showAlertMsg("Please select Record!");
                        return;
                    }
                    logIds = logIds.substring(0, logIds.length() - 1);

                    certifyLogBook();

                } else {
                    if (mListener != null) {
                        mListener.onSkipCertify();
                    }
                }
            }
        });

        LogBind();
    }

    private void LogBind() {
        selectedItemIndex = -1;
        logList = DailyLogDB.getUncertifiedDailyLog(driverId);

        if (logList.isEmpty()) {
            fabCertify.setVisibility(View.GONE);
        }

        adapter = new UnCertifyLogAdapter(R.layout.uncertified_row_layout, logList);
        lvData.setAdapter(adapter);
    }

    public void certifyLogBook() {

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

                        if (DailyLogDB.DailyLogCertify("", driverId, logIds)) {
                            String[] logs = logIds.split(",");
                            // need to discuss about this should we enter multiple event related to multiple certification
                            for (int i = 0; i < logs.length; i++) {
                                int logId = Integer.parseInt(logs[i]);
                                int n = DailyLogDB.getCertifyCount(logId) + 1;
                                DailyLogDB.CertifyCountUpdate(logId, n);
                                if (n > 9)
                                    n = 9;
                                // to be discuss about event
                                //123 LogFile.write(UnCertifiedFragment.class.getName() + "::certifyLogBook: " + "Driver's " + n + "'th certification of a daily record" + " of driverId:" + driverId, LogFile.USER_INTERACTION, LogFile.DRIVEREVENT_LOG);
                                EventDB.EventCreate(Utility.getCurrentDateTime(), 4, n, "Driver's " + n + "'th certification of a daily record", 1, 1, logId, driverId, "");
                            }
                        }
                        LogBind();

                        if (mListener != null) {
                            mListener.onLogbookCertified();
                        }
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

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {

            LogBind();
        }
    }

    // TODO: Rename and change types and number of parameters
    public static UnCertifiedFragment newInstance() {
        //UnCertifiedFragment fragment = new UnCertifiedFragment();
        return new UnCertifiedFragment();


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        isCertify = false;
        View view = inflater.inflate(R.layout.fragment_un_certified, container, false);
        initialize(view);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
    }

    public void selectItem() {
        String logId = "";
        for (DailyLogBean bean : logList) {
            if (bean.getCertifyFG() == 1) {
                logId += bean.get_id() + ",";
            }
        }
        if (!logId.isEmpty()) {
            fabCertify.setImageResource(R.drawable.ic_fab_certify);
            isCertify = true;
        } else {
            fabCertify.setImageResource(R.drawable.ic_fab_skip);
            isCertify = false;
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        void onLogbookCertified();

        void onSkipCertify();
    }


    /**
     * Created by Deepak.Sharma on 1/19/2016.
     */
    public class UnCertifyLogAdapter extends ArrayAdapter<DailyLogBean> {
        ArrayList<DailyLogBean> data;

        public UnCertifyLogAdapter(int resource,
                                   ArrayList<DailyLogBean> data) {
            super(Utility.context, resource, data);
            this.data = data;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final ViewHolderItem viewHolder;
            final DailyLogBean bean = data.get(position);
            if (convertView == null || convertView.getTag() == null) {

                LayoutInflater inflater = (LayoutInflater) Utility.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(
                        R.layout.uncertified_row_layout, parent,
                        false);
                viewHolder = new ViewHolderItem();
                viewHolder.lRow = (LinearLayout) convertView.findViewById(R.id.lRow);
                viewHolder.swSerial = (ToggleButton) convertView.findViewById(R.id.swSerialNo);
                viewHolder.swSerial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            bean.setCertifyFG(1);
                            viewHolder.lRow.setBackground(getResources().getDrawable(R.drawable.list_row_checked));
                        } else {
                            viewHolder.lRow.setBackgroundResource(0);
                            bean.setCertifyFG(0);
                        }
                        selectItem();
                    }
                });
                viewHolder.tvLogDate = (TextView) convertView.findViewById(R.id.tvLogDate);
                viewHolder.tvOdometerReading = (TextView) convertView.findViewById(R.id.tvOdometerReading);
                viewHolder.tvLogData = (TextView) convertView.findViewById(R.id.tvLogData);
            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            int startOdometer = bean.getStartOdometerReading().isEmpty() ? 0 : Double.valueOf(bean.getStartOdometerReading()).intValue();
            int endOdometer = bean.getEndOdometerReading().isEmpty() ? 0 : Double.valueOf(bean.getEndOdometerReading()).intValue();
            int distance = endOdometer - startOdometer;
            viewHolder.swSerial.setTextOff((position + 1) + "");

            viewHolder.swSerial.setChecked(bean.getCertifyFG() == 1);
            viewHolder.tvLogDate.setText(bean.getLogDate());
            viewHolder.tvOdometerReading.setText("Odometer: " + bean.getStartOdometerReading() + " - " + bean.getEndOdometerReading());
            viewHolder.tvLogData.setText("Distance: " + distance + ", Trailer Id: " +
                    (bean.getTrailerId().isEmpty() ? "N/A" : bean.getTrailerId()) + ", Shipping Id: "
                    + (bean.getShippingId().isEmpty() ? "N/A" : bean.getShippingId()));
            return convertView;
        }
    }

    static class ViewHolderItem {
        TextView tvLogDate, tvOdometerReading, tvLogData;
        ToggleButton swSerial;
        LinearLayout lRow;
    }
}