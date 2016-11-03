package com.hutchgroup.elog.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;


public class WelcomeSetupFragment extends Fragment implements View.OnClickListener {
    final String TAG = WelcomeSetupFragment.class.getName();

    private OnFragmentInteractionListener mListener;

    TextView tvVersion;
    ImageButton butGo;
    EditText edInstallerID;
    SharedPreferences prefs;
    AlertDialog alertDialog;

    public WelcomeSetupFragment() {

    }

    private void initialize(View view) {
        try {
            tvVersion = (TextView) view.findViewById(R.id.tvAppVersion);
            try {
                PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                String version = pInfo.versionName;
                tvVersion.setText("Version " + version);
            } catch (Exception e) {
                tvVersion.setVisibility(View.GONE);
            }

            butGo = (ImageButton) view.findViewById(R.id.butGo);
            butGo.setVisibility(View.GONE);
            butGo.setOnClickListener(this);
            edInstallerID = (EditText) view.findViewById(R.id.edInstallerID);
            edInstallerID.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    butGo.setVisibility(View.VISIBLE);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            edInstallerID.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                goToNextScreen();
                            }
                        }, 100);
                    }
                    return false;
                }
            });

        } catch (Exception e) {
            LogFile.write(WelcomeSetupFragment.class.getName() + "::initialize Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    public static WelcomeSetupFragment newInstance() {
        WelcomeSetupFragment fragment = new WelcomeSetupFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_setup_welcome_screen, container, false);
        prefs = getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE);

        initialize(view);

        return view;
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.butGo:
                    Utility.hideKeyboard(getActivity(), view);
                    goToNextScreen();

                    break;
            }
        } catch (Exception e) {
            LogFile.write(WelcomeSetupFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
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

        } catch (Exception e) {
            LogFile.write(WelcomeSetupFragment.class.getName() + "::onDetach Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }
    }

    private void goToNextScreen() {
        if (!edInstallerID.getText().toString().equals("")) {
            prefs.edit().putString("installer_id", edInstallerID.getText().toString()).commit();
            if (mListener != null) {
                mListener.onNextToWirelessConnectivity();
            }
        } else {
            if (getActivity() != null) {
                alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setTitle("E-Log");
                alertDialog.setIcon(R.drawable.ic_launcher);
                alertDialog.setMessage("Please enter your installer ID!");
                alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onNextToWirelessConnectivity();
    }
}
