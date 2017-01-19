package com.hutchgroup.elog.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.VehicleInfoBean;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.Utility;

public class VehicleInfoFragment extends Fragment {

    TextView tvDate, tvUnitNo, tvVinNo, tvEngineNo, tvPlateNo, tvOdometerReading, tvEngineHours, tvFuelUsed, tvCoolantLevel, tvRPM, tvBoost, tvFuelPressure, tvFuelLevel, tvEngineOilLevel,
            tvCoolantTemperature, tvAirInletTemperature, tvBarometricPressure, tvEngineOilPressure, tvEngineLoad, tvLowWasherFluidLevel, tvSpeed, tvDefFuelLevel, tvIdleFuelUsed, tvEngineIdleHours, tvPTOHours;
    private OnFragmentInteractionListener mListener;
    TextView tvEnginePower;
    TextView tvFuelEconomy, tvBatteryVoltage;
    TextView tvPTOFuelUsed, tvCruiseSetSpeed;
    TextView tvBrakeApplicationCount, tvMaxRoadSpeed;
    TextView tvCruiseTime, tvAirSuspensionLoad;
    TextView tvTransmissionOilLevel, tvTransmissionGearNo;

    ImageView imgCruise, imgabspowerunit, imgabstrailer, imgDeratedEngine, imgseatbelt, imgdef, imgwaterinfuel;

    private void initialize(View view) {
        imgCruise = (ImageView) view.findViewById(R.id.imgCruise);
        imgabspowerunit = (ImageView) view.findViewById(R.id.imgabspowerunit);
        imgabstrailer = (ImageView) view.findViewById(R.id.imgabstrailer);
        imgDeratedEngine = (ImageView) view.findViewById(R.id.imgDeratedEngine);
        imgseatbelt = (ImageView) view.findViewById(R.id.imgseatbelt);
        imgdef = (ImageView) view.findViewById(R.id.imgdef);
        imgwaterinfuel = (ImageView) view.findViewById(R.id.imgwaterinfuel);

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
        tvDefFuelLevel = (TextView) view.findViewById(R.id.tvDefFuelLevel);
        tvIdleFuelUsed = (TextView) view.findViewById(R.id.tvIdleFuelUsed);
        tvEngineIdleHours = (TextView) view.findViewById(R.id.tvEngineIdleHours);
        tvPTOHours = (TextView) view.findViewById(R.id.tvPTOHours);

        tvEnginePower = (TextView) view.findViewById(R.id.tvEnginePower);
        tvBrakeApplicationCount = (TextView) view.findViewById(R.id.tvBrakeApplicationCount);
        tvMaxRoadSpeed = (TextView) view.findViewById(R.id.tvMaxRoadSpeed);
        tvCruiseTime = (TextView) view.findViewById(R.id.tvCruiseTime);
        tvAirSuspensionLoad = (TextView) view.findViewById(R.id.tvAirSuspensionLoad);
        tvTransmissionOilLevel = (TextView) view.findViewById(R.id.tvTransmissionOilLevel);
        tvTransmissionGearNo = (TextView) view.findViewById(R.id.tvTransmissionGearNo);
        tvPTOFuelUsed = (TextView) view.findViewById(R.id.tvPTOFuelUsed);
        tvCruiseSetSpeed = (TextView) view.findViewById(R.id.tvCruiseSetSpeed);
        tvFuelEconomy = (TextView) view.findViewById(R.id.tvFuelEconomy);
        tvBatteryVoltage = (TextView) view.findViewById(R.id.tvBatteryVoltage);

        tvUnitNo.setText(Utility.UnitNo);
        tvPlateNo.setText(Utility.PlateNo);
        tvVinNo.setText(Utility.VIN);
        tvEngineNo.setText(CanMessages._vehicleInfo.getEngineSerialNo());
        tvDate.setText(Utility.getStringCurrentDate());
        tvEnginePower.setText(hasNull(CanMessages._vehicleInfo.getEngineRatePower(), ""));
    }

    private static final String DEGREE = " \u00b0F";

    String hasNull(String value, String unit) {
        return value.equals("0") ? "N/A" : (value + " " + unit);
    }

