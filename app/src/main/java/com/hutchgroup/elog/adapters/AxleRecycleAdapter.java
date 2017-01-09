package com.hutchgroup.elog.adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.AxleBean;
import com.hutchgroup.elog.common.Utility;

import java.util.ArrayList;

/**
 * Created by SAMSUNG on 26-12-2016.
 */

public class AxleRecycleAdapter extends RecyclerView.Adapter<AxleRecycleAdapter.ViewHolder> {
    ArrayList<AxleBean> data;

    public AxleRecycleAdapter(ArrayList<AxleBean> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tpms_row_layout, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final AxleBean bean = data.get(position);

        viewHolder.tvUnitNo.setText(bean.getUnitNo());
        viewHolder.tvPlateNo.setText(bean.getPlateNo());

        viewHolder.tvUnitNoNoBack.setText(bean.getUnitNo());
        viewHolder.tvPlateNoBack.setText(bean.getPlateNo());

        if (bean.isEmptyFg()) {
            viewHolder.layoutEmpty.setVisibility(View.VISIBLE);
            viewHolder.layoutSingleAxle.setVisibility(View.GONE);
            viewHolder.layoutDoubleAxle.setVisibility(View.GONE);
            viewHolder.layoutBackTire.setVisibility(View.GONE);
            if (bean.getAxlePosition() == 0) {
                viewHolder.btnHook.setText("UnHook");
                viewHolder.btnHook.setEnabled(true);
                viewHolder.btnHook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListner != null) {
                            final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
                            alertDialog.setCancelable(true);
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.setTitle("Unhook Confirmation");
                            alertDialog.setIcon(Utility.DIALOGBOX_ICON);
                            alertDialog.setMessage("Are you sure you want to unhook trailer?");
                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            int trailerId = bean.getVehicleId();
                                            mListner.unhook(trailerId);
                                        }
                                    });
                            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            alertDialog.cancel();
                                        }
                                    });
                            alertDialog.show();
                        }
                    }
                });
            } else {
                viewHolder.btnHook.setText("Hook");
                viewHolder.btnHook.setEnabled(bean.getAxlePosition() == 1);
                viewHolder.btnHook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListner != null) {
                            mListner.hook();
                        }
                    }
                });
            }

        } else {
            viewHolder.swUnhook.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(Utility.context).create();
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setTitle("Unhook Confirmation");
                    alertDialog.setIcon(Utility.DIALOGBOX_ICON);
                    alertDialog.setMessage("Are you sure you want to unhook trailer?");
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int trailerId = bean.getVehicleId();
                                    if (bean.isPowerUnitFg()) {
                                        trailerId = Integer.parseInt(Utility.hookedTrailers.get(1));
                                    }
                                    mListner.unhook(trailerId);
                                }
                            });
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.cancel();
                                }
                            });
                    alertDialog.show();
                }
            });

            if (bean.isPowerUnitFg()) {
                if (bean.getAxleNo() == 1) {
                    viewHolder.vSinglePowerUnit.setVisibility(View.VISIBLE);
                    viewHolder.vDoublePowerUnit.setVisibility(View.VISIBLE);
                  /*  viewHolder.layoutSingleRepeat.setLayoutParams(new LinearLayout.LayoutParams(224, 232));
                    viewHolder.layoutDoubleRepeat.setLayoutParams(new LinearLayout.LayoutParams(224, 232));*/

                    viewHolder.layoutSingleRepeat.setBackgroundResource(R.drawable.tpms_power_unit);
                    viewHolder.layoutDoubleRepeat.setBackgroundResource(R.drawable.tpms_power_unit);
                } else {
                    if (!bean.isFrontTireFg() && bean.getAxlePosition() == 1 && Utility.hookedTrailers.size() > 1) {
                        viewHolder.layoutHook.setVisibility(View.VISIBLE);
                    }
                    int background = Utility.hookedTrailers.size() > 1 ? R.drawable.tpms_trailer_axle : R.drawable.tpms_power_unit_axle;
                    viewHolder.layoutSingleRepeat.setBackgroundResource(background);
                    viewHolder.layoutDoubleRepeat.setBackgroundResource(background);
                }
            } else {
                if (bean.getAxleNo() == 1) {
                    if (data.size() > position - 1 && !data.get(position - 1).isPowerUnitFg())
                        viewHolder.layoutHook.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.layoutHook.setVisibility(View.GONE);
                }
                if (data.size() > position + 1) {
                    if (bean.getVehicleId() != data.get(position + 1).getVehicleId()) {
                        viewHolder.layoutLights.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.layoutLights.setVisibility(View.GONE);
                    }
                } else {
                    viewHolder.layoutLights.setVisibility(View.VISIBLE);
                }
                if (bean.getAxlePosition() == 1 && !bean.isFrontTireFg()) {
                    viewHolder.layoutBackTire.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.layoutBackTire.setVisibility(View.GONE);
                }
            }
            viewHolder.layoutEmpty.setVisibility(View.GONE);

            if (bean.isDoubleTireFg()) {
                viewHolder.layoutSingleAxle.setVisibility(View.GONE);
                viewHolder.layoutDoubleAxle.setVisibility(View.VISIBLE);

                setPressureWarning(viewHolder.tvPressure1, viewHolder.imgTire1, bean.getPressure1(), bean.getHighPressure(), bean.getLowPressure());
                setPressureWarning(viewHolder.tvPressure2, viewHolder.imgTire2, bean.getPressure2(), bean.getHighPressure(), bean.getLowPressure());
                setPressureWarning(viewHolder.tvPressure3, viewHolder.imgTire3, bean.getPressure3(), bean.getHighPressure(), bean.getLowPressure());
                setPressureWarning(viewHolder.tvPressure4, viewHolder.imgTire4, bean.getPressure4(), bean.getHighPressure(), bean.getLowPressure());

                setTemperatureWarnings(viewHolder.tvTemperature1, bean.getTemperature1(), bean.getHighTemperature(), bean.getLowTemperature());
                setTemperatureWarnings(viewHolder.tvTemperature2, bean.getTemperature2(), bean.getHighTemperature(), bean.getLowTemperature());
                setTemperatureWarnings(viewHolder.tvTemperature3, bean.getTemperature3(), bean.getHighTemperature(), bean.getLowTemperature());
                setTemperatureWarnings(viewHolder.tvTemperature4, bean.getTemperature4(), bean.getHighTemperature(), bean.getLowTemperature());

            } else {
                viewHolder.layoutSingleAxle.setVisibility(View.VISIBLE);
                viewHolder.layoutDoubleAxle.setVisibility(View.GONE);

                setPressureWarning(viewHolder.tvSinglePressure1, viewHolder.imgSingleTire1, bean.getPressure1(), bean.getHighPressure(), bean.getLowPressure());
                setPressureWarning(viewHolder.tvSinglePressure2, viewHolder.imgSingleTire2, bean.getPressure2(), bean.getHighPressure(), bean.getLowPressure());

                setTemperatureWarnings(viewHolder.tvSingleTemperature1, bean.getTemperature1(), bean.getHighTemperature(), bean.getLowTemperature());
                setTemperatureWarnings(viewHolder.tvSingleTemperature2, bean.getTemperature2(), bean.getHighTemperature(), bean.getLowTemperature());
            }
        }
    }


    private void setTemperatureWarnings(TextView tvTemp, double temp, double highTemp, double lowTemp) {
        tvTemp.setText(temp + "° F");
        if (temp > highTemp || temp < lowTemp) {
            tvTemp.setTextAppearance(Utility.context, R.style.TPMSTemp_Red);
        } else {
            tvTemp.setTextAppearance(Utility.context, R.style.TPMSTemp_Green);
        }

    }

    private void setPressureWarning(TextView tvPressure, ImageView imgTire, double pressure, double highPressure, double lowPressure) {
        tvPressure.setText(Math.round(pressure) + "");
        if (pressure > highPressure || pressure < lowPressure) {
            tvPressure.setBackgroundResource(R.drawable.tpms_value_bg_red);
            tvPressure.setTextAppearance(Utility.context, R.style.TPMSValue_White);
            imgTire.setImageResource(R.drawable.error_tire);
        } else {
            tvPressure.setBackgroundResource(R.drawable.tpms_value_bg_white);
            tvPressure.setTextAppearance(Utility.context, R.style.TPMSValue);
            imgTire.setImageResource(R.drawable.gray_tire);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPressure1, tvPressure2, tvPressure3, tvPressure4, tvTemperature1, tvTemperature2, tvTemperature3, tvTemperature4;
        ImageView imgTire1, imgTire2, imgTire3, imgTire4;


        TextView tvSinglePressure1, tvSinglePressure2, tvSingleTemperature1, tvSingleTemperature2;
        ImageView imgSingleTire1, imgSingleTire2;

        LinearLayout layoutSingleAxle, layoutDoubleAxle, layoutEmpty, layoutSingleRepeat, layoutDoubleRepeat, layoutHook;
        LinearLayout layoutBackTire, layoutLights;
        View vSinglePowerUnit, vDoublePowerUnit;
        CheckBox swUnhook;
        Button btnHook;
        TextView tvUnitNo, tvPlateNo, tvPlateNoBack, tvUnitNoNoBack;

        public ViewHolder(View convertView) {
            super(convertView);
            layoutBackTire = (LinearLayout) convertView.findViewById(R.id.layoutBackTire);
            layoutLights = (LinearLayout) convertView.findViewById(R.id.layoutLights);
            vSinglePowerUnit = convertView.findViewById(R.id.vSinglePowerUnit);
            vDoublePowerUnit = convertView.findViewById(R.id.vDoublePowerUnit);
            btnHook = (Button) convertView.findViewById(R.id.btnHook);
            swUnhook = (CheckBox) convertView.findViewById(R.id.swUnhook);

            tvUnitNo = (TextView) convertView.findViewById(R.id.tvUnitNo);
            tvPlateNo = (TextView) convertView.findViewById(R.id.tvPlateNo);

            tvUnitNoNoBack = (TextView) convertView.findViewById(R.id.tvUnitNoNoBack);
            tvPlateNoBack = (TextView) convertView.findViewById(R.id.tvPlateNoBack);

            tvPressure1 = (TextView) convertView.findViewById(R.id.tvPressure1);
            tvPressure2 = (TextView) convertView.findViewById(R.id.tvPressure2);
            tvPressure3 = (TextView) convertView.findViewById(R.id.tvPressure3);
            tvPressure4 = (TextView) convertView.findViewById(R.id.tvPressure4);


            tvTemperature1 = (TextView) convertView.findViewById(R.id.tvTemperature1);
            tvTemperature2 = (TextView) convertView.findViewById(R.id.tvTemperature2);
            tvTemperature3 = (TextView) convertView.findViewById(R.id.tvTemperature3);
            tvTemperature4 = (TextView) convertView.findViewById(R.id.tvTemperature4);


            tvSinglePressure1 = (TextView) convertView.findViewById(R.id.tvSinglePressure1);
            tvSinglePressure2 = (TextView) convertView.findViewById(R.id.tvSinglePressure2);


            tvSingleTemperature1 = (TextView) convertView.findViewById(R.id.tvSingleTemperature1);
            tvSingleTemperature2 = (TextView) convertView.findViewById(R.id.tvSingleTemperature2);


            imgTire1 = (ImageView) convertView.findViewById(R.id.imgTire1);
            imgTire2 = (ImageView) convertView.findViewById(R.id.imgTire2);
            imgTire3 = (ImageView) convertView.findViewById(R.id.imgTire3);
            imgTire4 = (ImageView) convertView.findViewById(R.id.imgTire4);


            imgSingleTire1 = (ImageView) convertView.findViewById(R.id.imgSingleTire1);
            imgSingleTire2 = (ImageView) convertView.findViewById(R.id.imgSingleTire2);


            layoutSingleAxle = (LinearLayout) convertView.findViewById(R.id.layoutSingleAxle);
            layoutDoubleAxle = (LinearLayout) convertView.findViewById(R.id.layoutDoubleAxle);
            layoutEmpty = (LinearLayout) convertView.findViewById(R.id.layoutEmpty);

            layoutSingleRepeat = (LinearLayout) convertView.findViewById(R.id.layoutSingleRepeat);
            layoutDoubleRepeat = (LinearLayout) convertView.findViewById(R.id.layoutDoubleRepeat);
            layoutHook = (LinearLayout) convertView.findViewById(R.id.layoutHook);
        }
    }

    public static IHookTrailer mListner;

    public interface IHookTrailer {
        void hook();

        void unhook(int trailerId);
    }
}
