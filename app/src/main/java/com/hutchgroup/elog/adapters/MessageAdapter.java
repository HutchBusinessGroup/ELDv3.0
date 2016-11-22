package com.hutchgroup.elog.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.MessageBean;
import com.hutchgroup.elog.common.Utility;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Dev-1 on 6/29/2016.
 */
public class MessageAdapter extends ArrayAdapter<MessageBean> {
    ArrayList<MessageBean> data;


    public MessageAdapter(int resource,
                          ArrayList<MessageBean> data) {
        super(Utility.context, resource, data);
        this.data = data;
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        ViewHolderItem viewHolder;
        if (convertView == null || convertView.getTag() == null) {

            LayoutInflater inflater = (LayoutInflater) Utility.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(
                    R.layout.message_row_layout, parent,
                    false);
            viewHolder = new ViewHolderItem();
            viewHolder.tvMessage = (TextView) convertView.findViewById(R.id.tvMessage);
            viewHolder.tvMessageTime = (TextView) convertView.findViewById(R.id.tvMessageTime);
            viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
            viewHolder.imgStatus = (ImageView) convertView.findViewById(R.id.imgStatus);
            viewHolder.layoutMessage = (LinearLayout) convertView.findViewById(R.id.layoutMessage);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        MessageBean bean = data.get(position);

        String messageDate = Utility.getDate(bean.getMessageDate());
        String messageTime = Utility.getTimeHHMM(bean.getMessageDate());

        viewHolder.tvMessage.setText(bean.getMessage());
        viewHolder.tvMessageTime.setText(messageTime);

        if (messageDate.equals(Utility.getCurrentDate())) {
            viewHolder.tvDate.setText("Today");
        } else if (messageDate.equals(Utility.sdf.format(Utility.addDays(Utility.newDate(), -1))))
            viewHolder.tvDate.setText("Yesterday");
        else {
            viewHolder.tvDate.setText(messageDate);
        }
        if (position > 0) {
            String prevDate = Utility.getDate(data.get(position - 1).getMessageDate());
            if (prevDate.equals(messageDate)) {
                viewHolder.tvDate.setVisibility(View.GONE);
            } else {
                prevDate = messageDate;
                viewHolder.tvDate.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.tvDate.setVisibility(View.VISIBLE);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT);
        params.weight = 1.0f;
        if (bean.getCreatedById() == Utility.onScreenUserId) {
            viewHolder.layoutMessage.setBackgroundResource(R.drawable.send_message_bg);
            params.setMargins(144, 2, 8, 2);
            //viewHolder.tvMessage.setTextColor(R.color.);
            params.gravity = Gravity.RIGHT;
            if (bean.getReadFg() == 1) {
                viewHolder.imgStatus.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.layoutMessage.setBackgroundResource(R.drawable.receive_message_bg);
            params.setMargins(8, 2, 144, 2);
            params.gravity = Gravity.LEFT;

        }

        viewHolder.layoutMessage.setLayoutParams(params);
        return convertView;
    }

    static class ViewHolderItem {
        TextView tvMessage, tvMessageTime, tvDate;
        LinearLayout layoutMessage;
        ImageView imgStatus;
    }
}
