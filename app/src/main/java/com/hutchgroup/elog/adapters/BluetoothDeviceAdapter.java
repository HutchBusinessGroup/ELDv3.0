package com.hutchgroup.elog.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;

import java.util.List;


public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {
    private ItemClickListener mListener;

    private final Context context;

    private List<BluetoothDevice> listDevices;

    private boolean isPairing;

    View selectedView;

    static class ViewHolder {
        public TextView tvBluetoothDevice;
        public TextView tvInfos;
    }

    public BluetoothDeviceAdapter(Context context, ItemClickListener listener, List<BluetoothDevice> values) {
        super(context, -1, values);
        this.mListener = listener;
        this.context = context;
        this.listDevices = values;
        isPairing = false;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        try {
            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.bluetooth_item, null);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.tvBluetoothDevice = (TextView) rowView.findViewById(R.id.tvBluetoothDevice);
                viewHolder.tvInfos = (TextView) rowView.findViewById(R.id.tvInfos);

                rowView.setTag(viewHolder);
            }

            // fill data
            Resources resources = context.getResources();

            final ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.tvBluetoothDevice.setText(listDevices.get(position).getName());
            holder.tvInfos.setVisibility(View.GONE);

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("BluetoothAdapter", "Item clicked!");
                    if (mListener != null) {
                        if (selectedView != null) {
                            ViewHolder h = (ViewHolder) selectedView.getTag();
                            if (h != null) {
                                h.tvInfos.setVisibility(View.GONE);
                            }
                        }
                        selectedView = v;
                        mListener.onItemClicked(listDevices.get(position));
                        holder.tvInfos.setVisibility(View.VISIBLE);
                    }
                }
            });
        } catch (Exception e) {
            LogFile.write(BluetoothPairedDeviceAdapter.class.getName() + "::getView Error: " + e.getMessage(), LogFile.SETUP, LogFile.ERROR_LOG);
        }

        return rowView;
    }

    public void updateParingText() {
        if (selectedView != null) {
            ViewHolder h = (ViewHolder) selectedView.getTag();
            if (h != null) {
                h.tvInfos.setVisibility(View.GONE);
            }
        }
    }

    public interface ItemClickListener {
        void onItemClicked(BluetoothDevice device);
    }
}