    private void fillValue() {
        tvDate.setText(Utility.getStringCurrentDate());
        VehicleInfoBean obj = CanMessages._vehicleInfo;
        //imgCruise, imgabspowerunit, imgabstrailer, imgenginerated, imgseatbelt, imgdef, imgwaterinfuel;
        imgCruise.setImageResource(obj.getCruiseSetFg() == 1 ? R.drawable.ic_vehicleinfo_cruise_on : R.drawable.ic_vehicleinfo_cruise_off);
        imgabspowerunit.setImageResource(obj.getPowerUnitABSFg() == 1 ? R.drawable.ic_vehicleinfo_abs_powerunit_on : R.drawable.ic_vehicleinfo_abs_powerunit_off);
        imgabstrailer.setImageResource(obj.getTrailerABSFg() == 1 ? R.drawable.ic_vehicleinfo_abs_trailer_on : R.drawable.ic_vehicleinfo_abs_trailer_off);
        imgDeratedEngine.setImageResource(obj.getDerateFg() == 1 ? R.drawable.ic_vehicleinfo_engine_rated_on : R.drawable.ic_vehicleinfo_engine_rated_off);
        imgseatbelt.setImageResource(obj.getSeatBeltFg() == 1 ? R.drawable.ic_vehicleinfo_seatbelt_on : R.drawable.ic_vehicleinfo_seatbelt_off);
        imgdef.setImageResource(obj.getDEFTankLevelLow() == "1" ? R.drawable.ic_vehicleinfo_def_on : R.drawable.ic_vehicleinfo_def_off);
        imgwaterinfuel.setImageResource(obj.getWaterInFuelFg() == 1 ? R.drawable.ic_vehicleinfo_water_in_fuel_on : R.drawable.ic_vehicleinfo_water_in_fuel_off);

        tvOdometerReading.setText(hasNull(obj.getOdometerReading(), " Kms"));
        tvEngineHours.setText(hasNull(obj.getEngineHour(), " Hrs"));
        tvFuelUsed.setText(hasNull(obj.getFuelUsed(), " Ltrs."));
        tvCoolantLevel.setText(hasNull(obj.getCoolantLevel(), " %"));
        tvRPM.setText(hasNull(obj.getRPM(), ""));
        tvBoost.setText(hasNull(obj.getBoost(), " Psi"));

        tvFuelLevel.setText(hasNull(obj.getFuelLevel() + "", " %"));
        tvEngineOilLevel.setText(hasNull(obj.getEngineOilLevel(), " %"));
        tvCoolantTemperature.setText(hasNull(obj.getCoolantTemperature(), DEGREE));

        tvEngineLoad.setText(hasNull(obj.getEngineLoad(), ""));
        tvLowWasherFluidLevel.setText(hasNull(obj.getWasherFluidLevel(), " %"));
        tvSpeed.setText(hasNull(obj.getSpeed(), " Km/h"));
        tvDefFuelLevel.setText(hasNull(obj.getDEFTankLevel(), ""));
        tvIdleFuelUsed.setText(hasNull(obj.getIdleFuelUsed(), " Litres"));
        tvEngineIdleHours.setText(hasNull(obj.getIdleHours(), " Hrs"));
        tvPTOHours.setText(hasNull(obj.getPTOHours(), " Hrs"));

        // below fields need to be added in the web and android database and web service need to be created
        tvFuelPressure.setText(hasNull(obj.getFuelPressure(), " Psi"));
        tvAirInletTemperature.setText(hasNull(obj.getAirInletTemperature(), DEGREE));
        tvBarometricPressure.setText(hasNull(obj.getBarometricPressure(), " Psi"));
        tvEngineOilPressure.setText(hasNull(obj.getEngineOilPressure(), " Psi"));

        tvFuelEconomy.setText(hasNull(obj.getAverage(), " KM/L"));
        tvBatteryVoltage.setText(hasNull(obj.getBatteryVoltage(), " V"));
        tvPTOFuelUsed.setText(hasNull(obj.getPTOFuelUsed(), " Ltrs."));
        tvCruiseSetSpeed.setText(hasNull(obj.getCruiseSpeed(), " Km/h"));
        tvBrakeApplicationCount.setText(hasNull(obj.getBrakeApplication() + "", ""));
        tvMaxRoadSpeed.setText(hasNull(obj.getMaxRoadSpeed(), " Km/h"));
        tvCruiseTime.setText(hasNull(obj.getCuriseTime(), " Hrs."));
        tvAirSuspensionLoad.setText(hasNull(obj.getAirSuspension(), " Psi"));
        tvTransmissionOilLevel.setText(hasNull(obj.getTransmissionOilLevel(), " %"));
        tvTransmissionGearNo.setText(hasNull(obj.getTransmissionGear() + "", " "));

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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup viewGroup = (ViewGroup) getView();
            View view = inflater.inflate(R.layout.fragment_vehicle_info, viewGroup, false);
            viewGroup.removeAllViews();
            viewGroup.addView(view);
            initialize(view);

        } catch (Exception exe) {
        }
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
                        Thread.sleep(2000);
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
