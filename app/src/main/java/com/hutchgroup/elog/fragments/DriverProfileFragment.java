package com.hutchgroup.elog.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.UserBean;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.UserDB;

public class DriverProfileFragment extends Fragment {
    TextView tvUserName, tvName, tvLicenseNo, tvJurisdiction, tvLicenseExpiry, tvExemptFg, tvSpecialCategory, tvEmail, tvMobileNo, tvDOTPassword;

    public DriverProfileFragment() {

    }


    public static DriverProfileFragment newInstance() {
        return new DriverProfileFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_profile, container, false);
        initialize(view);
        return view;
    }

    private void initialize(View view) {
        tvUserName = (TextView) view.findViewById(R.id.tvUserName);
        tvName = (TextView) view.findViewById(R.id.tvName);
        tvLicenseNo = (TextView) view.findViewById(R.id.tvLicenseNo);
        tvJurisdiction = (TextView) view.findViewById(R.id.tvJurisdiction);
        tvLicenseExpiry = (TextView) view.findViewById(R.id.tvLicenseExpiry);
        tvExemptFg = (TextView) view.findViewById(R.id.tvExemptFg);
        tvSpecialCategory = (TextView) view.findViewById(R.id.tvSpecialCategory); //0-none,1-PU,2-YM,3 Both
        tvEmail = (TextView) view.findViewById(R.id.tvEmail);
        tvMobileNo = (TextView) view.findViewById(R.id.tvMobileNo);
        tvDOTPassword = (TextView) view.findViewById(R.id.tvDOTPassword);
        UserBean user = UserDB.userInfoGet(Utility.onScreenUserId);

        tvUserName.setText(user.getUserName());
        tvName.setText(user.getFirstName() + " " + user.getLastName());
        tvLicenseNo.setText(user.getDrivingLicense());
        tvJurisdiction.setText(user.getDlIssueState());
        tvLicenseExpiry.setText(user.getLicenseExpiryDate());
        tvExemptFg.setText(user.getExemptELDUseFg() == 0 ? "No Exemption" : "Exempt Use");
        tvDOTPassword.setText(user.getDotPassword());
        String specialCategory;
        switch (user.getSpecialCategory()) {
            case "0":
                specialCategory = "None";
                break;
            case "1":
                specialCategory = "Authorized Personal Use of CMV";
                break;
            case "2":
                specialCategory = "Yard Move";
                break;
            case "3":
                specialCategory = "Authorized Personal Use of CMV, Yard Move";
                break;
            default:
                specialCategory = "None";
                break;
        }
        tvSpecialCategory.setText(specialCategory);
        tvEmail.setText(user.getEmailId());
        tvMobileNo.setText(user.getMobileNo());
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            //lvEvents.removeHeaderView(inforHeader);
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.fragment_driver_profile, null);

            ViewGroup viewGroup = (ViewGroup) getView();

            viewGroup.removeAllViews();
            viewGroup.addView(view);
            initialize(view);
        } catch (Exception e) {
        }
    }
}
