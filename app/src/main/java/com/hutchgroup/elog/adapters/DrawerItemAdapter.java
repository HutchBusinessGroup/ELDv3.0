package com.hutchgroup.elog.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.DrawerItemBean;
import com.hutchgroup.elog.common.Utility;

import java.util.ArrayList;

/**
 * Created by Dev-1 on 5/5/2016.
 */
public class DrawerItemAdapter extends ArrayAdapter<DrawerItemBean> {

    ArrayList<DrawerItemBean> data;
    Context context;

    public DrawerItemAdapter(int resource,
                             ArrayList<DrawerItemBean> data) {
        super(Utility.context, resource, data);
        this.context = Utility.context;
        this.data = data;
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
                        R.layout.drawer_row_layout, parent,
                        false);
                viewHolder = new ViewHolderItem();
                viewHolder.tvItem = (TextView) convertView.findViewById(R.id.tvItem);
                viewHolder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            DrawerItemBean bean = data.get(position);

            viewHolder.tvItem.setText(bean.getItem());
            viewHolder.imgIcon.setImageResource(bean.getIcon());
        } catch (Exception exe) {
        }
        return convertView;
    }

    static class ViewHolderItem {
        TextView tvItem;
        ImageView imgIcon;

    }
}


