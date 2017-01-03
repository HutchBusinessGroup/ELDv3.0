
package com.hutchgroup.elog.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class TrailerManageRecycleAdapter extends RecyclerView.Adapter<TrailerManageRecycleAdapter.ViewHolder> {
    ArrayList<AxleBean> data;

    public TrailerManageRecycleAdapter(ArrayList<AxleBean> data) {
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


        if (bean.isEmptyFg()) {
            viewHolder.layoutEmpty.setVisibility(View.VISIBLE);
            viewHolder.layoutSingleAxle.setVisibility(View.GONE);
            viewHolder.layoutDoubleAxle.setVisibility(View.GONE);
            viewHolder.tvBackTire.setVisibility(View.GONE);
            viewHolder.btnHook.setEnabled(bean.getAxlePosition() == 1);
            viewHolder.btnHook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListner != null) {
                        mListner.hook();
                    }
                }
            });

        } else {
            if (bean.getAxlePosition() == 1) {
                viewHolder.tvBackTire.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tvBackTire.setVisibility(View.GONE);
            }
            viewHolder.layoutEmpty.setVisibility(View.GONE);

            if (bean.isDoubleTireFg()) {
                viewHolder.layoutSingleAxle.setVisibility(View.GONE);
                viewHolder.layoutDoubleAxle.setVisibility(View.VISIBLE);

                setPressureWarning(viewHolder.imgTire1, bean.getPressure1(), bean.getHighPressure(), bean.getLowPressure());
                setPressureWarning(viewHolder.imgTire2, bean.getPressure2(), bean.getHighPressure(), bean.getLowPressure());
                setPressureWarning(viewHolder.imgTire3, bean.getPressure3(), bean.getHighPressure(), bean.getLowPressure());
                setPressureWarning(viewHolder.imgTire4, bean.getPressure4(), bean.getHighPressure(), bean.getLowPressure());

            } else {
                viewHolder.layoutSingleAxle.setVisibility(View.VISIBLE);
                viewHolder.layoutDoubleAxle.setVisibility(View.GONE);

                setPressureWarning(viewHolder.imgSingleTire1, bean.getPressure1(), bean.getHighPressure(), bean.getLowPressure());
                setPressureWarning(viewHolder.imgSingleTire2, bean.getPressure2(), bean.getHighPressure(), bean.getLowPressure());
            }
        }
    }

    private void setPressureWarning(ImageView imgTire, double pressure, double highPressure, double lowPressure) {
        if (pressure > highPressure || pressure < lowPressure) {
            imgTire.setImageResource(R.drawable.error_tire);
        } else {
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
        ImageView imgTire1, imgTire2, imgTire3, imgTire4;
        ImageView imgSingleTire1, imgSingleTire2;

        LinearLayout layoutSingleAxle, layoutDoubleAxle, layoutEmpty;
        TextView tvBackTire;
        Button btnHook;

        public ViewHolder(View convertView) {
            super(convertView);
            tvBackTire = (TextView) convertView.findViewById(R.id.tvBackTire);
            btnHook = (Button) convertView.findViewById(R.id.btnHook);
            imgTire1 = (ImageView) convertView.findViewById(R.id.imgTire1);
            imgTire2 = (ImageView) convertView.findViewById(R.id.imgTire2);
            imgTire3 = (ImageView) convertView.findViewById(R.id.imgTire3);
            imgTire4 = (ImageView) convertView.findViewById(R.id.imgTire4);


            imgSingleTire1 = (ImageView) convertView.findViewById(R.id.imgSingleTire1);
            imgSingleTire2 = (ImageView) convertView.findViewById(R.id.imgSingleTire2);


            layoutSingleAxle = (LinearLayout) convertView.findViewById(R.id.layoutSingleAxle);
            layoutDoubleAxle = (LinearLayout) convertView.findViewById(R.id.layoutDoubleAxle);
            layoutEmpty = (LinearLayout) convertView.findViewById(R.id.layoutEmpty);
        }
    }

    public static IHookTrailer mListner;

    public interface IHookTrailer {
        public void hook();
    }
}
