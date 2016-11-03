package com.hutchgroup.elog.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.beans.SettingsBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;


public class InputFragment extends Fragment implements View.OnClickListener {

    EditText edShippingNumber;
    //EditText edCommodity;
    EditText edTrailerNumber;

    Button butBack;
    Button butSave;

    int driverId;

    private OnFragmentInteractionListener mListener;

    public InputFragment() {
        // Required empty public constructor
    }

    private void initialize(View view) {
        try {

            edShippingNumber = (EditText) view.findViewById(R.id.edShippingNumber);
            /*edShippingNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    butSave.setEnabled(true);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });*/
            /*edShippingNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        Utility.hideKeyboard(getActivity(), v);
                    }
                }
            });*/
        /*edCommodity = (EditText) view.findViewById(R.id.edCommodity);
        edCommodity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                butSave.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });*/
            edTrailerNumber = (EditText) view.findViewById(R.id.edTrailerNumber);
           /* edTrailerNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    butSave.setEnabled(true);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });*/

           /* edTrailerNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        Utility.hideKeyboard(getActivity(), v);
                    }
                }
            });*/

         /*   edShippingNumber.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        // Perform action on Enter key press
                        edShippingNumber.clearFocus();
                        //etPassword.requestFocus();
                        (new Handler()).postDelayed(new Runnable() {
                            public void run() {
                                edTrailerNumber.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                                edTrailerNumber.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                            }
                        }, 100);

                        return true;
                    }
                    return false;
                }
            });

            edTrailerNumber.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                saveInformation();
                                if (mListener != null) {
                                    mListener.onInputSaved();
                                }
                            }
                        }, 100);
                    }
                    return false;
                }
            });*/

            butBack = (Button) view.findViewById(R.id.butBack);
            butBack.setOnClickListener(this);
            butSave = (Button) view.findViewById(R.id.butSave);
            butSave.setOnClickListener(this);

            //butSave.setEnabled(false);

            driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();

            if (Utility._appSetting.getCopyTrailer() == 1) {
                SharedPreferences sp = getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE);
                edShippingNumber.setText(Utility.ShippingNumber);
                //edCommodity.setText(sp.getString("commodity", ""));
                edTrailerNumber.setText(Utility.TrailerNumber);
            } else {
                if (Utility.user1.getAccountId() > 0 && Utility.user2.getAccountId() > 0) {
                    //has two users
                    if (Utility.user1.isOnScreenFg() && Utility.user1.isActive()) {
                        SharedPreferences sp = getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE);
                        edShippingNumber.setText(Utility.ShippingNumber);
                        //edCommodity.setText(sp.getString("commodity", ""));
                        edTrailerNumber.setText(Utility.TrailerNumber);
                    }
                    if (Utility.user2.isOnScreenFg() && Utility.user2.isActive()) {
                        SharedPreferences sp = getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE);
                        edShippingNumber.setText(Utility.ShippingNumber);
                        //edCommodity.setText(sp.getString("commodity", ""));
                        edTrailerNumber.setText(Utility.TrailerNumber);
                    }
                }
            }


            Utility.ShippingNumber = edShippingNumber.getText().toString();
            Utility.TrailerNumber = edTrailerNumber.getText().toString();
        } catch (Exception e) {
            LogFile.write(InputFragment.class.getName() + "::initialize Error:" + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    public static InputFragment newInstance() {
        return new InputFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_input_infos, container, false);

        //Intent intent = getActivity().getIntent();
        initialize(view);
        return view;
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.butBack:
                    //setResult(Activity.RESULT_CANCELED);
                    mListener.onInputFinished();
                    //finish();
                    break;
                case R.id.butSave:
                    saveInformation();
                    mListener.onInputSaved();
                    //finish();
                    break;
            }
            Utility.hideKeyboard(getActivity(), view);
        } catch (Exception e) {
            LogFile.write(InputFragment.class.getName() + "::onClick Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
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
        edShippingNumber.addTextChangedListener(null);
        edShippingNumber.setOnKeyListener(null);
        // edShippingNumber.setOnFocusChangeListener(null);

        edTrailerNumber.addTextChangedListener(null);
        // edTrailerNumber.setOnFocusChangeListener(null);
        edTrailerNumber.setOnKeyListener(null);
        butSave.setOnClickListener(null);
        butBack.setOnClickListener(null);

        mListener = null;
    }

    private void saveInformation() {
        SharedPreferences.Editor e = (getActivity().getSharedPreferences("HutchGroup", getActivity().getBaseContext().MODE_PRIVATE))
                .edit();
        e.putString("shipping_number", edShippingNumber.getText().toString());
        //e.putString("commodity", edCommodity.getText().toString());
        e.putString("trailer_number", edTrailerNumber.getText().toString());
        e.putInt("driverid", Utility.onScreenUserId);
        e.commit();

        Utility.ShippingNumber = edShippingNumber.getText().toString();
        Utility.TrailerNumber = edTrailerNumber.getText().toString();

        DailyLogDB.DailyLogCreate(driverId, edShippingNumber.getText().toString(), edTrailerNumber.getText().toString(), "");
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
        void onInputSaved();

        void onInputFinished();
    }
}
