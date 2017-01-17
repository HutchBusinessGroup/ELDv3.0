package com.hutchgroup.elog.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.VehicleInfoBean;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.Utility;

public class VehicleInfoFragment extends Fragment {

    TextView tvDate, tvUnitNo, tvVinNo, tvEngineNo, tvPlateNo, tvOdometerReading, tvEngineHours, tvFuelUsed, tvCoolantLevel, tvRPM, tvBoost, tvFuelPressure, tvFuelLevel, tvEngineOilLevel,
            tvCoolantTemperature, tvAirInletTemperature, tvBarometricPressure, tvEngineOilPressure, tvEngineLoad, tvLowWasherFluidLevel, tvSpeed, tvEPFFuelLevel, tvIdleFuelUsed, tvEngineIdleHours, tvPTOHours;
    private OnFragmentInteractionListener mListener;

    private void initialize(View view) {
        tvDate = (TextView) view.findViewById(R.id.tvDate);
        tvUnitNo = (TextView) view.findViewById(R.id.tvUnitNo);
        tvVinNo = (TextView) view.findViewById(R.id.tvVinNo);
        tvEngineNo = (TextView) view.findViewById(R.id.tvEngineNo);
        tvPlateNo = (TextView) view.findViewById(R.id.tvPlateNo);
        tvOdometerReading = (TextView) view.findViewById(R.id.tvOdometerReading);
        tvEngineHours = (TextView) view.findViewById(R.id.tvEngineHours);
        tvFuelUsed = (TextView) view.findViewById(R.id.tvFuelUsed);
        tvCoolantLevel = (TextView) view.findViewById(R.id.tvCoolantLevel);
        tvRPM = (TextView) view.findViewById(R.id.tvRPM);
        tvBoost = (TextView) view.findViewById(R.id.tvBoost);
        tvFuelPressure = (TextView) view.findViewById(R.id.tvFuelPressure);
        tvFuelLevel = (TextView) view.findViewById(R.id.tvFuelLevel);
        tvEngineOilLevel = (TextView) view.findViewById(R.id.tvEngineOilLevel);
        tvCoolantTemperature = (TextView) view.findViewById(R.id.tvCoolantTemperature);
        tvAirInletTemperature = (TextView) view.findViewById(R.id.tvAirInletTemperature);
        tvBarometricPressure = (TextView) view.findViewById(R.id.tvBarometricPressure);
        tvEngineOilPressure = (TextView) view.findViewById(R.id.tvEngineOilPressure);
        tvEngineLoad = (TextView) view.findViewById(R.id.tvEngineLoad);
        tvLowWasherFluidLevel = (TextView) view.findViewById(R.id.tvLowWasherFluidLevel);
        tvSpeed = (TextView) view.findViewById(R.id.tvSpeed);
        tvEPFFuelLevel = (TextView) view.findViewById(R.id.tvEPFFuelLevel);
        tvIdleFuelUsed = (TextView) view.findViewById(R.id.tvIdleFuelUsed);
        tvEngineIdleHours = (TextView) view.findViewById(R.id.tvEngineIdleHours);
        tvPTOHours = (TextView) view.findViewById(R.id.tvPTOHours);

        tvUnitNo.setText(Utility.UnitNo);
        tvPlateNo.setText(Utility.PlateNo);
        tvVinNo.setText(Utility.VIN);
        tvEngineNo.setText(CanMessages._vehicleInfo.getEngineSerialNo());
        tvDate.setText(Utility.getStringCurrentDate());
    }

    private static final String DEGREE = " \u00b0F";

    private void fillValue() {
        tvDate.setText(Utility.getStringCurrentDate());
        VehicleInfoBean obj = CanMessages._vehicleInfo;
        tvOdometerReading.setText(obj.getOdometerReading() + " Kms");
        tvEngineHours.setText(obj.getEngineHour() + " Hrs");
        tvFuelUsed.setText(obj.getFuelUsed() + " Ltrs.");
        tvCoolantLevel.setText(obj.getCoolantLevel() + " %");
        tvRPM.setText(obj.getRPM());
        tvBoost.setText(obj.getBoost() + " Psi");
        tvFuelPressure.setText("N/A");
        tvFuelLevel.setText(obj.getFuelLevel() + " %");
        tvEngineOilLevel.setText(obj.getEngineOilLevel());
        tvCoolantTemperature.setText(obj.getCoolantTemperature() + DEGREE);
        tvAirInletTemperature.setText("N/A");
        tvBarometricPressure.setText("N/A");
        tvEngineOilPressure.setText("N/A");
        tvEngineLoad.setText(obj.getEngineLoad());
        tvLowWasherFluidLevel.setText(obj.getWasherFluidLevel() + " %");
        tvSpeed.setText(obj.getSpeed() + "Km/h");
        tvEPFFuelLevel.setText("N/A");
        tvIdleFuelUsed.setText(obj.getIdleFuelUsed() + " Litres");
        tvEngineIdleHours.setText(obj.getIdleHours() + " Hrs");
        tvPTOHours.setText(obj.getPTOHours() + " Hrs");

    }

    public VehicleInfoFragment() {
        // Required empty public constructor
    }

    public static VehicleInfoFragment newInstance() {
        VehicleInfoFragment fragment = new VehicleInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_vehicle_info, container, false);
        initialize(view);
        startThread();
        return view;
    }

    private Thread thVehicleInfo;

    private void startThread() {
        if (thVehicleInfo != null) {
            thVehicleInfo.interrupt();
            thVehicleInfo = null;
        }

        thVehicleInfo = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (Thread.interrupted())
                            break;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fillValue();
                            }
                        });
                        Thread.sleep(5000);
                    } catch (Exception exe) {
                        break;
                    }
                }
            }
        });

        thVehicleInfo.setName("thVehicleInfo");
        thVehicleInfo.start();
    }

    private void stopThread() {
        if (thVehicleInfo != null) {
            thVehicleInfo.interrupt();
            thVehicleInfo = null;
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
        startThread();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
