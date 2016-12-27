package com.hutchgroup.elog.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        AxleBean bean = data.get(position);
        if (bean.getAxlePosition() == 1) {
            viewHolder.tvBackTire.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tvBackTire.setVisibility(View.GONE);
        }

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
            tvPressure.setBackgroundResource(R.drawable.tpms_temp_bg_white);
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

        LinearLayout layoutSingleAxle, layoutDoubleAxle;
        TextView tvBackTire;

        public ViewHolder(View convertView) {
            super(convertView);
            tvBackTire = (TextView) convertView.findViewById(R.id.tvBackTire);

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
        }
    }
}
