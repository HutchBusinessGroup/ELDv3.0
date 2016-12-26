package com.hutchgroup.elog.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hutchgroup.elog.beans.AxleBean;

/**
 * Created by SAMSUNG on 26-12-2016.
 */

public class AxleRecycleAdapter extends RecyclerView.Adapter<AxleRecycleAdapter.ViewHolder> {


    public AxleRecycleAdapter() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return 0;
    }

    static class ViewHolderItem {
        TextView tvPressure1, tvPressure2, tvPressure3, tvPressure4, tvTemperature1, tvTemperature2, tvTemperature3, tvTemperature4;
        ImageView imgTire1, imgTire2, imgTire3, imgTire4;


        TextView tvSinglePressure1, tvSinglePressure2, tvSingleTemperature1, tvSingleTemperature2;
        ImageView imgSingleTire1, imgSingleTire2;

        LinearLayout layoutSingleAxle, layoutDoubleAxle;
        TextView tvBackTire;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;

        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }
}
