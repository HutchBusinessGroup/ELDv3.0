package com.hutchgroup.elog.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.AxleBean;
import com.hutchgroup.elog.common.Utility;

import java.util.ArrayList;

/**
 * Created by Deepak on 12/23/2016.
 */

public class AxleAdapter extends ArrayAdapter<AxleBean> {
    ArrayList<AxleBean> data;

    public AxleAdapter(int resource, ArrayList<AxleBean> data) {
        super(Utility.context, resource, data);
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        final ViewHolderItem viewHolder;
        if (convertView == null || convertView.getTag() == null) {

            LayoutInflater inflater = (LayoutInflater) Utility.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(
                    R.layout.score_card_row_layout, parent,
                    false);
            viewHolder = new ViewHolderItem();
            viewHolder.tvPressure1 = (TextView) convertView.findViewById(R.id.tvPressure1);
            viewHolder.tvPressure2 = (TextView) convertView.findViewById(R.id.tvPressure2);
            viewHolder.tvPressure3 = (TextView) convertView.findViewById(R.id.tvPressure3);
            viewHolder.tvPressure4 = (TextView) convertView.findViewById(R.id.tvPressure4);


            viewHolder.tvTemperature1 = (TextView) convertView.findViewById(R.id.tvTemperature1);
            viewHolder.tvTemperature2 = (TextView) convertView.findViewById(R.id.tvTemperature2);
            viewHolder.tvTemperature3 = (TextView) convertView.findViewById(R.id.tvTemperature3);
            viewHolder.tvTemperature4 = (TextView) convertView.findViewById(R.id.tvTemperature4);


            viewHolder.tvSinglePressure1 = (TextView) convertView.findViewById(R.id.tvSinglePressure1);
            viewHolder.tvSinglePressure2 = (TextView) convertView.findViewById(R.id.tvSinglePressure2);


            viewHolder.tvSingleTemperature1 = (TextView) convertView.findViewById(R.id.tvSingleTemperature1);
            viewHolder.tvSingleTemperature2 = (TextView) convertView.findViewById(R.id.tvSingleTemperature2);


            viewHolder.imgTire1 = (ImageView) convertView.findViewById(R.id.imgTire1);
            viewHolder.imgTire2 = (ImageView) convertView.findViewById(R.id.imgTire2);
            viewHolder.imgTire3 = (ImageView) convertView.findViewById(R.id.imgTire3);
            viewHolder.imgTire4 = (ImageView) convertView.findViewById(R.id.imgTire4);


            viewHolder.imgSingleTire1 = (ImageView) convertView.findViewById(R.id.imgSingleTire1);
            viewHolder.imgSingleTire2 = (ImageView) convertView.findViewById(R.id.imgSingleTire2);


            viewHolder.layoutSingleAxle = (LinearLayout) convertView.findViewById(R.id.layoutSingleAxle);
            viewHolder.layoutDoubleAxle = (LinearLayout) convertView.findViewById(R.id.layoutDoubleAxle);


        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        AxleBean bean = data.get(position);
        if (bean.isDoubleTireFg()) {
            viewHolder.layoutSingleAxle.setVisibility(View.GONE);
            viewHolder.layoutDoubleAxle.setVisibility(View.VISIBLE);

            viewHolder.tvPressure1.setText(bean.getPressure1() + "");
            viewHolder.tvPressure2.setText(bean.getPressure2() + "");
            viewHolder.tvPressure3.setText(bean.getPressure3() + "");
            viewHolder.tvPressure4.setText(bean.getPressure4() + "");

            setPressureWarning(viewHolder.tvPressure1, bean.getPressure1(), bean.getHighPressure(), bean.getLowPressure());
            setPressureWarning(viewHolder.tvPressure2, bean.getPressure2(), bean.getHighPressure(), bean.getLowPressure());
            setPressureWarning(viewHolder.tvPressure3, bean.getPressure3(), bean.getHighPressure(), bean.getLowPressure());
            setPressureWarning(viewHolder.tvPressure4, bean.getPressure4(), bean.getHighPressure(), bean.getLowPressure());

            viewHolder.tvTemperature1.setText(bean.getTemperature1() + "");
            viewHolder.tvTemperature2.setText(bean.getTemperature2() + "");
            viewHolder.tvTemperature3.setText(bean.getTemperature3() + "");
            viewHolder.tvTemperature4.setText(bean.getTemperature4() + "");

            setTemperatureWarnings(viewHolder.tvTemperature1, bean.getTemperature1(), bean.getHighTemperature(), bean.getLowTemperature());
            setTemperatureWarnings(viewHolder.tvTemperature2, bean.getTemperature2(), bean.getHighTemperature(), bean.getLowTemperature());
            setTemperatureWarnings(viewHolder.tvTemperature3, bean.getTemperature3(), bean.getHighTemperature(), bean.getLowTemperature());
            setTemperatureWarnings(viewHolder.tvTemperature4, bean.getTemperature4(), bean.getHighTemperature(), bean.getLowTemperature());

        } else {
            viewHolder.layoutSingleAxle.setVisibility(View.VISIBLE);
            viewHolder.layoutDoubleAxle.setVisibility(View.GONE);


            viewHolder.tvSinglePressure1.setText(bean.getPressure1() + "");
            viewHolder.tvSinglePressure2.setText(bean.getPressure2() + "");
            setPressureWarning(viewHolder.tvSinglePressure1, bean.getPressure1(), bean.getHighPressure(), bean.getLowPressure());
            setPressureWarning(viewHolder.tvSinglePressure2, bean.getPressure2(), bean.getHighPressure(), bean.getLowPressure());

            viewHolder.tvSingleTemperature1.setText(bean.getTemperature1() + "");
            viewHolder.tvSingleTemperature2.setText(bean.getTemperature2() + "");

            setTemperatureWarnings(viewHolder.tvSingleTemperature1, bean.getTemperature1(), bean.getHighTemperature(), bean.getLowTemperature());
            setTemperatureWarnings(viewHolder.tvSingleTemperature2, bean.getTemperature2(), bean.getHighTemperature(), bean.getLowTemperature());


        }

        return convertView;
    }

    private void setTemperatureWarnings(TextView tvTemp, double temp, double highTemp, double lowTemp) {
        tvTemp.setText(temp + "° F");
        if (temp > highTemp && temp < lowTemp) {
            tvTemp.setTextAppearance(Utility.context, R.style.TPMSTemp_Red);
        } else {
            tvTemp.setTextAppearance(Utility.context, R.style.TPMSTemp_Green);
        }

    }

    private void setPressureWarning(TextView tvPressure, double pressure, double highPressure, double lowPressure) {
        tvPressure.setText(pressure + " psi");
        if (pressure > highPressure && pressure < lowPressure) {
            tvPressure.setBackgroundResource(R.drawable.tpms_value_bg_red);
        } else {
            tvPressure.setBackgroundResource(R.drawable.tpms_temp_bg_white);
        }
    }

    static class ViewHolderItem {
        TextView tvPressure1, tvPressure2, tvPressure3, tvPressure4, tvTemperature1, tvTemperature2, tvTemperature3, tvTemperature4;
        ImageView imgTire1, imgTire2, imgTire3, imgTire4;


        TextView tvSinglePressure1, tvSinglePressure2, tvSingleTemperature1, tvSingleTemperature2;
        ImageView imgSingleTire1, imgSingleTire2;

        LinearLayout layoutSingleAxle, layoutDoubleAxle;
    }
}
