package com.hutchgroup.elog.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.VehicleBean;

import java.util.ArrayList;

/**
 * Created by SAMSUNG on 29-12-2016.
 */

public class TrailerRecycleAdapter extends RecyclerView.Adapter<TrailerRecycleAdapter.ViewHolder> implements View.OnClickListener {

    private ArrayList<VehicleBean> data;

    public TrailerRecycleAdapter(ArrayList<VehicleBean> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.trailer_row_layout, parent, false);
        itemView.setOnClickListener(this);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VehicleBean bean = data.get(position);
        holder.tvUnitNo.setText("Unit: " + bean.getUnitNo());
        holder.tvPlateNo.setText("Plate No: " + bean.getPlateNo());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onClick(View v) {
        if (mListner != null) {
            mListner.onItemClick(v);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUnitNo, tvPlateNo;

        public ViewHolder(View view) {
            super(view);
            tvUnitNo = (TextView) view.findViewById(R.id.tvUnitNo);
            tvPlateNo = (TextView) view.findViewById(R.id.tvPlateNo);
        }
    }

    public static IViewHolder mListner;

    public static interface IViewHolder {
        public void onItemClick(View view);
    }
}
