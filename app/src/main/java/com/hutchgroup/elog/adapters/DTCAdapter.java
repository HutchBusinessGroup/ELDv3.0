package com.hutchgroup.elog.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.beans.DTCBean;
import com.hutchgroup.elog.common.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dev-1 on 11/4/2016.
 */

public class DTCAdapter extends ArrayAdapter<DTCBean> {
    private final Context context;

    private List<DTCBean> data;

    public DTCAdapter(Context context, int resource, ArrayList<DTCBean> values) {
        super(context, resource, values);
        this.context = context;
        this.data = values;
    }


    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        try {
            ViewHolderItem viewHolder;
            if (convertView == null || convertView.getTag() == null) {

                LayoutInflater inflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(
                        R.layout.dtc_item, parent,
                        false);
                viewHolder = new ViewHolderItem();
                viewHolder.tvSerialNo = (TextView) convertView.findViewById(R.id.tvSerialNo);
                viewHolder.tvSPNDescription = (TextView) convertView.findViewById(R.id.tvSPNDescription);
                viewHolder.tvFMIDescription = (TextView) convertView.findViewById(R.id.tvFMIDescription);
                viewHolder.tvOccurence = (TextView) convertView.findViewById(R.id.tvOccurence);
                viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
                viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            DTCBean bean = data.get(position);

            viewHolder.tvSerialNo.setText(position + 1);
            viewHolder.tvSPNDescription.setText(bean.getSpnDescription());
            viewHolder.tvFMIDescription.setText(bean.getFmiDescription());
            viewHolder.tvOccurence.setText(bean.getOccurence());

            try {
                viewHolder.tvDate.setText(new SimpleDateFormat("MMM dd,yyyy").format(Utility.sdf.parse(bean.getDateTime())));
                String format = "hh:mm a"; //12hr
                if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                    format = "HH:mm";
                }
                viewHolder.tvTime.setText(new SimpleDateFormat(format).format(Utility.sdf.parse(bean.getDateTime())));
            } catch (Exception exe) {

            }
        } catch (Exception exe) {
        }
        return convertView;
    }

    static class ViewHolderItem {
        TextView tvSerialNo, tvSPNDescription, tvFMIDescription, tvOccurence, tvTime, tvDate;

    }
}
