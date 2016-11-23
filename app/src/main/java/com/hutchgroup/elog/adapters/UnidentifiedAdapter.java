package com.hutchgroup.elog.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.beans.EventBean;
import com.hutchgroup.elog.beans.SettingsBean;
import com.hutchgroup.elog.common.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Deepak.Sharma on 1/15/2016.
 */
public class UnidentifiedAdapter extends ArrayAdapter<EventBean> {
    ArrayList<EventBean> data;
    public UnidentifiedInterface mListener;

    public UnidentifiedAdapter(int resource,
                               ArrayList<EventBean> data) {
        super(Utility.context, resource, data);
        this.data = data;
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        final ViewHolderItem viewHolder;
        if (convertView == null || convertView.getTag() == null) {

            LayoutInflater inflater = (LayoutInflater) Utility.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(
                    R.layout.unidentified_row_layout, parent,
                    false);
            viewHolder = new ViewHolderItem();
            viewHolder.lRow = (LinearLayout) convertView.findViewById(R.id.lRow);
            viewHolder.swEventCode = (ToggleButton) convertView.findViewById(R.id.swEventCode);
            viewHolder.swEventCode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    data.get(position).setChecked(isChecked);
                    if (isChecked) {
                        viewHolder.lRow.setBackground(Utility.context.getResources().getDrawable(R.drawable.list_row_checked));
                    } else {
                        viewHolder.lRow.setBackgroundResource(0);
                    }

                    if (mListener != null) {
                        mListener.selectItem();
                    }
                }
            });

            viewHolder.tvOdometerReading = (TextView) convertView.findViewById(R.id.tvOdometerReading);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
            viewHolder.tvEventDescription = (TextView) convertView.findViewById(R.id.tvEventDescription);
            viewHolder.tvLocationDescription = (TextView) convertView.findViewById(R.id.tvLocationDescription);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        EventBean bean = data.get(position);
        String eventDate=Utility.ConverDateFormat(bean.getEventDateTime());
        viewHolder.swEventCode.setTextOff((position + 1) + "");
        viewHolder.swEventCode.setChecked(bean.getChecked());
        viewHolder.tvEventDescription.setText(bean.getEventCodeDescription());
        viewHolder.tvOdometerReading.setText("Vehicle Miles: " + bean.getOdometerReading());
        viewHolder.tvLocationDescription.setText(bean.getLocationDescription());
        try {
            viewHolder.tvDate.setText(new SimpleDateFormat("MMM dd,yyyy").format(Utility.parse(bean.getEventDateTime())));
            String format = "hh:mm a"; //12hr
            if (Utility._appSetting.getTimeFormat() == AppSettings.AppTimeFormat.HR24.ordinal()) {
                format = "HH:mm";
            }
            viewHolder.tvTime.setText(new SimpleDateFormat(format).format(Utility.parse(bean.getEventDateTime())));
        }
        catch (Exception exe)
        {

        }
        return convertView;
    }

    static class ViewHolderItem {
        TextView tvEventDescription, tvOdometerReading, tvLocationDescription, tvTime,tvDate;
        ToggleButton swEventCode;
        LinearLayout lRow;
    }

    public interface UnidentifiedInterface {
        void selectItem();
    }
}